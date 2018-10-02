package org.eclipse.swt.tests.junit;

import static org.eclipse.swt.browser.LocationListener.changedAdapter;
import static org.eclipse.swt.browser.LocationListener.changingAdapter;
import static org.eclipse.swt.browser.ProgressListener.completedAdapter;
import static org.eclipse.swt.browser.VisibilityWindowListener.showAdapter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowAdapter;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.chromium.Browser;
import org.eclipse.swt.chromium.BrowserFunction;
import org.eclipse.swt.chromium.OpenWindowListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.tests.junit.SwtTestUtil;
import org.eclipse.swt.tests.junit.Test_org_eclipse_swt_widgets_Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Automated Test Suite for class org.eclipse.swt.browser.Browser
 *
 * @see org.eclipse.swt.browser.Browser
 */
public class Test_org_eclipse_swt_chromium_Browser extends Test_org_eclipse_swt_widgets_Composite {

	// CONFIG
	/** This forces tests to display the shell/browser for a brief moment. Useful to see what's going on with broken jUnits */
	boolean debug_show_browser = false; // true to display browser.
	int     debug_show_browser_timeout_seconds = 2; // if above set to true, then how long should the browser be shown for.
													// This is independent of whether test passes or fails.

	boolean debug_verbose_output = false;

	int secondsToWaitTillFail; // configured in setUp() to allow individual tests to override this.
	// CONFIG END

	@Rule
	public TestName name = new TestName();

	Browser browser;
	boolean isWebkit1 = false;
	boolean isWebkit2 = false;
	boolean isChromium = false;

	static int[] webkitGtkVersionInts = new int[3];

	/** Accumiliate logs, print only if test case fails. Cleared for each test case. */
	StringBuilder testLog;
	private void testLogAppend(String msg) {
		testLog.append("  " + msg + "\n");
	}


@Override
@Before
public void setUp() {
	super.setUp();
	secondsToWaitTillFail = Math.max(15, debug_show_browser_timeout_seconds);

	// If webkit crashes, it's very hard to tell which jUnit caused the JVM crash.
	// To get around this, we print each test's name and if there is a crash, it will be printed right after.
	// This is kept for future use as sometimes crashes can appear out of the blue with no changes in SWT code.
	// E.g an upgrade from WebkitGtk2.16 to WebkitGtk2.18 caused random crashes because dispose logic was changed.
	System.out.println("Running Test_org_eclipse_swt_browser_Browser#" + name.getMethodName());

	shell.setLayout(new FillLayout());
	browser = new Browser(shell, SWT.NONE);

	String shellTitle = name.getMethodName();
	if (SwtTestUtil.isGTK && browser.getBrowserType().equals("chromium")) {
	    isChromium = true;
	}
    if (SwtTestUtil.isGTK && browser.getBrowserType().equals("webkit")) {

		// Note, webkitGtk version is only available once Browser is instantiated.
		String webkitGtkVersionStr = System.getProperty("org.eclipse.swt.internal.webkitgtk.version"); //$NON-NLS-1$

		shellTitle = shellTitle + " Webkit version: " + webkitGtkVersionStr;

		String[] webkitGtkVersionStrParts = webkitGtkVersionStr.split("\\.");
		for (int i = 0; i < 3; i++) {
			webkitGtkVersionInts[i] = Integer.parseInt(webkitGtkVersionStrParts[i]);
		}

		// webkitgtk 2.5 and onwards uses webkit2.
		if (webkitGtkVersionInts[0] == 1 || (webkitGtkVersionInts[0] == 2 && webkitGtkVersionInts[1] <= 4)) {
			isWebkit1 = true;
		} else if (webkitGtkVersionInts[0] == 2 && webkitGtkVersionInts[1] > 4) {
			isWebkit2 = true;
		}
	}
	shell.setText(shellTitle);
	setWidget(browser); // For browser to occupy the whole shell, not just half of it.

	testLog = new StringBuilder("\nTest log:\n");
}


/**
 * Test that if Browser is constructed with the parent being "null", Browser throws an exception.
 */
@Override
@Test(expected = IllegalArgumentException.class)
public void test_ConstructorLorg_eclipse_swt_widgets_CompositeI() {
	Browser browser = new Browser(shell, SWT.NONE);
	browser.dispose();
	browser = new Browser(shell, SWT.BORDER);
	// System.out.println("Test_org_eclipse_swt_browser_Browser#test_Constructor*#getBrowserType(): " + browser.getBrowserType());
	browser.dispose();
	browser = new Browser(null, SWT.NONE); // Should throw.
}


@Test
public void test_evalute_Cookies () {
	final AtomicBoolean loaded = new AtomicBoolean(false);
	browser.addProgressListener(ProgressListener.completedAdapter(event -> loaded.set(true)));

	// Using JavaScript Cookie API on local (file) URL gives DOM Exception 18
	browser.setUrl("http://www.eclipse.org/swt");
	waitForPassCondition(loaded::get);

	// Set the cookies
	// document.cookie behaves different from other global vars
	browser.evaluate("document.cookie = \"cookie1=value1\";");
	browser.evaluate("document.cookie = \"cookie2=value2\";");

	// Retrieve entire cookie store
	String res = (String) browser.evaluate("return document.cookie;");

	assertTrue(!res.isEmpty());
}

@Test
public void test_ClearAllSessionCookies () {
	if (isWebkit2) {
		// clearSessions will only work for Webkit2 when >= 2.16
		assumeTrue(webkitGtkVersionInts[1] >= 16);
	}

	final AtomicBoolean loaded = new AtomicBoolean(false);
	browser.addProgressListener(ProgressListener.completedAdapter(event -> loaded.set(true)));

	// Using JavaScript Cookie API on local (file) URL gives DOM Exception 18
	browser.setUrl("http://www.eclipse.org/swt");
	waitForPassCondition(loaded::get);

	// Set the cookies
	if (isWebkit2) { // TODO: Remove this once Webkit2 Cookie port complete
		browser.evaluate("document.cookie = \"cookie1=value1\";");
		browser.evaluate("document.cookie = \"cookie2=value2\";");
	} else {
		Browser.setCookie("cookie1=value1", "http://www.eclipse.org/swt");
		Browser.setCookie("cookie2=value2", "http://www.eclipse.org/swt");
	}

	// Get the cookies
	String v1, v2;
	if (isWebkit2) { // TODO: Remove this once Webkit2 Cookie port complete
		v1 = (String) browser.evaluate(toCookieEvalString("cookie1"));
		v2 = (String) browser.evaluate(toCookieEvalString("cookie2"));
	} else {
		v1 = Browser.getCookie("cookie1", "http://www.eclipse.org/swt");
		v2 = Browser.getCookie("cookie2", "http://www.eclipse.org/swt");
	}
	assertEquals("value1", v1);
	assertEquals("value2", v2);

	Browser.clearSessions();

	// Should be empty
	String e1, e2;
	if (isWebkit2) { // TODO: Remove this once Webkit2 Cookie port complete
		e1 = (String) browser.evaluate(toCookieEvalString("cookie1"));
		e2 = (String) browser.evaluate(toCookieEvalString("cookie2"));
	} else {
		e1 = Browser.getCookie("cookie1", "http://www.eclipse.org/swt");
		e2 = Browser.getCookie("cookie2", "http://www.eclipse.org/swt");
	}
	assertTrue(e1 == null || e1.isEmpty());
	assertTrue(e2 == null || e2.isEmpty());
}

/**
 * TODO: Remove this once Webkit2 Cookie port complete
 */
private String toCookieEvalString (String key) {
	return  "var name = \"" + key + "=\";\n" +
			"    var decodedCookie = decodeURIComponent(document.cookie);\n" +
			"    var ca = decodedCookie.split(';');\n" +
			"    for(var i = 0; i < ca.length; i++) {\n" +
			"        var c = ca[i];\n" +
			"        while (c.charAt(0) == ' ') {\n" +
			"            c = c.substring(1);\n" +
			"        }\n" +
			"        if (c.indexOf(name) == 0) {\n" +
			"            return c.substring(name.length, c.length);\n" +
			"        }\n" +
			"    }\n" +
			"    return \"\";";
}

@Test
public void test_get_set_Cookies() {
	assumeFalse("Not implemented on webkit2 yet. Bug 522181.", isWebkit2);

	final AtomicBoolean loaded = new AtomicBoolean(false);
	browser.addProgressListener(ProgressListener.completedAdapter(event -> loaded.set(true)));

	// Using JavaScript Cookie API on local (file) URL gives DOM Exception 18
	browser.setUrl("http://www.eclipse.org/swt");
	waitForPassCondition(loaded::get);

	// Set the cookies
	Browser.setCookie("cookie1=value1", "http://www.eclipse.org/swt");
	Browser.setCookie("cookie2=value2", "http://www.eclipse.org/swt");

	// Get the cookies
	String v1 = Browser.getCookie("cookie1", "http://www.eclipse.org/swt");
	assertEquals("value1", v1);
	String v2 = Browser.getCookie("cookie2", "http://www.eclipse.org/swt");
	assertEquals("value2", v2);
}

@Override
@Test
public void test_getChildren() {
	// Win32's Browser is a special case. It has 1 child by default, the OleFrame.
	// See Bug 499387 and Bug 511874
	if (SwtTestUtil.isWindows) {
		int childCount = composite.getChildren().length;
		String msg = "Browser on Win32 is a special case, the first child is an OleFrame (ActiveX control). Actual child count is: " + childCount;
		assertTrue(msg, childCount == 1);
	} else {
		super.test_getChildren();
	}
}

@Test
public void test_CloseWindowListener_closeShell() {
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	Browser browser = new Browser(shell, SWT.NONE);
	browser.addCloseWindowListener(event -> {}); // shouldn't throw
	shell.close();
}

@Test(expected = IllegalArgumentException.class)
public void test_CloseWindowListener_addWithNullArg() {
	browser.addCloseWindowListener(null);
}

@Test(expected = IllegalArgumentException.class)
public void test_CloseWindowListener_removeWithNullArg() {
	browser.removeCloseWindowListener(null);
}

@Test
public void test_CloseWindowListener_addAndRemove () {
	CloseWindowListener listener = event -> {};
	for (int i = 0; i < 100; i++) browser.addCloseWindowListener(listener);
	for (int i = 0; i < 100; i++) browser.removeCloseWindowListener(listener);
}

@Test
public void test_CloseWindowListener_close () {
	AtomicBoolean browserCloseListenerFired = new AtomicBoolean(false);
	browser.addCloseWindowListener(	e -> {
		disposedIntentionally= true;
		browserCloseListenerFired.set(true);
	});
	browser.setText("<script language='JavaScript'>window.close()</script>");
	shell.open();
	boolean passed = waitForPassCondition(browserCloseListenerFired::get);
	assertTrue("Test timed out.", passed);
}

@Test
public void test_LocationListener_adapter_closeShell() {
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	Browser browser = new Browser(shell, SWT.NONE);
	LocationAdapter adapter = new LocationAdapter() {};
	browser.addLocationListener(adapter); // shouldn't throw
	shell.close();
}

@Test(expected = IllegalArgumentException.class)
public void test_LocationListener_addWithNullArg() {
	browser.addLocationListener(null);
}

@Test(expected = IllegalArgumentException.class)
public void test_LocationListener_removeWithNullArg() {
	browser.removeLocationListener(null);
}

@Test
public void test_LocationListener_addAndRemove() {
	LocationListener listener = new LocationListener() {
		@Override
		public void changed(LocationEvent event) {
		}
		@Override
		public void changing(LocationEvent event) {
		}
	};
	for (int i = 0; i < 100; i++) browser.addLocationListener(listener);
	for (int i = 0; i < 100; i++) browser.removeLocationListener(listener);
}

@Test
public void test_LocationListener_changing() {
	AtomicBoolean changingFired = new AtomicBoolean(false);
	browser.addLocationListener(changingAdapter(e -> changingFired.set(true)));
	shell.open();
	if (isChromium) {
	    browser.setUrl("about:version");
	} else { // Chromium cannot fire changing event for setText
	    browser.setText("Hello world");
	}
	boolean passed = waitForPassCondition(changingFired::get);
	assertTrue("LocationListener.changing() event was never fixed", passed);
}
@Test
public void test_LocationListener_changed() {
	AtomicBoolean changedFired = new AtomicBoolean(false);
	browser.addLocationListener(changedAdapter(e ->	changedFired.set(true)));
	shell.open();
	browser.setText("Hello world");
	boolean passed = waitForPassCondition(changedFired::get);
	assertTrue("LocationListener.changing() event was never fixed", passed);
}
@Test
public void test_LocationListener_changingAndOnlyThenChanged() {
	// Test proper order of events.
	// Check that 'changed' is only fired after 'changing' has fired at least once.
	AtomicBoolean changingFired = new AtomicBoolean(false);
	AtomicBoolean changedFired = new AtomicBoolean(false);
	AtomicBoolean changedFiredTooEarly = new AtomicBoolean(false);
	AtomicBoolean finished = new AtomicBoolean(false);

	browser.addLocationListener(new LocationListener() {
		@Override
		public void changing(LocationEvent event) { // Multiple changing events can occur during a load.
				changingFired.set(true);
		}
		@Override
		public void changed(LocationEvent event) {
			if (!changingFired.get())
				changedFiredTooEarly.set(true);

			changedFired.set(true);
			finished.set(true);
		}
	});
	shell.open();
	if (isChromium) {
	    browser.setUrl("about:version");
	} else { // Chromium cannot fire changing event for setText
	    browser.setText("Hello world");
	}
    waitForPassCondition(finished::get);

	if (finished.get() && changingFired.get() && changedFired.get() && !changedFiredTooEarly.get()) {
		return; // pass
	} else if (!finished.get()) {
		fail("Test timed out. 'changed()' never fired");
	} else {
		if (changedFiredTooEarly.get())
			fail("changed() was fired before changing(). Wrong signal order");
		else if (!changingFired.get())
			fail("changing() was never fired");
		else  {
			fail("LocationListener test failed. changing():" + changingFired.get()
			+ "  changed():" + changedFired.get() + " changedFiredTooEarly:" + changedFiredTooEarly.get());
		}
	}
}

@Test
public void test_LocationListener_then_ProgressListener() {
	AtomicBoolean locationChanged = new AtomicBoolean(false);
	AtomicBoolean progressChanged = new AtomicBoolean(false);
	AtomicBoolean progressChangedAfterLocationChanged = new AtomicBoolean(false);

	browser.addLocationListener(changedAdapter(event ->	locationChanged.set(true)));

	browser.addProgressListener(completedAdapter(event -> {
		if (locationChanged.get()) {
			progressChangedAfterLocationChanged.set(true);
		}
		progressChanged.set(true);
	}));

	shell.open();
	browser.setText("Hello world");

	waitForPassCondition(progressChanged::get);
	String errorMsg = "\nUnexpected listener states. Expecting true for all, but have:\n"
			+ "Location changed: " + locationChanged.get() + "\n"
			+ "ProgressChangedAfterLocationChanged: " + progressChangedAfterLocationChanged.get() + "\n"
			+ "progressChanged: " + progressChanged.get();

	assertTrue(errorMsg, progressChangedAfterLocationChanged.get());
}

@Test
/**
 * "event.doit = false" in Location.changing() should stop 'Loction.changed & progress.completed' from getting fired.
 */
public void test_LocationListener_ProgressListener_cancledLoad () {

	AtomicBoolean locationChanging = new AtomicBoolean(false);
	AtomicBoolean unexpectedLocationChanged = new AtomicBoolean(false);
	AtomicBoolean unexpectedProgressCompleted = new AtomicBoolean(false);

	browser.addLocationListener(new LocationListener() {
		@Override
		public void changing(LocationEvent event) {
			event.doit = false;
			locationChanging.set(true);
		}
		@Override
		public void changed(LocationEvent event) {
			if (event.location.length() != 0) { // See footnote 1
				unexpectedLocationChanged.set(true);
			}
		}
	});

	browser.addProgressListener(completedAdapter(event -> {
		String location = browser.getUrl();
		if (location.length() != 0) { // See footnote 1
			unexpectedProgressCompleted.set(true);
		}
	}));
	shell.open();
	if (isChromium) {
	    browser.setUrl("about:version");
	} else { // Chromium cannot fire changing event for setText
	    browser.setText("You should not see this message.");
	}

	// We must wait for events *not* to fire.
	// On Gtk, Quadcore (Intel i7-4870HQ pci-e SSD, all events fire after ~80ms.
	// For stability, wait 1000 ms.
	waitForMilliseconds(1000);

	boolean passed = locationChanging.get() && !unexpectedLocationChanged.get() && !unexpectedProgressCompleted.get();
	String errMsg = "\nUnexpected event fired. \n"
			+ "LocationChanging (should be true): " + locationChanging.get() + "\n"
			+ "LocationChanged unexpectedly (should be false): " + unexpectedLocationChanged.get() + "\n"
			+ "ProgressChanged unexpectedly (should be false): " + unexpectedProgressCompleted.get() + "\n";


	assertTrue(errMsg, passed);

	/* FOOTNOTE 1
	 *
	 * Feature on Internet Explorer. If there is no current location, IE still fires a DocumentComplete
	 * following the BeforeNavigate2 cancel event. This DocumentComplete event contains an empty URL
	 * since the URL in BeforeNavigate2 was correctly cancelled.
	 * The test considers it is OK to send a Location.changed and a Progress.completed events after
	 * a Location.changing cancel true - at the condition that the current location is empty,
	 * otherwise it is considered that the location was not successfully cancelled.
	 */
}


@Test
/** Ensue that only one changed and one completed event are fired for url changes */
public void test_LocationListener_ProgressListener_noExtraEvents() {
	AtomicInteger changedCount = new AtomicInteger(0);
	AtomicInteger completedCount = new AtomicInteger(0);

	browser.addLocationListener(changedAdapter(e ->	changedCount.incrementAndGet()));

	browser.addProgressListener(completedAdapter(e -> completedCount.incrementAndGet()));

	shell.open();
	browser.setText("Hello world");

	// We have to wait to check that no extra events are fired.
	// On Gtk, Quad Core, pcie this takes 80 ms. ~1000ms for stability.
	waitForMilliseconds(600);
	boolean passed = changedCount.get() == 1 && completedCount.get() == 1;
	String errorMsg = "\nIncorrect event sequences. Events missing or too many fired:"
			+ "\nExpected one of each, but received:"
			+ "\nChanged count: " + changedCount.get()
			+ "\nCompleted count: " + completedCount.get();
	assertTrue(errorMsg, passed);
}

@Test
public void test_OpenWindowListener_closeShell() {
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	Browser browser = new Browser(shell, SWT.NONE);
	browser.addOpenWindowListener(event -> {});
	shell.close();
}

@Test(expected = IllegalArgumentException.class)
public void test_OpenWindowListener_addWithNulArg() {
	browser.addOpenWindowListener(null);
}

@Test(expected = IllegalArgumentException.class)
public void test_OpenWindowListener_removeWithNullArg() {
	browser.removeOpenWindowListener(null);
}

@Test
public void test_OpenWindowListener_addAndRemove() {
	OpenWindowListener listener = event -> {};
	for (int i = 0; i < 100; i++) browser.addOpenWindowListener(listener);
	for (int i = 0; i < 100; i++) browser.removeOpenWindowListener(listener);
}

@Test
public void test_OpenWindowListener_openHasValidEventDetails() {
	AtomicBoolean openFiredCorrectly = new AtomicBoolean(false);
	final Browser browserChild = new Browser(shell, SWT.None);
	browser.addOpenWindowListener(event -> {
		assertTrue("Expected Browser1 instance, but have another instance", (Browser) event.widget == browser);
		assertTrue("Expected event.browser to be null", event.browser == null);
		openFiredCorrectly.set(true);
		event.browser = browserChild;
	});

	shell.open();
	browser.setText("<html><script type='text/javascript'>window.open()</script>\n" +
			"<body>This test uses javascript to open a new window.</body></html>");

	boolean passed = waitForPassCondition(openFiredCorrectly::get);
	assertTrue("Test timed out. OpenWindow event not fired.", passed);
}

/** Test that a script 'window.open()' opens a child popup shell. */
@Test
public void test_OpenWindowListener_open_ChildPopup() {
	AtomicBoolean childCompleted = new AtomicBoolean(false);

	Shell childShell = new Shell(shell, SWT.None);
	childShell.setText("Child shell");
	childShell.setLayout(new FillLayout());
	final Browser browserChild = new Browser(childShell, SWT.NONE);

	browser.addOpenWindowListener(event -> {
		event.browser = browserChild;
	});

	browserChild.addVisibilityWindowListener(showAdapter(event -> {
		childShell.open();
		browserChild.setText("Child Browser");
	}));
	 //Triggers test to finish.
	browserChild.addProgressListener(completedAdapter(event -> childCompleted.set(true)));

	shell.open();

	browser.setText("<html>"
			+ "<script type='text/javascript'>"
			+ "var newWin = window.open();" // opens child window.
			+ "</script>\n" +
			"<body>This test uses javascript to open a new window.</body></html>");

	boolean passed = waitForPassCondition(childCompleted::get);

	String errMsg = "\nTest timed out.";
	assertTrue(errMsg, passed);
}

/** Validate event order : Child's visibility should come before progress completed event */
@Test
public void test_OpenWindow_Progress_Listener_ValidateEventOrder() {
	AtomicBoolean windowOpenFired = new AtomicBoolean(false);
	AtomicBoolean childCompleted = new AtomicBoolean(false);
	AtomicBoolean visibilityShowed = new AtomicBoolean(false);

	Shell childShell = new Shell(shell, SWT.None);
	childShell.setText("Child shell");
	childShell.setLayout(new FillLayout());
	final Browser browserChild = new Browser(childShell, SWT.NONE);

	browser.addOpenWindowListener(event -> {
		event.browser = browserChild;
		assertFalse("OpenWindowListenr should have been fired first", visibilityShowed.get() || childCompleted.get()); // Validate event order.
		windowOpenFired.set(true);
	});

	browserChild.addVisibilityWindowListener(showAdapter(event -> {
		childShell.open();
		assertTrue("Child Visibility.show should have fired before progress completed",
				windowOpenFired.get() && !childCompleted.get()); // Validate event order.
		visibilityShowed.set(true);
	}));

	browserChild.addProgressListener(completedAdapter(event -> {
		assertTrue("Child's Progress Completed before parent's expected events",
				windowOpenFired.get() && visibilityShowed.get()); // Validate event order.
		childCompleted.set(true); // Triggers test to finish.
		browserChild.setText("Child Browser!");
	}));

	shell.open();

	browser.setText("<html>"
			+ "<script type='text/javascript'>"
			+ "var newWin = window.open();" // opens child window.
			+ "</script>\n" +
			"<body>This test uses javascript to open a new window.</body></html>");

	boolean passed = waitForPassCondition(() -> windowOpenFired.get() && visibilityShowed.get() && childCompleted.get());

	String errMsg = "\nTest timed out."
			+"\nExpected true for the below, but have:"
			+"\nWindoOpenFired:" + windowOpenFired.get()
			+"\nVisibilityShowed:" + visibilityShowed.get()
			+"\nChildCompleted:" + childCompleted.get();
	assertTrue(errMsg, passed);
}

@Test
public void test_ProgressListener_newProgressAdapter() {
	new ProgressAdapter() {};
}

@Test
public void test_ProgressListener_newProgressAdapter_closeShell() {
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	Browser browser = new Browser(shell, SWT.NONE);
	browser.addProgressListener(new ProgressAdapter() {});
	shell.close();
}

@Test
public void test_ProgressListener_newListener_closeShell() {
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	Browser browser = new Browser(shell, SWT.NONE);
	browser.addProgressListener(new ProgressListener() {
		@Override
		public void changed(ProgressEvent event) {
		}
		@Override
		public void completed(ProgressEvent event) {
		}
	});
	shell.close();
}

@Test(expected = IllegalArgumentException.class)
public void test_ProgressListener_addWithNullArg() {
	browser.addProgressListener(null);
}

@Test(expected = IllegalArgumentException.class)
public void test_ProgressListener_removeWithNullArg() {
	browser.removeProgressListener(null);
}

@Test
public void test_ProgressListener_addAndRemove() {
	ProgressListener listener = new ProgressListener() {
		@Override
		public void changed(ProgressEvent event) {
		}
		@Override
		public void completed(ProgressEvent event) {
		}
	};
	for (int i = 0; i < 100; i++) browser.addProgressListener(listener);
	for (int i = 0; i < 100; i++) browser.removeProgressListener(listener);
}

@Test
public void test_ProgressListener_completed_Called() {
	AtomicBoolean childCompleted = new AtomicBoolean(false);
	ProgressListener l = new ProgressListener() {

		@Override
		public void completed(ProgressEvent event) {
			childCompleted.set(true);
		}

		@Override
		public void changed(ProgressEvent event) {

		}
	};
	browser.addProgressListener(l);
	browser.setText("<html><body>This test ensures that the completed listener is called.</body></html>");
	boolean passed = waitForPassCondition(childCompleted::get);
	assertTrue(passed);
}

@Test(expected = IllegalArgumentException.class)
public void test_StatusTextListener_addWithNull() {
	browser.addStatusTextListener(null);
}

@Test(expected = IllegalArgumentException.class)
public void test_StatusTextListener_removeWithNullArg() {
	browser.removeStatusTextListener(null);
}

@Test
public void test_StatusTextListener_addAndRemove() {
	StatusTextListener listener = event -> {
	};
	for (int i = 0; i < 100; i++) browser.addStatusTextListener(listener);
	for (int i = 0; i < 100; i++) browser.removeStatusTextListener(listener);
}

/**
 * Test if hovering over a hyperlink triggers status Text change listener.
 * Logic:
 * 1) Create a page that has a hyper link (covering the whole page)
 * 2) Move shell to top left corner
 * 3) Upon compleation of page load, move cursor across whole shell.
 *    (Note, in current jUnit, browser sometimes only takes up half the shell).
 * 4) StatusTextListener should get triggered. Test passes.
 * 5) Else timeout & fail.
 *
 * Set variable "debug_show_browser" to true to see this being performed at human-observable speed.
 *
 * Note: Historically one could execute some javascript to change status bar (window.status=txt).
 * But most browsers don't support this anymore. Only hovering over a hyperlink changes status.
 *
 * StatusTextListener may be triggerd upon page load also. So this test can pass if
 * a page load sets the status text (on older browsers) or passes when the mouse hovers
 * over the hyperlink (newer Webkit2+) browser.
 */
@Test
public void test_StatusTextListener_hoverMouseOverLink() {
	AtomicBoolean statusChanged = new AtomicBoolean(false);
	int size = 500;

	// 1) Create a page that has a hyper link (covering the whole page)
	Browser browser = new Browser(shell, SWT.NONE);
	StringBuilder longhtml = new StringBuilder();
	for (int i = 0; i < 200; i++) {
		longhtml.append("text text text text text text text text text text text text text text text text text text text text text text text text<br>");
	}
	browser.setText("<a href='http://localhost'>" + longhtml + "</a>");

	// 2) Move shell to top left corner
	shell.setLocation(0, 0);
	shell.setSize(size, size);

	browser.addProgressListener(completedAdapter(event -> {
		// * 3) Upon compleation of page load, move cursor across whole shell.
		// * (Note, in current jUnit, browser sometimes only takes up half the shell).
		Display display = event.display;
		Point cachedLocation = display.getCursorLocation();
		display.setCursorLocation(20, 10);
		browser.getBounds();
		for (int i = 0; i < size; i = i + 5) {
			display.setCursorLocation(i, i);
			waitForMilliseconds(debug_show_browser ? 3 : 1); // Move mouse slower during debug.
		}
		display.setCursorLocation(cachedLocation); // for convenience of developer. Not needed for test.

	}));

	browser.addStatusTextListener(event -> {
		statusChanged.set(true);
	});

	shell.open();
	boolean passed = waitForPassCondition(statusChanged::get);
	String msg = "Mouse movent over text was suppose to trigger StatusTextListener. But it didn't";
	assertTrue(msg, passed);
}

@Test
public void test_TitleListener_addListener_closeShell() {
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	Browser browser = new Browser(shell, SWT.NONE);
	browser.addTitleListener(event -> {
	});
	shell.close();
}

@Test(expected = IllegalArgumentException.class)
public void test_TitleListener_addwithNull() {
	browser.addTitleListener(null);
}

@Test(expected = IllegalArgumentException.class)
public void test_TitleListener_removeWithNullArg() {
	browser.removeTitleListener(null);
}

@Test
public void test_TitleListener_addAndRemove() {
	TitleListener listener = event -> {};
	for (int i = 0; i < 100; i++) browser.addTitleListener(listener);
	for (int i = 0; i < 100; i++) browser.removeTitleListener(listener);
}

@Test
public void test_TitleListener_event() {
	AtomicBoolean titleListenerFired = new AtomicBoolean(false);
	browser.addTitleListener(event -> titleListenerFired.set(true));
	browser.setText("<html><title>Hello world</title><body>Page with a title</body></html>");
	shell.open();
	boolean passed = waitForPassCondition(titleListenerFired::get);
	String errMsg = "Title listener never fired. Test timed out.";
	assertTrue(errMsg, passed);
}


@Test
public void test_setText() {
	String expectedTitle = "Website Title";
	Runnable browserSetFunc = () -> {
		String html = "<html><title>Website Title</title><body>Html page with custom title</body></html>";
		boolean opSuccess = browser.setText(html);
		assertTrue("Expecting setText() to return true", opSuccess);
	};
	validateTitleChanged(expectedTitle, browserSetFunc);
}

@Test
public void test_setUrl_local() {
	String expectedTitle = "Website Title";
	Runnable browserSetFunc = () -> {

		String pluginPath = System.getProperty("PLUGIN_PATH");
		testLogAppend("PLUGIN_PATH: " + pluginPath);

		String url;
		// Depending on how the jUnit test is ran, (gui/maven/ant), url for local file needs to be acquired differently.
		if (pluginPath != null) {
			url = pluginPath + "/data/testWebsiteWithTitle.html";
		} else {
			// used when ran from Eclipse gui.
			url = Test_org_eclipse_swt_chromium_Browser.class.getClassLoader().getResource("testWebsiteWithTitle.html").toString();
		}

		testLogAppend("URL: " + url);

		boolean opSuccess = browser.setUrl(url);
		assertTrue("Expecting setUrl() to return true" + testLog.toString(), opSuccess);
	};
	validateTitleChanged(expectedTitle, browserSetFunc);
}

/** This test requires working Internet connection */
@Test
public void test_setUrl_remote() {

	// This test sometimes times out if build server has a bad connection. Thus for this test we have a longer timeout.
	secondsToWaitTillFail = 35;

	String url = "http://example.com"; // example.com loads very quickly and conveniently has a consistent title

	// Skip this test if we don't have a working Internet connection.
	assumeTrue("Skipping test due to bad internet connection", checkInternet(url));
	testLog.append("checkInternet() passed");

	String expectedTitle = "Example Domain";
	Runnable browserSetFunc = () -> {
		testLog.append("Setting Browser url to:" + url);
		boolean opSuccess = browser.setUrl(url);
		assertTrue("Expecting setUrl() to return true", opSuccess);
	};
	validateTitleChanged(expectedTitle, browserSetFunc);
}

/** This test requires working Internet connection */
@Test
public void test_setUrl_remote_with_post() {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Fails on webkit1 but likely not going to try to support it.

	// This test sometimes times out if build server has a bad connection. Thus for this test we have a longer timeout.
	secondsToWaitTillFail = 35;

	String url = "https://bugs.eclipse.org/bugs/buglist.cgi";

	// Skip this test if we don't have a working Internet connection.
	assumeTrue("Skipping test due to bad internet connection", checkInternet(url));
	testLog.append("checkInternet() passed");

	Runnable browserSetFunc = () -> {
		testLog.append("Setting Browser url to:" + url);
		boolean opSuccess = browser.setUrl(
				url, "bug_severity=enhancement&bug_status=NEW&email1=rgrunber&emailassigned_to1=1&emailtype1=substring",
				null);
		assertTrue("Expecting setUrl() to return true", opSuccess);
	};

	final AtomicReference<Boolean> completed = new AtomicReference<>(new Boolean(false));
	browser.addProgressListener(completedAdapter(event -> {
		testLog.append("ProgressListener fired");
		completed.set(true);
	}));
	browserSetFunc.run();
	shell.open();

	boolean hasFinished = waitForPassCondition(() -> completed.get().booleanValue());
	assertTrue("Test timed out. ProgressListener not fired " + testLog.toString(), hasFinished);

	// Even a successful empty query returns about 10000 chars of HTML
	int numChars = browser.getText().length();
	assertTrue("Response data contained " + numChars + " chars.", numChars > 10000);
}

private void validateTitleChanged(String expectedTitle, Runnable browserSetFunc) {
	final AtomicReference<String> actualTitle = new AtomicReference<>("");
	browser.addTitleListener(event ->  {
		testLog.append("TitleListener fired");
		assertTrue("event title is empty" + testLog.toString(), event.title != null);
		actualTitle.set(event.title);
	});
	browserSetFunc.run();
	shell.open();

	boolean hasFinished = waitForPassCondition(() -> actualTitle.get().length() != 0
			&& !actualTitle.get().contains("about:blank")); // Windows sometimes does 2 loads, one "about:blank", and one actual load.
	boolean passed = hasFinished && actualTitle.get().equals(expectedTitle);
	String errMsg = "";
	if (!hasFinished)
		errMsg = "Test timed out. TitleListener not fired";
	else if (!actualTitle.get().equals(expectedTitle)) {
		errMsg = "\nExpected title and actual title do not match."
				+ "\nExpected: " + expectedTitle
				+ "\nActual: " + actualTitle;
	}
	assertTrue(errMsg + testLog.toString(), passed);
}

@Test
public void test_VisibilityWindowListener_newAdapter() {
	new VisibilityWindowAdapter() {};
}

@Test
public void test_VisibilityWindowListener_newAdapter_closeShell() {
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	Browser browser = new Browser(shell, SWT.NONE);
	browser.addVisibilityWindowListener(new VisibilityWindowAdapter(){});
	shell.close();
}

@Test
public void test_VisibilityWindowListener_newListener_closeShell() {
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	Browser browser = new Browser(shell, SWT.NONE);
	browser.addVisibilityWindowListener(new VisibilityWindowListener() {
		@Override
		public void hide(WindowEvent event) {
		}
		@Override
		public void show(WindowEvent event) {
		}
	});
	shell.close();
}

@Test(expected = IllegalArgumentException.class)
public void test_VisibilityWindowListener_addWithNull() {
	browser.addVisibilityWindowListener(null);
}

@Test(expected = IllegalArgumentException.class)
public void test_VisibilityWindowListener_removeWithNullArg() {
	browser.removeVisibilityWindowListener(null);
}

@Test
public void test_VisibilityWindowListener_addAndRemove() {
	VisibilityWindowListener listener = new VisibilityWindowListener() {
		@Override
		public void hide(WindowEvent event) {
		}
		@Override
		public void show(WindowEvent event) {
		}
	};
	for (int i = 0; i < 100; i++) browser.addVisibilityWindowListener(listener);
	for (int i = 0; i < 100; i++) browser.removeVisibilityWindowListener(listener);
}

/** Verify that if multiple child shells are open, no duplicate visibility events are sent. */
@Test
public void test_VisibilityWindowListener_multiple_shells() {
		AtomicBoolean secondChildCompleted = new AtomicBoolean(false);
		AtomicInteger childCount = new AtomicInteger(0);

		browser.addOpenWindowListener(event -> {
			Shell childShell = new Shell(shell);
			childShell.setText("Child shell " + childCount.get());
			childShell.setLayout(new FillLayout());
			Browser browserChild = new Browser(childShell, SWT.NONE);
			event.browser = browserChild;
			browserChild.setText("Child window");
			browserChild.addVisibilityWindowListener(new VisibilityWindowAdapter() {
				AtomicInteger invocationCount = new AtomicInteger(1);
				AtomicInteger childID = new AtomicInteger(childCount.get());
				@Override
				public void show(WindowEvent event) {
					if (childID.get() == 0 && invocationCount.get() >= 2) {
						// Certain browsers fire multiple show events for no good reason. Further show events
						// are considered 'legal' as long as they don't contain size and location information.
						if (event.location != null || event.size != null) {
							fail("Child browser's visibility show listener should only be fired once");
						}
					}
					invocationCount.incrementAndGet();
				}
			});

			if (childCount.get() == 1) {
				browserChild.addProgressListener(new ProgressAdapter() {
					@Override
					public void completed(ProgressEvent event) {
						secondChildCompleted.set(true);
					}
				});
			}
			childShell.open();
			childCount.incrementAndGet();
		});

		shell.open();
		browser.setText("<html>"
				+ "<script type='text/javascript'>"
				+ "window.open();" // opens child window.
				+ "window.open();"
				+ "</script>\n" +
				"<body>This test uses javascript to open a new window.</body></html>");

		boolean passed = waitForPassCondition(secondChildCompleted::get);

		String errMsg = "\nTest timed out.";
		assertTrue(errMsg, passed);
}

/**
 *  Validate that when javascript opens a new window and specifies size,
 *  it's size is passed to the visibility event correctly.
 */
@Test
public void test_VisibilityWindowListener_eventSize() {
	shell.setSize(200,300);
	AtomicBoolean childCompleted = new AtomicBoolean(false);
	AtomicReference<Point> result = new AtomicReference<>(new Point(0,0));

	Shell childShell = new Shell(shell);
	childShell.setSize(250, 350);
	childShell.setText("Child shell");
	childShell.setLayout(new FillLayout());
	final Browser browserChild = new Browser(childShell, SWT.NONE);

	browser.addOpenWindowListener(event -> {
		event.browser = browserChild;
		testLog.append("openWindowListener fired");
	});

	browserChild.addVisibilityWindowListener(showAdapter(event -> {
		testLog.append("Visibilty show eventfired.\nEvent size: " + event.size);
		result.set(event.size);
		childShell.open();
		childCompleted.set(true);
	}));

	shell.open();
	browser.setText("<html>"
			+ "<script type='text/javascript'>"
			+ "window.open('javascript:\"Child Window\"','', \"height=200,width=300\")\n"
			+ "</script>\n" +
			"<body>This test uses javascript to open a new window.</body></html>");

	boolean finishedWithoutTimeout = waitForPassCondition(childCompleted::get);
	browserChild.dispose();

	boolean passed = false;
	if (isWebkit1 || SwtTestUtil.isCocoa) {
		// On webkit1, event.size doesn't work properly. Fields are differently. Solution: Webkit2.
		// On Cocoa, event height/width aren't respected if declared by javascript.
		passed = finishedWithoutTimeout && result.get().x != 0 && result.get().y != 0;
	} else
		passed = finishedWithoutTimeout && result.get().x == 300 && result.get().y == 200;

	String errMsg = finishedWithoutTimeout ?
			"Incorrect size received:"
			+ "\nexpected width=300, actual:" + result.get().x
			+ "\nexpected height=100, actual:" + result.get().y
			: "test timed out. Child's visibility Window listener didn't trigger";
	assertTrue(errMsg + testLog.toString(), passed);
}

@Override
@Test
public void test_isVisible() {
	// Note. This test sometimes crashes with webkit1 because shell.setVisible() calls g_main_context_iteration(). See Bug 509411
	// To reproduce, try running test suite 20 times in a loop.
	super.test_isVisible();
}

/**
 * Test that going back in history, when no new pages were visited, returns false.
 */
@Test
public void test_back() {
	for (int i = 0; i < 2; i++) {
		browser.back();
	}
	/* returning 10 times in history - expecting false is returned */
	boolean result = browser.back();
	assertFalse(result);
}

@Test(expected = IllegalArgumentException.class)
public void test_setTextNull() {
	browser.setText(null);
}

@Test(expected = IllegalArgumentException.class)
public void test_setUrlWithNullArg() {
	browser.setUrl(null);
}


/**
 * Logic:
 * - Load a page. Turn off javascript (which takes effect on next pageload)
 * - Load a second page. Try to execute some javascript. If javascript is exectuted then fail.
 */
@Test
public void test_setJavascriptEnabled() {
	AtomicInteger pageLoadCount = new AtomicInteger(0);
	AtomicBoolean testFinished = new AtomicBoolean(false);
	AtomicBoolean testPassed = new AtomicBoolean(false);

	browser.addProgressListener(completedAdapter(event -> {
		pageLoadCount.incrementAndGet();
		if (pageLoadCount.get() == 1) {
			browser.setJavascriptEnabled(false);
			browser.setText("Second page with javascript dissabled");
		} else if (pageLoadCount.get() == 2) {
			Boolean expectedNull = null;
			try {
				expectedNull = (Boolean) browser.evaluate("return true");
			} catch (Exception e) {
				fail("1) if javascript is dissabled, browser.evaluate() should return null. But an Exception was thrown");
			}
			assertNull("2) Javascript should not have executed. But not-null was returned:" + expectedNull,
					expectedNull);

			testPassed.set(true);
			testFinished.set(true);
		}
	}));

	shell.open();
	browser.setText("First page with javascript enabled. This should not be visiable as a second page should load");

	waitForPassCondition(testFinished::get);
	assertTrue("3) Javascript was executed on the second page. But it shouldn't have", testPassed.get());
}

/** Check that if there are two browser instances, turning off JS in one instance doesn't turn off JS in the other instance. */
@Test
public void test_setJavascriptEnabled_multipleInstances() {

	AtomicInteger pageLoadCount = new AtomicInteger(1);
	AtomicInteger pageLoadCountSecondInstance = new AtomicInteger(1);

	AtomicBoolean instanceOneFinishedCorrectly = new AtomicBoolean(false);
	AtomicBoolean instanceTwoFinishedCorrectly = new AtomicBoolean(false);


	Browser browserSecondInsance = new Browser(shell, SWT.None);

	browser.addProgressListener(completedAdapter(event -> {
		if (pageLoadCount.get() == 1) {
			browser.setJavascriptEnabled(false);

			pageLoadCount.set(2);
			browser.setText("First instance, second page (with javascript turned off)");

			pageLoadCountSecondInstance.set(2);
			browserSecondInsance.setText("Second instance, second page (javascript execution not changed)");
		} else if (pageLoadCount.get() == 2) {
			pageLoadCount.set(3);

			Boolean shouldBeNull = (Boolean) browser.evaluate("return true");
			assertNull("1) Evaluate execution should be null, but 'true was returned'", shouldBeNull);
			instanceOneFinishedCorrectly.set(true);
		}
	}));

	browserSecondInsance.addProgressListener(new ProgressAdapter() {
		@Override
		public void completed(ProgressEvent event) {
			if (pageLoadCountSecondInstance.get() == 2) {
				pageLoadCountSecondInstance.set(3);

				Boolean shouldBeTrue = (Boolean) browserSecondInsance.evaluate("return true");
				assertTrue("2) Javascript should be executable in second instance (as javascript was not turned off), but it was not. "
						+ "Expected:'someStr', Actual:"+shouldBeTrue, shouldBeTrue);
				instanceTwoFinishedCorrectly.set(true);
			}
		}
	});

	browser.setText("First Instance, first page");
	browserSecondInsance.setText("Second instance, first page");

	shell.open();
	boolean passed = waitForPassCondition(() -> {return instanceOneFinishedCorrectly.get() && instanceTwoFinishedCorrectly.get();});

	String message = "3) Test timed out. Debug Info:\n" +
			"InstanceOneFinishedCorrectly: " + instanceOneFinishedCorrectly.get() + "\n" +
			"InstanceTwoFinishedCorrectly: " + instanceTwoFinishedCorrectly.get() + "\n" +
			"Instance 1 & 2 page counts: " + pageLoadCount.get() + " & " + pageLoadCountSecondInstance.get();

	assertTrue(message, passed);
}

/**
*  This test replicates what happens internally
*  if you click on a link in a javadoc popup hoverbox.
*  I.e, in a location listener, evaluation() is performed.
*
*  The goal of this test is to ensure there are no 'Freezes'/deadlocks if
*  javascript evaluation is invoked inside an SWT listener.
*
*  At time of writing, it also highlights that evaluation inside SWT listeners
*  is not consistent across browsers.
*/
@Test
public void test_LocationListener_evaluateInCallback() {
	assumeTrue(isWebkit2 || isChromium || SwtTestUtil.isCocoa || SwtTestUtil.isWindows);
	// On Webki1 this test works, but is prone to crashes. See Bug 509411

	AtomicBoolean changingFinished = new AtomicBoolean(false);
	AtomicBoolean changedFinished = new AtomicBoolean(false);
	browser.addLocationListener(new LocationListener() {
		@Override
		public void changing(LocationEvent event) {
			browser.evaluate("SWTchanging = true");  // Broken on Webkit1. I.e evaluate() in a 'changing()' signal doesn't do anything.
			changingFinished.set(true);
		}
		@Override
		public void changed(LocationEvent event) {
			browser.evaluate("SWTchanged = true");
			changedFinished.set(true);
		}
	});

	browser.setText("<body>Hello <b>World</b></body>");

	// Wait till both listeners were fired.
	if (SwtTestUtil.isWindows) {
		waitForPassCondition(changingFinished::get); // Windows doesn't reach changedFinished.get();
	} else
		waitForPassCondition(() -> (changingFinished.get() && changedFinished.get()));

	// Inspect if evaluate() was executed correctly.
	Boolean changed = false;
	try { changed = (Boolean) browser.evaluate("return SWTchanged"); } catch (SWTException e) {}
	Boolean changing = false;
	try { changing = (Boolean) browser.evaluate("return SWTchanging"); } catch (SWTException e) {}


	String errMsg = "\n  changing:  fired:" +  changingFinished.get() + "    evaluated:" + changing +
				    "\n  changed:   fired:" + changedFinished.get() + "    evaluated:" + changed;
	boolean passed = false;

	if (isWebkit2) {
		// Evaluation works in all cases.
		passed = changingFinished.get() && changedFinished.get() && changed && changing;
	} else if (isWebkit1) {
		// On Webkit1, evaluation in 'changing' fails.
		passed = changingFinished.get() && changedFinished.get() && changed; // && changing (broken)
	} else if (SwtTestUtil.isCocoa) {
		// On Cocoa, evaluation in 'changing' fails.
		passed = changingFinished.get() && changedFinished.get() && changed; // && changing (broken)
	} else if (SwtTestUtil.isWindows) {
		// On Windows, evaluation inside SWT listeners fails altogether.
		// Further, only 'changing' is fired if evaluation is invoked inside listeners.
		passed = changingFinished.get();
	}
	assertTrue(errMsg, passed);
}

/** Verify that evaluation works inside an OpenWindowListener */
@Test
public void test_OpenWindowListener_evaluateInCallback() {
	assumeTrue(!isWebkit1); // This works on Webkit1, but can sporadically fail, see Bug 509411
	AtomicBoolean eventFired = new AtomicBoolean(false);
	browser.addOpenWindowListener(event -> {
		browser.evaluate("SWTopenListener = true");
		eventFired.set(true);
		event.required = true;
	});
	shell.open();
	browser.evaluate("window.open()");
	boolean fired = waitForPassCondition(eventFired::get);
	boolean evaluated = false;
	try { evaluated = (Boolean) browser.evaluate("return SWTopenListener"); } catch (SWTException e) {}
	boolean passed = fired && evaluated;
	String errMsg = "Event fired:" + fired + "   evaluated:" + evaluated;
	assertTrue(errMsg, passed);
}

/**
 * Test that going forward in history (without having gone back before) returns false.
 */
@Test
public void test_forward() {
	for (int i = 0; i < 2; i++) {
		browser.forward();
	}
	/* going forward 10 times in history - expecting false is returned */
	boolean result = browser.forward();
	assertFalse(result);
}

/**
 * Test that getUrl() returns a non-null string.
 */
@Test
public void test_getUrl() {
	String string = browser.getUrl();
	assertNotNull(string);
}


/**
 * Test of 'back in history' api.
 * - Test isBackEnabled() and back() return the same value.
 * - Test that going isBackEnabled still returns false if back was called multiple times.
 */
@Test
public void test_isBackEnabled() {

	/* back should return the same value that isBackEnabled previously returned */
	assertEquals(browser.isBackEnabled(), browser.back());

	for (int i = 0; i < 2; i++) {
		browser.back();
	}
	/* going back 10 times in history - expecting false is returned */
	boolean result = browser.isBackEnabled();
	assertFalse(result);
}

/**
 * Test of 'forward in history' api.
 * - Test isForwardEnabled() and forward() return the same value.
 * - Test that going isBackEnabled still returns false if back was called multiple times.
 */
@Test
public void test_isForwardEnabled() {
	/* forward should return the same value that isForwardEnabled previously returned */
	assertEquals(browser.isForwardEnabled(), browser.forward());

	for (int i = 0; i < 10; i++) {
		browser.forward();
	}
	/* going forward 10 times in history - expecting false is returned */
	boolean result = browser.isForwardEnabled();
	assertFalse(result);
}

/**
 * Test that refresh executes without throwing exceptions.
 * (Maybe we should actually load a page first?)
 */
@Test
public void test_refresh() {
	for (int i = 0; i < 2; i++) {
		browser.refresh();
	}
}



/** Text without html tags */
@Test
public void test_getText() {
	if (SwtTestUtil.isWindows) {
		// Window's Browser implementation returns the processed HTML rather than the original one.
		// The processed webpage has html tags added to it.
		getText_helper("helloWorld", "<html><head></head><body>helloWorld</body></html>");
	} else {
		// Linux Webkit1, Webkit2
		// Cocoa
		getText_helper("helloWorld", "helloWorld");
	}
}

@Test
public void test_getText_html() {
	String testString = "<html><head></head><body>hello<b>World</b></body></html>";
	getText_helper(testString, testString);
}

/** Ensure we get webpage before javascript processed it.
 *  E.g js would add 'style' tag to body after processing. */
@Test
public void test_getText_script() {
	String testString = "<html><head></head><body>hello World<script>document.body.style.backgroundColor = \"red\";</script></body></html>";
	if (SwtTestUtil.isWindows) {
		// Window's Browser implementation returns the processed HTML rather than the original one.
		// The processed page injects "style" property into the body from the script.
		getText_helper(testString, "<html><head></head><body style=\"background-color: red;\">hello World<script>document.body.style.backgroundColor = \"red\";</script></body></html>");
	} else {
		// Linux Webkit1, Webkit2
		// Cocoa
		getText_helper(testString, testString);
	}

}

/** Ensure that 'DOCTYPE' is not stripped out of original string */
@Test
public void test_getText_doctype() {
	String testString = "<!DOCTYPE html><html><head></head><body>hello World</body></html>";
	if (SwtTestUtil.isWindows) {
		// Window's Browser implementation returns the processed HTML rather than the original one.
		// The processed page strips out DOCTYPE.
		getText_helper(testString, "<html><head></head><body>hello World</body></html>");
	} else  {
		// Linux Webkit1, Webkit2
		// Cocoa
		getText_helper(testString,testString);
	}
}

private void getText_helper(String testString, String expectedOutput) {
	AtomicReference<String> returnString= new AtomicReference<>("");
	AtomicBoolean finished = new AtomicBoolean(false);
	browser.setText(testString);
	browser.addProgressListener(completedAdapter(event -> {
		returnString.set(browser.getText());
		if (debug_verbose_output)
			System.out.println(returnString.get());
		finished.set(true);
	}));
	shell.open();
	waitForPassCondition(finished::get);
	boolean passed = returnString.get().equals(expectedOutput);
	String error_msg = finished.get() ?
			"Test did not return correct string.\n"
			+ "Expected:"+testString+"\n"
			+ "Actual:"+returnString.get()
			: "Test timed out";
	assertTrue(error_msg, passed);
}

/**
 * Test that a page load an be stopped (stop()) without throwing an exception.
 */
@Test
public void test_stop() {
	/* THIS TEST REQUIRES WEB ACCESS! How else can we really test the http:// part of a browser widget? */
	browser.setUrl("http://www.eclipse.org/swt");
	waitForMilliseconds(1000);
	browser.stop();
}

@Test(expected = IllegalArgumentException.class)
public void test_execute_withNullArg() {
	browser.execute(null);
}

/**
 * Test execute and windowCloseListener.
 * Close listener used to tell if execute actually worked in some meaningful way.
 */
@Test
public void test_execute_and_closeListener () {
	AtomicBoolean hasClosed = new AtomicBoolean(false);

	browser.setText("You should not see this page, it should have been closed by javascript");
	browser.addCloseWindowListener(e -> {
		hasClosed.set(true);
	});

	browser.execute("window.close()");

	shell.open();
	boolean passed = waitForPassCondition(hasClosed::get);
	if (passed)
		disposedIntentionally = true;
	String message = "Either browser.execute() did not work (if you still see the html page) or closeListener Was not triggered if "
			+ "browser looks disposed, but test still fails.";
	assertTrue(message, passed);
}


/**
 * Test the evaluate() api that returns a String type. Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_string() {
	// This test sometimes crashes on webkit1, but it's useful to test at least one 'evaluate' situation.
	assumeFalse(webkit1SkipMsg(), (isWebkit1));

	final AtomicReference<String> returnValue = new AtomicReference<>();
	browser.addProgressListener(completedAdapter(event -> {
		String evalResult = (String) browser
				.evaluate("return document.getElementById('myid').childNodes[0].nodeValue;");
		returnValue.set(evalResult);
		if (debug_verbose_output)
			System.out.println("Node value: " + evalResult);
	}));

	browser.setText("<html><body><p id='myid'>HelloWorld</p></body></html>");
	shell.open();
	boolean passed = waitForPassCondition(()-> "HelloWorld".equals(returnValue.get()));
	assertTrue("Evaluation did not return a value. Or test timed out.", passed);
}

// Test where the script has the 'return' not in the beginning,
@Test
public void test_evaluate_returnMoved() {
	assumeFalse(webkit1SkipMsg(), (isWebkit1));

	final AtomicReference<String> returnValue = new AtomicReference<>();
	browser.addProgressListener(completedAdapter(event -> {
		String evalResult = (String) browser.evaluate("var x = 1; return 'hello'");
		returnValue.set(evalResult);
	}));

	browser.setText("test text");
	shell.open();
	boolean passed = waitForPassCondition(()-> "hello".equals(returnValue.get()));
	assertTrue("Evaluation did not return a value. Or test timed out.", passed);
}

/**
 * Test the evaluate() api that returns a number (Double). Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_number_normal() {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Bug 509411
	Double testNum = 123.0;
	boolean passed = evaluate_number_helper(testNum);
	assertTrue("Failed to evaluate number: " + testNum.toString(), passed);
}

/**
 * Test the evaluate() api that returns a number (Double). Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_number_negative() {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Bug 509411

	Double testNum = -123.0;
	boolean passed = evaluate_number_helper(testNum);
	assertTrue("Failed to evaluate number: " + testNum.toString(), passed);
}

/**
 * Test the evaluate() api that returns a number (Double). Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_number_big() {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Bug 509411

	Double testNum = 10000000000.0;
	boolean passed = evaluate_number_helper(testNum);
	assertTrue("Failed to evaluate number: " + testNum.toString(), passed);
}

boolean evaluate_number_helper(Double testNum) {
	final AtomicReference<Double> returnValue = new AtomicReference<>();
	browser.addProgressListener(completedAdapter(event -> {
		Double evalResult = (Double) browser.evaluate("return " + testNum.toString());
		returnValue.set(evalResult);
		if (debug_verbose_output)
			System.out.println("Node value: " + evalResult);
	}));

	browser.setText("<html><body>HelloWorld</body></html>");
	shell.open();
	boolean passed = waitForPassCondition(() -> testNum.equals(returnValue.get()));
	return passed;
}

/**
 * Test the evaluate() api that returns a boolean. Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_boolean() {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Bug 509411
	final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
	browser.addProgressListener(completedAdapter(event -> {
		Boolean evalResult = (Boolean) browser.evaluate("return true");
		atomicBoolean.set(evalResult);
		if (debug_verbose_output)
			System.out.println("Node value: " + evalResult);
	}));

	browser.setText("<html><body>HelloWorld</body></html>");
	shell.open();
	boolean passed = waitForPassCondition(atomicBoolean::get);
	assertTrue("Evaluation did not return a boolean. Or test timed out.", passed);
}

/**
 * Test the evaluate() api that returns null. Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_null() {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Bug 509411
	 // Boolen only used as dummy placeholder so the object is not null.
	final AtomicReference<Object> returnValue = new AtomicReference<>(new Boolean(true));
	browser.addProgressListener(completedAdapter(event -> {
		Object evalResult = browser.evaluate("return null");
		returnValue.set(evalResult);
		if (debug_verbose_output)
			System.out.println("Node value: " + evalResult);
	}));

	browser.setText("<html><body>HelloWorld</body></html>");
	shell.open();
	boolean passed = waitForPassCondition(() -> returnValue.get() == null);
	assertTrue("Evaluate did not return a null. Timed out.", passed);
}

/**
 * Test the evaluate() api that throws the invalid return value exception. Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_invalid_return_value() {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Bug 509411

	if (SwtTestUtil.isWindows) {
		/* Bug 508210 . Inconsistent beahiour on windows at the moment.
		 * Fixing requires deeper investigation. Disabling newly added test for now.
		 */
		return;
	}

	final AtomicInteger exception = new AtomicInteger(-1);
	browser.addProgressListener(completedAdapter(event -> {
		try {
			browser.evaluate("return new Date()"); // Date is not supoprted as return value.
		} catch (SWTException e) {
			exception.set(e.code);
		}
	}));

	browser.setText("<html><body>HelloWorld</body></html>");
	shell.open();

	AtomicBoolean wrongExceptionCode = new AtomicBoolean(false);
	boolean passed = waitForPassCondition(() -> {
		if (exception.get() != -1) {
			if (exception.get() == SWT.ERROR_INVALID_RETURN_VALUE) {
				return true;
			} else if (exception.get() == SWT.ERROR_FAILED_EVALUATE) {
				wrongExceptionCode.set(true);
				return true;
			}
		}
		return false;
	});
	if (wrongExceptionCode.get()) {
		System.err.println("SWT Warning: test_evaluate_invalid_return_value threw wrong exception code."
				+ " Expected ERROR_INVALID_RETURN_VALUE but got ERROR_FAILED_EVALUATE");
	}
	String message = exception.get() == -1 ? "Exception was not thrown. Test timed out" : "Exception thrown, but wrong code: " + exception.get();
	assertTrue(message, passed);
}

/**
 * Test the evaluate() api that throws the evaluation failed exception. Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_evaluation_failed_exception() {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Bug 509411
	final AtomicInteger exception = new AtomicInteger(-1);
	browser.addProgressListener(completedAdapter(event -> {
		try {
			browser.evaluate("return runSomeUndefinedFunctionInJavaScriptWhichCausesUndefinedError()");
		} catch (SWTException e) {
			exception.set(e.code);
		}
	}));

	browser.setText("<html><body>HelloWorld</body></html>");
	shell.open();
	AtomicReference<String> additionalErrorInfo = new AtomicReference<>("");
	boolean passed = waitForPassCondition(() -> {
		if (exception.get() != -1) {
			if (exception.get() == SWT.ERROR_FAILED_EVALUATE) {
				return true;
			} else  {
				additionalErrorInfo.set("Invalid exception thrown: " + exception.get());
			}
		}
		return false;
	});
	String message = "".equals(additionalErrorInfo.get()) ? "Javascript did not throw an error. Test timed out" :
		"Javascript threw an error, but not the right one." + additionalErrorInfo.get();
	assertTrue(message, passed);
}

/**
 * Test the evaluate() api that returns an array of numbers. Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_array_numbers() {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Bug 509411

	// Small note:
	// evaluate() returns 'Double' type. Java doesn't have AtomicDouble
	// for convienience we simply convert double to int as we're dealing with integers anyway.
	final AtomicIntegerArray atomicIntArray = new AtomicIntegerArray(3);
	atomicIntArray.set(0, -1);
	browser.addProgressListener(completedAdapter(event -> {
		Object[] evalResult = (Object[]) browser.evaluate("return new Array(1,2,3)");
		atomicIntArray.set(0, ((Double) evalResult[0]).intValue());
		atomicIntArray.set(1, ((Double) evalResult[1]).intValue());
		atomicIntArray.set(2, ((Double) evalResult[2]).intValue());
		if (debug_verbose_output)
			System.out.println("Node value: " + evalResult);
	}));

	browser.setText("<html><body><p id='myid'>HelloWorld</p></body></html>");
	shell.open();
	AtomicReference<String> additionalErrorInfo = new AtomicReference<>("");
	boolean passed = waitForPassCondition(() -> {
		if (atomicIntArray.get(0) != -1) {
			if (atomicIntArray.get(0) == 1 && atomicIntArray.get(1) == 2 && atomicIntArray.get(2) == 3) {
				return true;
			} else {
				additionalErrorInfo.set("Resulting numbers in the array are not as expected");
			}
		}
		return false;
	});
	String message = "".equals(additionalErrorInfo.get()) ? "Javascript did not call java" : "Javasscript called java, but passed wrong values: " + additionalErrorInfo.get();
	assertTrue(message, passed);
}

/**
 * Test the evaluate() api that returns an array of strings. Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_array_strings () {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Bug 509411

	final AtomicReferenceArray<String> atomicStringArray = new AtomicReferenceArray<>(3);
	atomicStringArray.set(0, "executing");
	browser.addProgressListener(completedAdapter(event -> {
		Object[] evalResult = (Object[]) browser.evaluate("return new Array(\"str1\", \"str2\", \"str3\")");
		atomicStringArray.set(0, (String) evalResult[0]);
		atomicStringArray.set(1, (String) evalResult[1]);
		atomicStringArray.set(2, (String) evalResult[2]);
		if (debug_verbose_output)
			System.out.println("Node value: " + evalResult);
	}));

	browser.setText("<html><body><p id='myid'>HelloWorld</p></body></html>");
	shell.open();
	AtomicReference<String> additionalErrorInfo = new AtomicReference<>("");
	boolean passed = waitForPassCondition(() -> {
		if (! "executing".equals(atomicStringArray.get(0))) {
			if (atomicStringArray.get(0).equals("str1")
					&& atomicStringArray.get(1).equals("str2")
					&& atomicStringArray.get(2).equals("str3")) {
				return true;
			} else
				additionalErrorInfo.set("Resulting strings in array are not as expected");
		}
		return false;
	});
	String message = "".equals(additionalErrorInfo.get()) ?
			"Expected an array of strings, but did not receive array or got the wrong result."
			: "Received a callback from javascript, but: " + additionalErrorInfo.get() + " : " + atomicStringArray.toString();
	assertTrue(message, passed);
}

/**
 * Test the evaluate() api that returns an array of mixed types. Functionality based on Snippet308.
 * Only wait till success. Otherwise timeout after 3 seconds.
 */
@Test
public void test_evaluate_array_mixedTypes () {
	assumeFalse(webkit1SkipMsg(), isWebkit1); // Bug 509411
	final AtomicReferenceArray<Object> atomicArray = new AtomicReferenceArray<>(3);
	atomicArray.set(0, "executing");
	browser.addProgressListener(completedAdapter(event -> {
		Object[] evalResult = (Object[]) browser.evaluate("return new Array(\"str1\", 2, true)");
		atomicArray.set(2, evalResult[2]);
		atomicArray.set(1, evalResult[1]);
		atomicArray.set(0, evalResult[0]); // should be set last. to avoid loop below ending & failing to early.
		if (debug_verbose_output)
			System.out.println("Node value: " + evalResult);
	}));


	browser.setText("<html><body><p id='myid'>HelloWorld</p></body></html>");
	shell.open();
	AtomicReference<String> additionalErrorInfo = new AtomicReference<>("");
	boolean passed = waitForPassCondition(() -> {
		if (! "executing".equals(atomicArray.get(0))) {
			if (atomicArray.get(0).equals("str1")
					&& ((Double) atomicArray.get(1)) == 2
					&& ((Boolean) atomicArray.get(2))) {
				return true;
			} else
				additionalErrorInfo.set("Resulting String are not as exected");
		}
		return false;
	});
	String message = "".equals(additionalErrorInfo.get()) ? "Javascript did not call java" : "Javascript called java but passed wrong values: " + atomicArray.toString();
	assertTrue(message, passed);
}


ProgressListener callCustomFunctionUponLoad = completedAdapter(event ->	browser.execute("callCustomFunction()"));

/**
 * Test that javascript can call java.
 * loosely based on Snippet307.
 */
@Test
public void test_BrowserFunction_callback () {
	// On webkit1, this test works if ran on it's own. But sometimes in test-suite with other tests it causes jvm crash.
	// culprit seems to be the main_context_iteration() call in shell.setVisible().
	// See Bug 509587.  Solution: Webkit2.
	assumeFalse(webkit1SkipMsg(), isWebkit1);

	AtomicBoolean javaCallbackExecuted = new AtomicBoolean(false);

	class JavascriptCallback extends BrowserFunction { // Note: Local class defined inside method.
		JavascriptCallback(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			javaCallbackExecuted.set(true);
			return null;
		}
	}

	String htmlWithScript = "<html><head>\n"
			+ "<script language=\"JavaScript\">\n"
			+ "function callCustomFunction() {\n"  // Define a javascript function.
			+ "     document.body.style.backgroundColor = 'red'\n"
			+ "		jsCallbackToJava()\n"        // This calls the javafunction that we registered.
			+ "}"
			+ "</script>\n"
			+ "</head>\n"
			+ "<body> I'm going to make a callback to java </body>\n"
			+ "</html>\n";

	browser.setText(htmlWithScript);
	new JavascriptCallback(browser, "jsCallbackToJava");

	browser.addProgressListener(callCustomFunctionUponLoad);

	shell.open();
	boolean passed = waitForPassCondition(javaCallbackExecuted::get);
	String message = "Java failed to get a callback from javascript. Test timed out";
	assertTrue(message, passed);
}

/**
 * Test that javascript can call java and pass an integer to java.
 * loosely based on Snippet307.
 */
@Test
public void test_BrowserFunction_callback_with_integer () {
	// On webkit1, this test works if ran on it's own. But sometimes in test-suite with other tests it causes jvm crash.
	// culprit seems to be the main_context_iteration() call in shell.setVisible().
	// See Bug 509587.  Solution: Webkit2.
	// It's useful to run at least one function test on webkit1 locally.
	assumeFalse(webkit1SkipMsg(), (isWebkit1));

	AtomicInteger returnInt = new AtomicInteger(0);

	class JavascriptCallback extends BrowserFunction { // Note: Local class defined inside method.
		JavascriptCallback(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			Double returnedDouble = (Double) arguments[0];
			returnInt.set(returnedDouble.intValue()); // 5.0 -> 5
			return null;
		}
	}

	String htmlWithScript = "<html><head>\n"
			+ "<script language=\"JavaScript\">\n"
			+ "function callCustomFunction() {\n"  // Define a javascript function.
			+ "     document.body.style.backgroundColor = 'red'\n"
			+ "		jsCallbackToJava(5)\n"        // This calls the javafunction that we registered ** with value of 5.
			+ "}"
			+ "</script>\n"
			+ "</head>\n"
			+ "<body> I'm going to make a callback to java </body>\n"
			+ "</html>\n";

	browser.setText(htmlWithScript);
	new JavascriptCallback(browser, "jsCallbackToJava");

	browser.addProgressListener(callCustomFunctionUponLoad);

	shell.open();
	boolean passed = waitForPassCondition(() -> returnInt.get() == 5);
	String message = "Javascript should have passed an integer to java. But this did not happen";
	assertTrue(message, passed);
}



/**
 * Test that javascript can call java and pass a Boolean to java.
 * loosely based on Snippet307.
 */
@Test
public void test_BrowserFunction_callback_with_boolean () {
	// On webkit1, this test works if ran on it's own. But sometimes in test-suite with other tests it causes jvm crash.
	// culprit seems to be the main_context_iteration() call in shell.setVisible().
	// See Bug 509587.  Solution: Webkit2.
	assumeFalse(webkit1SkipMsg(), isWebkit1);

	AtomicBoolean javaCallbackExecuted = new AtomicBoolean(false);

	class JavascriptCallback extends BrowserFunction { // Note: Local class defined inside method.
		JavascriptCallback(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			Boolean returnBool = (Boolean) arguments[0];
 			javaCallbackExecuted.set(returnBool);
			return null;
		}
	}

	String htmlWithScript = "<html><head>\n"
			+ "<script language=\"JavaScript\">\n"
			+ "function callCustomFunction() {\n"  // Define a javascript function.
			+ "     document.body.style.backgroundColor = 'red'\n"
			+ "		jsCallbackToJava(true)\n"        // This calls the javafunction that we registered.
			+ "}"
			+ "</script>\n"
			+ "</head>\n"
			+ "<body> I'm going to make a callback to java </body>\n"
			+ "</html>\n";

	browser.setText(htmlWithScript);
	new JavascriptCallback(browser, "jsCallbackToJava");

	browser.addProgressListener(callCustomFunctionUponLoad);

	shell.open();
	boolean passed = waitForPassCondition(javaCallbackExecuted::get);
	String message = "Javascript did not pass a boolean back to java";
	assertTrue(message, passed);
}


/**
 * Test that javascript can call java and pass a String to java.
 * loosely based on Snippet307.
 */
@Test
public void test_BrowserFunction_callback_with_String () {
	// On webkit1, this test works if ran on it's own. But sometimes in test-suite with other tests it causes jvm crash.
	// culprit seems to be the main_context_iteration() call in shell.setVisible().
	// See Bug 509587.  Solution: Webkit2.
	assumeFalse(webkit1SkipMsg(), isWebkit1);

	final AtomicReference<String> returnValue = new AtomicReference<>();
	class JavascriptCallback extends BrowserFunction { // Note: Local class defined inside method.
		JavascriptCallback(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			String returnString = (String) arguments[0];
			returnValue.set(returnString);
			return null;
		}
	}

	String htmlWithScript = "<html><head>\n"
			+ "<script language=\"JavaScript\">\n"
			+ "function callCustomFunction() {\n"  // Define a javascript function.
			+ "     document.body.style.backgroundColor = 'red'\n"
			+ "		jsCallbackToJava('hellojava')\n"        // This calls the javafunction that we registered.
			+ "}"
			+ "</script>\n"
			+ "</head>\n"
			+ "<body> I'm going to make a callback to java </body>\n"
			+ "</html>\n";

	browser.setText(htmlWithScript);
	new JavascriptCallback(browser, "jsCallbackToJava");

	browser.addProgressListener(callCustomFunctionUponLoad);

	shell.open();
	boolean passed = waitForPassCondition(() -> "hellojava".equals(returnValue.get()));
	String message = "Javascript was suppose to call java with a String. But it seems java did not receive the call or wrong value was passed";
	assertTrue(message, passed);
}


/**
 * Test that javascript can call java and pass multiple values to java.
 * loosely based on Snippet307.
 */
@Test
public void test_BrowserFunction_callback_with_multipleValues () {
	// On webkit1, this test works if ran on it's own. But sometimes in test-suite with other tests it causes jvm crash.
	// culprit seems to be the main_context_iteration() call in shell.setVisible().
	// See Bug 509587.  Solution: Webkit2.
	assumeFalse(webkit1SkipMsg(), isWebkit1);

	final AtomicReferenceArray<Object> atomicArray = new AtomicReferenceArray<>(3); // Strin, Double, Boolean
	atomicArray.set(0, "executing");

	class JavascriptCallback extends BrowserFunction { // Note: Local class defined inside method.
		JavascriptCallback(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			atomicArray.set(1, arguments[1]);
			atomicArray.set(2, arguments[2]);
			atomicArray.set(0, arguments[0]); // item at index 0 should be set last for this test case.
			return null;
		}
	}

	String htmlWithScript = "<html><head>\n"
			+ "<script language=\"JavaScript\">\n"
			+ "function callCustomFunction() {\n"  // Define a javascript function.
			+ "     document.body.style.backgroundColor = 'red'\n"
			+ "		jsCallbackToJava('hellojava', 5, true)\n"        // This calls the javafunction that we registered.
			+ "}"
			+ "</script>\n"
			+ "</head>\n"
			+ "<body> I'm going to make a callback to java </body>\n"
			+ "</html>\n";

	browser.setText(htmlWithScript);
	new JavascriptCallback(browser, "jsCallbackToJava");

	browser.addProgressListener(callCustomFunctionUponLoad);

	shell.open();
//	Screenshots.takeScreenshot(getClass(), "test_BrowserFunction_callback_with_multipleValues__BeforeWaiting"); // Useful if investigating build failures on Hudson

	boolean passed = waitForPassCondition(() -> {
		if (atomicArray.get(0).equals("hellojava")
				&& ((Double) atomicArray.get(1)) == 5
				&& ((Boolean) atomicArray.get(2))) {
			return true;
		} else {
			return false;
		}
	});
//	Screenshots.takeScreenshot(getClass(), "test_BrowserFunction_callback_with_multipleValues__AfterWaiting");  // Useful if investigating build failures on Hudson

	String msg = "Values not set. Test timed out. Array should be [\"hellojava\", 5, true], but is: " + atomicArray.toString();
	assertTrue(msg, passed);
}


/**
 * Test that javascript can call java, java returns an Integer back to javascript.
 *
 * It's a bit tricky to tell if javascript actually received the correct value from java.
 * Solution: make a second function/callback that is called with the value that javascript received from java.
 *
 * Logic:
 *  1) Java registers function callCustomFunction() by setting html body.
 *  2) which in turn calls JavascriptCallback, which returns value 42 back to javascript.
 *  3) javascript then calls JavascriptCallback_javascriptReceivedJavaInt() and passes it value received from java.
 *  4) Java validates that the correct value (42) was passed to javascript and was passed back to java.
 *
 * loosely based on Snippet307.
 */
@Test
public void test_BrowserFunction_callback_with_javaReturningInt () {
	// On webkit1, this test works if ran on it's own. But sometimes in test-suite with other tests it causes jvm crash.
	// culprit seems to be the main_context_iteration() call in shell.setVisible().
	// See Bug 509587.  Solution: Webkit2.
	assumeFalse(webkit1SkipMsg(), isWebkit1);
	AtomicInteger returnInt = new AtomicInteger(0);

	class JavascriptCallback extends BrowserFunction { // Note: Local class defined inside method.
		JavascriptCallback(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			return 42;
		}
	}

	class JavascriptCallback_javascriptReceivedJavaInt extends BrowserFunction { // Note: Local class defined inside method.
		JavascriptCallback_javascriptReceivedJavaInt(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			Double returnVal = (Double) arguments[0];
			returnInt.set(returnVal.intValue());  // 4)
			return null;
		}
	}

	String htmlWithScript = "<html><head>\n"
			+ "<script language=\"JavaScript\">\n"
			+ "function callCustomFunction() {\n"  // Define a javascript function.
			+ "     document.body.style.backgroundColor = 'red'\n"
			+ "     var retVal = jsCallbackToJava()\n"  // 2)
			+ "		document.write(retVal)\n"        // This calls the javafunction that we registered. Set HTML body to return value.
			+ "     jsSuccess(retVal)\n"				// 3)
			+ "}"
			+ "</script>\n"
			+ "</head>\n"
			+ "<body> If you see this, javascript did not receive anything from Java. This page should just be '42' </body>\n"
			+ "</html>\n";
	// 1)
	browser.setText(htmlWithScript);
	new JavascriptCallback(browser, "jsCallbackToJava");
	new JavascriptCallback_javascriptReceivedJavaInt(browser, "jsSuccess");

	browser.addProgressListener(callCustomFunctionUponLoad);

	shell.open();
	boolean passed = waitForPassCondition(() -> returnInt.get() == 42);
	String message = "Java should have returned something back to javascript. But something went wrong";
	assertTrue(message, passed);
}


/**
 * Test that a callback works even after a new page is loaded.
 * I.e, BrowserFunctions should have to be re-initialized after a page load.
 *
 * Logic:
 * - load a page.
 * - Register java callback.
 * - call java callback from javascript. (exec)
 *
 * - java callback instantiates new page load.
 * - new page load triggers 'completed' listener
 * - completed listener calls the registered function again.
 *
 * - once regiseterd function is called a 2nd time, it sets the test to pass.
 */
@Test
public void test_BrowserFunction_callback_afterPageReload() {
	// On webkit1, this test works if ran on it's own. But sometimes in test-suite with other tests it causes jvm crash.
	// culprit seems to be the main_context_iteration() call in shell.setVisible().
	// See Bug 509587.  Solution: Webkit2.
	assumeFalse(webkit1SkipMsg(), isWebkit1);

	AtomicBoolean javaCallbackExecuted = new AtomicBoolean(false);
	AtomicInteger callCount = new AtomicInteger(0);

	class JavascriptCallback extends BrowserFunction { // Note: Local class defined inside method.
		JavascriptCallback(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			if (callCount.get() == 0) {
				callCount.set(1);
				browser.setText("2nd page load");
			} else {
				javaCallbackExecuted.set(true);
			}
			return null;
		}
	}
	browser.setText("1st (initial) page load");
	new JavascriptCallback(browser, "jsCallbackToJava");
	browser.execute("jsCallbackToJava()");
	// see if function still works after a page change:
	browser.addProgressListener(completedAdapter(e -> browser.execute("jsCallbackToJava()")));

	shell.open();
	boolean passed = waitForPassCondition(javaCallbackExecuted::get);
	String message = "A javascript callback should work after a page has been reloaded. But something went wrong";
	assertTrue(message, passed);
}


/* custom */
/**
 * Wait for passTest to return true. Timeout otherwise.
 * @param passTest a Supplier lambda that returns true if pass condition is true. False otherwise.
 * @return true if test passes, false on timeout.
 */
private boolean waitForPassCondition(final Supplier<Boolean> passTest) {
	return waitForPassCondition(passTest, 1000 * secondsToWaitTillFail);
}

private boolean waitForPassCondition(final Supplier<Boolean> passTest, int millisecondsToWait) {
	final AtomicBoolean passed = new AtomicBoolean(false);
	final Instant timeOut = Instant.now().plusMillis(millisecondsToWait);
	final Instant debug_showBrowserTimeout = Instant.now().plusSeconds(debug_show_browser_timeout_seconds);
	final Display display = shell.getDisplay();

	// This thread tests the pass-condition periodically.
	// Triggers fail if timeout occurs.
	new Thread(() -> {
		while (Instant.now().isBefore(timeOut)) {
			if (passTest.get()) {
				passed.set(true);
				break;
			}
			try {Thread.sleep(2);} catch (InterruptedException e) {e.printStackTrace();}
		}

		// If debug_show_browser is enabled, it only wakes up the display thread after the timeout occured.
		while (debug_show_browser && Instant.now().isBefore(debug_showBrowserTimeout)) {
			try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
		}
		display.wake(); // timeout. Test failed by default.
	}).start();

	while (Instant.now().isBefore(timeOut)) {
		if (passed.get()) { // Logic to show browser window for longer if enabled.
			if (!debug_show_browser) break;
			if (Instant.now().isAfter(debug_showBrowserTimeout)) break;
		}

		if (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	return passed.get();
}

/** Contrary to Thread.wait(), this method allows swt's display to carry out actions. */
void waitForMilliseconds(final int milliseconds) {
	waitForPassCondition(() -> false, milliseconds);

}

private String webkit1SkipMsg() {
	return "Test_org_eclipse_swt_browser. Bug 509411. Skipping test on Webkit1 due to sporadic crash: "+ name.getMethodName();
}


/**
 * Check if Internet connection to a http url works.
 *
 * @param url a full url like http://www.example.com
 * @return true if server responded with correct code (200), false otherwise.
 */
private static Boolean checkInternet(String url) {
	HttpURLConnection connection = null;
    try {
        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("HEAD");
        int code = connection.getResponseCode(); // 200 is success. See https://tools.ietf.org/html/rfc7231#section-6.3.1.
        if (code == 200)
        	return true;
    } catch (MalformedURLException e) {
    	System.err.println("Given url is malformed: " + url + "Try a fully formed url like: http://www.example.com");
    	e.printStackTrace();
    } catch (IOException e) {
    	// No connection was made.
    } finally {
        if (connection != null) {
            connection.disconnect();
        }
    }
    return false;
}

}

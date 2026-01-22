/****************************************************************************
**
** Copyright (C) 2026 Equo
**
** This file is part of Equo Chromium.
**
** Commercial License Usage
** Licensees holding valid commercial Equo licenses may use this file in
** accordance with the commercial license agreement provided with the
** Software or, alternatively, in accordance with the terms contained in
** a written agreement between you and Equo. For licensing terms
** and conditions see https://www.equo.dev/terms.
**
** GNU General Public License Usage
** Alternatively, this file may be used under the terms of the GNU
** General Public License version 3 as published by the Free Software
** Foundation. Please review the following
** information to ensure the GNU General Public License requirements will
** be met: https://www.gnu.org/licenses/gpl-3.0.html.
**
****************************************************************************/
package com.equo.chromium.events;

/**
 * Event fired before a browser navigation occurs. Allows intercepting and
 * modifying the navigation URL or preventing the navigation entirely.
 */
public class BeforeBrowseEvent extends BrowserEvent {
	private final boolean userGesture;
	private final boolean isRedirect;
	private String url;
	private boolean prevented = false;

	/**
	 * Creates a new BeforeBrowseEvent.
	 * 
	 * @param frame       the frame that will be navigated
	 * @param request     the navigation request
	 * @param userGesture true if the navigation was initiated by user gesture
	 * @param isRedirect  true if this is a redirect
	 */
	public BeforeBrowseEvent(String url, boolean userGesture, boolean isRedirect) {
		this.userGesture = userGesture;
		this.isRedirect = isRedirect;
		this.url = url;
	}

	/**
	 * @return true if the navigation was initiated by user gesture
	 */
	public boolean isUserGesture() {
		return userGesture;
	}

	/**
	 * @return true if this is a redirect
	 */
	public boolean isRedirect() {
		return isRedirect;
	}

	/**
	 * @return the URL that will be navigated to
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets a new URL to navigate to instead of the original URL.
	 * 
	 * @param url the new URL to navigate to
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Prevents the navigation from occurring. When called, the browser will not
	 * navigate to the URL.
	 */
	public void prevent() {
		this.prevented = true;
	}

	/**
	 * @return true if the navigation has been prevented
	 */
	public boolean isPrevented() {
		return prevented;
	}

	@Override
	public EventType getEventType() {
		return EventType.onBeforeBrowse;
	}
}
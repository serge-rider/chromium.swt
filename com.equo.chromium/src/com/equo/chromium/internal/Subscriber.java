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
package com.equo.chromium.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.equo.chromium.events.BeforeBrowseEvent;
import com.equo.chromium.events.BeforePopupEvent;
import com.equo.chromium.events.BrowserEvent;
import com.equo.chromium.events.BrowserEventListener;
import com.equo.chromium.events.EventType;
import com.equo.chromium.events.LoadEndEvent;
import com.equo.chromium.events.SimpleEvent;
import com.equo.chromium.utils.ISubscriber;

/**
 * Manages event subscriptions and notifications for browser events.
 * <p>
 * Supports subscriptions using functional interface listeners for specific
 * event types. Subscribers can register for events, be notified when events
 * occur, and be unsubscribed individually or all at once.
 * </p>
 */
public class Subscriber implements ISubscriber {
	private final Map<EventType, List<BrowserEventListener<? extends BrowserEvent>>> listeners = new HashMap<>();
	private final Map<BrowserEventListener<?>, EventType> listenerToType = new HashMap<>();

	/**
	 * Subscribes a functional interface listener for the specified event type.
	 *
	 * @param <T>       the specific subtype of {@link BrowserEvent} the listener handles
	 * @param eventType the {@link EventType} for which to subscribe
	 * @param listener  the functional interface to invoke when the event occurs
	 */
	private synchronized <T extends BrowserEvent> void subscribe(EventType eventType, BrowserEventListener<T> listener) {
		listeners.computeIfAbsent(eventType, m -> new ArrayList<>()).add(listener);
		listenerToType.put(listener, eventType);
	}

	/**
	 * Unsubscribes a previously registered event listener.
	 *
	 * @param <E>                   the type of {@link BrowserEvent} handled by the listener
	 * @param browserEventListener  the listener to remove
	 * @return {@code true} if the listener was found and removed, {@code false} otherwise
	 */
	@Override
	public synchronized <E extends BrowserEvent> boolean unsubscribe(BrowserEventListener<E> browserEventListener) {
		if (browserEventListener == null)
			return false;
		EventType type = listenerToType.remove(browserEventListener);
		if (type == null)
			return false;
		List<BrowserEventListener<? extends BrowserEvent>> list = listeners.get(type);
		if (list != null) {
			list.remove(browserEventListener);
		}
		return true;
	}

	/**
	 * Removes all registered subscriptions.
	 * <p>
	 * Clears all event listeners and any associated mappings.
	 * </p>
	 */
	@Override
	public synchronized void unsubscribeAll() {
		listeners.clear();
	}

	/**
	 * Notifies all subscribers of the given {@link BrowserEvent}.
	 * <p>
	 * Executes all listeners registered for the event's {@link EventType}.
	 * Listeners for {@code onBeforeBrowse} events are executed synchronously,
	 * while others may execute asynchronously.
	 * </p>
	 *
	 * @param event the {@link BrowserEvent} instance to dispatch to listeners
	 * @return the same {@link BrowserEvent} instance after notification
	 */
	@SuppressWarnings("unchecked")
	public synchronized BrowserEvent notifySubscribers(BrowserEvent event) {
		List<BrowserEventListener<?>> listenersT = listeners.get(event.getEventType());
		if (listenersT != null) {
			listenersT.forEach(listenerT -> {
				BrowserEventListener<BrowserEvent> typed = (BrowserEventListener<BrowserEvent>) listenerT;
				if (EventType.onBeforeBrowse == event.getEventType() || EventType.onBeforePopup == event.getEventType()) {
					typed.onEvent(event);
				} else {
					new Thread(() -> typed.onEvent(event)).start();
				}
			});
		}
		return event;
	}

	/**
	 * Registers a listener specifically for {@link BeforeBrowseEvent}.
	 *
	 * @param listener the listener to notify before browser navigation
	 */
	@Override
	public void onBeforeBrowse(BrowserEventListener<BeforeBrowseEvent> listener) {
		subscribe(EventType.onBeforeBrowse, listener);
	}

	/**
	 * Registers a listener specifically for {@link LoadEndEvent}.
	 *
	 * @param listener the listener to notify when page loading finishes
	 */
	@Override
	public void onLoadEnd(BrowserEventListener<LoadEndEvent> listener) {
		subscribe(EventType.onLoadEnd, listener);
	}

	/**
	 * Registers a listener specifically for {@link AfterCreatedEvent}.
	 *
	 * @param listener the listener to notify after the browser instance is created
	 */
	@Override
	public void onAfterCreated(BrowserEventListener<SimpleEvent> listener) {
		subscribe(EventType.onAfterCreated, listener);
	}

	/**
	 * Registers a listener specifically for {@link BeforePopupEvent}.
	 *
	 * @param listener the listener to notify before a popup window is created
	 */
	@Override
	public void onBeforePopup(BrowserEventListener<BeforePopupEvent> listener) {
		subscribe(EventType.onBeforePopup, listener);
	}
}
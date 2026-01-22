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
package com.equo.chromium.utils;

import com.equo.chromium.events.BeforeBrowseEvent;
import com.equo.chromium.events.BeforePopupEvent;
import com.equo.chromium.events.BrowserEvent;
import com.equo.chromium.events.BrowserEventListener;
import com.equo.chromium.events.LoadEndEvent;
import com.equo.chromium.events.SimpleEvent;

/**
 * Interface defining the contract for managing browser event subscriptions.
 * <p>
 * Implementations should allow registering listeners for specific event types,
 * notifying all subscribers of an event, and removing listeners individually or
 * in bulk.
 * </p>
 */
public interface ISubscriber {
	/**
	 * Registers a listener to be notified before browser navigation occurs.
	 *
	 * @param listener the listener to notify for {@link BeforeBrowseEvent}
	 */
	void onBeforeBrowse(BrowserEventListener<BeforeBrowseEvent> listener);

	/**
	 * Registers a listener to be notified when a page has finished loading.
	 *
	 * @param listener the listener to notify for {@link LoadEndEvent}
	 */
	void onLoadEnd(BrowserEventListener<LoadEndEvent> listener);

	/**
	 * Registers a listener to be notified after a browser instance has been
	 * created.
	 *
	 * @param listener the listener to notify for {@link AfterCreatedEvent}
	 */
	void onAfterCreated(BrowserEventListener<SimpleEvent> listener);

	/**
	 * Registers a listener to be notified before a popup window is created.
	 *
	 * @param listener the listener to notify for {@link BeforePopupEvent}
	 */
	void onBeforePopup(BrowserEventListener<BeforePopupEvent> listener);

	/**
	 * Unsubscribes a previously registered listener.
	 *
	 * @param <E>                  the type of {@link BrowserEvent} handled by the
	 *                             listener
	 * @param browserEventListener the listener to remove
	 * @return {@code true} if the listener was found and removed, {@code false}
	 *         otherwise
	 */
	<E extends BrowserEvent> boolean unsubscribe(BrowserEventListener<E> browserEventListener);

	/**
	 * Removes all subscriptions managed by this Subscriber.
	 */
	void unsubscribeAll();
}

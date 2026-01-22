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
 * Event fired before a popup window is created. Allows intercepting, modifying
 * or preventing the creation of the popup.
 */
public class BeforePopupEvent extends BrowserEvent {
	private final String url;
	private final String frameName;
	private boolean prevented = false;

	/**
	 * Creates a new BeforePopupEvent.
	 *
	 * @param url       the target URL of the popup
	 * @param frameName the name of the frame requesting the popup
	 */
	public BeforePopupEvent(String url, String frameName) {
		this.url = url;
		this.frameName = frameName;
	}

	/**
	 * @return the target URL of the popup
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the name of the frame requesting the popup
	 */
	public String getFrameName() {
		return frameName;
	}

	/**
	 * Prevents the popup from being created.
	 */
	public void prevent() {
		this.prevented = true;
	}

	/**
	 * @return true if the popup creation has been prevented
	 */
	public boolean isPrevented() {
		return prevented;
	}

	@Override
	public EventType getEventType() {
		return EventType.onBeforePopup;
	}
}

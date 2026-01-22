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
 * Event fired when a frame finishes loading. Contains information about the
 * loaded frame, including the HTTP status code.
 */
public class LoadEndEvent extends BrowserEvent {

	private final int httpStatusCode;

	/**
	 * Creates a new LoadEndEvent.
	 *
	 * @param httpStatusCode the HTTP status code returned by the frame load
	 */
	public LoadEndEvent(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	/**
	 * Returns the HTTP status code of the loaded frame.
	 *
	 * @return the HTTP status code
	 */
	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	@Override
	public EventType getEventType() {
		return EventType.onLoadEnd;
	}
}

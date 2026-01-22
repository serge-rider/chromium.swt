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
 * Event fired when a browser load operation encounters an error. Contains
 * information about the error code, the failing URL, and an optional error
 * message.
 */
public class LoadErrorEvent extends BrowserEvent {
	private final int errorCode;
	private final String errorText;
	private final String failedUrl;

	/**
	 * Creates a new LoadErrorEvent.
	 *
	 * @param errorCode the error code indicating the type of load failure
	 * @param errorText the error message associated with the failure
	 * @param failedUrl the URL that failed to load
	 */
	public LoadErrorEvent(int errorCode, String errorText, String failedUrl) {
		this.errorCode = errorCode;
		this.errorText = errorText;
		this.failedUrl = failedUrl;
	}

	/**
	 * Returns the error code indicating the type of load failure.
	 *
	 * @return the error code
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the error message associated with the load failure.
	 *
	 * @return the error text
	 */
	public String getErrorText() {
		return errorText;
	}

	/**
	 * Returns the URL that failed to load.
	 *
	 * @return the failed URL
	 */
	public String getFailedUrl() {
		return failedUrl;
	}

	@Override
	public EventType getEventType() {
		return EventType.onLoadError;
	}
}

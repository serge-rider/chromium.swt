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
 * Event fired when the loading state of the browser changes. Contains the
 * current loading state, navigation capabilities, and the URL being loaded.
 */
public class LoadingStateChangeEvent extends BrowserEvent {

	private boolean isLoading;
	private boolean canGoBack;
	private boolean canGoForward;

	/**
	 * Creates a new LoadingStateChangeEvent.
	 *
	 * @param isLoading    true if the browser is currently loading, false otherwise
	 * @param canGoBack    true if the browser can navigate back
	 * @param canGoForward true if the browser can navigate forward
	 */
	public LoadingStateChangeEvent(boolean isLoading, boolean canGoBack, boolean canGoForward) {
		this.isLoading = isLoading;
		this.canGoBack = canGoBack;
		this.canGoForward = canGoForward;
	}

	/**
	 * @return true if the browser is currently loading, false otherwise
	 */
	public boolean isLoading() {
		return isLoading;
	}

	/**
	 * Sets the loading state of the browser.
	 *
	 * @param isLoading true if the browser is loading, false otherwise
	 */
	public void setLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}

	/**
	 * @return true if the browser can navigate back
	 */
	public boolean canGoBack() {
		return canGoBack;
	}

	/**
	 * Sets whether the browser can navigate back.
	 *
	 * @param canGoBack true if the browser can navigate back
	 */
	public void setCanGoBack(boolean canGoBack) {
		this.canGoBack = canGoBack;
	}

	/**
	 * @return true if the browser can navigate forward
	 */
	public boolean canGoForward() {
		return canGoForward;
	}

	/**
	 * Sets whether the browser can navigate forward.
	 *
	 * @param canGoForward true if the browser can navigate forward
	 */
	public void setCanGoForward(boolean canGoForward) {
		this.canGoForward = canGoForward;
	}

	@Override
	public EventType getEventType() {
		return EventType.onLoadingStateChange;
	}
}

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
 * Event fired when the browser enters or exits fullscreen mode. Contains the
 * current fullscreen state.
 */
public class FullscreenModeChangeEvent extends BrowserEvent {

	private final boolean fullscreen;

	/**
	 * Creates a new FullscreenModeChangeEvent.
	 *
	 * @param fullscreen true if the browser is now in fullscreen mode, false
	 *                   otherwise
	 */
	public FullscreenModeChangeEvent(boolean fullscreen) {
		this.fullscreen = fullscreen;
	}

	/**
	 * @return true if the browser is currently in fullscreen mode, false otherwise
	 */
	public boolean isFullscreen() {
		return fullscreen;
	}

	@Override
	public EventType getEventType() {
		return EventType.onFullscreenChange;
	}
}

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
 * Functional interface for type-safe event listeners.
 * 
 * @param <T> the specific type of BrowserEvent this listener handles
 */
@FunctionalInterface
public interface BrowserEventListener<T extends BrowserEvent> {
	/**
	 * Called when the subscribed event occurs.
	 * 
	 * @param event the event instance containing event-specific data
	 */
	void onEvent(T event);
}
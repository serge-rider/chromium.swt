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
 * Event fired when a find operation completes. Contains information about
 * search results, including match count, active match position, and the
 * selection rectangle of the match.
 */
public class FindResultEvent extends BrowserEvent {

	private final int identifier;
	private final int count;
	private final int activeMatchOrdinal;
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final boolean finalUpdate;

	/**
	 * Creates a new FindResultEvent.
	 *
	 * @param identifier         an identifier for the search operation
	 * @param count              the total number of matches found
	 * @param x                  the x-coordinate of the selection rectangle
	 * @param y                  the y-coordinate of the selection rectangle
	 * @param width              the width of the selection rectangle
	 * @param height             the height of the selection rectangle
	 * @param activeMatchOrdinal the ordinal position of the currently active match
	 *                           (1-based)
	 * @param finalUpdate        true if this is the final update for this search
	 */
	public FindResultEvent(int identifier, int count, int x, int y, int width, int height, int activeMatchOrdinal,
			boolean finalUpdate) {
		this.identifier = identifier;
		this.count = count;
		this.activeMatchOrdinal = activeMatchOrdinal;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.finalUpdate = finalUpdate;
	}

	/**
	 * @return the identifier of the search operation
	 */
	public int getIdentifier() {
		return identifier;
	}

	/**
	 * @return the total number of matches found
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return the ordinal position of the currently active match (1-based)
	 */
	public int getActiveMatchOrdinal() {
		return activeMatchOrdinal;
	}

	/**
	 * @return the x-coordinate of the selection rectangle
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y-coordinate of the selection rectangle
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the width of the selection rectangle
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height of the selection rectangle
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return true if this is the final update for this search
	 */
	public boolean isFinalUpdate() {
		return finalUpdate;
	}

	@Override
	public EventType getEventType() {
		return EventType.onFindResult;
	}
}

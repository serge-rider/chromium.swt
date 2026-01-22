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
 * Event fired when a console message is logged by the web page. Contains the
 * message details including severity level, content, source location, and line
 * number.
 */
public class ConsoleMessageEvent extends BrowserEvent {
	private final String level;
	private final String message;
	private final String source;
	private final int line;

	/**
	 * Creates a new ConsoleMessageEvent.
	 * 
	 * @param level   the severity level of the message (e.g., "INFO", "WARN",
	 *                "ERROR")
	 * @param message the content of the console message
	 * @param source  the source file or URL where the message originated
	 * @param line    the line number in the source file
	 */
	public ConsoleMessageEvent(String level, String message, String source, int line) {
		this.level = level;
		this.message = message;
		this.source = source;
		this.line = line;
	}

	/**
	 * @return the severity level of the message (e.g., "INFO", "WARN", "ERROR")
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * @return the content of the console message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the source file or URL where the message originated
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @return the line number in the source file
	 */
	public int getLine() {
		return line;
	}

	@Override
	public EventType getEventType() {
		return EventType.onConsoleMessage;
	}
}
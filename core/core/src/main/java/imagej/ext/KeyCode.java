/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ext;

import java.util.HashMap;
import java.util.Map;

/**
 * A UI-independent enumeration of keyboard key codes.
 * 
 * @author Curtis Rueden
 */
public enum KeyCode {

	/** Enter (a.k.a. Return). */
	ENTER(0x0a),

	/** Backspace. */
	BACK_SPACE(0x08),

	/** Tab. */
	TAB(0x09),

	/** Cancel. */
	CANCEL(0x03),

	/** Clear. */
	CLEAR(0x0C),

	/** Shift (left or right). */
	SHIFT(0x10),

	/** Control (left or right). */
	CONTROL(0x11),

	/** Alt (left or right). */
	ALT(0x12),

	/** Pause. */
	PAUSE(0x13),

	/** Caps Lock. */
	CAPS_LOCK(0x14),

	/** Escape. */
	ESCAPE(0x1B),

	/** Space (' '). */
	SPACE(0x20),

	/** Page Up. */
	PAGE_UP(0x21),

	/** Page Down. */
	PAGE_DOWN(0x22),

	/** End. */
	END(0x23),

	/** Home. */
	HOME(0x24),

	/** Left arrow (non-numpad). */
	LEFT(0x25),

	/** Up arrow (non-numpad). */
	UP(0x26),

	/** Right arrow (non-numpad). */
	RIGHT(0x27),

	/** Down arrow (non-numpad). */
	DOWN(0x28),

	/** Comma (','). */
	COMMA(0x2C),

	/** Minus ('-'). */
	MINUS(0x2D),

	/** Period ('.'). */
	PERIOD(0x2E),

	/** Forward slash ('/'). */
	SLASH(0x2F),

	/** Zero ('0', non-numpad). */
	NUM0(0x30),

	/** One ('1', non-numpad). */
	NUM1(0x31),

	/** Two ('2', non-numpad). */
	NUM2(0x32),

	/** Three ('3', non-numpad). */
	NUM3(0x33),

	/** Four ('4', non-numpad). */
	NUM4(0x34),

	/** Five ('5', non-numpad). */
	NUM5(0x35),

	/** Six ('6', non-numpad). */
	NUM6(0x36),

	/** Seven ('7', non-numpad). */
	NUM7(0x37),

	/** Eight ('8', non-numpad). */
	NUM8(0x38),

	/** Nine ('9', non-numpad). */
	NUM9(0x39),

	/** Semicolon (';'). */
	SEMICOLON(0x3B),

	/** Equals ('='). */
	EQUALS(0x3D),

	/** The letter A. */
	A(0x41),

	/** The letter B. */
	B(0x42),

	/** The letter C. */
	C(0x43),

	/** The letter D. */
	D(0x44),

	/** The letter E. */
	E(0x45),

	/** The letter F. */
	F(0x46),

	/** The letter G. */
	G(0x47),

	/** The letter H. */
	H(0x48),

	/** The letter I. */
	I(0x49),

	/** The letter J. */
	J(0x4A),

	/** The letter K. */
	K(0x4B),

	/** The letter L. */
	L(0x4C),

	/** The letter M. */
	M(0x4D),

	/** The letter N. */
	N(0x4E),

	/** The letter O. */
	O(0x4F),

	/** The letter P. */
	P(0x50),

	/** The letter Q. */
	Q(0x51),

	/** The letter R. */
	R(0x52),

	/** The letter S. */
	S(0x53),

	/** The letter T. */
	T(0x54),

	/** The letter U. */
	U(0x55),

	/** The letter V. */
	V(0x56),

	/** The letter W. */
	W(0x57),

	/** The letter X. */
	X(0x58),

	/** The letter Y. */
	Y(0x59),

	/** The letter Z. */
	Z(0x5A),

	/** Left bracket ('['). */
	OPEN_BRACKET(0x5B),

	/** Backslash ('\\'). */
	BACK_SLASH(0x5C),

	/** Right bracket (']'). */
	CLOSE_BRACKET(0x5D),

	/** Zero ('0') on numeric keypad. */
	NUMPAD_0(0x60),

	/** One ('1') on numeric keypad. */
	NUMPAD_1(0x61),

	/** Two ('2') on numeric keypad. */
	NUMPAD_2(0x62),

	/** Three ('3') on numeric keypad. */
	NUMPAD_3(0x63),

	/** Four ('4') on numeric keypad. */
	NUMPAD_4(0x64),

	/** Five ('5') on numeric keypad. */
	NUMPAD_5(0x65),

	/** Six ('6') on numeric keypad. */
	NUMPAD_6(0x66),

	/** Seven ('7') on numeric keypad. */
	NUMPAD_7(0x67),

	/** Eight ('8') on numeric keypad. */
	NUMPAD_8(0x68),

	/** Nine ('9') on numeric keypad. */
	NUMPAD_9(0x69),

	/** Asterisk ('*') on numeric keypad. */
	NUMPAD_ASTERISK(0x6A),

	/** Plus ('+') on numeric keypad. */
	NUMPAD_PLUS(0x6B),

	NUMPAD_SEPARATOR(0x6C),

	/** Minus ('-') on numeric keypad. */
	NUMPAD_MINUS(0x6D),

	/** Period ('.') on numeric keypad. */
	NUMPAD_PERIOD(0x6E),

	/** Slash ('/') on numeric keypad. */
	NUMPAD_SLASH(0x6F),

	/** Delete (non-numpad). */
	DELETE(0x7F),

	/** Num Lock. */
	NUM_LOCK(0x90),

	/** Scroll Lock. */
	SCROLL_LOCK(0x91),

	/** F1. */
	F1(0x70),

	/** F2. */
	F2(0x71),

	/** F3. */
	F3(0x72),

	/** F4. */
	F4(0x73),

	/** F5. */
	F5(0x74),

	/** F6. */
	F6(0x75),

	/** F7. */
	F7(0x76),

	/** F8. */
	F8(0x77),

	/** F9. */
	F9(0x78),

	/** F10. */
	F10(0x79),

	/** F11. */
	F11(0x7A),

	/** F12. */
	F12(0x7B),

	/** F13. */
	F13(0xF000),

	/** F14. */
	F14(0xF001),

	/** F15. */
	F15(0xF002),

	/** F16. */
	F16(0xF003),

	/** F17. */
	F17(0xF004),

	/** F18 */
	F18(0xF005),

	/** F19. */
	F19(0xF006),

	/** F20. */
	F20(0xF007),

	/** F21. */
	F21(0xF008),

	/** F22. */
	F22(0xF009),

	/** F23. */
	F23(0xF00A),

	/** F24. */
	F24(0xF00B),

	/** Print Screen. */
	PRINTSCREEN(0x9A),

	/** Insert. */
	INSERT(0x9B),

	/** Help. */
	HELP(0x9C),

	/** Meta. */
	META(0x9D),

	/** Backquote ('`'). */
	BACK_QUOTE(0xC0),

	/** Single quote ('\''). */
	QUOTE(0xDE),

	/** Up arrow on numeric keypad. */
	KP_UP(0xE0),

	/** Down arrow on numeric keypad. */
	KP_DOWN(0xE1),

	/** Left arrow on numeric keypad. */
	KP_LEFT(0xE2),

	/** Right arrow on numeric keypad. */
	KP_RIGHT(0xE3),

	/** TODO. */
	DEAD_GRAVE(0x80),

	/** TODO. */
	DEAD_ACUTE(0x81),

	/** TODO. */
	DEAD_CIRCUMFLEX(0x82),

	/** TODO. */
	DEAD_TILDE(0x83),

	/** TODO. */
	DEAD_MACRON(0x84),

	/** TODO. */
	DEAD_BREVE(0x85),

	/** TODO. */
	DEAD_ABOVEDOT(0x86),

	/** TODO. */
	DEAD_DIAERESIS(0x87),

	/** TODO. */
	DEAD_ABOVERING(0x88),

	/** TODO. */
	DEAD_DOUBLEACUTE(0x89),

	/** TODO. */
	DEAD_CARON(0x8a),

	/** TODO. */
	DEAD_CEDILLA(0x8b),

	/** TODO. */
	DEAD_OGONEK(0x8c),

	/** TODO. */
	DEAD_IOTA(0x8d),

	/** TODO. */
	DEAD_VOICED_SOUND(0x8e),

	/** TODO. */
	DEAD_SEMIVOICED_SOUND(0x8f),

	/** Ampersand ('&'). */
	AMPERSAND(0x96),

	/** Asterisk ('*'). */
	ASTERISK(0x97),

	/** Double quote ('"'). */
	QUOTEDBL(0x98),

	/** Less than ('<'). */
	LESS(0x99),

	/** Greater than ('>'). */
	GREATER(0xa0),

	/** Left curly brace ('{'). */
	BRACELEFT(0xa1),

	/** Right curly brace ('}'). */
	BRACERIGHT(0xa2),

	/** At sign ('@'). */
	AT(0x0200),

	/** Colon (':'). */
	COLON(0x0201),

	/** Caret ('^'). */
	CIRCUMFLEX(0x0202),

	/** Dollar sign ('$'). */
	DOLLAR(0x0203),

	/** Euro sign. */
	EURO_SIGN(0x0204),

	/** Bang ('!'). */
	EXCLAMATION_MARK(0x0205),

	/** Inverted bang. */
	INVERTED_EXCLAMATION_MARK(0x0206),

	/** Left parenthesis ('('). */
	LEFT_PARENTHESIS(0x0207),

	/** Pound sign ('#'). */
	NUMBER_SIGN(0x0208),

	/** Plus ('+'). */
	PLUS(0x0209),

	/** Right parenthesis (')'). */
	RIGHT_PARENTHESIS(0x020A),

	/** Underscore ('_'). */
	UNDERSCORE(0x020B),

	/** Windows key (both left and right). */
	WINDOWS(0x020C),

	/** Windows Context Menu key. */
	CONTEXT_MENU(0x020D),

	FINAL(0x0018),

	/** Convert function key. */
	CONVERT(0x001C),

	/** Don't Convert function key. */
	NONCONVERT(0x001D),

	/** Accept or Commit function key. */
	ACCEPT(0x001E),

	MODECHANGE(0x001F),

	KANA(0x0015),

	KANJI(0x0019),

	/** Alphanumeric function key. */
	ALPHANUMERIC(0x00F0),

	/** Katakana function key. */
	KATAKANA(0x00F1),

	/** Hiragana function key. */
	HIRAGANA(0x00F2),

	/** Full-Width Characters function key. */
	FULL_WIDTH(0x00F3),

	/** Half-Width Characters function key. */
	HALF_WIDTH(0x00F4),

	/** Roman Characters function key. */
	ROMAN_CHARACTERS(0x00F5),

	/** All Candidates function key. */
	ALL_CANDIDATES(0x0100),

	/** Previous Candidate function key. */
	PREVIOUS_CANDIDATE(0x0101),

	/** Code Input function key. */
	CODE_INPUT(0x0102),

	/** Japanese-Katakana function key. */
	JAPANESE_KATAKANA(0x0103),

	/** Japanese-Hiragana function key. */
	JAPANESE_HIRAGANA(0x0104),

	/** Japanese-Roman function key. */
	JAPANESE_ROMAN(0x0105),

	/** Locking Kana function key. */
	KANA_LOCK(0x0106),

	/** Input method on/off key. */
	INPUT_METHOD_ON_OFF(0x0107),

	/** Cut (Sun keyboard). */
	CUT(0xFFD1),

	/** Copy (Sun keyboard). */
	COPY(0xFFCD),

	/** Paste (Sun keyboard). */
	PASTE(0xFFCF),

	/** Undo (Sun keyboard). */
	UNDO(0xFFCB),

	/** Again (Sun keyboard). */
	AGAIN(0xFFC9),

	/** Find (Sun keyboard). */
	FIND(0xFFD0),

	/** Props (Sun keyboard). */
	PROPS(0xFFCA),

	/** Stop (Sun keyboard). */
	STOP(0xFFC8),

	/** Compose function key. */
	COMPOSE(0xFF20),

	/** AltGraph function key. */
	ALT_GRAPH(0xFF7E),

	/** Begin key. */
	BEGIN(0xFF58),

	/** Unknown code. */
	UNDEFINED(0x0);

	private static final Map<Integer, KeyCode> CODES =
		new HashMap<Integer, KeyCode>();

	private static final Map<String, KeyCode> NAMES =
		new HashMap<String, KeyCode>();

	static {
		for (final KeyCode keyCode : values()) {
			CODES.put(keyCode.getCode(), keyCode);
			NAMES.put(keyCode.name(), keyCode);
		}
	}

	private int code;

	private KeyCode(final int code) {
		this.code = code;
	}

	/** Gets the key's numeric code. */
	public int getCode() {
		return code;
	}

	/**
	 * Gets the KeyCode corresponding to the given numeric code, or
	 * {@link #UNDEFINED} if no such code.
	 */
	public static KeyCode get(final int code) {
		final KeyCode keyCode = CODES.get(code);
		if (keyCode == null) return UNDEFINED;
		return keyCode;
	}

	/**
	 * Gets the KeyCode with the given name, or {@link #UNDEFINED} if no such
	 * code.
	 */
	public static KeyCode get(final String name) {
		final KeyCode keyCode = NAMES.get(name);
		if (keyCode == null) return UNDEFINED;
		return keyCode;
	}

}

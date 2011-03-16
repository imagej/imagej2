//
// AWTMenuCreator.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.plugin.gui.awt;

import imagej.plugin.RunnablePlugin;
import imagej.plugin.api.MenuEntry;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginUtils;
import imagej.plugin.gui.AbstractMenuCreator;
import imagej.plugin.gui.ShadowMenu;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public abstract class AWTMenuCreator<M>
	extends AbstractMenuCreator<M, MenuItem>
{

	@Override
	public MenuItem createMenuItem(ShadowMenu shadow) {
		final MenuEntry menuEntry = shadow.getMenuEntry();

		final String name = menuEntry.getName();

		final MenuItem menuItem;
		if (shadow.isLeaf()) {
			// create leaf item
			menuItem = new MenuItem(name);
			linkAction(shadow.getPluginEntry(), menuItem);
		}
		else {
			// create menu and recursively add children
			final Menu menu = new Menu(name);
			final List<MenuItem> childMenuItems = createChildMenuItems(shadow);
			for (final MenuItem childMenuItem : childMenuItems) {
				if (childMenuItem == null) menu.addSeparator();
				else menu.add(childMenuItem);
			}
			menuItem = menu;
		}

		String acceleratorDefinition = shadow.getMenuEntry().getAccelerator();
		
		if (acceleratorDefinition != null && acceleratorDefinition.length() > 0)
		{
			Accelerator accelerator = new Accelerator(acceleratorDefinition);
			MenuShortcut shortcut = new MenuShortcut(accelerator.keyCode, accelerator.shift);
			menuItem.setShortcut(shortcut);
		}
		
		// NOTE - I don't think menu entry mnemonics are supported by AWT - maybe by '&'
		// in menu item name but I don't think so. Investigate further.
		//final char mnemonic = menuEntry.getMnemonic();
		//if (mnemonic != '\0') menuItem.setMnemonic(mnemonic);

		return menuItem;
	}

	private void linkAction(final PluginEntry<?> entry, final MenuItem menuItem) {
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO - find better solution for typing here
				@SuppressWarnings("unchecked")
				final PluginEntry<? extends RunnablePlugin> runnableEntry =
					(PluginEntry<? extends RunnablePlugin>) entry;
				PluginUtils.runPlugin(runnableEntry);
			}
		});
	}

	private class Accelerator {
		/** modifier key states */
		boolean shift, control, meta, alt;
		/** actual key pressed (a KeyEvent key code value) */
		int keyCode;
		/** internal working variable to minimize calculations */
		private int[] keyCodes;
		
		public Accelerator(String acceleratorEncoding) {
			keyCodes = null;
			String encoding = acceleratorEncoding.toLowerCase();
			shift = hasShift(encoding);
			meta = hasMeta(encoding);
			alt = hasAlt(encoding);
			control = hasControl(encoding);
			keyCode = determineKeyCode(encoding); 
		}

		private boolean hasShift(String encoding)
		{
			if (encoding.indexOf("shift") != -1)
				return true;
			
			// otherwise its possible its encoded in key code
			
			determineKeyCodes(encoding);
			
			for (int code : keyCodes)
				if (code == KeyEvent.VK_SHIFT)
					return true;
			
			return false;
		}
		
		private boolean hasAlt(String encoding)
		{
			if ((encoding.indexOf("alt") != -1) ||
				(encoding.indexOf("altgraph") != -1))
				return true;

			determineKeyCodes(encoding);
			
			for (int code : keyCodes)
				if (code == KeyEvent.VK_ALT)
					return true;
			
			return false;
		}
		
		private boolean hasMeta(String encoding)
		{
			if (encoding.indexOf("meta") != -1)
					return true;

			determineKeyCodes(encoding);
				
			for (int code : keyCodes)
				if (code == KeyEvent.VK_META)
					return true;
				
			return false;
		}
		
		private boolean hasControl(String encoding)
		{
			if ((encoding.indexOf("control") != -1) ||
				(encoding.indexOf("ctrl") != -1) ||
				(encoding.indexOf("^") != -1))  // NB : OUR OWN EXTENSION
				return true;

			// otherwise its possible its encoded in key code

			determineKeyCodes(encoding);
			
			for (int code : keyCodes)
				if (code == KeyEvent.VK_CONTROL)
					return true;
			
			return false;
		}
		
	  // FIXME : Nonascii charset a problem? Do our accelerator definitions exist
		//   outside the ascii charset? That depends on KeyStroke's specification.
		private int determineKeyCode(String lowercaseKeyStrokeEncoding)
		{
			determineKeyCodes(lowercaseKeyStrokeEncoding);
			
			return keyCodes[keyCodes.length-1];
		}
		
		private void determineKeyCodes(String lowercaseKeyStrokeEncoding)
		{
			if (keyCodes != null)
				return;
			
			if ((lowercaseKeyStrokeEncoding.indexOf("pressed") != -1) ||
					(lowercaseKeyStrokeEncoding.indexOf("released") != -1)) {
				int lastSpace = lowercaseKeyStrokeEncoding.lastIndexOf(' ');
				String keyEncoding = lowercaseKeyStrokeEncoding.substring(lastSpace+1);
				keyCodes = keyCodesFromString(keyEncoding);
			}
			
			// otherwise either the character is represented by "typed" followed by single unicode char
			//   or its just a single unicode char
			
			keyCodes = keyCodesFromChar(lowercaseKeyStrokeEncoding.charAt(lowercaseKeyStrokeEncoding.length()-1));
		}
		
		private int[] keyCodesFromString(String lowercaseKeyCodeName)
		{
			if (lowercaseKeyCodeName.equals("0"))
				return new int[]{KeyEvent.VK_0};
			else if (lowercaseKeyCodeName.equals("1"))
				return new int[]{KeyEvent.VK_1};
			else if (lowercaseKeyCodeName.equals("2"))
				return new int[]{KeyEvent.VK_2};
			else if (lowercaseKeyCodeName.equals("3"))
				return new int[]{KeyEvent.VK_3};
			else if (lowercaseKeyCodeName.equals("4"))
				return new int[]{KeyEvent.VK_4};
			else if (lowercaseKeyCodeName.equals("5"))
				return new int[]{KeyEvent.VK_5};
			else if (lowercaseKeyCodeName.equals("6"))
				return new int[]{KeyEvent.VK_6};
			else if (lowercaseKeyCodeName.equals("7"))
				return new int[]{KeyEvent.VK_7};
			else if (lowercaseKeyCodeName.equals("8"))
				return new int[]{KeyEvent.VK_8};
			else if (lowercaseKeyCodeName.equals("9"))
				return new int[]{KeyEvent.VK_9};
			else if (lowercaseKeyCodeName.equals("a"))
				return new int[]{KeyEvent.VK_A};
			else if (lowercaseKeyCodeName.equals("add"))
				return new int[]{KeyEvent.VK_ADD};
			else if (lowercaseKeyCodeName.equals("again"))
				return new int[]{KeyEvent.VK_AGAIN};
			else if (lowercaseKeyCodeName.equals("all_candidates"))
				return new int[]{KeyEvent.VK_ALL_CANDIDATES};
			else if (lowercaseKeyCodeName.equals("alphanumeric"))
				return new int[]{KeyEvent.VK_ALPHANUMERIC};
			else if (lowercaseKeyCodeName.equals("alt"))
				return new int[]{KeyEvent.VK_ALT};
			else if (lowercaseKeyCodeName.equals("alt_graph"))
				return new int[]{KeyEvent.VK_ALT_GRAPH};
			else if (lowercaseKeyCodeName.equals("ampersand"))
				return new int[]{KeyEvent.VK_AMPERSAND};
			else if (lowercaseKeyCodeName.equals("asterisk"))
				return new int[]{KeyEvent.VK_ASTERISK};
			else if (lowercaseKeyCodeName.equals("at"))
				return new int[]{KeyEvent.VK_AT};
			else if (lowercaseKeyCodeName.equals("b"))
				return new int[]{KeyEvent.VK_B};
			else if (lowercaseKeyCodeName.equals("back_quote"))
				return new int[]{KeyEvent.VK_BACK_QUOTE};
			else if (lowercaseKeyCodeName.equals("back_slash"))
				return new int[]{KeyEvent.VK_BACK_SLASH};
			else if (lowercaseKeyCodeName.equals("back_space"))
				return new int[]{KeyEvent.VK_BACK_SPACE};
			else if (lowercaseKeyCodeName.equals("begin"))
				return new int[]{KeyEvent.VK_BEGIN};
			else if (lowercaseKeyCodeName.equals("braceleft"))
				return new int[]{KeyEvent.VK_BRACELEFT};
			else if (lowercaseKeyCodeName.equals("braceright"))
				return new int[]{KeyEvent.VK_BRACERIGHT};
			else if (lowercaseKeyCodeName.equals("c"))
				return new int[]{KeyEvent.VK_C};
			else if (lowercaseKeyCodeName.equals("cancel"))
				return new int[]{KeyEvent.VK_CANCEL};
			else if (lowercaseKeyCodeName.equals("caps_lock"))
				return new int[]{KeyEvent.VK_CAPS_LOCK};
			else if (lowercaseKeyCodeName.equals("circumflex"))
				return new int[]{KeyEvent.VK_CIRCUMFLEX};
			else if (lowercaseKeyCodeName.equals("clear"))
				return new int[]{KeyEvent.VK_CLEAR};
			else if (lowercaseKeyCodeName.equals("close_bracket"))
				return new int[]{KeyEvent.VK_CLOSE_BRACKET};
			else if (lowercaseKeyCodeName.equals("code_input"))
				return new int[]{KeyEvent.VK_CODE_INPUT};
			else if (lowercaseKeyCodeName.equals("colon"))
				return new int[]{KeyEvent.VK_COLON};
			else if (lowercaseKeyCodeName.equals("comma"))
				return new int[]{KeyEvent.VK_COMMA};
			else if (lowercaseKeyCodeName.equals("compose"))
				return new int[]{KeyEvent.VK_COMPOSE};
			else if (lowercaseKeyCodeName.equals("context_menu"))
				return new int[]{KeyEvent.VK_CONTEXT_MENU};
			else if (lowercaseKeyCodeName.equals("control"))
				return new int[]{KeyEvent.VK_CONTROL};
			else if (lowercaseKeyCodeName.equals("convert"))
				return new int[]{KeyEvent.VK_CONVERT};
			else if (lowercaseKeyCodeName.equals("copy"))
				return new int[]{KeyEvent.VK_COPY};
			else if (lowercaseKeyCodeName.equals("cut"))
				return new int[]{KeyEvent.VK_CUT};
			else if (lowercaseKeyCodeName.equals("d"))
				return new int[]{KeyEvent.VK_D};
			else if (lowercaseKeyCodeName.equals("decimal"))
				return new int[]{KeyEvent.VK_DECIMAL};
			else if (lowercaseKeyCodeName.equals("delete"))
				return new int[]{KeyEvent.VK_DELETE};
			else if (lowercaseKeyCodeName.equals("divide"))
				return new int[]{KeyEvent.VK_DIVIDE};
			else if (lowercaseKeyCodeName.equals("dollar"))
				return new int[]{KeyEvent.VK_DOLLAR};
			else if (lowercaseKeyCodeName.equals("down"))
				return new int[]{KeyEvent.VK_DOWN};
			else if (lowercaseKeyCodeName.equals("e"))
				return new int[]{KeyEvent.VK_E};
			else if (lowercaseKeyCodeName.equals("end"))
				return new int[]{KeyEvent.VK_END};
			else if (lowercaseKeyCodeName.equals("enter"))
				return new int[]{KeyEvent.VK_ENTER};
			else if (lowercaseKeyCodeName.equals("equals"))
				return new int[]{KeyEvent.VK_EQUALS};
			else if (lowercaseKeyCodeName.equals("escape"))
				return new int[]{KeyEvent.VK_ESCAPE};
			else if (lowercaseKeyCodeName.equals("euro_sign"))
				return new int[]{KeyEvent.VK_EURO_SIGN};
			else if (lowercaseKeyCodeName.equals("exclamation_mark"))
				return new int[]{KeyEvent.VK_EXCLAMATION_MARK};
			else if (lowercaseKeyCodeName.equals("f"))
				return new int[]{KeyEvent.VK_F};
			else if (lowercaseKeyCodeName.equals("f1"))
				return new int[]{KeyEvent.VK_F1};
			else if (lowercaseKeyCodeName.equals("f2"))
				return new int[]{KeyEvent.VK_F2};
			else if (lowercaseKeyCodeName.equals("f3"))
				return new int[]{KeyEvent.VK_F3};
			else if (lowercaseKeyCodeName.equals("f4"))
				return new int[]{KeyEvent.VK_F4};
			else if (lowercaseKeyCodeName.equals("f5"))
				return new int[]{KeyEvent.VK_F5};
			else if (lowercaseKeyCodeName.equals("f6"))
				return new int[]{KeyEvent.VK_F6};
			else if (lowercaseKeyCodeName.equals("f7"))
				return new int[]{KeyEvent.VK_F7};
			else if (lowercaseKeyCodeName.equals("f8"))
				return new int[]{KeyEvent.VK_F8};
			else if (lowercaseKeyCodeName.equals("f9"))
				return new int[]{KeyEvent.VK_F9};
			else if (lowercaseKeyCodeName.equals("f10"))
				return new int[]{KeyEvent.VK_F10};
			else if (lowercaseKeyCodeName.equals("f11"))
				return new int[]{KeyEvent.VK_F11};
			else if (lowercaseKeyCodeName.equals("f12"))
				return new int[]{KeyEvent.VK_F12};
			else if (lowercaseKeyCodeName.equals("f13"))
				return new int[]{KeyEvent.VK_F13};
			else if (lowercaseKeyCodeName.equals("f14"))
				return new int[]{KeyEvent.VK_F14};
			else if (lowercaseKeyCodeName.equals("f15"))
				return new int[]{KeyEvent.VK_F15};
			else if (lowercaseKeyCodeName.equals("f16"))
				return new int[]{KeyEvent.VK_F16};
			else if (lowercaseKeyCodeName.equals("f17"))
				return new int[]{KeyEvent.VK_F17};
			else if (lowercaseKeyCodeName.equals("f18"))
				return new int[]{KeyEvent.VK_F18};
			else if (lowercaseKeyCodeName.equals("f19"))
				return new int[]{KeyEvent.VK_F19};
			else if (lowercaseKeyCodeName.equals("f20"))
				return new int[]{KeyEvent.VK_F20};
			else if (lowercaseKeyCodeName.equals("f21"))
				return new int[]{KeyEvent.VK_F21};
			else if (lowercaseKeyCodeName.equals("f22"))
				return new int[]{KeyEvent.VK_F22};
			else if (lowercaseKeyCodeName.equals("f23"))
				return new int[]{KeyEvent.VK_F23};
			else if (lowercaseKeyCodeName.equals("f24"))
				return new int[]{KeyEvent.VK_F24};
			else if (lowercaseKeyCodeName.equals("final"))
				return new int[]{KeyEvent.VK_FINAL};
			else if (lowercaseKeyCodeName.equals("find"))
				return new int[]{KeyEvent.VK_FIND};
			else if (lowercaseKeyCodeName.equals("full_width"))
				return new int[]{KeyEvent.VK_FULL_WIDTH};
			else if (lowercaseKeyCodeName.equals("g"))
				return new int[]{KeyEvent.VK_G};
			else if (lowercaseKeyCodeName.equals("greater"))
				return new int[]{KeyEvent.VK_GREATER};
			else if (lowercaseKeyCodeName.equals("h"))
				return new int[]{KeyEvent.VK_H};
			else if (lowercaseKeyCodeName.equals("half_width"))
				return new int[]{KeyEvent.VK_HALF_WIDTH};
			else if (lowercaseKeyCodeName.equals("help"))
				return new int[]{KeyEvent.VK_HELP};
			else if (lowercaseKeyCodeName.equals("hiragana"))
				return new int[]{KeyEvent.VK_HIRAGANA};
			else if (lowercaseKeyCodeName.equals("home"))
				return new int[]{KeyEvent.VK_HOME};
			else if (lowercaseKeyCodeName.equals("i"))
				return new int[]{KeyEvent.VK_I};
			else if (lowercaseKeyCodeName.equals("input_method_on_off"))
				return new int[]{KeyEvent.VK_INPUT_METHOD_ON_OFF};
			else if (lowercaseKeyCodeName.equals("insert"))
				return new int[]{KeyEvent.VK_INSERT};
			else if (lowercaseKeyCodeName.equals("inverted_exclamation_mark"))
				return new int[]{KeyEvent.VK_INVERTED_EXCLAMATION_MARK};
			else if (lowercaseKeyCodeName.equals("j"))
				return new int[]{KeyEvent.VK_J};
			else if (lowercaseKeyCodeName.equals("japanese_hiragana"))
				return new int[]{KeyEvent.VK_JAPANESE_HIRAGANA};
			else if (lowercaseKeyCodeName.equals("japanese_katakana"))
				return new int[]{KeyEvent.VK_JAPANESE_KATAKANA};
			else if (lowercaseKeyCodeName.equals("k"))
				return new int[]{KeyEvent.VK_K};
			else if (lowercaseKeyCodeName.equals("kana"))
				return new int[]{KeyEvent.VK_KANA};
			else if (lowercaseKeyCodeName.equals("kana_lock"))
				return new int[]{KeyEvent.VK_KANA_LOCK};
			else if (lowercaseKeyCodeName.equals("kanji"))
				return new int[]{KeyEvent.VK_KANJI};
			else if (lowercaseKeyCodeName.equals("kp_down"))
				return new int[]{KeyEvent.VK_KP_DOWN};
			else if (lowercaseKeyCodeName.equals("kp_left"))
				return new int[]{KeyEvent.VK_KP_LEFT};
			else if (lowercaseKeyCodeName.equals("kp_right"))
				return new int[]{KeyEvent.VK_KP_RIGHT};
			else if (lowercaseKeyCodeName.equals("kp_up"))
				return new int[]{KeyEvent.VK_KP_UP};
			else if (lowercaseKeyCodeName.equals("l"))
				return new int[]{KeyEvent.VK_L};
			else if (lowercaseKeyCodeName.equals("left"))
				return new int[]{KeyEvent.VK_LEFT};
			else if (lowercaseKeyCodeName.equals("left parenthesis"))
				return new int[]{KeyEvent.VK_LEFT_PARENTHESIS};
			else if (lowercaseKeyCodeName.equals("less"))
				return new int[]{KeyEvent.VK_LESS};
			else if (lowercaseKeyCodeName.equals("m"))
				return new int[]{KeyEvent.VK_M};
			else if (lowercaseKeyCodeName.equals("meta"))
				return new int[]{KeyEvent.VK_META};
			else if (lowercaseKeyCodeName.equals("minus"))
				return new int[]{KeyEvent.VK_MINUS};
			else if (lowercaseKeyCodeName.equals("modechange"))
				return new int[]{KeyEvent.VK_MODECHANGE};
			else if (lowercaseKeyCodeName.equals("multiply"))
				return new int[]{KeyEvent.VK_MULTIPLY};
			else if (lowercaseKeyCodeName.equals("n"))
				return new int[]{KeyEvent.VK_N};
			else if (lowercaseKeyCodeName.equals("nonconvert"))
				return new int[]{KeyEvent.VK_NONCONVERT};
			else if (lowercaseKeyCodeName.equals("num_lock"))
				return new int[]{KeyEvent.VK_NUM_LOCK};
			else if (lowercaseKeyCodeName.equals("number_sign"))
				return new int[]{KeyEvent.VK_NUMBER_SIGN};
			else if (lowercaseKeyCodeName.equals("numpad0"))
				return new int[]{KeyEvent.VK_NUMPAD0};
			else if (lowercaseKeyCodeName.equals("numpad1"))
				return new int[]{KeyEvent.VK_NUMPAD1};
			else if (lowercaseKeyCodeName.equals("numpad2"))
				return new int[]{KeyEvent.VK_NUMPAD2};
			else if (lowercaseKeyCodeName.equals("numpad3"))
				return new int[]{KeyEvent.VK_NUMPAD3};
			else if (lowercaseKeyCodeName.equals("numpad4"))
				return new int[]{KeyEvent.VK_NUMPAD4};
			else if (lowercaseKeyCodeName.equals("numpad5"))
				return new int[]{KeyEvent.VK_NUMPAD5};
			else if (lowercaseKeyCodeName.equals("numpad6"))
				return new int[]{KeyEvent.VK_NUMPAD6};
			else if (lowercaseKeyCodeName.equals("numpad7"))
				return new int[]{KeyEvent.VK_NUMPAD7};
			else if (lowercaseKeyCodeName.equals("numpad8"))
				return new int[]{KeyEvent.VK_NUMPAD8};
			else if (lowercaseKeyCodeName.equals("numpad9"))
				return new int[]{KeyEvent.VK_NUMPAD9};
			else if (lowercaseKeyCodeName.equals("o"))
				return new int[]{KeyEvent.VK_O};
			else if (lowercaseKeyCodeName.equals("open_bracket"))
				return new int[]{KeyEvent.VK_OPEN_BRACKET};
			else if (lowercaseKeyCodeName.equals("p"))
				return new int[]{KeyEvent.VK_P};
			else if (lowercaseKeyCodeName.equals("page_down"))
				return new int[]{KeyEvent.VK_PAGE_DOWN};
			else if (lowercaseKeyCodeName.equals("page_up"))
				return new int[]{KeyEvent.VK_PAGE_UP};
			else if (lowercaseKeyCodeName.equals("paste"))
				return new int[]{KeyEvent.VK_PASTE};
			else if (lowercaseKeyCodeName.equals("pause"))
				return new int[]{KeyEvent.VK_PAUSE};
			else if (lowercaseKeyCodeName.equals("period"))
				return new int[]{KeyEvent.VK_PERIOD};
			else if (lowercaseKeyCodeName.equals("plus"))
				return new int[]{KeyEvent.VK_PLUS};
			else if (lowercaseKeyCodeName.equals("previous_candidate"))
				return new int[]{KeyEvent.VK_PREVIOUS_CANDIDATE};
			else if (lowercaseKeyCodeName.equals("printscreen"))
				return new int[]{KeyEvent.VK_PRINTSCREEN};
			else if (lowercaseKeyCodeName.equals("props"))
				return new int[]{KeyEvent.VK_PROPS};
			else if (lowercaseKeyCodeName.equals("q"))
				return new int[]{KeyEvent.VK_Q};
			else if (lowercaseKeyCodeName.equals("quote"))
				return new int[]{KeyEvent.VK_QUOTE};
			else if (lowercaseKeyCodeName.equals("quotedbl"))
				return new int[]{KeyEvent.VK_QUOTEDBL};
			else if (lowercaseKeyCodeName.equals("r"))
				return new int[]{KeyEvent.VK_R};
			else if (lowercaseKeyCodeName.equals("right"))
				return new int[]{KeyEvent.VK_RIGHT};
			else if (lowercaseKeyCodeName.equals("right_parenthesis"))
				return new int[]{KeyEvent.VK_RIGHT_PARENTHESIS};
			else if (lowercaseKeyCodeName.equals("roman_characters"))
				return new int[]{KeyEvent.VK_ROMAN_CHARACTERS};
			else if (lowercaseKeyCodeName.equals("s"))
				return new int[]{KeyEvent.VK_S};
			else if (lowercaseKeyCodeName.equals("scroll_lock"))
				return new int[]{KeyEvent.VK_SCROLL_LOCK};
			else if (lowercaseKeyCodeName.equals("semicolon"))
				return new int[]{KeyEvent.VK_SEMICOLON};
			else if (lowercaseKeyCodeName.equals("separator"))
				return new int[]{KeyEvent.VK_SEPARATOR};
			else if (lowercaseKeyCodeName.equals("shift"))
				return new int[]{KeyEvent.VK_SHIFT};
			else if (lowercaseKeyCodeName.equals("slash"))
				return new int[]{KeyEvent.VK_SLASH};
			else if (lowercaseKeyCodeName.equals("space"))
				return new int[]{KeyEvent.VK_SPACE};
			else if (lowercaseKeyCodeName.equals("stop"))
				return new int[]{KeyEvent.VK_STOP};
			else if (lowercaseKeyCodeName.equals("subtract"))
				return new int[]{KeyEvent.VK_SUBTRACT};
			else if (lowercaseKeyCodeName.equals("t"))
				return new int[]{KeyEvent.VK_T};
			else if (lowercaseKeyCodeName.equals("tab"))
				return new int[]{KeyEvent.VK_TAB};
			else if (lowercaseKeyCodeName.equals("u"))
				return new int[]{KeyEvent.VK_U};
			else if (lowercaseKeyCodeName.equals("undefined"))
				return new int[]{KeyEvent.VK_UNDEFINED};
			else if (lowercaseKeyCodeName.equals("underscore"))
				return new int[]{KeyEvent.VK_UNDERSCORE};
			else if (lowercaseKeyCodeName.equals("undo"))
				return new int[]{KeyEvent.VK_UNDO};
			else if (lowercaseKeyCodeName.equals("up"))
				return new int[]{KeyEvent.VK_UP};
			else if (lowercaseKeyCodeName.equals("v"))
				return new int[]{KeyEvent.VK_V};
			else if (lowercaseKeyCodeName.equals("w"))
				return new int[]{KeyEvent.VK_W};
			else if (lowercaseKeyCodeName.equals("windows"))
				return new int[]{KeyEvent.VK_WINDOWS};
			else if (lowercaseKeyCodeName.equals("x"))
				return new int[]{KeyEvent.VK_X};
			else if (lowercaseKeyCodeName.equals("y"))
				return new int[]{KeyEvent.VK_Y};
			else if (lowercaseKeyCodeName.equals("z"))
				return new int[]{KeyEvent.VK_Z};
			throw new IllegalArgumentException("unknown key code string name - "+lowercaseKeyCodeName);
    }
		
		// NB - after researching there seems to be no predefined way to convert an
		// ascii character to a KeyEvent KeyCode. I found one implementation at:
		// http://sourceforge.net/projects/eclipsekbdrc/ which is adapted here
		
		// FIXME - what if code is not an ascii char but some other unicode char?
		//   Right now this will be flagged with an exception. Also QWERTY only?
		
		private int[] keyCodesFromChar(char asciiChar)
		{
			// reject non-ASCII (7 bit!)
			if (asciiChar < 0 || asciiChar > 127) {
				throw new IllegalArgumentException("Not an ASCII char: code " + asciiChar);
			}
			
			if (asciiChar == 0) {
		    return new int[] { KeyEvent.VK_CONTROL, KeyEvent.VK_AT };
			}
			
			if (asciiChar < 27) {
		    return new int[] { KeyEvent.VK_CONTROL, KeyEvent.VK_A + asciiChar - 1 };
			}
			
			if (asciiChar == 27) {
		    return new int[] { KeyEvent.VK_CONTROL, KeyEvent.VK_OPEN_BRACKET };
			}
			
			if (asciiChar == 28) {
		    return new int[] { KeyEvent.VK_CONTROL, KeyEvent.VK_BACK_SLASH };
			}
			
			if (asciiChar == 29) {
		    return new int[] { KeyEvent.VK_CONTROL, KeyEvent.VK_CLOSE_BRACKET };
			}
			
			if (asciiChar == 30) {
		    return new int[] { KeyEvent.VK_CONTROL, KeyEvent.VK_CIRCUMFLEX };
			}
			
			if (asciiChar == 31) {
		    return new int[] { KeyEvent.VK_CONTROL, KeyEvent.VK_UNDERSCORE };
			}
			
			// codes 32 - 47
			if (asciiChar == ' ') {
		    return new int[] { KeyEvent.VK_SPACE };
			}
			
			if (asciiChar == '!') {
		    return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_1 };
			}
			
			if (asciiChar == '"') {
		    return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE };
			}
			
			if (asciiChar == '#') {
		    return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_3 };
			}
			
			if (asciiChar == '$') {
		    return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_4 };
			}
			
			// No KeyEvent.VK_ constant for '%' !!!!
			if (asciiChar == '%') {
		    return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_5 };
			}
			
			if (asciiChar == '&') {
		    return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_7 };
			}
			
			if (asciiChar == '\'') {
				return new int[] { KeyEvent.VK_QUOTE };
			}
			
			if (asciiChar == '(') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_9 };
			}
			
			if (asciiChar == ')') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_0 };
			}
			
			if (asciiChar == '*') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_8 };
			}
			
			if (asciiChar == '+') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS };
			}
			
			if (asciiChar == ',') {
				return new int[] { KeyEvent.VK_COMMA };
			}
			
			if (asciiChar == '-') {
				return new int[] { KeyEvent.VK_MINUS };
			}
			
			if (asciiChar == '.') {
				return new int[] { KeyEvent.VK_PERIOD };
			}
			
			if (asciiChar == '/') {
				return new int[] { KeyEvent.VK_SLASH };
			}
			
			if (asciiChar >= '0' && asciiChar <= '9') {
				return new int[] { KeyEvent.VK_0 + asciiChar - '0' };
			}
			
			if (asciiChar == ':') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON };
			}
			
			if (asciiChar == ';') {
				return new int[] { KeyEvent.VK_SEMICOLON };
			}
			
			if (asciiChar == '<') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA };
			}
			
			if (asciiChar == '=') {
				return new int[] { KeyEvent.VK_EQUALS };
			}
			
			if (asciiChar == '>') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD };
			}
			
			// No KeyEvent.VK_ constant for '?' !!!!
			if (asciiChar == '?') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH };
			}
			
			if (asciiChar == '@') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_2 };
			}
			
			if (asciiChar >= 'A' && asciiChar <= 'Z') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_A + asciiChar - 'A' };
			}
			
			if (asciiChar == '[') {
				return new int[] { KeyEvent.VK_OPEN_BRACKET };
			}
			
			if (asciiChar == '\\') {
				return new int[] { KeyEvent.VK_BACK_SLASH };
			}
			
			if (asciiChar == ']') {
				return new int[] { KeyEvent.VK_CLOSE_BRACKET };
			}
			
			if (asciiChar == '^') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_6 };
			}
			
			if (asciiChar == '_') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS };
			}
			
			if (asciiChar == 96) {
				return new int[] { KeyEvent.VK_BACK_QUOTE };
			}
			
			if (asciiChar >= 'a' && asciiChar <= 'z') {
				return new int[] { KeyEvent.VK_A + asciiChar - 'a' }; // no SHIFT
			}
			
			if (asciiChar == '{') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET };
			}
			
			if (asciiChar == '|') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH };
			}
			
			if (asciiChar == '}') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET };
			}
			
			if (asciiChar == '~') {
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE };
			}
			
			if (asciiChar == 127) {
				return new int[] { KeyEvent.VK_DELETE };
			}
			
			throw new IllegalArgumentException("unknown ascii code : "+(int)asciiChar);
		}
	}
	
}

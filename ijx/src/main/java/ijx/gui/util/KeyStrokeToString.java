/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ijx.gui.util;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/**
 * from http://sangchin.byus.net/JavaHelps/Javax_swing/Key2Str.html_l=rel.html
 * @author GBH
 */
public class KeyStrokeToString {

//    The KeyStroke.toString() method does not return a string that can be parsed by KeyStroke.getKeyStroke().
//            The method keyStroke2String() in this example returns a string that is parseable by
//            KeyStroke.getKeyStroke(). However, there is one keystroke that cannot be represented
//            as a string that can be parsed back to a keystroke - - a typed space character.
//            In order to bind an action to a typed space character,
//            KeyStroke.getKeyStroke(new Character(' '), 0) needs to be called.

    public static String keyStroke2String(KeyStroke key) {
        StringBuffer s = new StringBuffer(50);
        int m = key.getModifiers();

        if ((m & (InputEvent.SHIFT_DOWN_MASK|InputEvent.SHIFT_MASK)) != 0) {
            s.append("shift ");
        }
        if ((m & (InputEvent.CTRL_DOWN_MASK|InputEvent.CTRL_MASK)) != 0) {
            s.append("ctrl ");
        }
        if ((m & (InputEvent.META_DOWN_MASK|InputEvent.META_MASK)) != 0) {
            s.append("meta ");
        }
        if ((m & (InputEvent.ALT_DOWN_MASK|InputEvent.ALT_MASK)) != 0) {
            s.append("alt ");
        }
        if ((m & (InputEvent.BUTTON1_DOWN_MASK|InputEvent.BUTTON1_MASK)) != 0) {
            s.append("button1 ");
        }
        if ((m & (InputEvent.BUTTON2_DOWN_MASK|InputEvent.BUTTON2_MASK)) != 0) {
            s.append("button2 ");
        }
        if ((m & (InputEvent.BUTTON3_DOWN_MASK|InputEvent.BUTTON3_MASK)) != 0) {
            s.append("button3 ");
        }

        switch (key.getKeyEventType()) {
        case KeyEvent.KEY_TYPED:
            s.append("typed ");
            s.append(key.getKeyChar() + " ");
            break;
        case KeyEvent.KEY_PRESSED:
            s.append("pressed ");
            s.append(getKeyText(key.getKeyCode()) + " ");
            break;
        case KeyEvent.KEY_RELEASED:
            s.append("released ");
            s.append(getKeyText(key.getKeyCode()) + " ");
            break;
        default:
            s.append("unknown-event-type ");
            break;
        }

        return s.toString();
    }

    public static String getKeyText(int keyCode) {
        if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9 ||
            keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            return String.valueOf((char)keyCode);
        }

        switch(keyCode) {
          case KeyEvent.VK_COMMA: return "COMMA";
          case KeyEvent.VK_PERIOD: return "PERIOD";
          case KeyEvent.VK_SLASH: return "SLASH";
          case KeyEvent.VK_SEMICOLON: return "SEMICOLON";
          case KeyEvent.VK_EQUALS: return "EQUALS";
          case KeyEvent.VK_OPEN_BRACKET: return "OPEN_BRACKET";
          case KeyEvent.VK_BACK_SLASH: return "BACK_SLASH";
          case KeyEvent.VK_CLOSE_BRACKET: return "CLOSE_BRACKET";

          case KeyEvent.VK_ENTER: return "ENTER";
          case KeyEvent.VK_BACK_SPACE: return "BACK_SPACE";
          case KeyEvent.VK_TAB: return "TAB";
          case KeyEvent.VK_CANCEL: return "CANCEL";
          case KeyEvent.VK_CLEAR: return "CLEAR";
          case KeyEvent.VK_SHIFT: return "SHIFT";
          case KeyEvent.VK_CONTROL: return "CONTROL";
          case KeyEvent.VK_ALT: return "ALT";
          case KeyEvent.VK_PAUSE: return "PAUSE";
          case KeyEvent.VK_CAPS_LOCK: return "CAPS_LOCK";
          case KeyEvent.VK_ESCAPE: return "ESCAPE";
          case KeyEvent.VK_SPACE: return "SPACE";
          case KeyEvent.VK_PAGE_UP: return "PAGE_UP";
          case KeyEvent.VK_PAGE_DOWN: return "PAGE_DOWN";
          case KeyEvent.VK_END: return "END";
          case KeyEvent.VK_HOME: return "HOME";
          case KeyEvent.VK_LEFT: return "LEFT";
          case KeyEvent.VK_UP: return "UP";
          case KeyEvent.VK_RIGHT: return "RIGHT";
          case KeyEvent.VK_DOWN: return "DOWN";

          // numpad numeric keys handled below
          case KeyEvent.VK_MULTIPLY: return "MULTIPLY";
          case KeyEvent.VK_ADD: return "ADD";
          case KeyEvent.VK_SEPARATOR: return "SEPARATOR";
          case KeyEvent.VK_SUBTRACT: return "SUBTRACT";
          case KeyEvent.VK_DECIMAL: return "DECIMAL";
          case KeyEvent.VK_DIVIDE: return "DIVIDE";
          case KeyEvent.VK_DELETE: return "DELETE";
          case KeyEvent.VK_NUM_LOCK: return "NUM_LOCK";
          case KeyEvent.VK_SCROLL_LOCK: return "SCROLL_LOCK";

          case KeyEvent.VK_F1: return "F1";
          case KeyEvent.VK_F2: return "F2";
          case KeyEvent.VK_F3: return "F3";
          case KeyEvent.VK_F4: return "F4";
          case KeyEvent.VK_F5: return "F5";
          case KeyEvent.VK_F6: return "F6";
          case KeyEvent.VK_F7: return "F7";
          case KeyEvent.VK_F8: return "F8";
          case KeyEvent.VK_F9: return "F9";
          case KeyEvent.VK_F10: return "F10";
          case KeyEvent.VK_F11: return "F11";
          case KeyEvent.VK_F12: return "F12";
          case KeyEvent.VK_F13: return "F13";
          case KeyEvent.VK_F14: return "F14";
          case KeyEvent.VK_F15: return "F15";
          case KeyEvent.VK_F16: return "F16";
          case KeyEvent.VK_F17: return "F17";
          case KeyEvent.VK_F18: return "F18";
          case KeyEvent.VK_F19: return "F19";
          case KeyEvent.VK_F20: return "F20";
          case KeyEvent.VK_F21: return "F21";
          case KeyEvent.VK_F22: return "F22";
          case KeyEvent.VK_F23: return "F23";
          case KeyEvent.VK_F24: return "F24";

          case KeyEvent.VK_PRINTSCREEN: return "PRINTSCREEN";
          case KeyEvent.VK_INSERT: return "INSERT";
          case KeyEvent.VK_HELP: return "HELP";
          case KeyEvent.VK_META: return "META";
          case KeyEvent.VK_BACK_QUOTE: return "BACK_QUOTE";
          case KeyEvent.VK_QUOTE: return "QUOTE";

          case KeyEvent.VK_KP_UP: return "KP_UP";
          case KeyEvent.VK_KP_DOWN: return "KP_DOWN";
          case KeyEvent.VK_KP_LEFT: return "KP_LEFT";
          case KeyEvent.VK_KP_RIGHT: return "KP_RIGHT";

          case KeyEvent.VK_DEAD_GRAVE: return "DEAD_GRAVE";
          case KeyEvent.VK_DEAD_ACUTE: return "DEAD_ACUTE";
          case KeyEvent.VK_DEAD_CIRCUMFLEX: return "DEAD_CIRCUMFLEX";
          case KeyEvent.VK_DEAD_TILDE: return "DEAD_TILDE";
          case KeyEvent.VK_DEAD_MACRON: return "DEAD_MACRON";
          case KeyEvent.VK_DEAD_BREVE: return "DEAD_BREVE";
          case KeyEvent.VK_DEAD_ABOVEDOT: return "DEAD_ABOVEDOT";
          case KeyEvent.VK_DEAD_DIAERESIS: return "DEAD_DIAERESIS";
          case KeyEvent.VK_DEAD_ABOVERING: return "DEAD_ABOVERING";
          case KeyEvent.VK_DEAD_DOUBLEACUTE: return "DEAD_DOUBLEACUTE";
          case KeyEvent.VK_DEAD_CARON: return "DEAD_CARON";
          case KeyEvent.VK_DEAD_CEDILLA: return "DEAD_CEDILLA";
          case KeyEvent.VK_DEAD_OGONEK: return "DEAD_OGONEK";
          case KeyEvent.VK_DEAD_IOTA: return "DEAD_IOTA";
          case KeyEvent.VK_DEAD_VOICED_SOUND: return "DEAD_VOICED_SOUND";
          case KeyEvent.VK_DEAD_SEMIVOICED_SOUND: return "DEAD_SEMIVOICED_SOUND";

          case KeyEvent.VK_AMPERSAND: return "AMPERSAND";
          case KeyEvent.VK_ASTERISK: return "ASTERISK";
          case KeyEvent.VK_QUOTEDBL: return "QUOTEDBL";
          case KeyEvent.VK_LESS: return "LESS";
          case KeyEvent.VK_GREATER: return "GREATER";
          case KeyEvent.VK_BRACELEFT: return "BRACELEFT";
          case KeyEvent.VK_BRACERIGHT: return "BRACERIGHT";
          case KeyEvent.VK_AT: return "AT";
          case KeyEvent.VK_COLON: return "COLON";
          case KeyEvent.VK_CIRCUMFLEX: return "CIRCUMFLEX";
          case KeyEvent.VK_DOLLAR: return "DOLLAR";
          case KeyEvent.VK_EURO_SIGN: return "EURO_SIGN";
          case KeyEvent.VK_EXCLAMATION_MARK: return "EXCLAMATION_MARK";
          case KeyEvent.VK_INVERTED_EXCLAMATION_MARK:
                   return "INVERTED_EXCLAMATION_MARK";
          case KeyEvent.VK_LEFT_PARENTHESIS: return "LEFT_PARENTHESIS";
          case KeyEvent.VK_NUMBER_SIGN: return "NUMBER_SIGN";
          case KeyEvent.VK_MINUS: return "MINUS";
          case KeyEvent.VK_PLUS: return "PLUS";
          case KeyEvent.VK_RIGHT_PARENTHESIS: return "RIGHT_PARENTHESIS";
          case KeyEvent.VK_UNDERSCORE: return "UNDERSCORE";

          case KeyEvent.VK_FINAL: return "FINAL";
          case KeyEvent.VK_CONVERT: return "CONVERT";
          case KeyEvent.VK_NONCONVERT: return "NONCONVERT";
          case KeyEvent.VK_ACCEPT: return "ACCEPT";
          case KeyEvent.VK_MODECHANGE: return "MODECHANGE";
          case KeyEvent.VK_KANA: return "KANA";
          case KeyEvent.VK_KANJI: return "KANJI";
          case KeyEvent.VK_ALPHANUMERIC: return "ALPHANUMERIC";
          case KeyEvent.VK_KATAKANA: return "KATAKANA";
          case KeyEvent.VK_HIRAGANA: return "HIRAGANA";
          case KeyEvent.VK_FULL_WIDTH: return "FULL_WIDTH";
          case KeyEvent.VK_HALF_WIDTH: return "HALF_WIDTH";
          case KeyEvent.VK_ROMAN_CHARACTERS: return "ROMAN_CHARACTERS";
          case KeyEvent.VK_ALL_CANDIDATES: return "ALL_CANDIDATES";
          case KeyEvent.VK_PREVIOUS_CANDIDATE: return "PREVIOUS_CANDIDATE";
          case KeyEvent.VK_CODE_INPUT: return "CODE_INPUT";
          case KeyEvent.VK_JAPANESE_KATAKANA: return "JAPANESE_KATAKANA";
          case KeyEvent.VK_JAPANESE_HIRAGANA: return "JAPANESE_HIRAGANA";
          case KeyEvent.VK_JAPANESE_ROMAN: return "JAPANESE_ROMAN";
          case KeyEvent.VK_KANA_LOCK: return "KANA_LOCK";
          case KeyEvent.VK_INPUT_METHOD_ON_OFF: return "INPUT_METHOD_ON_OFF";

          case KeyEvent.VK_AGAIN: return "AGAIN";
          case KeyEvent.VK_UNDO: return "UNDO";
          case KeyEvent.VK_COPY: return "COPY";
          case KeyEvent.VK_PASTE: return "PASTE";
          case KeyEvent.VK_CUT: return "CUT";
          case KeyEvent.VK_FIND: return "FIND";
          case KeyEvent.VK_PROPS: return "PROPS";
          case KeyEvent.VK_STOP: return "STOP";

          case KeyEvent.VK_COMPOSE: return "COMPOSE";
          case KeyEvent.VK_ALT_GRAPH: return "ALT_GRAPH";
        }

        if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9) {
            char c = (char)(keyCode - KeyEvent.VK_NUMPAD0 + '0');
            return "NUMPAD"+c;
        }

        return "unknown(0x" + Integer.toString(keyCode, 16) + ")";
    }

    public static void main(String[] args) {

    }
}

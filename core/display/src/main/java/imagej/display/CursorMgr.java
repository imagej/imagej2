package imagej.display;

import java.awt.Cursor;

/**
 * To decouple Cursors from GUI toolkit
 *
 * March 19: Not used yet
 * 
 * @author GBH
 */
public class CursorMgr {
//	void setCursor(int cursorCode) {
//        int newAwtCursorCode = getAWTCursorCode(cursorCode);
//        if (newAwtCursorCode == Cursor.CUSTOM_CURSOR && invisibleCursor == null) {
//            newAwtCursorCode = Cursor.DEFAULT_CURSOR;
//        }
//
//        if (newAwtCursorCode == Cursor.DEFAULT_CURSOR) {
//            cursorCode = Input.CURSOR_DEFAULT;
//        }
//
//        if (this.cursorCode != cursorCode || this.awtCursorCode != newAwtCursorCode) {
//            if (newAwtCursorCode == Cursor.CUSTOM_CURSOR) {
//                comp.setCursor(invisibleCursor);
//            }
//            else {
//                comp.setCursor(Cursor.getPredefinedCursor(newAwtCursorCode));
//            }
//            this.awtCursorCode = newAwtCursorCode;
//            this.cursorCode = cursorCode;
//        }
//    }


    private int getAWTCursorCode(CursorCodes cursorCode) {
        switch (cursorCode) {
            default:               return Cursor.DEFAULT_CURSOR;
            case CURSOR_DEFAULT:   return Cursor.DEFAULT_CURSOR;
			case CURSOR_OFF:       return Cursor.CUSTOM_CURSOR;
            case CURSOR_HAND:      return Cursor.HAND_CURSOR;
            case CURSOR_CROSSHAIR: return Cursor.CROSSHAIR_CURSOR;
            case CURSOR_MOVE:      return Cursor.MOVE_CURSOR;
            case CURSOR_TEXT:      return Cursor.TEXT_CURSOR;
            case CURSOR_WAIT:      return Cursor.WAIT_CURSOR;
            case CURSOR_N_RESIZE:  return Cursor.N_RESIZE_CURSOR;
            case CURSOR_S_RESIZE:  return Cursor.S_RESIZE_CURSOR;
            case CURSOR_W_RESIZE:  return Cursor.W_RESIZE_CURSOR;
            case CURSOR_E_RESIZE:  return Cursor.E_RESIZE_CURSOR;
            case CURSOR_NW_RESIZE: return Cursor.NW_RESIZE_CURSOR;
            case CURSOR_NE_RESIZE: return Cursor.NE_RESIZE_CURSOR;
            case CURSOR_SW_RESIZE: return Cursor.SW_RESIZE_CURSOR;
            case CURSOR_SE_RESIZE: return Cursor.SE_RESIZE_CURSOR;

        }
    }
}

enum CursorCodes {
	CURSOR_DEFAULT,
	CURSOR_OFF,
	CURSOR_CROSSHAIR,
	CURSOR_HAND,
	CURSOR_MOVE,
	CURSOR_TEXT,
	CURSOR_WAIT,
	CURSOR_N_RESIZE,
	CURSOR_S_RESIZE,
	CURSOR_W_RESIZE,
	CURSOR_E_RESIZE,
	CURSOR_NW_RESIZE,
	CURSOR_NE_RESIZE,
	CURSOR_SW_RESIZE,
	CURSOR_SE_RESIZE
}

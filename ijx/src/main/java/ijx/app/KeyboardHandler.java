/*
 * 
 * $Id$
 * 
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2010, Expression company is undefined on line 9, column 62 in Templates/Licenses/license-bsd.txt.
 * All rights reserved.
 * 
 * Redistribution and use of this software in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 * 
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 * 
 *   Neither the name of Expression company is undefined on line 24, column 41 in Templates/Licenses/license-bsd.txt. nor the names of its
 *   contributors may be used to endorse or promote products
 *   derived from this software without specific prior
 *   written permission of Expression company is undefined on line 27, column 43 in Templates/Licenses/license-bsd.txt.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ijx.app;

import ij.IJ;
import ij.Menus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.plugin.MacroInstaller;
import ijx.IjxImagePlus;
import java.awt.Event;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;

/**
 *
 * @author GBH <imagejdev.org>
 */
public class KeyboardHandler implements KeyListener {
    private boolean hotkey;
    private IjxApplication ijxApp;
    protected long keyPressedTime;
    protected String lastKeyCommand;

    public String getLastKeyCommand() {
        return lastKeyCommand;
    }

    public void setLastKeyCommand(String lastKeyCommand) {
        this.lastKeyCommand = lastKeyCommand;
    }

    public long getKeyPressedTime() {
        return keyPressedTime;
    }

    public void setKeyPressedTime(long keyPressedTime) {
        this.keyPressedTime = keyPressedTime;
    }

    public KeyboardHandler(IjxApplication ijxApp) {
        this.ijxApp = ijxApp;
    }

    public void setHotkey(boolean hotkey) {
        this.hotkey = hotkey;
    }

    public boolean isHotkey() {
        return hotkey;
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        IJ.setKeyDown(keyCode);
        hotkey = false;
        if (keyCode == e.VK_CONTROL || keyCode == e.VK_SHIFT) {
            return;
        }
        char keyChar = e.getKeyChar();
        int flags = e.getModifiers();
        if (IJ.debugMode) {
            IJ.log("keyPressed: code=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + "), char=\""
                    + keyChar + "\" (" + (int) keyChar + "), flags=" + KeyEvent.getKeyModifiersText(flags));
        }
        boolean shift = (flags & e.SHIFT_MASK) != 0;
        boolean control = (flags & e.CTRL_MASK) != 0;
        boolean alt = (flags & e.ALT_MASK) != 0;
        boolean meta = (flags & e.META_MASK) != 0;
        String cmd = "";
        IjxImagePlus imp = WindowManager.getCurrentImage();
        boolean isStack = (imp != null) && (imp.getStackSize() > 1);
        if (imp != null && !control && ((keyChar >= 32 && keyChar <= 255) || keyChar == '\b' || keyChar == '\n')) {
            Roi roi = imp.getRoi();
            if (roi instanceof TextRoi) {
                if ((flags & e.META_MASK) != 0 && IJ.isMacOSX()) {
                    return;
                }
                if (alt) {
                    switch (keyChar) {
                        case 'u':
                        case 'm':
                            keyChar = IJ.micronSymbol;
                            break;
                        case 'A':
                            keyChar = IJ.angstromSymbol;
                            break;
                        default:
                    }
                }
                ((TextRoi) roi).addChar(keyChar);
                return;
            }
        }

        // Handle one character macro shortcuts
        if (!control && !meta) {
            Hashtable macroShortcuts = Menus.getMacroShortcuts();
            if (macroShortcuts.size() > 0) {
                if (shift) {
                    cmd = (String) macroShortcuts.get(new Integer(keyCode + 200));
                } else {
                    cmd = (String) macroShortcuts.get(new Integer(keyCode));
                }
                if (cmd != null) {
                    //MacroInstaller.runMacroCommand(cmd);
                    MacroInstaller.runMacroShortcut(cmd);
                    return;
                }
            }
        }
        if (!Prefs.requireControlKey || control || meta) {
            Hashtable shortcuts = Menus.getShortcuts();
            if (shift) {
                cmd = (String) shortcuts.get(new Integer(keyCode + 200));
            } else {
                cmd = (String) shortcuts.get(new Integer(keyCode));
            }
        }
        if (cmd == null) {
            switch (keyChar) {
                case '<':
                    cmd = "Previous Slice [<]";
                    break;

                case '>':
                    cmd = "Next Slice [>]";
                    break;

                case '+':
                case '=':
                    cmd = "In";
                    break;

                case '-':
                    cmd = "Out";
                    break;

                case '/':
                    cmd = "Reslice [/]...";
                    break;

                default:

            }
        }
        if (cmd == null) {
            switch (keyCode) {
                case KeyEvent.VK_TAB:
                    WindowManager.putBehind();
                    return;

                case KeyEvent.VK_BACK_SPACE:
                    cmd = "Clear";
                    hotkey =
                            true;
                    break; // delete
//case KeyEvent.VK_BACK_SLASH: cmd=IJ.altKeyDown()?"Animation Options...":"Start Animation"; break;

                case KeyEvent.VK_EQUALS:
                    cmd = "In";
                    break;

                case KeyEvent.VK_MINUS:
                    cmd = "Out";
                    break;

                case KeyEvent.VK_SLASH:
                case 0xbf:
                    cmd = "Reslice [/]...";
                    break;

                case KeyEvent.VK_COMMA:
                case 0xbc:
                    cmd = "Previous Slice [<]";
                    break;

                case KeyEvent.VK_PERIOD:
                case 0xbe:
                    cmd = "Next Slice [>]";
                    break;

                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN: // arrow keys
                    Roi roi = null;
                    if (imp != null) {
                        roi = imp.getRoi();
                    }

                    if (roi == null) {
                        return;
                    }

                    if ((flags & KeyEvent.ALT_MASK) != 0) {
                        roi.nudgeCorner(keyCode);
                    } else {
                        roi.nudge(keyCode);
                    }

                    return;
                case KeyEvent.VK_ESCAPE:
                    ijxApp.abortPluginOrMacro(imp);
                    return;

                case KeyEvent.VK_ENTER:
                    IJ.getTopComponentFrame().toFront();
                    return;

                default:

                    break;
            }

        }

        if (cmd != null && !cmd.equals("")) {
            if (cmd.equals("Fill")) {
                hotkey = true;
            }

            if (cmd.charAt(0) == MacroInstaller.commandPrefix) {
                MacroInstaller.runMacroShortcut(cmd);
            } else {
                ijxApp.doCommand(cmd);
                keyPressedTime = System.currentTimeMillis();
                lastKeyCommand = cmd;
            }

        }
    }

    public void keyTyped(KeyEvent e) {
        char keyChar = e.getKeyChar();
        int flags = e.getModifiers();
        if (IJ.debugMode) {
            IJ.log("keyTyped: char=\"" + keyChar + "\" (" + (int) keyChar + "), flags= " + Integer.toHexString(
                    flags) + " (" + KeyEvent.getKeyModifiersText(flags) + ")");
        }

        if (keyChar == '\\' || keyChar == 171 || keyChar == 223) {
            if (((flags & Event.ALT_MASK) != 0)) {
                ijxApp.doCommand("Animation Options...");
            } else {
                ijxApp.doCommand("Start Animation [\\]");
            }

        }
    }

    public void keyReleased(KeyEvent e) {
        IJ.setKeyUp(e.getKeyCode());
    }
}

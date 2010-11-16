package ijx.gui.icon;

import ij.Prefs;
import ij.gui.Roi;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 *
 * @author GBH <imagejdev.org>
 */

public abstract class IconIjx {

/*

    private void drawButtons(Graphics g) {
        if (Prefs.antialiasedTools) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        for (int i = 0; i < LINE; i++) {
            drawButton(g, i);
        }
        drawButton(g, lineType);
        for (int i = POINT; i < NUM_TOOLS; i++) {
            drawButton(g, i);
        }
    }

    private void fill3DRect(Graphics g, int x, int y, int width, int height, boolean raised) {
        if (null == g) {
            return;
        }
        if (raised) {
            g.setColor(gray);
        } else {
            g.setColor(darker);
        }
        g.fillRect(x + 1, y + 1, width - 2, height - 2);
        g.setColor(raised ? brighter : evenDarker);
        g.drawLine(x, y, x, y + height - 1);
        g.drawLine(x + 1, y, x + width - 2, y);
        g.setColor(raised ? evenDarker : brighter);
        g.drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
        g.drawLine(x + width - 1, y, x + width - 1, y + height - 2);
    }

    private void drawButton(Graphics g, int tool) {
        if (g == null) {
            return;
        }
        int index = toolIndex(tool);
        fill3DRect(g, index * SIZE + 1, 1, SIZE, SIZE - 1, !down[tool]);
        g.setColor(toolColor);
        int x = index * SIZE + OFFSET;
        int y = OFFSET;
        if (down[tool]) {
            x++;
            y++;
        }
        this.g = g;
        if (tool >= SPARE1 && tool <= SPARE9 && icons[tool] != null) {
            drawIcon(g, tool, x, y);
            return;
        }
        switch (tool) {
            case RECTANGLE:
                xOffset = x;
                yOffset = y;
                if (roundRectMode) {
                    g.drawRoundRect(x + 1, y + 2, 15, 12, 8, 8);
                } else {
                    g.drawRect(x + 1, y + 2, 15, 12);
                }
                drawTriangle(15, 14);
                return;
            case OVAL:
                xOffset = x;
                yOffset = y;
                if (brushEnabled) {
                    m(9, 2);
                    d(13, 2);
                    d(13, 2);
                    d(15, 5);
                    d(15, 8);
                    d(13, 10);
                    d(10, 10);
                    d(8, 13);
                    d(4, 13);
                    d(2, 11);
                    d(2, 7);
                    d(4, 5);
                    d(7, 5);
                    d(9, 2);
                } else {
                    g.drawOval(x + 1, y + 2, 15, 12);
                }
                drawTriangle(15, 14);
                return;
            case POLYGON:
                xOffset = x + 1;
                yOffset = y + 3;
                m(4, 0);
                d(14, 0);
                d(14, 1);
                d(10, 5);
                d(10, 6);
                d(13, 9);
                d(13, 10);
                d(0, 10);
                d(0, 4);
                d(4, 0);
                return;
            case FREEROI:
                xOffset = x + 1;
                yOffset = y + 3;
                m(3, 0);
                d(5, 0);
                d(7, 2);
                d(9, 2);
                d(11, 0);
                d(13, 0);
                d(14, 1);
                d(15, 2);
                d(15, 4);
                d(14, 5);
                d(14, 6);
                d(12, 8);
                d(11, 8);
                d(10, 9);
                d(9, 9);
                d(8, 10);
                d(5, 10);
                d(3, 8);
                d(2, 8);
                d(1, 7);
                d(1, 6);
                d(0, 5);
                d(0, 2);
                d(1, 1);
                d(2, 1);
                return;
            case LINE:
                xOffset = x;
                yOffset = y;
                if (arrowMode) {
                    m(1, 14);
                    d(14, 1);
                    m(6, 5);
                    d(14, 1);
                    m(10, 9);
                    d(14, 1);
                    m(6, 5);
                    d(10, 9);
                } else {
                    m(0, 12);
                    d(17, 3);
                }
                drawTriangle(12, 14);
                return;
            case POLYLINE:
                xOffset = x;
                yOffset = y;
                m(14, 6);
                d(11, 3);
                d(1, 3);
                d(1, 4);
                d(6, 9);
                d(2, 13);
                drawTriangle(12, 14);
                return;
            case FREELINE:
                xOffset = x;
                yOffset = y;
                m(16, 4);
                d(14, 6);
                d(12, 6);
                d(9, 3);
                d(8, 3);
                d(6, 7);
                d(2, 11);
                d(1, 11);
                drawTriangle(12, 14);
                return;
            case POINT:
                xOffset = x;
                yOffset = y;
                if (multiPointMode) {
                    drawPoint(1, 3);
                    drawPoint(9, 1);
                    drawPoint(15, 5);
                    drawPoint(10, 11);
                    drawPoint(2, 12);
                } else {
                    m(1, 8);
                    d(6, 8);
                    d(6, 6);
                    d(10, 6);
                    d(10, 10);
                    d(6, 10);
                    d(6, 9);
                    m(8, 1);
                    d(8, 5);
                    m(11, 8);
                    d(15, 8);
                    m(8, 11);
                    d(8, 15);
                    m(8, 8);
                    d(8, 8);
                    g.setColor(Roi.getColor());
                    g.fillRect(x + 7, y + 7, 3, 3);
                }
                drawTriangle(14, 14);
                return;
            case WAND:
                xOffset = x + 2;
                yOffset = y + 2;
                dot(4, 0);
                m(2, 0);
                d(3, 1);
                d(4, 2);
                m(0, 0);
                d(1, 1);
                m(0, 2);
                d(1, 3);
                d(2, 4);
                dot(0, 4);
                m(3, 3);
                d(12, 12);
                return;
            case TEXT:
                xOffset = x + 2;
                yOffset = y + 1;
                m(0, 13);
                d(3, 13);
                m(1, 12);
                d(7, 0);
                d(12, 13);
                m(11, 13);
                d(14, 13);
                m(3, 8);
                d(10, 8);
                return;
            case MAGNIFIER:
                xOffset = x + 2;
                yOffset = y + 2;
                m(3, 0);
                d(3, 0);
                d(5, 0);
                d(8, 3);
                d(8, 5);
                d(7, 6);
                d(7, 7);
                d(6, 7);
                d(5, 8);
                d(3, 8);
                d(0, 5);
                d(0, 3);
                d(3, 0);
                m(8, 8);
                d(9, 8);
                d(13, 12);
                d(13, 13);
                d(12, 13);
                d(8, 9);
                d(8, 8);
                return;
            case HAND:
                xOffset = x + 1;
                yOffset = y + 1;
                m(5, 14);
                d(2, 11);
                d(2, 10);
                d(0, 8);
                d(0, 7);
                d(1, 6);
                d(2, 6);
                d(4, 8);
                d(4, 6);
                d(3, 5);
                d(3, 4);
                d(2, 3);
                d(2, 2);
                d(3, 1);
                d(4, 1);
                d(5, 2);
                d(5, 3);
                m(6, 5);
                d(6, 1);
                d(7, 0);
                d(8, 0);
                d(9, 1);
                d(9, 5);
                m(9, 1);
                d(11, 1);
                d(12, 2);
                d(12, 6);
                m(13, 4);
                d(14, 3);
                d(15, 4);
                d(15, 7);
                d(14, 8);
                d(14, 10);
                d(13, 11);
                d(13, 12);
                d(12, 13);
                d(12, 14);
                return;
            case DROPPER:
                xOffset = x;
                yOffset = y;
                g.setColor(foregroundColor);
                //m(0,0); d(17,0); d(17,17); d(0,17); d(0,0);
                m(12, 2);
                d(14, 2);
                m(11, 3);
                d(15, 3);
                m(11, 4);
                d(15, 4);
                m(8, 5);
                d(15, 5);
                m(9, 6);
                d(14, 6);
                m(10, 7);
                d(12, 7);
                d(12, 9);
                m(8, 7);
                d(2, 13);
                d(2, 15);
                d(4, 15);
                d(11, 8);
                g.setColor(backgroundColor);
                m(0, 0);
                d(16, 0);
                d(16, 16);
                d(0, 16);
                d(0, 0);
                return;
            case ANGLE:
                xOffset = x + 1;
                yOffset = y + 2;
                m(0, 11);
                d(11, 0);
                m(0, 11);
                d(15, 11);
                m(10, 11);
                d(10, 8);
                m(9, 7);
                d(9, 6);
                dot(8, 5);
                return;
        }
    }

    void drawTriangle(int x, int y) {
        g.setColor(triangleColor);
        xOffset += x;
        yOffset += y;
        m(0, 0);
        d(4, 0);
        m(1, 1);
        d(3, 1);
        dot(2, 2);
    }

    void drawPoint(int x, int y) {
        g.setColor(toolColor);
        m(x - 2, y);
        d(x + 2, y);
        m(x, y - 2);
        d(x, y + 2);
        g.setColor(Roi.getColor());
        dot(x, y);
    }

    void drawIcon(Graphics g, int tool, int x, int y) {
        if (null == g) {
            return;
        }
        icon = icons[tool];
        this.icon = icon;
        int length = icon.length();
        int x1, y1, x2, y2;
        pc = 0;
        while (true) {
            char command = icon.charAt(pc++);
            if (pc >= length) {
                break;
            }
            switch (command) {
                case 'B':
                    x += v();
                    y += v();
                    break;  // reset base
                case 'R':
                    g.drawRect(x + v(), y + v(), v(), v());
                    break;  // rectangle
                case 'F':
                    g.fillRect(x + v(), y + v(), v(), v());
                    break;  // filled rectangle
                case 'O':
                    g.drawOval(x + v(), y + v(), v(), v());
                    break;  // oval
                case 'o':
                    g.fillOval(x + v(), y + v(), v(), v());
                    break;  // filled oval
                case 'C':
                    g.setColor(new Color(v() * 16, v() * 16, v() * 16));
                    break; // set color
                case 'L':
                    g.drawLine(x + v(), y + v(), x + v(), y + v());
                    break; // line
                case 'D':
                    g.fillRect(x + v(), y + v(), 1, 1);
                    break; // dot
                case 'P': // polyline
                    x1 = x + v();
                    y1 = y + v();
                    while (true) {
                        x2 = v();
                        if (x2 == 0) {
                            break;
                        }
                        y2 = v();
                        if (y2 == 0) {
                            break;
                        }
                        x2 += x;
                        y2 += y;
                        g.drawLine(x1, y1, x2, y2);
                        x1 = x2;
                        y1 = y2;
                    }
                    break;
                case 'T': // text (one character)
                    x2 = x + v();
                    y2 = y + v();
                    int size = v() * 10 + v();
                    char[] c = new char[1];
                    c[0] = pc < icon.length() ? icon.charAt(pc++) : 'e';
                    g.setFont(new Font("SansSerif", Font.BOLD, size));
                    g.drawString(new String(c), x2, y2);
                    break;
                default:
                    break;
            }
            if (pc >= length) {
                break;
            }
        }
        if (menus[tool] != null && menus[tool].getSubElements().length > 0) {
            xOffset = x;
            yOffset = y;
            drawTriangle(14, 14);
        }
    }

    private void m(int x, int y) {
        this.x = xOffset + x;
        this.y = yOffset + y;
    }

    private void d(int x, int y) {
        x += xOffset;
        y += yOffset;
        g.drawLine(this.x, this.y, x, y);
        this.x = x;
        this.y = y;
    }

    private void dot(int x, int y) {
        g.fillRect(x + xOffset, y + yOffset, 1, 1);
    }

    int v() {
        if (pc >= icon.length()) {
            return 0;
        }
        char c = icon.charAt(pc++);
        //IJ.log("v: "+pc+" "+c+" "+toInt(c));
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'a':
                return 10;
            case 'b':
                return 11;
            case 'c':
                return 12;
            case 'd':
                return 13;
            case 'e':
                return 14;
            case 'f':
                return 15;
            default:
                return 0;
        }
    }
*/
 }

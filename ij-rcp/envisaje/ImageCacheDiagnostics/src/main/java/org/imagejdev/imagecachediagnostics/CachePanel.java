/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.imagecachediagnostics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.imagejdev.imagenio.nio.CacheManager;
import org.imagejdev.imagenio.nio.CacheMap;
import org.imagejdev.imagenio.nio.CacheMap.Region;
import org.openide.windows.TopComponent;

/**
 *
 * @author tim
 */
public class CachePanel extends JPanel implements ActionListener, MouseListener {
    private final Timer timer = new Timer (15000, this);
    @Override
    public void addNotify() {
        super.addNotify();
        startTimer();
        addMouseListener(this);
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        stopTimer();
        removeAll();
        removeMouseListener(this);
    }

    private void startTimer() {
        timer.setRepeats(true);
        timer.start();
        actionPerformed(null);
    }

    private void stopTimer() {
        timer.stop();
    }

    public void actionPerformed(ActionEvent e) {
        removeAll();
        CacheMap map = CacheManager.getMap();
        List <Region> regions = map.getRegions();
        System.err.println("Updating diagnostics.  " + regions.size() + " regions");
        for (Region r : regions) {
            RegionPanel nue = new RegionPanel (r, map);
            System.err.println(nue);
            nue.addMouseListener(this);
            add (nue);
        }
        if (regions.isEmpty()) {
            JLabel jl = new JLabel ("Cache is empty");
            add (jl);
            jl.addMouseListener(this);
        }
        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
        if (tc != null) {
            long kSize = map.getCacheFileLength() / (1024 * 1024);
            tc.setDisplayName("Diagnostics - File Size " + kSize + "Mb");
        }
        invalidate();
        revalidate();
        repaint();
    }
    
    @Override
    public void doLayout() {
        Component[] comps = getComponents();
        int x = 0;
        if (comps.length > 1) {
            for (Component c : comps) {
                Dimension d = c.getPreferredSize();
                c.setBounds (x, 0, d.width, d.height);
                x += d.width;
            }
        } else {
            comps[0].setBounds (0, 0, getWidth(), getHeight());
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 2) {
            actionPerformed(null);
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}

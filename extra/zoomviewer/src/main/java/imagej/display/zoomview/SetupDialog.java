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

package imagej.display.zoomview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.io.File;
import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.TitledBorder;


/**
 *
 * @author aivar
 */
public class SetupDialog extends JDialog {
    public enum Source { FILE, FAKE };
    public enum Pixels { ACTUAL, X4, X16, MP50, MP100, MP500, GP1, GP4 };
    static int s_cacheSize;
    static String s_fileName = "";
    static Source s_source = Source.FAKE;
    static Pixels s_pixels = Pixels.GP1;
    private JTextField m_cacheSizeTextField;
    private JTextField m_fileNameTextField;
    private ButtonGroup m_inputGroup;
    private ButtonGroup m_group;

    public SetupDialog(JFrame parent) {
        super(parent, true);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        m_cacheSizeTextField = new JTextField("1000", 25);

        JRadioButton fakeButton = new JRadioButton("Fake image");
        fakeButton.setSelected(true);
        fakeButton.setActionCommand(Source.FAKE.name());
        JRadioButton fileButton = new JRadioButton("Load file");
        fileButton.setActionCommand(Source.FILE.name());
        m_inputGroup = new ButtonGroup();
        m_inputGroup.add(fakeButton);
        m_inputGroup.add(fileButton);

        m_fileNameTextField = new JTextField(25);

        JPanel inputPanel = new JPanel(new GridLayout(0, 1));
        inputPanel.add(new JLabel("Cache size in MB:"));
        inputPanel.add(m_cacheSizeTextField);

        inputPanel.add(fakeButton);
        inputPanel.add(fileButton);

        inputPanel.add(new JLabel("Image file name:"));

        inputPanel.add(m_fileNameTextField);
        add(inputPanel);

        // Create the radio buttons.
        JRadioButton actualSizeButton = new JRadioButton("Actual Size");
        actualSizeButton.setSelected(true);
        actualSizeButton.setActionCommand(Pixels.ACTUAL.name());

        JRadioButton x4Button = new JRadioButton("4x");
        x4Button.setActionCommand(Pixels.X4.name());

        JRadioButton x16Button = new JRadioButton("16x");
        x16Button.setActionCommand(Pixels.X16.name());

        JRadioButton mp50Button = new JRadioButton("50 Megapixel");
        mp50Button.setActionCommand(Pixels.MP50.name());

        JRadioButton mp100Button = new JRadioButton("100 Megapixel");
        mp100Button.setActionCommand(Pixels.MP100.name());

        JRadioButton mp500Button = new JRadioButton("500 Megapizel");
        mp500Button.setActionCommand(Pixels.MP500.name());

        JRadioButton gp1Button = new JRadioButton("1 Gigapixel");
        gp1Button.setActionCommand(Pixels.GP1.name());

        JRadioButton gp4Button = new JRadioButton("4 Gigapixel");
        gp4Button.setActionCommand(Pixels.GP4.name());

        //Group the radio buttons.
        m_group = new ButtonGroup();
        m_group.add(actualSizeButton);
        m_group.add(x4Button);
        m_group.add(x16Button);
        m_group.add(mp50Button);
        m_group.add(mp100Button);
        m_group.add(mp500Button);
        m_group.add(gp1Button);
        m_group.add(gp4Button);

        //Put the radio buttons in a column in a panel.
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(actualSizeButton);
        radioPanel.add(x4Button);
        radioPanel.add(x16Button);
        radioPanel.add(mp50Button);
        radioPanel.add(mp100Button);
        radioPanel.add(mp500Button);
        radioPanel.add(gp1Button);
        radioPanel.add(gp4Button);

        add(radioPanel);
        //setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JButton button = new JButton("OK");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                s_cacheSize = Integer.parseInt(m_cacheSizeTextField.getText());
                s_fileName = m_fileNameTextField.getText();
                s_source = Enum.valueOf(Source.class, m_inputGroup.getSelection().getActionCommand());
                s_pixels = Enum.valueOf(Pixels.class, m_group.getSelection().getActionCommand());
                setVisible(false);
            }
        });
        add(button);

        pack();
        center(this);
        setVisible(true);
    }

    public static int getCacheSize() {
        return s_cacheSize;
    }

    public static String getFileName() {
        return s_fileName;
    }

    public static Source getSource() {
        return s_source;
    }

    public static Pixels getPixels() {
        return s_pixels;
    }

    private static void center(final Window win) {
        final Dimension size = win.getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = (screen.width - size.width) / 2;
        final int h = (screen.height - size.height) / 2;
        win.setLocation(w, h);
    }
}

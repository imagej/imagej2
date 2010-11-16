/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx.plugin;

import ijx.ExecuterIjx;
import ijx.sezpoz.ActionIjx;
import ij.plugin.PlugIn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author GBH
 */
public class PluginIjx implements PlugIn {
    @Override
    public void run(String arg) {
        System.out.println("I have been run.");
        //
    }

    //==========================================================
    @ActionIjx(label = "TestItem",
              menu = "File",
              commandKey = "commandName",
              toolbar = "main",
              hotKey = "alt shift X",
              mnemonic = java.awt.event.KeyEvent.VK_1,
              tip = "Tool tip displayed",
              position = 9,
              separate = true,
              icon = "demo/plugin1/movieNew16gif",
              bundle = "demo.plugin1.properties")

    public static class TestAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // ??
            new ExecuterIjx("theComand()", e).run();
        }
    }
    //==========================================================
}

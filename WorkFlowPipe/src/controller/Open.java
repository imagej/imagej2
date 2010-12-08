package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JTextField;

public class Open {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String referenceURL = "http://pipes.yahoo.com/pipes/pipe.edit";
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		final JTextField url = new JTextField(20);
		url.setText( referenceURL );
		//JButton button = new JButton( "Open Browser" );
		//button.addActionListener( new ActionListener() {
		//	public void actionPerformed( ActionEvent e ) {
				OpenBrowser.openURL( url.getText().trim() );
		//	}
		//});
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		//panel.add(new JLabel("URL:"));
		//panel.add(url);
		//panel.add(button);
		//frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);

	}

}

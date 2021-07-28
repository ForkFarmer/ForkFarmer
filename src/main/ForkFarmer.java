package main;

import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import util.Ico;
import util.jtattoo.hifi.HiFiLookAndFeel;

public class ForkFarmer {
	public static JFrame FRAME;

	public static void main(String[] args) {
		
		try {
			Properties props = new Properties();
			props.put("logoString", "");
			HiFiLookAndFeel.setCurrentTheme(props);
			UIManager.setLookAndFeel(new HiFiLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		javax.swing.SwingUtilities.invokeLater(ForkFarmer::startGUI);
	}
	
	
	private static void startGUI() {
		FRAME = new JFrame("Fork Farmer 0.4a");
		FRAME.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		FRAME.setIconImage(Ico.LOGO.getImage());

		//Create and set up the content pane.
		final MainGui mainPanel = new MainGui();
		mainPanel.setOpaque(true); //content panes must be opaque
		FRAME.setContentPane(mainPanel);

		FRAME.pack();
		FRAME.setLocationRelativeTo(null);
		FRAME.setVisible(true);
	}

	public static void showMsg(String title,String message) {
		JOptionPane.showMessageDialog(FRAME,message,title,JOptionPane.PLAIN_MESSAGE);
	}
		
		

}

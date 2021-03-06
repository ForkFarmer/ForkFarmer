package main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import logging.LogModel;
import util.I18n;
import util.Ico;
import util.jtattoo.hifi.HiFiLookAndFeel;

public class ForkFarmer {
	public static LogModel LOG = new LogModel();
	public static JFrame FRAME;
	public static String[] args;

	public static void main(String[] args) {
		try {
		
			Properties props = new Properties();
			props.put("logoString", "");
			HiFiLookAndFeel.setCurrentTheme(props);
			UIManager.setLookAndFeel(new HiFiLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		Args.args = args;
		javax.swing.SwingUtilities.invokeLater(ForkFarmer::startGUI);
	}
	
	private static void startGUI() {
		Settings.Load();
		
		FRAME = new JFrame(I18n.ForkFarmer.forkFarmerTitle);
		FRAME.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		FRAME.setIconImage(Ico.FFLOGO.getImage());

		//Create and set up the content pane.
		final MainGui mainPanel = new MainGui();
		mainPanel.setOpaque(true); //content panes must be opaque
		FRAME.setContentPane(mainPanel);

		FRAME.pack();
		FRAME.setLocationRelativeTo(null);
		FRAME.setVisible(true);
		
		FRAME.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		    	Settings.Save();
		    }
		});
		
	}
	
	public static JFrame newFrame(String title, ImageIcon ico, JPanel content) {
		JFrame frame = new JFrame(title);
		frame.setContentPane(content);
		showFrame(title,ico,frame);
		return frame;
	}
	
	public static void showFrame(String title, ImageIcon ico, JFrame frame) {
		frame.setIconImage(null == ico ? Ico.LOGO.getImage() : ico.getImage());
		frame.setTitle(title);
		frame.pack();
		frame.setLocationRelativeTo(FRAME);
		frame.setVisible(true);
	}

	public static void showMsg(String title,String message) {
		JOptionPane.showMessageDialog(FRAME,message,title,JOptionPane.PLAIN_MESSAGE);
	}
	
	public static boolean showPopup(String s, JPanel jp) {
		return (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(FRAME,jp,s,JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE));
	}
	
}

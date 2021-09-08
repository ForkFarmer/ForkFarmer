package util;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.GrayFilter;
import javax.swing.ImageIcon;

public final class Ico {
	public static final ImageIcon LOGO 		= loadIcon("icons/fork.png");
	public static final ImageIcon CLIPBOARD	= loadIcon("icons/clipboard.png");
	public static final ImageIcon START 	= loadIcon("icons/start.png");
	public static final ImageIcon STOP 		= loadIcon("icons/stop.png");
	public static final ImageIcon GEAR 		= loadIcon("icons/gear.png");
	public static final ImageIcon EYE 		= loadIcon("icons/eye.png");
	public static final ImageIcon HIDE 		= loadIcon("icons/hide.png");
	public static final ImageIcon REFRESH	= loadIcon("icons/refresh.png");
	public static final ImageIcon P2P		= loadIcon("icons/conn.png");
	public static final ImageIcon PLUS		= loadIcon("icons/plus.png");
	public static final ImageIcon DOLLAR	= loadIcon("icons/dollar.png");
	public static final ImageIcon CLI		= loadIcon("icons/cli.png");
	public static final ImageIcon BUG		= loadIcon("icons/bug.png");
	
	public static final ImageIcon POSAT		= loadIcon("icons/posat.png",16);
	
	//circle icons
	public static final ImageIcon GREEN 	= Ico.loadIcon("icons/circles/green.png");
	public static final ImageIcon RED 		= Ico.loadIcon("icons/circles/red.png");
	public static final ImageIcon YELLOW	= Ico.loadIcon("icons/circles/yellow.png");
	public static final ImageIcon GRAY		= Ico.loadIcon("icons/circles/gray.png");
	
	public static final String FORK_PATH = "icons/forks/";
		
	public static ImageIcon getDisabled(ImageIcon i) {
		final int w = i.getIconWidth();
		final int h = i.getIconHeight();
		GraphicsEnvironment   ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice        gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage      	  image = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
		Graphics2D            g2d = image.createGraphics();
		i.paintIcon( null,  g2d,  0, 0);
		Image gray = GrayFilter.createDisabledImage(image);
		
		return new ImageIcon(gray);
	}
	
	public static ImageIcon loadIcon(final String path, int size) {
		ImageIcon i = loadIcon(path);
		Image img = i.getImage();
		Image newimg = img.getScaledInstance(size, size,  java.awt.Image.SCALE_SMOOTH);
		return new ImageIcon(newimg);
	}
	
	public static ImageIcon loadIcon(final String path)
	{
		try {
			return new ImageIcon(getResource(path));
		} catch (Exception e) {
			throw new RuntimeException("Check your resources for missing icon: " + path);
		}
	}
	
	public static URL getResource(final String path) {
		return Ico.class.getClassLoader().getResource(path);
	}

	public static ImageIcon getForkIcon(String name) {
		try {
			return loadIcon(Ico.FORK_PATH + name + ".png",16);
		} catch (RuntimeException e) {
			try {
				return loadIcon(Ico.FORK_PATH + name + ".jpg",16);
			} catch (RuntimeException ee) {
			}
		}
		return null;
	}
}

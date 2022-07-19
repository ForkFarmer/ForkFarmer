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
	public static final ImageIcon ACTION 	= loadIcon("icons/firework.png");
	public static final ImageIcon EXPLORE 	= loadIcon("icons/explore.png");
	public static final ImageIcon START 	= loadIcon("icons/start.png");
	public static final ImageIcon STOP 		= loadIcon("icons/stop.png");
	public static final ImageIcon GEAR 		= loadIcon("icons/gear.png");
	public static final ImageIcon EYE 		= loadIcon("icons/eye.png");
	public static final ImageIcon HIDE 		= loadIcon("icons/hide.png");
	public static final ImageIcon REFRESH	= loadIcon("icons/refresh.png");
	public static final ImageIcon P2P		= loadIcon("icons/conn.png");
	public static final ImageIcon P2P_GREY	= getDisabled(P2P);
	public static final ImageIcon PLUS		= loadIcon("icons/plus.png");
	public static final ImageIcon DOLLAR	= loadIcon("icons/dollar.png");
	public static final ImageIcon BUG		= loadIcon("icons/bug.png");
	public static final ImageIcon WALLET	= loadIcon("icons/wallet.png",16);
	public static final ImageIcon WALLET_COLD= loadIcon("icons/wallet_cold.png",16);
	public static final ImageIcon WALLET_GRAY= loadIcon("icons/wallet_gray.png",16);
	public static final ImageIcon GRAPH		= loadIcon("icons/graph.png",16);
	public static final ImageIcon HANDSHAKE	= loadIcon("icons/handshake.png",16);
	public static final ImageIcon FCX		= loadIcon("icons/fcx.png",16);
	public static final ImageIcon XCHF		= loadIcon("icons/xchforks.png",16);
	public static final ImageIcon ATB		= loadIcon("icons/alltheblocks.png",16);
	public static final ImageIcon ATB_G		= loadIcon("icons/atb_green.png",16);
	public static final ImageIcon ATB_GRAY	= getDisabled(ATB);
	public static final ImageIcon ATB_R		= loadIcon("icons/atb_red.png",16);
	public static final ImageIcon ATB_Y		= loadIcon("icons/atb_yellow.png",16);
	public static final ImageIcon DOWNLOAD	= loadIcon("icons/download.png");
	public static final ImageIcon TARGET	= loadIcon("icons/target.png");
	
	public static final ImageIcon POSAT		= loadIcon("icons/posat.png",16);
	public static final ImageIcon EDIT_START = loadIcon("icons/edit_start.png",16);
	public static final ImageIcon POWER 	= loadIcon("icons/power.png");
	public static final ImageIcon PORTS 	= loadIcon("icons/ports.png");
	public static final ImageIcon QUESTION 	= loadIcon("icons/question.png");
	public static final ImageIcon TOOLS 	= loadIcon("icons/tools.png");
	public static final ImageIcon PEOPLE 	= loadIcon("icons/people.png");
	public static final ImageIcon SNOW		= loadIcon("icons/snow.png",16);
	public static final ImageIcon CAT		= loadIcon("icons/cat.png");
	public static final ImageIcon EXCHANGE	= loadIcon("icons/exchange.png");
	
	public static final ImageIcon ROULETTE 	= loadIcon("icons/roulette.png");
	
	public static final ImageIcon CLI			= loadIcon("icons/cmd.png");
	public static final ImageIcon POWERSHHELL	= loadIcon("icons/powershell.png");
	public static final ImageIcon TERMINAL	= loadIcon("icons/bash.png",16);
	
	public static final ImageIcon GITHUB	= loadIcon("icons/github.png",16);
	public static final ImageIcon XCHCALC	= loadIcon("icons/xchcalc.png");
	public static final ImageIcon HOME		= loadIcon("icons/home.png",16);
	public static final ImageIcon DISCORD	= loadIcon("icons/discord.png",16);
	public static final ImageIcon TWITTER	= loadIcon("icons/twitter.png");
	public static final ImageIcon FFLOGO	= loadIcon("icons/fflogo.png",16);
	
	public static final ImageIcon PAINT	= loadIcon("icons/paint.png");
	
	public static final ImageIcon TROPHY	= loadIcon("icons/trophy.png",14);
	public static final ImageIcon TROPHY_GR	= loadIcon("icons/trophy_g.png",14);
	
	public static final ImageIcon EXPAND	= loadIcon("icons/expand.png",16);
	public static final ImageIcon KEY		= loadIcon("icons/key.png",16);
	public static final ImageIcon COPY_DIR	= loadIcon("icons/folder_new.png",16);
	
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
			return new ImageIcon(Util.getResource(path));
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

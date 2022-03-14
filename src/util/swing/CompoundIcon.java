package util.swing;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class CompoundIcon implements Icon
{
	private transient final List<ImageIcon> iconList;

	public CompoundIcon(final List<ImageIcon> iconList) {
		this.iconList = iconList;
	}

	@Override
	public int getIconHeight() {
		final Optional<ImageIcon> maxIcon = iconList.stream().max(Comparator.comparing(ImageIcon::getIconHeight));
		return maxIcon.isPresent() ? maxIcon.get().getIconHeight() : 0;
	}

	@Override
	public int getIconWidth() {
		return iconList.stream().mapToInt(ImageIcon::getIconWidth).sum() + iconList.size();
	}

	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		int xPos = x;
		for (final Icon icon: iconList)
		{
			icon.paintIcon(c, g, xPos, y);
			xPos += icon.getIconWidth()+1;
		}
	
	}
	
	public ImageIcon getImage() {
	      int w = getIconWidth();
	      int h = getIconHeight();
	      GraphicsEnvironment ge = 
	        GraphicsEnvironment.getLocalGraphicsEnvironment();
	      GraphicsDevice gd = ge.getDefaultScreenDevice();
	      GraphicsConfiguration gc = gd.getDefaultConfiguration();
	      BufferedImage image = gc.createCompatibleImage(w, h,BufferedImage.TYPE_INT_ARGB);
	      Graphics2D g = image.createGraphics();
	      int xPos = 0;
			for (final ImageIcon icon: iconList)
			{
				g.drawImage(icon.getImage(),xPos,0,null);
				xPos += icon.getIconWidth()+1;
			}
	      
	      g.dispose();
	      return new ImageIcon(image);
	}
}
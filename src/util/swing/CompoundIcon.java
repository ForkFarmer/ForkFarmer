package util.swing;
import java.awt.Component;
import java.awt.Graphics;
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
}
package util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class CPanel extends JPanel {

	public CPanel() {
		super(new BorderLayout());
	}

	public CPanel addLS (final Component c) {
		add(c,BorderLayout.LINE_START);
		return this;
	}

	public CPanel addLE (final Component c) {
		add(c,BorderLayout.LINE_END);
		return this;
	}

	public CPanel addPS (final Component c) {
		add(c,BorderLayout.PAGE_START);
		return this;
	}

	public CPanel addPE (final Component c) {
		add(c,BorderLayout.PAGE_END);
		return this;
	}

	public CPanel addC (final Component c) {
		add(c,BorderLayout.CENTER);
		return this;
	}
	
	public CPanel setB(final Border b) {
		setBorder(b);
		return this;
	}
	
	public CPanel setPSize(final Dimension d) {
		super.setPreferredSize(d);
		return this;
	}


}

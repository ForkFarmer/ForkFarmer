package util.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import util.Util;

@SuppressWarnings("serial")
public class SwingEX {
	// most of these classes are to save a few lines of code for commonly used component idioms

	public static class JPM extends JPopupMenu {
		Runnable onVisible;
		Runnable onInvisible;
		Runnable onCanceled;
		
		public JPM(final JMenuItem... iList) {
			for (final JMenuItem i : iList) {
				if (null == i)
					addSeparator();
				else
					add(i);
			}
			
			addPopupMenuListener(new PopupMenuListener() {
				@Override public void popupMenuCanceled(PopupMenuEvent pme) {Util.runIfAble(onCanceled);};
				@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {Util.runIfAble(onInvisible);}
				@Override public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {Util.runIfAble(onVisible);}
			});
			
			
		}
		
		public JPM onVisible(Runnable r) {
			onVisible = r;
			return this;
		}
		
		public JPM onInvisible(Runnable r) {
			onInvisible = r;
			return this;
		}
		
		public JPM onCanceled(Runnable r) {
			onCanceled = r;
			return this;
		}

	}
	
	public static class JMI extends JMenuItem {
		public JMI(final String title,final ImageIcon ico, Runnable r) {
			super(title,ico);
			addActionListener(ae -> r.run());
		}
	}
	
	public static class JMCI extends JCheckBoxMenuItem {
		public JMCI(final String title, Runnable r) {
			super(title);
			addActionListener(ae -> r.run());
		}
	}


	public static class Btn extends JButton {
		public Btn(String title, final ImageIcon ico, Runnable r) {
			super(title,ico);
			addActionListener(ae -> r.run());
		}
	}
	
	public static class RBtn extends JRadioButton {
		public RBtn(String title, Runnable r) {
			super(title);
			addActionListener(ae -> r.run());
		}
	}
	
	public static class SBtn extends JButton {
		public SBtn(String title, final ImageIcon ico, Runnable r) {
			super(ico);
			addActionListener(ae -> r.run());
			setToolTipText(title);
		}
		
	}
	
	public static JRadioButtonMenuItem newRBtnMe(final ImageIcon i, Runnable r) {
		final JRadioButtonMenuItem jme = new JRadioButtonMenuItem(i);
		jme.addActionListener(ae -> r.run());
		return jme;
	}
	
	public static class JTB extends JToolBar {
		Component[] cl;
		public JTB(Component...cList) {
			super();
			cl = cList;
			for (Component c : cList)
				add(c);
			this.setFloatable(false);
		}
		
		@Override
		public void setEnabled(boolean b) {
			super.setEnabled(b);
			Arrays.stream(this.getComponents()).forEach(cl -> cl.setEnabled(b));
		}
		
		public JTB setF(boolean b) {
			super.setFloatable(b);
			return this;
		}
		
		
	}
	
	public static class MouseConsumer extends MouseAdapter {
		private transient final Consumer<MouseEvent> cme;

		public MouseConsumer(final Consumer<MouseEvent> cme) {
			super();
			this.cme = cme;
		}

		@Override
		public void mousePressed(final MouseEvent e) {
			cme.accept(e);
		}
	}

	public static class LTPanel extends JPanel {
		private JLabel lbl;
		public JTextField field;
		
		public LTPanel(String s) {
			this (s, null);
		}
		
		public LTPanel(String s, String defS) {
			setLayout(new BorderLayout());
			lbl = new JLabel(s);
			field = new JTextField(defS);
			add(lbl, BorderLayout.LINE_START);
			add(field, BorderLayout.CENTER);
		}
		
		public String getText() {
			return field.getText();
		}
		
		public int getAsInt() {
			return Integer.parseInt(getText());
		}
		
		public double getAsDouble() {
			return Double.parseDouble(getText());
		}

	}
	
	
	
	public static ButtonGroup newBtnGroup(final AbstractButton... bList) {
		final ButtonGroup bg = new ButtonGroup();
		for (final AbstractButton ab : bList)
			bg.add(ab);
		return bg;
	}
		
	public static DocumentListener newDocumentListener(Consumer<DocumentEvent> deConsumer ) {
		return new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent de) {
				deConsumer.accept(de);
			}

			@Override
			public void insertUpdate(DocumentEvent de) {
				deConsumer.accept(de);
			}

			@Override
			public void removeUpdate(DocumentEvent de) {
				deConsumer.accept(de);
			}
			
		};
	}
	
	public static class BLPanel extends JPanel {
		public BLPanel() {
			super(new BorderLayout());
		}

		public BLPanel addLS (final Component c) {
			add(c,BorderLayout.LINE_START);
			return this;
		}

		public BLPanel addLE (final Component c) {
			add(c,BorderLayout.LINE_END);
			return this;
		}

		public BLPanel addPS (final Component c) {
			add(c,BorderLayout.PAGE_START);
			return this;
		}

		public BLPanel addPE (final Component c) {
			add(c,BorderLayout.PAGE_END);
			return this;
		}

		public BLPanel addC (final Component c) {
			add(c,BorderLayout.CENTER);
			return this;
		}
		
		public BLPanel setB(final Border b) {
			setBorder(b);
			return this;
		}
		
		public BLPanel setPSize(final Dimension d) {
			super.setPreferredSize(d);
			return this;
		}
	}
	
	public static class JTF extends JTextField {
		public JTF() {
			super();
		}
		
		public JTF(String s) {
			super(s);
		}
		
		public JTF setEdt(boolean b) {
			super.setEditable(b);
			return this;
		}
		
		public JTF setEna(boolean b) {
			super.setEnabled(b);
			return this;
		}
	}
	
	public static class LblTT extends JLabel {
		public LblTT(String txt, String toolTip) {
			super(txt);
			setHorizontalTextPosition(SwingConstants.LEFT);
			//setIcon(Ico.QUESTION);
			setToolTipText(toolTip);
		}
	}
	
	public static class LIPanel extends JPanel {
		private JLabel lbl;
		public JTextField field;
		
		public LIPanel(String s) {
			this (s, null);
		}
		
		public LIPanel(String s, String defS) {
			setLayout(new BorderLayout());
			lbl = new JLabel(s);
			field = new JTextField(defS);
			add(lbl, BorderLayout.LINE_START);
			add(field, BorderLayout.CENTER);
		}
		
		public String getText() {
			return field.getText();
		}

		/*
		public void addLoadButton() {
			JFileChooser FC = new JFileChooser();
			add(new SwingEX.Btn("Load", Ico.DIRMON, () -> {
				if (JFileChooser.APPROVE_OPTION  == FC.showOpenDialog(MainGui.FRAME)) {
					try {
						field.setText(Util.loadToString(FC.getSelectedFile()));
					} catch (IOException e) {
						field.setText("Error: " + FC.getSelectedFile() + "(" + e.getMessage() +")");
					}	
				}
				
			}), BorderLayout.LINE_END);
			
		}
		*/
	}
	
	public static class JMIco extends JMenu {
		public JMIco(String s, ImageIcon ico) {
			super(s);
			setIcon(ico);
		}
	}
	
	
}

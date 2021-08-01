package forks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import util.Ico;
import util.NetSpace;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.Col;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ForkView extends JPanel {

	@SuppressWarnings("unchecked")
	public static Col<Fork> cols[] = new Col[] {
		new Col<Fork>("",   		22,	Icon.class,		f->f.ico),
		new Col<Fork>("Symbol",   	50,	String.class, 	f->f.symbol),
		new Col<Fork>("Balance",	110,String.class, 	Fork::getBalance),
		new Col<Fork>("Netspace",	80, NetSpace.class, f->f.ns),
		new Col<Fork>("Address",	-1,	String.class, 	f->f.addr),
		new Col<Fork>("Time",		50,	String.class, 	Fork::getReadTime),
		new Col<Fork>("", 		 	22, Icon.class, 	f->f.statusIcon)
	};
	
	final static ForkTableModel MODEL = new ForkTableModel();	
	public static final JTable TABLE = new JTable(MODEL);
	private static final JScrollPane JSP = new JScrollPane(TABLE);
	private static final JPopupMenu POPUP_MENU = new JPopupMenu();
	
	static class ForkTableModel extends JFunTableModel {
		public ForkTableModel() {
			super(cols);
			onGetRowCount(() -> Fork.LIST.size());
			onGetValueAt((r, c) -> cols[c].apply(Fork.LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}

	public ForkView() {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder("Installed Forks:"));
		add(JSP,BorderLayout.CENTER);
		Col.adjustWidths(TABLE,cols);

		JSP.setPreferredSize(new Dimension(700,400));
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		POPUP_MENU.add(new SwingEX.JMI("Start", 	Ico.START, 		() -> getSelectedForks().forEach(Fork::start)));
		POPUP_MENU.add(new SwingEX.JMI("Stop", 		Ico.STOP,  		() -> getSelectedForks().forEach(Fork::stop)));
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("View Log", 	Ico.EYE,  		ForkView::viewLog));
		POPUP_MENU.add(new SwingEX.JMI("New Addr", 	Ico.GEAR, 		() -> getSelectedForks().forEach(Fork::generate)));
		POPUP_MENU.add(new SwingEX.JMI("Copy", 		Ico.CLIPBOARD,  ForkView::copy));
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("Refresh",	Ico.REFRESH,  	ForkView::refresh));
		POPUP_MENU.add(new SwingEX.JMI("Hide", 		Ico.HIDE,  		ForkView::removeSelected));
	}
	
	static private void refresh() {
		getSelectedForks().forEach(Fork::refresh);
	}
	
	static private void removeSelected() {
		List<Fork> selList = getSelectedForks();
		selList.forEach(f -> f.cancel = true);
		Fork.LIST.removeAll(selList);
		update();
	}

	static private void viewLog() {
		getSelectedForks().forEach(Fork::viewLog);
	}
	
	static private void copy() {
		List<Fork> forkList = getSelectedForks();
		StringBuilder sb = new StringBuilder();
		for (Fork f: forkList)
			if (null != f.addr)
				sb.append(f.addr + "\n");
		
		StringSelection stringSelection = new StringSelection(sb.toString());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	public static List<Fork> getSelectedForks() {
		return SwingUtil.getSelected(TABLE, Fork.LIST);
	}

	public static void update() {
		MODEL.fireTableDataChanged();
	}
	
	public static void fireTableRowUpdated(int row) {
		MODEL.fireTableRowsUpdated(row, row);
	}
}

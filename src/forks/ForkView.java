package forks;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.StringWriter;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;

import util.Ico;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.Col;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ForkView extends JPanel {
	private static final JPopupMenu POPUP_MENU 			= new JPopupMenu();
	
	public static Col<?> cols[] = new Col[] {
			new Col<>("",   			22,	Icon.class,		Fork::getIcon),
			new Col<>("Symbol",   		50,	String.class, 	Fork::getSymbol),
			new Col<>("Balance",		65,	String.class, 	Fork::getBalanceStr),
			new Col<>("Address",		-1,	String.class, 	Fork::getAddr),
			new Col<>("Time",			50,	String.class, 	Fork::getReadTime),
			new Col<>("", 		 	 	22, Object.class, 	s -> s)
		};
	
	final static SensorTableModel MODEL = new SensorTableModel();
	private static final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	
	static class SensorTableModel extends JFunTableModel {
		public SensorTableModel() {
			super(cols);
			onGetRowCount(() -> Fork.LIST.size());
			onGetValueAt((r, c) -> cols[c].apply(Fork.LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public class MyRenderer extends DefaultTableCellRenderer {
		  public Component getTableCellRendererComponent(JTable table, Object value,
		                                                 boolean isSelected, boolean hasFocus, 
		                                                 int row, int column) {
		    Fork s = (Fork)value;
		    setIcon(s.sIco);
		    setToolTipText(s.getAddr());
		    return this;
		  }
		}
	
	public ForkView() {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder("Installed Forks:"));
		add(JSP,BorderLayout.CENTER);
		Col.adjustWidths(TABLE,cols);
		TABLE.getColumnModel().getColumn(5).setCellRenderer(new MyRenderer());

		JSP.setPreferredSize(new Dimension(650,300));
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		POPUP_MENU.add(new SwingEX.JMI("Start", 	Ico.START, 		() -> start(ForkView.getSelectedForks())));
		POPUP_MENU.add(new SwingEX.JMI("Stop", 		Ico.STOP,  		() -> stop(ForkView.getSelectedForks())));
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("View Log", 	Ico.EYE,  		() -> viewLog(ForkView.getSelectedForks())));
		POPUP_MENU.add(new SwingEX.JMI("New Addr", 	Ico.GEAR, 		() -> generate(ForkView.getSelectedForks())));
		POPUP_MENU.add(new SwingEX.JMI("Copy", 		Ico.CLIPBOARD,  () -> copy(ForkView.getSelectedForks())));
		
			
	}
	
	private void viewLog(List<Fork> fList) {
		fList.forEach(Fork::viewLog);
	}

	private void generate(List<Fork> fList) {
		fList.forEach(Fork::generate);
	}
	
	
	
	public void copy(List<Fork> fList) {
		StringWriter sw = new StringWriter();
		for (Fork f: fList) {
			String a = f.addr;
			if (null != a)
				sw.write(a + "\n");
		}
		
		StringSelection stringSelection = new StringSelection(sw.toString());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	private void start(List<Fork> fList) {
		fList.forEach(Fork::start);
	}
	
	private void stop(List<Fork> fList) {
		fList.forEach(Fork::stop);
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

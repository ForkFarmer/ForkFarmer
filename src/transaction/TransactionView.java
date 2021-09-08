package transaction;

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
import javax.swing.table.JTableHeader;

import util.Ico;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class TransactionView extends JPanel {
	
	final static TxTableModel MODEL = new TxTableModel();
	private final static JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	private static final JPopupMenu POPUP_MENU = new JPopupMenu();
	private static final JPopupMenu HEADER_MENU = new JPopupMenu();
	
	static class TxTableModel extends JFunTableModel<Transaction> {
		public TxTableModel() {
			super();
			addColumn("",   		22,		Icon.class,		t->t.getIcon()).showMandatory();
			addColumn("Date",   	140,	String.class, 	t->t.date).showMandatory();
			addColumn(" ",  		22,		Icon.class,		t->t.f.ico).show();
			addColumn("Symbol",  	50,		String.class,	t->t.f.symbol).show();
			addColumn("Name", 		80,		String.class, 	t->t.f.name).show();
			addColumn("Effort",		80,		Integer.class, 	t->t.effort).show();
			addColumn("To",   		-1,		String.class, 	t->t.target).showMandatory();
			addColumn("Amount", 	100,	String.class, 	t->t.amount).showMandatory();			
			
			onGetRowCount(() -> Transaction.LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(Transaction.LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public TransactionView() {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder("Transactions:"));
		add(JSP,BorderLayout.CENTER);
		//Col.adjustWidths(TABLE,cols);
		//TABLE.getColumnModel().getColumn(2).setCellRenderer(new MyRenderer());
		JSP.setPreferredSize(new Dimension(950,250));
		
		TABLE.setAutoCreateRowSorter(true);
		TABLE.getRowSorter().toggleSortOrder(1);
		TABLE.getRowSorter().toggleSortOrder(1);
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		POPUP_MENU.add(new SwingEX.JMI("View at posat.io", 	Ico.POSAT, 		() -> getSelected().forEach(Transaction::browse)));
		POPUP_MENU.add(new SwingEX.JMI("Copy", 				Ico.CLIPBOARD,  TransactionView::copy));
		
		SwingUtil.addDoubleClickAction(TABLE, r -> {
			if (1 != getSelected().size())
				return;
			getSelected().get(0).browse();
			
		});
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(HEADER_MENU);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,HEADER_MENU));

		SwingUtil.setColRight(TABLE, 7);
	}
	
	private static List<Transaction> getSelected() {
		return SwingUtil.getSelected(TABLE, Transaction.LIST);
	}

	public static synchronized void refresh() {
		MODEL.fireTableDataChanged();
	}
	
	static private void copy() {
		List<Transaction> list = getSelected();
		StringBuilder sb = new StringBuilder();
		for (Transaction t: list)
			if (null != t.target)
				sb.append(t.target + "\n");
		
		StringSelection stringSelection = new StringSelection(sb.toString());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
}

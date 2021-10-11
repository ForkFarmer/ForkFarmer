package transaction;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;

import main.ForkFarmer;
import main.Settings;
import types.Balance;
import util.Ico;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.Col;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class TransactionView extends JPanel {
	public final static TxTableModel MODEL = new TxTableModel();
	public final static JTable TABLE = new JTable(MODEL);
	public static final JScrollPane JSP = new JScrollPane(TABLE);
	private static final JPopupMenu POPUP_MENU = new JPopupMenu();
	private static final JPopupMenu HEADER_MENU = new JPopupMenu();
	
	public static class TxTableModel extends JFunTableModel<Transaction> {
		int DATE_COLUMN, AMOUNT_COLUMN, PRICE_COLUMN;
		public TxTableModel() {
			super();
			
			@SuppressWarnings("unchecked")
			List<Col<Transaction>> z = (List<Col<Transaction>>) Settings.settings.get("TxView Columns");
			loadColumns(z);
			
			addColumn("",   		22,		Icon.class,		t->t.getIcon()).showMandatory();
			addColumn("Date",   	140,	String.class, 	t->t.date).showMandatory();
			addColumn(" ",  		22,		Icon.class,		t->t.f.ico).show(true);
			addColumn("Symbol",  	50,		String.class,	t->t.f.symbol).show(true);
			addColumn("Name", 		80,		String.class, 	t->t.f.name).show(true);
			addColumn("Effort",		80,		Integer.class, 	t->t.effort).show(true);
			addColumn("To",   		-1,		String.class, 	t->t.target).showMandatory();
			addColumn("Amount", 	100,	Balance.class, 	t->t.amount).showMandatory();
			addColumn("$", 			60,		Balance.class, 	t->t.value);
			
			DATE_COLUMN = getIndex("Date");
			AMOUNT_COLUMN = getIndex("Amount");
			PRICE_COLUMN = getIndex("$");
			
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
		
		SwingUtil.persistDimension(JSP, () -> Settings.GUI.txViewDimension, d -> Settings.GUI.txViewDimension = d);
		
		TABLE.setAutoCreateRowSorter(true);
		TABLE.getRowSorter().toggleSortOrder(MODEL.DATE_COLUMN);
		TABLE.getRowSorter().toggleSortOrder(MODEL.DATE_COLUMN);
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		POPUP_MENU.add(new SwingEX.JMI("View at posat.io", 	Ico.POSAT, 		() -> getSelected().forEach(Transaction::browse)));
		POPUP_MENU.add(new SwingEX.JMI("Copy", 				Ico.CLIPBOARD,  TransactionView::copy));
		POPUP_MENU.add(new SwingEX.JMI("Report", 			Ico.GRAPH,  	TransactionView::report));
		
		SwingUtil.addDoubleClickAction(TABLE, r -> {
			if (1 != getSelected().size())
				return;
			getSelected().get(0).browse();
			
		});
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(HEADER_MENU);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,HEADER_MENU));

		SwingUtil.setColRight(TABLE, MODEL.AMOUNT_COLUMN);
		SwingUtil.setColRight(TABLE, MODEL.PRICE_COLUMN);
	}
	
	private static List<Transaction> getSelected() {
		return SwingUtil.getSelected(TABLE, Transaction.LIST);
	}

	public static synchronized void refresh() {
		SwingUtilities.invokeLater(() -> MODEL.fireTableDataChanged());
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
	
	static private void report() {
		TxReportView trv  = new TxReportView(getSelected());
		ForkFarmer.showPopup("TxReport: ", trv);
	}
	
}

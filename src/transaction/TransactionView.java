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
import types.Percentage;
import types.TimeU;
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
		public TxTableModel() {
			super();
			
			@SuppressWarnings("unchecked")
			List<Col<Transaction>> z = (List<Col<Transaction>>) Settings.settings.get("TxView Columns");
			loadColumns(z);
			
			addColumn("",   		22,		Icon.class,		t->t.getIcon()).showMandatory().fixed();
			addColumn("Date",   	140,	String.class, 	t->t.date).showMandatory();
			addColumn(" ",  		22,		Icon.class,		t->t.f.ico).show(true).fixed();
			addColumn("Symbol",  	50,		String.class,	t->t.f.symbol).show(true);
			addColumn("Name", 		80,		String.class, 	t->t.f.name).show(true);
			addColumn("Effort",		80,		Percentage.class, 	t->t.effort).show(true);
			addColumn("Prev Win",	90, 	TimeU.class, 	t->t.lastWinTime).show(true);
			addColumn("To",   		450,	String.class, 	t->t.target).show(true).flex();
			addColumn("Amount", 	80,		Balance.class, 	t->t.amount).show(true);
			addColumn("$", 			60,		Balance.class, 	t->t.value).show(true);
			addColumn("RW", 		22,		Icon.class, 	Transaction::getIco).show(true).fixed();
		
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
		
		SwingUtil.setColumnIcon(TABLE,MODEL.getIndex("RW"), Ico.TROPHY_GR);
		
		TABLE.setAutoCreateRowSorter(true);
		TABLE.getRowSorter().toggleSortOrder(MODEL.getIndex("Date"));
		TABLE.getRowSorter().toggleSortOrder(MODEL.getIndex("Date"));
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		POPUP_MENU.add(new SwingEX.JMI("View at ATB.net", 	Ico.ATB, 		() -> getSelected().forEach(Transaction::browse)));
		POPUP_MENU.add(new SwingEX.JMI("Copy", 				Ico.CLIPBOARD,  TransactionView::copy));
		POPUP_MENU.add(new SwingEX.JMI("Report", 			Ico.GRAPH,  	TransactionView::report));
		POPUP_MENU.add(new SwingEX.JMI("Update Reward", 	Ico.TARGET,  	TransactionView::setReward));
		
		SwingUtil.addDoubleClickAction(TABLE, r -> {
			if (1 != getSelected().size())
				return;
			getSelected().get(0).browse();
			
		});
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(HEADER_MENU);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,HEADER_MENU));

		SwingUtil.setColRight(TABLE, MODEL.getIndex("Amount"));
		SwingUtil.setColRight(TABLE, MODEL.getIndex("$"));
	}
	
	private static List<Transaction> getSelected() {
		return SwingUtil.getSelected(TABLE, Transaction.LIST);
	}

	public static synchronized void refresh() {
		SwingUtilities.invokeLater(() -> MODEL.fireTableDataChanged());
	}
	
	public static synchronized void setReward() {
		List<Transaction> txList = getSelected();
		
		for (Transaction t : txList) {
			t.f.fullReward = t.getAmount();
		}
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
		ForkFarmer.newFrame("TxReport: ", Ico.LOGO, new TxReportView(getSelected()));
	}
	
}

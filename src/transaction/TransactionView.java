package transaction;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;

import forks.Fork;
import main.ForkFarmer;
import main.Settings;
import types.Balance;
import types.Percentage;
import types.TimeU;
import util.I18n;
import util.Ico;
import util.Util;
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
	private final JMenu COPY_SUBMENU = new SwingEX.JMIco(I18n.ForkController.copy, Ico.CLIPBOARD);
	
	public static class TxTableModel extends JFunTableModel<Transaction> {
		public TxTableModel() {
			super();
			@SuppressWarnings("unchecked")
			List<Col<Transaction>> z = (List<Col<Transaction>>) Settings.settings.get("TxView Columns");
			loadColumns(z);
			show = true;
			
			addColumn("",   		22,		Icon.class,		t->t.getIcon()).showMandatory().fixed();
			addColumn("Date",   	140,	String.class, 	t->t.date).showMandatory().colName(I18n.TransactionView.dateColName);
			addColumn(" ",  		22,		Icon.class,		t->t.f.ico).show().fixed();
			addColumn("Symbol",  	50,		String.class,	t->t.f.symbol).colName(I18n.TransactionView.symbolColName);
			addColumn("Name", 		80,		String.class, 	t->t.getName()).colName(I18n.TransactionView.nameColName);
			addColumn("Effort",		80,		Percentage.class, 	t->t.effort).colName(I18n.TransactionView.effortColName);
			addColumn("Prev Win",	90, 	TimeU.class, 	t->t.lastWinTime).colName(I18n.TransactionView.lastWinTimeColName);
			addColumn("To",   		450,	String.class, 	t->t.str).flex().colName(I18n.TransactionView.targetColName);
			addColumn("Amount", 	80,		Balance.class, 	t->t.amount).colName(I18n.TransactionView.amountColName);
//			addColumn("Hash", 		80,		String.class, 	t->t.hash);
			addColumn("$", 			60,		Balance.class, 	t->t.value).show();
			addColumn("RW", 		22,		Icon.class, 	Transaction::getIco).fixed().show();
		
			onGetRowCount(() -> Transaction.LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(Transaction.LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public TransactionView() {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder(I18n.TransactionView.transactionTitle));
		add(JSP,BorderLayout.CENTER);
		//Col.adjustWidths(TABLE,cols);
		//TABLE.getColumnModel().getColumn(2).setCellRenderer(new MyRenderer());
		
		SwingUtil.persistDimension(JSP, () -> Settings.GUI.txViewDimension, d -> Settings.GUI.txViewDimension = d);
		
		SwingUtil.setColumnIcon(TABLE,MODEL.getIndex("RW"), Ico.TROPHY_GR);
		
		TABLE.setAutoCreateRowSorter(true);
		TABLE.getRowSorter().toggleSortOrder(MODEL.getIndex("Date"));
		TABLE.getRowSorter().toggleSortOrder(MODEL.getIndex("Date"));
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		POPUP_MENU.add(new SwingEX.JMI(I18n.TransactionView.viewAtATB, 	Ico.ATB, 		() -> getSelected().forEach(Transaction::browse)));
		
		POPUP_MENU.add(COPY_SUBMENU);
			COPY_SUBMENU.add(new SwingEX.JMI(I18n.ForkController.copyAddress, 	Ico.CLIPBOARD,  TransactionView::copyAddress));
			COPY_SUBMENU.add(new SwingEX.JMI(I18n.ForkController.copyCSV, 		Ico.CLIPBOARD,  TransactionView::copyCSV));
		
		POPUP_MENU.add(new SwingEX.JMI(I18n.TransactionView.report, 			Ico.GRAPH,  	TransactionView::report));
		POPUP_MENU.add(new SwingEX.JMI(I18n.TransactionView.updateReward, 	Ico.TARGET,  	TransactionView::setReward));
		
		SwingUtil.addDoubleClickAction(TABLE, r -> {
			if (1 != getSelected().size())
				return;
			getSelected().get(0).browse();
			
		});
		
		TABLE.getTableHeader().setComponentPopupMenu(MODEL.finalizeColumns(TABLE));
		TABLE.setDefaultRenderer(Object.class, colorRenderer);
		TABLE.setDefaultRenderer(Icon.class, colorRenderer);
		TABLE.setDefaultRenderer(TimeU.class, colorRenderer);
		TABLE.setDefaultRenderer(Balance.class, colorRenderer);
		TABLE.setDefaultRenderer(Percentage.class, colorRenderer);
		
		
		//SwingUtil.setColRight(TABLE, MODEL.getIndex("Amount"));
		//SwingUtil.setColRight(TABLE, MODEL.getIndex("$"));
	}
	
	DefaultTableCellRenderer colorRenderer = new DefaultTableCellRenderer(){
        @Override
        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
            Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

            
            if (null != value) {
	            if (value.getClass() == ImageIcon.class) {
	            	((JLabel)c).setIcon((Icon)value);
	            	((JLabel)c).setText(null);
	            } else {
	            	((JLabel)c).setIcon(null);
	            	((JLabel)c).setText(value.toString());
	            }
	            
	            ((JLabel)c).setHorizontalAlignment(value.getClass() == Balance.class ? JLabel.RIGHT : JLabel.LEFT);
            }
            	
            Transaction t = Transaction.LIST.get(TABLE.convertRowIndexToModel(row));
            
            Fork f = t.f;
            if (null != f.bgColor)
            	c.setBackground(isSelected ? f.bgColor.darker() : f.bgColor);
            else
            	c.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : UIManager.getColor("Table.background"));
            return c;
        }
    };
 	
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
	
	static private void copyAddress() {
		StringBuilder sb = new StringBuilder();

		for (Transaction t: getSelected())
			if (null != t.str)
				sb.append(t.str + "\n");
		
		Util.copyToClip(sb.toString());
	}
	
	static private void copyCSV() {
		StringBuilder sb = new StringBuilder();
		sb.append(I18n.TransactionView.dateColName);
		sb.append(",");
		sb.append(I18n.TransactionView.symbolColName);
		sb.append(",");
		sb.append(I18n.TransactionView.nameColName);
		sb.append(",");
		sb.append(I18n.TransactionView.targetColName);
		sb.append(",");
		sb.append(I18n.TransactionView.amountColName);
		sb.append("\n");
		
		for (Transaction t: getSelected()) {
			sb.append(t.date + ",");
			sb.append(t.f.symbol + ",");
			sb.append(t.f.name + ",");
			sb.append(t.str + ",");
			sb.append(t.getAmount());
			sb.append("\n");
		}
		
		Util.copyToClip(sb.toString());
	}
	
	static private void report() {
		ForkFarmer.newFrame(I18n.TransactionView.title, Ico.LOGO, new TxReportView(getSelected()));
	}

	public static void update(Transaction t) {
		SwingUtilities.invokeLater(() -> {
			t.getIndex().ifPresent(row -> {
				MODEL.fireTableRowsUpdated(row, row);
			});
		});
	}
	
}

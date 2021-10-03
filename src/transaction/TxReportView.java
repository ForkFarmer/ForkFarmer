package transaction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import types.Balance;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class TxReportView extends JPanel {
	List<Transaction> LIST = new ArrayList<>();
	Map<String,Transaction> MAP = new HashMap<>();
	
	private final TxTableModel MODEL = new TxTableModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	private static final JPopupMenu HEADER_MENU = new JPopupMenu();
	
	class TxTableModel extends JFunTableModel<Transaction> {
		public TxTableModel() {
			super();
			
			addColumn(" ",  		22,		Icon.class,		t->t.f.ico).show(true);
			addColumn("Symbol",  	50,		String.class,	t->t.f.symbol).show(true);
			addColumn("Name", 		80,		String.class, 	t->t.f.name).show(true);
			addColumn("Amount", 	100,	Balance.class, 	t->t.amount).showMandatory();			
			
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public TxReportView(List<Transaction> selList) {
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
		JSP.setPreferredSize(new Dimension(250,200));
		
		for (Transaction to : selList) {
			Transaction t = new Transaction(to);
			Transaction tx = MAP.get(t.f.symbol);
			if (null == tx) {
				LIST.add(t);
				MAP.put(t.f.symbol, t);
			} else {
				tx.amount.add(t.amount);
			}
		}
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,HEADER_MENU));
		MODEL.fireTableDataChanged();
		
	}	
	
}

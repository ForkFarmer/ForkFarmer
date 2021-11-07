package transaction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import main.Settings;
import types.Balance;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class TxReportView extends JPanel {
	List<Transaction> LIST = new ArrayList<>();
	Map<String,Transaction> MAP = new HashMap<>();
	
	private final TxTableModel MODEL = new TxTableModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	private final JPopupMenu HEADER_MENU = new JPopupMenu();
	private final JLabel valueLbl = new JLabel();
	private final JLabel effortLbl = new JLabel();
	
	
	class TxTableModel extends JFunTableModel<Transaction> {
		public TxTableModel() {
			super();
			
			addColumn(" ",  		22,		Icon.class,		t->t.f.ico).show(true);
			addColumn("Symbol",  	50,		String.class,	t->t.f.symbol).show(true);
			addColumn("Name", 		-1,		String.class, 	t->t.f.name).show(true);
			addColumn("Amount", 	100,	Balance.class, 	t->t.amount).showMandatory();
			addColumn("$", 			60,		Balance.class, 	t->t.value).show(true);
			
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public TxReportView(List<Transaction> selList) {
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
		JSP.setPreferredSize(new Dimension(320,500));
		
		JPanel topPanel = new JPanel(new BorderLayout());
		//JPanel topCenter = new JPanel(new BorderLayout());
		//topCenter.add(forklbl, BorderLayout.CENTER);
	        
		topPanel.add(valueLbl,BorderLayout.LINE_START);
		//topPanel.add(topCenter, BorderLayout.CENTER);
		topPanel.add(effortLbl,BorderLayout.LINE_END);
		
		add(topPanel,BorderLayout.PAGE_START);
	    
		for (Transaction to : selList) {
			Transaction t = new Transaction(to);
			Transaction tx = MAP.get(t.f.symbol);
			if (null == tx) {
				LIST.add(t);
				t.updateValue();
				MAP.put(t.f.symbol, t);
			} else {
				tx.amount.add(t.amount);
				tx.updateValue();
			}
		}
		
		Balance total = new Balance();
		for (Transaction t : LIST)
			total.add(t.value);
		valueLbl.setText("Value: " + Settings.GUI.currencySymbol + total.toString());
		
		double avg = LIST.stream().map(t -> t.effort.effort).filter(e -> e >= 0).collect(Collectors.averagingDouble(num -> num));
		effortLbl.setText("Average Effort: " + avg + "%");
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,HEADER_MENU));
		MODEL.fireTableDataChanged();
		
	}	
	
}

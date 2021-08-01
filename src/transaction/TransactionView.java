package transaction;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;

import util.swing.jfuntable.Col;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class TransactionView extends JPanel {
	
	@SuppressWarnings("unchecked")
	public static Col<Transaction> cols[] = new Col[] {
		new Col<Transaction>("",   		22,		Icon.class,		t->t.getIcon()),
		new Col<Transaction>("Symbol",  50,		String.class,	t->t.f.symbol),
		new Col<Transaction>("To",   	-1,		String.class, 	t->t.target),
		new Col<Transaction>("Date",   	150,	String.class, 	t->t.date),
		new Col<Transaction>("Amount", 	90,		String.class, 	t->t.amount),
	};
	
	final static SensorTableModel MODEL = new SensorTableModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	
	static class SensorTableModel extends JFunTableModel {
		public SensorTableModel() {
			super(cols);
			onGetRowCount(() -> Transaction.LIST.size());
			onGetValueAt((r, c) -> cols[c].apply(Transaction.LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public class MyRenderer extends DefaultTableCellRenderer {
		  public Component getTableCellRendererComponent(JTable table, Object value,
		                                                 boolean isSelected, boolean hasFocus, 
		                                                 int row, int column) {

			  super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
		                row, column);
		      Font f = new Font("Courier New",Font.BOLD,12); 
			  setFont(f);
		        
			  //Fork s = (Fork)value;
		    //setIcon(s.sIco);
		    //setToolTipText(s.getDetails());
			  
		    return this;
		  }
	}
	
	public TransactionView() {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder("Transactions:"));
		add(JSP,BorderLayout.CENTER);
		Col.adjustWidths(TABLE,cols);
		//TABLE.getColumnModel().getColumn(2).setCellRenderer(new MyRenderer());
		//SwingUtil.addDoubleClickAction(TABLE, MSG_QUEUE, LogEvent::view);
		JSP.setPreferredSize(new Dimension(800,250));
		
		TABLE.setAutoCreateRowSorter(true);
		TABLE.getRowSorter().toggleSortOrder(3);
		TABLE.getRowSorter().toggleSortOrder(3);
	}

	public static void refresh() {
		MODEL.fireTableDataChanged();
	}
	
	
}

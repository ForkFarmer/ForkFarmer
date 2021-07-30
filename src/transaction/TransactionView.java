package transaction;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	public static List<Transaction> LIST = new ArrayList<>();
	
	public static Col<?> cols[] = new Col[] {
		new Col<>("",   		22,		Icon.class,		Transaction::getIconR),
		new Col<>("Symbol",   	40,		String.class,	Transaction::getSymbol),
		new Col<>("To",   		-1,		String.class, 	Transaction::getTarget),
		new Col<>("Date",   	140,	String.class, 	Transaction::getDate),
		new Col<>("Amount", 	90,		String.class, 	Transaction::getAmount),
	};
	
	final static SensorTableModel MODEL = new SensorTableModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	
	static class SensorTableModel extends JFunTableModel {
		public SensorTableModel() {
			super(cols);
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> cols[c].apply(LIST.get(r)));
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
		JSP.setPreferredSize(new Dimension(750,200));
		
		TABLE.setAutoCreateRowSorter(true);
	}

	public static void refresh() {
		if (false == Transaction.newTX)
			return;
		Transaction.newTX = false;
		
		LIST = Transaction.TMAP.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
		/*LIST = Transaction.TMAP.entrySet().stream()
			    .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
			    .map(Map.Entry::getValue)
			    .collect(Collectors.toList());
		*/
		MODEL.fireTableDataChanged();
	}
	
	
}

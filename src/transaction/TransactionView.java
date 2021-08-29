package transaction;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import util.swing.SwingUtil;
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
	
	final static TxTableModel MODEL = new TxTableModel();
	private final static JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	
	static class TxTableModel extends JFunTableModel {
		public TxTableModel() {
			super(cols);
			onGetRowCount(() -> Transaction.LIST.size());
			onGetValueAt((r, c) -> cols[c].apply(Transaction.LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	/*
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
	*/
	
	private static void browse(Transaction t) {
			String name = t.f.name;
			String addr = t.target;
		    try {
		    	if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		    		Desktop.getDesktop().browse(new URI("https://" + name + ".posat.io/address/" + addr));
		    	}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		
	}
	
	public TransactionView() {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder("Transactions:"));
		add(JSP,BorderLayout.CENTER);
		Col.adjustWidths(TABLE,cols);
		//TABLE.getColumnModel().getColumn(2).setCellRenderer(new MyRenderer());
		JSP.setPreferredSize(new Dimension(950,250));
		
		TABLE.setAutoCreateRowSorter(true);
		TABLE.getRowSorter().toggleSortOrder(3);
		TABLE.getRowSorter().toggleSortOrder(3);
		
		//TABLE.setComponentPopupMenu(POPUP_MENU);
		//POPUP_MENU.add(new SwingEX.JMI("Start", 	Ico.MACHINE, 	TransactionView::view));
		
		SwingUtil.addDoubleClickAction(TABLE, r -> {
			if (1 != getSelected().size())
				return;
			Transaction t = getSelected().get(0);
			browse(t);
		});
		
		
	}
	
	private static List<Transaction> getSelected() {
		return SwingUtil.getSelected(TABLE, Transaction.LIST);
	}

	public static synchronized void refresh() {
		MODEL.fireTableDataChanged();
	}
	
	
}

package logging;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import util.LimitedQueue;
import util.swing.jfuntable.Col;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class LogView extends JPanel {
	private final LimitedQueue<LogEvent> MSG_QUEUE = new LimitedQueue<>(20);
	private final LogTableModel MODEL = new LogTableModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	
	public static Col<?> cols[] = new Col[] {
		new Col<>("Time",   		150,	String.class, LogEvent::getTime),
		new Col<>("Description",  	 -1,	String.class, LogEvent::getDetails)
	};
	
	class LogTableModel extends JFunTableModel {
		public LogTableModel() {
			super(cols);
			onGetRowCount(() -> MSG_QUEUE.size());
			onGetValueAt((r, c) -> cols[c].apply(MSG_QUEUE.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public LogView() {
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
		Col.adjustWidths(TABLE,cols);
		//SwingUtil.addDoubleClickAction(TABLE, MSG_QUEUE, LogEvent::view);
		JSP.setPreferredSize(new Dimension(300,200));
	}
	
	public synchronized void addToView(LogEvent le) {
		MSG_QUEUE.add(le);
		int idx = MSG_QUEUE.indexOf(le);
		MODEL.fireTableDataChanged();
		TABLE.scrollRectToVisible(TABLE.getCellRect(idx, 0, true));
	}
	
}

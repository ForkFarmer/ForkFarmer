package logging;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import util.LimitedQueue;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class LogView extends JPanel {
	private static final LimitedQueue<LogEvent> MSG_QUEUE = new LimitedQueue<>(50);
	private static final LogTableModel MODEL = new LogTableModel();
	private static final JTable TABLE = new JTable(MODEL);
	private static final JScrollPane JSP = new JScrollPane(TABLE);
	
	static class LogTableModel extends JFunTableModel<LogEvent> {
		public LogTableModel() {
			
			addColumn("Time",   		200,	String.class, LogEvent::getTime);
			addColumn("Description",  	 -1,	String.class, LogEvent::getDetails);
			
			onGetRowCount(() -> MSG_QUEUE.size());
			onGetValueAt((r, c) -> colList.get(c).apply(MSG_QUEUE.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public LogView() {
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,null));
		
		//SwingUtil.addDoubleClickAction(TABLE, MSG_QUEUE, LogEvent::view);
		JSP.setPreferredSize(new Dimension(300,200));
	}
	
	public static synchronized void addToView(LogEvent le) {
		MSG_QUEUE.add(le);
		int idx = MSG_QUEUE.indexOf(le);
		SwingUtilities.invokeLater(() -> {
			MODEL.fireTableDataChanged();
			TABLE.scrollRectToVisible(TABLE.getCellRect(idx, 0, true));
		});
	}
	
	public static synchronized void add(String msg) {
		addToView(new LogEvent(msg));
	}

	
	
}

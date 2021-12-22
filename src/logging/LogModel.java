package logging;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import util.I18n;
import util.LimitedQueue;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class LogModel extends JFunTableModel<LogEvent> {
	final LimitedQueue<LogEvent> MSG_QUEUE = new LimitedQueue<>(50);
		
	public LogModel() {
		addColumn("Time",   		200,	String.class, LogEvent::getTime).colName(I18n.LogModel.timeColName);
		addColumn("Description",  	 -1,	String.class, LogEvent::getDetails).colName(I18n.LogModel.descriptionColName);
			
		onGetRowCount(() -> MSG_QUEUE.size());
		onGetValueAt((r, c) -> colList.get(c).apply(MSG_QUEUE.get(r)));
		onisCellEditable((r, c) -> false);
		
		MSG_QUEUE.setReverse();
	}
		
	public synchronized void addToView(LogEvent le) {
		MSG_QUEUE.add(le);
		SwingUtilities.invokeLater(() -> {
			fireTableDataChanged();
		});
	}
		
	public synchronized void add(String msg) {
		addToView(new LogEvent(msg));
	}
	
	public JFrame newFrameView() {
		JFrame FRAME = new JFrame();
		FRAME.setContentPane(newPanelView());
		return FRAME;
	}
	
	public LogView newPanelView() {
		return new LogView(this);
	}
		
}
	


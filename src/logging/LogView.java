package logging;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class LogView extends JPanel {
	public final JTable TABLE;
	public final JScrollPane JSP;
	
	public LogView(LogModel m) {
		setLayout(new BorderLayout());
		TABLE = new JTable(m);
		JSP = new JScrollPane(TABLE);
		
		add(JSP,BorderLayout.CENTER);
		m.colList.forEach(c -> c.finalize(TABLE,null));
	}
	
	

}

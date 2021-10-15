package util.swing.jfuntable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class KVTable extends JPanel {
	
	public final KVModel MODEL = new KVModel();	
	public final JTable TABLE = new JTable(MODEL);
	List<KVPair> LIST = new ArrayList<>();
	
	public class KVModel extends JFunTableModel<KVPair> {
		
		public KVModel() {
			super();
			
			addColumn("", 100, String.class, kv->kv.key).showMandatory();
			addColumn(" ", -1, String.class, kv->kv.value).showMandatory().editable();
			
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(LIST.get(r)));
			onisCellEditable((r, c) -> colList.get(c).isEditable());
		}
		
		public void setValueAt(Object value, int row, int col) {
			if (1 == col) {
				LIST.get(row).value = (String)value;
			}
	    }

	}
	
	
	public KVTable(List<KVPair> kvList) {
		setLayout(new BorderLayout());
		add(TABLE, BorderLayout.CENTER);
		LIST = kvList;
		SwingUtilities.invokeLater(() -> MODEL.fireTableDataChanged());
		TABLE.setPreferredSize(new Dimension(400,90));

		MODEL.colList.forEach(c -> c.setSelectView(TABLE,null));
	}

}

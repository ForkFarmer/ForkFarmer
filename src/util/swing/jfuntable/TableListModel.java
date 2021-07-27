package util.swing.jfuntable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class TableListModel<T> extends AbstractLambdaTableModel {
	private List<T> l; 
	private Col<?>[] cols;
	public TableListModel(List<T> l, Col<?>[] cols) {
		super();
		this.l = l;
		this.cols = cols;
		onGetRowCount(()    -> this.l.size());
		onGetColumnCount(() -> cols.length);
		onGetValueAt((r, c) -> cols[c].apply(this.l.get(r)));
		onGetColumnClass(c  -> cols[c].getC());
		onGetColumnName(c   -> cols[c].name);
		onisCellEditable((r, c) -> colConsumer(c));
	}
	
	public TableListModel(Col<?>[] cols) {
		this(new ArrayList<T>(), cols);
	}
	
	@Override
	public void setValueAt(final Object v, final int row, final int col) {
		cols[col].set(l.get(row),v);
	}
	
	public void setList(List<T> l) {
		this.l = l;
		this.fireTableDataChanged();
	}
	
	public List<T> getList() {
		return l;
	}
	
	public T getValueAt(int row) {
		return l.get(row);
	}
	
	public boolean colConsumer(int c) {
		return (null != cols[c].consumer);
	}

}

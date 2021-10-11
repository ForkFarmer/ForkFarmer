package util.swing.jfuntable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

@SuppressWarnings("serial")
public class JFunTableModel<T> extends AbstractLambdaTableModel {
	public List<Col<T>> colList = new ArrayList<>();
	public Map<String,Col<T>> colMap = new HashMap<>();
	public boolean loadedColums;
	
	public JFunTableModel() {
		super();
		onGetColumnClass(c     -> colList.get(c).getC());
		onGetColumnCount(()    -> colList.size());
		onGetColumnName(c      -> colList.get(c).name);
		
		onisCellEditable((r,c) -> false);
	}
		
	@SuppressWarnings("unchecked")
	public Col<T> addColumn(final String name, final int width,  final Class<?> type, final Function<T, Object> getValue) {
		Col<T> c = colMap.get(name);
		if (null != c) { // already exist... probably loaded
			c.type = (Class<T>)type;
			c.getValue = getValue;
		} else {
			c = new Col<T>(name,width,type,getValue);
			c.colIndex = colList.size();
			colMap.put(name,c);
			colList.add(c);
		}
		return c;
	}
	
	public int getIndex(String s) {
		return colMap.get(s).colIndex;
	}
	
	public void loadColumns(List<Col<T>> list) {
		if (null != list) {
			colList = list;
			colList.forEach(c -> colMap.put(c.name, c));
			for (int i =0; i < list.size(); i++) {
				Col<T> c = list.get(i);
				c.colIndex = i;
				c.loaded = true;
			}
		}
	}
	
	public List<Col<T>> getColsDisplayOrder(JTable table) {
		List<Col<T>> list = new ArrayList<>();
		for (int i = 0; i < getColumnCount(); i++)
			list.add(null);
		
		 @SuppressWarnings("rawtypes")
		Enumeration e = table.getColumnModel().getColumns();
		    for (int i = 0; e.hasMoreElements(); i++) {
		    	list.set(i, colList.get(((TableColumn) e.nextElement()).getModelIndex()));
		    }
		return list;
	}
	
}

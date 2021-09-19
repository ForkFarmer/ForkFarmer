package util.swing.jfuntable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("serial")
public class JFunTableModel<T> extends AbstractLambdaTableModel {
	public List<Col<T>> colList = new ArrayList<>();
	public Map<String,Col<T>> colMap = new HashMap<>();
	
	public JFunTableModel() {
		super();
		onGetColumnClass(c     -> colList.get(c).getC());
		onGetColumnCount(()    -> colList.size());
		onGetColumnName(c      -> colList.get(c).name);
		
		onisCellEditable((r,c) -> false);
	}
	
	public Col<T> addColumn(Col<T> c) {
		colList.add(c);
		return c;
	}
	
	public Col<T> addColumn(final String name, final int width,  final Class<?> type, final Function<T, Object> getValue) {
		Col<T> c = new Col<T>(name,width,type,getValue);
		c.colIndex = colList.size();
		colMap.put(name,c);
		return addColumn(c);
	}
	
	public int getIndex(String s) {
		return colMap.get(s).colIndex;
	}
}

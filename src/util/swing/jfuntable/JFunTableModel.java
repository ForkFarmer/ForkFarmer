package util.swing.jfuntable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("serial")
public class JFunTableModel<T> extends AbstractLambdaTableModel {
	public List<Col<T>> colList = new ArrayList<>();
	
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
		return addColumn(new Col<T>(name,width,type,getValue));
	}

}

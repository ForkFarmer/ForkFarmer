package util.swing.jfuntable;

import java.util.List;

@SuppressWarnings("serial")
public class TableFieldModel extends TableListModel<TField>
{
	private final List<TField> fList;
	
	public static Col<?> cols[] = new Col[] {
		new Col<>("Field", -1, String.class, tf -> ((TField)tf).name),
		new Col<>("Value", -1, String.class,tf -> ((TField)tf).supplier.get()),
	};
	
	public TableFieldModel(List<TField> fList) {
		super(fList, cols);
		this.fList = fList;
		onisCellEditable((r,c) -> 1 == c);
	}
	
	public void setValueAt(final Object v, final int row, final int col) {
		if (1 == col) {
			fList.get(row).consumer.accept((String)v);
			this.fireTableDataChanged();
		}
	}
	
	

	
}
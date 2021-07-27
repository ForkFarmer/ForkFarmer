package util.swing.jfuntable;

@SuppressWarnings("serial")
public class JFunTableModel extends AbstractLambdaTableModel {
	public JFunTableModel(final Col<?>... cols) {
		super();
		onGetColumnClass(c     -> cols[c].getC());
		onGetColumnCount(()    -> cols.length);
		onGetColumnName(c      -> cols[c].name);
		onisCellEditable((r,c) -> false);
	}
	
	@SuppressWarnings("unused")
	private JFunTableModel(){
		super();
		// force proper constructor
	}
}

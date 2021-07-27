package util.swing.jfuntable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class AbstractLambdaTableModel extends AbstractTableModel {
	private Supplier<Integer> fgetColumnCount;
	private Supplier<Integer> fgetRowCount;
	private Function<Integer, String> fgetColumnName;
	private Function<Integer, Class<?>>  fgetColumnClass;
	private BiFunction<Integer,Integer,Object> fgetValueAt;
	private BiFunction<Integer,Integer,Boolean> fisCellEditable;
	//TriFunction<Object,Integer,Integer,Void> setValueAt;

	/* Implementing setValue seems to not be a good idea */

	@Override
	public Class<?> getColumnClass(final int col) {
		return fgetColumnClass.apply(col);
	}

	@Override
	public int getColumnCount() {
		return fgetColumnCount.get();
	}

	@Override
	public String getColumnName(final int col) {
		return fgetColumnName.apply(col);
	}

	@Override
	public int getRowCount() {
		return fgetRowCount.get();
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		return fgetValueAt.apply(row, col);
	}

	@Override
	public boolean isCellEditable(final int row, final int col) {
		return fisCellEditable.apply(row, col);
	}

	/*
	public AbstractLambdaTableModel onSetValueAt(TriFunction<Object,Integer,Integer,Void> setValueAt)
	{
		this.setValueAt = Objects.requireNonNull(setValueAt);
		return this;
	}
	 */


	public AbstractLambdaTableModel onGetColumnClass(final Function<Integer, Class<?>>  getColumnClass)
	{
		this.fgetColumnClass = Objects.requireNonNull(getColumnClass);
		return this;
	}

	public AbstractLambdaTableModel onGetColumnCount(final Supplier<Integer> getColumnCount)
	{
		this.fgetColumnCount = Objects.requireNonNull(getColumnCount);
		return this;
	}

	public AbstractLambdaTableModel onGetColumnName(final Function<Integer, String> getColumnName)
	{
		this.fgetColumnName = Objects.requireNonNull(getColumnName);
		return this;
	}

	public AbstractLambdaTableModel onGetRowCount(final Supplier<Integer> getRowCount)
	{
		this.fgetRowCount = Objects.requireNonNull(getRowCount);
		return this;
	}

	public AbstractLambdaTableModel onGetValueAt(final BiFunction<Integer,Integer,Object> getValueAt)
	{
		this.fgetValueAt = Objects.requireNonNull(getValueAt);
		return this;
	}

	public AbstractLambdaTableModel onisCellEditable(final BiFunction<Integer,Integer,Boolean> isCellEditable)
	{
		this.fisCellEditable = Objects.requireNonNull(isCellEditable);
		return this;
	}

	/*
	@Override
	public void setValueAt(Object value, int row, int col) {
		setValueAt.apply(value, row, col);
		fireTableCellUpdated(row, col);
    }
	 */



}

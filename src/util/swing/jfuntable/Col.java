package util.swing.jfuntable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

public class Col<T> {
	public String name;
	public int width;
	public boolean show;
	
	public transient int colIndex;
	public transient boolean selectable = true;
	public transient boolean editable = false;
	public transient Class<T> type;
	public transient Function<T, Object> getValue;
	public transient BiConsumer<T,Object> consumer;
	public transient JCheckBoxMenuItem jmi = null;
	public transient boolean loaded;
	public boolean fixed;
	public boolean flex;

	public Col(final String name, final int width,  final Class<?> type, final Function<T, Object> getValue) {
		this(name,width,type,getValue,null);
	}
	
	@SuppressWarnings("unchecked")
	public Col(final String name, final int width, final Class<?> type, final Function<T, Object> getValue, BiConsumer<T,Object> consumer) {
		this.type = null;
		this.name = name;
		this.width = width;
		this.type = (Class<T>) type;
		this.getValue = getValue;
		this.consumer = consumer;
	}
	
	public Col() {
		
	}

	public void resize (JTable table) {
		if (-1 == width)
			return;
		int w = 0;
		for (int z = 0; z < table.getColumnCount(); z++)
			if (name == table.getColumnName(z)) { // bad for dulplicate col names
				w = (null == jmi) ? width : (jmi.isSelected() ? width : 0);
				setWidth(table.getColumnModel().getColumn(z),w);
				show = (0 != w);
			}
	}
	
	public Class<T> getC() {
		return type;
	}
	
	public static void adjustWidths(final JTable table, final Col<?>[] cols) {
		for (int i = 0; i < cols.length; i++) {
			if (-1 == cols[i].width)
				continue;
			for (int z = 0; z < cols.length; z++) { // this is annoying
				if (table.getColumnName(z) == cols[i].name)
					cols[i].setWidth(table.getColumnModel().getColumn(z),cols[i].width);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Object apply(final Object o) {
		return getValue.apply((T)o);
	}

	private void setWidth(TableColumn column, int w) {
		
		column.setMinWidth(flex ? 0 : w);
		
		if (fixed)
			column.setMaxWidth(w);
		else
			column.setMaxWidth(2*w);
		
		column.setPreferredWidth(w);
	}

	@SuppressWarnings("unchecked")
	public void set(Object a, Object b) {
		if(null != consumer)
			consumer.accept((T)a,b);
	}
	
	public void setSelectView(JTable table, JPopupMenu menu) {
		if (selectable && null != menu) {
			jmi = new JCheckBoxMenuItem(name);
			menu.add(jmi);
			jmi.setSelected(show);
			
			jmi.addActionListener(ae -> resize(table));
		}
		resize(table);
	}
	
	public Col<T> showMandatory() {
		selectable = false;
		show = true;
		return this;
	}
	
	public Col<T> show(boolean s) {
		if (!loaded) {
			show = s;
		}
		return this;
	}
	
	public Col<T> index(Consumer<Integer> i) {
		i.accept(colIndex);
		return this;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public Col<T> editable() {
		editable = true;
		return this;
	}

	public Col<T> fixed() {
		fixed = true;
		return this;
	}

	public Col<T> flex() {
		flex = true;
		return this;
	}
	
}

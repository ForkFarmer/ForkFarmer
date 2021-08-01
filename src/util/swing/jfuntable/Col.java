package util.swing.jfuntable;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

public class Col<T> {
	public transient final String name;
	public transient int width;
	public transient final Class<T> type;
	public transient final Function<T, Object> getValue;
	public transient BiConsumer<T,Object> consumer;

	public Col(final String name, final int width,  final Class<?> type, final Function<T, Object> getValue) {
		this(name,width,type,getValue,null);
	}
	
	public Col(final String name, final int width,  final Function<T, Object> getValue) {
		this(name,width,String.class,getValue);
	}
	
	@SuppressWarnings("unchecked")
	public Col(final String name, final int width, final Class<?> type, final Function<T, Object> getValue, BiConsumer<T,Object> consumer) {
		this.name = name;
		this.width = width;
		this.type = (Class<T>) type;
		this.getValue = getValue;
		this.consumer = consumer;
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
					setWidth(table.getColumnModel().getColumn(z),cols[i].width);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Object apply(final Object o) {
		return getValue.apply((T)o);
	}

	public static void setWidth(TableColumn column, int w) {
		column.setMinWidth(w);
		column.setMaxWidth(w);
		column.setPreferredWidth(w);
	}

	@SuppressWarnings("unchecked")
	public void set(Object a, Object b) {
		if(null != consumer)
			consumer.accept((T)a,b);
	}

}

package util.swing.jfuntable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import main.Settings;
import util.swing.SwingUtil;

public class Col<T> {
	public String name;
	public int width;
	public boolean show;
	
	public transient boolean fixed;
	public transient boolean flex;
	public transient int idx;
	public transient Function<T, Object> getValue;
	
	transient Class<T> type;
	transient BiConsumer<T,Object> consumer;
	transient boolean loaded;

	private transient String viewName;
	private transient boolean viewRight;
	private transient boolean selectable = true;
	private transient boolean editable = false;
	private transient JCheckBoxMenuItem jmi = null;
	private transient ImageIcon ico;

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
	
	public void finalize(JTable table, JPopupMenu menu) {
		if (selectable && null != menu) {
			jmi = new JCheckBoxMenuItem(name);
			menu.add(jmi);
			jmi.setSelected(show);
			
			jmi.addActionListener(ae -> resize(table));
		}
		
		if (null != ico)
			SwingUtil.setColumnIcon(table,idx, ico);
		
		if (viewRight)
			SwingUtil.setColRight(table,idx);
		
		if (null != viewName)
			 SwingUtil.setColumnLabel(table, idx, viewName);
		
		if (null != Settings.colMap) {
			String s = (String)Settings.colMap.get(name);
			if (null != s && null != jmi)
				jmi.setToolTipText(s);
		}
		
		
		resize(table);
	}
	
	public Col<T> showMandatory() {
		selectable = false;
		show = true;
		return this;
	}
	
	public Col<T> show() {
		if (!loaded)
			show = true;
		return this;
	}
	
	public Col<T> index(Consumer<Integer> i) {
		i.accept(idx);
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

	public Col<T> colIco(ImageIcon ico) {
		this.ico = ico;
		return this;
	}

	public Col<T> viewRight() {
		viewRight = true;
		return this;
	}

	public void colName(String str) {
		viewName = str;
	}
	
}

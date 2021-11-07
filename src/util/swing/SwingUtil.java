package util.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;

import forks.Fork;

public class SwingUtil {
	
	public static <T> List<T> getSelected(JTable t,List<T> list) {
		
		List<T> res = new ArrayList<>();
		for (int i : t.getSelectedRows()) {
			if (i < list.size()) {
				T obj = list.get(t.convertRowIndexToModel(i));
				if (null != obj) {
					res.add(obj);
				}
			}
		}
		return res;
	}
	
	public static void addToolTipCol(JTable table, int i, Function<Integer,String> sup) {
		table.getColumnModel().getColumn(i).setCellRenderer(new ToolTipRenderer(sup));
	}
	
	@SuppressWarnings("serial")
	public static class ToolTipRenderer extends DefaultTableCellRenderer {
		private final Function<Integer,String> fString;
		
		public ToolTipRenderer(Function<Integer,String> sup) {
			this.fString = sup;
		}
		
	    public Component getTableCellRendererComponent(
	                        JTable table, Object value,
	                        boolean isSelected, boolean hasFocus,
	                        int row, int column) {
	    	int modelIndex = table.convertRowIndexToModel(row); 
	        JLabel c = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, modelIndex, column);
	        c.setToolTipText(fString.apply(modelIndex));
	        return c;
	    }
	}

	public static <T> void removeSelected(JTable t, List<T> list) {
		int selectedRow = t.getSelectedRow();
		
		list.removeAll(getSelected(t,list));
		((AbstractTableModel)t.getModel()).fireTableDataChanged();
		if (selectedRow > list.size()-1)
			selectedRow = list.size()-1;
		if (-1 != selectedRow)
			t.setRowSelectionInterval(selectedRow, selectedRow);
	}

	public static void mapChanges(JTextComponent tc, Consumer<String> c) {
		tc.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				c.accept(tc.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				c.accept(tc.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				c.accept(tc.getText());
			}
			
		});
		
	}
	
	public static DocumentListener addDocListener(Consumer<DocumentEvent> deConsumer ) {
		return new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent de) {
				deConsumer.accept(de);
			}

			@Override
			public void insertUpdate(DocumentEvent de) {
				deConsumer.accept(de);
			}

			@Override
			public void removeUpdate(DocumentEvent de) {
				deConsumer.accept(de);
			}
			
		};
	}
	
	
	public static void addSelectionListener(JTable table, Consumer<Boolean> wasSelected)
	{
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent lse) {
				if (lse.getValueIsAdjusting())
					return;
				wasSelected.accept(-1 != table.getSelectedRow());
			}
		});
	}
	
	@SuppressWarnings("serial")
	public static class NoBorderTableRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setBorder(noFocusBorder);
			return this;
		}
	}

	public static void addDoubleClickAction(JTable t, Consumer<Integer> rowConsumer) {
		t.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent mouseEvent) {
		        JTable table =(JTable) mouseEvent.getSource();
		        Point point = mouseEvent.getPoint();
		        int row = table.rowAtPoint(point);
		        if (-1 == row)
		        	return;
		        if (mouseEvent.getClickCount() == 2)
		        	rowConsumer.accept(row);
		    }
		});
	}
	
	public static void setComponentsEnabled(Component[] cArray, boolean status) {
		for (Component c : cArray)
			c.setEnabled(status);
	}
	
	public static void setColRight(JTable t, int i) {
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		t.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
	}

	public static void mapViewToModel(JTable table, List<Fork> lIST) {
		Fork[] mapping = new Fork[Fork.LIST.size()];
		for (int i = 0; i < mapping.length; i++)
			mapping[i] = Fork.LIST.get(table.convertRowIndexToModel(i));
		Fork.LIST.clear();
		table.getRowSorter().setSortKeys(null);
		for (int i = 0; i < mapping.length; i++)
			Fork.LIST.add(mapping[i]);
		
	}

	public static void persistDimension(Component cmpt, Supplier<Dimension> p, Consumer<Dimension> c) {
		cmpt.setPreferredSize(p.get());
		
		cmpt.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				c.accept(cmpt.getSize());
			}
		});
	}


	private static TableCellRenderer renderer;
	public static void setColumnIcon(JTable table, int idx, ImageIcon ico) {
		if (null == renderer)
			renderer = new SwingEX.JComponentTableCellRenderer();
		TableColumn atbColumn = table.getColumnModel().getColumn(idx);
	    atbColumn.setHeaderRenderer(renderer);
	    atbColumn.setHeaderValue(new JLabel("", ico, JLabel.CENTER));
	}
	
	public static void setColumnEmpty(JTable table, int idx) {
		setColumnLabel(table,idx,"");
	}

	public static void setColumnLabel(JTable table, int idx, String str) {
		if (null == renderer)
			renderer = new SwingEX.JComponentTableCellRenderer();
		TableColumn atbColumn = table.getColumnModel().getColumn(idx);
	    atbColumn.setHeaderRenderer(renderer);
	    atbColumn.setHeaderValue(new JLabel(str, JLabel.CENTER));
	}

	
	
}

package util.swing;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public abstract class ButtonEditor extends DefaultCellEditor implements ActionListener {
private static final long serialVersionUID = 1L;

/** The cell's row. */
protected int row;

/** The cell's column. */
protected int column;

/** The cell's column. */
protected JTable table;

/** The button we are editing. */
protected JButton button;

/** The panel used when editing. */
protected JPanel panel = new JPanel(new GridBagLayout());

/** Constructor */
public ButtonEditor() {super(new JCheckBox());}

/**
 * This method is called when the user try to modify a cell. 
 * In this case it will be called whenever the user click on the cell.
 * @param table
 * @param value
 * @param isSelected
 * @param row
 * @param column
 * @return JPanel The JPanel returned contains a JButton with an ActionListener. 
 */
@Override
public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) { 
    this.row = row;
    this.column = column;
    this.table = table;
    button = (JButton) value;

    //prevent to add the action listener everytime the user click on the cell.
    if(button.getActionListeners().length == 0) button.addActionListener(this); 

    panel.add(button);
    panel.setBackground(table.getGridColor());
    return panel;
}

/**
 * Return a renderer for JButtons. The result is a button centered in the table's cell.
 * @return
 */
public static TableCellRenderer getRenderer() {
    return new TableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.add((JButton) value);
            panel.setBackground(table.getGridColor());
            return panel;
        }
    };
}
}

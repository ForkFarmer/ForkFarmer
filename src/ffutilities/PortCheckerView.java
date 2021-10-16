package ffutilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import forks.Fork;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class PortCheckerView extends JPanel {
	final PortCheckModel MODEL = new PortCheckModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	
	class PortCheckModel extends JFunTableModel<Fork> {
		public PortCheckModel() {
			super();
			
			addColumn(" ",   			22,		Icon.class,		f->f.ico).showMandatory();
			addColumn("Symbol",  		50,		String.class, 	f->f.symbol).showMandatory();
			addColumn("Name",   		-1,		String.class,	f->f.name).showMandatory();
			addColumn("Daemon",   		80,		int.class,	f->f.fp.daemon).showMandatory();
			addColumn("Farmer",   		80,		int.class,	p->p.fp.farmer).showMandatory();
			addColumn("Farmer_rpc",		80,		int.class,	p->p.fp.farmer_rpc).showMandatory();
			addColumn("FullNode",   	80,		int.class, 	p->p.fp.fullnode).showMandatory();
			addColumn("FullNode_rpc", 	80,		int.class, 	p->p.fp.fullnode_rpc).showMandatory();
			addColumn("Harvester",   	80,		int.class, 	p->p.fp.harvester).showMandatory();
			addColumn("Harvester_rpc", 	80,		int.class, 	p->p.fp.harvester_rpc).showMandatory();
			
			onGetRowCount(() -> Fork.LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(Fork.LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public PortCheckerView() {
		setLayout(new BorderLayout());
		add(JSP);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,null));
		int height = 21 * (Fork.LIST.size()+1);
		if (height > 800)
			height = 800;
		JSP.setPreferredSize(new Dimension(750,height));
		
		TABLE.setDefaultRenderer(int.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
                Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                int port = (int) value;
                c.setForeground(Color.WHITE);
                Fork thisFork = Fork.LIST.get(row);
                Fork.LIST.stream()
                	.filter(f -> thisFork != f)
                	.filter(f -> f.fp.anyMatch(port))
                	.findAny()
                	.ifPresent(f -> c.setForeground(Color.RED));
                return c;
            }
        });
	}

}

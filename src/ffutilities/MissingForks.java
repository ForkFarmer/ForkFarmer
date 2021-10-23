package ffutilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import forks.Fork;
import forks.ForkData;
import util.Ico;
import util.Util;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class MissingForks extends JPanel {
	final List<ForkData> LIST = new ArrayList<>();
	final PortCheckModel MODEL = new PortCheckModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	
	class PortCheckModel extends JFunTableModel<ForkData> {
		public PortCheckModel() {
			super();
			
			addColumn(" ",   			22,		Icon.class,		f->f.ico).showMandatory();
			addColumn("Symbol",  		50,		String.class, 	f->f.coinPrefix).showMandatory();
			addColumn("Name",   		-1,		String.class,	f->f.displayName).showMandatory();
			addColumn("UserFolder",  	140,	String.class,	f->f.userFolder).showMandatory();
			addColumn("DaemonFolder", 	160,	String.class, 	f->f.daemonFolder).showMandatory();
			addColumn("1",   			22,		Icon.class,		f->(null != f.websiteURL) ? Ico.HOME : null).showMandatory();
			addColumn("2",   			22,		Icon.class,		f->(null != f.gitURL) ? Ico.GITHUB: null).showMandatory();
			addColumn("3",   			22,		Icon.class,		f->(null != f.discordURL) ? Ico.DISCORD : null).showMandatory();
			
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public MissingForks() {
		setLayout(new BorderLayout());
		add(JSP);
		
		JSP.setPreferredSize(new Dimension(600,500));
		
		for (ForkData fd : ForkData.LIST)
			if (Fork.LIST.stream().noneMatch(f -> f.symbol.equals(fd.coinPrefix)))
				LIST.add(fd);
				
			
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,null));
		
		SwingUtilities.invokeLater(MODEL::fireTableDataChanged);
		
		TABLE.addMouseListener(new MouseAdapter() {
			  public void mouseClicked(MouseEvent e) {
			    if (e.getClickCount() == 2) {
			      JTable target = (JTable)e.getSource();
			      int row = target.getSelectedRow();
			      int column = target.getSelectedColumn();
			      String colName = MODEL.getColsDisplayOrder(TABLE).get(column).name;
			      
			      if (colName.equals("1"))
			      	Util.openLink(LIST.get(row).websiteURL);
			      else if (colName.equals("2"))
			      	Util.openLink(LIST.get(row).gitURL);
			      else if (colName.equals("3"))
			      	Util.openLink(LIST.get(row).discordURL);
			    }
			  }
			});
		
	}

}

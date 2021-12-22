package ffutilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import forks.Fork;
import forks.ForkData;
import forks.ForkView;
import main.MainGui;
import util.I18n;
import util.Ico;
import util.Util;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class MissingForks extends JPanel {
	final JCheckBox viewAllForks = new JCheckBox(I18n.MissingForks.viewAllCheckBoxLabel);
	final List<ForkData> LIST = new ArrayList<>();
	final PortCheckModel MODEL = new PortCheckModel();
	
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	
	class PortCheckModel extends JFunTableModel<ForkData> {
		public PortCheckModel() {
			super();
			
			addColumn("",   			22,		Icon.class,		f->f.atbIcon).showMandatory();
			addColumn(" ",   			22,		Icon.class,		f->f.ico).showMandatory();
			addColumn("Symbol",  		50,		String.class, 	f->f.coinPrefix).showMandatory().colName(I18n.MissingForks.symbolColName);
			addColumn("Name",   		-1,		String.class,	f->f.displayName).showMandatory().colName(I18n.MissingForks.nameColName);
			addColumn("UserFolder",  	140,	String.class,	f->f.userFolder).showMandatory().colName(I18n.MissingForks.userfolderColName);
			addColumn("DaemonFolder", 	160,	String.class, 	f->f.daemonFolder).showMandatory().colName(I18n.MissingForks.daemonfolderColName);
			addColumn("1",   			22,		Icon.class,		f->(null != f.websiteURL) ? Ico.HOME : null).showMandatory();
			addColumn("2",   			22,		Icon.class,		f->(null != f.gitPath) ? Ico.GITHUB: null).showMandatory();
			addColumn("3",   			22,		Icon.class,		f->(null != f.discordURL) ? Ico.DISCORD : null).showMandatory();
			addColumn("4",   			22,		Icon.class,		f->(null != f.twitterURL) ? Ico.TWITTER : null).showMandatory();
			
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public MissingForks() {
		setLayout(new BorderLayout());
		add(viewAllForks,BorderLayout.PAGE_START);
		add(JSP,BorderLayout.CENTER);
		
		JSP.setPreferredSize(new Dimension(600,500));
		
		updateList();
		MODEL.colList.forEach(c -> c.finalize(TABLE,null));
		
		viewAllForks.addActionListener(al -> {
			updateList();
		});
		
		SwingUtilities.invokeLater(MODEL::fireTableDataChanged);
		
		TableCellRenderer renderer = new SwingEX.JComponentTableCellRenderer();
	    		
		TableColumn logoColumn = TABLE.getColumnModel().getColumn(MODEL.getIndex(""));
		logoColumn.setHeaderRenderer(renderer);
		logoColumn.setHeaderValue(new JLabel("", Ico.ATB_GRAY, JLabel.CENTER));
		
		TableColumn homeColumn = TABLE.getColumnModel().getColumn(MODEL.getIndex("1"));
		homeColumn.setHeaderRenderer(renderer);
		homeColumn.setHeaderValue(new JLabel("", Ico.HOME, JLabel.CENTER));
		
		TableColumn gitColumn = TABLE.getColumnModel().getColumn(MODEL.getIndex("2"));
		gitColumn.setHeaderRenderer(renderer);
		gitColumn.setHeaderValue(new JLabel("", Ico.GITHUB, JLabel.CENTER));
		
		TableColumn discordColumn = TABLE.getColumnModel().getColumn(MODEL.getIndex("3"));
		discordColumn.setHeaderRenderer(renderer);
		discordColumn.setHeaderValue(new JLabel("", Ico.DISCORD, JLabel.CENTER));
		
		TableColumn twitterColumn = TABLE.getColumnModel().getColumn(MODEL.getIndex("4"));
		twitterColumn.setHeaderRenderer(renderer);
		twitterColumn.setHeaderValue(new JLabel("", Ico.TWITTER, JLabel.CENTER));
		
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
						Util.openLink(ForkData.GITHUB_URL + LIST.get(row).gitPath);
					else if (colName.equals("3"))
						Util.openLink(LIST.get(row).discordURL);
					else if (colName.equals("4"))
						Util.openLink(LIST.get(row).twitterURL);
				}
			}
		});
		
		final JPopupMenu POPUP_MENU = new JPopupMenu();
		TABLE.setComponentPopupMenu(POPUP_MENU);
		
		POPUP_MENU.add(new SwingEX.JMI(I18n.MissingForks.unhide, null, () -> unhide()));
		
		DefaultTableCellRenderer balanceRendererR = new DefaultTableCellRenderer(){
	        @Override
	        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
	            Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
	            c.setBackground(null);
	            ForkData fd = LIST.get(row);
	            
	            if(Fork.FULL_LIST.stream().anyMatch(f -> f.hidden && !f.cold && f.fd == fd))
	            	c.setBackground(Color.red);
	            return c;
	        }
	    };
	    
	    TABLE.getColumnModel().getColumn(MODEL.getIndex("Name")).setCellRenderer(balanceRendererR);
	}
	
	
	
	public void unhide() {
		List<ForkData> fdList = SwingUtil.getSelected(TABLE, LIST);
		
		List<Fork> hList = Fork.FULL_LIST.stream().filter(f -> f.hidden && !f.cold).collect(Collectors.toList());
		for (Fork f : hList) {
			if (fdList.stream().anyMatch(fd -> fd == f.fd)) {
				f.hidden = false;
				Fork.LIST.add(f);
			}
		}
		ForkView.update();
		updateList();
		MainGui.updateNumForks();
	}
	
	public void updateList() {
		LIST.clear();
		if (viewAllForks.isSelected()) {
			LIST.addAll(ForkData.LIST);
		} else { 
			for (ForkData fd : ForkData.LIST)
				if (Fork.LIST.stream().noneMatch(f -> f.symbol.equals(fd.coinPrefix)))
					LIST.add(fd);
		}
		MODEL.fireTableDataChanged();
	}

}

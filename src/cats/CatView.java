package cats;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import forks.Fork;
import logging.LogModel;
import logging.LogView;
import util.FFUtil;
import util.I18n;
import util.Ico;
import util.json.JsonArray;
import util.json.JsonObject;
import util.swing.SwingEX;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class CatView extends JPanel {
	final List<Cat> LIST = new ArrayList<>();
	final Fork f;
	
	final CatTableModel MODEL = new CatTableModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	
	private final LogModel PVLOG = new LogModel();
	
	class CatTableModel extends JFunTableModel<Cat> {
		public CatTableModel() {
			super();
			
			addColumn("Name",   	120,	String.class,	c->c.name).colName(I18n.ForkView.nameColName);
			addColumn("Balance",   	80,		Double.class,	c->c.balance).colName(I18n.ForkView.balanceColName);
			addColumn("ID",  		-1,		String.class,	c->c.id).colName("ID");
			addColumn("1",   		22,		Icon.class,		c->(null != c.web) ? Ico.HOME : null).showMandatory();
			addColumn("2",   		22,		Icon.class,		c->(null != c.discord) ? Ico.DISCORD : null).showMandatory();
			
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public CatView(Fork f) {
		
		JsonArray catArray = (JsonArray)FFUtil.loadIntJSON("cats.json");
		catArray.forEach(o-> LIST.add(new Cat((JsonObject)o)));
		
		this.f = f;
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
		
		LogView logPanel = PVLOG.newPanelView();
		logPanel.JSP.setPreferredSize(new Dimension(300,150));
		
		add(logPanel,BorderLayout.PAGE_END);
		
		MODEL.colList.forEach(c -> c.finalize(TABLE,null));
		
		TableCellRenderer renderer = new SwingEX.JComponentTableCellRenderer();
		
		TableColumn gitColumn = TABLE.getColumnModel().getColumn(MODEL.getIndex("1"));
		gitColumn.setHeaderRenderer(renderer);
		gitColumn.setHeaderValue(new JLabel("", Ico.HOME, JLabel.CENTER));
		
		TableColumn discordColumn = TABLE.getColumnModel().getColumn(MODEL.getIndex("2"));
		discordColumn.setHeaderRenderer(renderer);
		discordColumn.setHeaderValue(new JLabel("", Ico.DISCORD, JLabel.CENTER));
		
		JSP.setPreferredSize(new Dimension(600,250));
		
		//new Thread(() -> loadStayCats()).start();
	}
	
	public void loadStrayCats() {
		
	}
	
}

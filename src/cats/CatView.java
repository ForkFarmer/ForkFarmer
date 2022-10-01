package cats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import forks.Fork;
import forks.ForkData;
import logging.LogModel;
import logging.LogView;
import main.ForkFarmer;
import main.Settings;
import main.TransactionPanel;
import types.Balance;
import util.I18n;
import util.Ico;
import util.Util;
import util.json.JsonArray;
import util.json.JsonObject;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.JFunTableModel;
import web.TailDatabase;

@SuppressWarnings("serial")
public class CatView extends JPanel {
	final List<Cat> LIST = new ArrayList<>();
	final Fork f;
	
	final CatTableModel MODEL = new CatTableModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	private final JPopupMenu POPUP_MENU = new JPopupMenu();
	private final TransactionPanel tp = new TransactionPanel();
	
	private final JLabel valuelbl = new JLabel(I18n.MainGui.value + Settings.GUI.currencySymbol + "0.0");
	private final JLabel numCatslbl = new JLabel("# Cats: 0");
	private final JPanel topPanel = new JPanel(new BorderLayout());
	
	private final LogModel CVLOG = new LogModel();
	
	class CatTableModel extends JFunTableModel<Cat> {
		public CatTableModel() {
			super();
			
			addColumn("Name",   	160,	String.class,	c->c.name).colName(I18n.ForkView.nameColName);
			addColumn("Symbol",   	50,		String.class,	c->c.code).colName(I18n.ForkView.symbolColName);
			addColumn("Balance",   	80,		Double.class,	c->c.balance).colName(I18n.ForkView.balanceColName);
			addColumn("$",   		40,		Double.class,	c->c.price);
			addColumn("Equity",		60, 	Balance.class, 	c->c.equity).viewRight().colName(I18n.ForkView.equityColName);
			addColumn("ID",  		-1,		String.class,	c->c.assetID).colName("ID");
			addColumn("#",  		20,		String.class,	c->c.getWID()).colName("#");
			addColumn("1",   		22,		Icon.class,		c->(null != c.web) ? Ico.HOME : null).showMandatory();
			addColumn("2",   		22,		Icon.class,		c->(null != c.discord) ? Ico.DISCORD : null).showMandatory();
			addColumn("3",   		22,		Icon.class,		c->(null != c.twitter) ? Ico.TWITTER : null).showMandatory();
			
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public List<Cat> getSelected() {
		return SwingUtil.getSelected(TABLE, LIST);
	}
	
	public CatView(Fork f) {
		
		//JsonArray catArray = (JsonArray)FFUtil.loadIntJSON("cats.json");
		//catArray.forEach(o-> LIST.add(new Cat((JsonObject)o)));
		
		topPanel.add(valuelbl,BorderLayout.LINE_START);
        topPanel.add(numCatslbl, BorderLayout.LINE_END);
        
        LogView logPanel = CVLOG.newPanelView();
		logPanel.JSP.setPreferredSize(new Dimension(500,150));

		JPanel topSplit = new JPanel(new BorderLayout());
		topSplit.add(topPanel,BorderLayout.PAGE_START);
		topSplit.add(JSP,BorderLayout.CENTER);
		
		JPanel bottomSplit = new JPanel(new BorderLayout());
		bottomSplit.add(tp,BorderLayout.PAGE_START);
		bottomSplit.add(logPanel,BorderLayout.CENTER);
		
		this.f = f;
		setLayout(new BorderLayout());
		add(topSplit,BorderLayout.CENTER);
		add(bottomSplit,BorderLayout.PAGE_END);
		
		MODEL.colList.forEach(c -> c.finalize(TABLE,null));
		
		TableCellRenderer renderer = new SwingEX.JComponentTableCellRenderer();
		
		TableColumn gitColumn = TABLE.getColumnModel().getColumn(MODEL.getIndex("1"));
		gitColumn.setHeaderRenderer(renderer);
		gitColumn.setHeaderValue(new JLabel("", Ico.HOME, JLabel.CENTER));
		
		TableColumn discordColumn = TABLE.getColumnModel().getColumn(MODEL.getIndex("2"));
		discordColumn.setHeaderRenderer(renderer);
		discordColumn.setHeaderValue(new JLabel("", Ico.DISCORD, JLabel.CENTER));
		
		TableColumn twitterColumn = TABLE.getColumnModel().getColumn(MODEL.getIndex("3"));
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
						Util.openLink(LIST.get(row).web);
					else if (colName.equals("2"))
						Util.openLink(ForkData.GITHUB_URL + LIST.get(row).discord);
					else if (colName.equals("3"))
						Util.openLink(LIST.get(row).twitter);
				}
			}
		});
		
		JSP.setPreferredSize(new Dimension(800,350));
		
		/*
		if (!f.rpc) {
			CVLOG.add("Cat view is not supported on non RPC clients");
			return;
		}
		*/
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		JSP.setComponentPopupMenu(POPUP_MENU);
		
		POPUP_MENU.add(new SwingEX.JMI("Query TailDB", Ico.TAIL_DB, 	() -> new Thread(() -> {
			CVLOG.add("Updating selected cats from taildatabase.com...");
			getSelected().forEach(TailDatabase::queryAsset);
			updateCats();
			CVLOG.add("Done updating selected cats");
		}).start()));
		
		POPUP_MENU.add(new SwingEX.JMI("Add Token", Ico.PLUS, () -> new Thread(() -> {
			Process p = null;
			
			for (Cat c : getSelected()) {
				CVLOG.add("Adding " + c.name + " to " + f.name + "...");
				try {
					p = Util.startProcess(f.exePath, "wallet", "add_token","-id",c.assetID, "-n", c.name);
				} catch (Exception e) {
					CVLOG.add("ERROR: " + e.toString());
				}
				Util.waitForProcess(p);
				CVLOG.add("Done adding token");
			}
			CVLOG.add("Done adding all Tokens");
			updateCats();
			
		}).start()));
		
		DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer(){
	        @Override
	        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
	            Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

	            c.setForeground(Color.WHITE);
	            
	            Cat cat = LIST.get(TABLE.convertRowIndexToModel(row));
	            
	            if (0 == cat.wid)
	            	c.setBackground(isSelected ? Color.DARK_GRAY.darker() : Color.DARK_GRAY);
	            else
	            	c.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : UIManager.getColor("Table.background"));
	            
	            return c;
	        }
	    };
		TABLE.getColumnModel().getColumn((MODEL.getIndex("Name"))).setCellRenderer(nameRenderer);
		
		tp.sendBtn.addActionListener(e -> sendTx());
		
		TABLE.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		    @Override
		    public void valueChanged(ListSelectionEvent event) {
		    	updateSelected();
		    }
		});
		
		updateSelected();
		new Thread(() -> loadCats()).start();
	}
	
	private void sendTx() {
		Cat c = getSelected().get(0);
		
		String address = tp.targetAddress.getText();
		
		Fork.getByAddress(address).ifPresentOrElse(f -> {
			String addr = new String(tp.targetAmt.getText());
			String fee = new String(tp.targetFee.getText());
			new Thread(() -> f.sendTX(c.wid,address,addr,fee)).start();
			tp.targetAddress.setText("");
			tp.targetAmt.setText("");
		}, 	() -> ForkFarmer.showMsg(I18n.MainGui.errorTitle, I18n.MainGui.errorContent));
		
	}
	
	public void updateSelected() {
		List<Cat> selList = getSelected();
		
		if (1 != selList.size()) {
			tp.sendBtn.setEnabled(false);
			return;
		}
		
		Cat c = selList.get(0);
		
		if (0 == c.wid) {
			tp.sendBtn.setEnabled(false);
			return;
		}
		
		tp.sendBtn.setEnabled(true);
	}
	
	public void updateCats() {
		SwingUtilities.invokeLater(() -> {
			MODEL.fireTableDataChanged();
		});
		numCatslbl.setText("# Cats: " + LIST.size());
	}
	
	public void update(Cat c) {
		SwingUtilities.invokeLater(() -> {
			getIndex(c).ifPresent(row -> {
				MODEL.fireTableRowsUpdated(row, row);
			});
		});
		
		double totalValue = 0;
		for (Cat ca : LIST) {
			if (null != ca.equity)
				totalValue += ca.equity.amt;
		}
		
		valuelbl.setText(I18n.MainGui.value + Settings.GUI.currencySymbol + Util.round(totalValue, 2));
	}
	
	public Optional<Integer> getIndex(Cat c) {
		int idx = LIST.indexOf(c);
		return (-1 != idx) ? Optional.of(idx) : Optional.empty();
	}
	
	public void loadCats() {
		CVLOG.add("Loading Cats");
		Process p = null;
		
		try {
			// 1) Load Named Tokens
			p = Util.startProcess(f.exePath, "rpc", "wallet", "get_wallets");
			JsonObject jo = Util.completeJsonRPC(p);
			
			CVLOG.add("Calling rpc wallet get_wallets...");
			if (null == jo) {
				CVLOG.add("Failed to load wallets");
				return;
			}
			
			JsonArray ja = (JsonArray) jo.get("wallets");
			int numWallets = ja.size();
			CVLOG.add("Loaded " + numWallets + " wallets");	
					
			for (int i = 0; i < numWallets; i++) {
				jo = (JsonObject) ja.get(i);
				int walletType = ((BigDecimal) jo.get("type")).intValue();
				if (6 != walletType)
					continue;
				String name = (String) jo.get("name");
				int wid = ((BigDecimal) jo.get("id")).intValue();
				String asset_id = (String) jo.get("data");
				asset_id = asset_id.substring(0, asset_id.length() - 2);
				Cat c = new Cat(wid,name, asset_id);
				LIST.add(c);
				f.assetMap.put(c.wid, c);
			}
				
			updateCats();	

			p = Util.startProcess(f.exePath, "rpc", "wallet", "get_stray_cats");
			jo = Util.completeJsonRPC(p);
			
			if (null == jo) {
				CVLOG.add("Failed to load stray cats");
			} else {
				ja = (JsonArray) jo.get("stray_cats");
				int numStrays = ja.size();
				
				for (int i = 0; i < numStrays; i++) {
					jo = (JsonObject) ja.get(i);
					String assetID = (String)jo.get("asset_id");
					
					if (LIST.stream().noneMatch(c -> c.assetID.equals(assetID)))
						LIST.add(new Cat(assetID));
				}
			}
			updateCats();
			loadCatBalances();
						
		} catch (Exception e) {
			CVLOG.add("ERROR: " + e.toString());
			e.printStackTrace();
		}
		
	}

	private void loadCatBalances() {
		CVLOG.add("Loading CAT balances...");
		for (Cat c: LIST) {
			if (0 == c.wid)
				continue;
			updateCatBalance(c);
		}
		CVLOG.add("Done loading CAT balances");
	}

	private void updateCatBalance(Cat c) {
		Process p = null;
		JsonObject jo;
		try {
			String query =  Util.isHostWin()? "{\\\"wallet_id\\\": " + c.wid + "}" : "{\"wallet_id\": " + c.wid + "}";
			p = Util.startProcess(f.exePath, "rpc", "wallet", "get_wallet_balance",query);
			jo = Util.completeJsonRPC(p);
			
			if (null == jo) {
				CVLOG.add("Failed to get balance for wallet id " + c.wid);
				return;
			}

			jo = (JsonObject) jo.get("wallet_balance");
			c.balance = ((BigDecimal)jo.get("confirmed_wallet_balance")).doubleValue();
			c.balance = c.balance/1000; // 1000 mojo per a CAT? usually?
			c.updateEquity();
			update(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

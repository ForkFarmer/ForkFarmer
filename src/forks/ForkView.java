package forks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import main.MainGui;
import main.Settings;
import types.Balance;
import types.Percentage;
import types.ReadTime;
import types.TimeU;
import types.Wallet;
import util.I18n;
import util.Ico;
import util.NetSpace;
import util.swing.Reorderable;
import util.swing.SwingUtil;
import util.swing.TableRowTransferHandler;
import util.swing.jfuntable.Col;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ForkView extends JPanel {
	public static final ForkTableModel MODEL = new ForkTableModel();	
	public static final JTable TABLE = new JTable(MODEL);
	private static final JScrollPane JSP = new JScrollPane(TABLE);
	
	public static class ForkTableModel extends JFunTableModel<Fork> implements Reorderable {
		private int BALANCE_COLUMN, PRICE_COLUMN, TIME_COLUMN, LIGHT_COLUMN, ATB_COLUMN, LOAD_COLUMN;
		private int W_HEIGHT_COLUMN, MAX_HEIGHT_COLUMN, FN_HEIGHT_COLUMN;
		
		public ForkTableModel() {
			super();
			@SuppressWarnings("unchecked")
			List<Col<Fork>> z = (List<Col<Fork>>) Settings.settings.get("ForkView Columns");
			loadColumns(z);
	 	
			addColumn("Logo",   	 22,	Icon.class,		f->f.ico).show().fixed().colName("");
			addColumn("Symbol",  	 50,	String.class, 	f->f.symbol).show().colName(I18n.ForkView.symbolColName);
			addColumn("Name",   	 80,	String.class,	f->f.name).colName(I18n.ForkView.nameColName);
			addColumn("Balance",	 80, 	Balance.class,	f->f.balance).show().viewRight().colName(I18n.ForkView.balanceColName);
			addColumn("$",			 60, 	Double.class, 	f->f.price).show().editable();
			addColumn("Equity",		 60, 	Balance.class, 	f->f.equity).viewRight().colName(I18n.ForkView.equityColName);
			addColumn("Netspace",	 80, 	NetSpace.class, f->f.fd.netspace).show().colName(I18n.ForkView.netspaceColName);
			addColumn("W Height", 	 80, 	Balance.class,  f->f.walletHeight).colName(I18n.ForkView.walletHeightColName);
			addColumn("FN Height",	 80, 	Balance.class,  f->f.fullnodeHeight).colName(I18n.ForkView.fullnodeHeightColName);
			addColumn("Max Height",	 80, 	Balance.class,  f->f.maxpeerHeight).colName(I18n.ForkView.maxpeerHeightColName);
			addColumn("Farm Size",	 80, 	NetSpace.class, f->f.plotSpace).colName(I18n.ForkView.farmSizeColName);
			addColumn("Version",	 80, 	String.class,   f->f.version).colName(I18n.ForkView.versionColName);
			addColumn("Latest Ver",	 80, 	String.class,   f->f.latestVersion).colName(I18n.ForkView.latestVerColName);
			addColumn("Published",	 80, 	String.class,   f->f.published).colName(I18n.ForkView.publishedColName);
			addColumn("Sync",		 80, 	String.class,   f->f.syncStatus).colName(I18n.ForkView.syncColName);
			addColumn("Farm",		 80, 	String.class,   f->f.farmStatus).show().colName(I18n.ForkView.farmColName);
			addColumn("ETW",		 70, 	TimeU.class,    f->f.fd.etw).show().colName(I18n.ForkView.etwColName);
			addColumn("24H Win",	 60,	Double.class, 	f->f.dayWin).colName(I18n.ForkView.h24hWinColName);
			addColumn("24H BW",		 60,	Long.class, 	f->f.dayWinBC).colName(I18n.ForkView.h24hBwColName);
			addColumn("Last Win",	 90, 	TimeU.class, 		Fork::getPreviousWin).colName(I18n.ForkView.lastWinColName);
			addColumn("Effort",		 50,	Percentage.class, 	Fork::getEffort).colName(I18n.ForkView.effortColName);
			addColumn("Address",	 450,	Wallet.class, 	f->f.wallet).show().flex().colName(I18n.ForkView.addressColName);
			addColumn("Reward",		 40,	Double.class, 	f->f.fullReward).editable().colName(I18n.ForkView.rewardColName);
			addColumn("# Peers",	 40,	Integer.class, 	f->f.peerList.size()).colName(I18n.ForkView.peersColName);
			addColumn("# Wallets",	 40,	Integer.class, 	f->f.walletList.size()).colName(I18n.ForkView.walletsColName);
			addColumn("# Harvesters",40,	Integer.class, 	f->f.numH).colName(I18n.ForkView.harvestersColName);
			addColumn("MiB up",		 60,	Double.class, 	f->f.upload).colName(I18n.ForkView.uploadColName);
			addColumn("MiB down",	 60,	Double.class, 	f->f.download).colName(I18n.ForkView.downloadColName);
			addColumn("Load", 		 40, 	String.class, 	f->f.load).colName(I18n.ForkView.loadColName);
			addColumn("Time",		 50,	ReadTime.class, f->f.readTime).show().viewRight().colName(I18n.ForkView.timeColName);
			addColumn("Full Node",	 30,	Boolean.class, 	f->f.fullNode).editable().fixed().colIco(Ico.P2P_GREY).colName(I18n.ForkView.fullNodeColName);
			addColumn("Wallet Node", 30,	Boolean.class, 	f->f.walletNode).editable().fixed().colIco(Ico.WALLET_GRAY);
			addColumn("", 			 22, 	Icon.class, 	f->f.statusIcon).showMandatory().fixed();
			addColumn("Nothing", 	 30, 	Boolean.class, 	f->f.nothing).editable().fixed().colName("");
			addColumn("ATB Status",	 22, 	Icon.class, 	Fork::getATBStatus).show().fixed().colIco(Ico.ATB_GRAY);
			
			BALANCE_COLUMN = getIndex("Balance");
			PRICE_COLUMN = getIndex("$");

			// these updated a lot
			W_HEIGHT_COLUMN = getIndex("W Height");
			FN_HEIGHT_COLUMN = getIndex("FN Height");
			MAX_HEIGHT_COLUMN = getIndex("Max Height");
			TIME_COLUMN = getIndex("Time");
			LOAD_COLUMN = getIndex("Load");
			LIGHT_COLUMN = getIndex("");
			ATB_COLUMN = getIndex("ATB Status");
			
			onGetRowCount(Fork.LIST::size);
			onGetValueAt((r, c) -> colList.get(c).apply(Fork.LIST.get(r)));
			onisCellEditable((r, c) -> colList.get(c).isEditable());
		}
		
		public void removeRow(int row) {
	        Fork.LIST.remove(row);
	    }
		
		public void setValueAt(Object value, int row, int col) {
			if (PRICE_COLUMN == col) {
				Fork.LIST.get(row).updatePrice((double) value);
				MainGui.updateTotal();
			} else if (getIndex("Reward") == col) {
				Fork.LIST.get(row).fullReward = (double) value;
			} else if (getIndex("Full Node") == col) {
				Fork.LIST.get(row).fullNode = (boolean) value;
			} else if (getIndex("Wallet Node") == col) {
				Fork.LIST.get(row).walletNode = (boolean) value;
			} else if (getIndex("Nothing") == col) {
				Fork.LIST.get(row).nothing = (boolean) value;
			}
			fireTableCellUpdated(row, col);
	    }

		@Override
		public void reorder(int fromIndex, int toIndex) {
			synchronized (Fork.LIST) {
				SwingUtil.mapViewToModel(TABLE,Fork.LIST);
						
				fromIndex = TABLE.convertRowIndexToModel(fromIndex);
				toIndex = TABLE.convertRowIndexToModel(toIndex);
				 
				if (toIndex > fromIndex) // need account from 'remove' in toIndx
					toIndex--;
				 
					Fork f = Fork.LIST.remove(fromIndex);
					Fork.LIST.add(toIndex, f);
				}
				ForkView.update();
		}
		
	}
	
	public ForkView() {
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
	
		SwingUtil.persistDimension(JSP, () -> Settings.GUI.forkViewDimension, d -> Settings.GUI.forkViewDimension = d);
		
		final JPopupMenu POPUP_MENU = ForkController.getPopupMenu();
		TABLE.setComponentPopupMenu(POPUP_MENU);
		JSP.setComponentPopupMenu(POPUP_MENU);
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(MODEL.finalizeColumns(TABLE));
		 
		TABLE.setDragEnabled(true);
		TABLE.setDropMode(DropMode.INSERT_ROWS);
		TABLE.setTransferHandler(new TableRowTransferHandler(TABLE));
		
		DefaultTableCellRenderer balanceRendererR = new DefaultTableCellRenderer(){
	        @Override
	        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
	            Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

	            c.setForeground(Color.WHITE);
	            
	            Fork f = Fork.LIST.get(TABLE.convertRowIndexToModel(row));
	            
	            if (null != f.bgColor)
	            	c.setBackground(isSelected ? f.bgColor.darker() : f.bgColor);
	            else
	            	c.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : UIManager.getColor("Table.background"));
	            
	            Wallet w =  f.wallet;
	            if (f.cold || (null != w && w.cold))
	            	c.setForeground(new Color(140,171,255));
	            
	            return c;
	        }
	    };
	    balanceRendererR.setHorizontalAlignment(JLabel.RIGHT);
		
		TABLE.getColumnModel().getColumn(MODEL.BALANCE_COLUMN).setCellRenderer(balanceRendererR);
	}
	
	public static List<Fork> getSelected() {
		return SwingUtil.getSelected(TABLE, Fork.LIST);
	}

	public static void update() {
		SwingUtilities.invokeLater(MODEL::fireTableDataChanged); 
	}
	
	public static void update(Fork f) {
		SwingUtilities.invokeLater(() -> {
			f.getIndex().ifPresent(row -> {
				f.updateIcon();
				MODEL.fireTableRowsUpdated(row, row);
			});
		});
	}
	
	public static void updateLog(Fork f) {
		SwingUtilities.invokeLater(() -> {
			f.getIndex().ifPresent(row -> {
				f.updateIcon();
				MODEL.fireTableCellUpdated(row, MODEL.TIME_COLUMN);
				MODEL.fireTableCellUpdated(row, MODEL.LIGHT_COLUMN);
				MODEL.fireTableCellUpdated(row, MODEL.ATB_COLUMN);
				MODEL.fireTableCellUpdated(row, MODEL.W_HEIGHT_COLUMN);
				MODEL.fireTableCellUpdated(row, MODEL.FN_HEIGHT_COLUMN);
				MODEL.fireTableCellUpdated(row, MODEL.MAX_HEIGHT_COLUMN);
				MODEL.fireTableCellUpdated(row, MODEL.LOAD_COLUMN);
			});
		}); 
	}
	
}

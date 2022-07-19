package peer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import forks.Fork;
import logging.LogModel;
import logging.LogView;
import util.I18n;
import util.Ico;
import util.Util;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.JFunTableModel;
import web.AllTheBlocks;

@SuppressWarnings("serial")
public class PeerView extends JPanel {
	final List<Peer> LIST = new ArrayList<>();
	final Fork f;
	
	final PeerTableModel MODEL = new PeerTableModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	private final JTextField newPeerField = new JTextField();
	private final JButton atbPeersBtn = new JButton(I18n.PeerView.getAtbBtn, Ico.ATB);
	
	private final JButton addPeers = new SwingEX.Btn(I18n.PeerView.addPeerBtn, Ico.PLUS, () -> {
		List<String> peerList = Arrays.asList(newPeerField.getText().split("\\s+"));
		new Thread(() -> addPeers(peerList)).start();
	});
	
	private final JButton copyPeers = new SwingEX.Btn(I18n.PeerView.copy, 	Ico.CLIPBOARD,  () -> {copy();});
	private final JButton copyCLI = new SwingEX.Btn(I18n.PeerView.cliCopy, Ico.CLI,  () -> {copyCLI();});
	private final JButton deleteBtn = new SwingEX.Btn(I18n.PeerView.deleteBtn, null,  () -> {deletePeers();});
	
	
	private final LogModel PVLOG = new LogModel();
	
	class PeerTableModel extends JFunTableModel<Peer> {
		public PeerTableModel() {
			super();
			
			addColumn("Address",   	-1,		String.class,	p->p.address).colName(I18n.PeerView.addressColName);
			addColumn("Height",   	80,		Integer.class,	p->p.height).colName(I18n.PeerView.heightColName);
			addColumn("Time",  		160,	String.class,	p->p.time).colName(I18n.PeerView.timeColName);
			addColumn("Upload",   	80,		double.class, 	p->p.ul).colName(I18n.PeerView.uploadColName);
			addColumn("Download",  	80,		double.class, 	p->p.dl).colName(I18n.PeerView.dowloadColName);
			addColumn("NodeID",  	80,		String.class, 	p->p.nodeID).colName(I18n.PeerView.nodeIDColName);
			
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public PeerView(Fork f) {
		this.f = f;
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
		
		LogView logPanel = PVLOG.newPanelView();
		logPanel.JSP.setPreferredSize(new Dimension(300,150));
		
		add(logPanel,BorderLayout.PAGE_END);
		
		MODEL.colList.forEach(c -> c.finalize(TABLE,null));
		
		JSP.setPreferredSize(new Dimension(600,250));
		
		JMenuBar MENU = new JMenuBar();
		MENU.add(copyPeers);
		MENU.add(copyCLI);
		MENU.add(deleteBtn);
		MENU.add(new JSeparator());
		MENU.add(addPeers);
		MENU.add(newPeerField);
		 
		atbPeersBtn.setEnabled(null != f.fd.atbPath);
		atbPeersBtn.addActionListener(al -> new Thread(() -> getATBpeers(f)).start());
		MENU.add(atbPeersBtn);
		
		addPeers.setEnabled(false);
		copyPeers.setEnabled(false);
		copyCLI.setEnabled(false);
		deleteBtn.setEnabled(false);
		
		addPeers.setToolTipText(I18n.PeerView.addPeerBtnTipText);
		
		add(MENU,BorderLayout.PAGE_START);
		
		new Thread(() -> loadPeers()).start();
		
		TABLE.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		    @Override
		    public void valueChanged(ListSelectionEvent event) {
		    	copyPeers.setEnabled(TABLE.getSelectedRow() > -1);
		    	copyCLI.setEnabled(TABLE.getSelectedRow() > -1);
		    	deleteBtn.setEnabled(TABLE.getSelectedRow() > -1);
		    }
		});
		
		newPeerField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				addPeers.setEnabled(newPeerField.getText().length() > 10);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				addPeers.setEnabled(newPeerField.getText().length() > 10);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				addPeers.setEnabled(newPeerField.getText().length() > 10);
			}
		    
		});
	}
	
	public void getATBpeers(Fork f) {
		atbPeersBtn.setEnabled(false);
		String forkPath = f.fd.atbPath;
		PVLOG.add("alltheblocks.net getting peers...");
		List<String> peerList = AllTheBlocks.getPeers(forkPath);
		PVLOG.add("received " + peerList.size() + " peers");
		addPeers(peerList);
		atbPeersBtn.setEnabled(true);
	}
	
	public void loadPeers() {
		LIST.clear();
		
		PVLOG.add("Loading peers...");
		LIST.addAll(f.loadPeers());
		PVLOG.add("Loaded " + LIST.size() + " peers" );
		
		SwingUtilities.invokeLater(() -> {
			MODEL.fireTableDataChanged();
		});
	}
	
	private void copyCLI() {
		List<Peer> peerList = SwingUtil.getSelected(TABLE, LIST);
		StringBuilder sb = new StringBuilder();
		for (Peer p: peerList)
			if (null != p.address)
				sb.append(f.name.toLowerCase() + " show -a " + p.address + "\n");
		
		Util.copyToClip(sb.toString());
	}
	
	private void deletePeers() {
		List<Peer> peerList = SwingUtil.getSelected(TABLE, LIST);
		
		PVLOG.add("Removing " + peerList.size() + " peers");
		new Thread(() -> {
			for (Peer p : peerList) {
				PVLOG.add(f.name + " show -r " + p.nodeID);
				@SuppressWarnings("unused")
				String s = Util.runProcessWait(f.exePath,"show","-r", p.nodeID);
			}
			PVLOG.add("Done removing peers");
			loadPeers();
		}).start();
		
	}


	private void copy() {
		List<Peer> peerList = SwingUtil.getSelected(TABLE, LIST);
		StringBuilder sb = new StringBuilder();
		for (Peer p: peerList)
			if (null != p.address)
				sb.append(p.address + "\n");
		
		Util.copyToClip(sb.toString());
	}
	

	public void addPeers(List<String> peers) {
		PVLOG.add("Trying " + peers.size() + " peers");
		for (String p : peers) {
			String s = Util.runProcessWait(f.exePath,"show","-a", p);
			s = s.replace("\n", " ").replace("\r", " ");
			PVLOG.add(s);
		}
		PVLOG.add("Done adding peers... reloading table");
		loadPeers();
	}
}

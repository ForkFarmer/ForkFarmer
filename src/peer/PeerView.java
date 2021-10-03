package peer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import forks.Fork;
import util.Ico;
import util.Util;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class PeerView extends JPanel {
	final List<Peer> LIST = new ArrayList<>();
	final Fork f;
	
	final PeerTableModel MODEL = new PeerTableModel();
	private final JTable TABLE = new JTable(MODEL);
	private final JScrollPane JSP = new JScrollPane(TABLE);
	
	private final JButton addPeers = new SwingEX.Btn("Add Peers", Ico.PLUS, () -> {new Thread(() -> addPeers()).start();});
	private final JButton copyPeers = new SwingEX.Btn("Copy", 	Ico.CLIPBOARD,  () -> {copy();});
	private final JButton copyCLI = new SwingEX.Btn("CLI Copy", Ico.CLI,  () -> {copyCLI();});
	private final JTextField newPeerField = new JTextField();
	
	class PeerTableModel extends JFunTableModel<Peer> {
		public PeerTableModel() {
			super();
			
			addColumn("Address",   	-1,		String.class,	p->p.address);
			addColumn("Height",   	80,		String.class,	p->p.height);
			addColumn("Time",  		160,	String.class,	p->p.time);
			addColumn("Upload",   	80,		double.class, 	p->p.ul);
			addColumn("Dowload",   	80,		double.class, 	p->p.dl);
			
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public PeerView(Fork f) {
		this.f = f;
		setLayout(new BorderLayout());
//		setBorder(new TitledBorder("Peer Connections:"));
		add(JSP,BorderLayout.CENTER);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,null));
		
		
		JSP.setPreferredSize(new Dimension(600,250));
		
		JMenuBar MENU = new JMenuBar();
		MENU.add(copyPeers);
		MENU.add(copyCLI);
		MENU.add(new JSeparator());
		MENU.add(addPeers);
		MENU.add(newPeerField);
		
		addPeers.setEnabled(false);
		copyPeers.setEnabled(false);
		copyCLI.setEnabled(false);
		
		addPeers.setToolTipText("Peer format ip:port delimited by space");
		
		add(MENU,BorderLayout.PAGE_START);
		
		Process p = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(f.exePath, "show", "-c");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String l = null;
			while ( null != (l = br.readLine())) {
				if (f.symbol.equals("HDD") && l.contains("FULL_NODE ")) {
					LIST.add(Peer.factorySingleLine(l));
            	} else if (l.contains("FULL_NODE ")) {
            		String l2 = br.readLine();
            		LIST.add(Peer.factoryMultiLine(l + l2));
            	}
            		
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		Util.waitForProcess(p);
		Util.closeQuietly(br);
		
		TABLE.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		    @Override
		    public void valueChanged(ListSelectionEvent event) {
		    	copyPeers.setEnabled(TABLE.getSelectedRow() > -1);
		    	copyCLI.setEnabled(TABLE.getSelectedRow() > -1);
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
		
		MODEL.fireTableDataChanged();
	}
	
	private void copyCLI() {
		List<Peer> peerList = SwingUtil.getSelected(TABLE, LIST);
		StringBuilder sb = new StringBuilder();
		for (Peer p: peerList)
			if (null != p.address)
				sb.append(f.name.toLowerCase() + " show -a " + p.address + "\n");
		
		StringSelection stringSelection = new StringSelection(sb.toString());
		
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
		
	}

	private void copy() {
		List<Peer> peerList = SwingUtil.getSelected(TABLE, LIST);
		StringBuilder sb = new StringBuilder();
		for (Peer p: peerList)
			if (null != p.address)
				sb.append(p.address + "\n");
		
		StringSelection stringSelection = new StringSelection(sb.toString());
		
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	

	public void addPeers() {
		String[] peers = newPeerField.getText().split("\\s+");
		for (String p : peers)
			Util.runProcessWait(f.exePath,"show","-a", p);
	}
}

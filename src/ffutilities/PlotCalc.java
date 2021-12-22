package ffutilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import util.I18n;
import util.Util;
import util.swing.SwingEX.LTPanel;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class PlotCalc extends JPanel {
	final List<KV> LIST = Arrays.asList(KV.values());
	final KValModel MODEL_KV = new KValModel();
	private final JTable TABLE_KV = new JTable(MODEL_KV);
	
	private final SolutionModel MODEL_SOL = new SolutionModel();
	private final JTable TABLE_SOL = new JTable(MODEL_SOL);
	private final JScrollPane JSP = new JScrollPane(TABLE_SOL);
	
	LTPanel hddSize = new LTPanel(I18n.PlotCalc.hddSize,"10000");
	
	HDD HDD = new HDD(hddSize.getAsDouble());
	List<HDD> SolutionList = new ArrayList<>();
	HDD bestSolution;
	
	
	public enum KV {
		K29(11.5),
		K30(23.8),
		K31(49.1),
		K32(101.36),
		K33(208.81),
		K34(429.86),
		K35(884.86);
		
		boolean enabled;
		double plotSize;
		
		KV(double plotSize) {
			this.plotSize = plotSize;
		}
		
		public double getSize() {
			return plotSize;
		}
		
		public boolean enabled() {
			return enabled;
		}
		
		public static Optional<KV> getMinEnabled() {
			return Arrays.stream(KV.values()).filter(KV::enabled).findFirst();
		}
		
		public static List<KV> getAvail() {
			return Arrays.stream(KV.values()).filter(KV::enabled).collect(Collectors.toList());
		}
		
	}
	
	public class KV_SOL {
		KV k;
		int amt;
		
		public KV_SOL(KV k) {
			this.k = k;
		}
		
		public KV_SOL(KV_SOL in) {
			k = in.k;
			amt = in.amt;
		}
		
		public double getSize() {
			return k.plotSize * (double)amt;
		}
		
		public String toString() {
			return k.name();
		}
	}
	
	class KValModel extends JFunTableModel<KV> {
		public KValModel() {
			super();
			addColumn("",   			22,		Boolean.class,	k->k.enabled).showMandatory().fixed();
			addColumn("K",   			40,		Enum.class,		k->k).showMandatory().fixed().colName(I18n.PlotCalc.tableHeaderKColName);
			addColumn("Plot Size (GB)",	150,	Double.class,	k->k.plotSize).colName(I18n.PlotCalc.tableHeaderPlotSizeColName);
			
			onGetRowCount(() -> LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(LIST.get(r)));
			onisCellEditable((r, c) -> (0 == c || 2 == c));
		}
		
		public void setValueAt(Object value, int row, int col) {
			if (0 == col) {
				LIST.get(row).enabled = (boolean) value;
				reCalc();
				MODEL_KV.fireTableDataChanged();
			} else if (2 == col) {
				LIST.get(row).plotSize = (double) value;
				reCalc();
				MODEL_KV.fireTableDataChanged();
			}
	    }
	}
	
	class SolutionModel extends JFunTableModel<HDD> {
		public SolutionModel() {
			super();
			addColumn("Soltuion",   200,	Enum.class,		h->h).showMandatory().fixed().colName(I18n.PlotCalc.solutionColName);
			addColumn("Free Space",	80,	String.class,	h->h.fsStr()).colName(I18n.PlotCalc.freeSpaceColName);
			
			onGetRowCount(() -> SolutionList.size());
			onGetValueAt((r, c) -> colList.get(c).apply(SolutionList.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	
	public class HDD implements Comparable<HDD> {
		double totalSpace;
		double freeSpace;
		List<KV_SOL> LIST = new ArrayList<>();
		
		public HDD(double space) {
			totalSpace = space;
			freeSpace = space;
			Arrays.stream(KV.values()).filter(KV::enabled).forEach(kv -> LIST.add(new KV_SOL(kv)));
		}
		
		public HDD(HDD hdd) {
			set(hdd);
		}
		
		public void addPlot(KV kv) {
			//System.out.println("Adding plot" + kv);
			get(kv).amt++;
			freeSpace -= kv.plotSize;
		}
		
		public void rmPlot(KV kv) {
			get(kv).amt--;
			freeSpace += kv.plotSize;
		}
		
		public KV_SOL get(KV k) {
			return LIST.stream().filter(kv -> kv.k == k).findAny().get();
		}

		public void set(HDD hdd) {
			LIST = new ArrayList<>(hdd.LIST.size());
			
			for(KV_SOL k : hdd.LIST)
				LIST.add(new KV_SOL(k));
			
			totalSpace = hdd.totalSpace;
			freeSpace = hdd.freeSpace;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			
			ListIterator<KV_SOL> li = LIST.listIterator(LIST.size());
			while (li.hasPrevious()) {
				KV_SOL k = li.previous();
				sb.append(k.toString() + ": " + k.amt + " ");
			}
			return sb.toString();
		}
		
		public String fsStr() {
			return String.valueOf(Util.round(freeSpace, 2));
		}

		public int compareTo(HDD hdd) {
			return ((Double)freeSpace).compareTo(hdd.freeSpace);
		}
		
	}
		
	public void reCalc() {
		SolutionList.clear();
		HDD = new HDD(hddSize.getAsDouble());
		MODEL_SOL.fireTableDataChanged();
		
		if (0 == HDD.totalSpace || 0 == KV.getAvail().size()) 
			return;
		
		if (HDD.totalSpace > 100000)
			return;
		
		fill(HDD,KV.getAvail());
		MODEL_SOL.fireTableDataChanged();
	}
	
	private List<KV> copyRm(List<KV> in) {
		List<KV> LIST = new ArrayList<>(in);
		LIST.remove(in.size()-1);
		return LIST;
	}

	public void fill(HDD hdd, List<KV> kvList) {
		if (0 == kvList.size())
			return;
		KV k = kvList.get(kvList.size()-1);
		
		while (hdd.freeSpace > k.plotSize) {
			fill(new HDD(hdd),copyRm(kvList));
			hdd.addPlot(k);
		}
		
		SolutionList.add(hdd);
		Collections.sort(SolutionList);
		if (SolutionList.size() > 200)
			SolutionList.remove(SolutionList.size()-1);
	}
	
	
	public PlotCalc() {
		KV.K32.enabled = true;
		setLayout(new BorderLayout());
		
		JPanel kvPanel = new JPanel(new BorderLayout());
		kvPanel.add(TABLE_KV.getTableHeader(),BorderLayout.PAGE_START);
		kvPanel.add(TABLE_KV, BorderLayout.CENTER);
		add(kvPanel,BorderLayout.CENTER);
		add(hddSize,BorderLayout.PAGE_START);
		kvPanel.setBorder(new TitledBorder(I18n.PlotCalc.borderPlotSize));
		
		add(JSP,BorderLayout.PAGE_END);	
		JSP.setPreferredSize(new Dimension(300,500));
		
		hddSize.setChangeListener(() -> {
		    reCalc();
		});
		
		
		
		
		MODEL_KV.colList.forEach(c -> c.finalize(TABLE_KV,null));
		MODEL_SOL.colList.forEach(c -> c.finalize(TABLE_SOL,null));
		reCalc();
	}
	

}

package edu.ucsf.rbvi.seqViz.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.List;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;

/**
 * A JPanel to display the sequence alongside the histograms.
 * @author aywu
 *
 */
public class SequenceView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 954541021302923935L;
	private JScrollBar scrollBar;
	private HistoPanel seqViewUp, seqViewDown;
	private SequencePanel seqPanel;
	private String sequence, windowSeq;
	private Font seqFont;
	private FontMetrics textMetrics;
	private long y_min = 0, y_max = 0, contigLength, binSize;
	private int minPos, maxPos;
	private CyTable table, nodeTable;
	private List<String> graphs;
	private CyNetwork network;
	
	/**
	 * Create SequenceView object
	 * @param network CyNetwork with appropriate tables in defaultNetworkTable() to create
	 *        histograms from.
	 * @param suid SUID of the contig node
	 * @param minPos left limit of the histogram/sequence to be displayed
	 * @param maxPos right limit of the histogram/sequence to be displayed
	 */
	public SequenceView(CyNetwork network, Long suid, int minPos, int maxPos) {
		minPos--;
		maxPos--;
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.network = network;
		table = network.getDefaultNetworkTable();
		nodeTable = network.getDefaultNodeTable();
		final String contig = nodeTable.getRow(suid).get(CyNetwork.NAME, String.class);
		graphs = table.getRow(network.getSUID()).getList(contig + ":graphColumns", String.class);
		contigLength = nodeTable.getRow(suid).get("length", Long.class);
		binSize = table.getRow(network.getSUID()).get("graphBinSize", Long.class);
		Random random = new Random(70);
		for (String s: graphs) {
			List<Long> graph = table.getRow(network.getSUID()).getList(s, Long.class);
			for (Long l: graph) {
				if (l > 0) y_max = l > y_max ? l: y_max;
				else y_min = l < y_min ? l: y_min;
			}
		}
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		sequence = nodeTable.getRow(suid).get("sequence", String.class);
		windowSeq = sequence.substring(minPos, maxPos);
		seqFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		seqPanel = new SequencePanel(sequence, minPos, maxPos);
		
		seqPanel.setFont(seqFont);
		textMetrics = seqPanel.getFontMetrics(seqFont);
		seqViewUp = new HistoPanel(textMetrics.stringWidth(windowSeq), (int) (400 * y_max / (y_max - y_min)), minPos+1, maxPos+1, 0, y_max);
		seqViewDown = new HistoPanel(textMetrics.stringWidth(windowSeq), (int) (400 * - y_min / (y_max - y_min)), minPos+1, maxPos+1, y_min, 0);
		seqPanel.setPreferredSize(new Dimension(textMetrics.stringWidth(windowSeq), textMetrics.getHeight()));
		add(seqViewUp);
		add(seqPanel);
		add(seqViewDown);
		
		scrollBar = new JScrollBar(JScrollBar.HORIZONTAL, minPos, 0, 0, sequence.length() - (maxPos - minPos + 1));
		scrollBar.addAdjustmentListener(new AdjustmentListener() {
			
			public void adjustmentValueChanged(AdjustmentEvent e) {
				SequenceView.this.maxPos = e.getValue() + (SequenceView.this.maxPos - SequenceView.this.minPos);
				SequenceView.this.minPos = e.getValue();
				updateGraph();
				repaint();
			}
		});
		add(scrollBar);
		
		updateGraph();
	/*	int beg = (int) (minPos / binSize), end = (int) (maxPos / binSize);
		if (beg > 0) beg--;
		if (end < (contigLength / binSize) + (contigLength % binSize == 0 ? 0: 1)) end++;
		for (final String s: graphs) {
			List<Long> graph = table.getRow(network.getSUID()).getList(s, Long.class);
			double[] y = new double[end - beg + 1], x = new double[end - beg + 1];
			int i = 0;
			for (Long l: graph) {
				if (beg <= i && i <= end) {
					y[i-beg] = l;
					x[i-beg] = i * binSize + 1;
				}
				i++;
			}
			final Color randomColor = new Color(((int) (random.nextFloat() * 4)) * 64, ((int) (random.nextFloat() * 4)) * 64, ((int) (random.nextFloat() * 4)) * 64);
			if (s.split(":")[2].equals("+"))
				seqViewUp.addGraph(s, randomColor, x, y);
			if (s.split(":")[2].equals("-"))
				seqViewDown.addGraph(s, randomColor, x, y);
		} */
	}
	
	private void updateGraph() {
		int beg = (int) (minPos / binSize), end = (int) (maxPos / binSize);
		if (beg > 0) beg--;
		if (end + 1 < (contigLength / binSize) + (contigLength % binSize == 0 ? 0: 1)) end++;
		Random random = new Random(70);
		for (final String s: graphs) {
			List<Long> graph = table.getRow(network.getSUID()).getList(s, Long.class);
			double[] y = new double[end - beg + 1], x = new double[end - beg + 1];
			int i = 0;
			for (Long l: graph) {
				if (beg <= i && i <= end) {
					y[i-beg] = l;
					x[i-beg] = i * binSize + 1;
				}
				i++;
			}
			final Color randomColor;
			if (! s.split(":")[0].equals(s.split(":")[1]))
				randomColor = new Color(((int) (random.nextFloat() * 4)) * 64, ((int) (random.nextFloat() * 4)) * 64, ((int) (random.nextFloat() * 4)) * 64);
			else
				randomColor = Color.GRAY;
			if (s.split(":")[2].equals("+"))
				seqViewUp.addGraph(s, randomColor, x, y);
			if (s.split(":")[2].equals("-"))
				seqViewDown.addGraph(s, randomColor, x, y);
		}
		seqPanel.changeWindow(minPos, maxPos);
		seqViewUp.setHistoPanelSize(minPos+1, maxPos+1, 0, (int) y_max);
		seqViewDown.setHistoPanelSize(minPos+1, maxPos+1, (int) y_min, 0);
	}
	
	private class SequencePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3536609604942989080L;
		private String seq;
		private int beg, end;
		
		public SequencePanel(String s, int beg, int end) {
			seq = s;
			this.beg = beg;
			this.end = end;
			setBackground(Color.WHITE);
		}
		
		public void changeWindow(int beg, int end) {
			this.beg = beg;
			this.end = end;
		}
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			String windowSeq = seq.substring(beg, end);
			int width = this.getWidth();
			double inc = (double) width / (double) windowSeq.length();
			AntiAlias.antiAliasing((Graphics2D) g);
			FontMetrics metrics = g.getFontMetrics();
			for (int i = 0; i < windowSeq.length(); i++) {
				switch (windowSeq.charAt(i)) {
					case 'A': g.setColor(Color.GREEN); break;
					case 'T': g.setColor(Color.RED); break;
					case 'C': g.setColor(Color.BLUE); break;
					case 'G': g.setColor(Color.BLACK); break;
					case 'a': g.setColor(Color.GREEN); break;
					case 't': g.setColor(Color.RED); break;
					case 'c': g.setColor(Color.BLUE); break;
					case 'g': g.setColor(Color.BLACK); break;
					default: g.setColor(Color.GRAY); break;
				}
				char[] character = {windowSeq.charAt(i)};
				g.drawString(new String(character), (int) (i * inc), metrics.getAscent());
			}
		}
	}
}

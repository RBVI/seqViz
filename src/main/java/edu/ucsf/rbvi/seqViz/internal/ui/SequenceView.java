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
	
	public SequenceView(CyNetwork network, Long suid, int minPos, int maxPos) {
		minPos--;
		maxPos--;
		CyTable table = network.getDefaultNetworkTable(), nodeTable = network.getDefaultNodeTable();
		final String contig = nodeTable.getRow(suid).get(CyNetwork.NAME, String.class);
		List<String> graphs = table.getRow(network.getSUID()).getList(contig + ":graphColumns", String.class);
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
		scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
		scrollBar.addAdjustmentListener(new AdjustmentListener() {
			
			public void adjustmentValueChanged(AdjustmentEvent e) {
				
			}
		});
		sequence = nodeTable.getRow(suid).get("sequence", String.class);
		windowSeq = sequence.substring(minPos, maxPos);
		seqFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		seqPanel = new SequencePanel(windowSeq);
		
		seqPanel.setFont(seqFont);
		textMetrics = seqPanel.getFontMetrics(seqFont);
		seqViewUp = new HistoPanel(textMetrics.stringWidth(windowSeq), (int) (400 * y_max / (y_max - y_min)), minPos+1, maxPos+1, 0, y_max);
		seqViewDown = new HistoPanel(textMetrics.stringWidth(windowSeq), (int) (400 * - y_min / (y_max - y_min)), minPos+1, maxPos+1, y_min, 0);
		seqPanel.setPreferredSize(new Dimension(textMetrics.stringWidth(windowSeq), textMetrics.getHeight()));
		add(seqViewUp);
		add(seqPanel);
		add(seqViewDown);
		add(scrollBar);
		
		int beg = (int) (minPos / binSize), end = (int) (maxPos / binSize);
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
		}
	}
	
	private class SequencePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3536609604942989080L;
		private String seq;
		
		public SequencePanel(String s) {
			seq = s;
			setBackground(Color.WHITE);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
		//	AntiAlias.antiAliasing((Graphics2D) g);
			g.setColor(Color.BLACK);
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(seq, 0, metrics.getAscent());
		}
	}
}

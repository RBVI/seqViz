package edu.ucsf.rbvi.seqViz.internal.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;

import edu.ucsf.rbvi.seqViz.internal.model.ComplementaryGraphs;
import edu.ucsf.rbvi.seqViz.internal.model.Contig;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class ContigView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7713836441534331408L;
	private JButton zoomIn, zoomOut, zoomInY, zoomOutY;
	private JSplitPane splitPane;
	private JScrollPane histoPane, settingsPane;
	private JPanel histoPanel, zoomPane, settingsPanel;
	private HistoPanel histoPanel2;
	private ContigsManager manager;
	private Contig contig;
	private ComplementaryGraphs graphs;
	private long y_min = 0, y_max = 0, contigLength = 0, binSize;
	private int width = 800, height = 400, widthScale = 1, heightScale = 1;
	
/*	public ContigView(ContigsManager manager, String contig) {
		this.manager = manager;
		this.contig = manager.getContig(contig);
		graphs = manager.createBpGraph(contig);
		
		for (long [] d: graphs.pos.values())
			for (int i = 0; i < d.length; i++)
				y_max = d[i] > y_max ? d[i]: y_max;
		for (long [] d: graphs.rev.values())
			for (int i = 0; i < d.length; i++)
				y_min = d[i] > y_min ? d[i]: y_min;
		y_min = - y_min;
		
		histoPanel2 = new HistoPanel(800, 400, 0, this.contig.sequence().length(), y_min, y_max);
		histoPane = new JScrollPane(histoPanel2);
		histoPanel = new JPanel();
		histoPanel.setMinimumSize(new Dimension(800,400));
		histoPanel.setLayout(new BorderLayout());
		zoomPane = new JPanel(new FlowLayout());
		histoPanel.add(zoomPane, BorderLayout.SOUTH);
		histoPanel.add(histoPane, BorderLayout.CENTER);
		zoomIn = new JButton("Zoom In");
		zoomOut = new JButton("Zoom Out");
		left = new JButton("<<<");
		right = new JButton(">>>");
		zoomPane.add(left);
		zoomPane.add(zoomIn);
		zoomPane.add(zoomOut);
		zoomPane.add(right);

		settingsPane = new JScrollPane();
		settingsPane.setMaximumSize(new Dimension(300,400));
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, histoPanel, settingsPane);
		Dimension splitPaneSize = new Dimension(1100,400);
		splitPane.setPreferredSize(splitPaneSize);
		splitPane.setOneTouchExpandable(true);
		setPreferredSize(splitPaneSize);
		
		for (String s: graphs.pos.keySet()) {
			long[] d = graphs.pos.get(s);
			double[] y = new double[d.length], x = new double[d.length];
			for (int i = 0; i < y.length; i++) {
				y[i] = d[i];
				x[i] = i + 1;
			}
			histoPanel2.addGraph(s, Color.BLUE, x, y);
		}
		for (String s: graphs.rev.keySet()) {
			long[] d = graphs.rev.get(s);
			double[] y = new double[d.length], x = new double[d.length];
			for (int i = 0; i < y.length; i++) {
				y[i] = -d[i];
				x[i] = i + 1;
			}
			histoPanel2.addGraph(s + " reverse", Color.YELLOW, x, y);
		}
	} */
	
	public ContigView(CyNetwork network, Long suid) {
		CyTable table = network.getDefaultNetworkTable(), nodeTable = network.getDefaultNodeTable();
		String contig = nodeTable.getRow(suid).get(CyNetwork.NAME, String.class);
		List<String> graphs = table.getRow(network.getSUID()).getList(contig + ":graphColumns", String.class);
	//	JPanel[] graphColor = new JPanel[graphs.size()];
		SortedMap<String, JPanel> graphColor = new TreeMap<String, JPanel>();
		JCheckBox[] displayGraph = new JCheckBox[graphs.size()];
		JPanel sameContigGraphPos = null, sameContigGraphRev = null;
		binSize = table.getRow(network.getSUID()).get("graphBinSize", Long.class);
		Random random = new Random(70);
		contigLength = nodeTable.getRow(suid).get("length", Long.class);
		for (String s: graphs) {
			List<Long> graph = table.getRow(network.getSUID()).getList(s, Long.class);
		//	contigLength = graph.size() * binSize;
			for (Long l: graph) {
				if (l > 0) y_max = l > y_max ? l: y_max;
				else y_min = l < y_min ? l: y_min;
			}
		}
		
		settingsPanel = new JPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS));

		histoPanel2 = new HistoPanel(width, height, 0, contigLength, y_min, y_max);
		int j = 0;
		int labelLength = 0, tempLength;
		for (String s: graphs)
			if (labelLength < (tempLength = s.split(":")[1].length() + s.split(":")[2].length() + 1))
				labelLength = tempLength;
		for (final String s: graphs) {
			List<Long> graph = table.getRow(network.getSUID()).getList(s, Long.class);
			double[] y = new double[graph.size()], x = new double[graph.size()];
			int i = 0;
			for (Long l: graph) {
				y[i] = l;
				x[i] = i * binSize + 1;
				i++;
			}
			final Color randomColor = new Color(((int) (random.nextFloat() * 4)) * 64, ((int) (random.nextFloat() * 4)) * 64, ((int) (random.nextFloat() * 4)) * 64);
			histoPanel2.addGraph(s, randomColor, x, y);
			JPanel newGraphColor = new JPanel();
		//	graphColor[j] = newGraphColor;
			newGraphColor.setLayout(new FlowLayout());
			if (! s.split(":")[0].equals(s.split(":")[1]))
				graphColor.put(s, newGraphColor);
			else {
				if (s.split(":")[2].equals("+"))
					sameContigGraphPos = newGraphColor;
				else if (s.split(":")[2].equals("-"))
					sameContigGraphRev = newGraphColor;
			}
		//	JLabel nodeTitle = new JLabel(s.split(":")[1] + "-" + s.split(":")[2]);
		//	nodeTitle.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		//	newGraphColor.add(nodeTitle);
		//	settingsPanel.add(nodeTitle);
			final JButton colorButton = new JButton();
		//	newGraphColor.add(colorButton = new JButton("Graph Color"));
		//	settingsPanel.add(colorButton = new JButton("Graph Color"));
			colorButton.setBackground(randomColor);
			colorButton.setToolTipText("Change the color of this graph.");
			colorButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					Color c = JColorChooser.showDialog(splitPane,
							"Choose color of graph", colorButton.getBackground());
					histoPanel2.changeColor(s, c);
					colorButton.setBackground(c);
					histoPanel2.repaint();
				}
			});
			String tempLabel = s.split(":")[1] + " " + s.split(":")[2];
			char[] labelString = new char[labelLength];
			for (int i1 = 0; i1 < labelString.length; i1++) {
				if (i1 < tempLabel.length())
					labelString[i1] = tempLabel.charAt(i1);
				else labelString[i1] = ' ';
			}
			final JCheckBox b = new JCheckBox(new String(labelString));
			displayGraph[j] = b;
			b.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			b.setToolTipText("Toggle on/off this graph.");
			b.setSelected(true);
			b.addItemListener(new ItemListener() {
				
				public void itemStateChanged(ItemEvent e) {
					histoPanel2.setGraphVisible(s, b.isSelected());
					histoPanel2.repaint();
				}
			});
			newGraphColor.add(b);
		//	settingsPanel.add(b);
			newGraphColor.add(colorButton);
			j++;
		}
		settingsPanel.add(sameContigGraphPos);
		settingsPanel.add(sameContigGraphRev);
		settingsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		for (JPanel pane: graphColor.values())
			settingsPanel.add(pane);
	/*	int labelLength = 0;
		for (JCheckBox b: displayGraph)
			if (labelLength < b.getText().length());
		for (JCheckBox b: displayGraph) {
			char[] labelString = new char[labelLength];
			String thisLabel = b.getText();
			for (int i = 0; i < labelString.length; i++) {
				if (i < thisLabel.length())
					labelString[i] = thisLabel.charAt(i);
				else labelString[i] = ' ';
			}
			b.setText(new String(labelString));
		} */
		histoPane = new JScrollPane(histoPanel2);
		histoPanel = new JPanel();
		histoPanel.setMinimumSize(new Dimension(800,400));
		histoPanel.setLayout(new BorderLayout());
		zoomPane = new JPanel(new FlowLayout());
		histoPanel.add(zoomPane, BorderLayout.SOUTH);
		histoPanel.add(histoPane, BorderLayout.CENTER);
		zoomIn = new JButton("Zoom In");
		zoomIn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (width * widthScale < contigLength) {
					widthScale *= 2;
					histoPanel2.setHistoPanelSize(width * widthScale, height * heightScale);
					histoPanel2.repaint();
					histoPane.revalidate();;
				}
			}
		});
		zoomOut = new JButton("Zoom Out");
		zoomOut.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (widthScale > 1) {
					widthScale /= 2;
					histoPanel2.setHistoPanelSize(width * widthScale, height * heightScale);
					histoPanel2.repaint();
					histoPanel2.revalidate();
				}
			}
		});
		zoomInY = new JButton("Zoom In");
		zoomInY.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (height * heightScale < y_max - y_min) {
					heightScale *= 2;
					histoPanel2.setHistoPanelSize(width * widthScale, height * heightScale);
					histoPanel2.repaint();
					histoPanel2.revalidate();
				}
			}
		});
		zoomOutY = new JButton("Zoom Out");
		zoomOutY.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (heightScale > 1) {
					heightScale /= 2;
					histoPanel2.setHistoPanelSize(width * widthScale, height * heightScale);
					histoPanel2.repaint();
					histoPanel2.revalidate();
				}
			}
		});
		JLabel xAxis = new JLabel("X-Axis Zoom:");
		xAxis.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		zoomPane.add(xAxis);
		zoomPane.add(zoomIn);
		zoomPane.add(zoomOut);
		JLabel yAxis = new JLabel("Y-Axis Zoom:");
		yAxis.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		zoomPane.add(yAxis);
		zoomPane.add(zoomInY);
		zoomPane.add(zoomOutY);

	//	for (int i = 0; i < graphColor.length; i++)
	//		settingsPanel.add(graphColor[i]);
		settingsPane = new JScrollPane(settingsPanel);
	//	settingsPane.setMaximumSize(new Dimension(300,400));
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, histoPanel, settingsPane);
	//	Dimension splitPaneSize = new Dimension(1200,500);
	//	splitPane.setPreferredSize(splitPaneSize);
		splitPane.setOneTouchExpandable(true);
	//	setPreferredSize(splitPaneSize);
	}

	public JSplitPane splitPane() {return splitPane;}
}

class HistoPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3604183601615232797L;
	private int width, height, x_center, y_center;
	private double x_inc, y_inc, x_min, x_max, y_min, y_max;
	private AffineTransform transform;
	private HashMap<String, Graphs> graphs;
	
	public HistoPanel(int width, int height, double x_min, double x_max, double y_min, double y_max) {
	/*	this.setPreferredSize(new Dimension(width, height));
		this.width = width;
		this.height = height;
		this.x_min = x_min;
		this.x_max = x_max;
		this.y_min = y_min;
		this.y_max = y_max;
		x_inc = (double) width / (x_max - x_min);
		y_inc = (double) height / (y_max - y_min);
		x_center = (int) (x_min * x_inc);
		y_center = (int) (y_max * y_inc);
		this.setBackground(Color.WHITE);
		transform = new AffineTransform();
		transform.translate(x_center, y_center);
		transform.scale(x_inc, -y_inc); */
		setHistoPanel(width, height, x_min, x_max, y_min, y_max);
		graphs = new HashMap<String, Graphs>();
	}
	
	private void setHistoPanel(int width, int height, double x_min, double x_max, double y_min, double y_max) {
		this.setPreferredSize(new Dimension(width, height));
		this.width = width;
		this.height = height;
		this.x_min = x_min;
		this.x_max = x_max;
		this.y_min = y_min;
		this.y_max = y_max;
		x_inc = (double) width / (x_max - x_min);
		y_inc = (double) height / (y_max - y_min);
		x_center = (int) (x_min * x_inc);
		y_center = (int) (y_max * y_inc);
		this.setBackground(Color.WHITE);
		transform = new AffineTransform();
		transform.translate(x_center, y_center);
		transform.scale(x_inc, -y_inc);
	}
	
	public void setHistoPanelSize(int width, int height) {
		setHistoPanel(width, height, this.x_min, this.x_max, this.y_min, this.y_max);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawAxes(g);
		for (Graphs graph: graphs.values())
			if (graph.draw) drawLineGraph(g, graph.points, graph.color);
	}
	
	public void addGraph(String name, Color c, double[] x, double[] y) {
		graphs.put(name, new Graphs(c, x, y));
	}
	
	public void changeColor(String name, Color c) {
		if (graphs.containsKey(name)) {
			Graphs g = graphs.get(name);
			g.color = c;
		}
	}
	
	public void setGraphVisible(String name, boolean display) {
		if (graphs.containsKey(name)) {
			Graphs g = graphs.get(name);
			g.draw = display;
		}
	}
	
	private void drawAxes(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
		g.fillRect(x_center, y_center - 2, width, 4);
		double x_step = 100;
		double y_step = 50;
		for (double j = x_center; j < width; j += x_step) {
			int i = (int) j;
			g.drawString(String.format("%.1f", i / x_inc), i, y_center - 5);
			g.drawLine(i, y_center - 5, i, y_center + 5);
		}
		for (int j = x_center; j > 0; j -= x_step) {
			int i = (int) j;
			g.drawString(String.format("%.1f", i / x_inc), i, y_center - 5);
			g.drawLine(i, y_center - 5, i, y_center + 5);
		}
		for (int j = y_center; j > 0; j -= y_step) {
			int i = (int) j;
			g.drawString(String.format("%.1f", (y_center - i) / y_inc), x_center, i);
			g.drawLine(x_center, i, width, i);
		}
		for (int j = y_center; j < height; j += y_step) {
			int i = (int) j;
			g.drawString(String.format("%.1f", (y_center - i) / y_inc), x_center, i);
			g.drawLine(x_center, i, width, i);
		}
	}
	
	private void drawLineGraph(Graphics g, double[] xy, Color c) {
		g.setColor(c);
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2));
		double[] final_coordinates = new double[xy.length];
		transform.transform(xy, 0, final_coordinates, 0, xy.length/2);
		for (int i = 0; i < final_coordinates.length - 2; i += 2) {
			g2.drawLine((int) final_coordinates[i], (int) final_coordinates[i+1], (int) final_coordinates[i+2], (int) final_coordinates[i+3]);
		}
	}
	
	private class Graphs {
		public boolean draw;
		public Color color;
		public double[] points;
		public Graphs(Color c, double[] p) {
			draw = true;
			color = c;
			points = p;
		}
		public Graphs(Color c, double[] x, double[] y) {
			draw = true;
			color = c;
			if (x.length == y.length) {
				double[] xy = new double[x.length + y.length];
				for (int i = 0; i < x.length; i++) {
					xy[2 * i] = x[i];
					xy[2 * i + 1] = y[i];
				}
				points = xy;
			}
		}
	}
}
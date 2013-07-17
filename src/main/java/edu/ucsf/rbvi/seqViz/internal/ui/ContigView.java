package edu.ucsf.rbvi.seqViz.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.ucsf.rbvi.seqViz.internal.model.Contig;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class ContigView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7713836441534331408L;
	private JButton zoomIn, zoomOut, left, right;
	private JSplitPane splitPane;
	private JScrollPane histoPane, settingsPane;
	private JPanel histoPanel, zoomPane, histoPanel2;
	private ContigsManager manager;
	private Contig contig;
	
	public ContigView(ContigsManager manager, String contig) {
		this.manager = manager;
		this.contig = manager.getContig(contig);
		
		histoPanel2 = new HistoPanel(800, 400, 0, 1000, -10, 11);
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
	//	histoPane.add(histoPanel);

		settingsPane = new JScrollPane();
		settingsPane.setMaximumSize(new Dimension(300,400));
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, histoPanel, settingsPane);
		Dimension splitPaneSize = new Dimension(1100,400);
		splitPane.setPreferredSize(splitPaneSize);
		splitPane.setOneTouchExpandable(true);
		setPreferredSize(splitPaneSize);
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
		graphs = new HashMap<String, Graphs>();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawAxes(g);
		for (Graphs graph: graphs.values())
			drawLineGraph(g, graph.points, graph.color);
	}
	
	public void addGraph(String name, Color c, double[] x, double[] y) {
		graphs.put(name, new Graphs(c, x, y));
	}
	
	private void drawAxes(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(x_center, y_center - 2, width, 4);
		for (int i = x_center; i < width; i += 100) {
			g.drawString((new Double(i / x_inc)).toString(), i, y_center - 5);
			g.drawLine(i, y_center - 5, i, y_center + 5);
		}
		for (int i = x_center; i > 0; i -= 100) {
			g.drawString((new Double(i / x_inc)).toString(), i, y_center - 5);
			g.drawLine(i, y_center - 5, i, y_center + 5);
		}
		for (int i = y_center; i > 0; i -= 50) {
			g.drawString((new Double((y_center - i) / y_inc)).toString(), x_center, i);
			g.drawLine(x_center, i, width, i);
		}
		for (int i = y_center; i < height; i += 50) {
			g.drawString((new Double((y_center - i) / y_inc)).toString(), x_center, i);
			g.drawLine(x_center, i, width, i);
		}
	}
	
	private void drawLineGraph(Graphics g, double[] xy, Color c) {
		g.setColor(c);
		double[] final_coordinates = new double[xy.length];
	/*	if (x.length == y.length) {
			double[] xy = new double[x.length + y.length];
			for (int i = 0; i < x.length; i++) {
				xy[2 * i] = x[i];
				xy[2 * i + 1] = y[i];
			}
		} */
		transform.transform(xy, 0, final_coordinates, 0, xy.length/2);
		for (int i = 0; i < final_coordinates.length - 2; i += 2) {
			g.drawLine((int) final_coordinates[i], (int) final_coordinates[i+1], (int) final_coordinates[i+2], (int) final_coordinates[i+3]);
		}
	}
	
	private class Graphs {
		public Color color;
		public double[] points;
		public Graphs(Color c, double[] p) {
			color = c;
			points = p;
		}
		public Graphs(Color c, double[] x, double[] y) {
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
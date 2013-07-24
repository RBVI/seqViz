package edu.ucsf.rbvi.seqViz.internal.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.HashMap;

import javax.swing.JPanel;

public class HistoPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3604183601615232797L;
	private int width, height, x_center, y_center;
	private Integer beg = null, end = null;
	private double x_inc, y_inc, x_min, x_max, y_min, y_max;
	private AffineTransform transform;
	private HashMap<String, Graphs> graphs;
	private String seq = null;
	private Font seqFont = null;
	
	public HistoPanel(int width, int height, double x_min, double x_max, double y_min, double y_max) {
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
		x_center = (int) (- x_min * x_inc);
		y_center = (int) (y_max * y_inc);
		this.setBackground(Color.WHITE);
		transform = new AffineTransform();
		transform.translate(x_center, y_center);
		transform.scale(x_inc, -y_inc);
	}
	
	public void setHistoPanelSize(int width, int height) {
		setHistoPanel(width, height, this.x_min, this.x_max, this.y_min, this.y_max);
	}
	
	public Point2D realCoordinates(Point2D d) throws NoninvertibleTransformException {
		Point2D temp = new Point2D() {
			private double x = 0, y = 0;
			@Override
			public void setLocation(double x, double y) {
				// TODO Auto-generated method stub
				this.x = x;
				this.y = y;
			}
			
			@Override
			public double getY() {
				// TODO Auto-generated method stub
				return y;
			}
			
			@Override
			public double getX() {
				// TODO Auto-generated method stub
				return x;
			}
		};
		return transform.inverseTransform(d, temp);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawAxes(g);
		if (seq != null) drawSequence(g);
		for (Graphs graph: graphs.values())
			if (graph.draw) drawLineGraph(g, graph.points, graph.color);
	}
	
	public void addSequence(String sequence, Font font, int beg, int end) {
		seq = sequence.substring(beg, end);
		seqFont = font;
		this.beg = beg;
		this.end = end;
		FontMetrics metrics = getFontMetrics(seqFont);
		setHistoPanelSize(metrics.stringWidth(seq), this.height);
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
		g.fillRect(0, y_center - 2, width, 4);
		double x_step = 100;
		double y_step = 50;
		for (double j = 0; j < width; j += x_step) {
			int i = (int) j;
			try {
				Point2D p = realCoordinates(new Point(i, 0));
				g.drawString(String.format("%.1f", p.getX()), i, y_center - 5);
				g.drawLine(i, y_center - 5, i, y_center + 5);

			} catch (NoninvertibleTransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int j = 0; j < height; j += y_step) {
			int i = (int) j;
			try {
				Point2D p = realCoordinates(new Point(0, i));
				g.drawString(String.format("%.1f", p.getY()), 0, i);
				g.drawLine(0, i, width, i);
			} catch (NoninvertibleTransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
/*		for (double j = x_center; j < width; j += x_step) {
			int i = (int) j;
			System.out.println(i);
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
			g.drawString(String.format("%.1f", (y_center - i) / y_inc), 0, i);
			g.drawLine(0, i, width, i);
		}
		for (int j = y_center; j < height; j += y_step) {
			int i = (int) j;
			g.drawString(String.format("%.1f", (y_center - i) / y_inc), 0, i);
			g.drawLine(0, i, width, i);
		} */
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
	
	private void drawSequence(Graphics g) {
		g.setFont(seqFont);
		g.drawString(seq, 0, y_center + 15);
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
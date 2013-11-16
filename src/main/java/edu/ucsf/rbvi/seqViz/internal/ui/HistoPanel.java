package edu.ucsf.rbvi.seqViz.internal.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.HashMap;

import javax.swing.JPanel;

/**
 * Creates a JPanel that displays many histograms as a line graphs. No longer
 * used since these feautres have been folded into SequencePanel.
 * @author Allan Wu
 *
 */
public class HistoPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3604183601615232797L;
	// Whether or not to draw the a vertical line, used for purposes of
	// "sweeping" an area on HistoPanel
	private boolean drawYLine = false;
	/**
	 * width -- width (number of pixels)
	 * height -- height (number of pixels)
	 * x_center -- horizontal center (0-point) of the histogram
	 * y_center -- vertical center (0-point) of the histogram
	 * begLine -- beginning of a sweep of the histogram (show line when drawYLine == true)
	 * endLine -- ending of a sweep of the histogram (show line when drawYLine == true)
	 */
	private int width, height, x_center, y_center, begLine, endLine;
	private Integer beg = null, end = null;
	/**
	 * x_inc -- number of pixels that equals an increment of 1 in the x-axis of the graph (can be a fraction)
	 * y_inc -- number of pixels that equals an increment of 1 in the y-axis of the graph (can be a fraction)
	 * x_min -- minimum x-axis range of histogram
	 * x_max -- maximum x-axis range of histogram
	 * y_min -- minimum y-axis range of histogram
	 * y_max -- maximum y-axis range of histogram
	 */
	private double x_inc, y_inc, x_min, x_max, y_min, y_max;
	// Affine transform that transforms screen coordinates into real coordinates
	private AffineTransform transform;
	// Graphs that can be displayed on this panel (controlled by setGraphVisible() method)
	private HashMap<String, Graphs> graphs;
	// The sequence to be displayed at the center of the histogram
	private String seq = null;
	// Font of the above sequence
	private Font seqFont = null;
	
	/**
	 * Create a HistoPanel. Creates a panel on the screen the size of width X height, with scales
	 * determined by the parameters x_min, x_max, y_min and y_max.
	 * @param width width of the panel (pixels)
	 * @param height height of the panel (pixel)
	 * @param x_min lower limit of the x-axis
	 * @param x_max upper limit of the x-axis
	 * @param y_min lower limit of the y-axis
	 * @param y_max upper limit of the y-axis
	 */
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
	
	/**
	 * Resize the HistoPanel.
	 * @param width width of the panel (pixels)
	 * @param height height of the panel (pixel)
	 */
	public void setHistoPanelSize(int width, int height) {
		setHistoPanel(width, height, this.x_min, this.x_max, this.y_min, this.y_max);
	}
	
	/**
	 * Change the x and y axes of the HistoPanel.
	 * @param x_min lower limit of the x-axis
	 * @param x_max upper limit of the x-axis
	 * @param y_min lower limit of the y-axis
	 * @param y_max upper limit of the y-axis
	 */
	public void setHistoPanelSize(int x_min, int x_max, int y_min, int y_max) {
		setHistoPanel(this.width, this.height, x_min, x_max, y_min, y_max);
	}
	
	/**
	 * Convert screen coordinates to real coordinates.
	 * @param d the screen coordinates
	 * @return Real coordinates.
	 * @throws NoninvertibleTransformException
	 */
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
	
	/**
	 * Convert real coordinates to screen coordinates
	 * @param d real coordinates
	 * @return screen coordinates
	 */
	public Point2D cartesianCoordinates(Point2D d) {
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
		return transform.transform(d, temp);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		AntiAlias.antiAliasing((Graphics2D) g);
		drawAxes(g);
		if (seq != null) drawSequence(g);
		for (Graphs graph: graphs.values())
			if (graph.draw) drawLineGraph(g, graph.points, graph.color);
		if (drawYLine) {
			g.setColor(Color.BLACK);
			g.drawLine(begLine, 0, begLine, this.getHeight());
			g.drawLine(endLine, 0, endLine, this.getHeight());
		}
	}
	
	/**
	 * Not to be used.
	 * @param sequence
	 * @param font
	 * @param beg
	 * @param end
	 */
	private void addSequence(String sequence, Font font, int beg, int end) {
		seq = sequence.substring(beg, end);
		seqFont = font;
		this.beg = beg;
		this.end = end;
		FontMetrics metrics = getFontMetrics(seqFont);
		setHistoPanelSize(metrics.stringWidth(seq), this.height);
	}
	
	/**
	 * Add histogram to the HistoPanel. If the name is the same as a previous histogram, the
	 * previous histogram is overwritten by the new one.
	 * @param name Name of the histogram.
	 * @param c Color the histogram.
	 * @param x x-coordinates of the points of the histogram.
	 * @param y y-coordinates of the name of the histogram.
	 */
	public void addGraph(String name, Color c, double[] x, double[] y) {
		graphs.put(name, new Graphs(c, x, y));
	}
	
	/**
	 * Change color of a histogram already in HistoPanel.
	 * @param name Name of the histogram.
	 * @param c Color of the histogram.
	 */
	public void changeColor(String name, Color c) {
		if (graphs.containsKey(name)) {
			Graphs g = graphs.get(name);
			g.color = c;
		}
	}
	
	/**
	 * Set a histogram as visible or invisible.
	 * @param name Name of the histogram.
	 * @param display "true" for allowing the histogram to be displayed, "false" if not.
	 */
	public void setGraphVisible(String name, boolean display) {
		if (graphs.containsKey(name)) {
			Graphs g = graphs.get(name);
			g.draw = display;
		}
	}
	
	/**
	 * Set a vertical line in the histogram. Used as left limit of SequenceView.
	 * @param beg x-coordinate of the vertical line.
	 */
	public void setBegLine(int beg) {
		begLine = beg;
	}
	
	/**
	 * Set a vertical line in the histogram. Used as right limit of SequenceView.
	 * @param end x-coordinate of vertical line.
	 */
	public void setEndLine(int end) {
		endLine = end;
	}
	
	/**
	 * Set vertical determined by setBegLine() and setEndLine() lines visible.
	 * @param b "true" for visible and "false" for invisible.
	 */
	public void setDrawYLines(boolean b) {
		drawYLine = b;
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
	
	/**
	 * Inner class to hold a graph for this HistoPanel
	 * @author Allan Wu
	 *
	 */
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
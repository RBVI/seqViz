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

/**
 * Creates a JPanel that displays many histograms as a line graphs. It also displays a DNA
 * sequence in the middle that is displayed when it is zoomed in close enough, and can be
 * copied by selecting it.
 * @author Allan Wu
 *
 */
public class SequencePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5894823555682540267L;
	// Default font for sequence
	private static final String fontName = Font.MONOSPACED;
	// Default font style for sequence
	private static int fontStyle = Font.PLAIN;
	// Default font
	private static Font font = new Font(fontName, fontStyle, 12);
	/**
	 *  drawYLine -- Whether or not to draw the a vertical line, used for purposes of
	 *  "sweeping" an area on HistoPanel
	 *  highlightSequence -- highlight the sequence in the panel
	 */
	private boolean drawYLine = false, highlightSequence = false;
	/**
	 * width -- width (number of pixels)
	 * height -- height (number of pixels)
	 * x_center -- horizontal center (0-point) of the histogram
	 * y_center -- vertical center (0-point) of the histogram
	 * begLine -- beginning of a sweep of the histogram (show line when drawYLine == true)
	 * endLine -- ending of a sweep of the histogram (show line when drawYLine == true)
	 * sequenceHeight -- height of sequence (pixels)
	 * characterWidth -- width of one character (pixels)
	 * xBeg -- beginning of selected sequence
	 * xEnd -- end of selected sequence
	 */
	private int width, height, x_center, y_center, y_center_lower, begLine, endLine, sequenceHeight, characterWidth, xBeg = 0, xEnd = 0;
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
	private AffineTransform transform, transformNeg;
	// Graphs that can be displayed on this panel (controlled by setGraphVisible() method)
	private HashMap<String, Graphs> graphs;
	// The sequence to be displayed at the center of the histogram
	private String seq = null, selectedGraph = null;
	// Font of the above sequence
	private Font seqFont;
	// FontMetrics for this panel
	private FontMetrics fontMetrics;
	
	/**
	 * Create a SequencePanel. Creates a panel on the screen the size of width X height, with scales
	 * determined by the parameters x_min, x_max, y_min and y_max.
	 * @param width width of the panel (pixels)
	 * @param height height of the panel (pixel)
	 * @param x_min lower limit of the x-axis
	 * @param x_max upper limit of the x-axis
	 * @param y_min lower limit of the y-axis
	 * @param y_max upper limit of the y-axis
	 */
	public SequencePanel(int width, int height, double x_min, double x_max, double y_min, double y_max, String sequence) {
		seq = sequence;
		setSequencePanel(width, height, x_min, x_max, y_min, y_max);
		graphs = new HashMap<String, Graphs>();
	}
	
	private void setSequencePanel(int width, int height, double x_min, double x_max, double y_min, double y_max) {
		this.setPreferredSize(new Dimension(width, height));
		this.width = width;
		this.height = height;
		this.x_min = x_min;
		this.x_max = x_max;
		this.y_min = y_min;
		this.y_max = y_max;
		
		fontMetrics = this.getFontMetrics(font);
		sequenceHeight = fontMetrics.getHeight();
		characterWidth = fontMetrics.stringWidth(seq) / seq.length();
		
		x_inc = (double) width / (x_max - x_min);
		y_inc = (double) (height - sequenceHeight) / (y_max - y_min);
		x_center = (int) (- x_min * x_inc);
		y_center = (int) (y_max * y_inc);
		y_center_lower = (int) (y_max * y_inc + sequenceHeight);
		this.setBackground(Color.WHITE);
		transform = new AffineTransform();
		transform.translate(x_center, y_center);
		transform.scale(x_inc, -y_inc);
		transformNeg = new AffineTransform();
		transformNeg.translate(x_center, y_center_lower);
		transformNeg.scale(x_inc, -y_inc);
	}
	
	/**
	 * Resize the SequencePanel.
	 * @param width width of the panel (pixels)
	 * @param height height of the panel (pixel)
	 */
	public void setSequencePanelSize(int width, int height) {
		setSequencePanel(width, height, this.x_min, this.x_max, this.y_min, this.y_max);
	}
	
	/**
	 * Change the x and y axes of the SequencePanel.
	 * @param x_min lower limit of the x-axis
	 * @param x_max upper limit of the x-axis
	 * @param y_min lower limit of the y-axis
	 * @param y_max upper limit of the y-axis
	 */
	public void setSequencePanelSize(int x_min, int x_max, int y_min, int y_max) {
		setSequencePanel(this.width, this.height, x_min, x_max, y_min, y_max);
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
		if (d.getY() <= y_center)
			return transform.inverseTransform(d, temp);
		else if (d.getY() <= y_center_lower) {
			transform.inverseTransform(d, temp);
			temp.setLocation(temp.getX(), 0);
			return temp;
		}
		else
			return transformNeg.inverseTransform(d, temp);
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
		if (d.getY() < 0)
			return transform.transform(d, temp);
		else
			return transform.transform(d, temp);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		AntiAlias.antiAliasing((Graphics2D) g);
	//	if (seq != null) drawSequence(g);
		for (Graphs graph: graphs.values())
			if (graph.draw && graph.points.containsKey(selectedGraph)) drawLineGraph(g, graph.points.get(selectedGraph), graph.color);
		g.setColor(Color.WHITE);
		g.fillRect(0, y_center, width, sequenceHeight);
		drawAxes(g);
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
	private void addSequence(String sequence, int beg, int end) {
		seq = sequence.substring(beg, end);
	//	seqFont = font;
		this.beg = beg;
		this.end = end;
	/*	FontMetrics metrics = getFontMetrics(seqFont);
		setSequencePanelSize(metrics.stringWidth(seq), this.height); */
	}
	
	/**
	 * Add histogram to the SequencePanel. If the name is the same as a previous histogram, the
	 * previous histogram is overwritten by the new one.
	 * @param name Name of the histogram.
	 * @param c Color the histogram.
	 * @param x x-coordinates of the points of the histogram.
	 * @param y y-coordinates of the name of the histogram.
	 */
	public void addGraph(String name, Color c, double[] x, double[] y) {
		if (graphs.containsKey(name)) {
			Graphs getGraph = graphs.get(name);
			getGraph.addSubGraph(c == null ? getGraph.color : c, x, y);
		}
		else
			graphs.put(name, new Graphs(c, x, y));
	}
	
	/**
	 * Get the X coordinates of graphs selected to be drawn in this panel.
	 * @return A HashMap<String, double[]> where the key is the name of the graph
	 * and the value are the value of the X coordinates.
	 */
	public HashMap<String, double[]> getSelectedGraphsX() {
		HashMap<String, double[]> results = new HashMap<String, double[]>();
		for (String name: graphs.keySet()) {
			Graphs g = graphs.get(name);
			if (g.draw) {
				double []	p = g.points.get(selectedGraph),
							x = new double[p.length/2];
				for (int i = 0; i < p.length / 2; i++)
					x[i] = p[i * 2];
				results.put(name, x);
			}
		}
		return results;
	}
	
	/**
	 * Get the Y coordinates of graphs selected to be drawn in this panel.
	 * @return A HashMap<String, double[]> where the key is the name of the graph
	 * and the value are the value of the Y coordinates.
	 */
	public HashMap<String, double[]> getSelectedGraphsY() {
		HashMap<String, double[]> results = new HashMap<String, double[]>();
		for (String name: graphs.keySet()) {
			Graphs g = graphs.get(name);
			if (g.draw) {
				double []	p = g.points.get(selectedGraph),
							y = new double[p.length/2];
				for (int i = 0; i < p.length / 2; i++)
					y[i] = p[i * 2 + 1];
				results.put(name, y);
			}
		}
		return results;
	}

	/**
	 * Change color of a histogram already in SequencePanel.
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
	
	/**
	 * Highlight part of sequence selected
	 * @param b "true" for visible and "false" for invisible.
	 */
	public void setHighlightSequence(boolean b) {
		highlightSequence = b;
	}
	
	/**
	 * Width of the font used to display the sequence.
	 * @return Width of the font used to display the sequence.
	 */
	public int characterWidth() {return characterWidth;}
	
	/**
	 * Beginning of sequence selected.
	 * @return
	 */
	public int seqBeg() {return xBeg;}
	
	/**
	 * End of sequence selected.
	 * @return
	 */
	public int seqEnd() {return xEnd;}
	
	/**
	 * Return the sequence selected in the SequencePanel.
	 * @return selected sequence
	 * @throws Exception Exception thrown if no sequence has been selected.
	 */
	public String selectedSequence() throws Exception {
		if (highlightSequence)
			return seq.substring(xBeg, xEnd+1);
		else throw new Exception();
	}
	
	/**
	 * Set the graph to display.
	 * @param g Graph that is displayed.
	 */
	public void setGraph(String g) {selectedGraph = g;}
	
	private void drawAxes(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
		g.fillRect(0, y_center - 2, width, 4);
		g.fillRect(0, y_center_lower -2, width, 4);
		double x_step = 100;
		double y_step = 50;
		for (double j = 0; j < width; j += x_step) {
			int i = (int) j;
			try {
				Point2D p = realCoordinates(new Point(i, 0));
				g.drawString(String.format("%.1f", p.getX()), i, y_center - 5);
				g.drawLine(i, y_center - 5, i, y_center);
				g.drawLine(i, y_center_lower, i, y_center_lower + 5);

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
		g.setFont(font);
		if (highlightSequence) {
			g.setColor(Color.BLACK);
			Point pBeg = new Point(begLine, 0), pEnd = new Point(endLine, 0);
			Point2D p2Beg, p2End;
			try {
				p2Beg = realCoordinates(pBeg);
				p2End = realCoordinates(pEnd);
				xBeg = (int) (p2Beg.getX()-1);
				xEnd = (int) (p2End.getX()-1);
				g.fillRect((int) (xBeg * x_inc), y_center, (int) (x_inc * (1 + xEnd - xBeg)), sequenceHeight);
			} catch (NoninvertibleTransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 0; i < seq.length(); i++) {
			if (highlightSequence && i >= xBeg && i <= xEnd) {
				switch (seq.charAt(i)) {
					case 'A': g.setColor(Color.RED); break;
					case 'T': g.setColor(Color.GREEN); break;
					case 'C': g.setColor(Color.YELLOW); break;
					case 'G': g.setColor(Color.WHITE); break;
					case 'a': g.setColor(Color.RED); break;
					case 't': g.setColor(Color.GREEN); break;
					case 'c': g.setColor(Color.YELLOW); break;
					case 'g': g.setColor(Color.WHITE); break;
					default: g.setColor(Color.GRAY); break;
				}
			}
			else {
				switch (seq.charAt(i)) {
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
			}
			char[] character = {seq.charAt(i)};
			if (x_inc > characterWidth)
				g.drawString(new String(character), (int) (i * x_inc), y_center + fontMetrics.getAscent());
			else if (x_inc >= 1)
				g.drawLine((int) ((i * x_inc) + (x_inc / 2)), y_center, (int) ((i * x_inc) + (x_inc / 2)), y_center_lower);
		}
	}
	
	private void drawLineGraph(Graphics g, double[] xy, Color c) {
		g.setColor(c);
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2));
		double[] final_coordinates = new double[xy.length];
		transform.transform(xy, 0, final_coordinates, 0, xy.length/2);
		for (int i = 1; i < final_coordinates.length; i += 2)
			if (xy[i] < 0)
				final_coordinates[i] += sequenceHeight;
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
		public HashMap<String, double[]> points;
		public Graphs(Color c, double[] p) {
			draw = true;
			color = c;
			points = new HashMap<String, double[]>();
		}
		public Graphs(Color c, double[] x, double[] y) {
			draw = true;
			points = new HashMap<String, double[]>();
			addSubGraph(c,x,y);
		}
		public void addSubGraph(Color c, double[] x, double[] y) {
			color = c;
			if (x.length == y.length) {
				double[] xy = new double[x.length + y.length];
				for (int i = 0; i < x.length; i++) {
					xy[2 * i] = x[i];
					xy[2 * i + 1] = y[i];
				}
				points.put(selectedGraph, xy);
			}
		}
	}
}

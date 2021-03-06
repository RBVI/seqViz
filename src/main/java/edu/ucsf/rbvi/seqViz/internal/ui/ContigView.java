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
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;

import edu.ucsf.rbvi.seqViz.internal.events.DisplayGraphEvent;
import edu.ucsf.rbvi.seqViz.internal.events.DisplayGraphEventListener;
import edu.ucsf.rbvi.seqViz.internal.model.ComplementaryGraphs;
import edu.ucsf.rbvi.seqViz.internal.model.Contig;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager.ReadType;

/**
 * A SplitPanel containing code for displaying detailed histograms of read coverage. Create a
 * ContigView object, then call SplitPane to get a JSplitPane containing the histogram.
 * 
 * @author aywu
 *
 */
public class ContigView implements DisplayGraphEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7713836441534331408L;
	/**
	 * defaultWidth -- default width of SequencePanel (pixels)
	 * defaultHeight -- default height of SequencePanel (pixels)
	 * buttonWidth -- default width of button (pixels)
	 * buttonHeight -- default height of button (pixels)
	 */
	private static final int defaultWidth = 800, defaultHeight = 400, buttonWidth = 20, buttonHeight = 10;
	/**
	 * Buttons for doing different tasks
	 * reset -- reset SequencePanel to default width and height
	 * zoomIn -- zoom in on x-axis by factor of 2
	 * zoomOut -- zoom out on x-axis by factor of 2
	 * zoomInY -- zoom in on y-axis by factor of 2
	 * zoomOutY -- zoom out on y-axis by factor of 2
	 * export -- export image of SequencePanel
	 * exportData -- export data of SequencePanel
	 */
	private JButton reset, zoomIn, zoomOut, zoomInY, zoomOutY, export, exportData;
	// JSplitPane returned
	private JSplitPane splitPane;
	/**
	 * Two major JScrollPanes used in the user interface
	 * histoPane -- a scroll pane containing SequencePanel
	 * settingsPane -- pane for controlling which graph is displayed and the color of the graph.
	 */
	private JScrollPane histoPane, settingsPane;
	/**
	 * JPanels used in the user interface
	 * histoPanel -- JPanel that contains histoPane and zoomPane that controls the display of SequencePanel
	 * zoomPane -- JPanel containing the zoom buttons placed at the bottom of histoPanel
	 */
	private JPanel histoPanel, zoomPane, settingsPanel;
	// The panel containing the histograms and sequence
	private SequencePanel histoPanel2;
	private ContigsManager manager;
	private Contig contig;
	private ComplementaryGraphs graphs;
	/**
	 * Various dimensions of the histogram
	 * y_min -- minimum of the y-axis range of the histogram
	 * y_min -- maximum of the y-axis range of the histogram
	 * contigLength -- length of the contig (in this case the sequence)
	 * binSize -- size of bin for histogram
	 */
	private long y_min = 0, y_max = 0, contigLength = 0, binSize;
	/**
	 * Various settings on the size of the histogram
	 * width -- width (pixels)
	 * height -- height (pixels)
	 * widthScale -- scaling factor for width (changed by zoom buttons)
	 * heightScale -- scaling factor for height (changed by zoom buttons)
	 * begLine -- beginning of the sweep
	 * endLine -- end of the sweep
	 * characterWidth -- width of each character in the sequence displayed
	 */
	private int width = defaultWidth, height = defaultHeight, widthScale = 1, heightScale = 1, begLine, endLine, characterWidth = 1;
	/**
	 * Another scaling factor for width (changed by sweeping)
	 */
	private double incWidthScale = 1;
	/**
	 * Clipboard to place the selected sequence into
	 */
	private Clipboard clipboard;
	private String selectedSequence = null;
	
	/**
	 * Create a ContigView object.
	 * @param networkView A CyNetworkView containing the tables in defaultNetworkTable() of the graph
	 *        generated by ContigsManager.
	 * @param suid SUID of the contig node
	 */
	public ContigView(CyNetworkView networkView, Long suid) {
		final CyNetworkView networkViewFinal = networkView;
		final CyNetwork network = networkView.getModel();
		final Long suid2 = suid;
		CyTable table = network.getDefaultNetworkTable();
		final CyTable nodeTable = network.getDefaultNodeTable();
		final String contig = nodeTable.getRow(suid).get(CyNetwork.NAME, String.class);
		List<String> graphs = table.getRow(network.getSUID()).getList(contig + ":graphColumns", String.class);
	//	JPanel[] graphColor = new JPanel[graphs.size()];
		SortedMap<String, JPanel> graphColor = new TreeMap<String, JPanel>();
		final HashMap<String, Long> contigMap = new HashMap<String, Long>();
		JCheckBox[] displayGraph = new JCheckBox[graphs.size()];
		JPanel sameContigGraphPos = null, sameContigGraphRev = null;
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		binSize = table.getRow(network.getSUID()).get("graphBinSize", Long.class);
		Random random = new Random(70);
		contigLength = nodeTable.getRow(suid).get("length", Long.class);
		for (long nodeID: nodeTable.getPrimaryKey().getValues(Long.class)) {
			contigMap.put(nodeTable.getRow(nodeID).get(CyNetwork.NAME, String.class), nodeID);
			nodeTable.getRow(nodeID).set(CyNetwork.SELECTED, false);
		}
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

		histoPanel2 = new SequencePanel(width, height, 1, contigLength, y_min, y_max, nodeTable.getRow(suid).get("sequence", String.class));
		characterWidth = histoPanel2.characterWidth();
		int j = 0;
		int labelLength = 0, tempLength;
		for (String s: graphs)
			if (labelLength < (tempLength = s.split(":")[1].length() + s.split(":")[2].length() + 1))
				labelLength = tempLength;
		for (final String s: graphs) {
			HashMap<String, List<Long>> allGraph = new HashMap<String, List<Long>>();
			HashMap<String, double[]> allY = new HashMap<String, double[]>(), allX = new HashMap<String, double[]>();
			for (ReadType rType: ReadType.values()) {
				String type = rType.toString();
				List<Long> graph = table.getRow(network.getSUID()).getList(s + (type == null ? "" : ":" + type), Long.class);
				if (graph != null) {
					double[] y = new double[graph.size()];
					double[] x = new double[graph.size()];
					int i = 0;
					for (Long l: graph) {
						y[i] = l;
						x[i] = i * binSize + 1;
						i++;
					}
					allGraph.put(type, graph);
					allY.put(type, y);
					allX.put(type, x);
				}
			}
			final Color randomColor;
			JPanel newGraphColor = new JPanel();
		//	graphColor[j] = newGraphColor;
			newGraphColor.setLayout(new FlowLayout());
			if (! s.split(":")[0].equals(s.split(":")[1])) {
				randomColor = new Color(((int) (random.nextFloat() * 4)) * 64, ((int) (random.nextFloat() * 4)) * 64, ((int) (random.nextFloat() * 4)) * 64);
				graphColor.put(s, newGraphColor);
			}
			else {
				if (s.split(":")[2].equals("+"))
					sameContigGraphPos = newGraphColor;
				else if (s.split(":")[2].equals("-"))
					sameContigGraphRev = newGraphColor;
				randomColor = Color.GRAY;
			}
			for (ReadType rType: ReadType.values()) {
				String t = rType.toString();
				if (allX.containsKey(t) && allY.containsKey(t)) {
					histoPanel2.setGraph(t);
					histoPanel2.addGraph(s, randomColor, allX.get(t), allY.get(t));
				}
			}
			histoPanel2.setGraph(null);
			final JButton colorButton = new JButton();
			BufferedImage image = new BufferedImage(buttonWidth, buttonHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			g2.setColor(randomColor);
			g2.fillRect(0, 0, buttonWidth, buttonHeight);
			g2.dispose();
			ImageIcon buttonColor = new ImageIcon(image);
			colorButton.setIcon(buttonColor);
			colorButton.setBackground(randomColor);
			colorButton.setToolTipText("Change the color of this graph.");
			colorButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					Color c = JColorChooser.showDialog(splitPane,
							"Choose color of graph", colorButton.getBackground());
					BufferedImage image = new BufferedImage(buttonWidth, buttonHeight, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2 = image.createGraphics();
					g2.setColor(c);
					g2.fillRect(0, 0, buttonWidth, buttonHeight);
					g2.dispose();
					ImageIcon buttonColor = new ImageIcon(image);
					histoPanel2.changeColor(s, c);
					colorButton.setIcon(buttonColor);
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
			if (sameContigGraphPos == newGraphColor || sameContigGraphRev == newGraphColor) {
				b.setSelected(false);
				histoPanel2.setGraphVisible(s, false);
			}
			else {
				b.setSelected(true);
				histoPanel2.setGraphVisible(s, true);
			}
			if (contigMap.containsKey(s.split(":")[1]))
				nodeTable.getRow(contigMap.get(s.split(":")[1])).set(CyNetwork.SELECTED, b.isSelected());
			b.addItemListener(new ItemListener() {
				
				public void itemStateChanged(ItemEvent e) {
					histoPanel2.setGraphVisible(s, b.isSelected());
					histoPanel2.repaint();
					if (contigMap.containsKey(s.split(":")[1]))
						nodeTable.getRow(contigMap.get(s.split(":")[1])).set(CyNetwork.SELECTED, b.isSelected());
					networkViewFinal.updateView();
				}
			});
			newGraphColor.add(b);
		//	settingsPanel.add(b);
			newGraphColor.add(colorButton);
			j++;
		}
		networkViewFinal.updateView();
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
		reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				incWidthScale = defaultWidth / width;
				widthScale = defaultWidth / width;
				heightScale = defaultHeight / height;
				histoPanel2.setSequencePanelSize(defaultWidth, defaultHeight);
				histoPanel2.revalidate();
				histoPanel2.repaint();
			}
		});
		zoomIn = new JButton("Zoom In");
		zoomIn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (width * widthScale < contigLength * characterWidth * 2) {
					widthScale *= 2;
					incWidthScale = widthScale;
					histoPanel2.setSequencePanelSize(width * widthScale, height * heightScale);
					histoPanel2.revalidate();
					histoPanel2.repaint();
				}
			}
		});
		zoomOut = new JButton("Zoom Out");
		zoomOut.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (widthScale > 1) {
					widthScale /= 2;
					incWidthScale = widthScale;
					histoPanel2.setSequencePanelSize(width * widthScale, height * heightScale);
					histoPanel2.revalidate();
					histoPanel2.repaint();
				}
			}
		});
		zoomInY = new JButton("Zoom In");
		zoomInY.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (height * heightScale < y_max - y_min) {
					heightScale *= 2;
					histoPanel2.setSequencePanelSize(width * widthScale, height * heightScale);
					histoPanel2.revalidate();
					histoPanel2.repaint();
					ChangeYScrollBar changeY = new ChangeYScrollBar(histoPane, (double) y_max / (double) (y_max - y_min));
					changeY.start();
				}
			}
		});
		zoomOutY = new JButton("Zoom Out");
		zoomOutY.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (heightScale > 1) {
					heightScale /= 2;
					histoPanel2.setSequencePanelSize(width * widthScale, height * heightScale);
					histoPanel2.revalidate();
					histoPanel2.repaint();
					ChangeYScrollBar changeY = new ChangeYScrollBar(histoPane, (double) y_max / (double) (y_max - y_min));
					changeY.start();
				}
			}
		});
		export = new JButton("Export Image");
		export.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser saveImage = new JFileChooser();
				ImageFilter chosenFilter;
				saveImage.addChoosableFileFilter(chosenFilter = new ImageFilter(Utils.png));
				saveImage.addChoosableFileFilter(new ImageFilter(Utils.jpg));
				saveImage.addChoosableFileFilter(new ImageFilter(Utils.gif));
				saveImage.setFileFilter(chosenFilter);
				saveImage.setAcceptAllFileFilterUsed(false);
				if (JFileChooser.APPROVE_OPTION == saveImage.showSaveDialog(splitPane)) {
					File outFile = saveImage.getSelectedFile();
					chosenFilter = (ImageFilter) saveImage.getFileFilter();
					Utils util = new Utils();
					if (util.getExtension(outFile) == null || !util.getExtension(outFile).equals(chosenFilter.getExtension()))
						outFile = new File(outFile.getAbsoluteFile() + "." + chosenFilter.getExtension());
					if (!outFile.exists()) {
						BufferedImage image = new BufferedImage(histoPanel2.getWidth(), histoPanel2.getHeight(), BufferedImage.TYPE_INT_ARGB);
						Graphics2D g = image.createGraphics();
						histoPanel2.paint(g);
						g.dispose();
						try {
							ImageIO.write(image, chosenFilter.getExtension(), outFile);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					else
						JOptionPane.showMessageDialog(splitPane, "File already exists. Choose different file name.", "Image not saved", JOptionPane.ERROR_MESSAGE);;
				}
			}
		});
		exportData = new JButton("Export Data");
		exportData.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser saveData = new JFileChooser();
				if (JFileChooser.APPROVE_OPTION == saveData.showSaveDialog(splitPane)) {
					File outFile = saveData.getSelectedFile();
					HashMap<String, double[]> x = histoPanel2.getSelectedGraphsX();
					HashMap<String, double[]> y = histoPanel2.getSelectedGraphsY();
					if (!outFile.exists()) {
						try {
							PrintWriter writer = new PrintWriter(outFile);
							int graphLength = 0;
							boolean first = true;
							for (String s: x.keySet()) {
								if (!first) writer.print(",");
								writer.print("\"" + s + ":position\"," + s + ":coverage");
								first = false;
								if (x.get(s).length > graphLength) graphLength = x.get(s).length;
							}
							writer.print("\n");
							for (int i = 0; i < graphLength; i++) {
								first = true;
								for (String s: x.keySet()) {
									if (!first) writer.print(",");
									if (x.get(s).length > i)
										writer.print("\"" + x.get(s)[i] + "\"," + y.get(s)[i] + "\"");
									else
										writer.print("\"\"");
									first = false;
								}
								writer.write("\n");
							}
							writer.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					else
						JOptionPane.showMessageDialog(splitPane, "File already exists. Choose different file name.", "File not saved", JOptionPane.ERROR_MESSAGE);;

				}
			}
		});
		JLabel xAxis = new JLabel("X-Axis Zoom:");
		zoomPane.add(reset);
		xAxis.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		zoomPane.add(xAxis);
		zoomPane.add(zoomIn);
		zoomPane.add(zoomOut);
		JLabel yAxis = new JLabel("Y-Axis Zoom:");
		yAxis.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		zoomPane.add(yAxis);
		zoomPane.add(zoomInY);
		zoomPane.add(zoomOutY);
		zoomPane.add(export);
		zoomPane.add(exportData);

	//	for (int i = 0; i < graphColor.length; i++)
	//		settingsPanel.add(graphColor[i]);
		settingsPane = new JScrollPane(settingsPanel);
	//	settingsPane.setMaximumSize(new Dimension(300,400));
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, histoPanel, settingsPane);
	//	Dimension splitPaneSize = new Dimension(1200,500);
	//	splitPane.setPreferredSize(splitPaneSize);
		splitPane.setOneTouchExpandable(true);
	//	setPreferredSize(splitPaneSize);
		histoPanel2.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {
				histoPanel2.setDrawYLines(false);
				StringSelection selection;
				boolean reLoadedPanel = false;
				double barFactor = 0;
				try {
					selection = new StringSelection(selectedSequence = histoPanel2.selectedSequence());
					clipboard.setContents(selection, null);
				} catch (Exception e2) {
					if (begLine < endLine) {
						double tempIncWidthScale = incWidthScale * width / (double) (endLine - begLine + 1);
						barFactor = ((double) begLine) / (double) (width * incWidthScale - (endLine - begLine));
						if (width * tempIncWidthScale < contigLength * characterWidth * 2) {
							incWidthScale = tempIncWidthScale;
							widthScale = (int) incWidthScale;
							histoPanel2.setSequencePanelSize((int) (width * incWidthScale), height * heightScale);
							reLoadedPanel = true;
						}
					}
				}
				histoPanel2.setHighlightSequence(false);
			/*	if (begLine < endLine) {
					Point d = new Point(begLine, 0);
					Point d2 = new Point(endLine, 0);
					try {
						Point2D p = histoPanel2.realCoordinates(d),
								p2 = histoPanel2.realCoordinates(d2);
						JPanel seqView = new SequenceView(network, suid2, (int) p.getX(), (int) p2.getX());
						JFrame frame = new JFrame(contig);
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						frame.getContentPane().add(seqView);
						frame.pack();
						frame.setVisible(true);
					} catch (NoninvertibleTransformException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} */
				histoPanel2.revalidate();
				histoPanel2.repaint();
				if (reLoadedPanel) {
					ChangeScrollBar change = new ChangeScrollBar(histoPane, barFactor);
					change.start();
				}
			}
			
			public void mousePressed(MouseEvent e) {
				histoPanel2.setBegLine(begLine = e.getX());
				histoPanel2.setEndLine(endLine = e.getX());
				Point pBeg = new Point(begLine, e.getY());
				Point2D p2Beg;
				boolean sequenceSelected = false;
				try {
					p2Beg = histoPanel2.realCoordinates(pBeg);
					sequenceSelected = p2Beg.getY() == 0;
				} catch (NoninvertibleTransformException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				if (sequenceSelected)
					histoPanel2.setHighlightSequence(true);
				else
					histoPanel2.setDrawYLines(true);
				histoPanel2.repaint();
			}
			
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseClicked(MouseEvent e) {
			/*	Point d = new Point(e.getX(), e.getY());
				Point2D d2;
				try {
					d2 = histoPanel2.realCoordinates(d);
					JPanel seqView = new SequenceView(net, suid2, (int) d2.getX() - 50, (int) d2.getX() + 50);
					JFrame frame = new JFrame(contig);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.getContentPane().add(seqView);
					frame.pack();
					frame.setVisible(true);
				} catch (NoninvertibleTransformException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} */
			}
		});
		histoPanel2.addMouseMotionListener(new MouseMotionListener() {
			
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseDragged(MouseEvent e) {
				if (e.getX() > begLine)
					histoPanel2.setEndLine(endLine = e.getX());
			/*	Point2D p = histoPanel2.cartesianCoordinates(new Point(0, 0)),
						p2 = histoPanel2.cartesianCoordinates(new Point(200, 0));
				int diff = (int) (p2.getX() - p.getX());
				if (begLine + diff <= endLine)
					histoPanel2.setBegLine(begLine = (endLine - diff)); */
				histoPanel2.repaint();
			}
		});
	}
	
	private class ChangeScrollBar extends Thread {
		private JScrollPane b;
		private double factor;
		public ChangeScrollBar(JScrollPane scroll, double barFactor) {
			b = scroll;
			factor = barFactor;
		}
		public void run() {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JScrollBar xBar = b.getHorizontalScrollBar();
			xBar.setValue((int) ((xBar.getMaximum()-xBar.getMinimum()-xBar.getBlockIncrement(1)) * factor));
		}
	}

	private class ChangeYScrollBar extends Thread {
		private JScrollPane b;
		private double factor;
		public ChangeYScrollBar(JScrollPane scroll, double barFactor) {
			b = scroll;
			factor = barFactor;
		}
		public void run() {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JScrollBar yBar = b.getVerticalScrollBar();
			yBar.setValue((int) ((yBar.getMaximum()-yBar.getMinimum()-yBar.getBlockIncrement(1)) * factor));
		}
	}
	
	/**
	 * Returns the SplitPane created.
	 * @return
	 */
	public JSplitPane splitPane() {return splitPane;}
	
	public void graphSelectionChange(DisplayGraphEvent event) {
		histoPanel2.setGraph(event.getDisplayGraphSettings().getReadType().toString());
		histoPanel2.repaint();
	}
	
	private class Utils {

	    public final static String jpeg = "jpeg";
	    public final static String jpg = "jpg";
	    public final static String gif = "gif";
	    public final static String tiff = "tiff";
	    public final static String tif = "tif";
	    public final static String png = "png";

	    /*
	     * Get the extension of a file.
	     */  
	    public String getExtension(File f) {
	        String ext = null;
	        String s = f.getName();
	        int i = s.lastIndexOf('.');

	        if (i > 0 &&  i < s.length() - 1) {
	            ext = s.substring(i+1).toLowerCase();
	        }
	        return ext;
	    }
	}
	
	private class ImageFilter extends FileFilter {
		private HashMap<String, String> formatName;
		private String extension;
		
		public ImageFilter(String s) {
			extension = s;
			formatName = new HashMap<String, String>();
			formatName.put(Utils.png, "PNG (*.png)");
			formatName.put(Utils.jpg, "JPEG (*.jpg)");
			formatName.put(Utils.gif, "GIF (*.gif)");
		}
		
		public String getExtension() {return extension;}
		
	    //Accept all directories and all gif, jpg, tiff, or png files.
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }
	        Utils util = new Utils();
	        String extension = util.getExtension(f);
	        if (extension != null) {
	            if (extension.equals(extension)) {
	                    return true;
	            } else {
	                return false;
	            }
	        }
	 
	        return false;
	    }
	 
	    //The description of this filter
	    public String getDescription() {
	        return formatName.get(extension);
	    }
	}
}

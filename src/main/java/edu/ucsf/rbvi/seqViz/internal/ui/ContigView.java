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
import javax.swing.JFrame;
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
	private int width = 800, height = 400, widthScale = 1, heightScale = 1, begLine, endLine;
	
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
		final CyNetwork net = network;
		final Long suid2 = suid;
		CyTable table = network.getDefaultNetworkTable(), nodeTable = network.getDefaultNodeTable();
		final String contig = nodeTable.getRow(suid).get(CyNetwork.NAME, String.class);
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

		histoPanel2 = new HistoPanel(width, height, 1, contigLength, y_min, y_max);
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
		histoPanel2.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {
				histoPanel2.setDrawYLines(false);
				if (begLine < endLine) {
					Point d = new Point(begLine, 0);
					Point d2 = new Point(endLine, 0);
					try {
						Point2D p = histoPanel2.realCoordinates(d),
								p2 = histoPanel2.realCoordinates(d2);
						JPanel seqView = new SequenceView(net, suid2, (int) p.getX(), (int) p2.getX() + 50);
						JFrame frame = new JFrame(contig);
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						frame.getContentPane().add(seqView);
						frame.pack();
						frame.setVisible(true);
					} catch (NoninvertibleTransformException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			
			public void mousePressed(MouseEvent e) {
				histoPanel2.setBegLine(begLine = e.getX());
				histoPanel2.setEndLine(endLine = e.getX());
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
				histoPanel2.setEndLine(endLine = e.getX());
				Point2D p = histoPanel2.cartesianCoordinates(new Point(0, 0)),
						p2 = histoPanel2.cartesianCoordinates(new Point(100, 0));
				int diff = (int) (p2.getX() - p.getX());
				if (begLine + diff <= endLine)
					histoPanel2.setBegLine(begLine = (endLine - diff));
				histoPanel2.repaint();
			}
		});
	}

	public JSplitPane splitPane() {return splitPane;}
}

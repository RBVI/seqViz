package edu.ucsf.rbvi.seqViz.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

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
	private JPanel histoPanel, zoomPane;
	private ContigsManager manager;
	private Contig contig;
	
	public ContigView(ContigsManager manager, String contig) {
		this.manager = manager;
		this.contig = manager.getContig(contig);
		
		histoPane = new JScrollPane();
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

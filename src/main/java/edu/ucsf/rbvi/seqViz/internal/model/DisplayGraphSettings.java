package edu.ucsf.rbvi.seqViz.internal.model;

import java.util.HashMap;

import org.cytoscape.view.vizmap.VisualStyle;

/**
 * This class is an object which contains information about which graph is displayed.
 * Changing the variables in this object will select which graph is displayed on the node
 * and Contigs View.
 * @author Allan Wu
 *
 */
public class DisplayGraphSettings {
	// Set of graphs that is to be displayed
	public HashMap<String, VisualStyle> networkViewSetting;
	// Specific graph from networkViewSetting to display
	public String graphSelection;
	public DisplayGraphSettings() {
		networkViewSetting = null;
		graphSelection = null;
	}
}

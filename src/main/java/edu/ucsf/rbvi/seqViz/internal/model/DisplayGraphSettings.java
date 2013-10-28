package edu.ucsf.rbvi.seqViz.internal.model;

import java.util.HashMap;

import org.cytoscape.view.vizmap.VisualStyle;

public class DisplayGraphSettings {
	public HashMap<String, VisualStyle> networkViewSetting;
	public String graphSelection;
	public DisplayGraphSettings() {
		networkViewSetting = null;
		graphSelection = null;
	}
}

package edu.ucsf.rbvi.seqViz.internal.model;

import java.util.Map;

import org.cytoscape.view.vizmap.VisualStyle;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager.ReadType;
import edu.ucsf.rbvi.seqViz.internal.utils.StyleMaker.HistogramType;

/**
 * This class is an object which contains information about which graph is displayed.
 * Changing the variables in this object will select which graph is displayed on the node
 * and Contigs View.
 * @author Allan Wu
 *
 */
public class DisplayGraphSettings {
	// Specific graph from networkViewSetting to display
	private String graphSelection;
	private HistogramType hType;
	private ReadType rType;
	private final ContigsManager contigsManager;

	public DisplayGraphSettings(final ContigsManager contigsManager) {
		graphSelection = null;
		this.contigsManager = contigsManager;
	}

	public void setHistogramType(HistogramType hType) {
		this.hType = hType;
	}

	public void setReadType(ReadType rType) {
		this.rType = rType;
	}

	public ReadType getReadType() {return rType;}
	public HistogramType getHistogramType() {return hType;}
	public VisualStyle getVisualStyle() {
		return contigsManager.getVisualStyle(hType, rType);
	}
}

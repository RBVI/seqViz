package edu.ucsf.rbvi.seqViz.internal.utils;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager.ReadType;
import edu.ucsf.rbvi.seqViz.internal.model.EdgeStat;
import edu.ucsf.rbvi.seqViz.internal.model.Histograms;

public class StyleMaker {
	private final CyServiceRegistrar registrar;
	private final ContigsManager contigsManager;
	private final VisualMappingManager vmm;
	private VisualStyle seqVizStyle = null;
	private VisualLexicon lex = null;
	private final VisualMappingFunctionFactory discreteVMF;
	private final VisualMappingFunctionFactory continuousVMF;
	private final VisualMappingFunctionFactory passthroughVMF;

	private static double MIN_WIDTH = 10.0;
	private static double MAX_WIDTH = 1000.0;

	public enum HistogramType {
		NONE("No Histogram"),
		BRIDGING_READS("Bridging Reads Histogram"),
		COVERAGE("Coverage Histogram");

		private final String title;
		HistogramType(final String title) {
			this.title = title;
		}
		public String toString() {return title;}
	}

	public StyleMaker(final CyServiceRegistrar registrar, final ContigsManager contigsManager) {
		this.registrar = registrar;
		this.contigsManager = contigsManager;
		vmm = registrar.getService(VisualMappingManager.class);

		// See if we already have a seqviz style
		for (VisualStyle style: vmm.getAllVisualStyles()) {
			if (style.getTitle().equals("seqViz")) {
				seqVizStyle = style;
				break;
			}
		}

		// Factories for our mapping functions
		discreteVMF = 
					registrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		continuousVMF = 
					registrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		passthroughVMF = 
					registrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
	}

	public VisualStyle getStyle(HistogramType hType, ReadType rType) {
		if (seqVizStyle == null) {
			createInitialStyle();
		}
		switch(hType) {
			case NONE:
				clearCustomGraphics();
				break;
			case BRIDGING_READS:
				addCustomGraphics(Histograms.BARCHART_PAIRED_END, 
				                  Histograms.BARCHART_PAIRED_END_REV, rType);
				break;
			case COVERAGE:
				addCustomGraphics(Histograms.BARCHART_READ_COV, 
				                  Histograms.BARCHART_READ_COV_REV, rType);
				break;
		}
		return seqVizStyle;
	}

	public void setVisualStyle(VisualStyle vs, CyNetworkView myView) {
		vmm.setVisualStyle(vs, myView);
		vs.apply(myView);
	}


	private void createInitialStyle() {
		// Factory to create our style
		VisualStyleFactory styleFactory = registrar.getService(VisualStyleFactory.class);

		// Get our visual lexicon
		if (lex == null)
			lex = registrar.getService(RenderingEngineManager.class).getDefaultVisualLexicon();

		seqVizStyle = styleFactory.createVisualStyle("seqViz");

		/*
		 * Node style
		 */

		// Set the default node shape to rounded rectangle
		seqVizStyle.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);
		seqVizStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.WHITE);

		// Disable nodeSizeLocked
		for (VisualPropertyDependency<?> dep: seqVizStyle.getAllVisualPropertyDependencies()) {
			if (dep.getIdString().equals("nodeSizeLocked"))
				dep.setDependency(false);
		}

		// Set the label to a passthrough from name
		PassthroughMapping<String, String> labelMapping = (PassthroughMapping<String,String>) 
						passthroughVMF.createVisualMappingFunction(CyNetwork.NAME, String.class, 
						                                           BasicVisualLexicon.NODE_LABEL);
		seqVizStyle.addVisualMappingFunction(labelMapping);

		// Set the width to depend on the length of the contig
		ContinuousMapping<Long, Double> widthMapping = (ContinuousMapping<Long,Double>) 
						continuousVMF.createVisualMappingFunction(ContigsManager.LENGTH, Long.class, 
						                                           BasicVisualLexicon.NODE_WIDTH);
		// Set up a mapping from low to high
		long minLength = contigsManager.minLength();
		long maxLength = contigsManager.maxLength();

		// Check for silly values
		if (maxLength == 0) maxLength = 500000;
		if (minLength == Long.MAX_VALUE) minLength = 0;

		widthMapping.addPoint(minLength, 
										new BoundaryRangeValues<Double>(MIN_WIDTH,MIN_WIDTH,MIN_WIDTH));
		widthMapping.addPoint(maxLength, 
										new BoundaryRangeValues<Double>(MAX_WIDTH,MAX_WIDTH,MAX_WIDTH));
		seqVizStyle.addVisualMappingFunction(widthMapping);

		// Set the node label position to the bottom of the node
		VisualProperty prop = lex.lookup(CyNode.class, "NODE_LABEL_POSITION");
		Object value = prop.parseSerializableString("S,N,c,0.0,1.0"); // North of label on south of node
		seqVizStyle.setDefaultValue(prop, value);

		/*
		 * Edge style
		 */
		
		// Edge color
		ContinuousMapping<Double, Paint> edgeColorMapping = (ContinuousMapping<Double,Paint>) 
						continuousVMF.createVisualMappingFunction(EdgeStat.RELIABILITY, Double.class, 
						                                           BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		edgeColorMapping.addPoint(0.0, new BoundaryRangeValues<Paint>(Color.RED,Color.RED,Color.RED));
		edgeColorMapping.addPoint(1.0, new BoundaryRangeValues<Paint>(Color.BLUE,Color.BLUE,Color.BLUE));
		seqVizStyle.addVisualMappingFunction(edgeColorMapping);

		// Edge width passthrough
		PassthroughMapping<Double, Double> edgeWidthMapping = (PassthroughMapping<Double,Double>) 
						passthroughVMF.createVisualMappingFunction(EdgeStat.WEIGHT_LOG, Double.class, 
						                                           BasicVisualLexicon.EDGE_WIDTH);
		seqVizStyle.addVisualMappingFunction(edgeWidthMapping);

		// Target arrow shape
		DiscreteMapping<String, ArrowShape> targetArrowMapping = (DiscreteMapping<String,ArrowShape>) 
						discreteVMF.createVisualMappingFunction(EdgeStat.ORIENTATION, String.class, 
						                                           BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
		targetArrowMapping.putMapValue(EdgeStat.MINUSMINUS, ArrowShapeVisualProperty.T);
		targetArrowMapping.putMapValue(EdgeStat.MINUSPLUS, ArrowShapeVisualProperty.CIRCLE);
		targetArrowMapping.putMapValue(EdgeStat.PLUSMINUS, ArrowShapeVisualProperty.T);
		targetArrowMapping.putMapValue(EdgeStat.PLUSPLUS, ArrowShapeVisualProperty.CIRCLE);
		seqVizStyle.addVisualMappingFunction(targetArrowMapping);
	
		// Source arrow shape
		DiscreteMapping<String, ArrowShape> sourceArrowMapping = (DiscreteMapping<String,ArrowShape>) 
						discreteVMF.createVisualMappingFunction(EdgeStat.ORIENTATION, String.class, 
						                                           BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
		sourceArrowMapping.putMapValue(EdgeStat.MINUSMINUS, ArrowShapeVisualProperty.T);
		sourceArrowMapping.putMapValue(EdgeStat.MINUSPLUS, ArrowShapeVisualProperty.T);
		sourceArrowMapping.putMapValue(EdgeStat.PLUSMINUS, ArrowShapeVisualProperty.CIRCLE);
		sourceArrowMapping.putMapValue(EdgeStat.PLUSPLUS, ArrowShapeVisualProperty.CIRCLE);
		seqVizStyle.addVisualMappingFunction(sourceArrowMapping);

		vmm.addVisualStyle(seqVizStyle);
	}

	private void clearCustomGraphics() {
		VisualProperty<?> prop = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
		seqVizStyle.removeVisualMappingFunction(prop);
		VisualProperty<?> prop2 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_2");
		seqVizStyle.removeVisualMappingFunction(prop2);
	}

	private void addCustomGraphics(String histo, String histoRev, ReadType rType) {
		String suffix = "";
		if (rType != ReadType.NONE)
			suffix = ":"+rType.toString();

		// Get our visual lexicon
		if (lex == null)
			lex = registrar.getService(RenderingEngineManager.class).getDefaultVisualLexicon();

		{
			// Handle forward mapping
			VisualProperty<?> prop = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
			PassthroughMapping<String,?> mapping = (PassthroughMapping<String,?>) 
						passthroughVMF.createVisualMappingFunction(histo+suffix, String.class, prop);
			seqVizStyle.addVisualMappingFunction(mapping);
		}

		{
			// Reverse mapping
			VisualProperty<?> prop = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_2");
			PassthroughMapping<String,?> mapping = (PassthroughMapping<String,?>) 
						passthroughVMF.createVisualMappingFunction(histoRev+suffix, String.class, prop);
			seqVizStyle.addVisualMappingFunction(mapping);
		}
	}
}

package edu.ucsf.rbvi.seqViz.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsf.rbvi.seqViz.internal.events.DisplayGraphEvent;
import edu.ucsf.rbvi.seqViz.internal.events.DisplayGraphEventListener;
import edu.ucsf.rbvi.seqViz.internal.events.FireDisplayGraphEvent;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager.ReadType;
import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;
import edu.ucsf.rbvi.seqViz.internal.model.SeqVizSettings;
import edu.ucsf.rbvi.seqViz.internal.utils.StyleMaker.HistogramType;
import edu.ucsf.rbvi.seqViz.internal.tasks.ChangeDisplayGraphTask;
import edu.ucsf.rbvi.seqViz.internal.tasks.ChangeDisplayGraphTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.ChangeStyleTask;
import edu.ucsf.rbvi.seqViz.internal.tasks.ChangeStyleTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.MapReadsTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.OpenContigViewTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.SeqVizSettingsTask;
import edu.ucsf.rbvi.seqViz.internal.tasks.SeqVizSettingsTaskFactory;

// TODO: Allow opening and closing the molecular navigator dialog
// TODO: Consider headless mode
public class CyActivator extends AbstractCyActivator {
	private static Logger logger = LoggerFactory
			.getLogger(edu.ucsf.rbvi.seqViz.internal.CyActivator.class);

	// Sets of graphs that are displayed
	private DisplayGraphSettings graphSettings;

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		// See if we have a graphics console or not
		boolean haveGUI = true;
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());

		if (ref == null) {
			haveGUI = false;
		}
		
		// Get a handle on the CyServiceRegistrar
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);

		// Create the context object
		ContigsManager seqManager = new ContigsManager(registrar);
		SeqVizSettingsTask setParams = new SeqVizSettingsTask(seqManager);
		graphSettings = new DisplayGraphSettings(seqManager);

		// Currently visual styles are loaded from /resources/seqVizStyle.xml
		graphSettings.setHistogramType(HistogramType.NONE);
		graphSettings.setReadType(ReadType.NONE);
		for (HistogramType styleType: HistogramType.values()) {
			ChangeStyleTaskFactory changeStyle = 
				new ChangeStyleTaskFactory(graphSettings, styleType);
			Properties changeStyleProps = new Properties();
			changeStyleProps.setProperty(PREFERRED_MENU, "Apps.SeqViz.Show Histograms");
			changeStyleProps.setProperty(TITLE, styleType.toString());
			String command = "";
			for (String s: styleType.toString().split(" ")) command = command + s;
			changeStyleProps.setProperty(COMMAND, command);
			changeStyleProps.setProperty(COMMAND_NAMESPACE, "seqViz");
			changeStyleProps.setProperty(IN_MENU_BAR, "true");
			// changeStyleProps.setProperty(ENABLE_FOR, "network");
			// mapReadsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			changeStyleProps.setProperty(MENU_GRAVITY, "1.0");
			registerService(bc, changeStyle, NetworkViewTaskFactory.class, changeStyleProps);
		}
		
		// Change display graph settings
		DisplayGraphEvent event = new DisplayGraphEvent(graphSettings);
		event.setDisplayGraphSettings(graphSettings);
		FireDisplayGraphEvent graphEvent = new FireDisplayGraphEvent(event);
		final TaskManager<?,?> taskManager = getService(bc, TaskManager.class);
		final CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		graphEvent.addDisplayGraphEventListener(new DisplayGraphEventListener() {
			
			public void graphSelectionChange(DisplayGraphEvent event) {
				taskManager.execute(new TaskIterator(new ChangeStyleTask(applicationManager.getCurrentNetworkView(), graphSettings, graphSettings.getHistogramType())));
			}
		});

		for (ReadType type: ReadType.values()) {
			String title = type.toString();
			if (title == null||title.length() == 0) title = "all";
			ChangeDisplayGraphTaskFactory changeDisplay = 
					new ChangeDisplayGraphTaskFactory(graphEvent, type);
			Properties changeStyleProps = new Properties();
			changeStyleProps.setProperty(PREFERRED_MENU, "Apps.SeqViz.Change Graph");
			changeStyleProps.setProperty(TITLE, title);
			String command = "";
			for (String s: title.split("&")) command = command + s;
			changeStyleProps.setProperty(COMMAND, command);
			changeStyleProps.setProperty(COMMAND_NAMESPACE, "seqViz");
			changeStyleProps.setProperty(IN_MENU_BAR, "true");
			// changeStyleProps.setProperty(ENABLE_FOR, "network");
			// mapReadsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			changeStyleProps.setProperty(MENU_GRAVITY, "1.0");
			registerService(bc, changeDisplay, TaskFactory.class, changeStyleProps);
		}
		
		// Menu task factories
		SeqVizSettingsTaskFactory settingsTask = new SeqVizSettingsTaskFactory(
				seqManager);
		Properties settingsProps = new Properties();
		settingsProps.setProperty(PREFERRED_MENU, "Apps.SeqViz");
		settingsProps.setProperty(TITLE, "Settings...");
		settingsProps.setProperty(COMMAND, "set");
		settingsProps.setProperty(COMMAND_NAMESPACE, "seqViz");
		settingsProps.setProperty(IN_MENU_BAR, "true");
		// settingsProps.setProperty(ENABLE_FOR, "network");
		settingsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		settingsProps.setProperty(MENU_GRAVITY, "10.0");
		registerService(bc, settingsTask, TaskFactory.class, settingsProps);

		MapReadsTaskFactory mapReadsTask = new MapReadsTaskFactory(taskManager, seqManager);
		Properties mapReadsProps = new Properties();
		mapReadsProps.setProperty(PREFERRED_MENU, "Apps.SeqViz");
		mapReadsProps.setProperty(TITLE, "Map Reads");
		mapReadsProps.setProperty(COMMAND, "mapReads");
		mapReadsProps.setProperty(COMMAND_NAMESPACE, "seqViz");
		mapReadsProps.setProperty(IN_MENU_BAR, "true");
		// settingsProps.setProperty(ENABLE_FOR, "network");
		// mapReadsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		mapReadsProps.setProperty(MENU_GRAVITY, "8.0");
		registerService(bc, mapReadsTask, TaskFactory.class, mapReadsProps);
		
		if (haveGUI) {
			OpenContigViewTaskFactory contigViewTaskFactory = new OpenContigViewTaskFactory(seqManager, graphEvent);
			Properties contigViewProps = new Properties();
			contigViewProps.setProperty(PREFERRED_MENU, "Apps.SeqViz");
			contigViewProps.setProperty(TITLE, "Open Contig View");
			contigViewProps.setProperty(COMMAND, "openContigView");
			contigViewProps.setProperty(COMMAND_NAMESPACE, "seqViz");
			contigViewProps.setProperty(IN_MENU_BAR, "true");
			settingsProps.setProperty(ENABLE_FOR, "network");
			contigViewProps.setProperty(MENU_GRAVITY, "9.0");
			registerService(bc, contigViewTaskFactory, NodeViewTaskFactory.class, contigViewProps);
		}
	}

}

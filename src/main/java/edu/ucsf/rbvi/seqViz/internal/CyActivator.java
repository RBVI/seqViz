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
import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;
import edu.ucsf.rbvi.seqViz.internal.model.SeqVizSettings;
import edu.ucsf.rbvi.seqViz.internal.tasks.ChangeDisplayGraphTask;
import edu.ucsf.rbvi.seqViz.internal.tasks.ChangeDisplayGraphTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.ChangeStyleTask;
import edu.ucsf.rbvi.seqViz.internal.tasks.ChangeStyleTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.MapReadsTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.OpenContigViewTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.ReadFASTAContigsTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.SeqVizSettingsTask;
import edu.ucsf.rbvi.seqViz.internal.tasks.SeqVizSettingsTaskFactory;

// TODO: Allow opening and closing the molecular navigator dialog
// TODO: Consider headless mode
public class CyActivator extends AbstractCyActivator {
	private static Logger logger = LoggerFactory
			.getLogger(edu.ucsf.rbvi.seqViz.internal.CyActivator.class);

	private DisplayGraphSettings graphSettings;
	public static final String[] graphTypes = {null, "best", "best&unique", "unique"};
	
	public CyActivator() {
		super();
		graphSettings = new DisplayGraphSettings();
	}

	public void start(BundleContext bc) {

		// See if we have a graphics console or not
		boolean haveGUI = true;
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());

		if (ref == null) {
			haveGUI = false;
			// Issue error and return
		}
		
		// Create new network and network view
	/*	CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
		CyNetwork network = networkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, "");
		
		CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
		networkManager.addNetwork(network);
		
		CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);
		CyNetworkView myView = networkViewFactory.createNetworkView(network);
		CyNetworkViewManager networkViewManager = getService(bc, CyNetworkViewManager.class);
		networkViewManager.addNetworkView(myView); */
		
		// Load new Visual Style for seqViz
		HashMap<String, HashMap<String,VisualStyle>> styles = new HashMap<String, HashMap<String,VisualStyle>>();
		VisualMappingManager vmmServiceRef = getService(bc,VisualMappingManager.class);
		InputStream stream = CyActivator.class.getResourceAsStream("/seqVizStyle.xml");
		VisualStyle style = null;
		if (stream != null) {
				LoadVizmapFileTaskFactory loadVizmapFileTaskFactory =  getService(bc,LoadVizmapFileTaskFactory.class);
				Set<VisualStyle> vsSet = loadVizmapFileTaskFactory.loadStyles(stream);
				if (vsSet != null)
					for (VisualStyle vs: vsSet) {
						vmmServiceRef.addVisualStyle(vs);
						style = vs;
						String styleTitle, title, graph;
						vs.setTitle(styleTitle = vs.getTitle().split("_")[0]);
						String[] styleTitle2 = styleTitle.split(":");
						title = styleTitle2[0];
						if (styleTitle2.length == 2)
							graph = styleTitle2[1];
						else graph = null;
						HashMap<String, VisualStyle> thisStyle;
						if (styles.containsKey(title))
							thisStyle = styles.get(title);
						else {
							thisStyle = new HashMap<String, VisualStyle>();
							styles.put(title, thisStyle);
						}
						thisStyle.put(graph, vs);
					}
		}
		
		// Get current network
	/*	CyApplicationManager appManager = getService(bc, CyApplicationManager.class);
		CyNetwork curNetwork = appManager.getCurrentNetwork(); */
		
		// Create the context object
		ContigsManager seqManager = new ContigsManager(getService(bc,CyServiceRegistrar.class), style);

		// Get OS information and set seqManager
		String OS = System.getProperty("os.name").toLowerCase();
		SeqVizSettingsTask setParams = new SeqVizSettingsTask(seqManager);
		if (OS.indexOf("win") >= 0) {
			setParams.loadBridgingReads = false;
			setParams.mapDir = "";
			setParams.tempDir = "%TEMP%\\";
			setParams.mapper.setSelectedValue("bowtie");
			try {
				setParams.run(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 || OS.indexOf("sunos") >= 0 || OS.indexOf("mac") >= 0) {
			setParams.loadBridgingReads = false;
			setParams.mapDir = "";
			setParams.tempDir = "/tmp/";
			setParams.mapper.setSelectedValue("bowtie");
			try {
				setParams.run(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Get a handle on the CyServiceRegistrar
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);

		// Create and register our listeners
	
		// Load visual styles
		for (String styleName: styles.keySet()) {
			ChangeStyleTaskFactory changeStyle = new ChangeStyleTaskFactory(graphSettings, styles.get(styleName));
			Properties changeStyleProps = new Properties();
			changeStyleProps.setProperty(PREFERRED_MENU, "Apps.SeqViz.Show Histograms");
			changeStyleProps.setProperty(TITLE, styleName);
			String command = "";
			for (String s: styleName.split(" ")) command = command + s;
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
		final TaskManager taskManager = getService(bc, TaskManager.class);
		final CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		graphEvent.addDisplayGraphEventListener(new DisplayGraphEventListener() {
			
			public void graphSelectionChange(DisplayGraphEvent event) {
				taskManager.execute(new TaskIterator(new ChangeStyleTask(applicationManager.getCurrentNetworkView(), graphSettings, graphSettings.networkViewSetting)));
			}
		});
		String [] graphTypes = {null, "best", "best&unique", "unique"};
		for (String type: graphTypes) {
			String title;
			if (type == null) title = "all";
			else title = type;
			ChangeDisplayGraphTaskFactory changeDisplay = new ChangeDisplayGraphTaskFactory(graphEvent, type);
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

	/*	ReadFASTAContigsTaskFactory readFASTAContigsTask = new ReadFASTAContigsTaskFactory(seqManager);
		Properties readFASTAProps = new Properties();
		readFASTAProps.setProperty(PREFERRED_MENU, "Apps.SeqViz.Load contigs file");
		readFASTAProps.setProperty(TITLE, "FASTA");
		readFASTAProps.setProperty(COMMAND, "contigsFile");
		readFASTAProps.setProperty(COMMAND_NAMESPACE, "seqViz");
		readFASTAProps.setProperty(IN_MENU_BAR, "true");
		// settingsProps.setProperty(ENABLE_FOR, "network");
		// readFASTAProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		readFASTAProps.setProperty(MENU_GRAVITY, "9.0");
		registerService(bc, readFASTAContigsTask, TaskFactory.class, readFASTAProps); */
		
		MapReadsTaskFactory mapReadsTask = new MapReadsTaskFactory(seqManager);
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
		
		OpenContigViewTaskFactory contigViewTaskFactory = new OpenContigViewTaskFactory(seqManager, graphEvent);
		Properties contigViewProps = new Properties();
		contigViewProps.setProperty(PREFERRED_MENU, "Apps.SeqViz");
		contigViewProps.setProperty(TITLE, "Open Contig View");
		contigViewProps.setProperty(COMMAND, "openContigView");
		contigViewProps.setProperty(COMMAND_NAMESPACE, "seqViz");
		contigViewProps.setProperty(IN_MENU_BAR, "true");
		// settingsProps.setProperty(ENABLE_FOR, "network");
		// mapReadsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		contigViewProps.setProperty(MENU_GRAVITY, "9.0");
		registerService(bc, contigViewTaskFactory, NodeViewTaskFactory.class, contigViewProps);
	}

}

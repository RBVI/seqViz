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
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.tasks.MapReadsTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.ReadFASTAContigsTaskFactory;
import edu.ucsf.rbvi.seqViz.internal.tasks.SeqVizSettingsTaskFactory;

// TODO: Allow opening and closing the molecular navigator dialog
// TODO: Consider headless mode
public class CyActivator extends AbstractCyActivator {
	private static Logger logger = LoggerFactory
			.getLogger(edu.ucsf.rbvi.seqViz.internal.CyActivator.class);

	public CyActivator() {
		super();
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
					}
		}
		
		// Create the context object
		ContigsManager seqManager = new ContigsManager(bc, style);

		// Get a handle on the CyServiceRegistrar
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);

		// Create and register our listeners
	
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
	}

}

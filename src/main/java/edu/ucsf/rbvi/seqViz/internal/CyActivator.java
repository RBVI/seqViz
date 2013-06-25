package edu.ucsf.rbvi.seqViz.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
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

		// Create the context object
		ContigsManager seqManager = new ContigsManager();

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

		ReadFASTAContigsTaskFactory readFASTAContigsTask = new ReadFASTAContigsTaskFactory(seqManager);
		Properties readFASTAProps = new Properties();
		readFASTAProps.setProperty(PREFERRED_MENU, "Apps.SeqViz.Load contigs file");
		readFASTAProps.setProperty(TITLE, "FASTA");
		readFASTAProps.setProperty(COMMAND, "contigsFile");
		readFASTAProps.setProperty(COMMAND_NAMESPACE, "seqViz");
		readFASTAProps.setProperty(IN_MENU_BAR, "true");
		// settingsProps.setProperty(ENABLE_FOR, "network");
		// readFASTAProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		readFASTAProps.setProperty(MENU_GRAVITY, "11.0");
		registerService(bc, readFASTAContigsTask, TaskFactory.class, readFASTAProps);
		
		MapReadsTaskFactory mapReadsTask = new MapReadsTaskFactory(seqManager);
		Properties mapReadsProps = new Properties();
		mapReadsProps.setProperty(PREFERRED_MENU, "Apps.SeqViz");
		mapReadsProps.setProperty(TITLE, "Map Reads");
		mapReadsProps.setProperty(COMMAND, "mapReads");
		mapReadsProps.setProperty(COMMAND_NAMESPACE, "seqViz");
		mapReadsProps.setProperty(IN_MENU_BAR, "true");
		// settingsProps.setProperty(ENABLE_FOR, "network");
		// mapReadsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		mapReadsProps.setProperty(MENU_GRAVITY, "12.0");
		registerService(bc, mapReadsTask, TaskFactory.class, mapReadsProps);
	}

}

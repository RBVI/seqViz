package edu.ucsf.rbvi.seqViz.internal.tasks;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.ui.ContigView;

public class OpenContigViewTask extends AbstractNodeViewTask {
	
	private ContigsManager manager;
	private String contig;
	private CyNetwork cyNetwork;
	private long suid;
	
	public OpenContigViewTask(View<CyNode> nodeView, CyNetworkView netView) {
		super(nodeView, netView);
	}
	
	public OpenContigViewTask(View<CyNode> nodeView, CyNetworkView netView, ContigsManager manager) {
		super(nodeView, netView);
		contig = netView.getModel().getDefaultNodeTable().getRow(nodeView.getModel().getSUID()).get(CyNetwork.NAME, String.class);
		cyNetwork = netView.getModel();
		suid = nodeView.getModel().getSUID();
		this.manager = manager;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
	//	ContigView panel = new ContigView(manager, contig);
		ContigView panel = new ContigView(cyNetwork, suid);
		JFrame frame = new JFrame(contig);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(panel.splitPane());
		frame.pack();
		frame.setVisible(true);
	}

}

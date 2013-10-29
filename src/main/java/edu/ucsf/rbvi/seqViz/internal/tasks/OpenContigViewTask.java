package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.events.FireDisplayGraphEvent;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.ui.ContigView;

public class OpenContigViewTask extends AbstractNodeViewTask {
	
	private ContigsManager manager;
	private String contig;
	private CyNetworkView cyNetwork;
	private long suid;
	private FireDisplayGraphEvent event;
	
	public OpenContigViewTask(View<CyNode> nodeView, CyNetworkView netView) {
		super(nodeView, netView);
	}
	
	public OpenContigViewTask(View<CyNode> nodeView, CyNetworkView netView, ContigsManager manager, FireDisplayGraphEvent graphEvent) {
		super(nodeView, netView);
		contig = netView.getModel().getDefaultNodeTable().getRow(nodeView.getModel().getSUID()).get(CyNetwork.NAME, String.class);
		cyNetwork = netView;
		suid = nodeView.getModel().getSUID();
		this.manager = manager;
		this.event = graphEvent;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
	//	ContigView panel = new ContigView(manager, contig);
		final ContigView panel = new ContigView(cyNetwork, suid);
		event.addDisplayGraphEventListener(panel);
		event.fireGraphSelectionChange();
		JFrame frame = new JFrame(contig);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(panel.splitPane());
		frame.pack();
		frame.setVisible(true);
		frame.addWindowListener(new WindowListener() {
			
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void windowClosing(WindowEvent e) {
				event.removeDisplayGraphEventListener(panel);
			}
			
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}

}

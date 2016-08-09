package dk.kaspergsm.stormdeploy.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadata.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dk.kaspergsm.stormdeploy.Tools;
import dk.kaspergsm.stormdeploy.configurations.Storm;

public class Attach {
	private static Logger log = LoggerFactory.getLogger(Attach.class);

	/**
	 * Attaches to cluster
	 */
	@SuppressWarnings("unchecked")
	public static void attach(String clustername, String stormVersion, ComputeServiceContext computeContext) {	
		
		/**
		 * Parse current running nodes for cluster
		 */
		ArrayList<NodeMetadata> zkNodes = new ArrayList<NodeMetadata>();
		ArrayList<NodeMetadata> workerNodes = new ArrayList<NodeMetadata>();
		NodeMetadata nimbus = null;
		NodeMetadata ui = null;
		for (NodeMetadata n : (Set<NodeMetadata>) computeContext.getComputeService().listNodes()) {			
			if (n.getStatus() != Status.TERMINATED &&
					n.getGroup() != null &&
					n.getGroup().toLowerCase().equals(clustername.toLowerCase()) &&
					n.getUserMetadata().containsKey("daemons")) {
				String daemons = n.getUserMetadata().get("daemons").replace("[", "").replace("]", "");
				
				for (String daemon : daemons.split(",")) {
					if (daemon.trim().toLowerCase().equals("master"))
						nimbus = n;
					if (daemon.trim().toLowerCase().equals("worker"))
						workerNodes.add(n);
					if (daemon.trim().toLowerCase().equals("zk"))
						zkNodes.add(n);
					if (daemon.trim().toLowerCase().equals("ui"))
						ui = n;
				}
			}
		}
		
		/**
		 * Update attachment
		 */
		try {
			
			Storm.writeStormAttachConfigFiles(
					Tools.getInstancesIp(zkNodes), 
					Tools.getInstancesIp(workerNodes), 
					Tools.getInstanceIp(nimbus),
					(ui == null) ? "" : Tools.getInstanceIp(ui),
					clustername,
					stormVersion);
			log.info("Attached to cluster");
		} catch (IOException ex) {
			log.error("Problem attaching to cluster", ex);
		}
	}
}

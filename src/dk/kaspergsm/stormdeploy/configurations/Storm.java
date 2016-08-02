package dk.kaspergsm.stormdeploy.configurations;

import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dk.kaspergsm.stormdeploy.userprovided.Configuration;
import org.jclouds.scriptbuilder.domain.Statement;

import dk.kaspergsm.stormdeploy.Tools;


/**
 * Contains all methods to configure Storm on nodes
 * 
 * @author Kasper Grud Skat Madsen
 */
public class Storm {

	public static List<Statement> download(String stormRemoteLocation) {

        return Tools.download(System.getProperty("install.dir"), stormRemoteLocation, true, true, "storm");
	}
	
	/**
	 * Write storm/conf/storm.yaml (basic settings only)
	 */
	public static List<Statement> configure(String hostname, List<String> zkNodesHostname, List<String> drpcHostname, String userName) {
		List<Statement> st = new ArrayList<Statement>();
		String installDir = System.getProperty("install.dir");
		st.add(exec("cd " + installDir + "storm/conf/"));
		st.add(exec("touch storm.yaml"));
		
		// Add nimbus.host
		//FIXME: This differs between storm versions! Need to check here
		st.add(exec("echo nimbus.seeds: [\"" + hostname + "\"]git  >> storm.yaml"));
		
		// Add storm.zookeeper.servers
		st.add(exec("echo storm.zookeeper.servers: >> storm.yaml"));
		for (int i = 1; i <= zkNodesHostname.size(); i++)
			st.add(exec("echo - \"" + zkNodesHostname.get(i-1) + "\" >> storm.yaml"));

		// Add drpc.servers
		if (drpcHostname.size() > 0) {
			st.add(exec("echo drpc.servers: >> storm.yaml"));
			for (int i = 1; i <= drpcHostname.size(); i++)
				st.add(exec("echo - \"" + drpcHostname.get(i-1) + "\" >> storm.yaml"));
		}

		// Add supervisor metadata
		st.add(exec("echo supervisor.scheduler.meta: >> storm.yaml"));
		st.add(exec("instancetype=$(cat " + installDir + ".instance-type)"));
		st.add(exec("echo \"  instancetype: \\\"$instancetype\\\"\" >> storm.yaml"));
		
		// Change owner of storm directory
		st.add(exec("chown -R " + userName + ":" + userName + " " + installDir + "storm"));
		
		// Add storm to execution PATH
		st.add(exec("echo \"export PATH=\\\"" + installDir + "storm/bin:\\$PATH\\\"\" >> ~/.bashrc"));
                
		return st;
	}
	
	/**
	 * Uses Monitor to restart daemon, if it stops
	 */
	public static List<Statement> startNimbusDaemonSupervision(String username) {
		String installDir = System.getProperty("install.dir");
		List<Statement> st = new ArrayList<Statement>();
		st.add(goToInstallDir());
		st.add(exec("su -c 'case $(head -n 1 " + installDir + "daemons) in *MASTER*) java -cp \"" + installDir + "sda/storm-deploy-alternative.jar\" dk.kaspergsm.stormdeploy.image.ProcessMonitor org.apache.storm.daemon.nimbus " + installDir + "storm/bin/storm nimbus ;; esac &' - " + username));
		return st;
	}
	
	/**
	 * Uses Monitor to restart daemon, if it stops
	 */
	public static List<Statement> startSupervisorDaemonSupervision(String username) {
		String installDir = System.getProperty("install.dir");
		List<Statement> st = new ArrayList<Statement>();
		st.add(goToInstallDir());
		st.add(exec("su -c 'case $(head -n 1 " + installDir + "daemons) in *WORKER*) java -cp \"" + installDir + "sda/storm-deploy-alternative.jar\" dk.kaspergsm.stormdeploy.image.ProcessMonitor org.apache.storm.daemon.supervisor " + installDir + "storm/bin/storm supervisor ;; esac &' - " + username));
		return st;
	}
	
	/**
	 * Uses Monitor to restart daemon, if it stops
	 */
	public static List<Statement> startUIDaemonSupervision(String username) {
		String installDir = System.getProperty("install.dir");
		List<Statement> st = new ArrayList<Statement>();
		st.add(goToInstallDir());
		st.add(exec("su -c 'case $(head -n 1 " + installDir + "daemons) in *UI*) java -cp \"" + installDir + "sda/storm-deploy-alternative.jar\" dk.kaspergsm.stormdeploy.image.ProcessMonitor org.apache.storm.ui.core " + installDir + "storm/bin/storm ui ;; esac &' - " + username));
		return st;
	}
	
	/**
	 * Uses Monitor to restart daemon, if it stops
	 */
	public static List<Statement> startDRPCDaemonSupervision(String username) {
		String installDir = System.getProperty("install.dir");
		List<Statement> st = new ArrayList<Statement>();
		st.add(goToInstallDir());
		st.add(exec("su -c 'case $(head -n 1 " + installDir + "daemons) in *DRPC*) java -cp \"" + installDir + "sda/storm-deploy-alternative.jar\" dk.kaspergsm.stormdeploy.image.ProcessMonitor org.apache.storm.daemon.drpc " + installDir + "storm/bin/storm drpc ;; esac &' - " + username));
		return st;
	}
	
    /**
     * Uses Monitor to restart daemon, if it stops
     */
	public static List<Statement> startLogViewerDaemonSupervision(String username) {
		String installDir = System.getProperty("install.dir");
		List<Statement> st = new ArrayList<Statement>();
		st.add(goToInstallDir());
		st.add(exec("su -c 'case $(head -n 1 " + installDir + "daemons) in *LOGVIEWER*) java -cp \"" + installDir + "sda/storm-deploy-alternative.jar\" dk.kaspergsm.stormdeploy.image.ProcessMonitor org.apache.storm.daemon.logviewer " + installDir + "storm/bin/storm logviewer ;; esac &' - " + username));
		return st;
	}
	
	/**
	 * Used to write config files to $HOME/.storm/
	 * these are needed for the storm script to know where to submit topologies etc.
	 */
	public static void writeStormAttachConfigFiles(List<String> zookeeperNodesHostname, List<String> supervisorNodesHostname, String nimbusHost, String uiHost, String clustername) throws IOException {
		String userHome = Tools.getHomeDir();
		new File(userHome + ".storm").mkdirs();
		
		// Write $HOME/.storm/storm.yaml
		FileWriter stormYaml = new FileWriter(userHome + ".storm/storm.yaml", false);
		stormYaml.append("storm.zookeeper.servers:\n");
		for (String zookeeperNode : zookeeperNodesHostname) {
			stormYaml.append("    - \"");
			stormYaml.append(zookeeperNode);
			stormYaml.append("\"\n");
		}
		stormYaml.append("nimbus.host: \"");
		stormYaml.append(nimbusHost);
		stormYaml.append("\"\n");
		stormYaml.append("ui.host: \"");
		stormYaml.append(uiHost);
		stormYaml.append("\"\n");
		stormYaml.append("cluster: \"");
		stormYaml.append(clustername);
		stormYaml.append("\"\n");
		
		stormYaml.flush();
		stormYaml.close();
		
		// Write $HOME/.storm/supervisor.yaml
		FileWriter supervisorYaml = new FileWriter(userHome + ".storm/supervisor.yaml", false);
		supervisorYaml.append("storm.supervisor.servers:\n");
		for (String supervisorNode : supervisorNodesHostname) {
			supervisorYaml.append("    - \"");
			supervisorYaml.append(supervisorNode);
			supervisorYaml.append("\"\n");
		}
		supervisorYaml.flush();
		supervisorYaml.close();
	}

	private static Statement goToInstallDir(){
		return exec("cd " + System.getProperty("install.dir"));
	}
}

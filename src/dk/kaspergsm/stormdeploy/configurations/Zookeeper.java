package dk.kaspergsm.stormdeploy.configurations;

import static org.jclouds.scriptbuilder.domain.Statements.exec;
import java.util.ArrayList;
import java.util.List;

import dk.kaspergsm.stormdeploy.userprovided.ConfigurationFactory;
import org.jclouds.scriptbuilder.domain.Statement;
import dk.kaspergsm.stormdeploy.Tools;

/**
 * Contains all methods to configure Zookeeper on nodes
 * 
 * @author Kasper Grud Skat Madsen
 */
public class Zookeeper {

	private Zookeeper() {
	}

	public static List<Statement> download(String installDir, String zookeeperRemoteLocation) {
		return Tools.download(installDir, zookeeperRemoteLocation, true, true, "zookeeper");
	}

	public static List<Statement> configure(List<String> zkNodesHostnames, String username, String installDir, String zkDataDir, String zkSnapshotRetainCount,
											String zkPurgeInterval) {
		List<Statement> st = new ArrayList<Statement>();
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= zkNodesHostnames.size(); i++) {
			sb.append("server.");
			sb.append(i);
			sb.append("=");
			sb.append(zkNodesHostnames.get(i-1));
			sb.append(":2888:3888");
			if (i != zkNodesHostnames.size())
				sb.append("\\n"); // escaped newline
		}
		st.add(exec("cd "+ installDir +"zookeeper/conf/"));
		st.add(exec("[ ! -e zoo.cfg ] && cp zoo_sample.cfg zoo.cfg && echo -e \"# the zookeeper ensemble\nserver.x\" >> zoo.cfg"));
		if (zkSnapshotRetainCount != null && zkPurgeInterval != null) {
			st.add(exec("sed \"s|#autopurge.snapRetainCount=.*|autopurge.snapRetainCount="+ zkSnapshotRetainCount +"|\" -i \"zoo.cfg\""));	// set number of snapshots to retain
			st.add(exec("sed \"s|#autopurge.purgeInterval=.*|autopurge.purgeInterval="+ zkPurgeInterval +"|\" -i \"zoo.cfg\""));	// set zookeeper snapshot purge interval
		}
		st.add(exec("sed \"s|dataDir=.*|dataDir="+ zkDataDir +"|\" -i \"zoo.cfg\""));	// set dataDir to /tmp/zktmp
		st.add(exec("sed \"s/server.*/server.x/\" -i \"zoo.cfg\""));				// convert each serverline to server.x
		st.add(exec("sed '$!N; /^\\(.*\\)\\n\\1$/!P; D' -i \"zoo.cfg\""));			// delete duplicate lines => one server.x
		st.add(exec("sed \"s/server.x/" + sb.toString() + "/\" -i \"zoo.cfg\""));	// replace server.x with new lines
		st.add(exec("mkdir -p " + zkDataDir));												// ensure folders exist
		st.add(exec("chown " + username + " " + zkDataDir));
		return st;
	}
	
	public static List<Statement> writeZKMyIds(String username, Integer zkid) {
		List<Statement> st = new ArrayList<Statement>();
		st.add(exec("mkdir -p /tmp/zktmp"));												// ensure folders exist
		st.add(exec("chown " + username + " /tmp/zktmp"));
		st.add(exec("echo " + zkid + " > /tmp/zktmp/myid"));								// write myid
		return st;
	}
	
	/**
	 * Uses Monitor to restart daemon, if it stops
	 */
	public static List<Statement> startDaemonSupervision(String username) {
		List<Statement> st = new ArrayList<Statement>();
		String installDir = ConfigurationFactory.getConfig().getInstallDir();
		st.add(exec("cd " + installDir));
		st.add(exec("su -c 'case $(head -n 1 " + installDir + "daemons) in *ZK*) java -cp \""+ installDir +"sda/storm-deploy-alternative.jar\" dk.kaspergsm.stormdeploy.image.ProcessMonitor org.apache.zookeeper.server "+ installDir +"zookeeper/bin/zkServer.sh start ;; esac &' - " + username));
		return st;
	}
}

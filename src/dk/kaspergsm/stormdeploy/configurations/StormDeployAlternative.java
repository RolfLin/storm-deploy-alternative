package dk.kaspergsm.stormdeploy.configurations;

import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dk.kaspergsm.stormdeploy.userprovided.ConfigurationFactory;
import org.jclouds.scriptbuilder.domain.Statement;
import dk.kaspergsm.stormdeploy.Tools;

/**
 * Contains all methods to configure SnormDeployAlternative on remote node
 * 
 * @author Kasper Grud Skat Madsen
 */
public class StormDeployAlternative {

	public static List<Statement> download(String username, String installDir) {
		List<Statement> st = new ArrayList<Statement>();
		st.add(exec("mkdir -p "+ installDir));
		if (!"~/".equals(installDir)) {
			st.add(exec("chown " + username + " " + installDir));
		}
		st.addAll(Tools.download(installDir, "https://s3-eu-west-1.amazonaws.com/storm-deploy-alternative/sda.tar.gz", true, true));
		return st;
	}
	
	/**
	 * Run memoryMonitor.
	 * 	Requires tools.jar from active jvm is on path. Is automatically searched and found if it exists in /usr/lib/jvm
	 */
	public static List<Statement> runMemoryMonitor(String username) {
		List<Statement> st = new ArrayList<Statement>();
		st.add(exec("su -c 'java -cp \"" + ConfigurationFactory.getConfig().getInstallDir() + "sda/storm-deploy-alternative.jar:$( find `ls -d /usr/lib/jvm/* | sort -k1 -r` -name tools.jar | head -1 )\" dk.kaspergsm.stormdeploy.image.MemoryMonitor &' - " + username));
		return st;
	}
	
	public static List<Statement> writeConfigurationFiles(String localConfigurationFile, String localCredentialFile) {
		String installDir = ConfigurationFactory.getConfig().getInstallDir();
		List<Statement> st = new ArrayList<Statement>();
		st.add(exec("mkdir " + installDir + "sda/conf"));
		st.addAll(Tools.echoFile(localConfigurationFile, installDir + "sda/conf/configuration.yaml"));
		st.addAll(Tools.echoFile(localCredentialFile, installDir + "sda/conf/credential.yaml"));
		return st;
	}
	
	public static List<Statement> writeLocalSSHKeys(String sshKeyName) {
		List<Statement> st = new ArrayList<Statement>();
		st.add(exec("mkdir ~/.ssh/"));
		st.addAll(Tools.echoFile(Tools.getHomeDir() + ".ssh" + File.separator + sshKeyName, "~/.ssh/id_rsa"));
		st.addAll(Tools.echoFile(Tools.getHomeDir() + ".ssh" + File.separator + sshKeyName + ".pub", "~/.ssh/id_rsa.pub"));
		return st;
	}
}
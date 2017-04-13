package dk.kaspergsm.stormdeploy.configurations;

import dk.kaspergsm.stormdeploy.Tools;
import dk.kaspergsm.stormdeploy.userprovided.ConfigurationFactory;
import org.jclouds.scriptbuilder.domain.Statement;

import java.util.ArrayList;
import java.util.List;

import static org.jclouds.scriptbuilder.domain.Statements.exec;

/**
 * Created by acelzj on 11/4/17.
 */
public class Hdfs {

    public static List<Statement> download() {
        List<Statement> st = new ArrayList<Statement>();
        st.add(exec("cd " + ConfigurationFactory.getConfig().getInstallDir()));
        st.add(exec("wget http://www.eu.apache.org/dist/hadoop/common/hadoop-2.6.1/hadoop-2.6.1.tar.gz"));
        st.add(exec("tar -zxf hadoop-2.6.1.tar.gz"));
        return st;
    }

    public static List<Statement> configure(String hostname, String installDir) {
        List<Statement> st = new ArrayList<Statement>();
        st.add(exec("cd " + installDir + "hadoop-2.6.1/etc/hadoop"));

        //define JAVA_HOME directory
        st.add(exec("touch hadoop-env.sh"));
        st.add(exec("sed -i 's/${JAVA_HOME}/\\/usr\\/lib\\/jvm\\/jdk1.8.0_112/g' hadoop-env.sh"));


        st.add(exec("mkdir -p /home/root/hdfs/namenode"));
        st.add(exec("mkdir /home/root/datanode"));

        // echo core-site.xml
        st.add(exec("touch core-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<property\\>' core-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<name\\>hadoop.tmp.dir\\<\\/name\\>' core-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<value\\>\\/home\\/root\\/tmp\\<\\/value\\>' core-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<\\/property\\>' core-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<property\\>' core-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<name\\>fs.default.name\\<\\/name\\>' core-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<value\\>hdfs:\\/\\/" + hostname + ":54310\\<\\/value\\>' core-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<\\/property\\>' core-site.xml"));


        //echo hdfs-site.xml
        st.add(exec("touch hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<property\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<name\\>dfs.replication\\<\\/name\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<value\\>1<\\/value\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<\\/property\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<property\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<name\\>dfs.namenode.name.dir\\<\\/name\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<value\\>file:\\/home\\/root\\/hdfs\\/namenode\\<\\/value\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<\\/property\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<property\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<name\\>dfs.datanode.data.dir\\<\\/name\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<value\\>file:\\/home\\/root\\/datanode\\<\\/value\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<\\/property\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<property\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<name\\>dfs.permissions\\<\\/name\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<value\\>false<\\/value\\>' hdfs-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<\\/property\\>' hdfs-site.xml"));


        // echo mapred-site.xml
        st.add(exec("mv mapred-site.xml.template mapred-site.xml"));
        st.add(exec("touch mapred-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<property\\>' mapred-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<name\\>mapred.job.tracker\\<\\/name\\>' mapred-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<value\\>hdfs:\\/\\/" + hostname + ":54311\\<\\/value\\>' mapred-site.xml"));
        st.add(exec("sed -i '/<\\/configuration\\>/i \\<\\/property\\>' mapred-site.xml"));


        // Add hdfs to execution PATH
        st.add(exec("echo \"export PATH=\\\"" + installDir + "hadoop-2.6.1/bin:\\$PATH\\\"\" >> ~/.bashrc"));
        st.add(exec("echo \"export PATH=\\\"" + installDir + "hadoop-2.6.1/sbin:\\$PATH\\\"\" >> ~/.bashrc"));

        st.add(exec("sudo sed -i 's/#   StrictHostKeyChecking ask/StrictHostKeyChecking no/g' /etc/ssh/ssh_config"));

        //configure ssh
        st.add(exec("ssh-keygen -t rsa -N \"\" -f /root/.ssh/id_rsa"));
        st.add(exec("cat /root/.ssh/id_rsa.pub >> /root/.ssh/authorized_keys"));

        //format namenode
        st.add(exec("cd " + installDir + "hadoop-2.6.1/bin"));
        st.add(exec("./hadoop namenode -format"));


        //run hdfs
        st.add(exec("cd " + installDir + "hadoop-2.6.1/sbin"));
        st.add(exec("./start-dfs.sh"));


        //make working directory in hdfs
        st.add(exec("cd " + installDir + "hadoop-2.6.1/bin"));
        st.add(exec("./hdfs dfs -mkdir -p /home/lzj"));
        return st;
    }


}

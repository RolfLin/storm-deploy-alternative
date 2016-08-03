package dk.kaspergsm.stormdeploy.userprovided;

import dk.kaspergsm.stormdeploy.Tools;

import java.io.File;

public class ConfigurationFactory {

    private static Configuration configuration;

    public static Configuration initialize(String clusterName) {
        configuration = Configuration.fromYamlFile(new File(Tools.getWorkDir() + "conf" + File.separator + "configuration.yaml"), clusterName);
        if (!configuration.sanityCheck()) {
            System.exit(0);
        }

        return configuration;
    }

    public static Configuration getConfig() {
        return configuration;
    }
}

package com.elasticbox.jenkins.k8s.plugin.clouds.slaves;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.JNLPLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.OfflineCause;
import hudson.slaves.RetentionStrategy;
import org.jenkinsci.plugins.durabletask.executors.OnceRetentionStrategy;
import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import sun.tools.jconsole.Messages;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


public class JenkinsKubernetesSlave extends AbstractCloudSlave {

    private static final String DESCRIPTION = "Jenkins Kubernetes Slave";
    private static final String NAME_PREFIX = "jks-k8s-slave-";
    private static final String DEFAULT_REMOTE_FS = "/home/jenkins";
    private static final int EXECUTORS = 1;

    private static final Logger LOGGER = Logger.getLogger(JenkinsKubernetesSlave.class.getName());

    private static final long serialVersionUID = -8642936855413034232L;

    /**
     * The resource bundle reference
     */
    private final static ResourceBundleHolder HOLDER = ResourceBundleHolder.get(Messages.class);

    private final KubernetesCloud kubernetesCloud;

    @DataBoundConstructor
    public JenkinsKubernetesSlave(KubernetesCloud kubernetesCloud,
                                  Label label,
                                  RetentionStrategy<JenkinsKubernetesComputer> retentionStrategy) throws Descriptor.FormException, IOException {

        super(NAME_PREFIX + UUID.randomUUID(),
            DESCRIPTION,
            DEFAULT_REMOTE_FS,
            EXECUTORS,
            Mode.NORMAL,
            label == null ? null : label.toString(),
            new JNLPLauncher(),
            retentionStrategy,
            Collections.<NodeProperty<Node>>emptyList());

        this.kubernetesCloud = kubernetesCloud;


    }


    @Override
    public JenkinsKubernetesComputer createComputer() {
        return new JenkinsKubernetesComputer(this);
    }

    @Override
    protected void _terminate(TaskListener listener) throws IOException, InterruptedException {

        LOGGER.log(Level.INFO, "Terminating Kubernetes instance for slave {0}", name);

        if (toComputer() == null) {
            LOGGER.log(Level.SEVERE, "Computer for slave is null: {0}", name);
            return;
        }

        try {
            cloud.connect().pods().inNamespace(cloud.getNamespace()).withName(name).delete();
            LOGGER.log(Level.INFO, "Terminated Kubernetes instance for slave {0}", name);
            toComputer().disconnect(OfflineCause.create(new Localizable(HOLDER, "offline")));
            LOGGER.log(Level.INFO, "Disconnected computer {0}", name);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failure to terminate instance for slave " + name, e);
        }
    }

    @Override
    public String toString() {
        return String.format("JenkinsKubernetesSlave name: %n", name);
    }

    @Extension
    public static final class DescriptorImpl extends SlaveDescriptor {

        @Override
        public String getDisplayName() {
            return "Kubernetes Slave";
        };

        @Override
        public boolean isInstantiable() {
            return false;
        }

    }
}

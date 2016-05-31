package com.elasticbox.jenkins.k8s.plugin.builders;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.slaves.OfflineCause;
import jenkins.model.Jenkins;
import org.jvnet.localizer.Localizable;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class BuildListener extends RunListener<AbstractBuild> {
    private static final Logger LOGGER = Logger.getLogger(BuildListener.class.getName());

    @Override
    public void onCompleted(AbstractBuild build, TaskListener listener) {
//        try {
//            Node node = build.getBuiltOn();
//            if (node.getDisplayName().startsWith("jenkins-slave")) {
//                if (node instanceof SwarmSlave) {
//                    SwarmSlave slave = (SwarmSlave) node;
//                    slave.getComputer().disconnect(OfflineCause.SimpleOfflineCause)
//                }
//
//                LOGGER.info(build.toString() + " has completed. Marking node for termination - " + node);
//                node.toComputer().disconnect(null);
//                Jenkins.getInstance().removeNode(node);
//            }
//        } catch (RuntimeException ex) {
//            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}

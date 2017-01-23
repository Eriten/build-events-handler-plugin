package org.jenkinsci.plugins.buildEventsHandler;

import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by etam on 5/23/16.
 */
@Extension
public class BuildEventsListener extends RunListener<AbstractBuild<?, ?>> {
    private static final Logger logger = Logger.getLogger(BuildEventsListener.class.getName());

    @Override
    public void onStarted(AbstractBuild<?, ?> abstractBuild, TaskListener listener) {
        Map<String, String> envVars = BuildEventsHandler.getEnvVars(abstractBuild, listener);

        logger.fine(abstractBuild.getFullDisplayName() + " Running Groovy Script Before Build Starts");
        listener.getLogger().println(
                BuildEventsHandler.getLogString("Before Build Starts", "Running Groovy Script")
        );
        BuildEventsHandler.runGroovyScript(
                abstractBuild,
                listener,
                envVars.get("buildStartEventGroovyScript")
        );
    }

    @Override
    public void onCompleted(AbstractBuild<?, ?> abstractBuild, @Nonnull TaskListener listener) {
        Map<String, String> envVars = BuildEventsHandler.getEnvVars(abstractBuild, listener);

        logger.fine(abstractBuild.getFullDisplayName() + " Running Groovy Script AfterBuild Finishes");
        listener.getLogger().println(
                BuildEventsHandler.getLogString("After Build Finishes", "Running Groovy Script")
        );
        BuildEventsHandler.runGroovyScript(
                abstractBuild,
                listener,
                envVars.get("buildFinishEventGroovyScript")
        );
    }
}

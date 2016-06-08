package org.jenkinsci.plugins.buildEventsHandler;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by etam on 5/23/16.
 */
@Extension
public class BuildEventsListener extends RunListener<AbstractBuild<?, ?>> {
    @Override
    public void onStarted(AbstractBuild<?, ?> abstractBuild, TaskListener listener) {
        Map<String, String> envVars = AbstractBuildEventsWrapper.getEnvVars(abstractBuild, listener);
        listener.getLogger().println("Build Events Handler at Build Start: Running Groovy Script");
        AbstractBuildEventsWrapper.runGroovyScript(abstractBuild, listener, envVars.get("buildStartEventGroovyScript"));
    }

    @Override
    public void onCompleted(AbstractBuild<?, ?> abstractBuild, @Nonnull TaskListener listener) {
        Map<String, String> envVars = AbstractBuildEventsWrapper.getEnvVars(abstractBuild, listener);
        listener.getLogger().println("Build Events Handler at Build Finish: Running Groovy Script");
        AbstractBuildEventsWrapper.runGroovyScript(abstractBuild, listener, envVars.get("buildFinishEventGroovyScript"));
    }
}

package org.jenkinsci.plugins.buildEventsHandler;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildWrapper;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractBuildEventsWrapper extends BuildWrapper {
    private final String buildStartGroovyScript;
    private final String preScmGroovyScript;
    private final String buildStepsStartGroovyScript;
    private final String buildStepsFinishGroovyScript;
    private final String buildFinishGroovyScript;
    private static final Logger logger = Logger.getLogger(AbstractBuildEventsWrapper.class.getName());

    @Override
    public void makeBuildVariables(AbstractBuild build, Map<String, String> variables) {
        variables.put("buildStartEventGroovyScript", getBuildStartGroovyScript());
        variables.put("buildPreScmEventGroovyScript", getPreScmGroovyScript());
        variables.put("buildStepsStartEventGroovyScript", getBuildStepsStartGroovyScript());
        variables.put("buildStepsFinishEventGroovyScript", getBuildStepsFinishGroovyScript());
        variables.put("buildFinishEventGroovyScript", getBuildFinishGroovyScript());
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {

        // Event hook before build steps start: Run Groovy Script
        listener.getLogger().println(
                BuildEventsHandler.getLogString("Before Build Steps", "Running Groovy Script")
        );
        BuildEventsHandler.runGroovyScript(build, listener, getBuildStepsStartGroovyScript());

        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener)
                    throws IOException, InterruptedException {

                // Event hook after build steps finish: Run Groovy Script
                listener.getLogger().println(
                        BuildEventsHandler.getLogString("After Build Steps", "Running Groovy Script")
                );
                return BuildEventsHandler.runGroovyScript(build, listener, getBuildStepsFinishGroovyScript());
            }
        };
    }

    @Override
    public void preCheckout(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {

        // Event hook before SCM checkout: Run Groovy Script
        listener.getLogger().println(
                BuildEventsHandler.getLogString("Before SCM Checkout", "Running Groovy Script")
        );
        BuildEventsHandler.runGroovyScript(build, listener, getPreScmGroovyScript());
    }

    @DataBoundConstructor
    public AbstractBuildEventsWrapper(
            String buildStartGroovyScript,
            String preScmGroovyScript,
            String buildStepsStartGroovyScript,
            String buildStepsFinishGroovyScript,
            String buildFinishGroovyScript
            ) {
        this.buildStartGroovyScript = buildStartGroovyScript;
        this.preScmGroovyScript = preScmGroovyScript;
        this.buildStepsStartGroovyScript = buildStepsStartGroovyScript;
        this.buildStepsFinishGroovyScript = buildStepsFinishGroovyScript;
        this.buildFinishGroovyScript = buildFinishGroovyScript;
    }

    public String getBuildStartGroovyScript() {
        return buildStartGroovyScript == null ? "" : buildStartGroovyScript;
    }

    public String getPreScmGroovyScript() {
        return preScmGroovyScript == null ? "" : preScmGroovyScript;
    }

    public String getBuildStepsStartGroovyScript() {
        return buildStepsStartGroovyScript == null ? "" : buildStepsStartGroovyScript;
    }

    public String getBuildStepsFinishGroovyScript() {
        return buildStepsFinishGroovyScript == null ? "" : buildStepsFinishGroovyScript;
    }

    public String getBuildFinishGroovyScript() {
        return buildFinishGroovyScript == null ? "" : buildFinishGroovyScript;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<BuildWrapper> {

        @Override
        public String getDisplayName() {
            return "Set Build Environment Events";
        }
    }
}

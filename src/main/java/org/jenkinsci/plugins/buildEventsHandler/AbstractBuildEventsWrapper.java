package org.jenkinsci.plugins.buildEventsHandler;

import groovy.lang.GroovyShell;
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


    public static Map<String, String> getEnvVars(AbstractBuild<?, ?> build, TaskListener listener) {
        Map<String, String> envVars = new HashMap<String, String>();
        if (build != null) {
            envVars.putAll(build.getCharacteristicEnvVars());
            envVars.putAll(build.getBuildVariables());
            try {
                envVars.putAll(build.getEnvironment(listener));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Couldn't get Env Variables: ", e);
            }
        }
        return envVars;
    }

    public static void runGroovyScript(AbstractBuild build, TaskListener listener, String script) {
        Map<String, String> envVars = getEnvVars(build, listener);

        GroovyShell shell = new GroovyShell();
        shell.setVariable("out", listener.getLogger());

        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            shell.setVariable("$" + entry.getKey(), entry.getValue());
        }

        Object shellOutput = shell.evaluate(script);
        // Need to check shellOutput for boolean, number or null
    }

    @Override
    public void makeBuildVariables(AbstractBuild build, Map<String, String> variables) {
        variables.put("buildStartEventGroovyScript", getBuildStartGroovyScript());
        variables.put("buildPreScmEventGroovyScript", getPreScmGroovyScript());
        variables.put("buildStepsStartEventGroovyScript", getBuildStepsStartGroovyScript());
        variables.put("buildStepsFinishEventGroovyScript", getBuildStepsFinishGroovyScript());
        variables.put("buildFinishEventGroovyScript", getBuildFinishGroovyScript());
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        listener.getLogger().println("Build Events Handler at Build Steps Start: Running Groovy Script");
        runGroovyScript(build, listener, getBuildStepsStartGroovyScript());

        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                listener.getLogger().println("Build Events Handler at Build Steps Finish: Running Groovy Script");
                runGroovyScript(build, listener, getBuildStepsFinishGroovyScript());
                return true;
            }
        };
    }

    @Override
    public void preCheckout(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        listener.getLogger().println("Build Events Handler at Pre SCM Checkout: Running Groovy Script");
        runGroovyScript(build, listener, getPreScmGroovyScript());
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

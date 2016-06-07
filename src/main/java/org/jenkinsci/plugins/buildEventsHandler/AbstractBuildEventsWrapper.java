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
    private final String groovyString;
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

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        Map<String, String> envVars = getEnvVars(build, listener);
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            builder.append(entry.getKey() + ":" + entry.getValue());
            builder.append(" | ");
        }

        logger.severe("1. setUp: " + builder.toString());

        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                return true;
            }
        };
    }

    @Override
    public void preCheckout(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        Map<String, String> envVars = getEnvVars(build, listener);
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            builder.append(entry.getKey() + ":" + entry.getValue());
            builder.append(" | ");
        }

        logger.severe("2. preCheckout: " + builder.toString());
    }

    @DataBoundConstructor
    public AbstractBuildEventsWrapper(
            String groovyString
            ) {
        this.groovyString = groovyString;
    }

    public String getGroovyString() {
        return groovyString == null ? "" : groovyString;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<BuildWrapper> {

        @Override
        public String getDisplayName() {
            return "Set Build Environment Event";
        }
    }


}

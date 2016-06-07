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

    private static final Logger logger = Logger.getLogger(BuildEventsListener.class.getName());

    @Override
    public Environment setUpEnvironment(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException, Run.RunnerAbortedException {
        Map<String, String> envVars = getEnvVars(build, listener);
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            builder.append(entry.getKey() + ":" + entry.getValue());
            builder.append(" | ");
        }

        logger.severe("5. onStarted: " + builder.toString());

        return new Environment() {
            @Override
            public void buildEnvVars(Map<String, String> env) {
            }
        };
    }

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
    public void onStarted(AbstractBuild<?, ?> abstractBuild, TaskListener listener) {
        Map<String, String> envVars = getEnvVars(abstractBuild, listener);
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            builder.append(entry.getKey() + ":" + entry.getValue());
            builder.append(" | ");
        }

        logger.severe("3. onStarted: " + builder.toString());
    }

//    @Override
//    public void onDeleted(AbstractBuild<?, ?> abstractBuild) {
//        super.onDeleted(abstractBuild);
//    }

//    @Override
//    public void onFinalized(AbstractBuild<?, ?> abstractBuild) {
//        super.onFinalized(abstractBuild);
//    }

    @Override
    public void onCompleted(AbstractBuild<?, ?> abstractBuild, @Nonnull TaskListener listener) {
        Map<String, String> envVars = getEnvVars(abstractBuild, listener);
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            builder.append(entry.getKey() + ":" + entry.getValue());
            builder.append(" | ");
        }

        logger.severe("4. onCompleted: " + builder.toString());
    }
}

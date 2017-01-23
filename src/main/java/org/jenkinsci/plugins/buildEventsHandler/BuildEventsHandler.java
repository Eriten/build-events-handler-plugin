package org.jenkinsci.plugins.buildEventsHandler;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by etam on 1/22/17.
 */
class BuildEventsHandler {
    private static final Logger logger = Logger.getLogger(BuildEventsHandler.class.getName());

    /**
     * Get all the environment variables of the Jenkins build
     * @param build Jenkins Build
     * @param listener Build listener for logging to Console Log
     * @return Jenkins build environment variables
     */
    static Map<String, String> getEnvVars(AbstractBuild<?, ?> build, TaskListener listener) {
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

    static String getLogString(String buildStage, String message) {
        return "Build Events Handler @ " + buildStage + ": " + message;
    }

    /**
     * Given the Jenkins variables and groovy script, run the script on Jenkins master with
     * pre-imported libraries and helper code
     * @param build Jenkins Build
     * @param listener Build listener for logging to Console Log
     * @param script Groovy script in String
     * @return the return status of the groovy script
     */
    public static boolean runGroovyScript(AbstractBuild build, TaskListener listener, String script) {

        // Short circuit if the script is empty
        if (script.isEmpty()) {
            return true;
        }

        // Load External libraries to the GroovyShell runner
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addImports(Unirest.class.getName());
        importCustomizer.addImports(HttpResponse.class.getName());
        importCustomizer.addImports(JsonNode.class.getName());

        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        compilerConfig.addCompilationCustomizers(importCustomizer);

        // Load Groovy classes and Use Groovy Compiler from Jenkins Master
        @Nonnull ClassLoader cl = Jenkins.getInstance().getPluginManager().uberClassLoader;

        /*
         Add GroovyShell Variables:
         1. Set Groovy's System.out to the build's listener, so its output will redirect to
            the build's Console Log
         2. Add Jenkins Build Environment Variables
         */
        Map<Object, Object> binding = new HashMap<Object, Object>();
        binding.put("out", listener.getLogger());
        binding.putAll(getEnvVars(build, listener));

        GroovyShell shell = new GroovyShell(cl, new Binding(binding), compilerConfig);

        // Helper code for running Shell commands
        String shellRunner =    "System.setOut(out)\n" +
                                "System.setErr(out)\n" +
                                "static void runShCmd(String cmd) {\n" +
                                "    def stdout = new StringBuilder()\n" +
                                "    def stderr = new StringBuilder()\n" +
                                "    println(\"Running cmd: ${cmd}\")\n" +
                                "    def process = cmd.execute()\n" +
                                "    process.consumeProcessOutput(stdout, stderr)\n" +
                                "    process.waitForOrKill(300000)\n" +
                                "    println(\"stdout> ${stdout}\")\n" +
                                "    def exitCode = process.exitValue()\n" +
                                "    if(exitCode != 0) {\n" +
                                "        System.err.println(\"stderr> ${stderr}\")\n" +
                                "        throw new RuntimeException(\"${cmd} returned ${exitCode}\")\n" +
                                "    }\n" +
                                "}\n";

        // Run the Groovy Script and return the script's exit status
        Object shellOutput = shell.evaluate(shellRunner + script);
        return evaluateGroovyShellOutput(shellOutput);
    }

    /**
     * Evaluate the return status from GroovyShell to determine if the groovy script ran successfully
     * or not
     * NOTE: Borrowed code from Groovy Plugin -
     * https://github.com/jenkinsci/groovy-plugin/blob/master/src/main/java/hudson/plugins/groovy/SystemGroovy.java
     * @param shellOutput The output from GroovyShell output
     * @return the return status of the groovy script
     */
    private static boolean evaluateGroovyShellOutput(Object shellOutput) {
        if (shellOutput instanceof Boolean) {
            return (Boolean) shellOutput;
        } else {
            if (shellOutput != null) {
                logger.fine("Script returned: " + shellOutput);
            }

            if (shellOutput instanceof Number) {
                return ((Number) shellOutput).intValue() == 0;
            }
        }

        return true;
    }
}

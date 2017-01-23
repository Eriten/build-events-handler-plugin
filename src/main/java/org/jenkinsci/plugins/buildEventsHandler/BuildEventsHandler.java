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

    public static boolean runGroovyScript(AbstractBuild build, TaskListener listener, String script) {
        Map<String, String> envVars = getEnvVars(build, listener);

        // Load External libraries to
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addImports( Unirest.class.getName() );
        importCustomizer.addImports( HttpResponse.class.getName() );
        importCustomizer.addImports( JsonNode.class.getName() );
        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        compilerConfig.addCompilationCustomizers(importCustomizer);

        // Load Groovy classes and Use Groovy Compiler from Jenkins Master
        @Nonnull ClassLoader cl = Jenkins.getInstance().getPluginManager().uberClassLoader;

        Map<Object, Object> binding = new HashMap<Object, Object>();
        binding.put("out", listener.getLogger()); // Setting System.out to Jenkins Console Log
        GroovyShell shell = new GroovyShell(cl, new Binding(binding), compilerConfig);

        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            shell.setVariable("$" + entry.getKey(), entry.getValue());
        }

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

        Object shellOutput = shell.evaluate(shellRunner + script);

        if (shellOutput instanceof Boolean) {
            return (Boolean) shellOutput;
        } else {
            if (shellOutput != null) {
                listener.getLogger().println("Script returned: " + shellOutput);
            }

            if (shellOutput instanceof Number) {
                return ((Number) shellOutput).intValue() == 0;
            }
        }


        // No error indication - success
        return true;
    }

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

}

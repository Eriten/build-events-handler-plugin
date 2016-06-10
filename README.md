# build-events-handler-plugin
A Jenkins plugin to handle different events throughout a Jenkins build.

# Q&A
1. Is this released and ready for production?
No. This plugin is still in development and still need refinement and extensive testing

2. What are the features you're trying to achieve before rolling out?
    1. Ability to run groovy script (Only support Groovy and Java core libraries) at each build event
    2. An option for advanced support of Matrix Job (Running Groovy scripts in the Matrix build and
    configuration levels separately)
    3. An option for failing the build if the groovy script returns a non zero status
    4. Job DSL support
    5. Install Groovy on the node automatically if Groovy is not installed
    6. This plugin will only be supported for Jenkins v1.645+

3. What works at the moment?
    * Added an option to Build Wrapper that allows you to record groovy scripts at various build events
    where each textarea has line number, groovy syntax highlight, and bracket matching.
    * Ability to execute Groovy scripts at the following build events -
        * When a Build starts
        * Before SCM starts
        * Before Build Steps start
        * After Build Steps finish (Before Build Publisher Starts)
        * After a Build finishes

4. What are you working on?
    * [Bug] Allowing Shell commands to be executes from the embedded Groovy Shell easily
    * [Bug] Check the exit code from the groovy script. Fail the build if the exit code is non zero
    * [Feature] An option for advanced support of Matrix Job (Running Groovy scripts in the Matrix build and
      configuration levels separately)
        * By default, the groovy script in the build wrappers run in both Matrix Parent build and
        Matrix configuration runs. This task will require adding a new option that allow running
        the groovy scripts in different levels -
            * The common groovy scripts between Matrix build and Matrix configuration runs.
            * The groovy scripts only for Matrix build
            * The groovy scripts only for Matrix configurations
    * [Feature] An option for failing the build if the groovy script returns a non zero status
    * [Feature] Adding Job DSL support
    * [Story] Adding Unit Tests for
        * GroovyShell
    * [Story] Adding Feature Tests that use JenkinsRule and Mockito for
        * One Feature test per Feature as mentioned in Section 2 except Feature 6.
    * [Story] Adding Documentation
        * Adding code documentation to clearly state what each class does
        * Adding code documentation so this plugin acts as a good example for Jenkins Plugin Starters
        * Adding usage documentation to instruct user using this plugin with screenshots!


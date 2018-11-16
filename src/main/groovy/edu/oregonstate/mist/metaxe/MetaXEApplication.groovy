package edu.oregonstate.mist.metaxe

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.metaxe.health.MetaXEHealthCheck
import io.dropwizard.setup.Environment

/**
 * Main application class.
 */
class MetaXEApplication extends Application<MetaXEConfiguration> {

    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(MetaXEConfiguration configuration, Environment environment) {
        this.setup(configuration, environment)

        XEAppDAO xeAppDAO = new XEAppDAO(configuration.xeAppsFilePath)
        environment.jersey().register(new XEAppsResource(xeAppDAO, configuration.api.endpointUri))

        MetaXEHealthCheck check = new MetaXEHealthCheck(configuration.xeAppsFilePath)
        environment.healthChecks().register("MetaXEHealthCheck", check)
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    public static void main(String[] arguments) throws Exception {
        new MetaXEApplication().run(arguments)
    }
}

package edu.oregonstate.mist.metaxe

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.api.Configuration
import io.dropwizard.setup.Environment

/**
 * Main application class.
 */
class MetaXEApplication extends Application<Configuration> {

    private static final String XE_APPS_FILE_PATH = "scripts/get_xe_apps/xe_apps.json"

    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(Configuration configuration, Environment environment) {
        this.setup(configuration, environment)

        XEAppDAO xeAppDAO = new XEAppDAO(XE_APPS_FILE_PATH)
        environment.jersey().register(new XEAppsResource(xeAppDAO, configuration.api.endpointUri))
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

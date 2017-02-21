package edu.oregonstate.mist.api

import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle
import edu.oregonstate.mist.api.BuildInfoManager
import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.InfoResource
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.BasicAuthenticator
import edu.oregonstate.mist.api.PrettyPrintResponseFilter
import edu.oregonstate.mist.api.jsonapi.GenericExceptionMapper
import edu.oregonstate.mist.api.jsonapi.NotFoundExceptionMapper
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.auth.basic.BasicCredentialAuthFilter
import io.dropwizard.jersey.errors.LoggingExceptionMapper
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import javax.ws.rs.WebApplicationException

/**
 * Main application base class.
 */
class Application<T extends Configuration> extends io.dropwizard.Application<T> {
    /**
     * Initializes application bootstrap.
     *
     * @param bootstrap
     */
    @Override
    public void initialize(Bootstrap<T> bootstrap) {
        bootstrap.addBundle(new TemplateConfigBundle())
    }

    /**
     * Registers basic API stuff like the InfoResource, build info lifecycle
     * manager, Jersey exception mappers, pretty print filter,
     * and authentication handler
     *
     * @param configuration
     * @param environment
     */
    protected void setup(T configuration, Environment environment) {

        Resource.loadProperties()
        BuildInfoManager buildInfoManager = new BuildInfoManager()
        environment.lifecycle().manage(buildInfoManager)

        environment.jersey().register(new NotFoundExceptionMapper())
        environment.jersey().register(new GenericExceptionMapper())
        environment.jersey().register(new LoggingExceptionMapper<WebApplicationException>(){})
        environment.jersey().register(new PrettyPrintResponseFilter())
        environment.jersey().register(new InfoResource(buildInfoManager.getInfo()))

        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<AuthenticatedUser>()
                .setAuthenticator(new BasicAuthenticator(configuration.getCredentialsList()))
                .setRealm(this.class.simpleName)
                .buildAuthFilter()
        ))

        environment.jersey().register(new AuthValueFactoryProvider.Binder
                <AuthenticatedUser>(AuthenticatedUser.class))
    }

    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(T configuration, Environment environment) {
        this.setup(configuration, environment)
    }
}

package com.sitewhere.web.microservice;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.sitewhere.grpc.model.client.UserManagementApiChannel;
import com.sitewhere.grpc.model.client.UserManagementGrpcChannel;
import com.sitewhere.grpc.model.spi.ApiNotAvailableException;
import com.sitewhere.grpc.model.spi.client.IUserManagementApiChannel;
import com.sitewhere.microservice.GlobalMicroservice;
import com.sitewhere.microservice.MicroserviceEnvironment;
import com.sitewhere.server.lifecycle.CompositeLifecycleStep;
import com.sitewhere.server.lifecycle.InitializeComponentLifecycleStep;
import com.sitewhere.server.lifecycle.StartComponentLifecycleStep;
import com.sitewhere.server.lifecycle.StopComponentLifecycleStep;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.server.lifecycle.ICompositeLifecycleStep;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.web.spi.microservice.IWebRestMicroservice;

/**
 * Microservice that provides web/REST functionality.
 * 
 * @author Derek
 */
public class WebRestMicroservice extends GlobalMicroservice implements IWebRestMicroservice {

    /** Static logger instance */
    private static Logger LOGGER = LogManager.getLogger();

    /** Microservice name */
    private static final String NAME = "Web/REST";

    /** Web/REST configuration file name */
    private static final String WEB_REST_CONFIGURATION = "web-rest.xml";

    /** List of configuration paths required by microservice */
    private static final String[] CONFIGURATION_PATHS = { WEB_REST_CONFIGURATION };

    /** User management GRPC channel */
    private UserManagementGrpcChannel userManagementGrpcChannel;

    /** User management API channel */
    private IUserManagementApiChannel userManagementApiChannel;

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.spi.IMicroservice#getName()
     */
    @Override
    public String getName() {
	return NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.microservice.spi.IGlobalMicroservice#getConfigurationPaths(
     * )
     */
    @Override
    public String[] getConfigurationPaths() throws SiteWhereException {
	return CONFIGURATION_PATHS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.spi.IGlobalMicroservice#
     * initializeFromSpringContexts(org.springframework.context.
     * ApplicationContext, java.util.Map)
     */
    @Override
    public void initializeFromSpringContexts(ApplicationContext global, Map<String, ApplicationContext> contexts)
	    throws SiteWhereException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.Microservice#afterMicroserviceStarted()
     */
    @Override
    public void afterMicroserviceStarted() {
	try {
	    waitForApisAvailable();
	    getLogger().info("All required APIs detected as available.");
	} catch (ApiNotAvailableException e) {
	    getLogger().error("Required APIs not available for web/REST.", e);
	}
    }

    /**
     * Wait for required APIs to become available.
     * 
     * @throws ApiNotAvailableException
     */
    protected void waitForApisAvailable() throws ApiNotAvailableException {
	getUserManagementApiChannel().waitForApiAvailable();
	getLogger().info("User management API detected as available.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.microservice.spi.IGlobalMicroservice#microserviceInitialize
     * (com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void microserviceInitialize(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Create GRPC components.
	createGrpcComponents();

	// Composite step for initializing microservice.
	ICompositeLifecycleStep init = new CompositeLifecycleStep("Initialize " + getName());

	// Initialize discoverable lifecycle components.
	init.addStep(initializeDiscoverableBeans(getWebRestApplicationContext(), monitor));

	// Initialize user management GRPC channel.
	init.addStep(new InitializeComponentLifecycleStep(this, getUserManagementGrpcChannel(),
		"User management GRPC channel", "Unable to initialize user management GRPC channel", true));

	// Execute initialization steps.
	init.execute(monitor);
    }

    /**
     * Create components that interact via GRPC.
     */
    protected void createGrpcComponents() {
	this.userManagementGrpcChannel = new UserManagementGrpcChannel(MicroserviceEnvironment.HOST_USER_MANAGEMENT,
		getInstanceSettings().getGrpcPort());
	this.userManagementApiChannel = new UserManagementApiChannel(getUserManagementGrpcChannel());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.microservice.spi.IGlobalMicroservice#microserviceStart(com.
     * sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void microserviceStart(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Composite step for starting microservice.
	ICompositeLifecycleStep start = new CompositeLifecycleStep("Start " + getName());

	// Start discoverable lifecycle components.
	start.addStep(startDiscoverableBeans(getWebRestApplicationContext(), monitor));

	// Start user mangement GRPC channel.
	start.addStep(new StartComponentLifecycleStep(this, getUserManagementGrpcChannel(),
		"User management GRPC channel", "Unable to start user management GRPC channel.", true));

	// Execute startup steps.
	start.execute(monitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.microservice.spi.IGlobalMicroservice#microserviceStop(com.
     * sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void microserviceStop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Composite step for stopping microservice.
	ICompositeLifecycleStep stop = new CompositeLifecycleStep("Stop " + getName());

	// Stop user mangement GRPC channel.
	stop.addStep(
		new StopComponentLifecycleStep(this, getUserManagementGrpcChannel(), "User management GRPC channel"));

	// Stop discoverable lifecycle components.
	stop.addStep(stopDiscoverableBeans(getWebRestApplicationContext(), monitor));

	// Execute shutdown steps.
	stop.execute(monitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.web.spi.microservice.IWebRestMicroservice#
     * getUserManagementGrpcChannel()
     */
    @Override
    public UserManagementGrpcChannel getUserManagementGrpcChannel() {
	return userManagementGrpcChannel;
    }

    public void setUserManagementGrpcChannel(UserManagementGrpcChannel userManagementGrpcChannel) {
	this.userManagementGrpcChannel = userManagementGrpcChannel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.web.spi.microservice.IWebRestMicroservice#
     * getUserManagementApiChannel()
     */
    @Override
    public IUserManagementApiChannel getUserManagementApiChannel() {
	return userManagementApiChannel;
    }

    public void setUserManagementApiChannel(IUserManagementApiChannel userManagementApiChannel) {
	this.userManagementApiChannel = userManagementApiChannel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.server.lifecycle.ILifecycleComponent#getLogger()
     */
    @Override
    public Logger getLogger() {
	return LOGGER;
    }

    protected ApplicationContext getWebRestApplicationContext() {
	return getGlobalContexts().get(WEB_REST_CONFIGURATION);
    }
}
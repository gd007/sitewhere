/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.mongodb;

import com.sitewhere.server.lifecycle.TenantEngineLifecycleComponent;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.spi.server.lifecycle.LifecycleComponentType;
import com.sitewhere.spi.server.lifecycle.LifecycleStatus;

/**
 * Base class for components in a tenant engine that rely on MongoDB
 * connectivity.
 */
public abstract class MongoTenantComponent<T extends MongoDbClient> extends TenantEngineLifecycleComponent {

    /** Number of seconds to wait between liveness checks */
    private static final int MONGODB_LIVENESS_CHECK_IN_SECS = 5;

    /** Number of retries before writing warnings to log */
    private static final int WARN_AFTER_RETRIES = 20;

    public MongoTenantComponent() {
    }

    public MongoTenantComponent(LifecycleComponentType type) {
	super(type);
    }

    /*
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#start(com.sitewhere.spi.
     * server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Don't allow the component to start until MongoDB is available.
	waitForMongoAvailable();

	// Ensure that collection indexes exist.
	ensureIndexes();
    }

    /**
     * Waits for a connection to MongoDB to become available before continuing.
     * 
     * @throws SiteWhereException
     */
    protected void waitForMongoAvailable() throws SiteWhereException {
	int retries = 0;
	while (true) {
	    if (getMongoClient().getLifecycleStatus() == LifecycleStatus.Started) {
		getLogger().info("MongoDB detected as available.");
		return;
	    }

	    try {
		Thread.sleep(MONGODB_LIVENESS_CHECK_IN_SECS * 1000);
		retries++;
		if (retries > WARN_AFTER_RETRIES) {
		    getLogger().warn(String.format("MongoDB not available after %d retries.", retries));
		}
	    } catch (InterruptedException e) {
		getLogger().warn("Interrupted while waiting for MongoDB to become available.");
		throw new SiteWhereException(e);
	    }
	}
    }

    /**
     * Ensure that required collection indexes exist.
     * 
     * @throws SiteWhereException
     */
    public abstract void ensureIndexes() throws SiteWhereException;

    /**
     * Set the MongoDB client used to access the database.
     * 
     * @param client
     * @throws SiteWhereException
     */
    public abstract void setMongoClient(T client) throws SiteWhereException;

    /**
     * Get the MongoDB client used to access the database.
     * 
     * @return
     * @throws SiteWhereException
     */
    public abstract T getMongoClient() throws SiteWhereException;
}
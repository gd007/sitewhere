/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.microservice.hazelcast.server;

import java.util.Map;
import java.util.UUID;

import com.sitewhere.device.DeviceManagementDecorator;
import com.sitewhere.grpc.client.cache.CacheUtils;
import com.sitewhere.grpc.client.device.DeviceManagementCacheProviders;
import com.sitewhere.security.UserContextManager;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.cache.ICacheProvider;
import com.sitewhere.spi.device.DeviceAssignmentStatus;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.IDeviceSpecification;
import com.sitewhere.spi.device.ISite;
import com.sitewhere.spi.device.request.IDeviceAssignmentCreateRequest;
import com.sitewhere.spi.device.request.IDeviceCreateRequest;
import com.sitewhere.spi.device.request.IDeviceSpecificationCreateRequest;
import com.sitewhere.spi.device.request.ISiteCreateRequest;
import com.sitewhere.spi.microservice.IMicroservice;
import com.sitewhere.spi.tenant.ITenant;

/**
 * Wraps {@link IDeviceManagement} implementation with cache support.
 * 
 * @author Derek
 */
public class CacheAwareDeviceManagement extends DeviceManagementDecorator {

    /** Site cache */
    private ICacheProvider<String, ISite> siteCache;

    /** Device specification cache */
    private ICacheProvider<String, IDeviceSpecification> deviceSpecificationCache;

    /** Device cache */
    private ICacheProvider<String, IDevice> deviceCache;

    /** Device assignment cache */
    private ICacheProvider<String, IDeviceAssignment> deviceAssignmentCache;

    public CacheAwareDeviceManagement(IDeviceManagement delegate, IMicroservice microservice) {
	super(delegate);
	this.siteCache = new DeviceManagementCacheProviders.SiteCache(microservice, true);
	this.deviceSpecificationCache = new DeviceManagementCacheProviders.DeviceSpecificationCache(microservice, true);
	this.deviceCache = new DeviceManagementCacheProviders.DeviceCache(microservice, true);
	this.deviceAssignmentCache = new DeviceManagementCacheProviders.DeviceAssignmentCache(microservice, true);
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#createSite(com.sitewhere.spi.
     * device.request.ISiteCreateRequest)
     */
    @Override
    public ISite createSite(ISiteCreateRequest request) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	ISite result = super.createSite(request);
	getSiteCache().setCacheEntry(tenant, result.getToken(), result);
	CacheUtils.logCacheUpdated(result);
	return result;
    }

    /*
     * @see com.sitewhere.device.DeviceManagementDecorator#getSiteByToken(java.lang.
     * String)
     */
    @Override
    public ISite getSiteByToken(String token) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	ISite result = super.getSiteByToken(token);
	if ((result != null) && (getSiteCache().getCacheEntry(tenant, token) == null)) {
	    getSiteCache().setCacheEntry(tenant, result.getToken(), result);
	    CacheUtils.logCacheUpdated(result);
	}
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#updateSite(java.util.UUID,
     * com.sitewhere.spi.device.request.ISiteCreateRequest)
     */
    @Override
    public ISite updateSite(UUID id, ISiteCreateRequest request) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	ISite result = super.updateSite(id, request);
	getSiteCache().setCacheEntry(tenant, result.getToken(), result);
	CacheUtils.logCacheUpdated(result);
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#deleteSite(java.util.UUID,
     * boolean)
     */
    @Override
    public ISite deleteSite(UUID id, boolean force) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	ISite result = super.deleteSite(id, force);
	getSiteCache().removeCacheEntry(tenant, result.getToken());
	CacheUtils.logCacheRemoved(result.getToken());
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#createDevice(com.sitewhere.spi
     * .device.request.IDeviceCreateRequest)
     */
    @Override
    public IDevice createDevice(IDeviceCreateRequest device) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDevice result = super.createDevice(device);
	getDeviceCache().setCacheEntry(tenant, result.getHardwareId(), result);
	CacheUtils.logCacheUpdated(result);
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#getDeviceByHardwareId(java.
     * lang.String)
     */
    @Override
    public IDevice getDeviceByHardwareId(String hardwareId) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDevice result = super.getDeviceByHardwareId(hardwareId);
	if ((result != null) && (getDeviceCache().getCacheEntry(tenant, hardwareId) == null)) {
	    getDeviceCache().setCacheEntry(tenant, result.getHardwareId(), result);
	    CacheUtils.logCacheUpdated(result);
	}
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#updateDevice(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceCreateRequest)
     */
    @Override
    public IDevice updateDevice(UUID id, IDeviceCreateRequest request) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDevice result = super.updateDevice(id, request);
	getDeviceCache().setCacheEntry(tenant, result.getHardwareId(), result);
	CacheUtils.logCacheUpdated(result);
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#deleteDevice(java.util.UUID,
     * boolean)
     */
    @Override
    public IDevice deleteDevice(UUID id, boolean force) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDevice result = super.deleteDevice(id, force);
	getDeviceCache().removeCacheEntry(tenant, result.getHardwareId());
	CacheUtils.logCacheRemoved(result.getHardwareId());
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#createDeviceAssignment(com.
     * sitewhere.spi.device.request.IDeviceAssignmentCreateRequest)
     */
    @Override
    public IDeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceAssignment result = super.createDeviceAssignment(request);
	getDeviceAssignmentCache().setCacheEntry(tenant, result.getToken(), result);
	IDevice device = super.getDevice(result.getDeviceId());
	getDeviceCache().removeCacheEntry(tenant, device.getHardwareId());
	CacheUtils.logCacheUpdated(result);
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#getDeviceAssignmentByToken(
     * java.lang.String)
     */
    @Override
    public IDeviceAssignment getDeviceAssignmentByToken(String token) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceAssignment result = super.getDeviceAssignmentByToken(token);
	if ((result != null) && (getDeviceAssignmentCache().getCacheEntry(tenant, token) == null)) {
	    getDeviceAssignmentCache().setCacheEntry(tenant, result.getToken(), result);
	    CacheUtils.logCacheUpdated(result);
	}
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#updateDeviceAssignmentMetadata
     * (java.util.UUID, java.util.Map)
     */
    @Override
    public IDeviceAssignment updateDeviceAssignmentMetadata(UUID id, Map<String, String> metadata)
	    throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceAssignment result = super.updateDeviceAssignmentMetadata(id, metadata);
	getDeviceAssignmentCache().setCacheEntry(tenant, result.getToken(), result);
	CacheUtils.logCacheUpdated(result);
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#updateDeviceAssignmentStatus(
     * java.util.UUID, com.sitewhere.spi.device.DeviceAssignmentStatus)
     */
    @Override
    public IDeviceAssignment updateDeviceAssignmentStatus(UUID id, DeviceAssignmentStatus status)
	    throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceAssignment result = super.updateDeviceAssignmentStatus(id, status);
	getDeviceAssignmentCache().setCacheEntry(tenant, result.getToken(), result);
	CacheUtils.logCacheUpdated(result);
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#deleteDeviceAssignment(java.
     * util.UUID, boolean)
     */
    @Override
    public IDeviceAssignment deleteDeviceAssignment(UUID id, boolean force) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceAssignment result = super.deleteDeviceAssignment(id, force);
	getDeviceCache().removeCacheEntry(tenant, result.getToken());
	CacheUtils.logCacheRemoved(result.getToken());
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#createDeviceSpecification(com.
     * sitewhere.spi.device.request.IDeviceSpecificationCreateRequest)
     */
    @Override
    public IDeviceSpecification createDeviceSpecification(IDeviceSpecificationCreateRequest request)
	    throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceSpecification result = super.createDeviceSpecification(request);
	getDeviceSpecificationCache().setCacheEntry(tenant, result.getToken(), result);
	CacheUtils.logCacheUpdated(result);
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#getDeviceSpecificationByToken(
     * java.lang.String)
     */
    @Override
    public IDeviceSpecification getDeviceSpecificationByToken(String token) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceSpecification result = super.getDeviceSpecificationByToken(token);
	if ((result != null) && (getDeviceAssignmentCache().getCacheEntry(tenant, token) == null)) {
	    getDeviceSpecificationCache().setCacheEntry(tenant, result.getToken(), result);
	    CacheUtils.logCacheUpdated(result);
	}
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#updateDeviceSpecification(java
     * .util.UUID,
     * com.sitewhere.spi.device.request.IDeviceSpecificationCreateRequest)
     */
    @Override
    public IDeviceSpecification updateDeviceSpecification(UUID id, IDeviceSpecificationCreateRequest request)
	    throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceSpecification result = super.updateDeviceSpecification(id, request);
	getDeviceSpecificationCache().setCacheEntry(tenant, result.getToken(), result);
	CacheUtils.logCacheUpdated(result);
	return result;
    }

    /*
     * @see
     * com.sitewhere.device.DeviceManagementDecorator#deleteDeviceSpecification(java
     * .util.UUID, boolean)
     */
    @Override
    public IDeviceSpecification deleteDeviceSpecification(UUID id, boolean force) throws SiteWhereException {
	ITenant tenant = UserContextManager.getCurrentTenant(true);
	IDeviceSpecification result = super.deleteDeviceSpecification(id, force);
	getDeviceSpecificationCache().removeCacheEntry(tenant, result.getToken());
	CacheUtils.logCacheRemoved(result.getToken());
	return result;
    }

    public ICacheProvider<String, ISite> getSiteCache() {
	return siteCache;
    }

    public void setSiteCache(ICacheProvider<String, ISite> siteCache) {
	this.siteCache = siteCache;
    }

    protected ICacheProvider<String, IDeviceSpecification> getDeviceSpecificationCache() {
	return deviceSpecificationCache;
    }

    protected void setDeviceSpecificationCache(ICacheProvider<String, IDeviceSpecification> deviceSpecificationCache) {
	this.deviceSpecificationCache = deviceSpecificationCache;
    }

    protected ICacheProvider<String, IDevice> getDeviceCache() {
	return deviceCache;
    }

    protected void setDeviceCache(ICacheProvider<String, IDevice> deviceCache) {
	this.deviceCache = deviceCache;
    }

    protected ICacheProvider<String, IDeviceAssignment> getDeviceAssignmentCache() {
	return deviceAssignmentCache;
    }

    protected void setDeviceAssignmentCache(ICacheProvider<String, IDeviceAssignment> deviceAssignmentCache) {
	this.deviceAssignmentCache = deviceAssignmentCache;
    }
}
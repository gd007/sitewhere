/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.device.persistence.rdb;

import com.sitewhere.configuration.instance.rdb.RDBConfiguration;
import com.sitewhere.rdb.DbClient;

public class DeviceManagementRDBClient extends DbClient {
    /**
     * @param configuration
     */
    public DeviceManagementRDBClient(RDBConfiguration configuration) {
        super(configuration);
    }
}
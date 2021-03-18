/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AddResourceStatement;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceAddedEvent;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceValidator;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.proxy.backend.exception.ResourceCheckedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.AbstractBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.converter.AddResourcesStatementConverter;

import java.util.Map;

/**
 * Add resource backend handler.
 */
public final class AddResourceBackendHandler extends AbstractBackendHandler<AddResourceStatement> {
    
    private final DatabaseType databaseType;
    
    public AddResourceBackendHandler(final DatabaseType databaseType, final AddResourceStatement sqlStatement, final String schemaName) {
        super(sqlStatement, schemaName);
        this.databaseType = databaseType;
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final AddResourceStatement sqlStatement) {
        Map<String, DataSourceConfiguration> dataSources = DataSourceParameterConverter.getDataSourceConfigurationMap(
                DataSourceParameterConverter.getDataSourceParameterMapFromYamlConfiguration(AddResourcesStatementConverter.convert(databaseType, sqlStatement)));
        if (!DataSourceValidator.validate(dataSources)) {
            throw new ResourceCheckedException(dataSources.keySet());
        }
        post(schemaName, dataSources);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void post(final String schemaName, final Map<String, DataSourceConfiguration> dataSources) {
        ShardingSphereEventBus.getInstance().post(new DataSourceAddedEvent(schemaName, dataSources));
    }
}

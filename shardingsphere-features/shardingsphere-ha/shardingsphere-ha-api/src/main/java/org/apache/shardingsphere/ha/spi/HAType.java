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

package org.apache.shardingsphere.ha.spi;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.typed.TypedSPI;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * HA type.
 */
public interface HAType extends TypedSPI {
    
    /**
     * Check HA config.
     *
     * @param dataSourceMap data source map
     * @param schemaName schema name
     * @throws SQLException SQL Exception
     */
    void checkHAConfig(Map<String, DataSource> dataSourceMap, String schemaName) throws SQLException;
    
    /**
     * Update primary data source.
     *
     * @param originalDataSourceMap original data source map
     * @param schemaName schema name
     * @param config config
     * @param disabledDataSourceNames disabled data source names
     */
    void updatePrimaryDataSource(Map<String, DataSource> originalDataSourceMap, String schemaName, RuleConfiguration config, Collection<String> disabledDataSourceNames);
    
    /**
     * Update member state.
     *
     * @param originalDataSourceMap original data source map
     * @param schemaName schema name
     * @param config config
     * @param disabledDataSourceNames disabled data source names
     */
    void updateMemberState(Map<String, DataSource> originalDataSourceMap, String schemaName,
                           RuleConfiguration config, Collection<String> disabledDataSourceNames);
    
    /**
     * Start periodical update.
     *
     * @param originalDataSourceMap original data source map
     * @param schemaName schema name
     * @param config config
     * @param disabledDataSourceNames disabled data source names
     */
    void startPeriodicalUpdate(Map<String, DataSource> originalDataSourceMap, String schemaName, RuleConfiguration config, Collection<String> disabledDataSourceNames);
    
    /**
     * Stop periodical update.
     */
    void stopPeriodicalUpdate();
    
    /**
     * Get primary data source.
     *
     * @return primary data source
     */
    String getPrimaryDataSource();
    
}

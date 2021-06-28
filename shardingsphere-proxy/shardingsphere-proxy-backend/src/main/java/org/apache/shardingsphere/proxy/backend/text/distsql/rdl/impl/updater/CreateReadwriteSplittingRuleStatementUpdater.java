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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.updater;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.distsql.update.RDLUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateRuleNamesException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidLoadBalancersException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.yaml.converter.ReadwriteSplittingRuleStatementConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Create readwrite-splitting rule statement updater.
 */
public final class CreateReadwriteSplittingRuleStatementUpdater implements RDLUpdater<CreateReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
    }
    
    @Override
    public void checkSQLStatement(final String schemaName, final CreateReadwriteSplittingRuleStatement sqlStatement, 
                                  final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        checkDuplicateRuleNames(schemaName, sqlStatement, currentRuleConfig);
        checkToBeCreatedResources(schemaName, sqlStatement, resource);
        checkToBeCreatedLoadBalancers(sqlStatement);
    }
    
    private void checkDuplicateRuleNames(final String schemaName, final CreateReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        if (null != currentRuleConfig) {
            Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList());
            Collection<String> duplicateRuleNames = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toList());
            if (!duplicateRuleNames.isEmpty()) {
                throw new DuplicateRuleNamesException(schemaName, duplicateRuleNames);
            }
        }
    }
    
    private void checkToBeCreatedResources(final String schemaName, final CreateReadwriteSplittingRuleStatement sqlStatement, final ShardingSphereResource resource) {
        Collection<String> notExistResources = resource.getNotExistedResources(getToBeCreatedResources(sqlStatement));
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistResources);
        }
    }
    
    private Collection<String> getToBeCreatedResources(final CreateReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> result = new LinkedHashSet<>();
        sqlStatement.getRules().stream().filter(each -> Strings.isNullOrEmpty(each.getAutoAwareResource())).forEach(each -> {
            result.add(each.getWriteDataSource());
            result.addAll(each.getReadDataSources());
        });
        return result;
    }
    
    private void checkToBeCreatedLoadBalancers(final CreateReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> notExistedLoadBalancers = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ReplicaLoadBalanceAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!notExistedLoadBalancers.isEmpty()) {
            throw new InvalidLoadBalancersException(notExistedLoadBalancers);
        }
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final String schemaName, final CreateReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        Optional<ReadwriteSplittingRuleConfiguration> toBeCreatedRuleConfig = new YamlRuleConfigurationSwapperEngine()
                .swapToRuleConfigurations(Collections.singleton(ReadwriteSplittingRuleStatementConverter.convert(sqlStatement)))
                .stream().filter(each -> each instanceof ReadwriteSplittingRuleConfiguration).findAny().map(each -> (ReadwriteSplittingRuleConfiguration) each);
        Preconditions.checkState(toBeCreatedRuleConfig.isPresent());
        if (null == currentRuleConfig) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().add(toBeCreatedRuleConfig.get());
        } else {
            currentRuleConfig.getDataSources().addAll(toBeCreatedRuleConfig.get().getDataSources());
            currentRuleConfig.getLoadBalancers().putAll(toBeCreatedRuleConfig.get().getLoadBalancers());
        }
        return false;
    }
    
    @Override
    public String getType() {
        return CreateReadwriteSplittingRuleStatement.class.getCanonicalName();
    }
}

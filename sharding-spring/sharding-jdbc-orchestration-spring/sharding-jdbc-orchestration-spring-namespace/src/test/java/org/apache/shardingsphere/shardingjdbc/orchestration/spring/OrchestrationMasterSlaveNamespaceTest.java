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

package org.apache.shardingsphere.shardingjdbc.orchestration.spring;

import org.apache.shardingsphere.underlying.common.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.properties.PropertiesConstant;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.strategy.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.core.strategy.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.datasource.OrchestrationSpringMasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.util.EmbedTestingServer;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.util.FieldValueUtil;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import org.apache.shardingsphere.spi.masterslave.MasterSlaveLoadBalanceAlgorithm;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/masterSlaveOrchestration.xml")
public class OrchestrationMasterSlaveNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertMasterSlaveDataSourceType() {
        assertNotNull(applicationContext.getBean("defaultMasterSlaveDataSourceOrchestration", OrchestrationSpringMasterSlaveDataSource.class));
    }
    
    @Test
    public void assertDefaultMaserSlaveDataSource() {
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("defaultMasterSlaveDataSourceOrchestration");
        assertThat(masterSlaveRule.getMasterDataSourceName(), is("dbtbl_0_master"));
        assertTrue(masterSlaveRule.getSlaveDataSourceNames().contains("dbtbl_0_slave_0"));
        assertTrue(masterSlaveRule.getSlaveDataSourceNames().contains("dbtbl_0_slave_1"));
    }
    
    @Test
    public void assertTypeMasterSlaveDataSource() {
        MasterSlaveRule randomSlaveRule = getMasterSlaveRule("randomMasterSlaveDataSourceOrchestration");
        MasterSlaveRule roundRobinSlaveRule = getMasterSlaveRule("roundRobinMasterSlaveDataSourceOrchestration");
        assertTrue(randomSlaveRule.getLoadBalanceAlgorithm() instanceof RandomMasterSlaveLoadBalanceAlgorithm);
        assertTrue(roundRobinSlaveRule.getLoadBalanceAlgorithm() instanceof RoundRobinMasterSlaveLoadBalanceAlgorithm);
    }
    
    @Test
    @Ignore
    // TODO TODO load balance algorithm have been construct twice for SpringMasterDatasource extends MasterSlaveDatasource.
    public void assertRefMasterSlaveDataSource() {
        MasterSlaveLoadBalanceAlgorithm randomLoadBalanceAlgorithm = applicationContext.getBean("randomLoadBalanceAlgorithm", MasterSlaveLoadBalanceAlgorithm.class);
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("refMasterSlaveDataSourceOrchestration");
        assertThat(masterSlaveRule.getLoadBalanceAlgorithm(), is(randomLoadBalanceAlgorithm));
    }
    
    private MasterSlaveRule getMasterSlaveRule(final String masterSlaveDataSourceName) {
        OrchestrationMasterSlaveDataSource masterSlaveDataSource = applicationContext.getBean(masterSlaveDataSourceName, OrchestrationMasterSlaveDataSource.class);
        MasterSlaveDataSource dataSource = (MasterSlaveDataSource) FieldValueUtil.getFieldValue(masterSlaveDataSource, "dataSource", true);
        return dataSource.getRuntimeContext().getRule();
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getProperties("defaultMasterSlaveDataSourceOrchestration").getValue(PropertiesConstant.SQL_SHOW);
        assertTrue(showSQL);
    }
    
    private ShardingSphereProperties getProperties(final String masterSlaveDataSourceName) {
        OrchestrationSpringMasterSlaveDataSource masterSlaveDataSource = applicationContext.getBean(masterSlaveDataSourceName, OrchestrationSpringMasterSlaveDataSource.class);
        MasterSlaveDataSource dataSource = (MasterSlaveDataSource) FieldValueUtil.getFieldValue(masterSlaveDataSource, "dataSource", true);
        return dataSource.getRuntimeContext().getProperties();
    }
}

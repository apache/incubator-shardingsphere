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
 *
 */

package org.apache.shardingsphere.agent.bootstrap;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.core.LoggingListener;
import org.apache.shardingsphere.agent.core.ShardingSphereTransformer;
import org.apache.shardingsphere.agent.core.config.AgentConfigurationLoader;
import org.apache.shardingsphere.agent.core.plugin.AgentPluginLoader;
import org.apache.shardingsphere.agent.core.utils.SingletonHolder;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

/**
 * ShardingSphere agent.
 */
public class ShardingSphereAgent {
    
    /**
     * Premain for instrumentation.
     *
     * @param agentArgs agent args
     * @param instrumentation instrumentation
     * @throws IOException IO exception
     */
    public static void premain(final String agentArgs, final Instrumentation instrumentation) throws IOException {
        SingletonHolder.INSTANCE.put(AgentConfigurationLoader.load());
        AgentPluginLoader agentPluginLoader = createAgentPluginLoader();
        setUpAgentBuilder(instrumentation, agentPluginLoader);
        Runtime.getRuntime().addShutdownHook(new Thread(agentPluginLoader::shutdownAllServices));
    }
    
    private static AgentPluginLoader createAgentPluginLoader() throws IOException {
        AgentPluginLoader result = AgentPluginLoader.getInstance();
        result.loadAllPlugins();
        result.initialAllServices();
        result.startAllServices();
        return result;
    }
    
    private static void setUpAgentBuilder(final Instrumentation instrumentation, final AgentPluginLoader agentPluginLoader) {
        AgentBuilder agentBuilder = new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED))
                .ignore(ElementMatchers.isSynthetic()).or(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent."));
        agentBuilder.type(agentPluginLoader.typeMatcher())
                .transform(new ShardingSphereTransformer(agentPluginLoader)).with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION).with(new LoggingListener()).installOn(instrumentation);
    }
}

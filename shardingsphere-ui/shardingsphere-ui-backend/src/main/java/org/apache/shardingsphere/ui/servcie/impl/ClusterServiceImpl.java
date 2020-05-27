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

package org.apache.shardingsphere.ui.servcie.impl;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.cluster.state.InstanceState;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.ui.servcie.ClusterService;
import org.apache.shardingsphere.ui.servcie.RegistryCenterService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of cluster service.
 */
@Service
@Slf4j
public final class ClusterServiceImpl implements ClusterService {
    
    @Resource
    private RegistryCenterService registryCenterService;
    
    @Override
    public Map<String, InstanceState> loadAllInstanceStates() {
        List<String> instanceIds = registryCenterService.getActivatedRegistryCenter()
                .getChildrenKeys(registryCenterService.getActivatedStateNode().getInstanceNodeRootPath());
        Map<String, InstanceState> instanceStateMap = new HashMap<>();
        try {
            instanceIds.forEach(each -> {
                instanceStateMap.put(each, loadInstanceState(each));
            });
        } catch (Exception ex) {
            log.error("Load all instance states error", ex);
        }
        return instanceStateMap;
    }
    
    private InstanceState loadInstanceState(final String instanceId) {
        String instanceStateData = registryCenterService.getActivatedRegistryCenter()
                .get(registryCenterService.getActivatedStateNode().getInstancesNodeFullPath(instanceId));
        Preconditions.checkNotNull(instanceStateData, "can not load instance '%s' state data", instanceId);
        return YamlEngine.unmarshal(instanceStateData, InstanceState.class);
    }
}

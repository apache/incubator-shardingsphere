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

package org.apache.shardingsphere.proxy.backend.cluster;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.cluster.configuration.config.HeartBeatConfiguration;
import org.apache.shardingsphere.cluster.facade.ClusterFacade;
import org.apache.shardingsphere.cluster.heartbeat.event.HeartBeatDetectNoticeEvent;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartBeatResponse;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartBeatResult;
import org.apache.shardingsphere.proxy.backend.schema.ShardingSphereSchema;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Objects;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Heart beat handler.
 */
public final class HeartBeatHandler {
    
    private HeartBeatConfiguration configuration;
    
    /**
     * Init heart beat handler.
     *
     * @param configuration heart beat configuration
     */
    public void init(final HeartBeatConfiguration configuration) {
        Preconditions.checkNotNull(configuration, "heart beat configuration can not be null.");
        this.configuration = configuration;
    }
    
    /**
     * Get heart beat handler instance.
     *
     * @return heart beat handler instance
     */
    public static HeartBeatHandler getInstance() {
        return HeartBeatHandlerHolder.INSTANCE;
    }
    
    public void handle(final Map<String, ShardingSphereSchema> schemas) {
        ExecutorService executorService = Executors.newFixedThreadPool(countDataSource(schemas));
        List<FutureTask<Map<String, HeartBeatResult>>> futureTasks = new ArrayList<>();
        schemas.values().forEach(value -> value.getBackendDataSource().getDataSources().entrySet().forEach(entry -> {
            FutureTask<Map<String, HeartBeatResult>> futureTask = new FutureTask<>(new HeartBeatDetect(value.getName(), entry.getKey(),
                    entry.getValue(), configuration));
            futureTasks.add(futureTask);
            executorService.submit(futureTask);
        }));
        reportHeartBeat(futureTasks);
        executorService.shutdown();
    }
    
    private Integer countDataSource(final Map<String, ShardingSphereSchema> schemas) {
        return Long.valueOf(schemas.values().stream().
                collect(Collectors.summarizingInt(entry -> entry.getBackendDataSource().
                        getDataSources().keySet().size())).getSum()).intValue();
    }
    
    private void reportHeartBeat(final List<FutureTask<Map<String, HeartBeatResult>>> futureTasks) {
        Map<String, Collection<HeartBeatResult>> heartBeatResultMap = new HashMap<>();
        futureTasks.stream().forEach(each -> {
            try {
                each.get().entrySet().forEach(entry -> {
                    if (Objects.isNull(heartBeatResultMap.get(entry.getKey()))) {
                        heartBeatResultMap.put(entry.getKey(), new ArrayList<>(Arrays.asList(entry.getValue())));
                    } else {
                        heartBeatResultMap.get(entry.getKey()).add(entry.getValue());
                    }
                });
            } catch (InterruptedException ex) {
            
            } catch (ExecutionException ex) {
            
            }
        });
        ClusterFacade.getInstance().reportHeartBeat(new HeartBeatResponse(heartBeatResultMap));
    }
    
    private static final class HeartBeatHandlerHolder {
        
        public static final HeartBeatHandler INSTANCE = new HeartBeatHandler();
    }
    
}

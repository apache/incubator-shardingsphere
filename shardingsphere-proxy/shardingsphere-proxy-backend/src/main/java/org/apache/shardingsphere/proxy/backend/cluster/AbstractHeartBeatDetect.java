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

import org.apache.shardingsphere.cluster.heartbeat.response.HeartBeatResult;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Abstract heart beat detect.
 */
public abstract class AbstractHeartBeatDetect implements Callable<Map<String, HeartBeatResult>> {
    
    private Boolean retryEnable;
    
    private Integer retryMaximum;
    
    private Integer retryInterval;
    
    public AbstractHeartBeatDetect(final Boolean retryEnable, final Integer retryMaximum, final Integer retryInterval) {
        this.retryEnable = retryEnable;
        this.retryMaximum = retryMaximum;
        this.retryInterval = retryInterval;
    }
    
    /**
     * Detect heart beat.
     *
     * @return heart beat result.
     */
    protected abstract Boolean detect();
    
    /**
     * Build heart beat result.
     *
     * @param result heart beat result
     * @return heart beat result
     */
    protected abstract Map<String, HeartBeatResult> buildResult(final Boolean result);
    
    @Override
    public Map<String, HeartBeatResult> call() {
        if (retryEnable && retryMaximum > 0) {
            Boolean result = Boolean.FALSE;
            for (int i = 0; i < retryMaximum; i++) {
                result = detect();
                if (result) {
                    break;
                }
                try {
                    Thread.sleep(retryInterval * 1000);
                } catch (InterruptedException ex) {
                
                }
            }
            return buildResult(result);
        } else {
            return buildResult(detect());
        }
    }
}

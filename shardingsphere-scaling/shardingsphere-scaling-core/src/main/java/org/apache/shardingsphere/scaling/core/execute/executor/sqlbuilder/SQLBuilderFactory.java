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

package org.apache.shardingsphere.scaling.core.execute.executor.sqlbuilder;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;

import java.util.Map;

/**
 * SQL builder factory.
 */
public final class SQLBuilderFactory {
    
    /**
     * New instance of SQL builder.
     *
     * @param databaseType database type
     * @return SQL builder
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static SQLBuilder newInstance(final String databaseType) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getSQLBuilderClass().getConstructor(Map.class).newInstance(Maps.newHashMap());
    }
}

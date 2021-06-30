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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Hold SQL statement schema for current thread.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementSchemaHolder {
    
    private static final ThreadLocal<String> SQL_STATEMENT_SCHEMA = new ThreadLocal<>();
    
    /**
     * Set SQL statement schema.
     *
     * @param schema SQL statement schema
     */
    public static void set(final String schema) {
        SQL_STATEMENT_SCHEMA.set(schema);
    }
    
    /**
     * Get SQL statement schema.
     *
     * @return SQL statement schema
     */
    public static String get() {
        return SQL_STATEMENT_SCHEMA.get();
    }
    
    /**
     * Remove SQL statement schema.
     */
    public static void remove() {
        SQL_STATEMENT_SCHEMA.remove();
    }
}

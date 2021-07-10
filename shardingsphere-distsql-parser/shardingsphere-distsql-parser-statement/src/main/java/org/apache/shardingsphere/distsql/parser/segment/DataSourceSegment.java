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

package org.apache.shardingsphere.distsql.parser.segment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;

import java.util.Properties;

/**
 * Data source segment.
 */
@RequiredArgsConstructor
@Getter
public final class DataSourceSegment implements ASTNode {
    
    private final String name;
    
    private final String url;
    
    private final String hostName;
    
    private final String port;
    
    private final String db;
    
    private final String user;
    
    private final String password;
    
    private final Properties properties;
}

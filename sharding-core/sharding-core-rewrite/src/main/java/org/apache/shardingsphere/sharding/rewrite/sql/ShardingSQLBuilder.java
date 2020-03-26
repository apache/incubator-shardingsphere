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

package org.apache.shardingsphere.sharding.rewrite.sql;

import org.apache.shardingsphere.sharding.rewrite.token.pojo.RouteUnitAware;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.sql.impl.AbstractSQLBuilder;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;

/**
 * SQL builder for sharding.
 */
public final class ShardingSQLBuilder extends AbstractSQLBuilder {
    
    private final RouteUnit routeUnit;
    
    public ShardingSQLBuilder(final SQLRewriteContext context, final RouteUnit routeUnit) {
        super(context);
        this.routeUnit = routeUnit;
    }
    
    @Override
    protected String getSQLTokenText(final SQLToken sqlToken) {
        if (sqlToken instanceof RouteUnitAware) {
            return ((RouteUnitAware) sqlToken).toString(routeUnit);
        }
        return sqlToken.toString();
    }
}

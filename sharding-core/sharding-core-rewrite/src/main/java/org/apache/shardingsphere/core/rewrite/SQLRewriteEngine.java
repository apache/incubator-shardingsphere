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

package org.apache.shardingsphere.core.rewrite;

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.builder.sql.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatementFactory;
import org.apache.shardingsphere.core.rewrite.token.builder.BaseTokenGeneratorBuilder;
import org.apache.shardingsphere.core.rewrite.token.builder.EncryptTokenGenerateBuilder;
import org.apache.shardingsphere.core.rewrite.token.SQLTokenGenerators;
import org.apache.shardingsphere.core.rewrite.token.builder.ShardingTokenGenerateBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.SQLToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SQL rewrite engine.
 * 
 * @author panjuan
 * @author zhangliang
 */
public final class SQLRewriteEngine {
    
    private final BaseRule baseRule;
    
    private final RewriteStatement rewriteStatement;
    
    private final List<SQLToken> sqlTokens;
    
    private final SQLBuilder sqlBuilder;
    
    private final ParameterBuilder parameterBuilder;
    
    public SQLRewriteEngine(final ShardingRule shardingRule, final TableMetas tableMetas, 
                            final SQLRouteResult sqlRouteResult, final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        baseRule = shardingRule;
        rewriteStatement = RewriteStatementFactory.newInstance(shardingRule, sqlRouteResult);
        parameterBuilder = ParameterBuilderFactory.newInstance(rewriteStatement, parameters, sqlRouteResult);
        sqlTokens = createSQLTokens(tableMetas, sqlRouteResult.getShardingConditions(), sqlRouteResult.getRoutingResult().isSingleRouting(), isQueryWithCipherColumn);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    public SQLRewriteEngine(final EncryptRule encryptRule, final TableMetas tableMetas,
                            final SQLStatementContext encryptStatement, final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        baseRule = encryptRule;
        rewriteStatement = RewriteStatementFactory.newInstance(encryptRule, encryptStatement);
        parameterBuilder = ParameterBuilderFactory.newInstance(rewriteStatement, parameters);
        sqlTokens = createSQLTokens(tableMetas, new ShardingConditions(Collections.<ShardingCondition>emptyList()), false, isQueryWithCipherColumn);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    public SQLRewriteEngine(final MasterSlaveRule masterSlaveRule, final SQLStatementContext sqlStatementContext, final String sql) {
        baseRule = masterSlaveRule;
        rewriteStatement = new RewriteStatement(sqlStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        parameterBuilder = ParameterBuilderFactory.newInstance(rewriteStatement, Collections.emptyList());
        sqlTokens = createSQLTokens(null, new ShardingConditions(Collections.<ShardingCondition>emptyList()), false, false);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    private List<SQLToken> createSQLTokens(final TableMetas tableMetas, final ShardingConditions shardingConditions, final boolean isSingleRoute, final boolean isQueryWithCipherColumn) {
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(new BaseTokenGeneratorBuilder().getSQLTokenGenerators());
        if (baseRule instanceof ShardingRule) {
            sqlTokenGenerators.addAll(new ShardingTokenGenerateBuilder((ShardingRule) baseRule, shardingConditions).getSQLTokenGenerators());
            sqlTokenGenerators.addAll(new EncryptTokenGenerateBuilder(((ShardingRule) baseRule).getEncryptRule(), isQueryWithCipherColumn).getSQLTokenGenerators());
        } else if (baseRule instanceof EncryptRule) {
            sqlTokenGenerators.addAll(new EncryptTokenGenerateBuilder((EncryptRule) baseRule, isQueryWithCipherColumn).getSQLTokenGenerators());
        }
        return sqlTokenGenerators.generateSQLTokens(rewriteStatement, parameterBuilder, tableMetas, isSingleRoute);
    }
    
    /**
     * Generate SQL.
     * 
     * @return SQL unit
     */
    public SQLUnit generateSQL() {
        return new SQLUnit(sqlBuilder.toSQL(), parameterBuilder.getParameters());
    }
    
    /**
     * Generate SQL.
     * 
     * @param routingUnit routing unit
     * @param logicAndActualTables logic and actual tables
     * @return SQL unit
     */
    public SQLUnit generateSQL(final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        return new SQLUnit(sqlBuilder.toSQL(routingUnit, logicAndActualTables), parameterBuilder.getParameters(routingUnit));
    }
}

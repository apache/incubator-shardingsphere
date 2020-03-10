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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.statement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.shadow.rewrite.judgement.impl.PreparedJudgementEngine;
import org.apache.shardingsphere.shadow.rewrite.judgement.ShadowJudgementEngine;
import org.apache.shardingsphere.shadow.rewrite.context.ShadowSQLRewriteContextDecorator;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractShardingPreparedStatementAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShadowConnection;
import org.apache.shardingsphere.sql.parser.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.sql.parser.binder.metadata.RelationMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.engine.impl.DefaultSQLRewriteEngine;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Shadow prepared statement.
 */
@Slf4j
public final class ShadowPreparedStatement extends AbstractShardingPreparedStatementAdapter {
    
    @Getter
    private final ShadowConnection connection;
    
    private final ShadowPreparedStatementGenerator preparedStatementGenerator;
    
    private final String sql;
    
    private PreparedStatement preparedStatement;
    
    private ResultSet resultSet;

    private boolean isShadowSQL;
    
    private final Collection<SQLUnit> sqlUnits = new LinkedList<>();
    
    public ShadowPreparedStatement(final ShadowConnection connection, final String sql) {
        this(connection, sql, -1, -1, -1, -1, null, null);
    }
    
    public ShadowPreparedStatement(final ShadowConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency) {
        this(connection, sql, resultSetType, resultSetConcurrency, -1, -1, null, null);
    }
    
    public ShadowPreparedStatement(final ShadowConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        this(connection, sql, resultSetType, resultSetConcurrency, resultSetHoldability, -1, null, null);
    }
    
    public ShadowPreparedStatement(final ShadowConnection connection, final String sql, final int autoGeneratedKeys) {
        this(connection, sql, -1, -1, -1, autoGeneratedKeys, null, null);
    }
    
    public ShadowPreparedStatement(final ShadowConnection connection, final String sql, final int[] columnIndexes) {
        this(connection, sql, -1, -1, -1, -1, columnIndexes, null);
    }
    
    public ShadowPreparedStatement(final ShadowConnection connection, final String sql, final String[] columnNames) {
        this(connection, sql, -1, -1, -1, -1, null, columnNames);
    }
    
    private ShadowPreparedStatement(final ShadowConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability,
                                   final int autoGeneratedKeys, final int[] columnIndexes, final String[] columnNames) {
        this.connection = connection;
        this.sql = sql;
        preparedStatementGenerator = new ShadowPreparedStatementGenerator(resultSetType, resultSetConcurrency, resultSetHoldability, autoGeneratedKeys, columnIndexes, columnNames);
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        try {
            SQLUnit sqlUnit = getSQLUnit(sql);
            preparedStatement = preparedStatementGenerator.createPreparedStatement(sqlUnit.getSql());
            replayMethodsInvocation(preparedStatement);
            replaySetParameter(preparedStatement, sqlUnit.getParameters());
            resultSet = preparedStatement.executeQuery();
            return resultSet;
        } finally {
            clearParameters();
        }
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        try {
            SQLUnit sqlUnit = getSQLUnit(sql);
            preparedStatement = preparedStatementGenerator.createPreparedStatement(sqlUnit.getSql());
            replayMethodsInvocation(preparedStatement);
            replaySetParameter(preparedStatement, sqlUnit.getParameters());
            return preparedStatement.executeUpdate();
        } finally {
            clearParameters();
        }
    }
    
    @Override
    public boolean execute() throws SQLException {
        try {
            SQLUnit sqlUnit = getSQLUnit(sql);
            preparedStatement = preparedStatementGenerator.createPreparedStatement(sqlUnit.getSql());
            replayMethodsInvocation(preparedStatement);
            replaySetParameter(preparedStatement, sqlUnit.getParameters());
            boolean result = preparedStatement.execute();
            resultSet = preparedStatement.getResultSet();
            return result;
        } finally {
            clearParameters();
        }
    }
    
    @Override
    public void addBatch() {
        sqlUnits.add(getSQLUnit(sql));
        clearParameters();
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        if (0 == sqlUnits.size()) {
            return new int[0];
        }
        try {
            preparedStatement = preparedStatementGenerator.createPreparedStatement(sqlUnits.iterator().next().getSql());
            replayMethodsInvocation(preparedStatement);
            replayBatchPreparedStatement();
            return preparedStatement.executeBatch();
        } finally {
            clearBatch();
        }
    }
    
    private void replayBatchPreparedStatement() throws SQLException {
        for (SQLUnit each : sqlUnits) {
            replaySetParameter(preparedStatement, each.getParameters());
            preparedStatement.addBatch();
        }
    }
    
    @SuppressWarnings("unchecked")
    private SQLUnit getSQLUnit(final String sql) {
        SQLStatement sqlStatement = connection.getRuntimeContext().getSqlParserEngine().parse(sql, true);
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(
                getRelationMetas(connection.getRuntimeContext().getMetaData().getTables()), sql, getParameters(), sqlStatement);
        ShadowJudgementEngine shadowJudgementEngine = new PreparedJudgementEngine(connection.getRuntimeContext().getRule(), sqlStatementContext, getParameters());
        isShadowSQL = shadowJudgementEngine.isShadowSQL();
        SQLRewriteContext sqlRewriteContext = new SQLRewriteEntry(connection.getRuntimeContext().getMetaData(), connection.getRuntimeContext().getProperties())
                .createSQLRewriteContext(sql, getParameters(), sqlStatementContext, createSQLRewriteContextDecorator(connection.getRuntimeContext().getRule()));
        SQLRewriteResult sqlRewriteResult = new DefaultSQLRewriteEngine().rewrite(sqlRewriteContext);
        showSQL(sqlRewriteResult.getSql());
        return new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters());
    }
    
    private RelationMetas getRelationMetas(final TableMetas tableMetas) {
        Map<String, RelationMetaData> result = new HashMap<>(tableMetas.getAllTableNames().size());
        for (String each : tableMetas.getAllTableNames()) {
            TableMetaData tableMetaData = tableMetas.get(each);
            result.put(each, new RelationMetaData(tableMetaData.getColumns().keySet()));
        }
        return new RelationMetas(result);
    }
    
    private Map<BaseRule, SQLRewriteContextDecorator> createSQLRewriteContextDecorator(final ShadowRule shadowRule) {
        Map<BaseRule, SQLRewriteContextDecorator> result = new HashMap<>(1, 1);
        result.put(shadowRule, new ShadowSQLRewriteContextDecorator());
        return result;
    }
    
    private void showSQL(final String sql) {
        boolean showSQL = connection.getRuntimeContext().getProperties().<Boolean>getValue(PropertiesConstant.SQL_SHOW);
        if (showSQL) {
            log.info("Rule Type: shadow");
            log.info("SQL: {} ::: IsShadowSQL: {}", sql, isShadowSQL);
        }
    }
    
    @Override
    protected boolean isAccumulate() {
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected Collection<? extends Statement> getRoutedStatements() {
        Collection<Statement> result = new LinkedList();
        if (null == preparedStatement) {
            return result;
        }
        result.add(preparedStatement);
        return result;
    }
    
    @Override
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    @Override
    public int getResultSetConcurrency() {
        return preparedStatementGenerator.resultSetConcurrency;
    }
    
    @Override
    public int getResultSetType() {
        return preparedStatementGenerator.resultSetType;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return preparedStatement.getGeneratedKeys();
    }
    
    @Override
    public int getResultSetHoldability() {
        return preparedStatementGenerator.resultSetHoldability;
    }
    
    @Override
    public void clearBatch() throws SQLException {
        if (null != preparedStatement) {
            preparedStatement.clearBatch();
        }
        sqlUnits.clear();
        clearParameters();
    }
    
    @RequiredArgsConstructor
    private final class ShadowPreparedStatementGenerator {
        
        private final int resultSetType;
        
        private final int resultSetConcurrency;
        
        private final int resultSetHoldability;
        
        private final int autoGeneratedKeys;
        
        private final int[] columnIndexes;
        
        private final String[] columnNames;
        
        private PreparedStatement createPreparedStatement(final String sql) throws SQLException {
            if (-1 != resultSetType && -1 != resultSetConcurrency && -1 != resultSetHoldability) {
                return isShadowSQL ? connection.getShadowConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability)
                        : connection.getActualConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            }
            if (-1 != resultSetType && -1 != resultSetConcurrency) {
                return isShadowSQL ? connection.getShadowConnection().prepareStatement(sql, resultSetType, resultSetConcurrency)
                        : connection.getActualConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
            }
            if (-1 != autoGeneratedKeys) {
                return isShadowSQL ? connection.getShadowConnection().prepareStatement(sql, autoGeneratedKeys)
                        : connection.getActualConnection().prepareStatement(sql, autoGeneratedKeys);
            }
            if (null != columnIndexes) {
                return isShadowSQL ? connection.getShadowConnection().prepareStatement(sql, columnIndexes)
                        : connection.getActualConnection().prepareStatement(sql, columnIndexes);
            }
            if (null != columnNames) {
                return isShadowSQL ? connection.getShadowConnection().prepareStatement(sql, columnNames)
                        : connection.getActualConnection().prepareStatement(sql, columnNames);
            }
            return isShadowSQL ? connection.getShadowConnection().prepareStatement(sql) : connection.getActualConnection().prepareStatement(sql);
        }
    }
}

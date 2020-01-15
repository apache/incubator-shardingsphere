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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShadowRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.ShadowPreparedStatement;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.ShadowStatement;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * Shadow connection.
 *
 * @author zhyee
 */
@RequiredArgsConstructor
@Getter
public final class ShadowConnection extends AbstractUnsupportedOperationConnection {
    
    private final Connection actualConnection;
    
    private final Connection shadowConnection;
    
    private final ShadowRuntimeContext runtimeContext;

    private boolean autoCommit = true;

    private boolean readOnly = true;

    private volatile boolean closed;

    private int transactionIsolation = TRANSACTION_READ_UNCOMMITTED;
    
    @Override
    public Statement createStatement() {
        return new ShadowStatement(this);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) {
        return new ShadowStatement(this, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        return new ShadowStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql) {
        return new ShadowPreparedStatement(this, sql);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) {
        return new ShadowPreparedStatement(this, sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        return new ShadowPreparedStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) {
        return new ShadowPreparedStatement(this, sql, autoGeneratedKeys);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) {
        return new ShadowPreparedStatement(this, sql, columnIndexes);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) {
        return new ShadowPreparedStatement(this, sql, columnNames);
    }
    
    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        actualConnection.setAutoCommit(autoCommit);
        shadowConnection.setAutoCommit(autoCommit);
    }
    
    @Override
    public boolean getAutoCommit() {
        return autoCommit;
    }
    
    @Override
    public void commit() throws SQLException {
        actualConnection.commit();
        shadowConnection.commit();
    }
    
    @Override
    public void rollback() throws SQLException {
        actualConnection.rollback();
        shadowConnection.rollback();
    }
    
    @Override
    public void close() throws SQLException {
        closed = true;
        actualConnection.close();
        shadowConnection.close();
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return actualConnection.getMetaData();
    }
    
    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        actualConnection.setReadOnly(readOnly);
        shadowConnection.setReadOnly(readOnly);
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        transactionIsolation = level;
        actualConnection.setTransactionIsolation(level);
        shadowConnection.setTransactionIsolation(level);
    }
    
    @Override
    public SQLWarning getWarnings() {
        return null;
    }
    
    @Override
    public void clearWarnings() {
    }
    
    @Override
    public void setHoldability(final int holdability) throws SQLException {
        actualConnection.setHoldability(holdability);
        shadowConnection.setHoldability(holdability);
    }
    
    @Override
    public int getHoldability() throws SQLException {
        return actualConnection.getHoldability();
    }
}

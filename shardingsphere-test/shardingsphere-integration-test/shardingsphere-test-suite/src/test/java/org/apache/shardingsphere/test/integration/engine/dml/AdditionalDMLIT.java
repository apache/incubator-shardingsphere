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

package org.apache.shardingsphere.test.integration.engine.dml;

import org.apache.shardingsphere.test.integration.cases.assertion.dml.DMLIntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLValue;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCaseType;
import org.apache.shardingsphere.test.integration.IntegrateTestParameters;
import org.apache.shardingsphere.test.integration.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertFalse;

public final class AdditionalDMLIT extends BaseDMLIT {
    
    private final DMLIntegrateTestCaseAssertion assertion;
    
    public AdditionalDMLIT(final String parentPath, final DMLIntegrateTestCaseAssertion assertion, final String ruleType,
                           final DatabaseType databaseType, final SQLCaseType caseType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(parentPath, assertion, ruleType, databaseType, caseType, sql);
        this.assertion = assertion;
    }
    
    @Parameters(name = "{2} -> {3} -> {4} -> {5}")
    public static Collection<Object[]> getParameters() {
        return IntegrateTestEnvironment.getInstance().isRunAdditionalTestCases() ? IntegrateTestParameters.getParametersWithAssertion(IntegrateTestCaseType.DML) : Collections.emptyList();
    }
    
    @Test
    public void executeUpdateWithAutoGeneratedKeys() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("replica_query".equals(getRuleType())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLCaseType.Literal == getCaseType() ? executeUpdateForStatementWithAutoGeneratedKeys(connection) : executeUpdateForPreparedStatementWithAutoGeneratedKeys(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeUpdateForStatementWithAutoGeneratedKeys(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(String.format(getSql(), assertion.getSQLValues().toArray()), Statement.NO_GENERATED_KEYS);
        }
    }
    
    private int executeUpdateForPreparedStatementWithAutoGeneratedKeys(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), Statement.NO_GENERATED_KEYS)) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
        }
    }
    
    @Test
    public void assertExecuteUpdateWithColumnIndexes() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("PostgreSQL".equals(getDatabaseType().getName()) || "replica_query".equals(getRuleType())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLCaseType.Literal == getCaseType() ? executeUpdateForStatementWithColumnIndexes(connection) : executeUpdateForPreparedStatementWithColumnIndexes(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeUpdateForStatementWithColumnIndexes(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(String.format(getSql(), assertion.getSQLValues().toArray()), new int[]{1});
        }
    }
    
    private int executeUpdateForPreparedStatementWithColumnIndexes(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), new int[]{1})) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
        }
    }
    
    @Test
    public void assertExecuteUpdateWithColumnNames() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("PostgreSQL".equals(getDatabaseType().getName()) || "replica_query".equals(getRuleType())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLCaseType.Literal == getCaseType() ? executeUpdateForStatementWithColumnNames(connection) : executeUpdateForPreparedStatementWithColumnNames(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeUpdateForStatementWithColumnNames(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(String.format(getSql(), assertion.getSQLValues().toArray()));
        }
    }
    
    private int executeUpdateForPreparedStatementWithColumnNames(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), new String[]{"TODO"})) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
        }
    }
    
    @Test
    public void assertExecuteWithoutAutoGeneratedKeys() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("replica_query".equals(getRuleType())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLCaseType.Literal == getCaseType() ? executeForStatementWithoutAutoGeneratedKeys(connection) : executeForPreparedStatementWithoutAutoGeneratedKeys(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeForStatementWithoutAutoGeneratedKeys(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertFalse("Not a DML statement.", statement.execute(String.format(getSql(), assertion.getSQLValues().toArray()), Statement.NO_GENERATED_KEYS));
            return statement.getUpdateCount();
        }
    }
    
    private int executeForPreparedStatementWithoutAutoGeneratedKeys(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), Statement.NO_GENERATED_KEYS)) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse("Not a DML statement.", preparedStatement.execute());
            return preparedStatement.getUpdateCount();
        }
    }
    
    @Test
    public void assertExecuteWithAutoGeneratedKeys() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("replica_query".equals(getRuleType())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLCaseType.Literal == getCaseType() ? executeForStatementWithAutoGeneratedKeys(connection) : executeForPreparedStatementWithAutoGeneratedKeys(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeForStatementWithAutoGeneratedKeys(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertFalse("Not a DML statement.", statement.execute(String.format(getSql(), assertion.getSQLValues().toArray()), Statement.RETURN_GENERATED_KEYS));
            return statement.getUpdateCount();
            // TODO assert statement.getGeneratedKeys();
        }
    }
    
    private int executeForPreparedStatementWithAutoGeneratedKeys(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), Statement.RETURN_GENERATED_KEYS)) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse("Not a DML statement.", preparedStatement.execute());
            return preparedStatement.getUpdateCount();
            // TODO assert preparedStatement.getGeneratedKeys();
        }
    }
    
    @Test
    public void assertExecuteWithColumnIndexes() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("PostgreSQL".equals(getDatabaseType().getName()) || "replica_query".equals(getRuleType())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLCaseType.Literal == getCaseType() ? executeForStatementWithColumnIndexes(connection) : executeForPreparedStatementWithColumnIndexes(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeForStatementWithColumnIndexes(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertFalse("Not a DML statement.", statement.execute(String.format(getSql(), assertion.getSQLValues().toArray()), new int[]{1}));
            return statement.getUpdateCount();
        }
    }
    
    private int executeForPreparedStatementWithColumnIndexes(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), new int[]{1})) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse("Not a DML statement.", preparedStatement.execute());
            return preparedStatement.getUpdateCount();
        }
    }
    
    @Test
    public void assertExecuteWithColumnNames() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("PostgreSQL".equals(getDatabaseType().getName()) || "replica_query".equals(getRuleType())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLCaseType.Literal == getCaseType() ? executeForStatementWithColumnNames(connection) : executeForPreparedStatementWithColumnNames(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeForStatementWithColumnNames(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertFalse("Not a DML statement.", statement.execute(String.format(getSql(), assertion.getSQLValues().toArray()), new String[]{"TODO"}));
            return statement.getUpdateCount();
        }
    }
    
    private int executeForPreparedStatementWithColumnNames(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), new String[]{"TODO"})) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse("Not a DML statement.", preparedStatement.execute());
            return preparedStatement.getUpdateCount();
        }
    }
}

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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.insert.InsertNamesAndValuesAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.parameter.ParameterMarkerAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.table.AlterTableAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl.InsertStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl.SelectStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.TCLStatement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * SQL statement assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementAssert {
    
    /**
     * Assert SQL statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual SQL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SQLStatement actual, final ParserResult expected) {
        ParameterMarkerAssert.assertCount(assertContext, actual.getParametersCount(), expected.getParameters().size());
        // TODO to be move TableAssert into statement details assert
        TableAssert.assertIs(assertContext, actual.findSQLSegments(TableSegment.class), expected.getTables());
        if (actual instanceof SelectStatement) {
            SelectStatementAssert.assertSelectStatement(assertContext, (SelectStatement) actual, expected);
        }
        if (actual instanceof InsertStatement) {
            InsertStatementAssert.assertInsertStatement(assertContext, (InsertStatement) actual, expected);
        }
        if (actual instanceof AlterTableStatement) {
            assertAlterTableStatement(assertContext, (AlterTableStatement) actual, expected);
        }
        if (actual instanceof TCLStatement) {
            assertTCLStatement((TCLStatement) actual, expected);
        }
    }
    
    private static void assertInsertStatement(final SQLCaseAssertContext assertContext, final InsertStatement actual, final ParserResult expected) {
        InsertNamesAndValuesAssert.assertIs(assertContext, actual, expected.getInsertColumnsAndValues());
    }
    
    private static void assertAlterTableStatement(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final ParserResult expected) {
        if (null != expected.getAlterTable()) {
            AlterTableAssert.assertIs(assertContext, actual, expected.getAlterTable());
        }
    }
    
    private static void assertTCLStatement(final TCLStatement actual, final ParserResult expected) {
        assertThat(actual.getClass().getName(), is(expected.getTclActualStatementClassType()));
        if (actual instanceof SetAutoCommitStatement) {
            assertThat(((SetAutoCommitStatement) actual).isAutoCommit(), is(expected.isAutoCommit()));
        }
    }
}

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

package org.apache.shardingsphere.core.merge.dql.pagination;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.dql.DQLMergeEngine;
import org.apache.shardingsphere.core.merge.fixture.TestQueryResult;
import org.apache.shardingsphere.core.preprocessor.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.Projection;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.preprocessor.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.preprocessor.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RowNumberDecoratorMergedResultTest {
    
    private DQLMergeEngine mergeEngine;
    
    private List<QueryResult> queryResults;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        List<ResultSet> resultSets = Lists.newArrayList(resultSet, mock(ResultSet.class), mock(ResultSet.class), mock(ResultSet.class));
        for (ResultSet each : resultSets) {
            when(each.next()).thenReturn(true, true, false);
        }
        queryResults = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            queryResults.add(new TestQueryResult(each));
        }
    }
    
    @Test
    public void assertNextForSkipAll() throws SQLException {
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, Integer.MAX_VALUE, true), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), null, routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithoutOffsetWithoutRowCount() throws SQLException {
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), null, routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        for (int i = 0; i < 8; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForRowCountBoundOpenedFalse() throws SQLException {
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 2, true), new NumberLiteralRowNumberValueSegment(0, 0, 4, false), Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), null, routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForRowCountBoundOpenedTrue() throws SQLException {
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 2, true), new NumberLiteralRowNumberValueSegment(0, 0, 4, true), Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), null, routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
}

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

package org.apache.shardingsphere.infra.optimize.core.convert.converter.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.SqlNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;

import java.util.Objects;
import java.util.Optional;

/**
 *  Column of order by converter. 
 */
public final class ColumnOrderByItemSqlNodeConverter implements SqlNodeConverter<ColumnOrderByItemSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final ColumnOrderByItemSegment columnOrderBy) {
        Optional<SqlNode> optional = new ColumnSqlNodeConverter().convert(columnOrderBy.getColumn());
        if (optional.isPresent() && Objects.equals(OrderDirection.DESC, columnOrderBy.getOrderDirection())) {
            optional = Optional.of(new SqlBasicCall(SqlStdOperatorTable.DESC, new SqlNode[] {optional.get()}, SqlParserPos.ZERO));
        }
        return optional;
    }
}

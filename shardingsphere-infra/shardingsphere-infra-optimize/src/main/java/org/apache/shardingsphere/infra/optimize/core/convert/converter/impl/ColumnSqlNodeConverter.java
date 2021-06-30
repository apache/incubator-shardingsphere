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

import com.google.common.collect.ImmutableList;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.SqlNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;

import java.util.Optional;

/**
 * Column converter.
 */
public final class ColumnSqlNodeConverter implements SqlNodeConverter<ColumnSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final ColumnSegment columnSegment) {
        Optional<OwnerSegment> owner = columnSegment.getOwner();
        String columnName = columnSegment.getIdentifier().getValue();
        if (owner.isPresent()) {
            return Optional.of(new SqlIdentifier(ImmutableList.of(owner.get().getIdentifier().getValue(), columnName), SqlParserPos.ZERO));
        }
        return Optional.of(new SqlIdentifier(columnName, SqlParserPos.ZERO));
    }
}

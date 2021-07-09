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

package org.apache.shardingsphere.infra.metadata.schema.refresher.type;

import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.DropTableEvent;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropViewStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * ShardingSphere schema refresher for drop view statement.
 */
public final class DropViewStatementSchemaRefresher implements SchemaRefresher<DropViewStatement> {
    
    @Override
    public void refresh(final ShardingSphereSchema schema, final Collection<String> routeDataSourceNames, final DropViewStatement sqlStatement, final SchemaBuilderMaterials materials) {
        sqlStatement.getViews().forEach(each -> schema.remove(each.getTableName().getIdentifier().getValue()));
        for (SimpleTableSegment each : sqlStatement.getViews()) {
            ShardingSphereEventBus.getInstance().post(new DropTableEvent(Collections.singletonList(each.getTableName().getIdentifier().getValue())));
        }
    }
}

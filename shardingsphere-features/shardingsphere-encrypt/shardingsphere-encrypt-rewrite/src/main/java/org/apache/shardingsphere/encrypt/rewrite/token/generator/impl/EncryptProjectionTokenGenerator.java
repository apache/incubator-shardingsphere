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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumn;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.*;

/**
 * Projection token generator for encrypt.
 */
@Setter
public final class EncryptProjectionTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator<SelectStatementContext>, QueryWithCipherColumnAware {
    
    private boolean queryWithCipherColumn;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getAllSimpleTableSegments().isEmpty();
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SelectStatementContext selectStatementContext) {
        ProjectionsSegment projectionsSegment = selectStatementContext.getSqlStatement().getProjections();
        // TODO process multiple tables
        IdentifierValue tableName = selectStatementContext.getAllSimpleTableSegments().iterator().next().getTableName().getIdentifier();
        return getEncryptRule().findEncryptTable(tableName.getValue()).map(
            encryptTable -> generateSQLTokens(projectionsSegment, tableName, selectStatementContext, encryptTable)).orElseGet(Collections::emptyList);
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final ProjectionsSegment segment, final IdentifierValue tableName,
                                                                       final SelectStatementContext selectStatementContext, final EncryptTable encryptTable) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (ProjectionSegment each : segment.getProjections()) {
            if (each instanceof ColumnProjectionSegment) {
                if (encryptTable.getLogicColumns().contains(((ColumnProjectionSegment) each).getColumn().getIdentifier().getValue())) {
                    result.add(generateSQLToken((ColumnProjectionSegment) each, tableName));
                }
            }
            if (isToGeneratedSQLToken(each, selectStatementContext, tableName)) {
                ShorthandProjection shorthandProjection = getShorthandProjection((ShorthandProjectionSegment) each, selectStatementContext.getProjectionsContext());
                if (!shorthandProjection.getActualColumns().isEmpty()) {
                    result.add(generateSQLToken((ShorthandProjectionSegment) each, shorthandProjection, tableName, encryptTable, selectStatementContext.getDatabaseType()));
                }
            }
        }
        return result;
    }
    
    private boolean isToGeneratedSQLToken(final ProjectionSegment projectionSegment, final SelectStatementContext selectStatementContext, final IdentifierValue tableName) {
        if (!(projectionSegment instanceof ShorthandProjectionSegment)) {
            return false;
        }
        Optional<OwnerSegment> ownerSegment = ((ShorthandProjectionSegment) projectionSegment).getOwner();
        return ownerSegment.map(segment -> selectStatementContext.getTablesContext().findTableNameFromSQL(segment.getIdentifier().getValue()).equalsIgnoreCase(tableName.getValue())).orElse(true);
    }
    
    private SubstitutableColumnNameToken generateSQLToken(final ColumnProjectionSegment segment, final IdentifierValue tableName) {
        String encryptColumnName = getEncryptColumnName(tableName.getValue(), segment.getColumn().getIdentifier().getValue());
//        if (!segment.getAlias().isPresent()) {
//            encryptColumnName += " AS " + segment.getColumn().getIdentifier().getValue();
//        }

        Optional<String> owner = segment.getColumn().getOwner().isPresent() ? Optional.ofNullable(segment.getColumn().getOwner().get().getIdentifier().getValue()) : Optional.empty();
        Optional<String> alias = segment.getAlias().isPresent() ? segment.getAlias() : Optional.ofNullable(segment.getColumn().getIdentifier().getValue());
        SubstitutableColumnNameToken substitutableColumnNameToken = owner.isPresent() ? new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), owner) :
                new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), owner);
        substitutableColumnNameToken.put(tableName.getValue(), new SubstitutableColumn(Optional.ofNullable(tableName.getValue()),owner, encryptColumnName,
                segment.getColumn().getIdentifier().getQuoteCharacter()
                ,alias));

        return substitutableColumnNameToken;
    }
    
    private SubstitutableColumnNameToken generateSQLToken(final ShorthandProjectionSegment segment,
                                                          final ShorthandProjection shorthandProjection, final IdentifierValue tableName, final EncryptTable encryptTable, final DatabaseType databaseType) {
        Optional<String> owner = segment.getOwner().isPresent() ? Optional.ofNullable(segment.getOwner().get().getIdentifier().getValue()) : Optional.empty();
        SubstitutableColumnNameToken substitutableColumnNameToken = new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), owner);
        QuoteCharacter quoteCharacter = databaseType.getQuoteCharacter();
        List<String> shorthandExtensionProjections = new LinkedList<>();
        for (ColumnProjection each : shorthandProjection.getActualColumns()) {
            if (encryptTable.getLogicColumns().contains(each.getName())) {
                shorthandExtensionProjections.add(new ColumnProjection(null == each.getOwner() ? null : quoteCharacter.wrap(each.getOwner()),
                        quoteCharacter.wrap(getEncryptColumnName(tableName.getValue(), each.getName())), each.getName()).getExpressionWithAlias());

                substitutableColumnNameToken.put(tableName.getValue(), new SubstitutableColumn(Optional.ofNullable(tableName.getValue()), Optional.ofNullable(each.getOwner()),
                        getEncryptColumnName(tableName.getValue(), each.getName()), quoteCharacter, Optional.ofNullable(each.getName())));
            } else {
                shorthandExtensionProjections.add(null == each.getOwner() ? quoteCharacter.wrap(each.getName()) : quoteCharacter.wrap(each.getOwner()) + "." + quoteCharacter.wrap(each.getName()));

                substitutableColumnNameToken.put(tableName.getValue(), new SubstitutableColumn(Optional.ofNullable(tableName.getValue()), Optional.ofNullable(each.getOwner()),
                        each.getName(), quoteCharacter, each.getAlias()));
            }
        }
        return substitutableColumnNameToken;
    }
    
    private String getEncryptColumnName(final String tableName, final String logicEncryptColumnName) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, logicEncryptColumnName);
        return plainColumn.isPresent() && !queryWithCipherColumn ? plainColumn.get() : getEncryptRule().getCipherColumn(tableName, logicEncryptColumnName);
    }
    
    private ShorthandProjection getShorthandProjection(final ShorthandProjectionSegment segment, final ProjectionsContext projectionsContext) {
        Optional<String> owner = segment.getOwner().isPresent() ? Optional.of(segment.getOwner().get().getIdentifier().getValue()) : Optional.empty();
        for (Projection each : projectionsContext.getProjections()) {
            if (each instanceof ShorthandProjection) {
                if (!owner.isPresent() && !((ShorthandProjection) each).getOwner().isPresent()) {
                    return (ShorthandProjection) each;
                }
                if (owner.isPresent() && owner.get().equals(((ShorthandProjection) each).getOwner().orElse(null))) {
                    return (ShorthandProjection) each;
                }
            }
        }
        throw new IllegalStateException(String.format("Can not find shorthand projection segment, owner is: `%s`", owner.orElse(null)));
    }
}

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

package org.apache.shardingsphere.core.rewrite.feature.shadow.token;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rewrite.feature.shadow.aware.ShadowRuleAware;
import org.apache.shardingsphere.core.rewrite.feature.shadow.token.generator.impl.RemoveShadowColumnTokenGenerator;
import org.apache.shardingsphere.core.rewrite.feature.shadow.token.generator.impl.ShadowInsertValuesTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.builder.SQLTokenGeneratorBuilder;
import org.apache.shardingsphere.core.rule.ShadowRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL token generator builder for shadow.
 *
 * @author zhyee
 */
@RequiredArgsConstructor
public final class ShadowTokenGenerateBuilder implements SQLTokenGeneratorBuilder {

    private final ShadowRule shadowRule;

    @Override
    public Collection<SQLTokenGenerator> getSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = buildSQLTokenGenerators();
        for (SQLTokenGenerator each : result) {
            ((ShadowRuleAware) each).setShadowRule(shadowRule);
        }
        return result;
    }

    private Collection<SQLTokenGenerator> buildSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = new LinkedList<>();
        //todo
        result.add(new ShadowInsertValuesTokenGenerator());
        result.add(new RemoveShadowColumnTokenGenerator());
        return result;
    }
}

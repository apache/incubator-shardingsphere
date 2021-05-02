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

package org.apache.shardingsphere.infra.optimizer.rel.physical;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.optimizer.planner.ShardingSphereConvention;

/**
 * Physical Relational operator with merge sort algorithm for multi input row stream. 
 */
public class SSMergeSort extends Sort implements SSRel {
    
    public SSMergeSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation) {
        super(cluster, traits, child, collation);
    }
    
    public SSMergeSort(final RelOptCluster cluster, final RelTraitSet traits, final RelNode child, final RelCollation collation, final RexNode offset, final RexNode fetch) {
        super(cluster, traits, child, collation, offset, fetch);
    }
    
    @Override
    public final Sort copy(final RelTraitSet traitSet, final RelNode newInput, final RelCollation newCollation, final RexNode offset, final RexNode fetch) {
        return new SSMergeSort(this.getCluster(), traitSet, newInput, newCollation, offset, fetch);
    }
    
    /**
     * Create <code>SSMergeSort</code>.
     * @param traitSet relTraitSet represents an ordered set of {@link org.apache.calcite.plan.RelTrait}s.
     * @param input Input of this operator 
     * @param collation Array of sort specifications
     * @param offset    Expression for number of rows to discard before returning first row
     * @param fetch     Expression for number of rows to fetch
     * @return <code>SSMergeSort</code> 
     */
    public static SSMergeSort create(final RelTraitSet traitSet, final RelNode input, final RelCollation collation,
                                     final RexNode offset, final RexNode fetch) {
        return new SSMergeSort(input.getCluster(), traitSet.replace(ShardingSphereConvention.INSTANCE), input, collation, offset, fetch);
    }
    
    /**
     * see {@link #create(RelTraitSet, RelNode, RelCollation, RexNode, RexNode)}.
     * @param traitSet traitSet
     * @param input input
     * @param collation collation
     * @return  <code>SSMergeSort</code> 
     */
    public static SSMergeSort create(final RelTraitSet traitSet, final RelNode input, final RelCollation collation) {
        return new SSMergeSort(input.getCluster(), traitSet.replace(ShardingSphereConvention.INSTANCE), input, collation);
    }
}
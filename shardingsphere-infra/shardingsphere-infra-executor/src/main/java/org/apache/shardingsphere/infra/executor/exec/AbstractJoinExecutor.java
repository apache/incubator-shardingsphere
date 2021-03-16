package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.core.JoinRelType;
import org.apache.shardingsphere.infra.executor.exec.evaluator.Evaluator;
import org.apache.shardingsphere.infra.executor.exec.meta.JoinColumnMetaData;
import org.apache.shardingsphere.infra.executor.exec.meta.JoinRow;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

public abstract class AbstractJoinExecutor extends AbstractExector {
    
    protected final Executor outer;
    protected final Executor inner;
    protected final Evaluator joinEvaluator;
    protected final JoinRelType joinType;
    
    public AbstractJoinExecutor(Executor outer, Executor inner, JoinRelType joinType, Evaluator joinEvaluator, final ExecContext execContext) {
        super(execContext);
        this.outer = outer;
        this.inner = inner;
        this.joinType = joinType;
        this.joinEvaluator = joinEvaluator;
    }
    
    @Override
    protected void executeInit() {
        outer.init();
        inner.init();
    }
    
    protected <T> T left(T outer, T inner) {
        return this.joinType.generatesNullsOnLeft() ? outer : inner;
    }
    
    protected <T> T right(T outer, T inner) {
        return this.joinType.generatesNullsOnLeft() ? inner : outer;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return new JoinColumnMetaData(left(outer.getMetaData(), inner.getMetaData()), right(outer.getMetaData(), inner.getMetaData()), joinType);
    }
    
    protected JoinRow newJoinRow(Row outerRow, Row innerRow) {
        return new JoinRow(left(outerRow, innerRow), right(outerRow, innerRow));
    }
}

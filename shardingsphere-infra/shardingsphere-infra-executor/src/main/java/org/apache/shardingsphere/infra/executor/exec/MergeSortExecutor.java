package org.apache.shardingsphere.infra.executor.exec;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.apache.calcite.rel.RelFieldCollation;
import org.apache.calcite.rel.RelFieldCollation.Direction;
import org.apache.calcite.rex.RexDynamicParam;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSMergeSort;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MergeSortExecutor extends AbstractExector implements Executor{
    
    private List<Executor> executors;
    
    private SSMergeSort mergeSort;
    
    private long offset;
    
    private long fetch;
    
    private Comparator<Row> ordering;
    
    private Iterator<Row> mergeSortIterator;
    
    private long count;
    
    public MergeSortExecutor(final ExecContext execContext, List<Executor> executors, SSMergeSort ssMergeSort) {
        super(execContext);
        this.executors = executors;
        this.mergeSort = ssMergeSort;
        List<RelFieldCollation> fieldCollations = this.mergeSort.collation.getFieldCollations();
        Iterable<Comparator<Row>> comparators = Iterables.transform(fieldCollations, fieldCollation -> comparator(fieldCollation));
        ordering = Ordering.compound(comparators);
    
        this.offset = resolveRexNodeValue(this.mergeSort.offset, 0L, Long.class);
        this.fetch = resolveRexNodeValue(this.mergeSort.fetch, Long.MAX_VALUE, Long.class);
    }
    
    public static Executor build(SSMergeSort rel, ExecutorBuilder executorBuilder) {
        Executor executor = executorBuilder.build(rel.getInput());
        if(executor instanceof MultiExecutor) {
            return new MergeSortExecutor(executorBuilder.getExecContext(), ((MultiExecutor)executor).getExecutors(), rel);
        }
        return executor;
    }
    
    @Override
    protected void executeInit() {
        executors.forEach(Executor::init);
        this.mergeSortIterator = Iterables.mergeSorted(executors, ordering).iterator();
        
        for(int i = 0; i < offset; i++) {
            if(!moveNext()) {
                break;
            }
            current();
        }
        
    }
    
    @Override
    public boolean moveNext() {
        if(!isInited()) {
            init();
        }
        if(count > fetch) {
            return false;
        }
        return mergeSortIterator.hasNext();
    }
    
    @Override
    public Row current() {
        count++;
        return mergeSortIterator.next();
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return executors.get(0).getMetaData();
    }
    
    private Comparator<Row> comparator(RelFieldCollation fieldCollation) {
        int nullComparison = fieldCollation.nullDirection.nullComparison;
        int fieldIdx = fieldCollation.getFieldIndex();
        if(fieldCollation.direction == Direction.ASCENDING) {
            return (o1, o2) -> comparator(o1, o2, fieldIdx, nullComparison);
        } else {
            return (o1, o2) -> comparator(o2, o1, fieldIdx, nullComparison);
        }
    } 
    
    private int comparator(Row r1, Row r2, int fieldIdx, int nullComparison) {
        Comparable<?> c1 = r1.getColumnValue(fieldIdx);
        Comparable<?> c2 = r2.getColumnValue(fieldIdx);
        return RelFieldCollation.compare(c1, c2, nullComparison);
    }
    
    private <T> T resolveRexNodeValue(RexNode rexNode, T defaultValue, Class<T> clazz) {
        if(rexNode == null) {
            return defaultValue;
        }
        if(rexNode instanceof RexLiteral) {
            return ((RexLiteral)rexNode).getValueAs(clazz);
        } else if(rexNode instanceof RexDynamicParam) {
            RexDynamicParam rexDynamicParam = (RexDynamicParam)rexNode;
            int idx = rexDynamicParam.getIndex();
            List<Object> parameters = execContext.getParameters();
            Object val = parameters.get(idx);
            // TODO using a Data type conveter
            return clazz.cast(val);
        }
        return defaultValue;
    }
}

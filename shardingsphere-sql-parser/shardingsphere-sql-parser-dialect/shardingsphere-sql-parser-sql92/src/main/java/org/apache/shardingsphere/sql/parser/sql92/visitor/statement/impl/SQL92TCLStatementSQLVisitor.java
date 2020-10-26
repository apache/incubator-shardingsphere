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

package org.apache.shardingsphere.sql.parser.sql92.visitor.statement.impl;

import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.type.impl.TCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.tcl.SQL92CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.tcl.SQL92RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.tcl.SQL92SetTransactionStatement;

/**
 * TCL Statement SQL visitor for SQL92.
 */
public final class SQL92TCLStatementSQLVisitor extends SQL92StatementSQLVisitor implements TCLSQLVisitor, SQLStatementVisitor {
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new SQL92SetTransactionStatement();
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new SQL92CommitStatement();
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new SQL92RollbackStatement();
    }
}

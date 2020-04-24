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

grammar TCLStatement;

import Symbol, Keyword, MySQLKeyword, Literals, BaseRule;

setTransaction
    : SET scope_? TRANSACTION transactionCharacteristic (COMMA_ transactionCharacteristic)*
    ;

setAutoCommit
    : SET scope_? AUTOCOMMIT EQ_ autoCommitValue
    ;

scope_
    : (GLOBAL | PERSIST | PERSIST_ONLY | SESSION)
    | AT_ AT_ (GLOBAL | PERSIST | PERSIST_ONLY | SESSION) DOT_
    ;

autoCommitValue
    : NUMBER_ | ON | OFF
    ;

beginTransaction
    : BEGIN | START TRANSACTION
    ;

commit
    : COMMIT
    ;

rollback
    : ROLLBACK
    ;

savepoint
    : SAVEPOINT
    ;

transactionCharacteristic
   : ISOLATION LEVEL level_ | accessMode_
   ;

level_
   : REPEATABLE READ | READ COMMITTED | READ UNCOMMITTED | SERIALIZABLE
   ;

accessMode_
   : READ (WRITE | ONLY)
   ;

/*
 * Licensed to the Apache Software Foundation (final ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, final Version 2.0
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

package org.apache.shardingsphere.agent.core.plugin.interceptor;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.advice.TargetObject;
import org.apache.shardingsphere.agent.core.mock.Material;
import org.apache.shardingsphere.agent.core.mock.advice.MockMethodAroundAdvice;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class MethodAroundInterceptorTest {
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private final boolean rebase;
    
    private final String methodName;
    
    private final String result;
    
    private final String[] expected;
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Lists.newArrayList(
                new Object[]{false, "mock", "invocation", new String[]{"before", "on", "after"}},
                new Object[]{true, "mock", "rebase invocation method", new String[]{"before", "after"}},
                new Object[]{false, "mockWithException", null, new String[]{"before", "exception", "after"}}
        );
    }
    
    @BeforeClass
    public static void setup() {
        ByteBuddyAgent.install();
        new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED))
                .with(new ByteBuddy())
                .type(ElementMatchers.named("org.apache.shardingsphere.agent.core.mock.Material"))
                .transform((builder, typeDescription, classLoader, module) -> {
                    if ("org.apache.shardingsphere.agent.core.mock.Material".equals(typeDescription.getTypeName())) {
                        return builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                                .implement(TargetObject.class)
                                .intercept(FieldAccessor.ofField(EXTRA_DATA));
                    }
                    return builder;
                }).installOnByteBuddyAgent();
    }
    
    @Test
    @SneakyThrows
    public void assertInterceptedMethod() {
        Material material = new ByteBuddy()
                .subclass(Material.class)
                .method(ElementMatchers.named(methodName))
                .intercept(MethodDelegation.withDefaultConfiguration().to(new MethodAroundInterceptor(new MockMethodAroundAdvice(rebase))))
                .make()
                .load(new MockClassLoader())
                .getLoaded()
                .newInstance();
        Deque<String> queue = new LinkedList<>();
        if ("mockWithException".equals(methodName)) {
            try {
                material.mockWithException(queue);
            } catch (IOException ignore) {
            }
        } else {
            Assert.assertThat(result, Matchers.is(material.mock(queue)));
        }
        Assert.assertThat(queue, Matchers.hasItems(expected));
    }
    
}

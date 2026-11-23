/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.dmn.feel.runtime.functions;

import org.junit.jupiter.api.Test;
import org.kie.dmn.feel.runtime.events.InvalidParametersEvent;

class SubstringAfterFunctionTest {

    private static final SubstringAfterFunction substringAfterFunction = SubstringAfterFunction.INSTANCE;

    @Test
    void invokeNull() {
        FunctionTestUtil.assertResultError(substringAfterFunction.invoke((String) null, null),
                                           InvalidParametersEvent.class);
        FunctionTestUtil.assertResultError(substringAfterFunction.invoke(null, "test"), InvalidParametersEvent.class);
        FunctionTestUtil.assertResultError(substringAfterFunction.invoke("test", null), InvalidParametersEvent.class);
    }

    @Test
    void invokeMatchExists() {
        FunctionTestUtil.assertResult(substringAfterFunction.invoke("foobar", "ob"), "ar");
        FunctionTestUtil.assertResult(substringAfterFunction.invoke("foobar", "o"), "obar");
    }

    @Test
    void invokeMatchExistsAtStart() {
        // match at index 0: everything after "foo" is "bar"
        FunctionTestUtil.assertResult(substringAfterFunction.invoke("foobar", "foo"), "bar");
    }

    @Test
    void invokeMatchExistsAtEnd() {
        // match at the very end: nothing follows "bar", so result is ""
        // this hits the index >= 0 hit-path but produces an empty string
        FunctionTestUtil.assertResult(substringAfterFunction.invoke("foobar", "bar"), "");
    }

    @Test
    void invokeMatchNotExists() {
        FunctionTestUtil.assertResult(substringAfterFunction.invoke("foobar", "oook"), "");
    }

    @Test
    void invokeEmptyMatch() {
        // empty match always found at index 0: entire string is returned
        FunctionTestUtil.assertResult(substringAfterFunction.invoke("hello", ""), "hello");
        FunctionTestUtil.assertResult(substringAfterFunction.invoke("", ""), "");
    }

    @Test
    void invokeEmptyString() {
        // non-empty match against empty string: not found, returns ""
        FunctionTestUtil.assertResult(substringAfterFunction.invoke("", "x"), "");
    }

    @Test
    void invokeUnicode() {
        // emoji / surrogate pairs — match found
        FunctionTestUtil.assertResult(substringAfterFunction.invoke("👋🌍🔥", "🌍"), "🔥");
        // emoji / surrogate pairs — match not found
        FunctionTestUtil.assertResult(substringAfterFunction.invoke("👋🌍🔥", "xyz"), "");
    }
}
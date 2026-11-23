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

class SubstringBeforeFunctionTest {

    private static final SubstringBeforeFunction substringBeforeFunction = SubstringBeforeFunction.INSTANCE;

    @Test
    void invokeNull() {
        FunctionTestUtil.assertResultError(substringBeforeFunction.invoke((String) null, null),
                                           InvalidParametersEvent.class);
        FunctionTestUtil.assertResultError(substringBeforeFunction.invoke(null, "test"), InvalidParametersEvent.class);
        FunctionTestUtil.assertResultError(substringBeforeFunction.invoke("test", null), InvalidParametersEvent.class);
    }

    @Test
    void invokeMatchExists() {
        // normal mid-string match: "foo" precedes "bar"
        FunctionTestUtil.assertResult(substringBeforeFunction.invoke("foobar", "bar"), "foo");
        FunctionTestUtil.assertResult(substringBeforeFunction.invoke("foobar", "b"), "foo");
    }

    @Test
    void invokeMatchExistsAtStart() {
        // match at index 0: nothing precedes it, so result is ""
        // exercises the critical index > 0 (not >= 0) boundary in the implementation
        FunctionTestUtil.assertResult(substringBeforeFunction.invoke("foobar", "foo"), "");
        FunctionTestUtil.assertResult(substringBeforeFunction.invoke("foobar", "f"), "");
    }

    @Test
    void invokeMatchNotExists() {
        FunctionTestUtil.assertResult(substringBeforeFunction.invoke("foobar", "xyz"), "");
    }

    @Test
    void invokeEmptyMatch() {
        // empty match always found at index 0: nothing precedes index 0, returns ""
        FunctionTestUtil.assertResult(substringBeforeFunction.invoke("hello", ""), "");
        FunctionTestUtil.assertResult(substringBeforeFunction.invoke("", ""), "");
    }

    @Test
    void invokeEmptyString() {
        // non-empty match against empty string: not found, returns ""
        FunctionTestUtil.assertResult(substringBeforeFunction.invoke("", "x"), "");
    }

    @Test
    void invokeUnicode() {
        // emoji / surrogate pairs — match found
        FunctionTestUtil.assertResult(substringBeforeFunction.invoke("👋🌍🔥", "🌍"), "👋");
        // emoji / surrogate pairs — match not found
        FunctionTestUtil.assertResult(substringBeforeFunction.invoke("👋🌍🔥", "xyz"), "");
    }
}

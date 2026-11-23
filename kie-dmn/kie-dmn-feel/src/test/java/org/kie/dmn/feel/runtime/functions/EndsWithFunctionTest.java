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

class EndsWithFunctionTest {

    private static final EndsWithFunction endsWithFunction = EndsWithFunction.INSTANCE;

    @Test
    void invokeParamsNull() {
        FunctionTestUtil.assertResultError(endsWithFunction.invoke((String) null, null), InvalidParametersEvent.class);
        FunctionTestUtil.assertResultError(endsWithFunction.invoke(null, "test"), InvalidParametersEvent.class);
        FunctionTestUtil.assertResultError(endsWithFunction.invoke("test", null), InvalidParametersEvent.class);
    }

    @Test
    void invokeEndsWith() {
        FunctionTestUtil.assertResult(endsWithFunction.invoke("test", "t"), true);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("test", "st"), true);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("test", "est"), true);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("test", "test"), true);
    }

    @Test
    void invokeNotEndsWith() {
        FunctionTestUtil.assertResult(endsWithFunction.invoke("test", "es"), false);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("test", "ttttt"), false);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("test", "estt"), false);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("test", "tt"), false);
    }

    @Test
    void invokeEmptyString() {
        FunctionTestUtil.assertResult(endsWithFunction.invoke("", ""), true);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("", "test"), false);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("test", ""), true);
    }

    @Test
    void invokeCaseSensitive() {
        // FEEL ends with() is case-sensitive
        FunctionTestUtil.assertResult(endsWithFunction.invoke("test", "ST"), false);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("TEST", "st"), false);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("Test", "Test"), true);
    }

    @Test
    void invokeNonAsciiMatch() {
        // BMP non-ASCII: é is a single code point / char
        FunctionTestUtil.assertResult(endsWithFunction.invoke("café", "é"), true);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("café", "E"), false);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("café", "caf"), false);
    }

    @Test
    void invokeSurrogatePairMatch() {
        // supplementary characters (emoji): each is a surrogate pair in UTF-16
        FunctionTestUtil.assertResult(endsWithFunction.invoke("👋🌍🔥", "🔥"), true);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("👋🌍🔥", "🌍"), false);
        FunctionTestUtil.assertResult(endsWithFunction.invoke("👋🌍🔥", "xyz"), false);
    }
}
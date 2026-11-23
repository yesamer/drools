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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class StringJoinFunctionTest {

    private static final StringJoinFunction stringJoinFunction = StringJoinFunction.INSTANCE;

    @Test
    void setStringJoinFunctionNullValues() {
        FunctionTestUtil.assertResultError(stringJoinFunction.invoke( null), InvalidParametersEvent.class);
        FunctionTestUtil.assertResultError(stringJoinFunction.invoke((List<?>) null , null), InvalidParametersEvent.class);
    }

    @Test
    void stringJoinFunctionEmptyList() {
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Collections.emptyList()), "");
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Collections.emptyList(), "X"), "");
    }

    @Test
    void stringJoinFunction() {
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList("a", "b", "c"), "_and_"), "a_and_b_and_c");
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList("a", "b", "c"), ""), "abc");
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList("a", "b", "c"), null), "abc");
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList("a"), "X"), "a");
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList("a", null, "c"), "X"), "aXc");
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList("a", "b", "c")), "abc");
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList("a", null, "c")), "ac");
    }

    @Test
    void stringJoinFunctionSingleElementNull() {
        // single-element list whose only element is null → null is ignored → empty string
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList((String) null), "X"), "");
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList((String) null)), "");
    }

    @Test
    void stringJoinFunctionSingleElementNonString() {
        // single-element list with a non-string element → error (fast-path branch)
        FunctionTestUtil.assertResultError(stringJoinFunction.invoke(List.of(42), "X"), InvalidParametersEvent.class);
        FunctionTestUtil.assertResultError(stringJoinFunction.invoke(List.of(42)), InvalidParametersEvent.class);
    }

    @Test
    void stringJoinFunctionNonStringElementInMultiElementList() {
        // non-string element somewhere in a multi-element list → error (StringBuilder path)
        FunctionTestUtil.assertResultError(stringJoinFunction.invoke(Arrays.asList("a", 42, "c"), "X"), InvalidParametersEvent.class);
        FunctionTestUtil.assertResultError(stringJoinFunction.invoke(List.of(1, 2), ","), InvalidParametersEvent.class);
    }

    @Test
    void stringJoinFunctionAllNullElements() {
        // all elements are null → all ignored → empty string (multi-element path)
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList(null, null, null), "X"), "");
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList(null, null)), "");
    }

    @Test
    void stringJoinFunctionCharSequenceElements() {
        // CharSequence elements that are not String instances must be accepted
        CharSequence cs1 = new StringBuilder("hello");
        CharSequence cs2 = new StringBuilder("world");
        // single-element fast-path, CharSequence branch
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(List.of(cs1), "-"), "hello");
        // multi-element StringBuilder path, CharSequence elements
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList(cs1, cs2), "-"), "hello-world");
        FunctionTestUtil.assertResult(stringJoinFunction.invoke(Arrays.asList(cs1, cs2), null), "helloworld");
    }

}

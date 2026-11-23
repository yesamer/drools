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

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.kie.dmn.feel.runtime.events.InvalidParametersEvent;

class StringLengthFunctionTest {

    private static final StringLengthFunction stringLengthFunction = StringLengthFunction.INSTANCE;

    @Test
    void invokeNull() {
        FunctionTestUtil.assertResultError(stringLengthFunction.invoke(null), InvalidParametersEvent.class);
    }

    @Test
    void invokeEmptyString() {
        FunctionTestUtil.assertResult(stringLengthFunction.invoke(""), BigDecimal.ZERO);
    }

    @Test
    void invoke() {
        FunctionTestUtil.assertResult(stringLengthFunction.invoke("testString"), BigDecimal.TEN);
    }

    @Test
    void invokeSingleChar() {
        FunctionTestUtil.assertResult(stringLengthFunction.invoke("a"), BigDecimal.ONE);
    }

    @Test
    void invokeLongerThanTen() {
        // Exercises BigDecimal.valueOf path beyond the JDK's internal 0-10 cache
        FunctionTestUtil.assertResult(stringLengthFunction.invoke("hello world"), new BigDecimal(11));
    }

    @Test
    void invokeBmpNonAscii() {
        // é, ñ, ü are BMP characters: each is one char unit and one code point
        FunctionTestUtil.assertResult(stringLengthFunction.invoke("héllo"), new BigDecimal(5));
    }

    @Test
    void invokeSingleSurrogatePair() {
        // 😀 (U+1F600) is encoded as two char units but counts as one code point
        FunctionTestUtil.assertResult(stringLengthFunction.invoke("\uD83D\uDE00"), BigDecimal.ONE);
    }

    @Test
    void invokeAllSurrogatePairs() {
        // Five emoji = five code points, but ten Java char units
        FunctionTestUtil.assertResult(stringLengthFunction.invoke("👋🌍🔥🚀🌟"), new BigDecimal(5));
    }

    @Test
    void invokeMixedAsciiAndSurrogatePair() {
        // "Order-📦" = 6 ASCII chars + 1 emoji = 7 code points, 8 Java char units
        FunctionTestUtil.assertResult(stringLengthFunction.invoke("Order-\uD83D\uDCE6"), new BigDecimal(7));
    }
}
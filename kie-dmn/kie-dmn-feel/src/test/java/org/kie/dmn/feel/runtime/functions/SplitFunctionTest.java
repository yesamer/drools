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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kie.dmn.feel.runtime.events.InvalidParametersEvent;

class SplitFunctionTest {

    private static final SplitFunction splitFunction = SplitFunction.INSTANCE;

    // ── Null guards ───────────────────────────────────────────────────────────

    @Test
    void invokeNullString() {
        FunctionTestUtil.assertResultError(splitFunction.invoke(null, ","), InvalidParametersEvent.class);
    }

    @Test
    void invokeNullDelimiter() {
        FunctionTestUtil.assertResultError(splitFunction.invoke("foo,bar", null), InvalidParametersEvent.class);
    }

    @Test
    void invokeBothNull() {
        FunctionTestUtil.assertResultError(splitFunction.invoke((String) null, (String) null), InvalidParametersEvent.class);
    }

    // ── Invalid delimiter (bad regex) ─────────────────────────────────────────

    @Test
    void invokeInvalidRegexDelimiter() {
        // Unmatched bracket is a PatternSyntaxException
        FunctionTestUtil.assertResultError(splitFunction.invoke("foo,bar", "[unclosed"), InvalidParametersEvent.class);
    }

    @Test
    void invokeInvalidRegexDelimiterDanglingQuantifier() {
        // Dangling quantifier with nothing to repeat
        FunctionTestUtil.assertResultError(splitFunction.invoke("foo,bar", "+"), InvalidParametersEvent.class);
    }

    // ── Basic splitting ───────────────────────────────────────────────────────

    @Test
    void invokeSimpleCommaDelimiter() {
        FunctionTestUtil.assertResult(splitFunction.invoke("foo,bar,baz", ","),
                Arrays.asList("foo", "bar", "baz"));
    }

    @Test
    void invokeMultiCharLiteralDelimiter() {
        // Two-character literal delimiter
        FunctionTestUtil.assertResult(splitFunction.invoke("foo, bar, baz", ", "),
                Arrays.asList("foo", "bar", "baz"));
    }

    @Test
    void invokeRegexCharClassDelimiter() {
        // Character class — matches either semicolon or pipe
        FunctionTestUtil.assertResult(splitFunction.invoke("foo;bar|baz", "[;|]"),
                Arrays.asList("foo", "bar", "baz"));
    }

    @Test
    void invokeRegexWhitespaceDelimiter() {
        // Whitespace regex — matches one or more whitespace chars
        FunctionTestUtil.assertResult(splitFunction.invoke("foo  bar\tbaz", "\\s+"),
                Arrays.asList("foo", "bar", "baz"));
    }

    @Test
    void invokeSingleToken() {
        // Delimiter not present → list with the whole string as the only element
        FunctionTestUtil.assertResult(splitFunction.invoke("foobar", ","),
                List.of("foobar"));
    }

    // ── Empty string / empty token edge cases ─────────────────────────────────

    @Test
    void invokeEmptyInputString() {
        // Empty string with a non-matching delimiter produces a single-element list [""]
        // (String.split("", -1) returns String[]{""}  — one empty token)
        FunctionTestUtil.assertResult(splitFunction.invoke("", ","),
                List.of(""));
    }

    @Test
    void invokeLeadingDelimiter() {
        // Leading delimiter produces empty first token
        FunctionTestUtil.assertResult(splitFunction.invoke(",a,b", ","),
                Arrays.asList("", "a", "b"));
    }

    @Test
    void invokeTrailingDelimiter() {
        // Trailing delimiter preserved (limit=-1): produces empty last token
        FunctionTestUtil.assertResult(splitFunction.invoke("a,b,", ","),
                Arrays.asList("a", "b", ""));
    }

    @Test
    void invokeConsecutiveDelimiters() {
        // Two consecutive delimiters produce an empty middle token
        FunctionTestUtil.assertResult(splitFunction.invoke("a,,b", ","),
                Arrays.asList("a", "", "b"));
    }

    @Test
    void invokeOnlyDelimiters() {
        // String is entirely delimiters → all-empty tokens
        FunctionTestUtil.assertResult(splitFunction.invoke(",,,", ","),
                Arrays.asList("", "", "", ""));
    }

    // ── Two-token splits ──────────────────────────────────────────────────────

    @Test
    void invokeTwoTokens() {
        FunctionTestUtil.assertResult(splitFunction.invoke("hello world", " "),
                Arrays.asList("hello", "world"));
    }

    // ── Spec examples (DMN 1.3 §10.3.4.4) ───────────────────────────────────

    @Test
    void invokeSpecExample() {
        // split("John Doe", "\\s") = ["John","Doe"]
        FunctionTestUtil.assertResult(splitFunction.invoke("John Doe", "\\s"),
                Arrays.asList("John", "Doe"));
    }

    @Test
    void invokeSpecExampleMultipleSeparators() {
        // split("a;b;c;d", ";") = ["a","b","c","d"]
        FunctionTestUtil.assertResult(splitFunction.invoke("a;b;c;d", ";"),
                Arrays.asList("a", "b", "c", "d"));
    }

    // ── Non-ASCII / supplementary character strings ───────────────────────────

    @Test
    void invokeNonAsciiTokens() {
        // BMP non-ASCII characters in tokens — delimiter is ASCII
        FunctionTestUtil.assertResult(splitFunction.invoke("café,naïve,résumé", ","),
                Arrays.asList("café", "naïve", "résumé"));
    }

    @Test
    void invokeSurrogatePairTokens() {
        // Tokens containing supplementary characters (emoji)
        FunctionTestUtil.assertResult(splitFunction.invoke("👋,🌍,🔥", ","),
                Arrays.asList("👋", "🌍", "🔥"));
    }

    @Test
    void invokeNonAsciiDelimiter() {
        // Delimiter is itself a non-ASCII character
        FunctionTestUtil.assertResult(splitFunction.invoke("fooçbarçbaz", "ç"),
                Arrays.asList("foo", "bar", "baz"));
    }
}

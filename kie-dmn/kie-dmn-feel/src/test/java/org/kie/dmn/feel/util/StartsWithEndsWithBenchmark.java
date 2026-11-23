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
package org.kie.dmn.feel.util;

import java.util.Arrays;

import org.kie.dmn.feel.runtime.functions.FEELFnResult;

/**
 * Standalone benchmark comparing the ORIGINAL vs FIXED strategies for the FEEL
 * {@code starts with()} and {@code ends with()} functions.
 *
 * <h2>What we are measuring</h2>
 *
 * <p>The current production code in both {@code StartsWithFunction.java} and
 * {@code EndsWithFunction.java} does:
 * <pre>
 *   return FEELFnResult.ofResult( string.startsWith( match ) );   // / endsWith
 * </pre>
 *
 * <p>On the happy path (no nulls) every call allocates:
 * <ol>
 *   <li>{@code Boolean.TRUE} or {@code Boolean.FALSE} — autoboxed from the primitive result of
 *       {@code startsWith}/{@code endsWith}, but these are JVM-cached so <em>no heap allocation
 *       here</em>.</li>
 *   <li>{@code Optional.ofNullable(Boolean)} → a <b>fresh {@code Optional<Boolean>}</b> wrapping
 *       the boxed value — heap allocation #1.</li>
 *   <li>{@code new FEELFnResult<>(Optional.empty(), optional)} → the wrapper object itself —
 *       heap allocation #2.</li>
 * </ol>
 *
 * <p>Because there are only two possible outcomes ({@code true} / {@code false}), pre-caching
 * both {@code FEELFnResult<Boolean>} singletons eliminates <em>every</em> allocation on the
 * happy path — exactly the same fix already applied to {@code ContainsFunction}.
 *
 * <h2>Scenarios</h2>
 * <ul>
 *   <li><b>A</b> — {@code FEELFnResult.ofResult()} allocation cost in isolation (no string work).
 *       Confirms the wrapping is the bottleneck, not the underlying {@code String} predicate.</li>
 *   <li><b>B</b> — full {@code starts with()} call, constant prefix <em>present</em> (always
 *       {@code true}).</li>
 *   <li><b>C</b> — full {@code starts with()} call, constant prefix <em>absent</em> (always
 *       {@code false}).</li>
 *   <li><b>D</b> — unique input per row for {@code starts with()}, cycling through a 2 000-entry
 *       pool (alternating hit/miss — keeps branch predictor honest).</li>
 *   <li><b>E</b> — full {@code ends with()} call, constant suffix <em>present</em>.</li>
 *   <li><b>F</b> — full {@code ends with()} call, constant suffix <em>absent</em>.</li>
 *   <li><b>G</b> — unique input per row for {@code ends with()}, pool of 2 000 entries.</li>
 * </ul>
 *
 * <p>Run:
 * <pre>
 *   mvn exec:java -pl kie-dmn/kie-dmn-feel \
 *       -Dexec.mainClass=org.kie.dmn.feel.util.StartsWithEndsWithBenchmark \
 *       -Dexec.classpathScope=test
 * </pre>
 */
public class StartsWithEndsWithBenchmark {

    // ── Representative inputs ─────────────────────────────────────────────────

    /**
     * Typical short business-rule strings — dominant case in DMN models.
     *
     * For starts with(): prefix "Order-" is present in HIT_SW, absent in MISS_SW.
     * For ends with():   suffix "-APPROVED" is present in HIT_EW, absent in MISS_EW.
     */
    static final String HIT_SW  = "Order-APPROVED-2024";  // startsWith("Order-")     → true
    static final String MISS_SW = "Invoice-APPROVED-2024"; // startsWith("Order-")    → false
    static final String PREFIX  = "Order-";

    static final String HIT_EW  = "Order-APPROVED-2024";  // endsWith("-2024")        → true
    static final String MISS_EW = "Order-APPROVED-NOW";   // endsWith("-2024")        → false
    static final String SUFFIX  = "-2024";

    /** Pool of unique strings for scenarios D and G. Half match, half do not. */
    static final int      POOL_SIZE = 2_000;
    static final String[] POOL_SW;   // pool for starts with
    static final String[] POOL_EW;   // pool for ends with
    static {
        POOL_SW = new String[POOL_SIZE];
        POOL_EW = new String[POOL_SIZE];
        for (int i = 0; i < POOL_SIZE; i++) {
            // Alternating hit / miss keeps the branch predictor honest
            POOL_SW[i] = (i % 2 == 0)
                    ? "Order-ITEM-"   + i   // startsWith("Order-") → true
                    : "Invoice-ITEM-" + i;  // startsWith("Order-") → false
            POOL_EW[i] = (i % 2 == 0)
                    ? "Order-ITEM-"   + i + "-2024"  // endsWith("-2024") → true
                    : "Order-ITEM-"   + i + "-NOW";  // endsWith("-2024") → false
        }
    }

    // ── Harness constants ─────────────────────────────────────────────────────

    static final int ROWS_PER_RUN = 500_000;
    static final int WARMUP_ITERS = 50;
    static final int TIMED_RUNS   = 5;

    @FunctionalInterface interface Workload { void run(int n); }

    /** Prevents the JIT from dead-code-eliminating benchmark calls. */
    static volatile Object objectSink = null;

    // ── Shared cached singletons (the fix) ───────────────────────────────────

    static final FEELFnResult<Boolean> RESULT_TRUE  = FEELFnResult.ofResult(Boolean.TRUE);
    static final FEELFnResult<Boolean> RESULT_FALSE = FEELFnResult.ofResult(Boolean.FALSE);

    // ── starts with() implementations ────────────────────────────────────────

    /**
     * ORIGINAL: mirrors the current production code for {@code StartsWithFunction} exactly.
     * Allocates a fresh {@code Optional<Boolean>} + {@code FEELFnResult} on every call.
     */
    static FEELFnResult<Boolean> startsWithOriginal(String string, String match) {
        return FEELFnResult.ofResult(string.startsWith(match));
    }

    /**
     * FIXED: pre-cached singleton results — zero allocation on the happy path.
     */
    static FEELFnResult<Boolean> startsWithFixed(String string, String match) {
        return string.startsWith(match) ? RESULT_TRUE : RESULT_FALSE;
    }

    // ── ends with() implementations ──────────────────────────────────────────

    /**
     * ORIGINAL: mirrors the current production code for {@code EndsWithFunction} exactly.
     * Allocates a fresh {@code Optional<Boolean>} + {@code FEELFnResult} on every call.
     */
    static FEELFnResult<Boolean> endsWithOriginal(String string, String match) {
        return FEELFnResult.ofResult(string.endsWith(match));
    }

    /**
     * FIXED: pre-cached singleton results — zero allocation on the happy path.
     */
    static FEELFnResult<Boolean> endsWithFixed(String string, String match) {
        return string.endsWith(match) ? RESULT_TRUE : RESULT_FALSE;
    }

    // ── Wrapping micro-benchmark helpers ─────────────────────────────────────

    /** ORIGINAL wrapping: allocates every call. */
    static FEELFnResult<Boolean> wrapOriginal(boolean value) {
        return FEELFnResult.ofResult(value);
    }

    /** FIXED wrapping: returns the pre-cached singleton. */
    static FEELFnResult<Boolean> wrapFixed(boolean value) {
        return value ? RESULT_TRUE : RESULT_FALSE;
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        header();

        // ── SCENARIO A — FEELFnResult wrapping cost in isolation ─────────────
        section("SCENARIO A – FEELFnResult<Boolean> wrapping only (500k rows/run)");
        note("Holds the boolean constant (=true); isolates the wrapper allocation cost.");
        note("No String predicate work; pure measure of boxing overhead (shared by starts/ends with).");

        System.out.println("  [1] ORIGINAL: FEELFnResult.ofResult(true)  — allocates Optional + FEELFnResult:");
        long[] oA = timedRuns("orig/wrap/true", n -> {
            for (int i = 0; i < n; i++) { objectSink = wrapOriginal(true); }
        });
        System.out.println("  [2] FIXED:    cached RESULT_TRUE singleton — zero allocation:");
        long[] fA = timedRuns("fixed/wrap/true", n -> {
            for (int i = 0; i < n; i++) { objectSink = wrapFixed(true); }
        });
        printSummary(oA, fA, ROWS_PER_RUN);

        // ── SCENARIO B — starts with(), constant prefix present (hit) ────────
        section("SCENARIO B – Full starts with(), constant prefix FOUND (500k rows/run)");
        note("HIT_SW=\"" + HIT_SW + "\", PREFIX=\"" + PREFIX + "\" → always true.");

        System.out.println("  [1] ORIGINAL:");
        long[] oB = timedRuns("orig/sw/hit", n -> {
            for (int i = 0; i < n; i++) { objectSink = startsWithOriginal(HIT_SW, PREFIX); }
        });
        System.out.println("  [2] FIXED:");
        long[] fB = timedRuns("fixed/sw/hit", n -> {
            for (int i = 0; i < n; i++) { objectSink = startsWithFixed(HIT_SW, PREFIX); }
        });
        printSummary(oB, fB, ROWS_PER_RUN);

        // ── SCENARIO C — starts with(), constant prefix absent (miss) ────────
        section("SCENARIO C – Full starts with(), constant prefix NOT FOUND (500k rows/run)");
        note("MISS_SW=\"" + MISS_SW + "\", PREFIX=\"" + PREFIX + "\" → always false.");

        System.out.println("  [1] ORIGINAL:");
        long[] oC = timedRuns("orig/sw/miss", n -> {
            for (int i = 0; i < n; i++) { objectSink = startsWithOriginal(MISS_SW, PREFIX); }
        });
        System.out.println("  [2] FIXED:");
        long[] fC = timedRuns("fixed/sw/miss", n -> {
            for (int i = 0; i < n; i++) { objectSink = startsWithFixed(MISS_SW, PREFIX); }
        });
        printSummary(oC, fC, ROWS_PER_RUN);

        // ── SCENARIO D — starts with(), unique input per row ─────────────────
        section("SCENARIO D – starts with() unique input per row, pool of " + POOL_SIZE + " strings (500k rows/run)");
        note("Simulates a DMN table where every row carries a different string value.");
        note("Pool alternates hit/miss to keep branch predictor honest.");

        System.out.println("  [1] ORIGINAL:");
        long[] oD = timedRuns("orig/sw/pool", n -> {
            for (int i = 0; i < n; i++) { objectSink = startsWithOriginal(POOL_SW[i % POOL_SIZE], PREFIX); }
        });
        System.out.println("  [2] FIXED:");
        long[] fD = timedRuns("fixed/sw/pool", n -> {
            for (int i = 0; i < n; i++) { objectSink = startsWithFixed(POOL_SW[i % POOL_SIZE], PREFIX); }
        });
        printSummary(oD, fD, ROWS_PER_RUN);

        // ── SCENARIO E — ends with(), constant suffix present (hit) ──────────
        section("SCENARIO E – Full ends with(), constant suffix FOUND (500k rows/run)");
        note("HIT_EW=\"" + HIT_EW + "\", SUFFIX=\"" + SUFFIX + "\" → always true.");

        System.out.println("  [1] ORIGINAL:");
        long[] oE = timedRuns("orig/ew/hit", n -> {
            for (int i = 0; i < n; i++) { objectSink = endsWithOriginal(HIT_EW, SUFFIX); }
        });
        System.out.println("  [2] FIXED:");
        long[] fE = timedRuns("fixed/ew/hit", n -> {
            for (int i = 0; i < n; i++) { objectSink = endsWithFixed(HIT_EW, SUFFIX); }
        });
        printSummary(oE, fE, ROWS_PER_RUN);

        // ── SCENARIO F — ends with(), constant suffix absent (miss) ──────────
        section("SCENARIO F – Full ends with(), constant suffix NOT FOUND (500k rows/run)");
        note("MISS_EW=\"" + MISS_EW + "\", SUFFIX=\"" + SUFFIX + "\" → always false.");

        System.out.println("  [1] ORIGINAL:");
        long[] oF = timedRuns("orig/ew/miss", n -> {
            for (int i = 0; i < n; i++) { objectSink = endsWithOriginal(MISS_EW, SUFFIX); }
        });
        System.out.println("  [2] FIXED:");
        long[] fF = timedRuns("fixed/ew/miss", n -> {
            for (int i = 0; i < n; i++) { objectSink = endsWithFixed(MISS_EW, SUFFIX); }
        });
        printSummary(oF, fF, ROWS_PER_RUN);

        // ── SCENARIO G — ends with(), unique input per row ───────────────────
        section("SCENARIO G – ends with() unique input per row, pool of " + POOL_SIZE + " strings (500k rows/run)");
        note("Simulates a DMN table where every row carries a different string value.");
        note("Pool alternates hit/miss to keep branch predictor honest.");

        System.out.println("  [1] ORIGINAL:");
        long[] oG = timedRuns("orig/ew/pool", n -> {
            for (int i = 0; i < n; i++) { objectSink = endsWithOriginal(POOL_EW[i % POOL_SIZE], SUFFIX); }
        });
        System.out.println("  [2] FIXED:");
        long[] fG = timedRuns("fixed/ew/pool", n -> {
            for (int i = 0; i < n; i++) { objectSink = endsWithFixed(POOL_EW[i % POOL_SIZE], SUFFIX); }
        });
        printSummary(oG, fG, ROWS_PER_RUN);

        // ── Validation ────────────────────────────────────────────────────────
        System.out.println();
        System.out.println("Validation checks (ORIGINAL == FIXED for all inputs):");
        validateSW(HIT_SW,   PREFIX);
        validateSW(MISS_SW,  PREFIX);
        validateSW("",       PREFIX);
        validateSW("hello",  "");
        validateSW("hello",  "hel");
        validateSW("hello",  "ell");
        validateSW("👋🌍🔥", "👋");
        validateSW("👋🌍🔥", "🌍");

        validateEW(HIT_EW,   SUFFIX);
        validateEW(MISS_EW,  SUFFIX);
        validateEW("",       SUFFIX);
        validateEW("hello",  "");
        validateEW("hello",  "llo");
        validateEW("hello",  "hel");
        validateEW("👋🌍🔥", "🔥");
        validateEW("👋🌍🔥", "👋");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void validateSW(String string, String match) {
        FEELFnResult<Boolean> orig  = startsWithOriginal(string, match);
        FEELFnResult<Boolean> fixed = startsWithFixed(string, match);
        Boolean origVal  = orig.cata(e -> null, v -> v);
        Boolean fixedVal = fixed.cata(e -> null, v -> v);
        if (!origVal.equals(fixedVal)) {
            throw new IllegalStateException(String.format(
                "Mismatch! starts with('%s', '%s')  original=%s  fixed=%s",
                string, match, origVal, fixedVal));
        }
        System.out.printf("  ✔ starts with('%s', '%s') = %s%n", string, match, fixedVal);
    }

    private static void validateEW(String string, String match) {
        FEELFnResult<Boolean> orig  = endsWithOriginal(string, match);
        FEELFnResult<Boolean> fixed = endsWithFixed(string, match);
        Boolean origVal  = orig.cata(e -> null, v -> v);
        Boolean fixedVal = fixed.cata(e -> null, v -> v);
        if (!origVal.equals(fixedVal)) {
            throw new IllegalStateException(String.format(
                "Mismatch! ends with('%s', '%s')  original=%s  fixed=%s",
                string, match, origVal, fixedVal));
        }
        System.out.printf("  ✔ ends with('%s', '%s') = %s%n", string, match, fixedVal);
    }

    static long[] timedRuns(String label, Workload workload) {
        System.out.printf("  [%-28s] warming up...%n", label);
        workload.run(WARMUP_ITERS * ROWS_PER_RUN / 10);
        System.gc();
        long[] times = new long[TIMED_RUNS];
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t = System.nanoTime();
            workload.run(ROWS_PER_RUN);
            times[i] = System.nanoTime() - t;
            System.out.printf("    run %d: %,d ms%n", i + 1, times[i] / 1_000_000);
        }
        return times;
    }

    static double median(long[] ns) {
        long[] s = ns.clone();
        Arrays.sort(s);
        int m = s.length / 2;
        return (s.length % 2 == 0 ? (s[m - 1] + s[m]) / 2.0 : s[m]) / 1_000_000.0;
    }

    static double throughput(double medMs, int rows) {
        return rows / (medMs / 1000.0);
    }

    static void header() {
        System.out.println("==========================================================================");
        System.out.println("    FEEL starts with() / ends with() FUNCTION PERFORMANCE BENCHMARK");
        System.out.println("==========================================================================");
        System.out.println();
    }

    static void section(String title) {
        System.out.println();
        System.out.println(title);
        System.out.println(new String(new char[title.length()]).replace("\0", "-"));
    }

    static void note(String msg) {
        System.out.println("  NOTE: " + msg);
    }

    static void printSummary(long[] before, long[] after, int rows) {
        double bMed = median(before);
        double aMed = median(after);
        double bTp  = throughput(bMed, rows);
        double aTp  = throughput(aMed, rows);

        double speedup  = bMed / aMed;
        double savedMs  = bMed - aMed;
        double savedPct = (savedMs / bMed) * 100.0;

        System.out.println();
        System.out.printf("  ┌─ ORIGINAL median : %8.1f ms  (%,10.0f rows/s)%n", bMed, bTp);
        System.out.printf("  ├─ FIXED    median : %8.1f ms  (%,10.0f rows/s)%n", aMed, aTp);
        System.out.printf("  └─ Speedup: %.1fx  |  time saved: %.1f ms (%.1f%%)%n",
                speedup, savedMs, savedPct);
        System.out.println();
    }
}

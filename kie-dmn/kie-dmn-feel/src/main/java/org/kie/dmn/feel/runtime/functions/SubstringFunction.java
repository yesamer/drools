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


import org.kie.dmn.api.feel.runtime.events.FEELEvent.Severity;
import org.kie.dmn.feel.runtime.FEELStringFunction;
import org.kie.dmn.feel.runtime.events.InvalidParametersEvent;

public class SubstringFunction
        extends BaseFEELFunction implements FEELStringFunction {

    public static final SubstringFunction INSTANCE = new SubstringFunction();

    private SubstringFunction() {
        super( "substring" );
    }

    public FEELFnResult<String> invoke(@ParameterName("string") String string, @ParameterName("start position") Number start) {
        return invoke(string, start, null);
    }

    public FEELFnResult<String> invoke(@ParameterName("string") String string, @ParameterName("start position") Number start, @ParameterName("length") Number length) {
        if ( string == null ) {
            return FEELFnResult.ofError( new InvalidParametersEvent( Severity.ERROR, "string", "cannot be null" ) );
        }
        if ( start == null ) {
            return FEELFnResult.ofError( new InvalidParametersEvent( Severity.ERROR, "start position", "cannot be null" ) );
        }
        if ( length != null && length.intValue() <= 0 ) {
            return FEELFnResult.ofError( new InvalidParametersEvent( Severity.ERROR, "length", "must be a positive number when specified" ) );
        }
        if ( start.intValue() == 0 ) {
            return FEELFnResult.ofError( new InvalidParametersEvent( Severity.ERROR, "start position", "cannot be zero" ) );
        }
        int codePointCount = string.codePointCount(0, string.length());
        if ( Math.abs( start.intValue() ) > codePointCount ) {
            return FEELFnResult.ofError( new InvalidParametersEvent( Severity.ERROR, "parameter 'start position' inconsistent with the actual length of the parameter 'string'" ) );
        }

        int charLength = string.length();
        int skip = start.intValue() > 0 ? start.intValue() - 1 : codePointCount + start.intValue();
        int startCharIndex;
        int endCharIndex;

        if (codePointCount == charLength) {
            // Fast-path: BMP (Basic Multilingual Plane) characters only (no surrogate pairs).
            // Every Unicode character fits in exactly one 16-bit Java char (codePointCount == charLength).
            // We can bypass native method calls and perform instant O(1) index calculations.
            startCharIndex = skip;
            if (length != null) {
                long limit = length.longValue();
                endCharIndex = (skip + limit > charLength) ? charLength : (int) (skip + limit);
            } else {
                endCharIndex = charLength;
            }
        } else {
            // Slow-path: contains surrogate pairs (characters outside the BMP, like emojis).
            // These characters occupy two 16-bit Java chars (a high and low surrogate) per code point.
            // We must use offsetByCodePoints to locate correct char indices without splitting surrogate pairs.
            startCharIndex = string.offsetByCodePoints(0, skip);
            if (length != null) {
                long limit = length.longValue();
                if (skip + limit > codePointCount) {
                    endCharIndex = charLength;
                } else {
                    endCharIndex = string.offsetByCodePoints(startCharIndex, (int) limit);
                }
            } else {
                endCharIndex = charLength;
            }
        }
        return FEELFnResult.ofResult(string.substring(startCharIndex, endCharIndex));
    }
}

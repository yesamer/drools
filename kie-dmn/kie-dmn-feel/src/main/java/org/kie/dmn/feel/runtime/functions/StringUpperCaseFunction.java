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

import java.util.Locale;

import org.kie.dmn.api.feel.runtime.events.FEELEvent.Severity;
import org.kie.dmn.feel.runtime.FEELStringFunction;
import org.kie.dmn.feel.runtime.events.InvalidParametersEvent;

public class StringUpperCaseFunction
        extends BaseFEELFunction implements FEELStringFunction {

    public static final StringUpperCaseFunction INSTANCE = new StringUpperCaseFunction();

    private StringUpperCaseFunction() {
        super( "upper case" );
    }

    public FEELFnResult<String> invoke(@ParameterName("string") String string) {
        if ( string == null ) {
            return FEELFnResult.ofError(new InvalidParametersEvent(Severity.ERROR, "string", "cannot be null"));
        } else {
            // Locale.ROOT ensures locale-independent Unicode casing as required by the FEEL/DMN
            // specification (matching fn:upper-case() in XPath/XQuery).  The JDK's
            // toUpperCase(Locale) also returns 'this' when no character changes, avoiding a
            // String allocation for already-uppercase inputs.
            return FEELFnResult.ofResult( string.toUpperCase( Locale.ROOT ) );
        }
    }
}

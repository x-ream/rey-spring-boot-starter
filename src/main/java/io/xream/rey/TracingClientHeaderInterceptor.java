/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
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
package io.xream.rey;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.xream.rey.api.ClientHeaderInterceptor;
import org.springframework.http.HttpHeaders;

/**
 * @author Sim
 */
public class TracingClientHeaderInterceptor implements ClientHeaderInterceptor {

    private Tracer tracer;

    public TracingClientHeaderInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void apply(HttpHeaders httpHeaders) {

        Span span = tracer.scopeManager().activeSpan();
        if (span == null)
            return;
        String traceId = span.context().toTraceId();
        httpHeaders.add("TraceId",traceId);
    }
}

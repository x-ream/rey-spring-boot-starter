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

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.HttpHeadersCarrier;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Deprecated
public class TracingReyClientInterceptor implements ClientHttpRequestInterceptor {
    private static final String SPAN_URI = "uri";

    private Tracer tracer;

    public TracingReyClientInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse httpResponse;

        Span span = tracer.buildSpan("ReyClient-Response")
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(SPAN_URI, request.getURI().toString())
                .start();

        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS,
                new HttpHeadersCarrier(request.getHeaders()));

        try (Scope scope = tracer.activateSpan(span)) {
            httpResponse = execution.execute(request, body);
        } catch (Exception ex) {
            throw ex;
        } finally {
            span.finish();
        }
        return httpResponse;
    }

}
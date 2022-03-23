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
package io.xream.rey.logtrace;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.slf4j.MDC;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * @author Sim
 */
//@Order(0)
//@Component
//@WebFilter(filterName = "traceIdFilter", urlPatterns = "/*")
class LogWithTraceIdFilter implements Filter {

    public static final String TRACE_ID = "traceId";

    @Resource
    private Tracer tracer;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        String path = ((HttpServletRequest) servletRequest).getServletPath();
        if (path.length() > 30) {
            path = path.substring(0, 30);
        }

        Span span = tracer.scopeManager().activeSpan();
        String traceId = span == null ? path : span.context().toTraceId() +"@"+path;
        MDC.put(TRACE_ID, traceId);

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        MDC.clear();
    }
}

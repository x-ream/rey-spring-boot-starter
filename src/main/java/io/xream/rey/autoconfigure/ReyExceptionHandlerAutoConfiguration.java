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
package io.xream.rey.autoconfigure;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.xream.rey.api.exceptionhandler.CallNotPermittedExceptionConverter;
import io.xream.rey.api.exceptionhandler.ClientExceptionProcessSupportable;
import io.xream.rey.api.exceptionhandler.FallbackDeterminate;
import io.xream.rey.api.exceptionhandler.RespondedExceptionConverter;
import io.xream.rey.internal.DefaultClientExceptionProcessSupportable;
import io.xream.rey.internal.DefaultFallbackDeterminate;
import io.xream.rey.internal.DefaultRespondedExceptionConverter;
import io.xream.rey.resilience4j.R4JCallNotPermittedExceptionConverter;
import io.xream.rey.spring.exceptionhandler.CallNotPermittedExceptionHandler;
import io.xream.rey.spring.exceptionhandler.IgnoreFallbackExceptionHandler;
import io.xream.rey.spring.exceptionhandler.ReyInternalExceptionHandler;
import io.xream.rey.spring.exceptionhandler.ReyRuntimeExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;


/**
 * @author Sim
 */
public class ReyExceptionHandlerAutoConfiguration {

    @ConditionalOnBean(CircuitBreakerRegistry.class)
    @Bean
    public CallNotPermittedExceptionHandler callNotPermittedExceptionHandler(){
        return new CallNotPermittedExceptionHandler();
    }
    @Bean
    public IgnoreFallbackExceptionHandler ignoreFallbackExceptionHandler(){
        return new IgnoreFallbackExceptionHandler();
    }

    @Bean
    public ReyInternalExceptionHandler reyInternalExceptionHandler(){
        return new ReyInternalExceptionHandler();
    }

    @Bean
    public ReyRuntimeExceptionHandler reyRuntimeExceptionHandler(){
        return new ReyRuntimeExceptionHandler();
    }

    @Bean
    public ClientExceptionProcessSupportable clientExceptionProcessSupportable() {

        RespondedExceptionConverter respondedExceptionConverter
                = new DefaultRespondedExceptionConverter();

        CallNotPermittedExceptionConverter callNotPermittedExceptionConverter
                = new R4JCallNotPermittedExceptionConverter();

        FallbackDeterminate fallbackDeterminate = new DefaultFallbackDeterminate();

        DefaultClientExceptionProcessSupportable supportable
                = new DefaultClientExceptionProcessSupportable();

        supportable.setRespondedExceptionConverter(respondedExceptionConverter);
        supportable.setCallNotPermittedExceptionConverter(callNotPermittedExceptionConverter);
        supportable.setFallbackDeterminate(fallbackDeterminate);

        return supportable;
    }
}

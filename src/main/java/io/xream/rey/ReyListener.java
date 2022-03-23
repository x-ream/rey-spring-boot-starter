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

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.opentracing.Tracer;
import io.xream.rey.api.ClientHeaderInterceptor;
import io.xream.rey.api.ClientRestTemplate;
import io.xream.rey.api.ReyTemplate;
import io.xream.rey.api.custom.RestTemplateCustomizer;
import io.xream.rey.api.exceptionhandler.ClientExceptionProcessSupportable;
import io.xream.rey.config.ReyConfigurable;
import io.xream.rey.internal.ClientBackend;
import io.xream.rey.internal.ClientBackendImpl;
import io.xream.rey.resilience4j.R4JTemplate;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.client.RestTemplate;

public class ReyListener implements
        ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        backendImpl(event);
        customizeRestTemplate(event);
        wrap(event);
    }


    private void backendImpl(ApplicationStartedEvent event){
        try{
            ClientBackendImpl impl = event
                    .getApplicationContext()
                    .getBean(ClientBackendImpl.class);

            if (impl == null)
                return;

            try{

                ClientExceptionProcessSupportable supportable
                        = event.getApplicationContext()
                        .getBean(ClientExceptionProcessSupportable.class);
                impl.setClientExceptionProcessSupportable(supportable);

            }catch (Exception e){

            }

        }catch (Exception e) {

        }

    }


    private void customizeRestTemplate(ApplicationStartedEvent event) {

        try {
            RestTemplateCustomizer bean = event.getApplicationContext().getBean(RestTemplateCustomizer.class);
            if (bean == null)
                return;
            ClientRestTemplate wrapper = bean.customize();
            if (wrapper != null){
                ClientBackendImpl clientBackend = event.getApplicationContext().getBean(ClientBackendImpl.class);
                clientBackend.setClientRestTemplate(wrapper);
            }

        }catch (Exception e) {

        }

    }

    private void wrap(ApplicationStartedEvent event){
        try{
            RestTemplate restTemplate = restTemplate(event);
            ClientRestTemplate clientRestTemplate = event.getApplicationContext().getBean(ClientRestTemplate.class);
            clientRestTemplate.wrap(restTemplate);
            wrapR4jTemplate(event);
            headerInterceptor(clientRestTemplate,event);
        }catch (Exception e) {

        }
    }

    private void headerInterceptor(ClientRestTemplate wrapper, ApplicationStartedEvent event) {
        try{
            Tracer tracer = event.getApplicationContext().getBean(Tracer.class);
            ClientHeaderInterceptor clientHeaderInterceptor = new TracingClientHeaderInterceptor(tracer);
            wrapper.headerInterceptor(clientHeaderInterceptor);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void wrapR4jTemplate(ApplicationStartedEvent event) {

        try{
            CircuitBreakerRegistry circuitBreakerRegistry = event.getApplicationContext().getBean(CircuitBreakerRegistry.class);
            RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
            ReyTemplate reyTemplate = event.getApplicationContext().getBean(ReyTemplate.class);
            R4JTemplate r4jTemplate = (R4JTemplate) reyTemplate;
            r4jTemplate.wrap(circuitBreakerRegistry,retryRegistry);

            ClientBackend clientBackend = event.getApplicationContext().getBean(ClientBackend.class);
            ClientBackendImpl impl = (ClientBackendImpl) clientBackend;
            impl.setReyTemplate(reyTemplate);

            ReyConfigurable reyConfigurable = event.getApplicationContext().getBean(ReyConfigurable.class);
            impl.setReyConfigurable(reyConfigurable);

        }catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private RestTemplate restTemplate(ApplicationStartedEvent applicationStartedEvent) {
        try {
            RestTemplate restTemplate = applicationStartedEvent.getApplicationContext().getBean(RestTemplate.class);
            return restTemplate;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

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


import io.xream.internal.util.StringUtil;
import io.xream.rey.annotation.ReyClient;
import io.xream.rey.internal.BackendDecoration;
import io.xream.rey.internal.ClientBackendProxy;
import io.xream.rey.internal.ReyParser;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


class ReyClientBeanRegistrar implements EnvironmentAware,ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

        String basePackage = ClassUtils.getPackageName(annotationMetadata.getClassName());

        Set<String> packageSet = new HashSet<>();
        packageSet.add(basePackage);

        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableReyClient.class.getName());

        Object obj = attributes.get("basePackages");
        if (obj != null){
            String[] strArr = (String[]) obj;
            for (String str : strArr){
                packageSet.add(str);
            }
        }

        Set<BeanDefinition> definitionSet = new LinkedHashSet<>();

        ClassPathScanningCandidateComponentProvider scanningProvider = getScanningProvider();
        scanningProvider.setResourceLoader(this.resourceLoader);
        scanningProvider.addIncludeFilter(new AnnotationTypeFilter(ReyClient.class));
        for (String pkg : packageSet) {
            definitionSet.addAll(scanningProvider.findCandidateComponents(pkg));
        }

        for (BeanDefinition bd : definitionSet) {
            GenericBeanDefinition definition = (GenericBeanDefinition) bd;
            AnnotationMetadata am = ((AnnotatedBeanDefinition) bd).getMetadata();
            Map<String, Object> map = am.getAnnotationAttributes(ReyClient.class.getCanonicalName());

            String className = am.getClassName();
            Class clzz = ClassUtils.resolveClassName(className, null);

            ReyParser.init(clzz,
                    urlPattern -> environment.resolvePlaceholders(urlPattern));

            String config = MapUtils.getString(map,"config");
            boolean retry = MapUtils.getBooleanValue(map,"retry");
            BackendDecoration backendDecoration = new BackendDecoration();
            backendDecoration.setServiceName(clzz.getSimpleName());
            backendDecoration.setConfigName(StringUtil.isNullOrEmpty(config) ? "default" : config);
            backendDecoration.setRetry(retry);
            String url = MapUtils.getString(map,"value");
            Class fallback = (Class)map.get("fallback");
            Class<? extends Throwable>[] ignoreExceptions = (Class<? extends Throwable>[])map.get("ignoreExceptions");

            definition.getPropertyValues().add("objectType", clzz);
            definition.getPropertyValues().add("backendDecoration", backendDecoration);

            definition.getPropertyValues().add("service", clzz.getSimpleName());
            definition.getPropertyValues().add("url", url);
            definition.getPropertyValues().add("config", config);
            definition.getPropertyValues().add("fallback", fallback);
            definition.getPropertyValues().add("ignoreExceptions", ignoreExceptions);
            definition.setBeanClass(ClientBackendProxy.class);
            definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);

            registry.registerBeanDefinition(className, definition);
        }

    }

    protected ClassPathScanningCandidateComponentProvider getScanningProvider() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(
                    AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }


    private Environment environment;
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private ResourceLoader resourceLoader;
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}

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


import io.xream.internal.util.ClassFileReader;
import io.xream.rey.annotation.Fallback;
import io.xream.rey.fallback.FallbackParser;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.Set;


class ReyFallbackBeanRegistrar implements ImportBeanDefinitionRegistrar {


    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

        String basePackage = ClassUtils.getPackageName(annotationMetadata.getClassName());

        Set<Class<?>> set = ClassFileReader.getClasses(basePackage);
        try {
            Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableFallback.class.getName());
            Object obj = attributes.get("basePackages");
            if (obj != null) {
                String[] strArr = (String[]) obj;
                for (String str : strArr) {
                    Set<Class<?>> eachSet = ClassFileReader.getClasses(str);
                    set.addAll(eachSet);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for (Class clz : set) {
            Fallback annotation = (Fallback)clz.getAnnotation(Fallback.class);
            if (annotation == null)
                continue;

            FallbackParser.init(annotation.ignoreExceptions(), clz,
                    annotation.fallback());

        }
    }

}

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
package io.xream.rey.configure;

import io.xream.rey.config.ReyConfigurable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.Resource;

/**
 * @author Sim
 */
@EnableConfigurationProperties({ReyProperties.class})
public class ReyPropertiesConfiguration implements ReyConfigurable {

    @Resource
    private ReyProperties reyProperties;

    @Override
    public boolean isCircuitbreakerEnabled(String name) {
        CircuitbreakerProperties properties = reyProperties.getCircuitbreaker().get(name);
        if (properties == null)
            return true;
        return reyProperties.getCircuitbreaker().get(name).isEnabled();
    }
}

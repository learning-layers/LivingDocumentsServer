/**
 * Code contributed to the Learning Layers project
 * http://www.learning-layers.eu
 * Development is partly funded by the FP7 Programme of the European
 * Commission under Grant Agreement FP7-ICT-318209.
 * Copyright (c) 2014, Karlsruhe University of Applied Sciences.
 * For a list of contributors see the AUTHORS file at the top-level directory
 * of this distribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hska.livingdocuments.core.config;

import de.hska.livingdocuments.core.controller.aop.JcrSessionAspect;
import de.hska.livingdocuments.core.service.JcrService;
import de.hska.livingdocuments.core.service.RoleService;
import de.hska.livingdocuments.core.service.SubscriptionService;
import de.hska.livingdocuments.core.service.UserService;
import de.hska.livingdocuments.core.service.impl.JackrabbitService;
import de.hska.livingdocuments.core.service.impl.RoleServiceImpl;
import de.hska.livingdocuments.core.service.impl.SubscriptionServiceImpl;
import de.hska.livingdocuments.core.service.impl.UserServiceImpl;
import org.springframework.boot.context.embedded.MultiPartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

@Configuration
public class CoreConfig {

    @Bean
    public UserService userService() {
        return new UserServiceImpl();
    }

    @Bean
    public RoleService roleService() {
        return new RoleServiceImpl();
    }

    @Bean
    public JcrService jcrService() {
        return new JackrabbitService();
    }

    @Bean
    public SubscriptionService subscriptionService() {
        return new SubscriptionServiceImpl();
    }

    @Bean
    public JcrSessionAspect jcrSessionAspect() {
        return new JcrSessionAspect();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultiPartConfigFactory factory = new MultiPartConfigFactory();
        factory.setMaxFileSize("128MB");
        factory.setMaxRequestSize("128MB");
        return factory.createMultipartConfig();
    }
}

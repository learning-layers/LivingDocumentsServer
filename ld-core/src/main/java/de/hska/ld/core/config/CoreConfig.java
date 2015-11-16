/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2015, Karlsruhe University of Applied Sciences.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.hska.ld.core.config;

import de.hska.ld.core.logging.ExceptionLogger;
import de.hska.ld.core.logging.ExceptionLoggerImpl;
import de.hska.ld.core.service.MailService;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.service.impl.MailServiceImpl;
import de.hska.ld.core.service.impl.RoleServiceImpl;
import de.hska.ld.core.service.impl.UserServiceImpl;
import de.hska.ld.core.util.AsyncExecutor;
import de.hska.ld.core.util.SpringAsyncExecutor;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.servlet.MultipartConfigElement;

@Configuration
@EnableAsync
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
    public MailService mailService() {
        return new MailServiceImpl();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("128MB");
        factory.setMaxRequestSize("128MB");
        return factory.createMultipartConfig();
    }

    @Bean
    public AsyncExecutor asyncExecutor() {
        return new SpringAsyncExecutor();
    }

    @Bean
    public ApplicationContextProvider applicationContextProvder() {
        return new ApplicationContextProvider();
    }

    @Bean
    public ExceptionLogger exceptionLogger() {
        return new ExceptionLoggerImpl();
    }
}

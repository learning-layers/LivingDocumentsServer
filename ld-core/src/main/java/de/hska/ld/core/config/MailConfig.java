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

package de.hska.ld.core.config;

import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${email.host}")
    private String host;

    @Value("${email.port}")
    private Integer port;

    @Value("${email.smtp.auth}")
    private Boolean smtpAuth;

    @Value("${email.smtp.starttls.enable}")
    private Boolean startTlsEnable;

    @Value("${mail.smtp.ssl.trust}")
    private Boolean sslTrust;

    @Value("${email.username}")
    private String username;

    @Value("${email.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailService() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);
        javaMailSender.setUsername(username);
        javaMailSender.setPassword(password);
        javaMailSender.setJavaMailProperties(getMailProperties());
        return javaMailSender;
    }

    @Bean
    public VelocityEngineFactoryBean velocityEngine() {
        VelocityEngineFactoryBean factoryBean = new VelocityEngineFactoryBean();
        Properties properties = new Properties();
        properties.put("resource.loader", "class");
        properties.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        factoryBean.setVelocityProperties(properties);
        return factoryBean;
    }

    private Properties getMailProperties() {
        Properties properties = new Properties();
        //properties.setProperty("mail.transport.protocol", protocol);
        properties.setProperty("mail.smtp.auth", smtpAuth.toString());
        properties.setProperty("mail.smtp.starttls.enable", startTlsEnable.toString());
        properties.setProperty("mail.smtp.ssl.trust", sslTrust.toString());
        return properties;
    }
}

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import java.util.Properties;

@Configuration
public class MailConfig {

    public static final Properties MAIL_PROPERTIES = new Properties();

    @Autowired
    private Environment env;

    @Autowired
    private ApplicationContext context;

    @Bean
    public JavaMailSender javaMailService() {
        String userHome = System.getProperty("user.home");
        String emailCfgLocation = env.getProperty("email.config.file");
        emailCfgLocation = emailCfgLocation.replace("~", userHome);

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        Resource resource = context.getResource("file:" + emailCfgLocation);

        try {
            MAIL_PROPERTIES.load(resource.getInputStream());
            javaMailSender.setHost(MAIL_PROPERTIES.getProperty("email.host"));
            javaMailSender.setPort(Integer.parseInt(MAIL_PROPERTIES.getProperty("email.port")));
            javaMailSender.setUsername(MAIL_PROPERTIES.getProperty("email.username"));
            javaMailSender.setPassword(MAIL_PROPERTIES.getProperty("email.password"));
            javaMailSender.setJavaMailProperties(getMailProperties());
        } catch (Exception e) {
            System.err.println("Java Mail Sender could not be initialized. Maybe the configuration file is not in place.");
        }

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
        properties.setProperty("mail.transport.protocol", "smtps");
        properties.setProperty("mail.smtp.auth", env.getProperty("email.smtp.auth"));
        //properties.setProperty("mail.smtp.starttls.enable", env.getProperty("email.smtp.starttls.enable"));
        properties.setProperty("mail.smtp.ssl.trust", env.getProperty("mail.smtp.ssl.trust"));

        //properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        //properties.setProperty("mail.smtps.debug", "true");

        return properties;
    }
}

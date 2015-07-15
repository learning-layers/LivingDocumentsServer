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

package de.hska.ld.core.service.impl;

import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.MailService;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class MailServiceImpl implements MailService {

    @Autowired
    private VelocityEngine velocityEngine;

    @Autowired
    private Environment env;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private void init() {
        /*String emailCfgLocation = env.getProperty("email.config.file");
        Resource resource = context.getResource(emailCfgLocation);
        try {
            MAIL_PROPERTIES.load(resource.getInputStream());
        } catch (Exception e) {
            System.err.println("Java Mail Sender could not be initialized. Maybe the configuration file is not in place.");
        }*/
    }

    @Override
    public void sendMail(User user, Map<String, Object> templateModel) {
        sendMail(user, DEFAULT_TEMPLATE, templateModel);
    }

    @Override
    public void sendMail(User user, String templateFileName, Map<String, Object> model) {
        sendMail(user.getFullName(), user.getEmail(), templateFileName, model);
    }

    @Override
    public void sendMail(String fullName, String email, String templateFileName, Map<String, Object> model) {
        ;
        if (Boolean.parseBoolean(System.getenv("LDS_EMAIL_ENABLED"))) { // env.getProperty("email.enabled")
            Locale locale = LocaleContextHolder.getLocale();
            ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
            model.put("dear", bundle.getString("email.dear"));
            model.put("fullName", fullName);
            model.put("greeting", bundle.getString("email.greeting"));

            String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                    "templates/mail/" + templateFileName, "UTF-8", model);

            Properties properties = getMailProperties();

            Session session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    System.getenv("LDS_EMAIL_USERNAME"), // MAIL_PROPERTIES.getProperty("email.username")
                                    System.getenv("LDS_EMAIL_PASSWORD") // MAIL_PROPERTIES.getProperty("email.password")
                            );
                        }
                    });

            try {
                MimeMessage message = new MimeMessage(session);
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom(System.getenv("LDS_EMAIL_FROM_NAME")); // MAIL_PROPERTIES.getProperty("email.from.system")
                helper.setTo(email);
                helper.setSubject(model.containsKey("subject") ? (String) model.get("subject") : "");
                helper.setText(text, true);
                Transport.send(message);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    private Properties getMailProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", env.getProperty("email.smtp.auth"));
        properties.put("mail.smtp.starttls.enable", env.getProperty("email.smtp.starttls.enable"));
        properties.put("mail.smtp.host", System.getenv("LDS_EMAIL_HOST")); // MAIL_PROPERTIES.getProperty("email.host")
        properties.put("mail.smtp.port", System.getenv("LDS_EMAIL_PORT")); // MAIL_PROPERTIES.getProperty("email.port")
        return properties;
    }
}

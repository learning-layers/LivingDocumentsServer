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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class MailServiceImpl implements MailService {

    @Autowired
    private MailSender mailSender;

    @Autowired
    private VelocityEngine velocityEngine;

    @Value("${email.from.system}")
    private String fromSystem;

    @Override
    public void sendMail(User user, String templateFileName, Map<String, Object> model) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromSystem);
        simpleMailMessage.setTo(user.getEmail());
        simpleMailMessage.setSubject(model.containsKey("subject") ? (String) model.get("subject") : "");

        Locale locale = LocaleContextHolder.getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        model.put("dear", bundle.getString("vm.dear"));
        model.put("fullName", user.getFullName());
        model.put("greeting", bundle.getString("user.confirmation.greeting"));

        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                "templates/mail/" + templateFileName, "UTF-8", model);
        simpleMailMessage.setText(text);
        mailSender.send(simpleMailMessage);
    }
}

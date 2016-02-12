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

package de.hska.ld.core.events.user;

import de.hska.ld.core.persistence.domain.User;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserEventsPublisher {
    @Autowired
    private ApplicationEventPublisher publisher;

    private String extractAuthenticationInformation() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        return token.getAccessTokenValue();
    }

    public UserLoginEvent sendUserLoginEvent(User user) {
        String accessToken = extractAuthenticationInformation();
        UserLoginEvent event = new UserLoginEvent(user, accessToken);
        try {
            this.publisher.publishEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public UserFirstLoginEvent sendUserFirstLoginEvent(User user) {
        String accessToken = extractAuthenticationInformation();
        UserFirstLoginEvent event = new UserFirstLoginEvent(user, accessToken);
        try {
            this.publisher.publishEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }
}

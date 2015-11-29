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

package de.hska.ld.content.events.document;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DocumentEventsPublisher {
    @Autowired
    private ApplicationEventPublisher publisher;

    private String extractAuthenticationInformation() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        return token.getAccessTokenValue();
    }

    public DocumentCreationEvent sendDocumentCreationEvent(Document document) {
        String accessToken = extractAuthenticationInformation();
        DocumentCreationEvent event = new DocumentCreationEvent(document, accessToken);
        this.publisher.publishEvent(event);
        return event;
    }

    public DocumentReadEvent sendDocumentReadEvent(Document document) {
        String accessToken = extractAuthenticationInformation();
        DocumentReadEvent event = new DocumentReadEvent(document, accessToken);
        this.publisher.publishEvent(event);
        return event;
    }

    public DocumentAddTagEvent sendAddTagEvent(Document document, Tag tag) {
        String accessToken = extractAuthenticationInformation();
        DocumentAddTagEvent event = new DocumentAddTagEvent(document, tag, accessToken);
        this.publisher.publishEvent(event);
        return event;
    }
}

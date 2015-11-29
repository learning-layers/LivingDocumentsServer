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

package de.hska.ld.recommendation.listeners;

import de.hska.ld.content.events.document.DocumentAddTagEvent;
import de.hska.ld.content.events.document.DocumentReadEvent;
import de.hska.ld.content.events.user.UserContentAddTagEvent;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.recommendation.client.SSSClient;
import de.hska.ld.recommendation.persistence.domain.DocumentRecommInfo;
import de.hska.ld.recommendation.service.DocumentRecommInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("ld-recommendation-LDToSSSEventListener")
public class LDToSSSEventListener {

    @Autowired
    private SSSClient sssClient;

    @Autowired
    private DocumentRecommInfoService documentRecommInfoService;

    @Async
    @EventListener
    public void handleDocumentReadEvent(DocumentReadEvent event) throws IOException {
        Document document = (Document) event.getSource();
        DocumentRecommInfo documentRecommInfo = documentRecommInfoService.findByDocument(document);
        if (documentRecommInfo == null) {
            // send current tags of the document to the SSS
            sssClient.performInitialSSSTagLoad(document.getId(), event.getAccessToken());
        }
        event.setResultDocument(document);
    }

    @Async
    @EventListener
    public void handleDocumentAddTagEvent(DocumentAddTagEvent event) throws IOException {
        Document document = (Document) event.getSource();
        Tag tag = event.getTag();
        sssClient.addTagToDocument(document.getId(), tag.getId(), event.getAccessToken());
        event.setResultDocument(document);
    }

    @Async
    @EventListener
    public void handleUserContentAddTagEvent(UserContentAddTagEvent event) throws IOException {
        User user = (User) event.getSource();
        Tag tag = event.getTag();
        sssClient.addTagToUser(user.getId(), tag.getId(), event.getAccessToken());
        event.setResultUser(user);
    }
}

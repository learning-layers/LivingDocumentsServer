/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2016, Karlsruhe University of Applied Sciences.
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

import de.hska.ld.content.dto.DocumentListItemDto;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.domain.Page;

public class DocumentReadListEvent extends ApplicationEvent {

    private static final long serialVersionUID = -1903319969171271551L;

    private Page<DocumentListItemDto> resultList;
    private String accessToken;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DocumentReadListEvent(Object source, String accessToken) {
        super(source);
        this.accessToken = accessToken;
    }

    public Page<DocumentListItemDto> getResultDocumentList() {
        return resultList;
    }

    public void setResultDocument(Page<DocumentListItemDto> resultDocument) {
        this.resultList = resultDocument;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}

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

package de.hska.ld.etherpad.dto;

import java.util.ArrayList;
import java.util.List;

public class ConversationsForCommentsReqDto {
    private String authorId = null;
    private String padId = null;
    private String apiKey = null;
    private List<String> commentIdList = new ArrayList<>();

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getPadId() {
        return padId;
    }

    public void setPadId(String padId) {
        this.padId = padId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> getCommentIdList() {
        return commentIdList;
    }

    public void setCommentIdList(List<String> commentIdList) {
        this.commentIdList = commentIdList;
    }

    @Override
    public String toString() {
        return "EtherpadDocumentUpdateDto{" +
                "authorId='" + authorId + '\'' +
                ", padId='" + padId + '\'' +
                ", apiKey='" + apiKey + '\'' +
                '}';
    }
}

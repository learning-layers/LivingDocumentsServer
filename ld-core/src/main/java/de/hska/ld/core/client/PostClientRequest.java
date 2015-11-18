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

package de.hska.ld.core.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

public class PostClientRequest<T> extends ClientRequest {

    public PostClientRequest(String url, String action) {
        super(url, action);
    }

    public void execute(HttpEntity entity) {
        execute(entity, null);
    }

    public void execute(HttpEntity entity, String accessToken) {
        HttpPost post = new HttpPost(this.url);
        if (accessToken != null) {
            addHeaderInformation(post, accessToken);
        }
        if (entity != null) {
            post.setEntity(entity);
        }
        try {
            HttpResponse response = this.client.execute(post);
            this.processResponse();
            this.response = response;
        } catch (Exception e) {
            this.exceptionLogger.log(this.getLoggingPrefix() + this.action, e);
        }
    }

    @Override
    protected String getLoggingPrefix() {
        return "PostClientRequest>";
    }

    private HttpPost addHeaderInformation(HttpPost post, String accessToken) {
        post.setHeader("Content-type", "application/json");
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Authorization", "Bearer " + accessToken);
        return post;
    }
}

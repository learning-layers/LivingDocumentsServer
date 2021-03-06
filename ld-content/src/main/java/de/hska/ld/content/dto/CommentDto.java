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

package de.hska.ld.content.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hska.ld.content.persistence.domain.Comment;

import java.lang.reflect.Field;

public class CommentDto extends Comment {

    private Long jsonParentId;

    @JsonProperty("jsonParentId")
    public Long getJsonParentId() {
        if (this.getParent() != null) {
            jsonParentId = this.getParent().getId();
            this.setParent(null);
        }
        return jsonParentId;
    }

    @JsonProperty("jsonParentId")
    public void setJsonParentId(Long id) {
        if (this.getParent() != null) {
            jsonParentId = this.getParent().getId();
        } else {
            jsonParentId = id;
        }
    }

    public CommentDto() {
    }

    public CommentDto(Comment comment) {
        try {
            // extract and set values per reflection
            Class subclass = comment.getClass();
            Class superclass = subclass.getSuperclass();
            while (superclass != null) {
                Field[] declaredFields = superclass.getDeclaredFields();
                for (Field commentfield : declaredFields) {
                    commentfield.setAccessible(true);
                    Object obj = commentfield.get(comment);
                    commentfield.set(this, obj);
                }
                superclass = superclass.getSuperclass();
            }
            Field[] declaredFields = comment.getClass().getDeclaredFields();
            for (Field commentfield : declaredFields) {
                commentfield.setAccessible(true);
                Object obj = commentfield.get(comment);
                commentfield.set(this, obj);
            }
            this.setId(comment.getId());
        } catch (IllegalAccessException e) {
            //
        }
        this.getJsonParentId();
    }
}

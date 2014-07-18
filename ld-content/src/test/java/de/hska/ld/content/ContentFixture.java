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

package de.hska.ld.content;

import de.hska.ld.content.persistence.domain.Attachment;
import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;

import java.io.InputStream;
import java.util.UUID;

public class ContentFixture {

    static final String TEST_PDF = "test.pdf";

    public static String TAG_NAME1 = "tagName1";
    public static String TAG_DESCRIPTION1 = "tagDescription1";

    public static Document newDocument() {
        Document document = new Document();
        document.setTitle(UUID.randomUUID().toString());
        document.setDescription(UUID.randomUUID().toString());

        Tag tag = new Tag();
        tag.setName(UUID.randomUUID().toString());
        tag.setDescription(UUID.randomUUID().toString());
        document.getTagList().add(tag);

        Comment comment = new Comment();
        comment.setText(UUID.randomUUID().toString());
        document.getCommentList().add(comment);

        InputStream in = ContentFixture.class.getResourceAsStream("/" + TEST_PDF);
        Attachment attachment = new Attachment(in, TEST_PDF);
        document.getAttachmentList().add(attachment);

        return document;
    }

    public static Comment newComment() {
        Comment comment = new Comment();
        comment.setText(UUID.randomUUID().toString());

        Tag tag = new Tag();
        tag.setName(UUID.randomUUID().toString());
        tag.setDescription(UUID.randomUUID().toString());
        comment.getTagList().add(tag);

        Comment subComment = new Comment();
        subComment.setText(UUID.randomUUID().toString());
        comment.getCommentList().add(subComment);

        return comment;
    }

    public static Tag newTag() {
        return newTag(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    public static Tag newTag(String name, String description) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        return tag;
    }
}

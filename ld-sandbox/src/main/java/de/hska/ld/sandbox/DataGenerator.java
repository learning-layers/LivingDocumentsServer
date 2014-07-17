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

package de.hska.ld.sandbox;

import de.hska.ld.content.persistence.domain.Attachment;
import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

public class DataGenerator {

    @Autowired
    public void init(DocumentService documentService) {
        InputStream in = null;
        try {
            in = DataGenerator.class.getResourceAsStream("/" + "sandbox.pdf");

            Document document = new Document();
            document.setTitle("Sandbox Document");
            document.setDescription("This is the sandbox document");

            Tag tag = new Tag();
            tag.setName("Tag");
            tag.setDescription("Description");
            document.getTagList().add(tag);

            Comment comment = new Comment();
            comment.setText("Text");
            document.getCommentList().add(comment);

            Attachment attachment = new Attachment(in, "sandbox.pdf");
            document.getAttachmentList().add(attachment);

            documentService.save(document);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
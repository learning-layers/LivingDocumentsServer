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

package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface DocumentService extends ContentService<Document> {

    Page<Document> getDocumentsPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    void markAsDeleted(Long id);

    Comment addComment(Long id, Comment comment);

    Document removeComment(Long id, Comment comment);

    void addTag(Long id, Long tagId);

    void removeTag(Long id, Long tagId);

    Document addDiscussionToDocument(Long id, Document discussion);

    Document addAccess(Long id, User user, Access.Permission... permission);

    Document removeAccess(Document document, User user, Access.Permission... permissions);

    Long addAttachment(Long documentId, MultipartFile file, String fileName);

    Long addAttachment(Long documentId, InputStream is, String fileName);

    InputStream getAttachmentSource(Long documentId, int position);

    Page<Tag> getDocumentTagsPage(Long documentId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    Long updateAttachment(Long documentId, Long attachmentId, MultipartFile file, String name);
}

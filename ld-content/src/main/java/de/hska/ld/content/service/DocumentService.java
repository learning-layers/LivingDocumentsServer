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

import de.hska.ld.content.dto.BreadcrumbDto;
import de.hska.ld.content.dto.DiscussionSectionDto;
import de.hska.ld.content.persistence.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface DocumentService extends ContentService<Document> {

    Page<Document> getDocumentsPage(Integer pageNumber, Integer pageSize, String sortDirection,
                                    String sortProperty);

    Page<Document> getDocumentsPage(Integer pageNumber, Integer pageSize, String sortDirection,
                                    String sortProperty, String searchTerm);

    void markAsDeleted(Long id);

    Comment addComment(Long id, Comment comment);

    Document removeComment(Long id, Comment comment);

    Document addTag(Long id, Long tagId);

    Document removeTag(Long id, Long tagId);

    Document addDiscussionToDocument(Long id, Document discussion);

    Long addAttachment(Long documentId, MultipartFile file, String fileName);

    Long addAttachment(Long documentId, InputStream is, String fileName);

    Attachment getAttachment(Long documentId, int position);

    Page<Tag> getDocumentTagsPage(Long documentId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    Long updateAttachment(Long documentId, Long attachmentId, MultipartFile file, String name);

    Attachment updateAttachment(Long documentId, Long attachmentId, Attachment attachment);

    List<Tag> getDocumentTagsList(Long documentId);

    Document addSubscription(Long id, Subscription.Type... type);

    Document removeSubscription(Long id, Subscription.Type... type);

    Page<Document> getDiscussionDocumentsPage(Long documentId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    List<BreadcrumbDto> getBreadcrumbs(Long documentId);

    Page<Attachment> getDocumentAttachmentPage(Long documentId, String attachmentType, String excludedAttachmentTypes, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    Attachment getAttachmentByAttachmentId(Long documentId, Long attachmentId);

    Hyperlink addHyperlink(Long documentId, Hyperlink hyperlink);

    List<Attachment> getDocumentAttachmentList(Long documentId);

    void removeAttachment(Long documentId, Long attachmentId);

    Document saveContainsList(Document d);

    Document addAccess(Long documentId, String userIds, String permissions);

    List<Access> getUsersByPermissions(Long documentId, String permissions);

    Document loadCurrentUserPermissions(Document document);

    Access getCurrentUserPermissions(Long documentId, String permissions);

    Document addExpert(Long documentId, String username);

    Document setAccessAll(Long documentId, boolean accessAll);

    void checkPermission(Document document, Access.Permission permission);

    Document addDiscussionToDocument(Long documentId, DiscussionSectionDto discussionSectionDto);

    Page<Access> getUsersByDocumentPermission(Long documentId, String permissions, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    void removeAccess(Long documentId, Long userId);

    String getAuthorIdForCurrentUser();

    String getGroupPadIdForDocument(Document document);

}

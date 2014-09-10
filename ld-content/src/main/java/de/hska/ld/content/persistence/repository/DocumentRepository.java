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

package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Attachment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends CrudRepository<Document, Long> {

    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN d.accessList al WHERE (al.user = :user OR d.creator = :user OR d.accessAll = true) AND (d.deleted = false OR d.deleted IS NULL)")
    Page<Document> findAll(@Param("user") User user, Pageable pageable);

    @Query("SELECT dtl FROM Document d LEFT JOIN d.tagList dtl WHERE d.id = :documentId AND (dtl.deleted = false OR dtl.deleted IS NULL)")
    Page<Tag> findAllTagsForDocument(@Param("documentId") Long documentId, Pageable pageable);

    @Query("SELECT d FROM Document d LEFT JOIN d.accessList al RIGHT JOIN d.parent p WHERE (al.user = :user OR d.creator = :user  OR d.accessAll = true) AND p.id = :documentId AND (d.deleted = false OR d.deleted IS NULL)")
    Page<Document> findDiscussionsAll(@Param("documentId") Long documentId, @Param("user") User user, Pageable pageable);

    @Query("SELECT atl from Document d LEFT JOIN d.attachmentList atl WHERE d.id = :documentId AND atl.mimeType NOT IN (:excludedMimeTypes) AND (atl.deleted = false OR atl.deleted IS NULL)")
    Page<Attachment> findAttachmentsWithTypeExclusionForDocument(@Param("documentId") Long documentId, @Param("excludedMimeTypes") List<String> excludedAttachmentTypesList, Pageable pageable);

    @Query("SELECT atl from Document d LEFT JOIN d.attachmentList atl WHERE d.id = :documentId AND atl.mimeType IN (:mimeTypes) AND atl.mimeType NOT IN (:excludedMimeTypes) AND (atl.deleted = false OR atl.deleted IS NULL)")
    Page<Attachment> findAttachmentsByTypeWithExclusionForDocument(@Param("documentId") Long documentId, @Param("mimeTypes") List<String> attachmentTypes, @Param("excludedMimeTypes") List<String> excludedAttachmentTypesList, Pageable pageable);
}
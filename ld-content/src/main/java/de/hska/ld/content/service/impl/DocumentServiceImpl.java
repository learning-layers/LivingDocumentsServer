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

package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.*;
import de.hska.ld.content.persistence.repository.DocumentRepository;
import de.hska.ld.content.service.AttachmentService;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.SubscriptionService;
import de.hska.ld.content.service.TagService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DocumentServiceImpl extends AbstractContentService<Document> implements DocumentService {

    @Autowired
    private DocumentRepository repository;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private TagService tagService;

    @Autowired
    private SubscriptionService subscriptionService;

    private Comparator<Content> byDateTime = (c1, c2) -> {
        if (c1.getCreatedAt() == null) {
            return -1;
        } else if (c2.getCreatedAt() == null) {
            return 1;
        } else {
            int compareVal = c2.getCreatedAt().compareTo(c1.getCreatedAt());
            return compareVal;
        }
    };

    @Override
    public Page<Document> getDocumentsPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        User user = Core.currentUser();
        return repository.findAll(user, pageable);
    }

    @Override
    @Transactional
    public Document save(Document document) {
        if (document.getTitle() == null) {
            throw new ValidationException("title");
        }
        Document dbDocument = findById(document.getId());
        User currentUser = Core.currentUser();
        if (dbDocument == null) {
            // TODO more dynamic solution
            document.setAccessList(null);
            document.setAttachmentList(null);
            document.setCommentList(null);
            document.setSubscriptionList(null);
            document.setTagList(null);
            Attachment attachment = new Attachment();
            attachment.setName("maincontent.html");
            attachment.setMimeType("text/html");
            attachment.setSource("".getBytes());
            document.getAttachmentList().add(0, attachment);
        } else {
            // document already present in the database
            boolean isCreator = dbDocument.getCreator().getId().equals(currentUser.getId());
            boolean hasAccess = dbDocument.getAccessList().stream().anyMatch(a -> {
                boolean user = a.getUser().getId().equals(currentUser.getId());
                boolean permission = a.getPermissionList().contains(Access.Permission.WRITE);
                return user && permission;
            });
            if (!hasAccess && !isCreator) {
                throw new UserNotAuthorizedException();
            }
            dbDocument.setModifiedAt(new Date());
            dbDocument.setTitle(document.getTitle());
            dbDocument.setDescription(document.getDescription());
            document = dbDocument;
        }
        return super.save(document);
    }

    @Override
    @Transactional
    public Comment addComment(Long id, Comment comment) {
        Document document = findById(id);
        checkPermission(document, Access.Permission.WRITE);
        document.getCommentList().add(comment);
        comment.setParent(document);
        document = super.save(document);
        Optional<Comment> optional = document.getCommentList().stream().sorted(byDateTime).findFirst();
        return optional.get();
    }

    @Override
    @Transactional
    public Document removeComment(Long id, Comment comment) {
        Document document = findById(id);
        document.getCommentList().remove(comment);
        return super.save(document);
    }

    @Override
    @Transactional
    public void addTag(Long id, Long tagId) {
        Document document = findById(id);
        Tag tag = tagService.findById(tagId);
        checkPermission(document, Access.Permission.WRITE);
        if (!document.getAttachmentList().contains(tag)) {
            document.getTagList().add(tag);
        }
        super.save(document);
    }

    @Override
    @Transactional
    public void removeTag(Long id, Long tagId) {
        Document document = findById(id);
        Tag tag = tagService.findById(tagId);
        document.getTagList().remove(tag);
        super.save(document);
    }

    @Override
    @Transactional
    public Document addDiscussionToDocument(Long id, Document discussion) {
        Document document = findById(id);

        document.getDiscussionList().add(discussion);
        discussion.setParent(document);
        this.save(discussion);
        document = super.save(document);
        document.getDiscussionList().size();

        return document;
    }

    @Override
    @Transactional
    public Document addAccess(Long id, User user, Access.Permission... permissions) {
        Document document = findById(id);
        Access access;
        try {
            access = document.getAccessList().stream().filter(a -> a.getUser().equals(user)).findFirst().get();
            List<Access.Permission> pl = access.getPermissionList();
            for (Access.Permission p : permissions) {
                if (!pl.contains(p)) {
                    pl.add(p);
                }
            }
        } catch (NoSuchElementException e) {
            access = new Access();
            document.getAccessList().add(access);
            access.setUser(user);
            access.getPermissionList().addAll(Arrays.asList(permissions));
        }
        return super.save(document);
    }

    @Override
    @Transactional
    public Document removeAccess(Document document, User user, Access.Permission... permissions) {
        Access access;
        try {
            access = document.getAccessList().stream().filter(a -> a.getUser().equals(user)).findFirst().get();
            List<Access.Permission> pl = access.getPermissionList();
            for (Access.Permission p : permissions) {
                if (pl.contains(p)) {
                    pl.remove(p);
                }
            }
            if (pl.size() == 0) {
                document.getAccessList().remove(access);
            }
        } catch (NoSuchElementException e) {
            // do nothing
        }
        return super.save(document);
    }

    @Override
    public Long addAttachment(Long documentId, MultipartFile file, String fileName) {
        try {
            return addAttachment(documentId, file.getInputStream(), fileName);
        } catch (IOException e) {
            throw new ValidationException("file");
        }
    }

    @Override
    @Transactional
    public Long addAttachment(Long documentId, InputStream is, String fileName) {
        Document document = findById(documentId);
        if (document == null) {
            throw new ValidationException("id");
        }
        checkPermission(document, Access.Permission.WRITE);
        Attachment attachment;
        attachment = new Attachment(is, fileName);
        document.getAttachmentList().add(attachment);
        super.save(document);
        return document.getAttachmentList().get(document.getAttachmentList().size() - 1).getId();
    }

    @Override
    public Attachment getAttachment(Long documentId, int position) {
        Document document = findById(documentId);
        checkPermission(document, Access.Permission.READ);
        if (position < 0 || position >= document.getAttachmentList().size()) {
            throw new ValidationException("position");
        }
        Attachment attachment = document.getAttachmentList().get(position);
        return attachment;
        /*byte[] source = attachment.getSource();
        return new ByteArrayInputStream(source);*/
    }

    @Override
    public Page<Tag> getDocumentTagsPage(Long documentId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        return repository.findAllTagsForDocument(documentId, pageable);
    }

    @Override
    public Long updateAttachment(Long documentId, Long attachmentId, MultipartFile file, String fileName) {
        try {
            return updateAttachment(documentId, attachmentId, file.getInputStream(), fileName);
        } catch (IOException e) {
            throw new ValidationException("file");
        }
    }

    @Override
    public List<Tag> getDocumentTagsList(Long documentId) {
        Document document = findById(documentId);
        return document.getTagList();
    }

    @Override
    public List<Notification> getNotifications(User user) {
        return subscriptionService.deliverSubscriptionItems(user);
    }

    @Override
    @Transactional
    public Document addSubscription(Long id, Subscription.Type... types) {
        User user = Core.currentUser();
        Document document = findById(id);
        if (document == null) {
            throw new NotFoundException("id");
        }
        Subscription subscription = new Subscription(user, types);
        document.getSubscriptionList().add(subscription);
        return save(document);
    }

    @Override
    @Transactional
    public Document removeSubscription(Long id, Subscription.Type... types) {
        User user = Core.currentUser();
        Document document = findById(id);
        if (document == null) {
            throw new NotFoundException("id");
        }
        Subscription subscription = document.getSubscriptionList().stream()
                .filter(s -> s.getUser().equals(user)).findFirst().get();
        List<Subscription.Type> stl = subscription.getTypeList();
        for (Subscription.Type st : types) {
            if (stl.contains(st)) {
                stl.remove(st);
            }
        }
        if (stl.size() == 0) {
            document.getSubscriptionList().remove(subscription);
        }
        return save(document);
    }

    @Override
    public Page<Document> getDiscussionDocumentsPage(Long documentId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        User user = Core.currentUser();
        return repository.findDiscussionsAll(documentId, user, pageable);
    }

    @Transactional
    private Long updateAttachment(Long documentId, Long attachmentId, InputStream is, String fileName) {
        Document document = findById(documentId);
        if (document == null) {
            throw new ValidationException("id");
        }
        checkPermission(document, Access.Permission.WRITE);
        Attachment attachment = attachmentService.findById(attachmentId);
        // check if attachment belongs to the document
        if (!document.getAttachmentList().contains(attachment)) {
            throw new UserNotAuthorizedException();
        }
        // update attachment
        attachment.setNewValues(is, fileName);
        attachment = attachmentService.save(attachment);
        return attachment.getId();
    }

    private void checkPermission(Document document, Access.Permission permission) {
        User user = Core.currentUser();
        if (!document.getCreator().equals(user)) {
            try {
                Access access = document.getAccessList().stream().filter(a -> a.getUser().equals(user)).findFirst().get();
                Access.Permission result = access.getPermissionList().stream().filter(p -> p.equals(permission)).findFirst().get();
                if (result == null) {
                    throw new UserNotAuthorizedException();
                }
            } catch (NoSuchElementException e) {
                throw new UserNotAuthorizedException();
            }
        }
    }

    private void createSubscriptionItems(Long documentId, Subscription.Type type, List<Subscription> subscriptionList) {
        subscriptionList.stream().forEach(s -> {
            if (s.getTypeList().contains(type)) {
                subscriptionService.saveItem(s.getUser().getId(), documentId);
            }
        });
    }

    @Override
    public Document loadContentCollection(Document document, Class... clazzArray) {
        document = super.loadContentCollection(document, clazzArray);
        for (Class clazz : clazzArray) {
            if (Attachment.class.equals(clazz)) {
                document.getAttachmentList().size();
            }
        }
        return document;
    }

    @Override
    public DocumentRepository getRepository() {
        return repository;
    }
}

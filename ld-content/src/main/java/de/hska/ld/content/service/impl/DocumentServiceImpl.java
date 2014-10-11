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

import de.hska.ld.content.dto.BreadcrumbDto;
import de.hska.ld.content.dto.DiscussionSectionDto;
import de.hska.ld.content.persistence.domain.*;
import de.hska.ld.content.persistence.repository.DocumentRepository;
import de.hska.ld.content.service.*;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.service.annotation.Logging;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class DocumentServiceImpl extends AbstractContentService<Document> implements DocumentService {

    @Autowired
    private DocumentRepository repository;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private TagService tagService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

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
        return getDocumentsPage(pageNumber, pageSize, sortDirection, sortProperty, null);
    }

    @Override
    public Page<Document> getDocumentsPage(Integer pageNumber, Integer pageSize, String sortDirection,
                                           String sortProperty, String searchTerm) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        User user = Core.currentUser();
        if (searchTerm == null || searchTerm.isEmpty()) {
            return repository.findAll(user, pageable);
        } else {
            return repository.searchDocumentByTitleOrDescription(searchTerm, pageable);
        }
    }

    @Override
    @Logging("Save document auditing example")
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
            checkPermission(dbDocument, Access.Permission.WRITE);
            dbDocument.setModifiedAt(new Date());
            dbDocument.setTitle(document.getTitle());
            dbDocument.setDescription(document.getDescription());
            if (document.isAccessAll()) {
                dbDocument.setAccessAll(true);
            }
            document = dbDocument;
        }
        createNotifications(document, Subscription.Type.MAIN_CONTENT);

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
        createNotifications(document, Subscription.Type.COMMENT);
        Optional<Comment> optional = document.getCommentList().stream().sorted(byDateTime).findFirst();

        commentService.sendMentionNotifications(comment);
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
    public Document addTag(Long id, Long tagId) {
        Document document = findById(id);
        Tag tag = tagService.findById(tagId);
        checkPermission(document, Access.Permission.WRITE);
        if (!document.getTagList().contains(tag)) {
            document.getTagList().add(tag);
        }
        return super.save(document);
    }

    @Override
    @Transactional
    public Document removeTag(Long id, Long tagId) {
        Document document = findById(id);
        Tag tag = tagService.findById(tagId);
        document.getTagList().remove(tag);
        return super.save(document);
    }

    @Override
    @Transactional
    public Document addDiscussionToDocument(Long id, Document discussion) {
        Document document = findById(id);

        document.getDiscussionList().add(discussion);
        discussion.setParent(document);
        this.save(discussion);
        document = super.save(document);
        createNotifications(document, Subscription.Type.DISCUSSION);
        document.getDiscussionList().size();

        return document;
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
        document = super.save(document);
        createNotifications(document, Subscription.Type.ATTACHMENT);
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
    @Transactional
    public Attachment updateAttachment(Long documentId, Long attachmentId, Attachment attachment) {
        Document document = findById(documentId);
        if (document == null) {
            throw new NotFoundException("documentId");
        }
        document.getAttachmentList().size();
        boolean found = false;
        for (Attachment attachmentItem : document.getAttachmentList()) {
            if (attachmentItem.getId().equals(attachmentId)) {
                found = true;
            }
        }
        if (!found) {
            throw new NotFoundException("attachmentId");
        }
        Attachment dbAttachment = attachmentService.findById(attachmentId);
        dbAttachment.setName(attachment.getName());
        return attachmentService.save(dbAttachment);
    }

    @Override
    public List<Tag> getDocumentTagsList(Long documentId) {
        Document document = findById(documentId);
        return document.getTagList();
    }

    @Override
    @Transactional
    public Document addSubscription(Long id, Subscription.Type... types) {
        User user = Core.currentUser();
        Document document = findById(id);
        if (document == null) {
            throw new NotFoundException("id");
        }
        checkPermission(document, Access.Permission.READ);

        Subscription subscription = null;
        if (document.getSubscriptionList().size() != 0) {
            // retrieve subscription if already present for the current user
            subscription = document.getSubscriptionList().stream()
                    .filter(s -> s.getUser().equals(user)).findFirst().get();
        }
        if (subscription != null) {
            // if the subscription was already present on the document
            List<Subscription.Type> typeList = Arrays.asList(types);
            // remove all the types that shall be added (to prevent duplicates)
            subscription.getTypeList().removeAll(typeList);
            // add all types
            subscription.getTypeList().addAll(typeList);
        } else {
            // add a new subscription
            subscription = new Subscription(user, types);
            document.getSubscriptionList().add(subscription);
        }
        return super.save(document);
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

    @Override
    public List<BreadcrumbDto> getBreadcrumbs(Long documentId) {
        Document document = findById(documentId);
        List<BreadcrumbDto> breadcrumbList = new ArrayList<>();
        if (document != null) {
            // add the document itself to the breadcrumbs
            BreadcrumbDto breadcrumbDto = new BreadcrumbDto();
            breadcrumbDto.setCurrent(true);
            breadcrumbDto.setDocumentId(document.getId());
            breadcrumbDto.setDocumentTitle(document.getTitle());
            breadcrumbList.add(breadcrumbDto);
            // retrieve parent document information
            Document currentParent = document.getParent();
            while (currentParent != null) {
                BreadcrumbDto parentBreadcrumbDto = new BreadcrumbDto();
                parentBreadcrumbDto.setCurrent(false);
                parentBreadcrumbDto.setDocumentId(currentParent.getId());
                parentBreadcrumbDto.setDocumentTitle(currentParent.getTitle());
                breadcrumbList.add(parentBreadcrumbDto);
                currentParent = currentParent.getParent();
            }
        }
        return breadcrumbList;
    }

    @Override
    public Page<Attachment> getDocumentAttachmentPage(Long documentId, String attachmentType, String excludedAttachmentTypes, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        List<String> attachmentTypes = Arrays.asList(attachmentType.split(";"));
        if (attachmentTypes.size() == 0) {
            attachmentTypes.add("NoValue");
        }
        List<String> excludedAttachmentTypesList;
        if (!"".equals(excludedAttachmentTypes)) {
            excludedAttachmentTypesList = Arrays.asList(excludedAttachmentTypes.split(";"));
        } else {
            excludedAttachmentTypesList = new ArrayList<>();
            excludedAttachmentTypesList.add("NoValue");
        }
        if ("all".equals(attachmentType)) {
            return repository.findAttachmentsWithTypeExclusionForDocument(documentId, excludedAttachmentTypesList, pageable);
        } else {
            return repository.findAttachmentsByTypeWithExclusionForDocument(documentId, attachmentTypes, excludedAttachmentTypesList, pageable);
        }
    }

    @Override
    @Transactional
    public Attachment getAttachmentByAttachmentId(Long documentId, Long attachmentId) {
        Document document = findById(documentId);
        checkPermission(document, Access.Permission.READ);
        Attachment attachment = attachmentService.findById(attachmentId);
        if (attachment != null && !document.getAttachmentList().contains(attachment)) {
            throw new UserNotAuthorizedException();
        }
        return attachment;
    }

    @Override
    @Transactional
    public Hyperlink addHyperlink(Long documentId, Hyperlink hyperlink) {
        Document document = findById(documentId);
        checkPermission(document, Access.Permission.WRITE);
        document.getHyperlinkList().add(hyperlink);
        super.save(document);
        return hyperlink;
    }

    @Override
    @Transactional
    public List<Attachment> getDocumentAttachmentList(Long documentId) {
        Document document = findById(documentId);
        if (document == null) {
            throw new NotFoundException("documentId");
        }
        this.loadContentCollection(document, Attachment.class);
        return document.getAttachmentList();
    }

    @Override
    @Transactional
    public void removeAttachment(Long documentId, Long attachmentId) {
        Document document = findById(documentId);
        if (document == null) {
            throw new NotFoundException("documentId");
        }
        Attachment attachment = attachmentService.findById(attachmentId);
        if (attachment == null) {
            throw new NotFoundException("attachmentId");
        }
        if (!document.getAttachmentList().contains(attachment)) {
            throw new UserNotAuthorizedException();
        }
        attachment.setDeleted(true);
        attachmentService.save(attachment);
    }

    @Override
    public Document saveContainsList(Document d) {
        return super.save(d);
    }

    @Override
    public Document addAccess(Long documentId, String combinedUserIdString, String combinedPermissionString) {
        Document document = findById(documentId);
        List<User> userList;
        try {
            String[] userIdStringArray = combinedUserIdString.split(";");
            userList = new ArrayList<>();
            for (String userIdString : userIdStringArray) {
                Long userId = Long.valueOf(userIdString);
                User user = userService.findById(userId);
                userList.add(user);
            }
        } catch (Exception e) {
            throw new ValidationException("userIdString");
        }
        List<Access.Permission> permissionList;
        try {
            String[] permissionStringArray = combinedPermissionString.split(";");
            permissionList = new ArrayList<>();
            for (String permissionString : permissionStringArray) {
                Access.Permission permission = Access.Permission.valueOf(permissionString);
                permissionList.add(permission);
            }
        } catch (Exception e) {
            throw new ValidationException("permissionString");
        }
        addAccess(documentId, userList, permissionList);
        return document;
    }

    @Override
    public List<Access> getUsersByPermissions(Long documentId, String combinedPermissionString) {
        Document document = findById(documentId);
        if (document == null) {
            throw new NotFoundException("documentId");
        }
        if ("all".equals(combinedPermissionString)) {
            combinedPermissionString = "WRITE;READ";
        }
        List<Access.Permission> permissionList;
        try {
            String[] permissionStringArray = combinedPermissionString.split(";");
            permissionList = new ArrayList<>();
            for (String permissionString : permissionStringArray) {
                Access.Permission permission = Access.Permission.valueOf(permissionString);
                permissionList.add(permission);
            }
        } catch (Exception e) {
            throw new ValidationException("permissionString");
        }
        List<Access> resultUsers = createUserAccessDtoList(document, permissionList);
        return resultUsers;
    }

    private List<Access> createUserAccessDtoList(Document document, List<Access.Permission> permissionList) {
        List<Access> resultAccess = new ArrayList<>();
        for (Access access : document.getAccessList()) {
            for (Access.Permission permission : permissionList) {
                if (access.getPermissionList().contains(permission) && !resultAccess.contains(access)) {
                    resultAccess.add(access);
                }
            }
        }
        return resultAccess;
    }

    @Override
    public Document loadCurrentUserPermissions(Document document) {
        List<Access> access = repository.getCurrentUserPermissionsForDocument(document.getId(), Core.currentUser().getId());
        document.setAccessList(access);
        return document;
    }

    @Override
    @Transactional(readOnly = true)
    public Access getCurrentUserPermissions(Long documentId, String permissions) {
        Document document = findById(documentId);
        if (document == null) {
            throw new NotFoundException("documentId");
        }
        if (document.getCreator().equals(Core.currentUser())) {
            return null;
        }
        List<Access> accessList = getUsersByPermissions(documentId, permissions);
        Access tempAccess = new Access();
        tempAccess.setUser(Core.currentUser());
        int idx = accessList.indexOf(tempAccess);
        if (idx != -1) {
            return accessList.get(idx);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public Document addExpert(Long documentId, String username) {
        Document document = findById(documentId);
        if (document == null) {
            throw new NotFoundException("documentId");
        }
        User expert = userService.findByUsername(username);
        if (expert == null) {
            throw new NotFoundException("username");
        }
        document.getExpertList().add(expert);
        return save(document);
    }

    @Override
    @Transactional
    public Document setAccessAll(Long documentId, boolean accessAll) {
        Document document = findById(documentId);
        checkPermission(document, Access.Permission.WRITE);
        document.setAccessAll(accessAll);
        return save(document);
    }

    @Override
    @Transactional
    public Document addDiscussionToDocument(Long documentId, DiscussionSectionDto discussionSectionDto) {
        Document document = addDiscussionToDocument(documentId, discussionSectionDto.getDocument());
        Document discussion = document.getDiscussionList().get(document.getDiscussionList().size() - 1);
        if (discussionSectionDto.getSectionText() != null) {
            Attachment mainAttachment = discussion.getAttachmentList().get(0);
            mainAttachment.setSource(discussionSectionDto.getSectionText().getBytes());
        }
        return discussion;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Access> getUsersByDocumentPermission(Long documentId, String combinedPermissionString, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Document document = findById(documentId);
        if (document == null) {
            throw new NotFoundException("documentId");
        }
        if ("all".equals(combinedPermissionString)) {
            combinedPermissionString = "WRITE;READ";
        }
        List<Access.Permission> permissionList;
        try {
            String[] permissionStringArray = combinedPermissionString.split(";");
            permissionList = new ArrayList<>();
            for (String permissionString : permissionStringArray) {
                Access.Permission permission = Access.Permission.valueOf(permissionString);
                permissionList.add(permission);
            }
        } catch (Exception e) {
            throw new ValidationException("permissionString");
        }
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        Page<Access> usersByDocumentPermission = repository.getUsersByDocumentPermission(documentId, permissionList, pageable);
        for (Access access : usersByDocumentPermission) {
            access.getPermissionList().size();
        }
        return usersByDocumentPermission;
    }

    @Override
    @Transactional
    public void removeAccess(Long documentId, Long userId) {
        Document document = findById(documentId);
        User user = userService.findById(userId);
        Access userAccess = new Access();
        userAccess.setUser(user);
        document.getAccessList().remove(userAccess);
        save(document);
    }

    public void addAccess(Long documentId, List<User> userList, List<Access.Permission> permissionList) {
        Document document = findById(documentId);
        for (User user : userList) {
            Access.Permission[] permissionArray = permissionList.toArray(new Access.Permission[permissionList.size()]);
            addAccess(document.getId(), user, permissionArray);
        }
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
        createNotifications(document, Subscription.Type.ATTACHMENT);
        return attachment.getId();
    }

    private void createNotifications(Document document, Subscription.Type type) {
        User editor = Core.currentUser();
        Long documentId = document.getId();
        List<Subscription> subscriptionList = document.getSubscriptionList();
        subscriptionList.stream().forEach(s -> {
            if (s.getTypeList().contains(type)) {
                if (!s.getUser().getId().equals(editor.getId())) {
                    subscriptionService.saveNotification(documentId, s.getUser().getId(), editor.getId(), type);
                }
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Document loadContentCollection(Document document, Class... clazzArray) {
        document = super.loadContentCollection(document, clazzArray);
        if (document != null) {
            for (Class clazz : clazzArray) {
                if (Attachment.class.equals(clazz)) {
                    document.setAttachmentList(filterDeletedListItems(document.getAttachmentList(), Attachment.class));
                }
                if (User.class.equals(clazz)) {
                    document.getExpertList().size();
                }
            }
        }
        return document;
    }

    @Override
    public DocumentRepository getRepository() {
        return repository;
    }
}

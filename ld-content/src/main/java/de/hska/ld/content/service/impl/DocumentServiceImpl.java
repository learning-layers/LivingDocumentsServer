package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.*;
import de.hska.ld.content.persistence.repository.DocumentRepository;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.TagService;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DocumentServiceImpl extends AbstractContentService<Document> implements DocumentService {

    @Autowired
    private DocumentRepository repository;

    @Autowired
    private TagService tagService;

    private Comparator<Content> byDateTime = (c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt());

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
        } else {
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
        document.getTagList().add(tag);
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
    public void addAttachment(Long documentId, MultipartFile file, String fileName) {
        try {
            addAttachment(documentId, file.getInputStream(), fileName);
        } catch (IOException e) {
            throw new ValidationException("file");
        }
    }

    @Override
    @Transactional
    public void addAttachment(Long documentId, InputStream is, String fileName) {
        Document document = findById(documentId);
        if (document == null) {
            throw new ValidationException("id");
        }
        checkPermission(document, Access.Permission.WRITE);
        Attachment attachment;
        attachment = new Attachment(is, fileName);
        document.getAttachmentList().add(attachment);
        super.save(document);
    }

    @Override
    public InputStream getAttachmentSource(Long documentId, int position) {
        Document document = findById(documentId);
        checkPermission(document, Access.Permission.READ);
        if (position < 0 || position >= document.getAccessList().size()) {
            throw new ValidationException("position");
        }
        Attachment attachment = document.getAttachmentList().get(position);
        byte[] source = attachment.getSource();
        return new ByteArrayInputStream(source);
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

    @Override
    public DocumentRepository getRepository() {
        return repository;
    }
}

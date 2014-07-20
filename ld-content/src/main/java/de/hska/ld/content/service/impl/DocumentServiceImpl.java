package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.*;
import de.hska.ld.content.persistence.repository.DocumentRepository;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class DocumentServiceImpl extends AbstractContentService<Document> implements DocumentService {

    @Autowired
    private DocumentRepository repository;

    private Comparator<Content> byDateTime = (c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt());

    @Override
    public Page<Document> getDocumentPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
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
        Document dbDocument = findById(document.getId());
        User currentUser = Core.currentUser();
        if (dbDocument == null) {
            document.setCreator(currentUser);
            document.setCreatedAt(new Date());
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
        User user = Core.currentUser();
        Document document = findById(id);
        if (hasPermission(document, user, Access.Permission.WRITE)) {
            document.getCommentList().add(comment);
            document = super.save(document);
            Optional<Comment> optional = document.getCommentList().stream().sorted(byDateTime).findFirst();
            return optional.get();
        } else {
            throw new UserNotAuthorizedException();
        }
    }

    @Override
    @Transactional
    public void addTag(Long id, Tag tag) {
        Document document = findById(id);
        User user = Core.currentUser();
        if (hasPermission(document, user, Access.Permission.WRITE)) {
            document.getTagList().add(tag);
            super.save(document);
        } else {
            throw new UserNotAuthorizedException();
        }
    }

    @Override
    public void removeTag(Long id, Tag tag) {
        Document document = findById(id);
        document.getTagList().remove(tag);
        super.save(document);
    }

    @Override
    public Document addAccess(Document document, User user, Access.Permission... permissions) {
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

    private boolean hasPermission(Document document, User user, Access.Permission permission) {
        if (document.getCreator().equals(user)) {
            return true;
        }
        try {
            Access access = document.getAccessList().stream().filter(a -> a.getUser().equals(user)).findFirst().get();
            Access.Permission result = access.getPermissionList().stream().filter(p -> p.equals(permission)).findFirst().get();
            return result != null;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public DocumentRepository getRepository() {
        return repository;
    }
}

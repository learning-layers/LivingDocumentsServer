package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.*;
import de.hska.ld.content.persistence.repository.DocumentRepository;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class DocumentServiceImpl extends AbstractContentService<Document> implements DocumentService {

    @Autowired
    private DocumentRepository repository;

    @Override
    @Transactional
    public Document save(Document document) {
        Document dbDocument = findById(document.getId());
        User currentUser = Core.currentUser();
        if (dbDocument == null) {
            document.setCreator(currentUser);
            document.setCreatedAt(new Date());
        } else {
            boolean isCreator = dbDocument.getCreator().getId().equals(currentUser.getId());
            boolean hasAccess = dbDocument.getAccessList().stream().anyMatch(a -> {
                boolean user = a.getUser().getId().equals(currentUser.getId());
                boolean permission = a.getPermission().equals(Access.Permission.WRITE);
                return user && permission;
            });
            if (!hasAccess && !isCreator) {
                throw new UserNotAuthorizedException();
            }
            dbDocument.setModifiedAt(new Date());
            dbDocument.setTitle(document.getTitle());
            dbDocument.setDescription(document.getDescription());
            dbDocument.setSubscriberList(document.getSubscriberList());
            dbDocument.setAccessList(document.getAccessList());
            document = dbDocument;
        }
        /*document.setTagList(prepareContentList(document.getTagList()));
        document.setCommentList(prepareContentList(document.getCommentList()));
        document.setAttachmentList(prepareContentList(document.getAttachmentList()));*/
        return super.save(document);
    }

    @Override
    public void markAsDeleted(Long id) {
        Document document = findById(id);
        document.setDeleted(true);
        super.save(document);
    }

    /*private <T extends Content> List<T> prepareContentList(List<T> contentList) {
        User currentUser = Core.currentUser();
        Date now = new Date();
        contentList.stream().forEach(c -> {
            if (c.getId() == null) {
                c.setCreator(currentUser);
                c.setCreatedAt(now);
            } else {
                c.setModifiedAt(now);
            }
        });
        return contentList;
    }*/

    private Comparator<Content> byDateTime = (c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt());

    @Override
    @Transactional
    public Comment addComment(Long id, Comment comment) {
        Document document = findById(id);
        document.getCommentList().add(comment);
        document = super.save(document);

        Optional<Comment> optional = document.getCommentList().stream().sorted(byDateTime).findFirst();
        return optional.get();
    }

    @Override
    @Transactional
    public void addTag(Long id, Tag tag) {
        Document document = findById(id);
        document.getTagList().add(tag);
        super.save(document);
    }

    @Override
    @Transactional
    public void removeTag(Long id, Tag tag) {
        Document document = findById(id);
        document.getTagList().remove(tag);
        super.save(document);
    }

    @Override
    public DocumentRepository getRepository() {
        return repository;
    }
}

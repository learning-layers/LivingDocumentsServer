package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.core.service.Service;

public interface DocumentService extends Service<Document> {

    void markAsDeleted(Long id);

    Comment addComment(Long id, Comment comment);
}

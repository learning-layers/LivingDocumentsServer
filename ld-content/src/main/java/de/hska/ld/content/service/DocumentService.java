package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.core.service.Service;

public interface DocumentService extends ContentService<Document> {

    void markAsDeleted(Long id);

    Comment addComment(Long id, Comment comment);
}

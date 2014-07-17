package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Content;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.ContentService;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.impl.AbstractService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractContentService<T extends Content> extends AbstractService<T> implements ContentService<T> {

    @Override
    @Transactional
    public T loadContentCollection(T t, Class... clazzArray) {
        t = findById(t.getId());
        for (Class clazz : clazzArray) {
            if (Tag.class.equals(clazz)) {
                t.getTagList().size();
            } else if (User.class.equals(clazz)) {
                t.getSubscriberList().size();
            } else if (Comment.class.equals(clazz)) {
                t.getCommentList().size();
            }
        }
        return t;
    }

    @Override
    public <R> CrudRepository<R, Long> getRepository() {
        return null;
    }
}

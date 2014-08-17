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

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Content;
import de.hska.ld.content.persistence.domain.Subscription;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.ContentService;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.impl.AbstractService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class AbstractContentService<T extends Content> extends AbstractService<T> implements ContentService<T> {

    @Override
    @Transactional
    public void markAsDeleted(Long id) {
        T t = findById(id);
        t.setDeleted(true);
        super.save(t);
    }

    @Override
    @Transactional
    public T loadContentCollection(T t, Class... clazzArray) {
        t = findById(t.getId());
        for (Class clazz : clazzArray) {
            if (Tag.class.equals(clazz)) {
                t.getTagList().size();
            } else if (User.class.equals(clazz)) {
                t.getSubscriptionList().size();
            } else if (Comment.class.equals(clazz)) {
                t.getCommentList().size();
            }
        }
        return t;
    }

    @Override
    @Transactional
    public T addSubscription(Long id, User user, Subscription.Type... types) {
        T t = findById(id);
        Subscription subscription = new Subscription(user, types);
        t.getSubscriptionList().add(subscription);
        return save(t);
    }

    @Override
    @Transactional
    public T removeSubscription(Long id, User user, Subscription.Type... types) {
        T t = findById(id);
        Subscription subscription = t.getSubscriptionList().stream().filter(s -> s.getUser().equals(user)).findFirst().get();
        List<Subscription.Type> stl = subscription.getTypeList();
        for (Subscription.Type st : types) {
            if (stl.contains(st)) {
                stl.remove(st);
            }
        }
        if (stl.size() == 0) {
            t.getSubscriptionList().remove(subscription);
        }
        return save(t);
    }

    @Override
    public <R> CrudRepository<R, Long> getRepository() {
        return null;
    }
}

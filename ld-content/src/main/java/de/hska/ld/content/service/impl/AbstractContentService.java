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

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Content;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.ContentService;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.impl.AbstractService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
                t.setTagList((List<Tag>) filterDeletedListItems(t.getTagList(), clazz));
            } else if (Comment.class.equals(clazz)) {
                t.getCommentList().size();
                t.setCommentList((List<Comment>) filterDeletedListItems(t.getTagList(), clazz));
            }
        }
        return t;
    }

    @Override
    @Transactional
    public T addAccess(Long contentId, User user, Access.Permission... permissions) {
        Access access;
        T t = findById(contentId);
        try {
            access = t.getAccessList().stream().filter(a -> a.getUser().equals(user)).findFirst().get();
            List<Access.Permission> pl = access.getPermissionList();
            for (Access.Permission p : permissions) {
                if (!pl.contains(p)) {
                    pl.add(p);
                }
            }
        } catch (NoSuchElementException e) {
            access = new Access();
            t.getAccessList().add(access);
            access.setUser(user);
            access.getPermissionList().addAll(Arrays.asList(permissions));
        }
        return super.save(t);
    }

    @Override
    @Transactional
    public T removeAccess(Long contentId, User user, Access.Permission... permissions) {
        Access access;
        T t = findById(contentId);
        try {
            access = t.getAccessList().stream().filter(a -> a.getUser().equals(user)).findFirst().get();
            List<Access.Permission> pl = access.getPermissionList();
            for (Access.Permission p : permissions) {
                if (pl.contains(p)) {
                    pl.remove(p);
                }
            }
            if (pl.size() == 0) {
                t.getAccessList().remove(access);
            }
        } catch (NoSuchElementException e) {
            // do nothing
        }
        return super.save(t);
    }

    public <I> List<? extends Content> filterDeletedListItems(List tList, Class<I> clazz) {
        if (Content.class.isAssignableFrom(clazz)) {
            if (tList.size() > 0) {
                List<? extends Content> cList = tList;
                List<? extends Content> filteredCList = cList.stream().filter(tItem -> !tItem.isDeleted()).collect(Collectors.toList());
                return filteredCList;
            }
        }
        return tList;
    }

    @Override
    public <R> CrudRepository<R, Long> getRepository() {
        return null;
    }
}

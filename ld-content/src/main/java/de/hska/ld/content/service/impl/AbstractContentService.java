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
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.service.impl.AbstractService;
import de.hska.ld.core.util.Core;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractContentService<T extends Content> extends AbstractService<T> implements ContentService<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContentService.class);

    @Autowired
    private RoleService roleService;

    @Override
    @Transactional
    public T save(T t) {
        return super.save(t);
    }

    @Override
    @Transactional
    public void markAsDeleted(Long id) {
        T t = findById(id);
        t.setDeleted(true);
        super.save(t);
    }

    @Override
    @Transactional(readOnly = true)
    public T loadContentCollection(T t, Class... clazzArray) {
        t = findById(t.getId());
        for (Class clazz : clazzArray) {
            if (Tag.class.equals(clazz)) {
                t.getTagList().size();
                t.setTagList(filterDeletedListItems(t.getTagList(), Tag.class));
            } else if (Comment.class.equals(clazz)) {
                t.getCommentList().size();
                t.setCommentList(filterDeletedListItems(t.getCommentList(), Comment.class));
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

    public void checkPermission(T t, Access.Permission permission) {
        User user = Core.currentUser();
        if (!t.isAccessAll() && !t.getCreator().equals(user)) {
            try {
                Access access = t.getAccessList().stream().filter(a -> a.getUser().equals(user)).findFirst().get();
                Access.Permission result = access.getPermissionList().stream().filter(p -> p.equals(permission)).findFirst().get();
                if (result == null) {
                    throw new UserNotAuthorizedException();
                }
            } catch (NoSuchElementException e) {
                throw new UserNotAuthorizedException();
            }
        }
    }

    public boolean checkPermissionResult(T t, Access.Permission permission) {
        try {
            checkPermission(t, permission);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public <I> List<I> filterDeletedListItems(List tList, Class<I> clazz) {
        if (Content.class.isAssignableFrom(clazz) && tList.size() > 0) {
            List<I> cList = tList;
            List<I> filteredCList = cList.stream().filter(cItem -> !((Content) cItem).isDeleted()).collect(Collectors.toList());
            return filteredCList;
        }
        return new ArrayList<I>(tList);
    }

    public List<String> compare(T oldT, T newT) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        BeanMap map = new BeanMap(oldT);
        PropertyUtilsBean propUtils = new PropertyUtilsBean();
        //StringBuilder sb = new StringBuilder();
        //sb.append("UPDATE process: >> object=[class=" + newT.getClass() + ", " + newT.getId() + "]:");
        List<String> differentProperties = new ArrayList<>();
        for (Object propNameObject : map.keySet()) {
            String propertyName = (String) propNameObject;
            if (propertyName.endsWith("List")) continue;
            try {
                Object property1 = propUtils.getProperty(oldT, propertyName);
                Object property2 = propUtils.getProperty(newT, propertyName);
                if (property1 != null) {
                    if (!(property1 instanceof List) && !(property1 instanceof Date)) {
                        if (property1.equals(property2)) {
                            //sb.append(" ||" + propertyName + " is equal ||");
                        } else {
                            try {
                                //sb.append(" ||> " + propertyName + " is different (oldValue=\"" + property1 + "\", newValue=\"" + property2 + "\") ||");
                                differentProperties.add(propertyName);
                            } catch (Exception e) {
                                //sb.append(" ||> " + propertyName + " is different (newValue=\"" + property2 + "\") ||");
                                differentProperties.add(propertyName);
                            }
                        }
                    }
                } else {
                    if (property2 == null) {
                        //sb.append(" ||" + propertyName + " is equal ||");
                    } else {
                        if (!(property2 instanceof List) && !(property2 instanceof Date)) {
                            //sb.append(" ||> " + propertyName + " is different (newValue=\"" + property2 + "\") ||");
                            differentProperties.add(propertyName);
                        }
                    }
                }
            } catch (Exception e) {
                //sb.append(" ||> Could not compute difference for property with name=" + propertyName + "||");
            }
        }
        //sb.append(" <<");
        //LOGGER.info(sb.toString());
        return differentProperties;
    }

    @Override
    public <R> CrudRepository<R, Long> getRepository() {
        return null;
    }
}

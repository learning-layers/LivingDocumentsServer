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

package de.hska.ld.core.service.impl;

import de.hska.ld.core.service.Service;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractService<T> implements Service<T> {

    private CrudRepository<T, Long> repository;

    @PostConstruct
    private void init() {
        repository = getRepository();
    }

    @Override
    public T findById(Long id) {
        if (id != null) {
            return repository.findOne(id);
        }
        return null;
    }

    @Override
    public List<T> findAll() {
        return convertIterableToList(repository.findAll());
    }

    @Override
    public T save(T t) {
        return repository.save(t);
    }

    @Override
    public void delete(T t) {
        repository.delete(t);
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

    protected List<T> convertIterableToList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for (T t : iterable) {
            list.add(t);
        }
        return list;
    }

    public abstract <T> CrudRepository<T, Long> getRepository();
}
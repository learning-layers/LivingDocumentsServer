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

package de.hska.ld.content.persistence.repository.custom.impl;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.repository.custom.DocumentRepositoryCustom;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.List;

public class DocumentRepositoryImpl implements DocumentRepositoryCustom {

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public Page<Document> searchDocumentByTitleOrDescription(String searchTerm, Pageable pageable) {

        FullTextEntityManager fullTextEntityManager =
                org.hibernate.search.jpa.Search.getFullTextEntityManager(em);

        // create native Lucene query unsing the query DSL
        // alternatively you can write the Lucene query using the Lucene query parser
        // or the Lucene programmatic API. The Hibernate Search DSL is recommended though
        QueryBuilder searchQb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Document.class).get();
        org.apache.lucene.search.Query luceneQuery = searchQb
                .phrase()
                .onField("title").andField("description")
                        //.onFields("title", "description")
                .sentence(searchTerm)
                        //.matching(searchTerm)
                .createQuery();

        // wrap Lucene query in a javax.persistence.Query
        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Document.class);

        long total = jpaQuery.getResultList().size();

        jpaQuery.setMaxResults(pageable.getPageSize());
        jpaQuery.setFirstResult(pageable.getOffset());

        // execute search
        List result = jpaQuery.getResultList();

        return new PageImpl<Document>(result, pageable, total);
    }
}

package de.hska.ld.content.persistence.repository.custom.impl;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.persistence.repository.custom.TagRepositoryCustom;
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

public class TagRepositoryImpl implements TagRepositoryCustom {
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public Page<Tag> searchTagByNameOrDescription(String searchTerm, Pageable pageable) {

        FullTextEntityManager fullTextEntityManager =
                org.hibernate.search.jpa.Search.getFullTextEntityManager(em);

        // create native Lucene query unsing the query DSL
        // alternatively you can write the Lucene query using the Lucene query parser
        // or the Lucene programmatic API. The Hibernate Search DSL is recommended though
        QueryBuilder searchQb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Tag.class).get();
        org.apache.lucene.search.Query luceneQuery = searchQb
                .phrase()
                .onField("name").andField("description")
                        //.onFields("title", "description")
                .sentence(searchTerm)
                        //.matching(searchTerm)
                .createQuery();

        /*org.apache.lucene.search.Query luceneQuery = searchQb.keyword()
                .onFields("name")
                .matching(searchTerm).createQuery();*/

        // wrap Lucene query in a javax.persistence.Query
        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Tag.class);

        long total = jpaQuery.getResultList().size();

        jpaQuery.setMaxResults(pageable.getPageSize());
        int pageOffset = pageable.getOffset();
        jpaQuery.setFirstResult(pageOffset);

        // execute search
        List result = jpaQuery.getResultList();

        return new PageImpl<Tag>(result, pageable, total);
    }
}

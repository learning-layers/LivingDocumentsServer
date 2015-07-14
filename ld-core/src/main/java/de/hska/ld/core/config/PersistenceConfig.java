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

package de.hska.ld.core.config;

import org.apache.commons.io.FileUtils;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@EnableJpaRepositories("de.hska.ld.*.persistence")
@EnableTransactionManagement
public class PersistenceConfig {

    @Autowired
    private Environment env;

    @Autowired
    private void init() {
        String ddl = env.getProperty("module.core.db.ddl");
        if (ddl != null && ddl.contains("update")) {
            try {
                FileUtils.deleteDirectory(new File(env.getProperty("module.core.search.location")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("module.core.db.driver"));
        dataSource.setUrl(System.getenv("LDS_MYSQL_URL"));
        dataSource.setUsername(System.getenv("LDS_MYSQL_USER"));
        dataSource.setPassword(System.getenv("LDS_MYSQL_PASSWORD"));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws SQLException {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(jpaVendorAdapter());
        factory.setPackagesToScan("de.hska.ld.*.persistence.domain");
        factory.setDataSource(dataSource());
        //factory.setJpaDialect(new HibernateJpaDialect());

        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("module.core.db.ddl"));
        jpaProperties.setProperty("hibernate.search.default.indexBase", env.getProperty("module.core.search.location"));

        factory.setJpaProperties(jpaProperties);
        factory.afterPropertiesSet();

        return factory;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(Boolean.parseBoolean(env.getProperty("module.core.db.log.sql")));
        vendorAdapter.setDatabasePlatform(env.getProperty("module.core.db.dialect"));
        vendorAdapter.setGenerateDdl(true);
        return vendorAdapter;
    }

    @Bean
    public EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws SQLException {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return txManager;
    }

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

    public class HibernateJpaDialect extends org.springframework.orm.jpa.vendor.HibernateJpaDialect {
        private FlushMode flushMode;

        public String getFlushMode() {
            return flushMode != null ? flushMode.toString() : null;
        }

        public void setFlushMode(String aFlushMode) {
            flushMode = FlushMode.valueOf(aFlushMode);
            if (aFlushMode != null && flushMode == null) {
                throw new IllegalArgumentException(aFlushMode + " value invalid. See class org.hibernate.FlushMode for valid values");
            }
        }

        public Object prepareTransaction(EntityManager entityManager, boolean readOnly, String name)
                throws PersistenceException {

            Session session = getSession(entityManager);
            FlushMode currentFlushMode = session.getFlushMode();
            FlushMode previousFlushMode = null;
            if (getFlushMode() != null) {
                session.setFlushMode(flushMode);
                previousFlushMode = currentFlushMode;
            } else if (readOnly) {
                // We should suppress flushing for a read-only transaction.
                session.setFlushMode(FlushMode.MANUAL);
                previousFlushMode = currentFlushMode;
            } else {
                // We need AUTO or COMMIT for a non-read-only transaction.
                if (currentFlushMode.lessThan(FlushMode.COMMIT)) {
                    session.setFlushMode(FlushMode.AUTO);
                    previousFlushMode = currentFlushMode;
                }
            }
            return new SessionTransactionData(session, previousFlushMode);
        }

        public void cleanupTransaction(Object transactionData) {
            ((SessionTransactionData) transactionData).resetFlushMode();
        }

        private class SessionTransactionData {

            private final Session session;

            private final FlushMode previousFlushMode;

            public SessionTransactionData(Session session, FlushMode previousFlushMode) {
                this.session = session;
                this.previousFlushMode = previousFlushMode;
            }

            public void resetFlushMode() {
                if (this.previousFlushMode != null) {
                    this.session.setFlushMode(this.previousFlushMode);
                }
            }
        }
    }
}

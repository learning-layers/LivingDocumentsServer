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

package de.hska.ld.core.persistence.repository;

import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByUsername(String username);

    @Query("FROM User u WHERE u.email = :email AND u.email is not null")
    User findByEmail(@Param("email") String email);

    User findByRegistrationConfirmationKey(String confirmationKey);

    User findByForgotPasswordConfirmationKey(String confirmationKey);

    User findByChangeEmailConfirmationKey(String confirmationKey);

    @Query("FROM User u WHERE u.username = :userKey OR (u.email = :userKey AND u.email is not null)")
    User findByUsernameOrEmail(@Param("userKey") String userKey);

    @Query("SELECT u FROM User u LEFT JOIN u.roleList r WHERE r.name = :roleName")
    List<User> findByRole(@Param("roleName") String roleName);

    Page<User> findAll(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.enabled is not true")
    Page<User> findDisabledAll(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.username LIKE :term")
    List<User> findMentionSuggestions(@Param("term") String term);

    @Query("SELECT u FROM User u WHERE u.username LIKE :term")
    Page<User> findMentionSuggestions(@Param("term") String term, Pageable pageable);
}

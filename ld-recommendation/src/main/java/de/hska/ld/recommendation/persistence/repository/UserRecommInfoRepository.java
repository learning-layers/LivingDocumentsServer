/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2015, Karlsruhe University of Applied Sciences.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.hska.ld.recommendation.persistence.repository;

import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.recommendation.persistence.domain.UserRecommInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRecommInfoRepository extends CrudRepository<UserRecommInfo, Long> {
    UserRecommInfo findByUser(User user);

    UserRecommInfo findById(Long id);

    // TODO add deleted flag to query
    @Query("SELECT DISTINCT u FROM User u WHERE NOT EXISTS (SELECT user FROM User user, UserRecommInfo uri WHERE uri.user.id = user.id AND user.id = u.id)")
    List<User> findAllUserWithoutUserRecommInfo();
}

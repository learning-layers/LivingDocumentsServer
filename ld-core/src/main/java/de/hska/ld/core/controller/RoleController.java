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

package de.hska.ld.core.controller;

import de.hska.ld.core.dto.IdDto;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.Callable;

/**
 * <p><b>RESOURCE</b> {@code /api/roles}
 */
@RestController
@RequestMapping(Core.RESOURCE_ROLE)
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * <pre>
     * Saves a role. If no ID is provided a new role will be created otherwise an existing role will be updated.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> POST {@value Core#RESOURCE_ROLE}
     * </pre>
     *
     * @param role the role instance to be saved or modified as request body. Example: <br>
     *             {name: 'ROLE_SUBSCRIBER'}
     * @return <b>201 Created</b> and the role ID or <br>
     * <b>200 OK</b> and the ID of the updated role instance or <br>
     * <b>403 Forbidden</b> if authorization failed
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.POST)
    public Callable saveRole(@RequestBody @Valid final Role role) {
        return () -> {
            boolean isNew = role.getId() == null;
            Role dbRole = roleService.findById(role.getId());
            if (dbRole != null) {
                dbRole.setName(role.getName());
            }
            dbRole = roleService.save(role);
            IdDto idDto = new IdDto(dbRole.getId());
            if (isNew) {
                return new ResponseEntity<>(idDto, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(idDto, HttpStatus.OK);
            }
        };
    }

    /**
     * <pre>
     * Deletes the role.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> DELETE {@value Core#RESOURCE_ROLE}/{id}
     * </pre>
     *
     * @param id the role ID as a path variable
     * @return <b>200 OK</b> if deletion was successful or <br>
     * <b>404 Not Found</b> if no role exists with the given ID or <br>
     * <b>403 Forbidden</b> if authorization failed
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public Callable deleteRole(@PathVariable Long id) {
        return () -> {
            Role role = roleService.findById(id);
            if (role != null) {
                roleService.delete(role);
                return new ResponseEntity(HttpStatus.OK);
            } else {
                throw new NotFoundException("id");
            }
        };
    }
}

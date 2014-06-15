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
import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p><b>RESOURCE</b> {@code /api/roles}
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * <pre>
     * Saves a role. This means a new role will be created if no ID is specified or a old role will be
     * updated if ID is specified.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> POST /api/roles
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
    public ResponseEntity<IdDto> saveRole(@RequestBody @Valid Role role) {
        Role dbRole = roleService.findById(role.getId());
        if (dbRole != null) {
            dbRole.setName(role.getName());
            role = dbRole;
        }
        role = roleService.save(role);
        IdDto idDto = new IdDto(role.getId());
        if (dbRole != null) {
            return new ResponseEntity<>(idDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(idDto, HttpStatus.CREATED);
        }
    }

    /**
     * <pre>
     * Deletes the role.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> DELETE /api/roles/{id}
     * </pre>
     *
     * @param id
     * @return {@code 200 OK} if deletion was successful or
     * {@code 404 Not Found} if no role exists with the given ID
     * {@code 403 Forbidden} if authorization failed.
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity deleteRole(@PathVariable Long id) {
        Role role = roleService.findById(id);
        if (role != null) {
            roleService.delete(role);
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}

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

package de.hska.ld.sandbox;

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DataGenerator {

    @Autowired
    @Transactional
    public void init(DocumentService documentService, UserService userService, RoleService roleService, Environment env) {
        if (false) {
            String sandboxUsersConcatString = null;
            try {
                sandboxUsersConcatString = env.getProperty("module.sandbox.users");
                createSandboxUsers(userService, roleService, sandboxUsersConcatString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO Martin
            String ddl = env.getProperty("module.core.db.ddl");
            if ("create".equals(ddl) || "create-drop".equals(ddl)) {
                User user = userService.findByUsername(Core.BOOTSTRAP_USER);
                createSandboxDocument(documentService, userService, user);
            }
        }
    }

    private User newUser(String firstname, String surname, String password, String role) {
        User user = new User();
        user.setPassword(password);
        user.setEmail(firstname + "." + surname + "@learning-layers.de");
        user.setUsername(firstname + surname);
        user.setFullName(firstname + " " + surname);
        user.setLastupdatedAt(new Date());
        return user;
    }

    private void createSandboxUsers(UserService userService, RoleService roleService, String sandboxUsersConcatString) {
        User admin = userService.findByUsername(Core.BOOTSTRAP_ADMIN);
        userService.runAs(admin, () -> {
            Role dbUserRole = roleService.findByName("ROLE_USER");
            if (dbUserRole == null) {
                // create initial roles
                String newUserRoleName = "ROLE_USER";
                createNewUserRole(roleService, newUserRoleName);
                String newAdminRoleName = "ROLE_ADMIN";
                createNewUserRole(roleService, newAdminRoleName);
            }

            final Role adminRole = roleService.findByName("ROLE_ADMIN");
            final Role userRole = roleService.findByName("ROLE_USER");

            if (sandboxUsersConcatString != null && !"".equals(sandboxUsersConcatString)) {
                String[] sandboxUsersString = sandboxUsersConcatString.split(";");
                Arrays.stream(sandboxUsersString).forEach(userString -> {
                    String[] userData = userString.split(":");
                    if (userData.length == 4) {
                        User user = userService.findByUsername(userData[0]);
                        if (user == null) {
                            String firstname = userData[0];
                            String lastname = userData[1];
                            String password = userData[2];
                            String role = userData[3];
                            try {
                                user = userService.save(newUser(firstname, lastname, password, role));
                                if ("Admin".equals(role)) {
                                    List<Role> roleList = new ArrayList<Role>();
                                    roleList.add(adminRole);
                                    roleList.add(userRole);
                                    user.setRoleList(roleList);
                                    userService.save(user);
                                } else {
                                    List<Role> roleList = new ArrayList<Role>();
                                    roleList.add(userRole);
                                    user.setRoleList(roleList);
                                    userService.save(user);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    private Role createNewUserRole(RoleService roleService, String newRoleName) {
        Role newUserRole = new Role();
        newUserRole.setName(newRoleName);
        return roleService.save(newUserRole);
    }

    private void createSandboxDocument(DocumentService documentService, UserService userService, User user) {
        userService.runAs(user, () -> {
            Document document = new Document();
            document.setTitle("Sandbox Document");
            document.setDescription("This is the sandbox document");

            Tag tag = new Tag();
            tag.setName("Tag");
            tag.setDescription("Description");
            document.getTagList().add(tag);

            Comment comment = new Comment();
            comment.setText("Text");
            document.getCommentList().add(comment);

            document = documentService.save(document);

            document = documentService.addAccess(document.getId(), user, Access.Permission.READ);

            User adminUser = userService.findByUsername(Core.BOOTSTRAP_ADMIN);
            documentService.addAccess(document.getId(), adminUser, Access.Permission.READ, Access.Permission.WRITE);

            /*String fileName = "sandbox.pdf";
            InputStream in = null;
            try {
                in = DataGenerator.class.getResourceAsStream("/" + fileName);
                documentService.addAttachment(document.getId(), in, fileName);
            } finally {
                IOUtils.closeQuietly(in);
            }*/
        });
    }
}

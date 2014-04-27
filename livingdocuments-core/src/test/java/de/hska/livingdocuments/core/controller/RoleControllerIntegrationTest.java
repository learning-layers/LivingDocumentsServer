package de.hska.livingdocuments.core.controller;

import org.junit.Assert;
import org.junit.Test;
import de.hska.livingdocuments.core.AbstractIntegrationTest;
import de.hska.livingdocuments.core.persistence.domain.Role;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static de.hska.livingdocuments.core.fixture.CoreFixture.*;

public class RoleControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String ROLE_RESOURCES = "/roles";

    @Test
    public void thatSaveRoleUsesHttpCreatedOnPersist() {
        ResponseEntity<Role> response = exchange(ROLE_RESOURCES, HttpMethod.POST,
                createAdminHeader(newRole()), Role.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}

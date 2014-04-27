package de.hska.livingdocuments.core.controller;

import org.junit.Assert;
import org.junit.Test;
import de.hska.livingdocuments.core.AbstractIntegrationTest;
import de.hska.livingdocuments.core.dto.IdDto;
import de.hska.livingdocuments.core.dto.UserDto;
import de.hska.livingdocuments.core.persistence.domain.User;
import de.hska.livingdocuments.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static de.hska.livingdocuments.core.fixture.CoreFixture.*;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String USER_RESOURCE = "/users";

    @Autowired
    private UserService userService;

    @Test
    public void thatSaveUserUsesHttpCreatedOnPersist() {
        UserDto userDto = newUserDto();
        ResponseEntity<IdDto> response = exchange(USER_RESOURCE, HttpMethod.POST,
                createAdminHeader(userDto), IdDto.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void thatSaveUserUsesHttpOkOnUpdate() {
        User user = userService.save(newUser(), PASSWORD);
        user.setFullName(user.getFullName() + " (updated)");

        String auth = user.getUsername() + ":" + PASSWORD;
        ResponseEntity<IdDto> response = exchange(USER_RESOURCE, HttpMethod.POST,
                createHeader(new UserDto(user, null), auth.getBytes()), IdDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void thatSaveUserAsAdminUsesHttpOkOnUpdate() {
        User user = userService.save(newUser(), PASSWORD);
        user.setFullName(user.getFullName() + " (updated)");

        ResponseEntity<IdDto> response = exchange(USER_RESOURCE, HttpMethod.POST,
                createAdminHeader(new UserDto(user, null)), IdDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

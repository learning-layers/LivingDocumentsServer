package de.hska.ld.core.service;

import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.persistence.domain.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static de.hska.ld.core.fixture.CoreFixture.newUser;

public class UserServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    UserService userService;

    @Test
    public void testRegistration() {
        User user = newUser();
        user.setEmail("martin@bachl.pro");
        userService.register(user);
    }
}

//TODO include this test again when Jenkins is configured
/*package de.hska.ld.core.service;

import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.persistence.domain.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static de.hska.ld.core.fixture.CoreFixture.newUser;

public class UserServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    UserService userService;

    private static String email = "martin.bachl@web.de";

    @Test
    public void testEmail() {
        // TODO include test of this class again
    }

    //@Test TODO include this test again when Jenkins is configured
    /*public void testRegistration() {
        User user = newUser();
        user.setEmail("martin.bachl@web.de");
        userService.register(user);
    }*/

    //@Test TODO include this test again when Jenkins is configured
    /*public void testForgotPassword() {
        User user = newUser();
        user.setEmail("martin.bachl@web.de");
        user = userService.save(user);

        userService.forgotPassword(user.getEmail());
        user = userService.findById(user.getId());
        userService.forgotPasswordConfirm(user.getForgotPasswordConfirmationKey(), "changedPass");
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        user = userService.findByEmail("martin.bachl@web.de");
        Assert.assertTrue(encoder.matches("changedPass", user.getPassword()));
    }*/

    //@Test TODO include this test again when Jenkins is configured
    /*@Test
    public void testChangeEmail() {
        userService.changeEmail(testUser, email);

        testUser = userService.findById(testUser.getId());
        Assert.assertEquals(email, testUser.getEmailToBeConfirmed());

        userService.changeEmailConfirm(testUser.getChangeEmailConfirmationKey());
        testUser = userService.findById(testUser.getId());
        Assert.assertEquals(email, testUser.getEmail());
        Assert.assertNull(testUser.getChangeEmailConfirmationKey());
        Assert.assertNull(testUser.getEmailToBeConfirmed());
    }*/
/*}*/

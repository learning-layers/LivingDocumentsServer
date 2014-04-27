package de.hska.livingdocuments.core.fixture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soehnen.placeholdergenerator.Placeholder;
import de.hska.livingdocuments.core.dto.UserDto;
import de.hska.livingdocuments.core.persistence.domain.Role;
import de.hska.livingdocuments.core.persistence.domain.User;

import java.util.UUID;

public class CoreFixture {

    protected static ObjectMapper objectMapper = new ObjectMapper();

    public static final String PASSWORD = "pass";

    public static UserDto newUserDto() {
        return new UserDto(newUser(), PASSWORD);
    }

    public static User newUser() {
        String firstName = UUID.randomUUID().toString();
        String lastName = UUID.randomUUID().toString();

        User user = new User();
        user.setUsername(firstName + "." + lastName);
        user.setFullName(firstName + " " + lastName);

        return user;
    }

    public static Role newRole() {
        return new Role(Placeholder.text(1));
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}

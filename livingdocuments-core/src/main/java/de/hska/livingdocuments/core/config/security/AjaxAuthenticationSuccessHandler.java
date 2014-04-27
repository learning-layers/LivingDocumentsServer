package de.hska.livingdocuments.core.config.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.livingdocuments.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AjaxAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AjaxAuthenticationSuccessHandler.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String userJson = null;
        try {
            userJson = mapper.writeValueAsString(authentication.getPrincipal());
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }

        response.getWriter().write(userJson);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

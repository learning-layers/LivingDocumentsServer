package de.hska.ld.core.config.security;

import de.hska.ld.core.persistence.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FormAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Object userPrincipal = authentication.getPrincipal();
        String password = authentication.getCredentials().toString();
        if (userPrincipal instanceof User) {
            if (authentication.isAuthenticated()) {
                return authentication;
            } else {
                User user = (User) userPrincipal;
                boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
                if (!passwordMatches) {
                    return null;
                } else {
                    List<GrantedAuthority> grantedAuths = new ArrayList<>();
                    user.getRoleList().forEach(role -> {
                        grantedAuths.add(new SimpleGrantedAuthority(role.getName()));
                    });
                    return new UsernamePasswordAuthenticationToken(user, password, grantedAuths);
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}

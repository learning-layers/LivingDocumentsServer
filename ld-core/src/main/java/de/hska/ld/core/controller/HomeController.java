/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2016, Karlsruhe University of Applied Sciences.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.hska.ld.core.controller;

import de.hska.ld.core.config.security.FormAuthenticationProvider;
import de.hska.ld.core.service.UserService;
import org.mitre.openid.connect.client.OIDCAuthenticationFilter;
import org.mitre.openid.connect.client.SubjectIssuerGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Locale;
import java.util.Set;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    // filter reference so we can get class names and things like that.
    @Autowired
    private OIDCAuthenticationFilter filter;

    @Resource(name = "namedAdmins")
    private Set<SubjectIssuerGrantedAuthority> admins;

    @Autowired
    private FormAuthenticationProvider formAuthenticationProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private Environment env;

    /**
     * Simply selects the home view to render by returning its name.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Locale locale, Model model, Principal p) {

        model.addAttribute("issuerServiceClass", filter.getIssuerService().getClass().getSimpleName());
        model.addAttribute("serverConfigurationServiceClass", filter.getServerConfigurationService().getClass().getSimpleName());
        model.addAttribute("clientConfigurationServiceClass", filter.getClientConfigurationService().getClass().getSimpleName());
        model.addAttribute("authRequestOptionsServiceClass", filter.getAuthRequestOptionsService().getClass().getSimpleName());
        model.addAttribute("authRequestUriBuilderClass", filter.getAuthRequestUrlBuilder().getClass().getSimpleName());

        model.addAttribute("admins", admins);

        return "home";
    }

    @RequestMapping("/user")
    @PreAuthorize("hasRole('ROLE_USER')")
    public String user(Locale locale, Model model, Principal p) {
        model.addAttribute("principal", p);
        return "user";
    }

    @RequestMapping("/open")
    public String open(Principal p) {
        return "open";
    }

    @RequestMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String admin(Model model, Principal p) {
        model.addAttribute("principal", p);
        model.addAttribute("admins", admins);

        return "admin";
    }

    /*@RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(HttpServletRequest request, Locale locale, Model model, Principal p) throws OperationNotSupportedException, AccessDeniedException {
        String username = request.getParameter("username");
        if (username == null) {
            username = request.getParameter("user");
        }
        String password = request.getParameter("password");
        if (username != null && password != null) {
            try {
                User user = userService.findByUsername(username);
                if (user != null) {
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, password);
                    token.setDetails(new WebAuthenticationDetails(request));
                    Authentication authentication = formAuthenticationProvider.authenticate(token);
                    logger.debug("Logging in with [{}]", authentication.getPrincipal());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    throw new AccessDeniedException("Username or password wrong! (1)");
                }
            } catch (Exception e) {
                SecurityContextHolder.getContext().setAuthentication(null);
                logger.error("Failure in autoLogin for user with username=[" + username + "]", e);
                throw new AccessDeniedException("Username or password wrong! (2)");
            }

        } else {
            throw new UnsupportedOperationException("No Authorization credentials provided!");
        }

        return "home";
    }*/

    @RequestMapping("/login_old")
    public String login(Principal p) {
        return "login_old";
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Principal p) throws ServletException {
        request.logout();
        javax.servlet.http.Cookie cookie = new Cookie("sessionID", "");
        cookie.setPath("/");
        if (!"localhost".equals(env.getProperty("module.core.oidc.server.endpoint.main.domain"))) {
            cookie.setDomain(env.getProperty("module.core.oidc.server.endpoint.main.domain"));
        }
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:";
    }
}

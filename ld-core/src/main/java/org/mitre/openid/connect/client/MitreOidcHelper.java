package org.mitre.openid.connect.client;

import org.springframework.core.env.Environment;

public class MitreOidcHelper {
    public static OIDCAuthenticationFilter createOIDCAuthenticationFilter(Environment env) {
        if ("development".equals(env.getProperty("lds.app.instance"))) {
            return new LDOIDCAuthenticationFilter();
        } else {
            return new OIDCAuthenticationFilter();
        }
    }
}

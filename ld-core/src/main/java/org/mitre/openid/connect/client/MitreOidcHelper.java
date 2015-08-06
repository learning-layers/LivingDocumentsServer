package org.mitre.openid.connect.client;

public class MitreOidcHelper {

    public static OIDCAuthenticationFilter createOIDCAuthenticationFilter() {
        if ("development".equals(System.getenv("LDS_APP_INSTANCE"))) {
            return new LDOIDCAuthenticationFilter();
        } else {
            return new OIDCAuthenticationFilter();
        }
    }
}

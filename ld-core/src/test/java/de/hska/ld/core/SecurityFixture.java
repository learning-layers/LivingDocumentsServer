package de.hska.ld.core;

import org.apache.http.cookie.Cookie;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.HttpURLConnection;

public class SecurityFixture extends SimpleClientHttpRequestFactory {
    private Cookie cookie;

    public SecurityFixture(Cookie cookie) {
        this.cookie = cookie;
    }

    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) {
        connection.setRequestProperty("Cookie", "JSESSIONID=" + cookie.getValue());
    }

    public Cookie getCookie() {
        return cookie;
    }
}

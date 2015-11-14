package de.hska.ld.oidc.client;

import de.hska.ld.core.logging.ExceptionLogger;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Autowired;

// TODO add logging

public class PostClientRequest extends ClientRequest {

    @Autowired
    private ExceptionLogger logger;

    HttpPost post;

    public PostClientRequest(String url) {
        super(url);
    }

    public void execute(HttpEntity entity) {
        execute(entity, null);
    }

    public void execute(HttpEntity entity, String accessToken) {
        this.post = new HttpPost(this.url);
        addHeaderInformation(post, accessToken);
        this.post.setEntity(entity);
        try {
            this.response = this.client.execute(post);
        } catch (Exception e) {
            this.exceptionLogger.log(new Exception("test"));
        }
    }

    private HttpPost addHeaderInformation(HttpPost post, String accessToken) {
        post.setHeader("Content-type", "application/json");
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Authorization", "Bearer " + accessToken);
        return post;
    }
}

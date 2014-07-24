package de.hska.ld.content.controller;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.dto.CommentDto;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.util.Content;
import de.hska.ld.content.util.RequestBuilder;
import de.hska.ld.core.AbstractIntegrationTest2;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CommentControllerIntegrationTest extends AbstractIntegrationTest2 {

    private static final String RESOURCE_DOCUMENT = Content.RESOURCE_DOCUMENT;
    private static final String RESOURCE_COMMENT = Content.RESOURCE_COMMENT;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";

    Document document;

    @Autowired
    UserService userService;

    @Autowired
    DocumentService documentService;

    @Before
    public void setUp() throws Exception {
        User user = userService.findByUsername("user");
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);
    }

    @Test
    public void testAddCommentToCommentHttpOk() {
        // Add document
        ResponseEntity<Document> response = post().resource(RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());

        // Add comment to the document
        String URIAddCommentToDocument = RESOURCE_DOCUMENT + "/" + response.getBody().getId() + "/comment";
        Comment comment = new Comment();
        comment.setText("Text");
        HttpRequest request = post().resource(URIAddCommentToDocument).asUser().body(comment);
        ResponseEntity<Comment> responseAddCommentToDocument = request.exec(Comment.class);
        Assert.assertEquals(HttpStatus.CREATED, responseAddCommentToDocument.getStatusCode());

        // read document comments
        String requestParamPageNumber = "page-number=0";
        String requestParamPageSize = "page-size=10";
        String requestParamSortDirection = "sort-direction=DESC";
        String requestParamSortProperty = "sort-property=createdAt";
        String combinedRequestParams =
                RequestBuilder.buildCombinedRequestParams(
                        requestParamPageNumber, requestParamPageSize, requestParamSortDirection, requestParamSortProperty
                );
        HttpRequest request3 = get().resource(URIAddCommentToDocument + combinedRequestParams).asUser();
        ResponseEntity<List<LinkedHashMap>> response3 = request3.exec((Class<List<LinkedHashMap>>) (Class) ArrayList.class);
        Assert.assertEquals(HttpStatus.OK, response3.getStatusCode());
        long listSize = response3.getBody().size();
        Assert.assertTrue(listSize > 0);
        Assert.assertTrue(response3.getBody().get(0).size() > Content.class.getDeclaredFields().length);

        // create sub comment
        // Add comment to the existing comment
        String URIAddCommentToComment = RESOURCE_COMMENT + "/" + responseAddCommentToDocument.getBody().getId() + "/comment";
        Comment subComment = new Comment();
        subComment.setText("Text");
        HttpRequest requestAddCommentToComment = post().resource(URIAddCommentToComment).asUser().body(subComment);
        ResponseEntity<CommentDto> responseAddCommentToComment = requestAddCommentToComment.exec(CommentDto.class);
        Assert.assertEquals(HttpStatus.CREATED, responseAddCommentToComment.getStatusCode());
        Assert.assertNotNull(responseAddCommentToComment.getBody().getJsonParentId());
    }

}

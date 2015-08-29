package de.hska.ld.core.controller;

import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.ResponseHelper;
import de.hska.ld.core.UserSession;
import de.hska.ld.core.controller.InfoController.Info;
import de.hska.ld.core.util.Core;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class InfoControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_INFO = Core.RESOURCE_INFO;

    @Test
    public void testGetInfoHttpOkOnSuccess() throws Exception {
        HttpResponse response = UserSession.user().get(RESOURCE_INFO);
        ResponseHelper.getBody(response, Info.class);

        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(response));
        //TODO add info data to testset
        //Assert.assertNotNull(respondedInfo.title);
    }
}

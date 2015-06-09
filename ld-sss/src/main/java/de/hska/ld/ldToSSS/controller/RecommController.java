package de.hska.ld.ldToSSS.controller;

import com.sun.mail.iap.ConnectionException;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.UserContentInfoService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import de.hska.ld.ldToSSS.client.RecommClient;
import de.hska.ld.ldToSSS.persistence.domain.RecommInfo;
import de.hska.ld.ldToSSS.service.RecommInfoService;
import de.hska.ld.ldToSSS.util.LDocsToSSS;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping(LDocsToSSS.RESOURCE_DOCUMENT_LDToSSS)
public class RecommController {
    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserContentInfoService userContentInfoService;

    @Autowired
    private RecommInfoService recommInfoService;

    @Autowired
    private Environment env;

    @Autowired
    private RecommClient recommClient;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/recomm/update/{id}")
    @Transactional(readOnly = true)
    public Callable editRecommUser(@PathVariable long id) {
        return () -> {
            RecommInfo recommInfo = recommInfoService.findOne(id);

            //Make sure the user we are about to PUT in SSS exists in our DB as well
            if (recommInfo != null) {
                //GET DOCUMENTS in which user is EXPERT and update TAGS, INDIRECT TAGS
                recommInfo.updateTagList();
                ArrayList<Tag> userDirectTags = recommInfo.getTags();
                //userContentInfoService.getUserContentTagsPage(id,0,10, "DESC","id");

                URI uri = new URIBuilder()
                        .setScheme("http")
                        .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                        .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/update")
                        .build();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("realm", recommInfo.getRealm());
                jsonObject.put("forUser", recommInfo.getForUser());
                jsonObject.put("entity", recommInfo.getEntity());
                jsonObject.put("tags", recommInfo.retrieveTagNames(userDirectTags));

                StringEntity bodyRequest = new StringEntity(jsonObject.toString());
                HttpEntity entity = null;
                //EXECUTE PUT REQUEST TO SSS server
                try {
                    entity = recommClient.httpPUTRequest(uri, recommClient.getTokenHeader(), bodyRequest);

                }catch (RequestRejectedException|TimeoutException e){
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
                }

                if(entity!=null) {
                    // CONVERT RESPONSE TO STRING
                    String result = EntityUtils.toString(entity);

                    //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                    EntityUtils.consume(entity);
                    return new ResponseEntity<>(result, HttpStatus.CREATED);
                }else{
                    return new ResponseEntity<>("We could't connect to Social Semantic Server, please report it to IT administrators", HttpStatus.CONFLICT);
                }

            } else {
                if(userService.findById(id) != null){
                    return new ResponseEntity<>("This user doesn't have any recommendations!", HttpStatus.ACCEPTED);

                }else
                    throw new NotFoundException("id");
            }
        };
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/recomm/users/{realm}")
    @Transactional(readOnly = true)
    public Callable getRecommUsersRealm(@PathVariable String realm) {
        return () -> {
            /************************************************************************
             * JUST AND EXAMPLE of how to perform a GET request to SSS server
             ************************************************************************/
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                    .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/users/realm/"+realm)
                    .build();

            //EXECUTE GET REQUEST TO SSS server
            HttpEntity entity = recommClient.httpGETRequest(uri, recommClient.getTokenHeader());

            if(entity!=null) {
                // CONVERT RESPONSE TO STRING
                String result = EntityUtils.toString(entity);
                //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                EntityUtils.consume(entity);
                return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
            }else{
                throw new NotFoundException("realm");
            }

        };
    }



}

package de.hska.ld.ldToSSS.controller;

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
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
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
    @RequestMapping(method = RequestMethod.POST, value = "/recomm/update/{id}")
    @Transactional(readOnly = true)
    public Callable editRecommUser(@PathVariable long id) {
        return () -> {
            RecommInfo recommInfo = recommInfoService.findOne(id);

            //Make sure the user we are about to PUT in SSS exists in our DB as well
            if (recommInfo != null) {
                //UPDATE INDIRECT TAGS in which user is EXPERT
                recommInfo.updateTagList();

                //GET TAGS from DOCUMENTS in which this user is an EXPERT
                ArrayList<Tag> userINDirectTags = recommInfo.getTags();

                //GET user DIRECT TAGS from user-content_Info
                //User is strongly attached to published document
                Page<Tag> tagsPage = userContentInfoService.getUserContentTagsPage(id, 0, 10, "DESC", "id");
                ArrayList<Tag> userDirTags = null;

                if (tagsPage != null && tagsPage.getNumberOfElements() > 0) {
                    userDirTags = new ArrayList<Tag>();
                    userDirTags.addAll(tagsPage.getContent());
                }

                //TODO DIFFERENTIATE in the SSS about what to do with DIRECT TAGS
                // INDIRECT TAGS (Just as an expert on the topic)
                //currently tags are only for one
                if(userDirTags != null && userDirTags.size()>0){
                    userINDirectTags.addAll(userDirTags);
                }

                URI uri = new URIBuilder()
                        .setScheme("http")
                        .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                        .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/update")
                        .build();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("realm", recommInfo.getRealm());
                jsonObject.put("forUser", recommInfo.getForUser());
                jsonObject.put("entity", recommInfo.getEntity());
                jsonObject.put("tags", recommInfo.retrieveUniqueTagNames(userINDirectTags));

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
                    JSONObject jsonResult = convertToJson(result);

                    //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                    EntityUtils.consume(entity);
                    return new ResponseEntity<>(jsonResult, HttpStatus.CREATED);
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
    @RequestMapping(method = RequestMethod.PUT, value = "/recomm/update",headers = {"Content-type=application/json"})
    @Transactional(readOnly = true)
    public Callable updateUserSSS(@RequestBody RecommInfo recommInfo) {
        return () -> {

            if (recommInfo != null) {

                URI uri = new URIBuilder()
                        .setScheme("http")
                        .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                        .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/update")
                        .build();
                HttpEntity entity = null;
                //EXECUTE PUT REQUEST TO SSS server
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("realm", recommInfo.getRealm());
                    jsonObject.put("forUser", recommInfo.getForUser());
                    jsonObject.put("entity", recommInfo.getEntity());
                    jsonObject.put("tags", recommInfo.getTags());

                    StringEntity bodyRequest = new StringEntity(jsonObject.toString());
                    entity = recommClient.httpPUTRequest(uri, recommClient.getTokenHeader(), bodyRequest);

                }catch (RequestRejectedException|TimeoutException e){
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
                }

                if(entity!=null) {
                    // CONVERT RESPONSE TO STRING
                    String result = EntityUtils.toString(entity);
                    JSONObject jsonObject = convertToJson(result);

                    //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                    EntityUtils.consume(entity);
                    return new ResponseEntity<>(jsonObject, HttpStatus.CREATED);
                }else{
                    return new ResponseEntity<>("We could't connect to Social Semantic Server, please report it to IT administrators", HttpStatus.CONFLICT);
                }

            } else {
                    return new ResponseEntity<>("Not possible to process your request! Please verify your object", HttpStatus.BAD_REQUEST);
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

                JSONObject jsonObject = convertToJson(result);
                //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                EntityUtils.consume(entity);
                return new ResponseEntity<>(jsonObject, HttpStatus.ACCEPTED);
            }else{
                throw new NotFoundException("realm");
            }

        };
    }


    //This function is called when you want to get recomm for an entity (many objects (users, documents, etc) can belong to an entity but not
    //the other way around
    //This function can be used when another realm is used in the request
    //Remember entity has to be encoded to be accepted as a request
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/recomm/users/{realm}/{entity}")
    @Transactional(readOnly = true)
    public Callable getRecommUsersEntity(@PathVariable String realm,@PathVariable("entity") String entity) {
        return () -> {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                    .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/users/realm/"+realm+"/entity/"+entity)
                    .build();
            //EXECUTE GET REQUEST TO SSS server
            HttpEntity httpEntity = recommClient.httpGETRequest(uri, recommClient.getTokenHeader());

            if(httpEntity!=null) {
                // CONVERT RESPONSE TO STRING
                String result = EntityUtils.toString(httpEntity);
                JSONObject jsonResult = convertToJson(result);

                //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                EntityUtils.consume(httpEntity);
                return new ResponseEntity<>(jsonResult, HttpStatus.ACCEPTED);
            }else{
                throw new NotFoundException("entity");
            }

        };
    }


    //This functions helps to get recommendations for a specific user (NOTE: you only need the id)
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/recomm/user/{userId}")
    @Transactional(readOnly = true)
    public Callable getRecommForUser(@PathVariable("userId") long userId) {
        return () -> {
            RecommInfo recommInfo = recommInfoService.findOne(userId);

            //Make sure the user we are about to PUT in SSS exists in our DB as well
            if (recommInfo != null) {
                String realm = URLEncoder.encode(recommInfo.getRealm(), "UTF-8");
                String forUser = URLEncoder.encode(recommInfo.getForUser(), "UTF-8");
                URI uri = new URIBuilder()
                        .setScheme("http")
                        .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                        .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/users/realm/" + realm + "/user/" + forUser)
                        .build();
                //EXECUTE GET REQUEST TO SSS server
                HttpEntity httpEntity = recommClient.httpGETRequest(uri, recommClient.getTokenHeader());

                if(httpEntity!=null) {
                    // CONVERT RESPONSE TO STRING
                    String result = EntityUtils.toString(httpEntity);
                    JSONObject jsonResult = convertToJson(result);

                    //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                    EntityUtils.consume(httpEntity);
                    return new ResponseEntity<>(jsonResult, HttpStatus.ACCEPTED);
                }else{
                    return new ResponseEntity<>("USER was not found within the SSS, please contact IT administrators", HttpStatus.CONFLICT);
                }
            }else {
                if(userService.findById(userId) != null){
                    return new ResponseEntity<>("This user doesn't have any recommendations!", HttpStatus.ACCEPTED);

                }else
                    throw new NotFoundException("userId");
            }

        };
    }

    //This functions helps to get recommendations for a specific user (NOTE: you have to provide realm as well)
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/recomm/users/{realm}/user/{forUser}")
    @Transactional(readOnly = true)
    public Callable getRecommForUser(@PathVariable String realm,@PathVariable("forUser") String forUser) {
        return () -> {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                    .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/users/realm/" + realm + "/user/" + forUser)
                    .build();
            //EXECUTE GET REQUEST TO SSS server
            HttpEntity httpEntity = recommClient.httpGETRequest(uri, recommClient.getTokenHeader());

            if(httpEntity!=null) {
                // CONVERT RESPONSE TO STRING
                String result = EntityUtils.toString(httpEntity);
                JSONObject jsonResult = convertToJson(result);

                //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                EntityUtils.consume(httpEntity);
                return new ResponseEntity<>(jsonResult, HttpStatus.ACCEPTED);
            }else{
                return new ResponseEntity<>("USER was not found within the SSS, please contact IT administrators", HttpStatus.CONFLICT);
            }

        };
    }

    //NOT SURE IF DOCUMENT IS CREATED WHEN THEY ARE TYPING IT THE FIRST TIME....
    //TOCHECK  First we must add tag to document      POST /api/documents/{documentId}/tag/{tagId}
    //Then we add tag to user                         POST /api/users/{userId}/tag/{tagId}
    //Update user in SSS                              POST /api/ldToSSS/recomm/update/{userId}
    //Get current recomm from SSS                     GET  /api/ldToSSS/recomm/user/{userId}

    public JSONObject convertToJson(String object){
        try {
//            Map<String,String> map;
//            ObjectMapper mapper = new ObjectMapper();
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(object);


            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

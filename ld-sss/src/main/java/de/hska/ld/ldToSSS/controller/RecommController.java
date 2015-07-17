package de.hska.ld.ldToSSS.controller;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.UserContentInfoService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.persistence.domain.User;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    private RecommClient recommClient;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/recomm/file/create",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Transactional(readOnly = false)
    public Callable createRecommFile(@RequestBody RecommInfo postRecommInfo) {
        return () -> {
            Long id = postRecommInfo.getTypeID();

            if(id != null && documentService.findById(id) != null && recommInfoService.findOneFile(id) == null){
                RecommInfo recommInfo = generateFileData(postRecommInfo);
                Document document = documentService.findById(id);
                ArrayList<String> tagsJSON = null;

                //Look for document Tags
                List<Tag> fileTags = document.getTagList();

                if(fileTags!= null && !fileTags.isEmpty()){
                    tagsJSON = recommInfo.retrieveUniqueTagNames(new ArrayList<>(fileTags));
                }

                //First we store new object in our local DB before sending it to the SSS
                recommInfoService.save(recommInfo);

                URI uri = new URIBuilder()
                        .setScheme("http")
                        .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                        .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/update")
                        .build();

                String postingEntity = LDocsToSSS.RESOURCE_LD_URI + "/user/" + Core.currentUser().getId(); ;

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("realm", recommInfo.getRealm());
                jsonObject.put("forUser", postingEntity);
                jsonObject.put("entity", recommInfo.getEntity());

                if(tagsJSON!=null)
                    jsonObject.put("tags", tagsJSON);

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
                    return new ResponseEntity<>("We couldn't connect to Social Semantic Server, please report it to IT administrators", HttpStatus.CONFLICT);
                }
            }else if(id == null || documentService.findById(id) == null){
                throw new NotFoundException("id_file");
            }else {
                return new ResponseEntity<>("This FILE already exists in Recomm Datatable!", HttpStatus.CONFLICT);
            }
        };
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/recomm/user/create",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Transactional(readOnly = false)
    public Callable createRecommUser(@RequestBody RecommInfo postRecommInfo) {
        return () -> {
            Long id = postRecommInfo.getTypeID();
            if(id != null && userService.findById(id) != null && recommInfoService.findOneUser(id) == null){
                RecommInfo recommInfo = generateUserData(postRecommInfo);
//                UPDATE INDIRECT TAGS in which user is EXPERT
//                recommInfo.updateTagList();

                //GET TAGS from DOCUMENTS in which this user is an EXPERT
                ArrayList<Tag> userINDirectTags = recommInfo.getTags();

                //GET user DIRECT TAGS from user-content_Info
                //User is strongly attached to published document
                Page<Tag> tagsPage = userContentInfoService.getUserContentTagsPage(id, 0, 50, "DESC", "id");
                ArrayList<Tag> userDirTags = null;

                if (tagsPage != null && tagsPage.getNumberOfElements() > 0) {
                    userDirTags = new ArrayList<Tag>();
                    userDirTags.addAll(tagsPage.getContent());
                }

                // INDIRECT TAGS (Just as an expert on the topic)
                //currently tags are only for one
                if(userDirTags != null && userDirTags.size()>0){
                    userINDirectTags.addAll(userDirTags);
                }

                //First we store new object in our local DB before sending it to the SSS
                recommInfoService.save(recommInfo);

                URI uri = new URIBuilder()
                        .setScheme("http")
                        .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                        .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/update")
                        .build();

                String postingEntity = LDocsToSSS.RESOURCE_LD_URI + "/user/" + Core.currentUser().getId(); ;

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("realm", recommInfo.getRealm());
                jsonObject.put("forUser", postingEntity);
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
                    return new ResponseEntity<>("We couldn't connect to Social Semantic Server, please report it to IT administrators", HttpStatus.CONFLICT);
                }
            }else if(id == null || userService.findById(id) == null){
                throw new NotFoundException("id_user");
            }else {
                return new ResponseEntity<>("This USER already exists in Recomm Datatable!", HttpStatus.CONFLICT);
            }
        };
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/recomm/user/updateSSS/{id}")
    @Transactional(readOnly = true)
    public Callable editRecommUser(@PathVariable long id) {
        return () -> {
            RecommInfo recommInfo = recommInfoService.findOneUser(id);

            //Make sure the user we are about to PUT in SSS exists in our DB as well
            if (recommInfo != null) {

                //GET TAGS from DOCUMENTS in which this user is an EXPERT
                ArrayList<Tag> userINDirectTags = recommInfo.getTags();

                //GET user DIRECT TAGS from user-content_Info
                //User is strongly attached to published document
                Page<Tag> tagsPage = userContentInfoService.getUserContentTagsPage(id, 0, 50, "DESC", "id");
                ArrayList<Tag> userDirTags = null;

                if (tagsPage != null && tagsPage.getNumberOfElements() > 0) {
                    userDirTags = new ArrayList<Tag>();
                    userDirTags.addAll(tagsPage.getContent());
                }

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

                String postingEntity = LDocsToSSS.RESOURCE_LD_URI + "/user/" + Core.currentUser().getId(); ;

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("realm", recommInfo.getRealm());
                jsonObject.put("forUser", postingEntity);
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
                    return new ResponseEntity<>("We couldn't connect to Social Semantic Server, please report it to IT administrators", HttpStatus.CONFLICT);
                }

            } else {
                if(userService.findById(id) != null){
                    return new ResponseEntity<>("This USER doesn't have any recommendations!", HttpStatus.ACCEPTED);

                }else
                    throw new NotFoundException("id");
            }
        };
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/recomm/file/updateSSS/{id}")
    @Transactional(readOnly = true)
    public Callable editRecommFile(@PathVariable long id) {
        return () -> {
            RecommInfo recommInfo = recommInfoService.findOneFile(id);

            //Make sure the user we are about to PUT in SSS exists in our DB as well
            if (recommInfo != null) {
                Document document = documentService.findById(id);
                ArrayList<String> tagsJSON = null;

                //Look for document Tags
                List<Tag> fileTags = document.getTagList();

                if(fileTags!= null && !fileTags.isEmpty()){
                    tagsJSON = recommInfo.retrieveUniqueTagNames(new ArrayList<Tag>(fileTags));
                }

                URI uri = new URIBuilder()
                        .setScheme("http")
                        .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                        .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/update")
                        .build();

                String postingEntity = LDocsToSSS.RESOURCE_LD_URI + "/user/" + Core.currentUser().getId(); ;

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("realm", recommInfo.getRealm());
                jsonObject.put("forUser", postingEntity);
                jsonObject.put("entity", recommInfo.getEntity());
                jsonObject.put("tags", tagsJSON);

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
                    return new ResponseEntity<>("We couldn't connect to Social Semantic Server, please report it to IT administrators", HttpStatus.CONFLICT);
                }

            } else {
                if(userService.findById(id) != null){
                    return new ResponseEntity<>("This FILE doesn't have any recommendations!", HttpStatus.ACCEPTED);

                }else
                    throw new NotFoundException("file_id");
            }
        };
    }


    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/recomm")
    @Transactional(readOnly = true)
    public Callable getObjectsSSS(@RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                @RequestParam(value = "sort-property", defaultValue = "id") String sortProperty) {
        return () -> {
            Page<RecommInfo> pageRecommInfo = recommInfoService.findAll(pageNumber,pageSize,sortDirection,sortProperty);

            if (pageRecommInfo != null && pageRecommInfo.getNumberOfElements() > 0) {
                JSONObject pageResponse = retrieveLocalInfo(pageRecommInfo);

                return new ResponseEntity<>(pageResponse, HttpStatus.OK);
            } else {
                throw new NotFoundException();
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

                JSONArray recomms = (JSONArray)jsonObject.get("users");
                JSONArray respArray = retrieveLocalInfo(recomms);
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

                JSONArray recomms = (JSONArray)jsonResult.get("users");
                JSONArray respArray = retrieveLocalInfo(recomms);
                //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                EntityUtils.consume(httpEntity);
                return new ResponseEntity<>(respArray, HttpStatus.ACCEPTED);
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
            RecommInfo recommInfo = recommInfoService.findOneUser(userId);

            //Make sure the user we are about to PUT in SSS exists in our DB as well
            if (recommInfo != null) {
                String realm = URLEncoder.encode(recommInfo.getRealm(), "UTF-8");
                String forUser = URLEncoder.encode(recommInfo.getEntity(), "UTF-8");
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

                    //In the SSS they send all recommendations (Users/Files) inside an array called "users",
                    //therefore we receive a JSON response that contains multiple JSONs within the property called "users"
                    JSONArray recomms = (JSONArray)jsonResult.get("users");
                    JSONArray respArray = retrieveLocalInfo(recomms);

                    //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                    EntityUtils.consume(httpEntity);
                    return new ResponseEntity<>(respArray, HttpStatus.ACCEPTED);
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

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/recomm/file/{fileId}")
    @Transactional(readOnly = true)
    public Callable getRecommForFile(@PathVariable("fileId") long fileId) {
        return () -> {
            RecommInfo recommInfo = recommInfoService.findOneFile(fileId);

            //Make sure the user we are about to PUT in SSS exists in our DB as well
            if (recommInfo != null) {
                String realm = URLEncoder.encode(recommInfo.getRealm(), "UTF-8");
                String entity = URLEncoder.encode(recommInfo.getEntity(), "UTF-8");
                URI uri = new URIBuilder()
                        .setScheme("http")
                        .setHost(LDocsToSSS.RESOURCE_SSS_HOST)
                        .setPath(LDocsToSSS.RESOURCE_SSS_RECOMM_PATH + "/recomm/users/realm/" + realm + "/entity/" + entity)
                        .build();
                //EXECUTE GET REQUEST TO SSS server
                HttpEntity httpEntity = recommClient.httpGETRequest(uri, recommClient.getTokenHeader());

                if(httpEntity!=null) {
                    // CONVERT RESPONSE TO STRING
                    String result = EntityUtils.toString(httpEntity);
                    JSONObject jsonResult = convertToJson(result);

                    JSONArray recomms = (JSONArray)jsonResult.get("users");
                    JSONArray respArray = retrieveLocalInfo(recomms);

                    //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                    EntityUtils.consume(httpEntity);
                    return new ResponseEntity<>(respArray, HttpStatus.ACCEPTED);
                }else{
                    return new ResponseEntity<>("FILE was not found within the SSS, please contact IT administrators", HttpStatus.CONFLICT);
                }
            }else {
                if(documentService.findById(fileId) != null){
                    return new ResponseEntity<>("This file doesn't have any recommendations!", HttpStatus.ACCEPTED);
                }else
                    throw new NotFoundException("fileId");
            }

        };
    }

    //This functions helps to get recommendations for a specific user (NOTE: you have to provide realm as well)
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/recomm/users/{realm}/user/{forUser}")
    @Transactional(readOnly = true)
    public Callable getRecommForSendingUser(@PathVariable String realm,@PathVariable("forUser") String forUser) {
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

                JSONArray recomms = (JSONArray)jsonResult.get("users");
                JSONArray respArray = retrieveLocalInfo(recomms);

                //VERY IMPORTANT TO FREE RESOURCES AFTER CREATING HTTP ENTITY
                EntityUtils.consume(httpEntity);
                return new ResponseEntity<>(respArray, HttpStatus.ACCEPTED);
            }else{
                return new ResponseEntity<>("USER was not found within the SSS, please contact IT administrators", HttpStatus.CONFLICT);
            }

        };
    }



    //NOT SURE IF DOCUMENT IS CREATED WHEN THEY ARE TYPING IT THE FIRST TIME....
    //TOCHECK  First we must add tag to document      POST /api/documents/{documentId}/tag/{tagId}
    //Then we add tag to user                         POST /api/users/{userId}/tag/{tagId}
    //Update user in SSS                              POST /api/ldToSSS/recomm/user/updateSSS/{id}
    //Get current recomm from SSS                     GET  /api/ldToSSS/recomm/user/{userId}

    public JSONObject convertToJson(String object){
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(object);


            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public RecommInfo generateUserData(RecommInfo recommInfo){
        String entity = LDocsToSSS.RESOURCE_LD_URI + "/user/" + recommInfo.getTypeID();
        recommInfo.setType("USER");
        recommInfo.setEntity(entity);
        return recommInfo;
    }

    public RecommInfo generateFileData(RecommInfo recommInfo){
        String entity = LDocsToSSS.RESOURCE_LD_URI + "/file/" + recommInfo.getTypeID();
        recommInfo.setType("FILE");
        recommInfo.setEntity(entity);
        return recommInfo;
    }

    public JSONArray retrieveLocalInfo(JSONArray array){
        Iterator<JSONObject> iterator = array.iterator();
        JSONArray responseArray = new JSONArray();
        String entity, objectType;
        Double likelihood;

        while(iterator.hasNext()) {
            //Recover the correct properties from the JSON we received (Social Semantic Server HTTP response)
            JSONObject row = iterator.next();
            JSONObject jsonAux = new JSONObject();
            JSONObject object = (JSONObject)row.get("user");
            likelihood = (Double)row.get("likelihood");

            //Properties within OBJECT (USER/FILE) from JSON HTTP response
            entity = (String) object.get("id");
            RecommInfo recomm = recommInfoService.findByEntity(entity);

            if(recomm!=null) {
                Long id_Type = recomm.getTypeID();
                objectType = recomm.getType();

                //We need to build the user entirely with the proper data before adding it to array
                jsonAux.put("likelihood", likelihood);
                jsonAux.put("type", objectType);
                switch(objectType){
                    case "USER":
                        User user = userService.findById(id_Type);
                        jsonAux.put("name", user.getUsername());
                        jsonAux.put("description", user.getDescription());
                        jsonAux.put("email", user.getEmail());

                        break;
                    case "FILE":
                        Document document = documentService.findById(id_Type);
                        jsonAux.put("name", document.getTitle());
                        jsonAux.put("description", document.getDescription());
                        jsonAux.put("email", "");
                        break;
                }
                jsonAux.put("entity", entity);
                responseArray.add(jsonAux);
            }

        }
        return responseArray;
    }

    public JSONObject retrieveLocalInfo(Page<RecommInfo> pageRecommInfo){
        Iterator<RecommInfo> iterator = pageRecommInfo.iterator();
        JSONObject finalResponse = new JSONObject();
        JSONArray contentArray = new JSONArray();
        String objectType;

        while(iterator.hasNext()) {
            JSONObject jsonAux = new JSONObject();
            //Recover the correct properties from the JSON we received (Social Semantic Server HTTP response)
            //content
            RecommInfo recomm = iterator.next();

            Long id_Type = recomm.getTypeID();
            objectType = recomm.getType();
            jsonAux.put("id", recomm.getId());
            jsonAux.put("typeID", recomm.getTypeID());
            jsonAux.put("type", objectType);
            switch(objectType){
                case "USER":
                    User user = userService.findById(id_Type);
                    jsonAux.put("name", user.getFullName());
                    jsonAux.put("description", user.getDescription());
                    jsonAux.put("email", user.getEmail());

                    break;
                case "FILE":
                    Document document = documentService.findById(id_Type);
                    jsonAux.put("name", document.getTitle());
                    jsonAux.put("description", document.getDescription());
                    jsonAux.put("email", "");

                    break;
            }
            jsonAux.put("realm", recomm.getRealm());
            jsonAux.put("entity", recomm.getEntity());
            contentArray.add(jsonAux);
        }

        finalResponse.put("content", contentArray);
        finalResponse.put("totalPages", pageRecommInfo.getTotalPages());
        finalResponse.put("totalElements", pageRecommInfo.getTotalElements());
        finalResponse.put("last", pageRecommInfo.isLast());
        finalResponse.put("size", pageRecommInfo.getSize());
        finalResponse.put("number", pageRecommInfo.getNumber());
        finalResponse.put("sort", pageRecommInfo.getSort());
        finalResponse.put("first", pageRecommInfo.isFirst());
        finalResponse.put("numberOfElements", pageRecommInfo.getNumberOfElements());
        return finalResponse;
    }

}

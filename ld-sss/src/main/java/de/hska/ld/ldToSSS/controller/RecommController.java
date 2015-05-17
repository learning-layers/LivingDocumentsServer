package de.hska.ld.ldToSSS.controller;

import java.lang.Object;
import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.ldToSSS.client.RecommClient;
import de.hska.ld.ldToSSS.persistence.domain.RecommInfo;
import de.hska.ld.ldToSSS.service.RecommInfoService;
import de.hska.ld.ldToSSS.util.LDocsToSSS;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.apache.avro.data.Json;
import org.apache.avro.generic.GenericData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Callable;

@RestController
@RequestMapping(LDocsToSSS.RESOURCE_DOCUMENT_LDToSSS)
public class RecommController {
    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private RecommInfoService recommInfoService;

    @Autowired
    private Environment env;

    @Autowired
    private RecommClient recommClient;

    //Id received is a user_id that already has recommendations
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/recomm/update/{id}")
    @Transactional(readOnly = true)
    public Callable editRecommUser(@PathVariable long id) {
        return () -> {

            RecommInfo recommInfo = recommInfoService.findOne(id);

            if (recommInfo != null) {
                //This update gets the documents in which user is Expert and retrieves tags of all of them
                recommInfo.updateTagList();
                return new ResponseEntity<>(recommInfo, HttpStatus.CREATED);
            } else {
                if(userService.findById(id) != null){
                    return new ResponseEntity<>("This user doesn't have any recommendations!", HttpStatus.ACCEPTED);

                }else
                    throw new NotFoundException("id");
            }



        };
    }

}

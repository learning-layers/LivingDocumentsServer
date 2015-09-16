package de.hska.ld.prerender.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prerender")
public class PrerenderController {

    @Autowired
    private React react;

    @Autowired
    private ObjectMapper mapper;

    @RequestMapping(method = RequestMethod.GET)
    ModelAndView index(Map<String, Object> model) throws ScriptException, NoSuchMethodException, JsonProcessingException {
        List<Comment> comments = Arrays.asList(new Comment("author1", "content1"), new Comment("author2", "content2"), new Comment("author3", "content3"));
        String commentBox = react.renderCommentBox(comments);
        String data = mapper.writeValueAsString(comments);
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("content", commentBox);
        mav.addObject("data", data);
        return mav;
    }
}

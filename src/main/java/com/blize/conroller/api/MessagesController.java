package com.blize.conroller.api;

import com.blize.conf.UserDetailsService;
import com.blize.responder.MessageResponder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/messages")
public class MessagesController {

    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    MessageResponder messageResponder;

    @PostMapping({"/add/{user}"})
    public Object add(
            @PathVariable("user") Integer otherUserId,
            @RequestParam("text") String text){

        return messageResponder.add(this.userDetailsService.getSessionUser(), otherUserId, text);
    }

    @GetMapping({"/group-list/{offset}", "/{offset}"})
    public Object groupList(
            @PathVariable("offset") Integer offset) {

        return messageResponder.groupList(this.userDetailsService.getSessionUser(), offset);
    }

    @GetMapping({"/detail/{user}/{offset}"})
    public Object detail(
            @PathVariable("user") Integer otherUserId,
            @PathVariable("offset") Integer offset) {

        return messageResponder.detail(this.userDetailsService.getSessionUser(), otherUserId, offset);
    }

    @GetMapping({"/read/{user}"})
    public Object read(
            @PathVariable("user") Integer otherUserId) {

        return messageResponder.read(this.userDetailsService.getSessionUser(), otherUserId);
    }
}

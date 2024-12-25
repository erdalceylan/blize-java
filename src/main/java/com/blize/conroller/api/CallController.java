package com.blize.conroller.api;

import com.blize.conf.UserDetailsService;
import com.blize.responder.CallResponder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/call")
public class CallController {

    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    CallResponder callResponder;

    @PostMapping({"/call/{userId}"})
    public Object add(
            @PathVariable("userId") Integer userId,
            @RequestParam("video") String video){

        return callResponder.call(this.userDetailsService.getSessionUser(), userId, Boolean.parseBoolean(video));
    }

    @GetMapping("/accept/{userId}/{_id}")
    public Object accept(
            @PathVariable("userId") Integer userId,
            @PathVariable("_id") String _id) {

        return callResponder.accept(this.userDetailsService.getSessionUser(), userId, _id);
    }

    @GetMapping("/close/{userId}/{_id}")
    public Object close(
            @PathVariable("userId") Integer userId,
            @PathVariable("_id") String _id) {

        return callResponder.close(this.userDetailsService.getSessionUser(), userId, _id);
    }

}

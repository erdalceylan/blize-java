package com.blize.conroller.api;

import com.blize.conf.UserDetailsService;
import com.blize.responder.StoryResponder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController()
@RequestMapping("/story")
public class StoryController {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private StoryResponder storyResponder;

    @PostMapping("/add")
    public Object add(
            @RequestParam("image") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded.");
        }
        return storyResponder.add(this.userDetailsService.getSessionUser(), file);
    }

    @GetMapping("/group-list/{offset}")
    public Object groupList(
            @PathVariable("offset") Integer offset) {

        return storyResponder.groupList(this.userDetailsService.getSessionUser(), offset);
    }

    @GetMapping("/me-list")
    public Object meList() {

        return storyResponder.meList(this.userDetailsService.getSessionUser());
    }

    @GetMapping("/view-list/{_id}/{offset}")
    public Object viewList(
            @PathVariable("_id") String _id,
            @PathVariable("offset") Integer offset) {

        return storyResponder.viewList(this.userDetailsService.getSessionUser(), _id, offset);
    }

    @GetMapping("/seen/{_id}")
    public Object seen(
            @PathVariable("_id") String _id) {

        return storyResponder.seen(this.userDetailsService.getSessionUser(), _id);
    }

    @GetMapping("/delete/{_id}")
    public Object delete(
            @PathVariable("_id") String _id) {

        return storyResponder.delete(this.userDetailsService.getSessionUser(), _id);
    }

}

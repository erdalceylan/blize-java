package com.blize.conroller.api;

import com.blize.conf.UserDetailsService;
import com.blize.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDetailsService userDetailsService;

    @RequestMapping(value = {"/{offset}", "/list/{offset}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public Object search(
            @PathVariable Integer offset
    ) {

       var user = userDetailsService.getSessionUser();
       return this.userService.findUsersNotInIds(List.of(user.getId()), offset);
    }

}

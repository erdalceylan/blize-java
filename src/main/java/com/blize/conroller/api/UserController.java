package com.blize.conroller.api;

import com.blize.conf.UserDetailsService;
import com.blize.entity.User;
import com.blize.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/ping")
    public User ping() {

       var user = userDetailsService.getSessionUser();
       return this.userService.updateLastSeen(user);
    }

    @GetMapping("/detail/{userId}")
    public User detail(
            @PathVariable String userId) {
       return this.userService.findById(Integer.parseInt(userId));
    }

    @GetMapping("/detail-username/{userName}")
    public User detailByUsername(
            @PathVariable String userName) {
       return this.userService.findByUserName(userName);
    }

    @PostMapping("/jwt")
    public Object jwt() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        var user = userDetailsService.getSessionUser();
        return userService.createJwtToken(user);
    }
}

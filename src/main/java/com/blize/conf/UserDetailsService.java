package com.blize.conf;

import com.blize.entity.User;
import com.blize.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.lang.reflect.Method;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findForLogin(username);
        if(user==null) {
            throw new UsernameNotFoundException("Could not find user");
        }
        return new UserDetails(user);
    }

    public User getSessionUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {

                Method getUser = authentication.getPrincipal().getClass().getMethod("getUser");
                Object user = getUser.invoke(authentication.getPrincipal());
                Method getId = user.getClass().getMethod("getId");
                int id = (int)getId.invoke(user);
                return this.userRepository.findById(id);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}

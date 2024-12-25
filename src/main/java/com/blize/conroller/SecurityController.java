package com.blize.conroller;

import com.blize.dto.request.RegisterRequestDTO;
import com.blize.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Controller
public class SecurityController {

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/login", "/"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String login(HttpSession session, Model model, Locale locale) {
        if (session.getAttribute("loginError") != null){
            model.addAttribute("loginError", messageSource.getMessage("login.error", null, locale));
            session.removeAttribute("loginError");
        }

        return "login";
    }

    @GetMapping("/register")
    public String register(
            @ModelAttribute RegisterRequestDTO registerRequestDTO){
        return "register";
    }

    @PostMapping("/register")
    public String register(
            HttpServletRequest request,
            @Valid @ModelAttribute RegisterRequestDTO registerRequestDTO,
            BindingResult bindingResult){

        if (!bindingResult.hasErrors()) {
            userService.register(registerRequestDTO, null);
            userService.manuelAuthenticateUser(registerRequestDTO.getUsername(), registerRequestDTO.getPassword(), request);
            return "redirect:/dashboard";
        }

        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        return "dashboard";
    }
}

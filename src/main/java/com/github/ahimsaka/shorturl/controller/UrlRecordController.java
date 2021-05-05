package com.github.ahimsaka.shorturl.controller;

import com.github.ahimsaka.shorturl.dto.UserDto;
import com.github.ahimsaka.shorturl.entity.UrlRecord;
import com.github.ahimsaka.shorturl.entity.User;
import com.github.ahimsaka.shorturl.exception.UserAlreadyExistException;
import com.github.ahimsaka.shorturl.security.MyUserDetails;
import com.github.ahimsaka.shorturl.service.UrlRecordService;
import com.github.ahimsaka.shorturl.service.impl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

@RestController
public class UrlRecordController {

    private Logger log = LoggerFactory.getLogger(UrlRecordController.class);
    private UrlRecordService urlRecordService;
    private UserService userService;

    UrlRecordController(UrlRecordService urlRecordService, UserService userService){
        this.urlRecordService = urlRecordService;
        this.userService = userService;
    }

    @GetMapping("/login")
    public ModelAndView showLoginForm() {
        ModelAndView mav = new ModelAndView("login.html");
        return mav;
    }

    @GetMapping("/user")
    public ModelAndView showUserPage(Authentication authentication){
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

        ModelAndView mav = new ModelAndView("user.html", "urls", userDetails.getUser().getUrlRecords());
        return mav;
    }

    @GetMapping("/user/registration")
    public ModelAndView showRegistrationForm() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("registration");
        UserDto userDto = new UserDto();

        modelAndView.getModel().put("user", userDto);
        return modelAndView;
    }

    @PostMapping("/user/registration")
    public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto,
                                            HttpServletRequest request, Errors errors) {

        ModelAndView mav = new ModelAndView();
        try {
            User registered = userService.registerNewUserAccount(userDto);
        } catch (UserAlreadyExistException uaeEx) {
            mav.addObject("message", "An account for that username/email already exists.");
            return mav;
        }
        return new ModelAndView("successRegister", "user", userDto);
    }

    @PostMapping("/url")
    public String saveUrlRecord(UrlRecord urlRecord){
        UrlRecord record = urlRecordService.saveUrlRecord(urlRecord);
        if (record != null) {
            return record.getExtension();
        } else {
            return "Error: UrlRecord not saved.";
        }
    }

    @PostMapping("/user")
    public RedirectView saveUserUrlRecord(UrlRecord urlRecord, Authentication authentication){
        UrlRecord savedUrlRecord = urlRecordService.saveUrlRecord(urlRecord);
        User user = ((MyUserDetails) authentication.getPrincipal()).getUser();
        user.getUrlRecords().add(savedUrlRecord);
        userService.saveUser(user);
        return new RedirectView("/user");
    }

    @GetMapping("/delete/{extension}")
    public RedirectView deleteUrlRecord(@PathVariable(name = "extension") String extension, HttpServletRequest request) {
        urlRecordService.deleteUrlRecord(urlRecordService.getUrlRecordByExtension(extension));
        return new RedirectView("/admin");
    }

    @GetMapping("/admin")
    public ModelAndView adminPage() {
        return new ModelAndView("admin", "urls", urlRecordService.getAllUrlRecords());
    }

    @GetMapping("/url/{extension}")
    public RedirectView getUrlRecordByExtension(@PathVariable(name = "extension") String extension){
        RedirectView redirectView = new RedirectView();
        redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        redirectView.setUrl(urlRecordService.getUrlRecordByExtension(extension).getUrl());
        return redirectView;
    }


}

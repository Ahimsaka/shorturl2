package com.github.ahimsaka.shorturl.controller;

import com.github.ahimsaka.shorturl.entity.UrlRecord;
import com.github.ahimsaka.shorturl.entity.User;
import com.github.ahimsaka.shorturl.security.MyUserDetails;
import com.github.ahimsaka.shorturl.service.IUserService;
import com.github.ahimsaka.shorturl.service.UrlRecordService;
import com.github.ahimsaka.shorturl.service.impl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UrlRecordController {

    private Logger log = LoggerFactory.getLogger(UrlRecordController.class);
    private UrlRecordService urlRecordService;
    private IUserService userService;


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
        if (!user.getUrlRecords().contains(savedUrlRecord)){
            user.getUrlRecords().add(savedUrlRecord);
            user = userService.saveRegisteredUser(user);
            MyUserDetails myUserDetails = new MyUserDetails(user);
            ((MyUserDetails) authentication.getPrincipal()).setUser(user);
        }
        return new RedirectView("/user");
    }

    @GetMapping("/delete/{extension}")
    public RedirectView deleteUrlRecord(@PathVariable(name = "extension") String extension, HttpServletRequest request,
                                        Authentication authentication) {
        User user = ((MyUserDetails) authentication.getPrincipal()).getUser();

        user.getUrlRecords().removeIf(urlRecord -> {
            return urlRecord.getExtension().equals(extension);
        });

        userService.saveRegisteredUser(user);

        urlRecordService.deleteUrlRecord(urlRecordService.getUrlRecordByExtension(extension));
        return new RedirectView(request.getHeader("referer"));
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

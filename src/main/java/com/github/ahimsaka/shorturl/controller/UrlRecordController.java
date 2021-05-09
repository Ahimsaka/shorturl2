package com.github.ahimsaka.shorturl.controller;

import com.github.ahimsaka.shorturl.dto.UserDto;
import com.github.ahimsaka.shorturl.entity.UrlRecord;
import com.github.ahimsaka.shorturl.entity.User;
import com.github.ahimsaka.shorturl.entity.VerificationToken;
import com.github.ahimsaka.shorturl.exception.UserAlreadyExistException;
import com.github.ahimsaka.shorturl.registration.OnRegistrationCompleteEvent;
import com.github.ahimsaka.shorturl.security.MyUserDetails;
import com.github.ahimsaka.shorturl.service.IUserService;
import com.github.ahimsaka.shorturl.service.UrlRecordService;
import com.github.ahimsaka.shorturl.service.impl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Locale;

@RestController
public class UrlRecordController {

    private Logger log = LoggerFactory.getLogger(UrlRecordController.class);
    private UrlRecordService urlRecordService;
    private IUserService userService;
    private ApplicationEventPublisher applicationEventPublisher;
    private MessageSource messages;

    UrlRecordController(UrlRecordService urlRecordService, UserService userService,
                        ApplicationEventPublisher applicationEventPublisher, MessageSource messages){
        this.urlRecordService = urlRecordService;
        this.userService = userService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.messages = messages;
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

        try {
            User registered = userService.registerNewUserAccount(userDto);

            String appUrl = request.getContextPath();
            applicationEventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), appUrl));

        } catch (UserAlreadyExistException uaeEx) {

            ModelAndView mav = new ModelAndView("registration", "user", userDto);
            mav.addObject("message", "An account for that username/email already exists.");
            return mav;
        } catch (RuntimeException ex) {
            log.info(ex.getMessage());
            return new ModelAndView("emailError", "user", userDto);
        }

        return new ModelAndView("successRegister", "user", userDto);
    }

    @GetMapping("/registrationConfirm.html")
    public RedirectView confirmRegistration(WebRequest request,
                                            @RequestParam("token") String token) {
        RedirectView redirectView = new RedirectView();

        Locale locale = request.getLocale();

        VerificationToken verificationToken = userService.getVerificationToken(token);

        if (verificationToken == null) {
           String message = messages.getMessage("auth.message.invalidToken", null, locale);
           redirectView.addStaticAttribute("message", message);
           redirectView.setUrl("/badUser.html?lang=" + locale.getLanguage());
           return redirectView;
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
           String messageValue = messages.getMessage("auth.message.expired", null, locale);
           redirectView.addStaticAttribute("message", messageValue);
           redirectView.setUrl("/badUser.html?lang=" + locale.getLanguage());
           return redirectView;
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        redirectView.setUrl("/login");
        return redirectView;
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
            userService.saveRegisteredUser(user);
        }
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

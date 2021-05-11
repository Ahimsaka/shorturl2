package com.github.ahimsaka.shorturl.controller;

import com.github.ahimsaka.shorturl.dto.UserDto;
import com.github.ahimsaka.shorturl.entity.User;
import com.github.ahimsaka.shorturl.entity.VerificationToken;
import com.github.ahimsaka.shorturl.exception.UserAlreadyExistException;
import com.github.ahimsaka.shorturl.exception.util.GenericResponse;
import com.github.ahimsaka.shorturl.registration.OnRegistrationCompleteEvent;
import com.github.ahimsaka.shorturl.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
public class RegistrationController {
    private final Logger log = LoggerFactory.getLogger(RegistrationController.class);
    private IUserService userService;
    private ApplicationEventPublisher applicationEventPublisher;
    private MessageSource messages;
    private JavaMailSender mailSender;

    RegistrationController(IUserService userService, ApplicationEventPublisher applicationEventPublisher,
                           MessageSource messages, JavaMailSender mailSender) {
        this.userService = userService;
        this.messages = messages;
        this.applicationEventPublisher = applicationEventPublisher;
        this.mailSender = mailSender;
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
            return new ModelAndView("emailError", "user", userDto);
        }

        return new ModelAndView("successRegister", "user", userDto);
    }

    @GetMapping("/registrationConfirm")
    public ModelAndView confirmRegistration(WebRequest request,
                                            @RequestParam("token") String token) {
        ModelAndView mav = new ModelAndView();

        //RedirectView redirectView = new RedirectView();

        Locale locale = request.getLocale();

        VerificationToken verificationToken = userService.getVerificationToken(token);

        if (verificationToken == null) {
            String message = messages.getMessage("auth.message.invalidToken", null, locale);
            //redirectView.addStaticAttribute("message", message);
            //redirectView.setUrl("/badUser.html?lang=" + locale.getLanguage());
            //return redirectView;
            mav.setViewName("badUser");
            mav.getModel().put("message", message);
            return mav;
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();

        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            String messageValue = messages.getMessage("auth.message.expired", null, locale);
            //redirectView.addStaticAttribute("message", messageValue);
            //redirectView.addStaticAttribute("token", token);
            //redirectView.addStaticAttribute("expired", true);
            //redirectView.setUrl("/badUser.html?lang=" + locale.getLanguage());
            //return redirectView;
            mav.setViewName("badUser");
            mav.getModel().put("message", messageValue);
            mav.getModel().put("token", token);
            mav.getModel().put("expired", true);
            return mav;

        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        //redirectView.setUrl("/login");
        //redirectView.addStaticAttribute("message",
        //        messages.getMessage("message.accountVerified", null, locale));
        //return redirectView;
        mav.setViewName("login");
        mav.getModel().put("message", messages.getMessage("message.accountVerified", null, locale));
        return mav;
    }

    @GetMapping("/user/resendRegistrationToken")
    public GenericResponse resendRegistrationToken(HttpServletRequest request,
                                                   @RequestParam("token") String existingToken) {
        VerificationToken newToken = userService.generateNewVerificationToken(existingToken);

        User user = userService.getUser(newToken.getToken());

        String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() +
                request.getContextPath();

        SimpleMailMessage email = constructResendVerificationTokenEmail(appUrl, request.getLocale(), newToken, user);
        mailSender.send(email);

        return new GenericResponse(messages.getMessage("message.resendToken", null, request.getLocale()));
    }

    //   NON API

    private SimpleMailMessage constructResendVerificationTokenEmail (String contextPath, Locale locale,
                                                                     VerificationToken newToken, User user){
        String confirmationUrl = contextPath + "/registrationConfirm?token=" + newToken.getToken();

        String message = messages.getMessage("message.resendToken", null, locale);

        SimpleMailMessage email = new SimpleMailMessage();

        email.setSubject("Resend Registration Token");
        email.setText(message + " \r\n" + confirmationUrl);
        email.setTo(user.getUsername());
        return email;
    }
}

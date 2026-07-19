package com.apple.inc.user.controllers;

import com.apple.inc.log.standardization.annotations.ApiRecord;
import com.apple.inc.log.standardization.annotations.CompleteLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @ApiRecord
    @CompleteLog
    @GetMapping("/")
    public String sampleMethod() {
        return "sample method response";
    }

}

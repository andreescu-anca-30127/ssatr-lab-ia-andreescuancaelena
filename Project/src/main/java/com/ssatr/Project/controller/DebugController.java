package com.ssatr.Project.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @GetMapping("/debug/whoami")
    public String whoami(Authentication auth) {
        if (auth == null) return "NO_AUTH";
        return "AUTH=" + auth.getName() + " | authorities=" + auth.getAuthorities();
    }
}

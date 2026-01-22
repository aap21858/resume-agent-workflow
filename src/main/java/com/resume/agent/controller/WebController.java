package com.resume.agent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web UI controller for HTML pages.
 */
@Controller
public class WebController {
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
    
    @GetMapping("/requirements")
    public String requirements() {
        return "requirements";
    }
    
    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }
    
    @GetMapping("/workflow")
    public String workflow() {
        return "workflow";
    }
    
    @GetMapping("/analysis")
    public String analysis() {
        return "analysis";
    }
}

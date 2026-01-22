package com.resumeagent.controller;

import com.resumeagent.model.AnalysisResult;
import com.resumeagent.model.Candidate;
import com.resumeagent.model.ClientRequirement;
import com.resumeagent.model.InterviewPrep;
import com.resumeagent.service.AnalysisService;
import com.resumeagent.service.CandidateService;
import com.resumeagent.service.ClientService;
import com.resumeagent.service.InterviewPrepService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class WebController {
    
    private final ClientService clientService;
    private final CandidateService candidateService;
    private final AnalysisService analysisService;
    private final InterviewPrepService interviewPrepService;
    
    public WebController(ClientService clientService,
                        CandidateService candidateService,
                        AnalysisService analysisService,
                        InterviewPrepService interviewPrepService) {
        this.clientService = clientService;
        this.candidateService = candidateService;
        this.analysisService = analysisService;
        this.interviewPrepService = interviewPrepService;
    }
    
    @GetMapping("/")
    public String index(Model model) {
        List<Candidate> candidates = candidateService.getAllCandidates();
        List<ClientRequirement> clients = clientService.getAllClients();
        model.addAttribute("candidateCount", candidates.size());
        model.addAttribute("clientCount", clients.size());
        return "index";
    }
    
    @GetMapping("/clients")
    public String clients(Model model) {
        List<ClientRequirement> clients = clientService.getAllClients();
        model.addAttribute("clients", clients);
        return "clients";
    }
    
    @GetMapping("/candidates")
    public String candidates(Model model) {
        List<Candidate> candidates = candidateService.getAllCandidates();
        model.addAttribute("candidates", candidates);
        return "candidates";
    }
    
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }
    
    @GetMapping("/analyze")
    public String analyzePage(Model model) {
        List<Candidate> candidates = candidateService.getAllCandidates();
        List<ClientRequirement> clients = clientService.getAllClients();
        List<AnalysisResult> analyses = analysisService.getAllAnalyses();
        model.addAttribute("candidates", candidates);
        model.addAttribute("clients", clients);
        model.addAttribute("analyses", analyses);
        return "analyze";
    }
    
    @GetMapping("/analysis/{candidateId}/{clientId}")
    public String analysisDetail(@PathVariable String candidateId, 
                                 @PathVariable String clientId,
                                 Model model) {
        try {
            AnalysisResult analysis = analysisService.getAnalysis(candidateId, clientId);
            Candidate candidate = candidateService.getCandidate(candidateId);
            ClientRequirement client = clientService.getClient(clientId);
            model.addAttribute("analysis", analysis);
            model.addAttribute("candidate", candidate);
            model.addAttribute("client", client);
            return "analysis-detail";
        } catch (IOException e) {
            return "redirect:/analyze";
        }
    }
    
    @GetMapping("/interview-prep")
    public String interviewPrepPage(Model model,
                                   @RequestParam(required = false) String candidateId,
                                   @RequestParam(required = false) String clientId) {
        if (candidateId != null && clientId != null) {
            try {
                InterviewPrep prep = interviewPrepService.getInterviewPrep(candidateId, clientId);
                Candidate candidate = candidateService.getCandidate(candidateId);
                ClientRequirement client = clientService.getClient(clientId);
                model.addAttribute("prep", prep);
                model.addAttribute("candidate", candidate);
                model.addAttribute("client", client);
            } catch (IOException e) {
                // Ignore and show empty page
            }
        }
        
        List<Candidate> candidates = candidateService.getAllCandidates();
        List<ClientRequirement> clients = clientService.getAllClients();
        model.addAttribute("candidates", candidates);
        model.addAttribute("clients", clients);
        return "interview-prep";
    }
}

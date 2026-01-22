package com.resumeagent.controller;

import com.resumeagent.model.ClientRequirement;
import com.resumeagent.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    
    private final ClientService clientService;
    
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }
    
    @PostMapping
    public ResponseEntity<ClientRequirement> createClient(@RequestBody Map<String, String> request) {
        try {
            String rawRequirement = request.get("requirement");
            ClientRequirement client = clientService.createClient(rawRequirement);
            return ResponseEntity.ok(client);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ClientRequirement>> getAllClients() {
        List<ClientRequirement> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientRequirement> getClient(@PathVariable String clientId) {
        try {
            ClientRequirement client = clientService.getClient(clientId);
            return ResponseEntity.ok(client);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

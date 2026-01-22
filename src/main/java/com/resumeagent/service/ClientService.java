package com.resumeagent.service;

import com.resumeagent.agent.RequirementsParserAgent;
import com.resumeagent.model.ClientRequirement;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClientService {
    
    private final FileStorageService fileStorage;
    private final RequirementsParserAgent parserAgent;
    
    public ClientService(FileStorageService fileStorage, RequirementsParserAgent parserAgent) {
        this.fileStorage = fileStorage;
        this.parserAgent = parserAgent;
    }
    
    public ClientRequirement createClient(String rawRequirement) throws IOException {
        ClientRequirement requirement = parserAgent.parse(rawRequirement);
        fileStorage.saveJson(requirement, "clients/" + requirement.getId() + ".json");
        return requirement;
    }
    
    public ClientRequirement getClient(String clientId) throws IOException {
        return fileStorage.loadJson("clients/" + clientId + ".json", ClientRequirement.class);
    }
    
    public List<ClientRequirement> getAllClients() {
        List<ClientRequirement> clients = new ArrayList<>();
        try {
            List<String> files = fileStorage.listFiles("clients");
            for (String file : files) {
                if (file.endsWith(".json")) {
                    ClientRequirement client = fileStorage.loadJson("clients/" + file, ClientRequirement.class);
                    clients.add(client);
                }
            }
        } catch (IOException e) {
            // Return empty list on error
        }
        return clients;
    }
}

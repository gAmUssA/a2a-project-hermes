package com.a2a.kafka.web;

import com.a2a.kafka.agents.registry.AgentRegistryEntry;
import com.a2a.kafka.agents.registry.AgentRegistryRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(path = "/registry", produces = MediaType.APPLICATION_JSON_VALUE)
public class RegistryController {

    private final AgentRegistryRepository repository;

    public RegistryController(AgentRegistryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Collection<AgentRegistryEntry> list() {
        return repository.findAll();
    }

    @GetMapping("/{name}")
    public AgentRegistryEntry get(@PathVariable("name") String name) {
        return repository.findByName(name);
    }
}

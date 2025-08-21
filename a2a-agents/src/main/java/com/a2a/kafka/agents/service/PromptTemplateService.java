package com.a2a.kafka.agents.service;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class PromptTemplateService {

    private final Map<String, String> templates = new HashMap<>();

    public PromptTemplateService() {
        // Seed with a few basic templates; can be extended later or loaded from external sources
        templates.put("translator", "Translate the following text to ${targetLanguage}: ${text}");
        templates.put("summarizer", "Summarize the following content in a ${lengthHint} form: ${text}");
        templates.put("general", "You are a helpful AI assistant. Answer the user's request: ${text}");
    }

    public Map<String, String> getTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    public void putTemplate(String name, String template) {
        templates.put(name, template);
    }

    public boolean hasTemplate(String name) {
        return templates.containsKey(name);
    }

    public String render(String name, Map<String, Object> variables) {
        String template = templates.get(name);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + name);
        }
        if (variables == null) variables = Collections.emptyMap();
        return StringSubstitutor.replace(template, variables, "${", "}");
    }
}

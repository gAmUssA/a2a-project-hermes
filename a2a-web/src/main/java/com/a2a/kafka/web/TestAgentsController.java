package com.a2a.kafka.web;

import com.a2a.kafka.agents.gp.GeneralPurposeAgent;
import com.a2a.kafka.agents.summarizer.SummarizerAgent;
import com.a2a.kafka.agents.translator.TranslationResult;
import com.a2a.kafka.agents.translator.TranslatorAgent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
public class TestAgentsController {

    private final TranslatorAgent translator;
    private final SummarizerAgent summarizer;
    private final GeneralPurposeAgent gp;

    public TestAgentsController(TranslatorAgent translator, SummarizerAgent summarizer, GeneralPurposeAgent gp) {
        this.translator = translator;
        this.summarizer = summarizer;
        this.gp = gp;
    }

    public record TranslateRequest(String text, String targetLanguage) {}

    @PostMapping(path = "/translate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TranslationResult translate(@RequestBody TranslateRequest req) {
        return translator.translate(req.text(), req.targetLanguage());
    }

    public record SummarizeRequest(String text, String lengthHint) {}
    public record SummarizeResponse(String summary) {}

    @PostMapping(path = "/summarize", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SummarizeResponse summarize(@RequestBody SummarizeRequest req) {
        String out = summarizer.summarize(req.text(), req.lengthHint());
        return new SummarizeResponse(out);
    }

    public record GpRequest(String text, Integer maxTokens, Double temperature) {}
    public record GpResponse(String response) {}

    @PostMapping(path = "/gp", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GpResponse generalPurpose(@RequestBody GpRequest req) {
        String out;
        if (req.maxTokens() != null || req.temperature() != null) {
            out = gp.respond(req.text(), req.maxTokens(), req.temperature());
        } else {
            out = gp.respond(req.text());
        }
        return new GpResponse(out);
    }

    @GetMapping(path = "/ping")
    public Map<String, Object> ping() {
        return Map.of("status", "ok");
    }
}

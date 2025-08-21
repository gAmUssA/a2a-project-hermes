package com.a2a.kafka.agents.translator;

public class TranslationResult {
    private final String translatedText;
    private final String sourceLanguage;
    private final String targetLanguage;
    private final double confidence;

    public TranslationResult(String translatedText, String sourceLanguage, String targetLanguage, double confidence) {
        this.translatedText = translatedText;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.confidence = confidence;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public double getConfidence() {
        return confidence;
    }
}

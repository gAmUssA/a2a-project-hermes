# Running and Validating Agents Standalone (No Kafka)

This guide explains how to run and validate the Translator, Summarizer, and General‑Purpose LLM agents on their own, without Kafka or the orchestrator.

## Prerequisites
- Java 21
- Git, Gradle Wrapper
- Optional: OpenAI API key for live LLM calls

```bash
# Verify Java
java -version

# Clone & build
./gradlew clean build -x test
./gradlew test
```

If all tests pass, your environment is ready. The unit tests already verify most agent behaviors with a stubbed LLM. The steps below let you exercise the agents manually.

## Option A: Quick Validation via Tests (Recommended)
Run the agent test suites. These use a stubbed ChatService and validate input handling, safety filters, streaming, etc.

```bash
# All modules
./gradlew clean test

# Just agents module
./gradlew :a2a-agents:test
```

You can also run individual test classes from your IDE:
- a2a-agents/src/test/java/com/a2a/kafka/agents/translator/TranslatorAgentTest.java
- a2a-agents/src/test/java/com/a2a/kafka/agents/summarizer/SummarizerAgentTest.java
- a2a-agents/src/test/java/com/a2a/kafka/agents/gp/GeneralPurposeAgentTest.java

Passing tests confirm the agents function on their own (no Kafka required).

## Option B: Manual Validation Using JShell (Stubbed LLM)
You can manually instantiate the agents in JShell using a simple stub of `ChatServiceClient`. This requires only the compiled classes and no external services.

1) Build the project to ensure compiled classes are available:
```bash
./gradlew :a2a-agents:classes
```

2) Start JShell with the compiled classes on the classpath (adjust path if needed):
```bash
jshell --class-path a2a-agents/build/classes/java/main:a2a-core/build/classes/java/main
```

3) In JShell, paste the following snippets.

- Common imports:
```java
import java.util.*;
import com.a2a.kafka.agents.service.ChatServiceClient;
```

- Create a tiny stub LLM:
```java
class StubChat implements ChatServiceClient {
  public String chat(String templateName, Map<String,Object> variables) {
    if ("translator".equals(templateName)) {
      return "[" + variables.get("targetLanguage") + "] " + variables.get("text");
    }
    if ("summarizer".equals(templateName)) {
      return "SUMMARY(" + variables.get("lengthHint") + "):" + variables.get("text");
    }
    return "RESP:" + variables.get("text");
  }
  public String chat(String t, Map<String,Object> v, String m, Double temp, Integer maxTok){
    return chat(t, v);
  }
}
var chat = new StubChat();
```

- TranslatorAgent:
```java
import com.a2a.kafka.agents.util.LanguageDetector;
import com.a2a.kafka.agents.translator.TranslatorAgent;
import com.a2a.kafka.agents.translator.TranslationResult;
var translator = new TranslatorAgent(chat, new LanguageDetector());
TranslationResult tr = translator.translate("Hello world", "es");
tr.getTranslatedText();
```
Expected: `[es] Hello world`

- SummarizerAgent:
```java
import com.a2a.kafka.agents.summarizer.SummarizerAgent;
var summarizer = new SummarizerAgent(chat);
String sum = summarizer.summarize("This is some sufficiently long content to summarize.", "short");
sum;
```
Expected example: `SUMMARY(short):This is some sufficiently long content to summarize.`

- GeneralPurposeAgent:
```java
import com.a2a.kafka.agents.gp.GeneralPurposeAgent;
var gp = new GeneralPurposeAgent(chat);
String resp = gp.respond("Please provide a safe response without hate speech.");
resp;
```
Expected example: `RESP:Please provide a safe response without [REDACTED] speech.` (redaction depends on content)

This confirms agents work standalone using a stubbed LLM.

## Option C: Manual Validation With Real OpenAI (Live LLM)
If you want to exercise the agents against a real model, you can start a Spring Boot context to obtain the real `ChatService` from the container and manually instantiate the agents around it.

1) Set environment variables:
```bash
export OPENAI_API_KEY=your-key
# Optional overrides
export OPENAI_MODEL=gpt-3.5-turbo
export OPENAI_TEMPERATURE=0.7
```

2) Start the Spring app (provides ChatModel/ChatClient/ChatService beans):
```bash
./gradlew :a2a-web:bootRun
```
This launches the application with Spring AI auto-configuration enabled (see a2a-web/src/main/resources/application.yml). Keep it running.

3) In a separate terminal, open JShell with the running app’s classpath. The simplest way is to run JShell from your IDE with the a2a-web module on the classpath. If using CLI, you can approximate with:
```bash
jshell --class-path \
  a2a-web/build/classes/java/main: \
  a2a-agents/build/classes/java/main: \
  a2a-core/build/classes/java/main
```
Note: Depending on your environment, you may need additional dependency jars on the classpath. Using your IDE’s JShell or a temporary scratch class inside a2a-web is often easier.

4) In JShell, wire the real ChatService and instantiate agents:
```java
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.SpringApplication;
import com.a2a.kafka.web.A2AWebApplication;
import com.a2a.kafka.agents.service.ChatService;
import com.a2a.kafka.agents.util.LanguageDetector;
import com.a2a.kafka.agents.translator.TranslatorAgent;

ConfigurableApplicationContext ctx = SpringApplication.run(A2AWebApplication.class);
ChatService chatSvc = ctx.getBean(ChatService.class);
var translator = new TranslatorAgent(chatSvc, new LanguageDetector());
translator.translate("Hello from live model!", "fr").getTranslatedText();
```
You should receive a live translation from the configured model. You can similarly create `SummarizerAgent` and `GeneralPurposeAgent` using `chatSvc`.

Tip: Stop the application with Ctrl+C and close the JShell session after you’re done.

## Troubleshooting
- openai.api-key is required for live calls. Ensure `OPENAI_API_KEY` is set or adjust `a2a-web/src/main/resources/application.yml`.
- Network/proxy issues may prevent reaching OpenAI endpoints; check logs.
- To avoid live costs, use Option A or B (tests or stubbed JShell).
- Agents are not exposed as REST endpoints by default. They are plain Java classes and can be instantiated directly.

## Summary
- Fastest validation: run tests (`./gradlew :a2a-agents:test`).
- Manual validation without external services: use JShell + stubbed `ChatServiceClient`.
- Live validation with real LLM: run the a2a-web app, obtain `ChatService` from Spring, and instantiate agents around it.

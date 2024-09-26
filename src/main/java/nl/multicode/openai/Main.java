package nl.multicode.openai;

import static dev.langchain4j.data.message.UserMessage.userMessage;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;

public class Main {


    public static void main(String[] args) {

        ChatLanguageModel model = OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(300, new OpenAiTokenizer());

        chatMemory.add(userMessage("In the middle of december 2013 a child was born at noon "));
        AiMessage answer = model.generate(chatMemory.messages()).content();
        chatMemory.add(answer);

        chatMemory.add(userMessage("Can you format the date-time of birth in ISO format and only answer with the formatted date?"));

        AiMessage answe = model.generate(chatMemory.messages()).content();
        System.out.println(answe.text());
        chatMemory.add(answe);
    }
}

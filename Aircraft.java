package dev.datainmotion;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;

/**
 *
 */
public class Aircraft {
    public static final String AIRCRAFTRAG = "aircraftrag";
    public static final String FLIGHTS = "Tell me about flight EDV5295";
    public static final String OLLAMAHOST = "localhost";
    public static final int DIMENSION = 384;
    //static String MODEL_NAME = "llama3.2";
    static String MODEL_NAME = "llama3.2:3b-instruct-fp16";
    //static String MODEL_NAME3 = "llama3.1";
    static Integer PORT = 11434;
    private static final String MILVUS_HOST = "http://192.168.1.166:19530";

    /**
     * RAG
     *
     * https://docs.langchain4j.dev/tutorials/ai-services#streaming
     *
     */
    public static void rag(String prompt) {
        try {
            EmbeddingStore<TextSegment> embeddingStore = MilvusEmbeddingStore.builder()
                    .uri(MILVUS_HOST)
                    .collectionName(AIRCRAFTRAG)
                    .dimension(DIMENSION)
                    .build();

            interface Assistant {
                String chat(String userMessage);
            }

            ChatLanguageModel chatModel = OllamaChatModel.builder()
                    .baseUrl(String.format("http://%s:%d", OLLAMAHOST, PORT ))
                    .modelName(MODEL_NAME)
                    .temperature(0.0)
                    .build();

            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatLanguageModel(chatModel)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                    .build();

            System.out.println("****Prompt=" +prompt);
            String answer = assistant.chat(prompt);

            System.out.println("****Answer=\n" + answer);
        }catch(Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("***RAG against Ollama with " + MODEL_NAME);

        String prompt = FLIGHTS;

        if ( args.length >0  && args[0] != null) {
            System.out.println(args[0]);
            prompt = args[0];
        }

        rag(prompt);
        System.exit(0);
    }
}

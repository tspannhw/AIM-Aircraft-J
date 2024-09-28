package dev.datainmotion;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class Aircraft {

    public static final String AIRCRAFTRAG = "aircraftrag";
    public static final String FLIGHTS = "Tell me about flight EDV5295";
    public static final String OLLAMAHOST = "localhost";
    public static final int DIMENSION = 384;
    static String MODEL_NAME = "llama3.2";
    static Integer PORT = 11434;
    private static final String MILVUS_HOST = "http://192.168.1.166:19530";

    /**
     * RAG
     *
     * https://docs.langchain4j.dev/tutorials/ai-services#streaming
     *
     */
    public static void rag() {
        //(MilvusContainer milvus = new MilvusContainer("milvusdb/milvus:v2.4.5"))
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

            System.out.println(FLIGHTS);
            String answer = assistant.chat(FLIGHTS);

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
        System.out.println("***RAG against Ollama with Llama 3.2");
        rag();
        System.exit(0);
    }
}

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
public class AircraftSearch {
    public static final String AIRCRAFTRAG = "aircraftrag";
    public static final String FLIGHTS = "Tell me about all Delta Air Lines flights";
    public static final String OLLAMAHOST = "localhost";
    public static final int DIMENSION = 384;
    static String MODEL_NAME = "llama3.2";
    static Integer PORT = 11434;
    private static final String MILVUS_HOST = "http://192.168.1.166:19530";

    /**
     *
     */
    private static void query() {
        EmbeddingStore<TextSegment> embeddingStore = MilvusEmbeddingStore.builder()
                .uri(MILVUS_HOST)
                .collectionName(AIRCRAFTRAG)
                .dimension(DIMENSION)
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        Embedding queryEmbedding = embeddingModel.embed(FLIGHTS).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(10)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

        List<EmbeddingMatch<TextSegment>> embeddingMatches = searchResult.matches();

        for (EmbeddingMatch<TextSegment> embeddingMatch: embeddingMatches) {
            System.out.println("Score: " + embeddingMatch.score()); // 0.8144287765026093
            System.out.println("Match: " + embeddingMatch.embedded().text()); // I like football.
        }
    }

    /**
     * todo add search parameter
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("**** Search ****");
        query();
        System.exit(0);
    }
}
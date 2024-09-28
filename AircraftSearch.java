package dev.datainmotion;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import java.util.List;

/**
 *
 */
public class AircraftSearch {
    public static final String AIRCRAFTRAG = "aircraftrag";
    public static final String FLIGHTS = "Tell me about all Delta Air Lines flights";
    public static final int DIMENSION = 384;
    private static final String MILVUS_HOST = "http://192.168.1.166:19530";

    /**
     *
     */
    private static void query(String searchCriteria) {
        EmbeddingStore<TextSegment> embeddingStore = MilvusEmbeddingStore.builder()
                .uri(MILVUS_HOST)
                .collectionName(AIRCRAFTRAG)
                .dimension(DIMENSION)
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        Embedding queryEmbedding = embeddingModel.embed(searchCriteria).content();

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
        String searchCriteria = FLIGHTS;

        if ( args.length >0  && args[0] != null) {
            System.out.println(args[0]);
            searchCriteria = args[0];
        }
        query(searchCriteria);
        System.exit(0);
    }
}
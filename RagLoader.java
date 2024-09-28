package dev.datainmotion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.QueryResp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class RagLoader {

    /***
     * @TODO: move to configuration
     */
    private static final String ID_FIELD = "id";
    private static final String TEXT_FIELD = "text";
    private static final String VECTOR_FIELD = "vector";
    private static final String JSON_FIELD = "metadata";
    private static final String MILVUS_HOST = "http://192.168.1.166:19530";
    private static final String collectionName = "aircraftrag";
    private static final String sourceCollectionName = "liveplanes";
    public static final String FILTER = "flightidentifier != 'NA' ";

    /**
     * @TODO:   move constants
     * @param args
     */
    public static void main(String[] args) {
        loader();
        System.exit(0);
    }

    /**
     * search from liveplanes collection and insert matching into special RAG collection
     */
    private static void loader() {
        ConnectConfig config = ConnectConfig.builder()
                .uri(MILVUS_HOST)
                .build();
        MilvusClientV2 client = new MilvusClientV2(config);

        List<String> fields = Arrays.asList("id","details","plane_text_vector","icao","geometricaltitude",
                "groundspeed", "latitude", "longitude","flightidentifier");

        QueryResp insertList = client.query(QueryReq.builder()
                .collectionName(sourceCollectionName)
                .filter(FILTER)
                .outputFields(fields)
                .consistencyLevel(ConsistencyLevel.EVENTUALLY)
                .limit(2500L)
                .build());

        List<JsonObject> rows = new ArrayList<>();
        Gson gson = new Gson();

        // Copy from liveplanes to RAG
        for (QueryResp.QueryResult result : insertList.getQueryResults()) {
            JsonObject metadata = new JsonObject();
            metadata.addProperty("icao", String.valueOf( result.getEntity().getOrDefault("icao","NA") ));
            metadata.addProperty("geometricaltitude", String.valueOf( result.getEntity().getOrDefault("geometricaltitude","NA") ));
            metadata.addProperty("groundspeed", String.valueOf( result.getEntity().getOrDefault("groundspeed","NA") ));
            metadata.addProperty("latitude", String.valueOf( result.getEntity().getOrDefault("latitude","NA") ));
            metadata.addProperty("longitude", String.valueOf( result.getEntity().getOrDefault("longitude","NA") ));
            metadata.addProperty("flightidentifier", String.valueOf( result.getEntity().getOrDefault("flightidentifier","NA") ));

            JsonObject row = new JsonObject();
            row.addProperty(ID_FIELD, String.valueOf( result.getEntity().getOrDefault("id","1000") ) );
            row.addProperty(TEXT_FIELD, (String)result.getEntity().getOrDefault("details","NA"));
            row.add(JSON_FIELD,metadata);
            row.add(VECTOR_FIELD, gson.toJsonTree ( result.getEntity().get("plane_text_vector") ) );
            rows.add(row);
        }

        InsertResp insertR = client.insert(InsertReq.builder()
                .collectionName(collectionName)
                .data(rows)
                .build());
        System.out.printf("%d rows inserted\n", insertR.getInsertCnt());

        client.close();
    }
}
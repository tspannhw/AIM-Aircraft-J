package dev.datainmotion;

import io.milvus.v2.client.*;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.response.*;
import java.util.*;
import com.google.gson.*;
import io.milvus.v2.client.*;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.*;

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

    /**
     * @TODO:   move constants
     * @param args
     */
    public static void main(String[] args) {
        ConnectConfig config = ConnectConfig.builder()
                .uri(MILVUS_HOST)
                .build();
        MilvusClientV2 client = new MilvusClientV2(config);

        String collectionName = "aircraftrag";
        String sourceCollectionName = "liveplanes";

        List<String> fields = Arrays.asList("id","details","plane_text_vector","icao","geometricaltitude","groundspeed", "latitude",
                "longitude","flightidentifier");

        QueryResp insertList = client.query(QueryReq.builder()
                .collectionName(sourceCollectionName)
                .filter("flightidentifier != 'NA' ")
                .outputFields(fields)
                .consistencyLevel(ConsistencyLevel.EVENTUALLY)
                .limit(500L)
                .build());

        List<JsonObject> rows = new ArrayList<>();
        Gson gson = new Gson();

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
        System.exit(0);
    }
}
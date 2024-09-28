package dev.datainmotion;

import io.milvus.v2.client.*;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.response.*;
import java.util.*;

/**
 * search milvus with java
 *
 * https://java.testcontainers.org/modules/milvus/
 */
public class Main {

    private static final String MILVUS_HOST = "http://192.168.1.166:19530";
    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        ConnectConfig config = ConnectConfig.builder()
                .uri(MILVUS_HOST)
                .build();
        MilvusClientV2 client = new MilvusClientV2(config);

        String collectionName = "liveplanes";

        QueryResp countR = client.query(QueryReq.builder()
                .collectionName(collectionName)
                .filter("")
                .outputFields(Collections.singletonList("count(*)"))
                .consistencyLevel(ConsistencyLevel.EVENTUALLY)
                .build());
        System.out.printf("%d rows persisted\n", (long)countR.getQueryResults().get(0).getEntity().get("count(*)"));

        // Retrieve
        List<Object> ids = Arrays.asList(452498223117728851L,452498223117728836L,452498223117728802L);
        List<String> fields = Arrays.asList("url_photo","latitude","longitude","groundspeed","location","details");
        GetResp getR = client.get(GetReq.builder()
                .collectionName(collectionName)
                .ids(ids)
                .outputFields(fields)
                .build());
        System.out.println("\nRetrieve results:");
        for (QueryResp.QueryResult result : getR.getGetResults()) {
            System.out.println(result.getEntity());
        }

        QueryResp queryr = client.query(QueryReq.builder()
                .collectionName(collectionName)
                .filter("groundspeed > 0 && flightidentifier != \"NA\"")
                .outputFields(fields)
                .consistencyLevel(ConsistencyLevel.EVENTUALLY)
                .build());
        for (QueryResp.QueryResult result : queryr.getQueryResults()) {
            System.out.println(result.getEntity());

           String photourl = (String)result.getEntity().getOrDefault("url_photo","");
           System.out.println("Photo:" + photourl);
        }

        client.close();
    }
}
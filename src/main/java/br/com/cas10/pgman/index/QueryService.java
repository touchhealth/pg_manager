package br.com.cas10.pgman.index;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.IndexTemplatesExistRequest;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.time.DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT;

@Service
public class QueryService {

    private static final String INDEX_NAME = "postgres-query";
    private static final String INDEX_NAME_PREFIX = "postgres-query-";

    private boolean initialized = false;
    private RestHighLevelClient elasticsearch;

    public QueryService(RestHighLevelClient elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    @PostConstruct
    public void initializeIndex() {
        while (!initialized) {
            try {
                final String templatePattern = INDEX_NAME_PREFIX + "*";
                IndexTemplatesExistRequest request = new IndexTemplatesExistRequest(INDEX_NAME);
                boolean exists = elasticsearch.indices().existsTemplate(request, RequestOptions.DEFAULT);
                if (!exists) {
                    PutIndexTemplateRequest createRequest = new PutIndexTemplateRequest(INDEX_NAME);
                    createRequest.patterns(Arrays.asList(templatePattern));
                    Map<String, Object> jsonMap = new HashMap<>();
                    {
                        Map<String, Object> properties = new HashMap<>();
                        {
                            Map<String, Object> timestamp = new HashMap<>();
                            timestamp.put("type", "date");
                            timestamp.put("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis");
                            properties.put("@timestamp", timestamp);

                            Map<String, Object> query = new HashMap<>();
                            query.put("type", "text");
                            {
                                Map<String, Object> fields = new HashMap<>();
                                {
                                    Map<String, Object> keyword = new HashMap<>();
                                    keyword.put("type", "keyword");
                                    keyword.put("ignore_above", 10240);
                                    fields.put("keyword", keyword);
                                }
                                query.put("fields", fields);
                            }
                            properties.put("query", query);
                        }
                        jsonMap.put("properties", properties);
                    }
                    createRequest.mapping(jsonMap);
                    elasticsearch.indices().putTemplate(createRequest, RequestOptions.DEFAULT);
                }
                initialized = true;
            } catch (Exception e) {
                System.err.println("Erro ao conectar no elasticsearch");
            }
            if (!initialized) {
                try {
                    System.out.println("Esperando ElasticSearch...");
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void save(QuerySnapshot... data) {
        if (data == null) return;
        long timestamp = System.currentTimeMillis();
        String date = ISO_8601_EXTENDED_DATE_FORMAT.format(timestamp);
        try {
            int count = 0;
            for (QuerySnapshot snapshot : data) {
                Map<String, Object> content = snapshot.toJson();
                content.put("@timestamp", timestamp);
                if (content != null) {
                    IndexRequest indexRequest = new IndexRequest(INDEX_NAME_PREFIX + date).source(content);
                    IndexResponse response = elasticsearch.index(indexRequest, RequestOptions.DEFAULT);
                    count++;
                }
            }
            System.out.println("enviados: " + count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

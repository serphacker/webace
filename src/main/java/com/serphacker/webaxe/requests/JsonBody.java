package com.serphacker.webaxe.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.nio.charset.StandardCharsets;

public class JsonBody implements PostBodyEntity {

    private final static ObjectMapper JSON = new ObjectMapper();

    public static JsonBody of(Object object) throws JsonProcessingException {
        String json = JSON.writeValueAsString(object);
        ContentType contentType = ContentType.create("application/json", StandardCharsets.UTF_8);
        return new JsonBody(new StringEntity(json, contentType));
    }

    StringEntity entity;

    public JsonBody(StringEntity entity) {
        this.entity = entity;
    }

    @Override
    public HttpEntity getHttpEntity() {
        return entity;
    }
}

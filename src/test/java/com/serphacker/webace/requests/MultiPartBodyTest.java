/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.requests;

import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.core5.http.HttpEntity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiPartBodyTest {

    @Test
    void encodeableCharset() throws IOException {
        final HttpEntity entity = MultiPartBody.builder(StandardCharsets.UTF_8)
            .add("string-key-ôî", "string-value-ôî")
            .add("byte-key-ôî", "byte-value-ôî".getBytes())
            .add("body-key-ôî", new ByteArrayBody("body-value-ôi".getBytes(), "filename"))
            .build().getHttpEntity();

        var value = new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(value.contains("string-key-ôî"));
        assertTrue(value.contains("byte-key-ôî"));
        assertTrue(value.contains("body-key-ôî"));
    }

    @Test
    void unencodeableCharset() throws IOException {
        final HttpEntity entity = MultiPartBody.builder(StandardCharsets.US_ASCII)
            .add("string-key-ôî", "string-value-ôî")
            .add("byte-key-ôî", "byte-value-ôî".getBytes())
            .add("body-key-ôî", new ByteArrayBody("body-value-ôi".getBytes(), "filename"))
            .build().getHttpEntity();

        var value = new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(value.contains("string-key-oi"));
        assertTrue(value.contains("byte-key-oi"));
        assertTrue(value.contains("body-key-oi"));
    }

}
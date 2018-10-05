/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.requests;

import org.apache.hc.core5.http.HttpEntity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormBodyTest {

    @Test
    void encodeableCharset() throws IOException {
        final HttpEntity entity = FormBody.of(
            StandardCharsets.UTF_8,
            "key-ôî", "value-ôî細"
        ).getHttpEntity();
        var value = new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("key-%C3%B4%C3%AE=value-%C3%B4%C3%AE%E7%B4%B0", value);
    }

    @Test
    void unencodeableCharset() throws IOException {
        final HttpEntity entity = FormBody.of(
            StandardCharsets.US_ASCII,
            "key-ôî", "value-ôî細"
        ).getHttpEntity();
        var value = new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("key-oi=value-oi", value);
    }

}
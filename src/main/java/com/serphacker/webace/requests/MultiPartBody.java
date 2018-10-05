/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.requests;

import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MultiPartBody implements PostBodyEntity  {

    public final static Logger LOG = LoggerFactory.getLogger(MultiPartBody.class);

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Charset charset) {
        return new Builder(charset);
    }

    public static final class Builder {
        Charset charset;
        ContentType contentType;
        MultipartEntityBuilder builder;
        boolean hasUnsupportedEncoding = false;


        public Builder() {
            this(StandardCharsets.UTF_8);
        }

        public Builder(Charset charset) {
            this.charset = charset;
            this.contentType = ContentType.create("form-data", charset);
            this.builder = MultipartEntityBuilder
                .create()
                .setCharset(charset)
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        }

        public Builder charset(Charset charset) {
            builder.setCharset(charset);
            return this;
        }

        public Builder add(String key, String value) {
            if(!CharsetFixer.canEncode(charset, key)) {
                key = CharsetFixer.forceASCII(key);
                hasUnsupportedEncoding = true;
            }

            if(!CharsetFixer.canEncode(charset, value)) {
                value = CharsetFixer.forceASCII(value);
                hasUnsupportedEncoding = true;
            }

            builder.addTextBody(key, value, contentType);
            return this;
        }

        public Builder add(String key, byte[] value) {
            if(!CharsetFixer.canEncode(charset, key)) {
                key = CharsetFixer.forceASCII(key);
                hasUnsupportedEncoding = true;
            }

            builder.addBinaryBody(key, value);
            return this;
        }

        public Builder add(String key, ContentBody value) {
            if(!CharsetFixer.canEncode(charset, key)) {
                key = CharsetFixer.forceASCII(key);
                hasUnsupportedEncoding = true;
            }

            builder.addPart(key, value);
            return this;
        }

        public MultiPartBody build() {
            if(hasUnsupportedEncoding){
                LOG.warn("failed to encode some post data to {} forced to ascii", charset.name());
            }
            return new MultiPartBody(builder.build());
        }
    }

    HttpEntity entity;

    private MultiPartBody(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public HttpEntity getHttpEntity() {
        return entity;
    }

}

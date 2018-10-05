/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.requests;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FormBody implements PostBodyEntity {

    public final static Logger LOG = LoggerFactory.getLogger(FormBody.class);

    public static FormBody of(String... parameters) {
        return of(StandardCharsets.UTF_8, parameters);
    }

    public static FormBody of(Charset charset, String... parameters) {
        boolean hasUnsupportedEncoding = false;

        if(parameters.length%2 != 0) {
            throw new IllegalArgumentException("uneven parameter number");
        }

        List<NameValuePair> pairs = new ArrayList<>();
        for (int i = 0; i < parameters.length; i+=2) {
            String key = parameters[i];
            String value = parameters[i+1];

            if(!CharsetFixer.canEncode(charset, key)) {
                hasUnsupportedEncoding = true;
                key = CharsetFixer.forceASCII(key);
            }

            if(!CharsetFixer.canEncode(charset, value)) {
                hasUnsupportedEncoding = true;
                value = CharsetFixer.forceASCII(value);
            }

            pairs.add(new BasicNameValuePair(key, value));
        }

        if(hasUnsupportedEncoding){
            LOG.warn("failed to encode some post data to {} forced to ascii", charset.name());
        }

        return new FormBody(new UrlEncodedFormEntity(pairs, charset));
    }

    UrlEncodedFormEntity entity;

    public FormBody(UrlEncodedFormEntity entity) {
        this.entity = entity;
    }

    @Override
    public HttpEntity getHttpEntity() {
        return entity;
    }

}

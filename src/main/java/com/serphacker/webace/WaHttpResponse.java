/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace;

import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WaHttpResponse {

    final static Pattern HTML_CHARSET_PATTERN = Pattern.compile("charset=['\"]?([^\"'\\s]+)");

    HttpResponse httpResponse;
    HttpClientContext context;
    long executionTimeMilli;
    Exception exception;
    byte[] data;

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public HttpClientContext getContext() {
        return context;
    }

    public void setContext(HttpClientContext context) {
        this.context = context;
    }

    public int code() {
        if(httpResponse == null || exception != null) {
            return -1;
        }

        return httpResponse.getCode();
    }

    public byte[] data() {
        return data;
    }

    public void setContent(byte[] content) {
        this.data = content;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getLastRedirect() {
        if(context == null){
            return null;
        }

        RedirectLocations redirects = context.getRedirectLocations();
        if(redirects == null || redirects.size() == 0){
            return null;
        }

        return redirects.get(redirects.size()-1).toString();
    }

    public long getExecutionTimeMilli() {
        return executionTimeMilli;
    }

    public void executionTimerStart() {
        executionTimeMilli = System.currentTimeMillis();
    }

    public void executionTimerStop() {
        executionTimeMilli = System.currentTimeMillis() - executionTimeMilli;
    }

    public String getHeader(String name) {
        if(httpResponse == null){
            return null;
        }

        Header header = httpResponse.getFirstHeader(name);
        if (header == null) {
            return null;
        }

        return header.getValue();
    }

    public String text() {
        if (data == null) {
            return null;
        }

        Charset charset = getDetectedCharset();

        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }

        return new String(data, charset);
    }

    public Charset getDetectedCharset() {
        if(httpResponse == null) {
            return null;
        }

        ContentType contentType = getContentType();
        if(contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        }

        return detectCharsetFromHtmlMeta();
    }

    protected ContentType getContentType() {
        if(httpResponse == null) {
            return null;
        }

        final Header header = httpResponse.getLastHeader(HttpHeaders.CONTENT_TYPE);
        if(header == null){
            return null;
        }

        try {
            return ContentType.parseLenient(header.getValue());
        } catch (Exception ex){
            return null;
        }
    }

    protected Charset detectCharsetFromHtmlMeta() {
        if (data == null) {
            return null;
        }

        int len = data.length > 4096 ? 4096 : data.length;
        Matcher matcher = HTML_CHARSET_PATTERN.matcher(new String(data, 0, len));
        if (matcher.find()) {
            try {
                return Charset.forName(matcher.group(1));
            } catch (Exception ex) {
                return null;
            }
        }

        return null;
    }

}

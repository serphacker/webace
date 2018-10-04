package com.serphacker.webaxe;

import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WaHttpResponseTest {

    @Test
    public void getContentType() {
        WaHttpResponse response = new WaHttpResponse();
        assertNull(response.getContentType());

        {
            BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(200);
            httpResponse.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/html"));
            response.setHttpResponse(httpResponse);
            assertEquals("text/html", response.getContentType().getMimeType());
            assertNull(response.getContentType().getCharset());
        }

        {
            BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(200);
            httpResponse.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8"));
            response.setHttpResponse(httpResponse);
            assertEquals("text/html", response.getContentType().getMimeType());
            assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());
        }

        {
            BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(200);
            httpResponse.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset=\"iso-8859-1\""));
            response.setHttpResponse(httpResponse);
            assertEquals("text/plain", response.getContentType().getMimeType());
            assertEquals(StandardCharsets.ISO_8859_1, response.getContentType().getCharset());
        }

        {
            BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(200);
            httpResponse.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/bou"));
            response.setHttpResponse(httpResponse);
            assertEquals("text/bou", response.getContentType().getMimeType());
            assertNull(response.getContentType().getCharset());
        }

        {
            BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(200);
            httpResponse.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/bou; charset='iso-8859-1'"));
            response.setHttpResponse(httpResponse);
            assertNull(response.getContentType());
        }

    }

    @Test
    public void detectCharsetFromHtmlMeta(){
        WaHttpResponse response = new WaHttpResponse();

        response.data = "qdsfqsdf<meta charset=\"utf-8\" />qsdfs".getBytes();
        assertEquals(StandardCharsets.UTF_8, response.detectCharsetFromHtmlMeta());

        response.data = "http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />".getBytes();
        assertEquals(StandardCharsets.UTF_8, response.detectCharsetFromHtmlMeta());

        response.data = "qdsfqsdf<meta charset=\"iso-8859-1\" />qsdfs".getBytes();
        assertEquals(StandardCharsets.ISO_8859_1, response.detectCharsetFromHtmlMeta());

        response.data = "http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />".getBytes();
        assertEquals(StandardCharsets.ISO_8859_1, response.detectCharsetFromHtmlMeta());

        response.data = "http-equiv=\"Content-Type\" content=\"text/html; charset=xxx\" />".getBytes();
        assertNull(response.detectCharsetFromHtmlMeta());
    }



}
package com.serphacker.webace;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.List;

public class WaHttpHeaders {

    List<Header> headers = new ArrayList<>();

    public void add(Header header) {
        headers.add(header);
    }

    public void add(String name, String value) {
        headers.add(new BasicHeader(name, value));
    }

    public void set(Header header) {
        remove(header.getName());
        headers.add(header);
    }

    public void set(String name, String value) {
        set(new BasicHeader(name, value));
    }

    public void remove(String name) {
        headers.removeIf((Header t) -> t.getName().toLowerCase().equals(name.toLowerCase()));
    }

    public void clear() {
        headers.clear();
    }

    public List<Header> list() {
        return headers;
    }

}

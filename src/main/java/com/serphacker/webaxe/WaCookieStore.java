package com.serphacker.webaxe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;

import java.util.List;
import java.util.function.Predicate;

public class WaCookieStore {

    BasicCookieStore store = new BasicCookieStore();

    public void add(Cookie cookie) {
        if(cookie.getDomain() == null) {
            throw new IllegalArgumentException("missing domain");
        }
        store.addCookie(cookie);
    }

    public void add(Cookie[] cookies) {
        for (var cookie: cookies) {
            add(cookie);
        }
    }

    public List<Cookie> list() {
        return store.getCookies();
    }

    public void clear() {
        store.clear();
    }

    public void remove(Predicate<Cookie> matching) {
        final List<Cookie> filteredCookies = store.getCookies();
        filteredCookies.removeIf(matching);
        store.clear();
        add(filteredCookies.toArray(new Cookie[filteredCookies.size()]));
    }

    public int size() {
        return store.getCookies().size();
    }

}

/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class WaCookieStoreTest {

    @Test
    public void addClear() {
        WaCookieStore store = new WaCookieStore();

        final BasicClientCookie c1 = new BasicClientCookie("c1", "v1");
        c1.setDomain("example.com");
        c1.setSecure(true);
        store.add(c1);

        final BasicClientCookie c2 = new BasicClientCookie("c2", "v2");
        c2.setDomain("example.com");
        store.add(c2);

        final BasicClientCookie c3 = new BasicClientCookie("c3", "v3");
        c3.setDomain("c3.example.com");
        c3.setPath("/path");
        c3.setAttribute("c3.attr.name", "c3.attr.val");
        c3.setExpiryDate(new Date(System.currentTimeMillis() + 10000000l));
        store.add(c3);

        assertEquals(3, store.size());

        Cookie[] cookies = new Cookie[]{
            new BasicClientCookie("c4", "v4"),
            new BasicClientCookie("c5", "v5"),
            new BasicClientCookie("c6", "v6")
        };
        for(var cookie: cookies) {
            ((BasicClientCookie)cookie).setDomain("boo.example.com");
        }

        store.add(cookies);

        assertEquals(6, store.size());

        store.remove(p -> "boo.example.com".equals(p.getDomain()));

        assertEquals(3, store.size());

        store.clear();
        assertEquals(0, store.size());
    }

    @Test
    public void ensureDomainPresent() {

        assertThrows(IllegalArgumentException.class, () -> {
            WaCookieStore store = new WaCookieStore();
            BasicClientCookie cookie = new BasicClientCookie("c1", "v1");
            store.add(cookie);
        });

        assertDoesNotThrow(() -> {
            WaCookieStore store = new WaCookieStore();
            BasicClientCookie cookie = new BasicClientCookie("c1", "v1");
            cookie.setDomain("example.com");
            store.add(cookie);
        });

    }

}
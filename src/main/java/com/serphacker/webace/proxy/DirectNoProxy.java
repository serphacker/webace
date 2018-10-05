/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.webace.proxy;


public class DirectNoProxy extends WaProxy {

    public final static DirectNoProxy INSTANCE = new DirectNoProxy();

    private DirectNoProxy() {
    }

    @Override
    public String toString() {
        return "proxy:none";
    }
}

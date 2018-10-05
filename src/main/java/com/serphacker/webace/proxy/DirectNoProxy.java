/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
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

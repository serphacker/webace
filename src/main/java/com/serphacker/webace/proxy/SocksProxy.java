/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.webace.proxy;

import java.util.Objects;


public class SocksProxy extends AuthentProxy {
    String ip;
    int port;

    public SocksProxy(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public SocksProxy(String ip, int port, String username, String password) {
        super(username, password);
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        String str = "proxy:socks://";
        if(username != null || password != null){
            str += username + ":" + password + "@";
        }
        str += ip + ":" + port + "/";
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SocksProxy that = (SocksProxy) o;
        return port == that.port &&
            Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ip, port);
    }

}

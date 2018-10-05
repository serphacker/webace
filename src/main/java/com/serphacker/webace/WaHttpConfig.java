package com.serphacker.webace;

public class WaHttpConfig {

    String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0";
    long timeoutMilli = 30_000l;
    int maxResponseLength = 4_000_000;
    int maxRedirect = 0;
    boolean trustAllSsl = false;
    WaHttpHeaders defaultHeaders = new WaHttpHeaders();


    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public long getTimeoutMilli() {
        return timeoutMilli;
    }

    public void setTimeoutMilli(long timeoutMilli) {
        this.timeoutMilli = timeoutMilli;
    }

    public int getMaxResponseLength() {
        return maxResponseLength;
    }

    public void setMaxResponseLength(int maxResponseLength) {
        this.maxResponseLength = maxResponseLength;
    }

    public void followRedirect(int maxRedirect) {
        this.maxRedirect = maxRedirect;
    }

    public boolean isTrustAllSsl() {
        return trustAllSsl;
    }

    public void setTrustAllSsl(boolean trustAllSsl) {
        this.trustAllSsl = trustAllSsl;
    }

    WaHttpHeaders defaultHeaders() {
        return defaultHeaders;
    }


}

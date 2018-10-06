/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace;

public class HttpDefaultPort {

    public static int determine(int port, String scheme) {
        if (port != -1) {
            return port;
        }

        if (scheme == null) {
            return 80;
        }

        switch (scheme) {
            case "http":
                return 80;
            case "https":
                return 443;
            case "ftp":
                return 21;
            default:
                return 80;
        }
    }

}

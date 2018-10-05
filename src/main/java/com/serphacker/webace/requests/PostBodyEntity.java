/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.requests;

import org.apache.hc.core5.http.HttpEntity;

public interface PostBodyEntity {
    HttpEntity getHttpEntity();
}

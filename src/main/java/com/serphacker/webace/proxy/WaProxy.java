/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class WaProxy {

    public Map<String,Object> context = new ConcurrentHashMap<>();

    public boolean hasAttr(String key){
        return context.containsKey(key);
    }

    public void setAttr(String key, Object value){
        context.put(key, value);
    }

    public <T> T getAttr(String key, Class<T> clazz){
        return (T) context.get(key);
    }

    public void removeAttr(String key){
        context.remove(key);
    }

    public void clearAttrs(){
        context.clear();
    }
}

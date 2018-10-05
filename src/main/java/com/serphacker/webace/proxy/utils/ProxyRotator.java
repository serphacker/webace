/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.webace.proxy.utils;

import com.serphacker.webace.proxy.WaProxy;

import java.util.*;


/**
 * thread safe
 * @author admin
 */
public class ProxyRotator {

    final Queue<WaProxy> proxies = new ArrayDeque<>();

    public ProxyRotator(Collection<WaProxy> proxies) {
        this.proxies.addAll(proxies);
    }
    
    public boolean addAll(Collection<WaProxy> proxies){
        synchronized(proxies){
            return this.proxies.addAll(proxies);
        }
    }
    
    public boolean add(WaProxy proxy){
        synchronized(proxy){
            return proxies.add(proxy);
        }
    }
    
    public WaProxy poll(){
        return rotate(null);
    }
    
    public WaProxy rotate(WaProxy previousProxy){
        synchronized(proxies){
            if(previousProxy != null){
                proxies.add(previousProxy);
            }
            return proxies.poll();
        }
    }
    
    public int remaining(){
        synchronized(proxies){
            return proxies.size();
        }
    }
    
    public List<WaProxy> list(){
        synchronized(proxies){
            return new ArrayList<>(proxies);
        }
    } 
    
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.servershepherd.beume;

/**
 *
 * @author marc
 */
public class CacheException extends Exception {

    public CacheException(String message) {
        super(message);
    }
    public CacheException(String message, Throwable ex) {
        super(message,ex);
    }
}

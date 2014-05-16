/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.servershepherd.beume;

import java.io.Serializable;

/**
 *
 * @author marc
 */
public class BackKey implements Serializable {

    public String source_url;
    public int resize_x;
    public int resize_y;
    public String resize_filter;
    public int mod;
    String serialized;

    public BackKey(String source_url, int resize_x, int resize_y, String resize_filter, int mod) {
        this.source_url = source_url;
        this.resize_x = resize_x;
        this.resize_y = resize_y;
        this.resize_filter = resize_filter;
        this.mod = mod;
    }

    public BackKey(FrontKey fk) {
        this.source_url = fk.source_url;
        this.resize_x = fk.resize_x;
        this.resize_y = fk.resize_y;
        this.resize_filter = fk.resize_filter;
        this.mod = fk.mod;
    }

    @Override
    public String toString() {
        return source_url;
    }

    @Override
    public int hashCode() {
        return source_url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof BackKey)) {
            return source_url.equals(((BackKey) obj).source_url);
        } else {
            return false;
        }
    }
}

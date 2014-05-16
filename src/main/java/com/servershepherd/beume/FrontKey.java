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
public class FrontKey implements Serializable {

    public String source_url;
    public int resize_x;
    public int resize_y;
    public String resize_filter;
    public int mod;
    private final String serialized;

    public FrontKey(String source_url, int resize_x, int resize_y, String resize_filter, int mod) {
        this.source_url = source_url;
        this.resize_x = resize_x;
        this.resize_y = resize_y;
        this.resize_filter = resize_filter;
        this.mod = mod;
        serialized = source_url + resize_x + resize_y + resize_filter + mod;
    }

    @Override
    public String toString() {
        return serialized;
    }

    @Override
    public int hashCode() {
        return serialized.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof FrontKey)) {
            return serialized.equals(((FrontKey) obj).serialized);
        }
        return false;
    }
}

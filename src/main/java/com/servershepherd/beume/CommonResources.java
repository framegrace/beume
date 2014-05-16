/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.servershepherd.beume;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;

/**
 *
 * @author marc
 */
public class CommonResources {
    
    public static String CONF_FILE=System.getProperty("user.home")+"/beume/beume.properties";
    public static String EHCACHE_CONF_FILE=System.getProperty("user.home")+"/beume/ehcache.xml";
    
    public static String DEFAULT_FILTER="lanczos";
    
    // Maximum image size for backend requests
    public static int BACK_REQ_MAX_LENGTH = 4 * 1024 * 1024;
    // Request or resize errors will be retained this time to avoid clients 
    // hammering the server or the remote backend provider with failing queries. 
    // After an error, it will not be checked again until this time has passed.
    // One for backend cache, and one for the Front one
    public static int BACK_ERROR_RETAIN_SECS = 30;
    public static int FRONT_ERROR_RETAIN_SECS = 10;
    
    // LISTEN HOST, PORT AND BASE_URL
    public static String BASE_URL = "/";
    public static String HOST = "localhost";
    public static int PORT = 8080;
    public static String ADM_BASE_URL = "/";
    public static String ADM_HOST = "localhost";
    public static int ADM_PORT = 8085;
    
    // Front and Backend caches.
    @NoStore
    public static SelfPopulatingCache back;
    @NoStore
    public static SelfPopulatingCache front;

    public static void initialize() throws IOException {
        
        Properties prop=new Properties();
        prop.load(new FileInputStream(CONF_FILE));
        for(Object so:prop.keySet()) {
            String s=(String)so;
            try {
                Field f=CommonResources.class.getField(s);
                if (f.getType()==Integer.class||f.getType()==int.class) {
                    f.setInt(null, Integer.parseInt(prop.getProperty(s)));
                } else if (f.getType()==String.class) {
                    f.set(null, prop.getProperty(s));
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(CommonResources.class.getName()).log(Level.SEVERE, null, ex);
            }
        }  
        back = SPCacheFactory.createCache("backCache", new BackCacheUpdater());
        front = SPCacheFactory.createCache("frontCache", new FrontCacheUpdater(CommonResources.back));
    }
    public static void saveConfig() throws IllegalArgumentException, IllegalAccessException, FileNotFoundException, IOException {
        Properties prop=new Properties();
        for (Field f:CommonResources.class.getFields()) {
            if (f.getAnnotation(NoStore.class)==null) {
                prop.setProperty(f.getName(), f.get(CommonResources.class)+"");
            }
        }
        System.out.println(CONF_FILE);
        File f=new File(CONF_FILE);
        f.createNewFile();
        prop.store(new FileOutputStream(CONF_FILE), BASE_URL);
    }
    public static void main(String[] args) throws IOException {
        try {
            CommonResources.initialize();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CommonResources.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
@Retention(RetentionPolicy.RUNTIME)
@interface NoStore {
    
}
package com.servershepherd.beume;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 * @author marc
 */
class BackCacheUpdater implements UpdatingCacheEntryFactory {

    HttpClient client;

    public BackCacheUpdater() {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        client = new HttpClient(connectionManager);
    }

    @Override
    public void updateEntryValue(Object key, Object value) throws Exception {
        Object im=getImage(key);
        // Do not update the entry if its returning errors
        if (im instanceof String) return;
        value=im;
    }

    @Override
    public Object createEntry(Object key) {
        Object im=getImage(key);
        if (im instanceof String) {
            return new Element(key, im,0,CommonResources.BACK_ERROR_RETAIN_SECS);
        }
        return im;
    }

    private Object getImage(Object key) {
        byte[] v ;
        BackKey fk = (BackKey) key;
        String message="Unknown error";
        try {
            GetMethod method = new GetMethod();
            method.setURI(new URI(fk.source_url, true));
            int returnCode = client.executeMethod(method);
            if (returnCode == HttpStatus.SC_OK) {
                HashMap<String, Object> headers = extractHeaders(method);
                String contentType = (String) headers.get("content-type");
                Integer contentLength = (Integer) headers.get("content-length");
                if ((contentType != null) && (contentType.startsWith("image"))) {
                    if ((contentLength != null) && (contentLength < CommonResources.BACK_REQ_MAX_LENGTH)) {
                        // We could use getResponseBody As a Stream, but we will do 
                        // exactly the same as the library. Why bother.
                        v = method.getResponseBody();       
                        return v;
                    } else {
                        message="Request: " + fk.source_url + " above max length threshold (" + contentLength + "/" + CommonResources.BACK_REQ_MAX_LENGTH + ")";
                        Logger.getLogger(BackCacheUpdater.class.getName()).log(Level.WARNING,message);
                    }
                } else {
                    message="Request: " + fk.source_url + " is not an image (" + contentType + ")";
                    Logger.getLogger(BackCacheUpdater.class.getName()).log(Level.WARNING, message );
                }
            } else {
                message= "Request: " + fk.source_url + " failed. Return code : " + returnCode;
                Logger.getLogger(BackCacheUpdater.class.getName()).log(Level.WARNING, message);
            }
        } catch (IllegalStateException|URIException ex) {
            message="Request: " + fk.source_url + " URI problem. ";
            Logger.getLogger(BackCacheUpdater.class.getName()).log(Level.SEVERE, message, ex);
        } catch (IOException ex) {
            message="Request: " + fk.source_url + " IO Exception. ";
            Logger.getLogger(BackCacheUpdater.class.getName()).log(Level.SEVERE, message, ex);
        }
        return message;
    }

    private HashMap<String,Object> extractHeaders(GetMethod method) throws NumberFormatException {
        HashMap<String,Object> headers=new HashMap<>();
        for (Header a : method.getResponseHeaders()) {
            if (a.getName()!=null) {
                String pkey=a.getName().toLowerCase();
                Object value=a.getValue();
                switch (pkey) {
                    case "content-length":
                        value=Integer.parseInt((String)value);
                    default:
                        headers.put(pkey, value);
                }
            }
        }
        return headers;
    }
}

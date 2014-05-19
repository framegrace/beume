package com.servershepherd.beume;
 
import java.io.IOException;
 
import javax.ws.rs.core.UriBuilder;
 
import org.glassfish.grizzly.http.server.HttpServer;
 
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

 
public class App {
 
    protected static HttpServer startServer() throws IOException, CacheException {
        System.out.println("Starting Thumb Server...");
        ResourceConfig rc = new PackagesResourceConfig("com.servershepherd.beume.resources");
        return GrizzlyServerFactory.createHttpServer(UriBuilder.fromUri("http://"+CommonResources.HOST+CommonResources.BASE_URL).port(CommonResources.PORT).build(), rc);
    }
    protected static HttpServer startAdminServer() throws IOException {
        System.out.println("Starting admin Server...");
        ResourceConfig rc = new PackagesResourceConfig("com.servershepherd.beume.adminResources");
        return GrizzlyServerFactory.createHttpServer(UriBuilder.fromUri("http://"+CommonResources.ADM_HOST+CommonResources.ADM_BASE_URL).port(CommonResources.ADM_PORT).build(), rc);
    }
    
    public static void main(String[] args) throws IOException {
        Logger root = Logger.getLogger("");
        Handler[] handlers = root.getHandlers();
        for (Handler h : handlers) {
            h.setLevel(Level.INFO);
        }
        if (args.length>=1) CommonResources.CONF_FILE=args[1];
        
        Logger ehcacheL = Logger.getLogger("net.sf.ehcache");
        ehcacheL.setLevel(Level.INFO);
        CommonResources.initialize();
        if (CommonResources.back != null && CommonResources.front != null) {
            try {
                HttpServer httpServer = startServer();
                HttpServer adminServer = startAdminServer();
                System.out.println("Server Started");
                System.in.read();
                httpServer.stop();
                adminServer.stop();
            } catch (CacheException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "Caches not fully initialized... exiting");
        }
    }
}

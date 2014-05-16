package com.servershepherd.beume.adminResources;
 

import com.servershepherd.beume.SPCacheFactory;
import java.util.regex.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.statistics.StatisticsGateway;
 
/**
 *
 * @author marc
 */
@Path("/stats")
public class stats {
 
    Pattern p = Pattern.compile("^(.*)\\[([^\\]]+)\\]");
    @GET
    @Produces("text/html")
    public Response getStats() {
        return Response.status(Response.Status.BAD_REQUEST).entity(printStats(SPCacheFactory.manager)).type(MediaType.TEXT_HTML).build();
    }

    private String printStats(CacheManager cacheManager) {
        String[] cacheNames = cacheManager.getCacheNames();
        Runtime runtime = Runtime.getRuntime();
        String result="<HTML><head>\n" +
"<style>\n" +
"#stats\n" +
"{\n" +
"font-family:\"Trebuchet MS\", Arial, Helvetica, sans-serif;\n" +
"width:100%;\n" +
"border-collapse:collapse;\n" +
"}\n" +
"#stats td, #stats th \n" +
"{\n" +
"font-size:1em;\n" +
"border:1px solid #98bf21;\n" +
"padding:3px 7px 2px 7px;\n" +
"}\n" +
"#stats th \n" +
"{\n" +
"font-size:1.1em;\n" +
"text-align:center;\n" +
"padding-top:5px;\n" +
"padding-bottom:4px;\n" +
"background-color:#A7C942;\n" +
"color:#ffffff;\n" +
"}\n" +
"#stats tr.alt td \n" +
"{\n" +
"color:#000000;\n" +
"background-color:#EAF2D3;\n" +
"}\n" +
"#stats td.number\n" +
"{\n" +
"  text-align:right;\n" +
"}\n" +
"</style>" +
"</head>" + "<BODY><TABLE id=\"stats\"><tr><th>Cache Name</th><th>Size</th><th>Used Memory</th><th>Get Ops</th><th>Hits</th><th>Miss</th><th>Put</th><th>Evicted</th><th>Expired</th></tr>";
        boolean alt=false;
        for (String cacheName : cacheNames) {
            StatisticsGateway c = cacheManager.getCache(cacheName).getStatistics();
            result+=alt?"<tr><td>":"<tr><td class=\"alt\">";
            result+=cacheName + "</td><td class=\"number\">" + 
                    c.getSize()+"</td><td class=\"number\">"+
                    ((runtime.totalMemory()-runtime.freeMemory())/(1024*1024))+" MB</td><td class=\"number\">"+
                    c.cacheGetOperation().count().value()+"</td><td class=\"number\">"+
                    c.cacheHitCount()+"</td><td class=\"number\">"+
                    c.cacheMissCount()+"</td><td class=\"number\">"+
                    c.cachePutCount()+"</td><td class=\"number\">"+
                    c.cacheEvictedCount()+"</td><td class=\"number\">"+
                    c.cacheExpiredCount()+"</td></tr>";
                    
//            for(Object Key:cacheManager.getCache(cacheName).getKeys()) {
//                result+=("|"+Key+"|\n");
//            }
        }
        result+="</TABLE></BODY></HTML>";
        return result;
    }
}

package com.servershepherd.beume.resources;
 
import com.servershepherd.beume.CacheException;
import com.servershepherd.beume.CommonResources;
import com.servershepherd.beume.FrontKey;
import java.util.regex.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.sf.ehcache.Element;
 
/**
 *
 * @author marc
 */
@Path("/cidi.jpg")
public class Resizer {
 
    Pattern p = Pattern.compile("^(.*)\\[([^\\]]+)\\]");
    @GET
    @Produces("image/*")
    public Response getFullImage(@QueryParam("source") String source,
                                 @QueryParam("resize") String resize,
                                 @QueryParam("filter") String filter,
                                 @QueryParam("q") int mod) {
        
//      System.out.println("Source:"+source);
//      System.out.println("Resize:"+resize);
//      System.out.println("mod:"+mod);
        Element result;
        try {
            FrontKey fk = createKey(mod, source, resize, filter);
            result = CommonResources.getFrontCache().get(fk);
            if (result == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Image not found").build();
            } else if (result.getObjectValue() instanceof String) {
                return Response.status(Response.Status.BAD_REQUEST).entity(result.getObjectValue()).type(MediaType.TEXT_PLAIN).build();
            } else {
                return Response.ok(result.getObjectValue()).type("image/jpeg").build();    
            }
        } catch (CacheException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.toString()).type(MediaType.TEXT_PLAIN).build();
        }
    }

    private FrontKey createKey(int mod, String source, String resize, String filter) throws NumberFormatException, CacheException {
        if (mod>100||mod<0) throw new CacheException("Quality must be 0-100");
        int fmod = mod;
        String[] a = resize.split("x");
        if (a.length!=2) throw new CacheException("Wrong resize format \""+resize+"\" not \"WxH\"");
        int fresize_x,fresize_y;
        try {
            fresize_x = Integer.parseInt(a[0]);
            fresize_y = Integer.parseInt(a[1]);
        } catch (NumberFormatException nfe) {
            throw new CacheException("Wrong resize format (NaN)",nfe);
        }
        if ((filter==null)||("".equals(filter))) filter=CommonResources.DEFAULT_FILTER;
        return new FrontKey(source, fresize_x, fresize_y, filter, fmod);
    }
}

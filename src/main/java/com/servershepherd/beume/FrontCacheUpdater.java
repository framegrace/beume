/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.servershepherd.beume;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import net.coobird.thumbnailator.Thumbnails;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory;
import org.imgscalr.Scalr;

/**
 *
 * @author marc
 */
class FrontCacheUpdater implements UpdatingCacheEntryFactory {

    SelfPopulatingCache backCache;

    public FrontCacheUpdater(SelfPopulatingCache back) {
        backCache = back;
    }

    @Override
    public void updateEntryValue(Object key, Object value) {
        Object im = calculateResize(key);
        // Do not update the entry if its returning errors
        if (im instanceof String) {
            return;
        }
        value = im;
    }

    @Override
    public Object createEntry(Object key) {
        Object im = calculateResize(key);
        if (im instanceof String) {
            return new Element(key, im, 0, CommonResources.FRONT_ERROR_RETAIN_SECS);
        }
        return im;
    }

    private Object calculateResize(Object key) {
        FrontKey fk = (FrontKey) key;
        String message = "Unknown error";
        try {
            Element e = backCache.get(new BackKey(fk));
            byte[] res = null;
            if (e != null) {
                if (e.getObjectValue() instanceof String) {
                    // Backend generated an error
                    return e.getObjectValue();
                }
                byte[] img = (byte[]) e.getObjectValue();
                ByteArrayInputStream bais = new ByteArrayInputStream(img);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                switch (fk.resize_filter.toLowerCase()) {
                    case "lanczos":
                        // Lanczos3 rescale algorithm from:
                        // Always non proportion preserving
                        // https://code.google.com/p/java-image-scaling/
                        BufferedImage src = ImageIO.read(bais);
                        ResampleOp resampleOp = new ResampleOp(fk.resize_x, fk.resize_y);
                        resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.None);
                        resampleOp.setNumberOfThreads(2);
                        createJPEG(fk.mod, baos, resampleOp.filter(src, null));
                        src.flush();
                        break;
                    case "imgscalr" :
                        // imgscalr method (Always proportion preserving)
                        // http://www.thebuzzmedia.com/software/imgscalr-java-image-scaling-library/
                        // Lile slower than lanczos
                        BufferedImage src2 = ImageIO.read(bais);
                        createJPEG(fk.mod,baos,Scalr.resize(src2, Scalr.Method.QUALITY, fk.resize_x, fk.resize_y, Scalr.OP_ANTIALIAS));
                        src2.flush();
                        break;
                    default:
                        // Thumbnailator library method
                        // https://code.google.com/p/thumbnailator/
                        Thumbnails.of(bais)
                                .forceSize(fk.resize_x, fk.resize_y)
                                .outputFormat("jpg").outputQuality((double) fk.mod / 100.0).toOutputStream(baos);
                }
                res = baos.toByteArray();
                return res;
            }
        } catch (IOException ex) {
            message = "Request: " + fk.source_url + " resize failed.";
            Logger.getLogger(FrontCacheUpdater.class.getName()).log(Level.WARNING, message, ex);
        } catch (IllegalStateException ex2) {
            message = "Unknown Error " + ex2.getLocalizedMessage();
            Logger.getLogger(FrontCacheUpdater.class.getName()).log(Level.WARNING, message, ex2);
        }
        return message;
    }

    private void createJPEG(int mod, ByteArrayOutputStream baos, BufferedImage ri) throws IOException {
        Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = (ImageWriter) iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality((float) mod / (float) 100);
        writer.setOutput(ImageIO.createImageOutputStream(baos));
        IIOImage image = new IIOImage(ri, null, null);
        writer.write(null, image, iwp);
        writer.dispose();
        ri.flush();
    }
}

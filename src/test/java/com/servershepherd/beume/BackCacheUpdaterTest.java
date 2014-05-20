/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.servershepherd.beume;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.tools.Shell;
import net.sf.ehcache.Element;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author marc
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CommonResources.class)
public class BackCacheUpdaterTest {
    
    public BackCacheUpdaterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of updateEntryValue method, of class BackCacheUpdater.
     */
    @Test
    public void testUpdateEntryValue() throws Exception {
        System.out.println("updateEntryValue");
        mockConfiguration();
        BackKey key = new BackKey("http://localhost/image.jpg", 100, 100, "lanczos", 100);
        Object value = null;
        BackCacheUpdater instance = new BackCacheUpdater();
        instance.updateEntryValue(key, value);
        org.junit.Assert.assertNull("Ok", value);
    }

    /**
     * Test of createEntry method, of class BackCacheUpdater.
     */
    @Test
    public void testCreateEntry() {
        System.out.println("createEntry");
        mockConfiguration();
        // Check normal usage. Returns an image
        BackKey key = new BackKey("http://localhost/image.jpg", 100, 100, "lanczos", 100);
        BackCacheUpdater instance = new BackCacheUpdater();
        byte[] expResult = new byte[10];
        Object result = instance.createEntry(key);
        System.out.println("result:" + result);
        assertEquals("Not byte[]",result.getClass(), expResult.getClass());
        assertArrayEquals("Wrong response data",expResult,(byte[]) result);
        // Backend do not return an image
        key = new BackKey("http://localhost/image.jpg", 100, 100, "lanczos", 100);
        result = instance.createEntry(key);
        System.out.println("result:" + result);
        assertEquals("Not String (must return an error message)",result.getClass(), Element.class);
        assertTrue("Not returned no image",((String)((Element)result).getObjectValue()).contains("not an image"));
        // Backend returns a big image
        key = new BackKey("http://localhost/image.jpg", 100, 100, "lanczos", 100);
        result = instance.createEntry(key);
        System.out.println("result1:" + result);
        assertEquals("Not String (must return an error message)",result.getClass(), Element.class);
        assertTrue("Not returned no image",((String)((Element)result).getObjectValue()).contains("length threshold"));
        // Backend returns 404
        key = new BackKey("http://localhost/image.jpg", 100, 100, "lanczos", 100);
        result = instance.createEntry(key);
        System.out.println("result2:" + result);
        assertEquals("Not String (must return an error message)",result.getClass(), Element.class);
        assertTrue("Not returned no image",((String)((Element)result).getObjectValue()).contains("code : 404"));
    }

    private void mockConfiguration() {
        HttpClient mockClient = Mockito.mock(HttpClient.class);
        try {
            Mockito.when(mockClient.executeMethod(Mockito.any(GetMethod.class))).thenReturn(200).thenReturn(200).thenReturn(200).thenReturn(404);

            GetMethod getMethod = Mockito.mock(GetMethod.class);
            Header[] OkayHeader = new Header[2];
            OkayHeader[0] = new Header("content-type", "image/jpeg");
            OkayHeader[1] = new Header("content-length", "10");
            Header[] NoHeader = new Header[0];
            Header[] NoImageHeader = new Header[2];
            NoImageHeader[0] = new Header("content-type", "text/html");
            NoImageHeader[1] = new Header("content-length", "100");
            Header[] BigImageHeader = new Header[2];
            BigImageHeader[0] = new Header("content-type", "image/jpeg");
            BigImageHeader[1] = new Header("content-length", "20000000");
            Mockito.when(getMethod.getResponseHeaders()).thenReturn(OkayHeader).thenReturn(NoImageHeader).thenReturn(BigImageHeader).thenReturn(NoHeader);
            Mockito.when(getMethod.getResponseBody()).thenReturn(new byte[10]).thenReturn(new byte[20]).thenReturn(new byte[30]).thenReturn(new byte[40]);

            PowerMockito.mockStatic(CommonResources.class);

            Mockito.when(CommonResources.createGetMethod()).thenReturn(getMethod);
            Mockito.when(CommonResources.getHttpClient()).thenReturn(mockClient);
        } catch (IOException ex) {
            Logger.getLogger(BackCacheUpdaterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

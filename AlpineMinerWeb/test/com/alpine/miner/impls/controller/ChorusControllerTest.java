package com.alpine.miner.impls.controller;


import com.alpine.miner.impls.chorus.ChorusConfiguration;
import com.alpine.miner.impls.chorus.ChorusUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class ChorusControllerTest extends AbstractControllerTest {

    private static ChorusController chorusController;

    private static final String CHORUSHOST = "chorushost";
    private static final String CHORUSPORT = "chorusport";

    //private static final String cHost = "http://10.84.18.227";
    private static final String cHost = "http://10.84.18.1";
    private static final String cPort = "8080";



    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeChorusControllerTest() throws Exception {
        chorusController = new ChorusController();

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterChorusControllerTest() throws Exception {

    }

    @Test
    public void testChorusConnectionTest() throws ServletException, IOException {
        refreshMocks();

        HashMap<String, String> props = new HashMap<String, String>();
        props.put(CHORUSHOST, cHost);
        props.put(CHORUSPORT, cPort);
        ChorusConfiguration config = new ChorusConfiguration(props);
        String body = gson.toJson(config);

        request.setContent(body.getBytes());

        chorusController.testChorusConnection(request, response, null);

        Assert.assertEquals(200, response.getStatus());
        // we return 200 but this will timeout with no chorus server
        Assert.assertTrue(response.getContentAsString().indexOf("timed out") > 0);


    }

    @Test
    public void getChorusConfigTest() throws ServletException, IOException {
        refreshMocks();

        chorusController.getChorusConfig(request, response, null);
        String jsonString = response.getContentAsString();
        ChorusConfiguration config = gson.fromJson(jsonString, ChorusConfiguration.class);

        Assert.assertNotNull(config.getHost());
        Assert.assertFalse(config.getHost().equals(""));
        Assert.assertNotNull(config.getPort());
        Assert.assertFalse(config.getPort().equals(""));
        Assert.assertNotNull(config.getApiKey());
        Assert.assertFalse(config.getApiKey().equals(""));
        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void updateChorusConfigTest() throws ServletException, IOException {
        refreshMocks();

        HashMap<String, String> props = new HashMap<String, String>();
        props.put(CHORUSHOST, cHost);
        props.put(CHORUSPORT, cPort);
        ChorusConfiguration config = new ChorusConfiguration(props);
        String body = gson.toJson(config);

        request.setContent(body.getBytes());
        chorusController.updateChorusConfig(request, response, null);

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("{\"message\":\"success\"}", response.getContentAsString());


    }


    @Test
    public void getActiveWorkSpacesTest() throws ServletException, IOException {

        /* Need chorus server - else will timeout */
    }

    @Test
    public void publishFlowTest() throws ServletException, IOException {

        /* Need chorus server - else will timeout */
    }

    @Test
    public void runWorkFlowTest() throws ServletException, IOException {
        refreshMocks();
        chorusController.runWorkFlow("myID", "7daa9eb7-976b-4e93-ace7-63f2f9560f38", "1", "ChorusWorkfile", "UnitTestFlow", request, response, null);
        /* session info required, so this will return unauthorized */
        Assert.assertEquals(401,response.getStatus());


    }


    @Test
    public void getWorkFlowImageTest() throws ServletException, IOException, Exception {
        refreshMocks();
        //begin make image
        File f1 = new File("AlpineMinerWeb/WebContent/images/icons");
        String imgPath = f1.getAbsolutePath();
        File f2 = new File("AlpineMinerWeb/test_data/sample_variable");
        String flowPath = f2.getAbsolutePath();
        ChorusUtil.storeFlowImage(imgPath, flowPath, "myID");
        // end make img

        chorusController.getWorkFlowImage("myID", "7daa9eb7-976b-4e93-ace7-63f2f9560f38", request, response, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("image/png", response.getContentType());

    }

    @Test
    public void postCommentTest() throws ServletException, IOException {
        refreshMocks();

        ChorusController.ChorusComment chorusComment = new ChorusController.ChorusComment("my comment text!");
        String body = gson.toJson(chorusComment);

        request.setContent(body.getBytes());

        chorusController.postComment("1", "Workfile", "58", request, response, null);
        /* doesn't really test anything - we need session info */
        Assert.assertEquals(200,response.getStatus());
    }
}

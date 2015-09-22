/*
* ChorusConfigurationTest.java
*
* @author Robbie
*
* */

package com.alpine.miner.impls.chorus;

import com.alpine.miner.impls.flow.AbstractFlowTest;
import com.alpine.miner.impls.persistence.AbstractPersistenceTest;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.interfaces.ChorusConfigManager;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class ChorusConfigurationTest extends AbstractFlowTest {

    private static ChorusConfigManager chorusConfigManager = new ChorusConfigMgrImpl();
    private static ChorusConfiguration config;


    private static String preHost = "http://chorusUnitTestHost";
    private static String prePort = "9090";
    private static String preKey  = "12345678";

    private static String host = "http://chorushost";
    private static String port = "9191";


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeChildClass() throws Exception {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("chorushost", host);
        props.put("chorusport", port);
        config = new ChorusConfiguration(props);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterChildClass() throws Exception {
    }

    @Test
    public void testGetURLBase() throws Exception {
        String url = config.getURLBase("/route");
        Assert.assertEquals(host + ":" + port + "/route", url);
        Assert.assertNotNull(config.getApiKey());
    }

    @Test
    public void testHasAPIKey() throws Exception {
        Assert.assertNotNull(config.getApiKey());
    }

    @Test
    public void readConfig() throws Exception {
        ChorusConfiguration readConfig = chorusConfigManager.readConfig();
        Assert.assertEquals(prePort, readConfig.getPort());
        Assert.assertEquals(preHost, readConfig.getHost());
        Assert.assertEquals(preKey, readConfig.getApiKey());
    }

    @Test
    public void storeAndReadTest() throws Exception {

        chorusConfigManager.saveConfig(config);
        ChorusConfiguration readConfig = chorusConfigManager.readConfig();

        Assert.assertEquals(port, readConfig.getPort());
        Assert.assertEquals(host, readConfig.getHost());
        Assert.assertNotNull(config.getApiKey());

    }

}

/*
* AbstractPersistenceTest.java
*
* @author Robbie
*
* */

package com.alpine.miner.impls.persistence;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public abstract class AbstractPersistenceTest {

    private static final String TEST_DIR = "alpine_test";
    private static String original = null;
    public static final String test_root = System.getProperty("java.io.tmpdir") + TEST_DIR;

    private static File A_D_R = new File("AlpineMinerWeb/test_data/ALPINE_DATA_REPOSITORY");
    private static File root = new File(test_root);


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        original = System.getProperty("user.home");
        System.getProperties().put("user.home",test_root);
        FileUtils.copyDirectoryToDirectory(A_D_R, root);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        System.getProperties().put("user.home",original);

    }

    /*@Test
    public void confirmTest() {
        Assert.assertEquals(test_root, System.getProperty("java.io.tmpdir") + TEST_DIR);
    }*/

}

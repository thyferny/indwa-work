/*
* ChorusUtilTest.java
*
* @author Robbie
*
* */

package com.alpine.miner.impls.chorus;

import com.alpine.miner.impls.flow.AbstractFlowTest;
import com.alpine.miner.impls.persistence.AbstractPersistenceTest;
import com.alpine.miner.impls.web.resource.FilePersistence;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class ChorusUtilTest extends AbstractFlowTest {

    private static final String id = "myID";
    private static final String ext = ".ext";

    private File f1 = new File("AlpineMinerWeb/WebContent/images/icons");
    private String imgPath = f1.getAbsolutePath();
    private File f2 = new File("AlpineMinerWeb/test_data/sample_variable");
    private String flowPath = f2.getAbsolutePath();

    @Test
    public void testCreateFilePath() throws Exception {

        String path = ChorusUtil.createFilePath("myID", ".ext");
        File f = new File(FilePersistence.Chorus_PREFIX);
        String expected = f.getCanonicalPath() + File.separator + id + ext;

        Assert.assertEquals(expected, path);

    }

    @Test
    public void testStoreFlowFile() throws Exception {

        ChorusUtil.storeFlowFile(imgPath, flowPath, id);
        File f = new File(ChorusUtil.createFilePath("myID", FilePersistence.AFM));

        Assert.assertTrue(f.exists());

    }

    @Test
    public void testStoreFlowImage() throws Exception {

        ChorusUtil.storeFlowImage(imgPath, flowPath, id);
        File f = new File(ChorusUtil.createFilePath("myID", ".png"));

        Assert.assertTrue(f.exists());

    }

}

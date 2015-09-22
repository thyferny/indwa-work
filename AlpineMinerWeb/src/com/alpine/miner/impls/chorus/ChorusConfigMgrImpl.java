package com.alpine.miner.impls.chorus;

import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.interfaces.ChorusConfigManager;
import com.alpine.miner.utils.PropertiesEditor;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author robbie
 * 01/14/2013
 */

public class ChorusConfigMgrImpl implements ChorusConfigManager {
    private static Logger itsLogger = Logger.getLogger(ChorusConfigMgrImpl.class);

    private static final String CONFIG_PATH = FilePersistence.Preference_PREFIX+"chorusConfiguration.properties";

    @Override
    public ChorusConfiguration readConfig() {
        Map config = null;
        try {
            config = PropertiesEditor.readProp(CONFIG_PATH);
        } catch ( Exception e ) {
            itsLogger.error(e.getMessage(),e);
        }
        return new ChorusConfiguration(config);
    }

    @Override
    public void saveConfig(ChorusConfiguration config) throws Exception {
        PropertiesEditor.storeProp(config.returnProps(), CONFIG_PATH);
    }
}

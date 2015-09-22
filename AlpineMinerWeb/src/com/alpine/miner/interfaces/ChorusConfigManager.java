package com.alpine.miner.interfaces;

import com.alpine.miner.impls.chorus.ChorusConfigMgrImpl;
import com.alpine.miner.impls.chorus.ChorusConfiguration;

/**
 * @author robbie
 * 01/14/2013
 */

public interface ChorusConfigManager {

    public static ChorusConfigManager INSTANCE = new ChorusConfigMgrImpl();

    ChorusConfiguration readConfig();

    void saveConfig(ChorusConfiguration config) throws Exception;
}

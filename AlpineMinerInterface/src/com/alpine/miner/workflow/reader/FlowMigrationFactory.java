/**
 * ClassName FlowMigrationFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-26
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.reader;

import java.util.ArrayList;
import java.util.List;

import com.alpine.utility.file.StringUtil;

public class FlowMigrationFactory {
	
	private static List<String> versionSupportList=new ArrayList<String>();
	static {
		versionSupportList.add(FlowMigrator.Version_1);
		versionSupportList.add(FlowMigrator.Version_23);
		versionSupportList.add(FlowMigrator.Version_234);
		versionSupportList.add(FlowMigrator.Version_25);
		versionSupportList.add(FlowMigrator.Version_111);
	}
	
	public static FlowMigrator getMigrator(String sourceVersion,String targetVersion) {
		if((versionSupportList.contains(sourceVersion))
				&&targetVersion.equals(FlowMigrator.Version_3)) {
			return FlowMigrator23To3.INSTANCE;
		}else if(sourceVersion.equals(FlowMigrator.Version_3)
				&&targetVersion.equals(FlowMigrator.Version_23)){
			return FlowMigrator3To23.INSTANCE;
		}else if(StringUtil.isEmpty(sourceVersion)){
			return FlowMigrator23To3.INSTANCE;
		}
		return null;
	}

}

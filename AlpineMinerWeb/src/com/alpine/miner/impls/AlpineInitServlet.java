/**
 * 
 */
package com.alpine.miner.impls;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.ifc.HadoopConnectionManagerFactory;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.datasourcemgr.WebHadoopResourceManager;
import com.alpine.miner.impls.importdata.UploadDataService;
import com.alpine.miner.impls.license.LicenseManager;
import com.alpine.miner.impls.taskmanager.TaskManagerStore;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.interfaces.TempFileManager;
import com.alpine.miner.security.eula.EULAManager;
import com.alpine.miner.security.rolemgr.impl.FixedRoleManageServiceImpl;
import com.alpine.miner.utils.SysConfigManager;
import com.alpine.miner.workflow.operator.customize.UDFManager;
import com.alpine.utility.log.LogEvent;
import com.alpine.utility.log.LogPoster;
import org.apache.log4j.Logger;

/**
 * @author Gary
 *
 */
public class AlpineInitServlet extends HttpServlet {
    private static Logger itsLogger = Logger.getLogger(AlpineInitServlet.class);
    private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		//this is important and should be init first...
		initSysConfigManager();
		initTaskManager();	
		//make sure init the task manager first because the tempfile will use it..
		initTempFileManager(config);
		initPreference();
		initUDFManager();
		initDBManager();
		initHadoopManager();
        initLogger();
		
		LicenseManager.initialize();
		ResourceManager.getInstance().initJDBCDriverInfo();
		
		UploadDataService.INSTANCE.clearUploadData();
		FixedRoleManageServiceImpl.init();
		EULAManager.getInstance().init();
		
		//log system start-up
		LogEvent e = LogPoster.getInstance().createEvent("Server has been start-up.", null, null);
		LogPoster.getInstance().sendEvent(e);
	}

    //note: this needs to be called after initPreference()
    private void initLogger()
    {
        try
        {
            LogPoster.getInstance().startup(ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_LOG,PreferenceInfo.KEY_LOG_CUST_ID), ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_LOG,PreferenceInfo.KEY_LOG_OPT_OUT));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


	private void initSysConfigManager() {
		SysConfigManager.INSTANCE.init();
	
		
	}


	private void initTempFileManager(ServletConfig config) {
		String root=	config.getServletContext().getRealPath("/");//.getContextPath();
		try {
			TempFileManager.INSTANCE.init(root,SysConfigManager.INSTANCE.getLiveTime(),SysConfigManager.INSTANCE.getScanFrequency()) ;
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e) ;
			e.printStackTrace();
		}  
	}

 
	private void initDBManager() {
		//register the db manager
		DBResourceManagerFactory.INSTANCE.registerDBResourceManager(WebDBResourceManager.getInstance()) ;
	}
	
	private void initHadoopManager() {
        HadoopConnectionManagerFactory.INSTANCE.registerHadoopResourceManager(WebHadoopResourceManager.getInstance());
	}

	private void initUDFManager() {
		//tell the UDF manager the filePath to save
		UDFManager.INSTANCE.setRootDir(FilePersistence.UDFPRFIX);
		UDFManager.INSTANCE.setOperatorRegistryRootDir(FilePersistence.UDFPRFIX);
	}

	private void initPreference()   {
		//please be careful, this is very important to init the preference stuff
		ResourceManager rmgr = ResourceManager.getInstance();
		if(rmgr.isPreferenceInited()==false){
			rmgr.SetPreferenceInited(true);
			Collection<PreferenceInfo> preferences =null;
			try {
				preferences = rmgr.getPreferences();
			} catch (Exception e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e) ;
				//throw new ServletException(e);
			}//if user delete the preference file, then use the defualt value...
			if(preferences==null){
				preferences=rmgr.getPreferencesDefaultValue( );
			}
			for(Iterator<PreferenceInfo> it= preferences.iterator();it.hasNext();){ 
				PreferenceInfo pref = it.next();		 
				rmgr.updateProfileReader(pref);
			}
			
		}
	}

	private void initTaskManager() {
		TaskManagerStore.SCHEDULER.getInstance().startup();
		
	}

}

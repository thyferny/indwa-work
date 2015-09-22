package com.alpine.miner.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.OperatorPosition;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.util.AlpineUtil;
import com.alpine.util.FlowUtility;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;

/**   
 * ClassName:RequestUtil  
 *   
 * Author   kemp zhang   
 *
 * Version  Ver 1.0
 *   
 * Date     2011-3-29    
 *  
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.    
 */
public class RequestUtil {
    private static Logger itsLogger = Logger.getLogger(RequestUtil.class);
    public static DataStore flowToDataStore(OperatorWorkFlow flow) {
		List<UIOperatorConnectionModel> connLists = flow.getConnModelList();
		List<UIOperatorModel> operatorList = flow.getChildList();
		Iterator<UIOperatorModel>  iter = operatorList.iterator();
		DataStore ds = new DataStore();
		RowInfo row = new RowInfo();
		row.addColumn(Resources.WORKFLOW_UID);
		row.addColumn(Resources.WORKFLOW_NAME);
		row.addColumn(Resources.WORKFLOW_CLASSNAME);
		row.addColumn(Resources.WORKFLOW_X);
		row.addColumn(Resources.WORKFLOW_Y);
		row.addColumn(Resources.WORKFLOW_ICON);
		row.addColumn(Resources.WORKFLOW_ICONS);
		while(iter.hasNext()){
			UIOperatorModel operatorModel=iter.next();
			String className = operatorModel.getClassName();
			String name = operatorModel.getId();
			String uid = operatorModel.getUUID();
			OperatorPosition position = operatorModel.getPosition();
			int x = position.getX();
			int y = position.getY();
			
			String[] icons = ResourceManager.getInstance().getClassIcons(className);
			String[] item = new String[7];
			item[0]=uid;
			item[1]=name;
			item[2]=className;
			item[3]=x+"";
			item[4]=y+"";
			item[5]=icons[1];
			item[6]=icons[2];
			row.addRow(item);
		}
		ds.setRow(row);
		List<String[]> linkLists = FlowUtility.createLinkList(connLists);
		ds.setWorkFlowLinks(linkLists );
		return ds;
	}



	public static String toJson(DataStore ds) {
		StringBuffer jsonBuffer = new StringBuffer();
		jsonBuffer.append("{");
		jsonBuffer.append(JsonParam.JSON_RESUTL+":[");
		if(ds.getRow()!=null&&ds.getRow().getRowList()!=null){
			appendRowInfo(ds, jsonBuffer);
		}
		
		jsonBuffer.append("]");
		
		if(ds.getWorkFlowLinks()!=null && ds.getWorkFlowLinks().size() > 0)
		{
			appendWorkFlowInfo(ds, jsonBuffer);
		}
		jsonBuffer.append("}");
		
		return jsonBuffer.toString();
	}


	private static void appendRowInfo(DataStore ds, StringBuffer jsonBuffer) {
		//table header
		List<String> columnList=ds.getRow().getColumnList();
		
		for(int j=0;j<ds.getRow().getRowList().size();j++){
			
			jsonBuffer.append("{");
			for(int k=0;k<ds.getRow().getColumnList().size();k++){
				
				String key = AlpineUtil.addDoubleQuo(columnList.get(k)); 
				String msg = ds.getRow().getRowList().get(j)[k];
				if(StringUtil.isEmpty(msg)==false&&msg.trim().endsWith("\\")){
					msg =msg.trim()+ "\\";
				}
				String value = AlpineUtil.addDoubleQuo(msg);
				jsonBuffer.append(key+":"+value);
				if(k<ds.getRow().getColumnList().size()-1){
					jsonBuffer.append(",");
				}
			}
			jsonBuffer.append("}");
			if(j<ds.getRow().getRowList().size()-1){
				jsonBuffer.append(",");
			}
		}
	}


	private static void appendWorkFlowInfo(DataStore ds, StringBuffer jsonBuffer) {
		List<String []> list = ds.getWorkFlowLinks();
		jsonBuffer.append(","+JsonParam.JSON_LINKS+":[");
		for(int i=0;i<list.size();i++){
			jsonBuffer.append("{");
			String source = ((String[])list.get(i))[0];
			String target = ((String[])list.get(i))[1];
			List<String[]> item = ds.getRow().getRowList();
			for(int j = 0 ; j < item.size() ; j++)
			{
				String[] operator = item.get(j);
				if(operator[1].equals(source))
				{
					String key = AlpineUtil.addDoubleQuo(JsonParam.JSON_SOURCEID);
					String value = AlpineUtil.addDoubleQuo(operator[0]);
					jsonBuffer.append(key+":"+value+",");
					key = AlpineUtil.addDoubleQuo(JsonParam.JSON_X1);
					value = AlpineUtil.addDoubleQuo(operator[3]);
					jsonBuffer.append(key+":"+value+",");
					key = AlpineUtil.addDoubleQuo(JsonParam.JSON_Y1);
					value = AlpineUtil.addDoubleQuo(operator[4]);
					jsonBuffer.append(key+":"+value+",");
					break;
				}
			}
			
			for(int j = 0 ; j < item.size() ; j++)
			{
				String[] operator = item.get(j);
				if(operator[1].equals(target))
				{
					String key = AlpineUtil.addDoubleQuo(JsonParam.JSON_TARGETID);
					String value = AlpineUtil.addDoubleQuo(operator[0]);
					jsonBuffer.append(key+":"+value+",");
					key = AlpineUtil.addDoubleQuo(JsonParam.JSON_X2);
					
					value = AlpineUtil.addDoubleQuo(operator[3]);
					jsonBuffer.append(key+":"+value+",");
					
					key = AlpineUtil.addDoubleQuo(JsonParam.JSON_Y2);
					
					value = AlpineUtil.addDoubleQuo(operator[4]);
					
					jsonBuffer.append(key+":"+value);
					break;
				}
			}
			jsonBuffer.append("}");
			if(i<list.size()-1){
				jsonBuffer.append(",");
			}
		}
		jsonBuffer.append("]");
	}
	
	
	public static String getFromRequest(HttpServletRequest request) {
		String jsonRequest = "";
		try {
			javax.servlet.ServletInputStream servletinputstream = request.getInputStream();
			BufferedReader reader = (new BufferedReader(new InputStreamReader(
					/*TYPE_ERROR*/servletinputstream, Persistence.ENCODING)));
			StringBuffer sb = new StringBuffer();
			int c;
			while ((c = reader.read()) != -1)
				sb.append((char) c);
			jsonRequest = sb.toString();
		} catch (IOException e) {
			itsLogger.error(e.getMessage(),e);
			e.printStackTrace();
		}

//		JSONObject jsonObj = JSONObject.fromObject(jsonRequest);
		return jsonRequest;
	}
}

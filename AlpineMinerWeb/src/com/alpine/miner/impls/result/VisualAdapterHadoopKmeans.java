/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterKmeans.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.output.hadoop.HadoopKmeansOutput;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputBasicInfo;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputModel;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputProfiles;
import com.alpine.datamining.operator.hadoop.output.ClusterRangeInfo;
import com.alpine.datamining.operator.hadoop.output.KmeansValueRange;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.view.ui.dataset.ClusterAllEntity;
import com.alpine.miner.view.ui.dataset.ClusterScatterEntity;
import com.alpine.miner.workflow.output.visual.VisualPoint;
import com.alpine.miner.workflow.output.visual.VisualPointGroup;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelClusterProfile;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelLayered;
import com.alpine.miner.workflow.output.visual.VisualizationModelPieChart;
import com.alpine.miner.workflow.output.visual.VisualizationModelScatter;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.util.VisualUtils;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DataTypeConverter;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.tools.AlpineMath;

public class VisualAdapterHadoopKmeans extends DBUpdateOutPutVisualAdapter implements
		OutPutVisualAdapter {

	public static final VisualAdapterHadoopKmeans INSTANCE = new VisualAdapterHadoopKmeans();
	//max 200 , dedfault 30 ... 

	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
		// toVText((LinearRegressionModelDB)model.getModel())

		Object obj = null;
        ClusterOutputModel clusterModel = null;
		if (analyzerOutPut instanceof HadoopKmeansOutput) {
			obj = ((HadoopKmeansOutput) analyzerOutPut).getClusterModel();
			if (obj instanceof ClusterOutputModel) {
				clusterModel = (ClusterOutputModel) obj;
			}
		}
		String name = analyzerOutPut.getAnalyticNode().getName();
		String message = getMessage(clusterModel,  locale);
		VisualizationModelText textModel = new VisualizationModelText(VisualNLS.getMessage(VisualNLS.MESSAGE_TITLE,locale),
				message);// message);

		List<VisualizationModel> models = new ArrayList<VisualizationModel>();
	
		models.add(textModel);
        models.add(createProfileChart(clusterModel, locale));
        models.add(createSummaryTable(analyzerOutPut, clusterModel, locale));
        models.add(createCenterPointTable(clusterModel,  locale));
        VisualizationModel warningModel=createWarningText(clusterModel,  locale);

        if(warningModel!=null){
            models.add(warningModel);
        }

        try {
            VisualizationModel model = createScatterPoint(analyzerOutPut,clusterModel,  locale);
            //this is special --don't create report because too many pictures MINERWEB-448

            if(model!=null){
                model.setNeedGenerateReport(false);
                models.add(model);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e) ;

        }

		VisualizationModelComposite visualModel = new VisualizationModelComposite(
				name, models);

		return visualModel;

	}

	//profile is a layered composite model, 4 pie chart share the same legend...(use xlabels...)
	private VisualizationModel createProfileChart(ClusterOutputModel clusterModel,Locale locale) {
        ClusterOutputProfiles outputProfiles = clusterModel.getOutputProfiles();
        ClusterOutputBasicInfo basicInfo = clusterModel.getOutputText();
        List<String[]> list = null;
        List<String> tableHeader = null;
        if(outputProfiles!=null&&basicInfo!=null){

            list = new ArrayList<String[]>();
            int clusterCount = basicInfo.getClusterCount();
            String[] tableColumn=new String[3+clusterCount];

            tableColumn[0] = VisualLanguagePack.getMessage(VisualLanguagePack.VARIABLES, locale);
            tableColumn[1] = VisualLanguagePack.getMessage(VisualLanguagePack.STATES,locale);


            long totalRowCounts = outputProfiles.getTotalRowCounts();
            tableColumn[2] = VisualLanguagePack.getMessage(VisualLanguagePack.POPULATION,locale)+": "+totalRowCounts;

            List<ClusterRangeInfo> clusterRangeInfos = outputProfiles.getClusterRangeInfo();

            if(clusterRangeInfos!=null){
                for(int i=0;i<clusterRangeInfos.size();i++){
                    tableColumn[i+3]=VisualLanguagePack.getMessage(VisualLanguagePack.CLUSTER,locale)+clusterRangeInfos.get(i).getClusterName()+",Size: "
                            +clusterRangeInfos.get(i).getClusterRowCounts();
                }
            }
            tableHeader = createProfileTableHeader(tableColumn,  locale);

            List<String> columnNames = clusterModel.getColumnNames();
            Map<String, List<KmeansValueRange>> columnRangeMap = outputProfiles.getColumnRangeMap();
            if(columnNames!=null&&columnRangeMap!=null&&clusterRangeInfos!=null){
                for(String columnName:columnNames){
                    String[] tableItem=new String[3+clusterCount];
                    tableItem[0]=columnName;
                    List<KmeansValueRange> columnRangeList = columnRangeMap.get(columnName);
                    if(columnRangeList!=null){
                        StringBuffer sb=new StringBuffer();
                        for(KmeansValueRange valueRange:columnRangeList){
                            double minValue = valueRange.getMinValue();
                            double maxValue = valueRange.getMaxValue();
                            sb.append(minValue).append(" - ").append(maxValue).append(",");
                        }
                        sb=sb.deleteCharAt(sb.length()-1);
                        tableItem[1]=sb.toString();
                    }
                    Map<Integer,Long> sumMap=new HashMap<Integer,Long>();
                    for(int i=0;i<clusterRangeInfos.size();i++){
                        StringBuffer sb=new StringBuffer();
                        Map<String, List<Long>> columnRangeRowCountMap = clusterRangeInfos.get(i).getColumnRangeRowCountMap();
                        List<Long> columnRangeRowCountList = columnRangeRowCountMap.get(columnName);
                        int index=0;
                        for(Long l:columnRangeRowCountList){
                            sb.append(l).append(",");
                            if(sumMap.containsKey(index)){
                                Long oldSum = sumMap.get(index);
                                Long newSum=oldSum+l;
                                sumMap.put(index, newSum);
                            }else{
                                sumMap.put(index, l);
                            }
                            index++;
                        }
                        sb=sb.deleteCharAt(sb.length()-1);
                        tableItem[3+i]=sb.toString();
                    }
                    StringBuffer sb=new StringBuffer();
                    for(int i=0;i<sumMap.size();i++){
                        sb.append(sumMap.get(i)).append(",");
                    }
                    sb=sb.deleteCharAt(sb.length()-1);
                    tableItem[2]=sb.toString();

                    list.add(tableItem);
                }
            }
        }

		if(list == null || list.size()==0){
			return null;
		}

		List<List<VisualizationModel>> models = new ArrayList<List<VisualizationModel>>();
		for (String[] temp : list) {
			List<VisualizationModel> modelList = new ArrayList<VisualizationModel>();
			for (int i = 0; i < temp.length; i++) {
				String strArray=temp[i];
				VisualizationModel model = null;
				model = createProfileModel(i, strArray);
				modelList.add(model);
			}
			models.add(modelList);
		}


		VisualizationModelClusterProfile tableGroupModel =new VisualizationModelClusterProfile(
				VisualNLS.getMessage(VisualNLS.CLUSTER_PROFILES,locale), tableHeader, models)	;
		return tableGroupModel;
	}

	//this is the vmodel for each cell in the profile table
	private VisualizationModel createProfileModel(int index, String strArray) { 
		VisualizationModel model;
		if (index == 0) {
			// attribute name, the first column
			model = new VisualizationModelText("", strArray);
		} else if (index == 1) {
			String[] labels = strArray.split(",");
			model = new VisualizationModelPieChart("",
					Arrays.asList(labels), null);

		} else {
			//String numberStr = strArray.split(";")[1];
			String[] numbers = strArray.split(",");
			// the client will draw a new chart from the barchart model
			model = new VisualizationModelPieChart("", null,
					Arrays.asList(numbers));
		}
		return model;
	}



	public    HashMap<String, HashMap<String, VisualizationModel>>  generateOutPutMap(
			AnalyticOutPut analyzerOutPut, Locale locale) throws Exception {
        Object obj = null;
        ClusterOutputModel clusterModel = null;
        HashMap<String, HashMap<String, VisualizationModel>> eachClusterModel = null;

        if (analyzerOutPut instanceof HadoopKmeansOutput) {
            obj = ((HadoopKmeansOutput) analyzerOutPut).getClusterModel();
            if (obj instanceof ClusterOutputModel) {
                clusterModel = (ClusterOutputModel) obj;
            }
        }

        if (clusterModel == null){
            return null;
        }

        List<String> columnTypeList = clusterModel.getColumnTypes();
        List<String> columnNames = clusterModel.getColumnNames();
        Map<String,Map<String, List<Double>>> scatters = clusterModel.getOutputScatters();

        String[] defaultKey = new String[2];
        Map<String,Map<String, Double>>  listM = clusterModel.getCentroidsContents();//.getCenterPoint();
        ArrayList<String> keyLists = new ArrayList<String>();
        ArrayList<ArrayList<String>> list = getList(listM,keyLists);
        if (list != null && list.size() > 0) {
            ClusterAllEntity allEntity = new ClusterAllEntity();
            if (list.get(0).size() < 2){
                return null;
            }

            eachClusterModel = new HashMap<String, HashMap<String, VisualizationModel>>();

            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {// i=0 title name list
                    ClusterScatterEntity entity = new ClusterScatterEntity();
                    for (int j = 0; j < list.get(i).size(); j++) {
                            entity.addCenterPoint(list.get(0).get(j).toString(),
                                    Double.valueOf(list.get(i).get(j)));
                    }

                    List<String> doubleColumnList = new ArrayList<String>();
                    for (int n = 0; n < columnNames.size(); n++) {
                            if(isNumberColumnType(columnTypeList.get(n))){
                                doubleColumnList.add(columnNames.get(n));
                            }
                    }
                    filterDoubleColumn(doubleColumnList, entity);
                    if (entity.getCenterHt().size() < 2){
                           return null;
                    }
                    setDefautlQueryKey(defaultKey, entity);
                    setQueryColumn(allEntity, entity);

                    Map<String, List<Double>> scatterPoints =  scatters.get(String.valueOf(i-1));
                    if(null!=scatterPoints){
                             Set<String> set = entity.getCenterHt().keySet();
                             Iterator<String> iter = set.iterator();
                             while (iter.hasNext()) {
                                 String key = iter.next();
                                 if (entity.getDataHt().get(key) == null) {
                                     List<Double> integerList = new ArrayList<Double>();
                                     entity.getDataHt().put(key, integerList);
                                 }
                                 entity.getDataHt().get(key).addAll(scatterPoints.get(key));
                             }
                    }

                        Map<String, List<Double>> eachColumnArray = entity
                                .getDataHt();
                        Set<Entry<String, List<Double>>> entrySet = eachColumnArray
                                .entrySet();
                        Iterator<Entry<String, List<Double>>> iter = entrySet
                                .iterator();
                        while (iter.hasNext()) {
                            Entry<String, List<Double>> entry = iter.next();
                            String columnName = entry.getKey();
                            HashMap<String, VisualizationModel> eachPoint = 	eachClusterModel.get(columnName);
                            if(eachPoint==null){
                                eachPoint=new HashMap<String, VisualizationModel>();
                            }

                            List<Double> columnValues = entry.getValue();
                            Iterator<Entry<String, List<Double>>> newIter = entrySet
                                    .iterator();
                            while (newIter.hasNext()) {
                                Entry<String, List<Double>> newEntry = newIter
                                        .next();
                                String newColumnName = newEntry.getKey();
                                if (columnName.equals(newColumnName)) {
                                    eachPoint.put(newColumnName, null);
                                } else {
                                    List<Double> newColumnValues = newEntry
                                            .getValue();
                                    VisualPointGroup pointList = new VisualPointGroup(
                                            VisualNLS.getMessage(VisualNLS.Cluster_Point,locale) +" " +keyLists.get(i-1));
                                    for (int j = 0; j < columnValues.size(); j++) {
                                        VisualPoint point=new VisualPoint(
                                                String.valueOf(newColumnValues.get(j)), //x
                                                String.valueOf(columnValues.get(j))); //y

                                        pointList.addVisualPoint(point);
                                    }


                                    VisualizationModel vModel =eachPoint.get(newColumnName);
                                    if(vModel==null){

                                        vModel = createNewScatterModel(	columnName, newColumnName);
                                    }
                                    //....pointList

                                    ((VisualizationModelScatter)vModel).addVisualPointGroup(pointList);

                                    eachPoint.put(newColumnName, vModel);
                                }

                                // here need add center point...

                                VisualizationModelScatter vModel =(VisualizationModelScatter)eachPoint.get(newColumnName);

                                //for the center point ...

                                //the first line is the table column...
                                //ArrayList<String> colNames = list.get(0);
                                ArrayList<String> colNames = list.get(0);
                                if (i> 0&&vModel!=null) {
                                    VisualPointGroup group = new VisualPointGroup(
                                            VisualNLS.getMessage(VisualNLS.Cetner_Point,locale)
                                                    +" " +keyLists.get(i-1));
                                    group.setColor("red");
                                    ArrayList<String> row = list.get(i);
                                    String x=null;
                                    String y=null;
                                    for (int k=0;k<row.size();k++) {
                                        String value = (String) row.get(k);
                                        if(newColumnName.equals(colNames.get(k))){
                                            x=value;
                                        }else if(columnName.equals(colNames.get(k))){
                                            y=value;
                                        }

                                    }
                                    if(x!=null&&y!=null){

                                        group.addVisualPoint(new VisualPoint(x, y) ) ;
                                    }
                                    vModel.addVisualPointGroup(group );
                                }
                            }
                            eachClusterModel.put(columnName, eachPoint);
                        }


                }
            }
            //For precision
            buildeachClusterModel4Precision(eachClusterModel);
            //
        }
		return eachClusterModel;
	}

    private boolean isNumberColumnType(String columnType) {
      String[]  numberTypes = new String[]{"Float","Numeric","Int","int","long","float","double","bytearray"};
        if(null!=columnType){
            if(Arrays.binarySearch(numberTypes,columnType)!=-1){
                return true;
            }else {
                columnType=columnType.toUpperCase();

                if(columnType.indexOf("DECIMAL(")!=-1 && columnType.lastIndexOf(")")!=-1){
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private ArrayList<ArrayList<String>> getList(Map<String, Map<String, Double>> listM,ArrayList<String> keyList) {
        ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
        if(null!=listM && listM.size()>0){
               // Set<Entry<String,Map<String,Double>>> listMEntrySet = listM.entrySet();
               Set<String> listMKeySet =  listM.keySet();
                Iterator<String> listMKeySetIterator = listMKeySet.iterator();
                while (listMKeySetIterator.hasNext()){
                    String key = listMKeySetIterator.next();
                    keyList.add(key);
                    Map<String, Double> map = listM.get(key);
                    Set<Entry<String,Double>> entrySet = map.entrySet();
                    int j=0;
                    if(entrySet!=null && entrySet.size()>0){
                        Iterator<Entry<String,Double>> iterator = entrySet.iterator();

                        if(null!=iterator){
                            ArrayList<String> listColumn = new ArrayList<String>();
                            ArrayList<String> listValue = new ArrayList<String>();
                            while (iterator.hasNext()){
                                Entry<String,Double> entry = iterator.next();
                                if(j==0){
                                    listColumn.add(entry.getKey());
                                    listValue.add(String.valueOf(entry.getValue()));
                                }else{
                                    listValue.add(String.valueOf(entry.getValue()));
                                }
                            }

                            if(list.size()==0){
                                list.add(listColumn);
                                list.add(listValue);
                            }else{
                                list.add(listValue);
                            }
                        }

                    }
                    j++;
                }
        }
        return list;
    }

    // for precision
	private void buildeachClusterModel4Precision(
			HashMap<String, HashMap<String, VisualizationModel>> eachClusterModel) {
		   if(null!=eachClusterModel){
			  Set<Entry<String, HashMap<String, VisualizationModel>>> set4ClusterModelSet = eachClusterModel.entrySet();
			  if(null!=set4ClusterModelSet && set4ClusterModelSet.size()>0){
				  for (Iterator iterator = set4ClusterModelSet.iterator(); iterator
						.hasNext();) {
					Entry<String, HashMap<String, VisualizationModel>> entry = (Entry<String, HashMap<String, VisualizationModel>>) iterator
							.next();
					buildVisualizationModel(entry.getValue());
					
				}
				  
			  }
		   }
		
	}
    //For precision
	private void buildVisualizationModel(
			HashMap<String, VisualizationModel> visualModelMap) {
		if(null!=visualModelMap){
			Set<Entry<String,VisualizationModel>> set4VisualModelSet = visualModelMap.entrySet();
			if(null!=set4VisualModelSet){
				for (Iterator iterator = set4VisualModelSet.iterator(); iterator
						.hasNext();) {
					Entry<String, VisualizationModel> entry = (Entry<String, VisualizationModel>) iterator
							.next();
					if(null!=entry){
						buildPrecisionVisualizationModelScatter((VisualizationModelScatter)entry.getValue());
					}
				}
			}
		}
	}
	
	

	private void buildPrecisionVisualizationModelScatter(
			VisualizationModelScatter scatterVisualModel) {
		// TODO Auto-generated method stub
		if(null!=scatterVisualModel){
			List<VisualPointGroup> pointGroups = scatterVisualModel.getPointGroups();
			float maxX= 0.0f;
			float minX = 0.0f;
			float maxY = 0.0f;
			float minY = 0.0f;
			try {
				maxX = Float.valueOf(pointGroups.get(0).getPoints().get(0).getX());
				minX = Float.valueOf(pointGroups.get(0).getPoints().get(0).getX());
				maxY = Float.valueOf(pointGroups.get(0).getPoints().get(0).getY());
				minY = Float.valueOf(pointGroups.get(0).getPoints().get(0).getY());				
			} catch (NumberFormatException e) {
				// TODO: handle exception
			}
			
		    for (Iterator iterator = pointGroups.iterator(); iterator.hasNext();) {
				VisualPointGroup visualPointGroup = (VisualPointGroup) iterator
						.next();
				
				List<VisualPoint> point = visualPointGroup.getPoints();
				
				for (Iterator iterator2 = point.iterator(); iterator2.hasNext();) {
					VisualPoint visualPoint = (VisualPoint) iterator2.next();
					float tempFloatX = Float.valueOf(visualPoint.getX());
					float tempFloatY = Float.valueOf(visualPoint.getY());
					
					if(maxX<tempFloatX){
						maxX = tempFloatX;
					}
					if(minX>tempFloatX){
						minX = tempFloatX;
					}
					if(maxY<tempFloatY){
						maxY = tempFloatY;
					}
					if(minY>tempFloatY){
						minY = tempFloatY;
					}
				}
			}
		    
	    	long n = 1l;
	    		n = AlpineMath.adjustUnits(Double.valueOf(minX), Double.valueOf(maxX));
	        
	    	long m = 1l;
	    		m=AlpineMath.adjustUnits(Double.valueOf(minY), Double.valueOf(maxY));
	    	String xTitle = "";
	    	String yTitle = "";
	    	if(n!=1){
	    		xTitle = " ("+VisualUtils.getScientificNumber(n)+")";
	    	}
	    	if(m!=1){
	    		yTitle = " ("+VisualUtils.getScientificNumber(m)+")";
	    	}
	    	
	    	if("".equals(xTitle)==false){
	    		scatterVisualModel.setxAxisTitle(scatterVisualModel.getxAxisTitle()+ xTitle);
	    		
	    	}
	    	if("".equals(yTitle)==false){
	    		scatterVisualModel.setyAxisTitle(scatterVisualModel.getyAxisTitle()+ yTitle);	    		
	    	}
	    	
	    	for (Iterator iterator = pointGroups.iterator(); iterator.hasNext();) {
				VisualPointGroup visualPointGroup = (VisualPointGroup) iterator
						.next();
				List<VisualPoint> point = visualPointGroup.getPoints();
				
				for (Iterator iterator2 = point.iterator(); iterator2.hasNext();) {
					VisualPoint visualPoint = (VisualPoint) iterator2.next();
					
					float tempFloatX = Float.valueOf(visualPoint.getX());
					float tempFloatY = Float.valueOf(visualPoint.getY());
					
					visualPoint.setX(String.valueOf(tempFloatX/n));
					visualPoint.setY(String.valueOf(tempFloatY/m));
				}
				
			}
		    
		}
	}

	private VisualizationModel createNewScatterModel(String columnName,
			String newColumnName) {
 
		VisualizationModelScatter	vModel=new VisualizationModelScatter( ) ;
		vModel.setSourceOperatorClass(VisualizationModelScatter.Source_Operator_KMeans);
		
		vModel.setWidth(900) ;
		vModel.setHeight(450) ;
		vModel.sethGrid(true);
		vModel.setvGrid(true);
		vModel.setxAxisTitle(newColumnName);
		vModel.setyAxisTitle(columnName);
		vModel.setTitle("X = \""+newColumnName +"\", Y = \"" +columnName+"\"") ;
		return vModel;
	}

	private void setDefautlQueryKey(String[] defaultKey,
			ClusterScatterEntity entity) {
		Set<String> set = entity.getCenterHt().keySet();
		List<String> keyList = new ArrayList<String>();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			keyList.add(iter.next());
		}
		String[] keys = new String[keyList.size()];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = keyList.get(i);
		}
		Arrays.sort(keys);
		defaultKey[0] = keys[0];
		defaultKey[1] = keys[1];
	}

	private void filterDoubleColumn(List<String> doubleColumnList,
			ClusterScatterEntity entity) {
		Set<String> set = entity.getCenterHt().keySet();
		Set<String> needRemovedKeySet=new HashSet<String>();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			boolean isDouble = false;
			for (String doubleColumn : doubleColumnList) {
				if (key.equalsIgnoreCase(doubleColumn)) {
					isDouble = true;
				}
			}
			if (!isDouble) {
				needRemovedKeySet.add(key);
			}
		}
		if(needRemovedKeySet.size()>0){
			set.removeAll(needRemovedKeySet);
		}
	}

	private void setQueryColumn(ClusterAllEntity allEntity,
			ClusterScatterEntity entity) {
		if (allEntity.getQueryColumn() == null
				|| allEntity.getQueryColumn().length == 0) {
			String[] columns = new String[entity.getCenterHt().size()];
			Set<String> set = entity.getCenterHt().keySet();
			int i = 0;
			Iterator<String> iter = set.iterator();
			while (iter.hasNext()) {
				columns[i] = iter.next();
				i++;
			}
			Arrays.sort(columns);
			allEntity.setQueryColumn(columns);
		}
	}

	private VisualizationModel createScatterPoint(AnalyticOutPut outPut, ClusterOutputModel clusterModel,Locale locale) throws Exception {

		   HashMap<String, HashMap<String, VisualizationModel> > outputModelMap= generateOutPutMap(outPut,locale);
		  
		 if(outputModelMap==null){
			 return null;
		 }
		// we use 2 layerd ..x, and y ...
		List<String> yKeys = getKeysList(outputModelMap.keySet(),null);

		HashMap<String, VisualizationModel> yModelMap = getYModelMaps(yKeys,outputModelMap,locale);

		VisualizationModelLayered layeredModel = new VisualizationModelLayered(
				VisualNLS.getMessage(VisualNLS.CLUSTER,locale), 
			 
				"Y "+VisualNLS.getMessage( VisualNLS.Axis,locale), yKeys, yModelMap);
		layeredModel.setVisualizationType(VisualizationModel.TYPE_CLUSRTER_CHART) ;
		
		return layeredModel;
	}

	private HashMap<String, VisualizationModel> getYModelMaps(
			List<String> yKeys,
			HashMap<String, HashMap<String, VisualizationModel>> outputModelMap, Locale locale) {
		HashMap<String, VisualizationModel>  maps= new HashMap<String, VisualizationModel> ();
		 
		//for test use
		
	 	for (Iterator iterator = yKeys.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			HashMap<String, VisualizationModel> yMaps = outputModelMap.get(key); 
			VisualizationModelLayered layeredModel= new VisualizationModelLayered("",
					"X "+VisualNLS.getMessage( VisualNLS.Axis,locale), getKeysList(yMaps.keySet(),key), yMaps) ;
		
			layeredModel.setVisualizationType(VisualizationModel.TYPE_CLUSRTER_CHART) ;
			//this is test use to make the json size small
			maps.put(key, layeredModel);
		} 
	
		return maps;
	}

	private List<String> getKeysList(Set<String> keys,String notContain) {
		 List<String> result= new ArrayList<String>(); 
		 
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			if(notContain==null||key.equals(notContain)==false){
				result.add(key) ;
			}
		}
		return result;
	}

	private VisualizationModel createCenterPointTable(ClusterOutputModel clusterModel,Locale locale) {
		if (clusterModel == null)
			return null;
		DataTable dataTable = new DataTable();
        List<TableColumnMetaInfo> metaInfos = new ArrayList<TableColumnMetaInfo>();
        List<DataRow> rows = new ArrayList<DataRow>();
        if(clusterModel!=null&&clusterModel.getColumnNames()!=null){
            List<String> columnNames = clusterModel.getColumnNames();
            String[] titles=new String[columnNames.size()+1];
            titles[0]="Cluster";
            //tableEntity.addSortColumn(titles[0],DataTypeConverterUtil.numberType);
            metaInfos.add(new TableColumnMetaInfo(titles[0],"number"));
            for(int i=0;i<columnNames.size();i++){
                titles[i+1]=columnNames.get(i);
                //tableEntity.addSortColumn(columnNames.get(i),DataTypeConverterUtil.numberType);
                metaInfos.add(new TableColumnMetaInfo(titles[i+1],"number"));
            }
            //tableEntity.setColumn(titles);
            dataTable.setColumns(metaInfos);
        }
        if(clusterModel!=null&&clusterModel.getCentroidsContents()!=null&&clusterModel.getColumnNames()!=null){
            Map<String,Map<String, Double>> centroidsContents = clusterModel.getCentroidsContents();
            List<String> columnNames = clusterModel.getColumnNames();
            //int index=0;

            Set<String> centroidsContentsKeySet = centroidsContents.keySet();
            Iterator<String> keySetItor = centroidsContentsKeySet.iterator();
            while (keySetItor.hasNext()){
                String clusterValue = keySetItor.next();
                Map<String, Double> centroids = centroidsContents.get(clusterValue);
                String[] tableItem=new String[columnNames.size()+1];
                tableItem[0]=String.valueOf(clusterValue);
                for(int i=0;i<columnNames.size();i++){
                    tableItem[i+1]=String.valueOf(centroids.get(columnNames.get(i)));
                }
                //tableEntity.addItem(tableItem);
                //dataTable.setRows(Arrays.asList());
                rows.add(new DataRow(tableItem));
               // index++;
            }

            dataTable.setRows(rows);
        }
		VisualizationModel model = new VisualizationModelDataTable(
				VisualNLS.getMessage(VisualNLS.CENTER_POINT,locale), dataTable);
		return model;

	}

	private VisualizationModel createWarningText(ClusterOutputModel clusterModel,Locale locale) {
		if (!clusterModel.isStable()) {
			VisualizationModel model = new VisualizationModelText(
					VisualNLS.getMessage(VisualNLS.WARING_MESSAGE_TITLE,locale),
                    VisualNLS.getMessage(VisualNLS.Hadoop_KMEANS_NOTSTABLE, locale));
			return model;
		}
		return null;
	}

	private VisualizationModel createSummaryTable(
			AnalyticOutPut analyzerOutPut, ClusterOutputModel clusterModel,Locale locale) {
		DataTable dataTable = new DataTable();
        List<DataRow> dataRows = new ArrayList<DataRow>();
        List<TableColumnMetaInfo> columnMetaInfos = new ArrayList<TableColumnMetaInfo>();
        if(clusterModel!=null&&clusterModel.getDataSampleContents()!=null){
            List<String[]> dataSampleContents = clusterModel.getDataSampleContents();
            int index =0;
            for(String[] tableItem:dataSampleContents){
                if(index==0){
                    //tableEntity.setColumn(tableItem);
                    buildColumnMetInfos(tableItem,columnMetaInfos);
                }else{
                    //tableEntity.addItem(tableItem);
                        DataRow row = new DataRow(tableItem);
                        dataRows.add(row);

                }
                index++;
            }
            dataTable.setColumns(columnMetaInfos);
            dataTable.setRows(dataRows);
        }
		VisualizationModelDataTable visualModel = new VisualizationModelDataTable(
				VisualNLS.getMessage(VisualNLS.KMeans,locale), dataTable);

		return visualModel;
	}

    private void buildColumnMetInfos(String[] tableItem, List<TableColumnMetaInfo> columnMetaInfos) {
        if(null!=tableItem && null!=columnMetaInfos){
            for(int i=0;i<tableItem.length;i++){
                TableColumnMetaInfo metaInfo = new TableColumnMetaInfo(tableItem[i],"string");
                columnMetaInfos.add(metaInfo);
            }
        }
    }

    private String getMessage(ClusterOutputModel clusterModel,Locale locale) {

		StringBuffer sb = new StringBuffer();
		if (clusterModel != null) {
            ClusterOutputBasicInfo basicInfo = clusterModel.getOutputText();
            if(null!=basicInfo){
                sb.append(VisualNLS.getMessage(VisualNLS.CLUSTER_COLUMN_NAME,locale) + " : "
                        + basicInfo.getClusterColumName());
                sb.append("\n");
                sb.append(VisualNLS.getMessage(VisualNLS.CLUSTER_COUNT,locale) + " : "
                        + basicInfo.getClusterCount());
                sb.append("\n");
                sb.append(VisualNLS.getMessage(VisualNLS.AVG_MEASUREMENT,locale) + " : "
                        + basicInfo.getAvgDistanceMeasurement());
            }
		}
		return sb.toString();
	}

	@Override
	protected void fillDataTables(DataTable dataTable, ResultSet rs,
			int fetchSize, ResultSetMetaData rsmd, AnalyticOutPut outPut)
			throws SQLException {
		int count = rsmd.getColumnCount();
		List<DataRow> rows = new ArrayList<DataRow>();
		while (rs.next() && fetchSize > 0) {
			fetchSize--;
			String[] items = new String[count];
			for (int i = 0; i < count; i++) {
				if (DataTypeConverter.isDoubleType(rsmd.getColumnType(i + 1))) {
					items[i] = AlpineUtil.dealNullValue(rs, i + 1);
				} else {
					items[i] = rs.getString(i + 1);
				}
			}

			DataRow row = new DataRow();
			row.setData(items);
			rows.add(row);

		}
		 
		dataTable.setRows(rows);
	}

	
	private List<String> createProfileTableHeader(String[] headerLabels,Locale locale) {
		List<String> tableHeader = new ArrayList<String>();
        if(null==headerLabels || headerLabels.length==0){
            return tableHeader;
        }
		//tableHeader.add( VisualNLS.getMessage(VisualNLS.VARIABLES,locale));
		//tableHeader.add( VisualNLS.getMessage(VisualNLS.STATES,locale));
        tableHeader.add(headerLabels[0]);
        tableHeader.add(headerLabels[1]);
		for(int i=2;i<headerLabels.length;i++){
				if(i==2){
					tableHeader.add(headerLabels[i]);
				}
				if(i>2){
					//tableHeader.add(VisualNLS.getMessage(VisualNLS.CLUSTER,locale)+list.get(0)[i].split(";")[0]);
					tableHeader.add(headerLabels[i]);
				}
			}
		return tableHeader;
	}
}

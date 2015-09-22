/**
 * ClassName HadoopNullValueReplaceAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-12-12
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopReplaceNullConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementItem;
import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.hadoop.HadoopDataType;
/**
 * @author john zhao, robbie gill
 *
 */
public class HadoopNullValueReplaceAnalyzer extends AbstractHadoopAttributeAnalyzer {
    private static final Logger itsLogger=Logger.getLogger(HadoopNullValueReplaceAnalyzer.class);

    @Override
	public String generateScript(HadoopDataOperationConfig config,String pureFileName) {
		  AnalysisNullReplacementModel npModel = ((HadoopReplaceNullConfig )config).getNullReplacementModel();
		  
		List<AnalysisNullReplacementItem> repItems = npModel.getNullReplacements();
        String groupBy = npModel.getGroupBy();
        AnalysisFileStructureModel fileStructure = ((HadoopAnalyticSource)getAnalyticSource()).getHadoopFileStructureModel();
        List<String> nameList = fileStructure.getColumnNameList();
        List<String> typeList = fileStructure.getColumnTypeList();
        String groupByType = "";
        if (groupBy != null && !groupBy.equals("")) {
            Integer ind = nameList.indexOf(groupBy);
            if (ind != -1) {groupByType = typeList.get(ind);}
        }

        /* ----------------------------setup--------------------------------------- */
        long randId = randLong();
        String inputfile = "inputfile" + randId;
        String grpd = "grpd" + randId;
        String nullrep = "nullrep" + randId;
        String bigtab = "bigtab" + randId;
        String groupByAlias = "ALP_"+groupBy+"_ALP";
        /* ------------------------end setup--------------------------------------- */


        StringBuilder sb = new StringBuilder();
        Boolean hasAgg = false;
        for (AnalysisNullReplacementItem rep : repItems) {
            if(rep.getType() != null && !rep.getType().equals("value") ){
                hasAgg = true;
                break;
            }
        }

        if (hasAgg) {
            Boolean hasGroup;
            if (groupBy != null && !groupBy.equals("")) {
                // with agg and groupBy
                // inputfile = foreach sourcefile generate (colg is null ? 1239314512 : colg) as colgALP, colg as colg, col1 as col1, col2 as col2;
                // grpd      = GROUP inputfile BY colgALP;
                // nullrep   = foreach grpd generate group as colgALP, AVG(inputfile.col1) as AVG_col1, AVG(inputfile.col2) as AVG_col2;
                // bigtab    = join inputfile by colgALP left outer, nullrep by colgALP USING 'replicated';
                // final     = foreach bigtab generate inputfile::colg as colg, (inputfile::col1 is null ? nullrep::AVG_col1 : inputfile::col1) as col1, inputfile::col2 as col2;
                hasGroup = true;
                sb.append(getInputFileWithGroup(inputfile, pureFileName, groupBy, groupByAlias, groupByType, nameList));
                sb.append(getGrpdWithGroup(grpd, inputfile, groupByAlias));
                sb.append(getNullRep(nullrep, inputfile, grpd, groupByAlias, repItems, hasGroup, randId));
                sb.append(getBigtabWithGroup(bigtab, inputfile, nullrep, groupByAlias));
                sb.append(getOutputWithGroup(inputfile, nullrep, bigtab, groupBy, nameList, repItems,hasGroup, randId));
            } else {
                // with agg and no groupBy
                //grpd      = GROUP sourcefile all;
                //nullrep   = foreach grpd generate AVG(inputfile.col1) as AVG_col1, AVG(inputfile.col2) as AVG_col2;
                //bigtab    = cross inputfile, nullrep;
                //final     = foreach bigtab generate inputfile::colg as colg, (inputfile::col1 is null ? nullrep::AVG_col1 : inputfile::col1) as col1, inputfile::col2 as col2;
                hasGroup = false;
                sb.append(getGrpd(grpd, pureFileName));
                sb.append(getNullRep(nullrep, pureFileName, grpd, groupBy, repItems, hasGroup, randId));
                sb.append(getJoinCross(pureFileName, nullrep, bigtab));
                sb.append(getOutputWithGroup(pureFileName, nullrep, bigtab, groupBy, nameList, repItems, hasGroup, randId));
            }

        } else {
            //with no agg type cols
            sb.append(getOutputTempName() ).append(" = FOREACH ").append( pureFileName).append( " GENERATE ");
            for (String colName : nameList) {
                AnalysisNullReplacementItem item = getReplace4Column(repItems,colName);
                if(item==null){
                    sb.append(" ").append(colName).append("  AS ").append(colName).append(" ");

                }else{
                    String columnName = item.getColumnName();
                    String replaceValue = item.getValue();
                    sb.append("(").append(columnName).append(" is null ? ").append(replaceValue) .append(":").append(columnName).append(")  AS ").append(columnName).append(" ");

                }
                sb.append(",") ;
            }
            sb=sb.deleteCharAt(sb.length()-1);
            sb.append(";");
        }

		itsLogger.debug(sb.toString());
		return sb.toString();
    }

	@Override
	protected AnalysisFileStructureModel getOutPutStructure() {
		AnalysisFileStructureModel oldModel = hadoopSource.getHadoopFileStructureModel();
		AnalysisFileStructureModel newModel = generateNewFileStructureModel(oldModel);

		HadoopReplaceNullConfig newConfig = (HadoopReplaceNullConfig)config;
		AnalysisNullReplacementModel nullReplacementModel = newConfig.getNullReplacementModel();
		List<String> newColumnNameList = new ArrayList<String>();
		List<String> newColumnTypeList = new ArrayList<String>();
		if(nullReplacementModel != null && nullReplacementModel.getNullReplacements()!= null){
			List<AnalysisNullReplacementItem> nullReplacementFields = nullReplacementModel.getNullReplacements();
			List<String> columnNameList = oldModel.getColumnNameList();
			List<String> columnTypeList = oldModel.getColumnTypeList();
			for(int i = 0;i < columnNameList.size();i++){
				newColumnNameList.add(columnNameList.get(i));
				newColumnTypeList.add(columnTypeList.get(i));
				for(AnalysisNullReplacementItem nullReplacementField : nullReplacementFields){
					if(nullReplacementField.getColumnName().equals(columnNameList.get(i))){
						if("AVG".equals(nullReplacementField.getValue())){
							newColumnTypeList.add(HadoopDataType.DOUBLE);
						}
						break;
					}
				}
			}
		}
		
		newModel.setColumnNameList(newColumnNameList);
		newModel.setColumnTypeList(newColumnTypeList);

		return newModel; 
	}
	private AnalysisNullReplacementItem getReplace4Column(
			List<AnalysisNullReplacementItem> repItems, String colName) {
		for (AnalysisNullReplacementItem analysisNullReplacementItem : repItems) {
			String columnName = analysisNullReplacementItem.getColumnName();
			if (columnName.equals(colName))
				return analysisNullReplacementItem;
		}
		return null;
	}

	@Override
	public AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.REPLACE_NULL_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.REPLACE_NULL_DESCRIPTION,locale));
		return nodeMetaInfo;
	}

    private String getInputFileWithGroup(String inputfile,
                                         String pureFileName,
                                         String groupBy,
                                         String groupByAlias,
                                         String groupByType,
                                         List<String> nameList
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(inputfile).append(" = FOREACH ").append(pureFileName).append(" GENERATE (").append(groupBy);
        sb.append(" is null ? ");
        if (groupByType.equals("chararray")) {
            sb.append("'ALP_zaq9910qaz_ALP'");
        } else {
            sb.append("1112341251");
        }
        sb.append(" : ").append(groupBy).append(") AS ").append(groupByAlias).append(",");

        for (String colName : nameList) {
            sb.append(" ").append(colName).append(" AS ").append(colName).append(",");
        }
        sb=sb.deleteCharAt(sb.length()-1);
        sb.append(";");
        sb.append("\n");
        return sb.toString();
    }

    private String getGrpdWithGroup(String grpd,
                                    String inputfile,
                                    String groupByAlias
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(grpd).append(" = GROUP ").append(inputfile).append(" BY ").append(groupByAlias).append(";");
        sb.append("\n");
        return sb.toString();
    }

    private String getNullRep(String nullrep,
                              String inputfile,
                              String grpd,
                              String groupByAlias,
                              List<AnalysisNullReplacementItem> repItems,
                              Boolean hasGroup,
                              long randId
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(nullrep).append(" = FOREACH ").append(grpd).append(" GENERATE ");
        if (hasGroup) {
            sb.append(" group AS ").append(groupByAlias).append(", ");
        }
        for (AnalysisNullReplacementItem rep : repItems) {
            String columnName = rep.getColumnName();
            String replaceValue = rep.getValue();
            String replaceType = rep.getType();
            if ((!replaceType.equals("value"))) {
                sb.append(replaceValue).append("(").append(inputfile).append(".").append(columnName).append(") AS ").
                        append(replaceValue).append("_").append(columnName).append(randId).append(",");
            }
        }
        sb=sb.deleteCharAt(sb.length()-1);
        sb.append(";");
        sb.append("\n");
        return sb.toString();
    }

    private String getBigtabWithGroup(String bigtab,
                                      String inputfile,
                                      String nullrep,
                                      String groupByAlias
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(bigtab).append(" = JOIN ").append(inputfile).append(" BY ").append(groupByAlias).append(" LEFT OUTER, ");
        if (getContext().isLocalModelPig() == true ) {
            /*
            * local mode pig is failing with USING 'replicated'.
            * It should provide a performance improvement for joins so we want to keep it if not in local mode
            * */
            sb.append(nullrep).append(" BY ").append(groupByAlias).append(";");
        } else {
            sb.append(nullrep).append(" BY ").append(groupByAlias).append(" USING 'replicated';");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String getOutputWithGroup(String inputfile,
                                      String nullrep,
                                      String bigtab,
                                      String groupBy,
                                      List<String> nameList,
                                      List<AnalysisNullReplacementItem> repItems,
                                      Boolean hasGroup,
                                      long randId
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(getOutputTempName()).append(" = FOREACH ").append(bigtab).append(" GENERATE ");
        /* we should generate the output in the same order as the input */
        for (String colName : nameList) {
            if (!colName.equals(groupBy)) {
                AnalysisNullReplacementItem item = getReplace4Column(repItems,colName);
                if(item==null){
                    sb.append(" ").append(colName).append(" AS ").append(colName).append(" ");
                }else {
                    String columnName = item.getColumnName();
                    String replaceValue = item.getValue();
                    String replaceType = item.getType();
                    if (!replaceType.equals("value")) {
                        sb.append("(").append(inputfile).append("::").append(columnName).append(" is null ? ")
                                .append(nullrep).append("::").append(replaceValue).append("_").append(columnName).append(randId)
                                .append(" : ").append(inputfile).append("::").append(columnName).append(") AS ").append(columnName);
                    } else {
                        sb.append("(").append(inputfile).append("::").append(columnName).append(" is null ? ")
                                .append(replaceValue).append(" : ").append(inputfile).append("::").append(columnName).append(") AS ").append(columnName);
                    }
                }
                sb.append(",") ;
            } else {
                sb.append(" ").append(colName).append(" AS ").append(colName).append(",");
            }
        }
        sb=sb.deleteCharAt(sb.length()-1);
        sb.append(";");
        return sb.toString();
    }

    //For group all
    private String getGrpd(String grpd, String pureFileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(grpd).append(" = GROUP ").append(pureFileName).append(" ALL;");
        sb.append("\n");
        return sb.toString();
    }

    private String getJoinCross(String pureFileName, String nullrep, String bigtab) {
        StringBuilder sb = new StringBuilder();
        sb.append(bigtab).append(" = CROSS ").append(pureFileName).append(", ").append(nullrep).append(";");
        sb.append("\n");
        return sb.toString();
    }

    private long randLong() {
        long range = 123456789L;
        Random r = new Random();
        return (long)(r.nextDouble()*range);
    }

}

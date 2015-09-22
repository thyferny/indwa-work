package com.alpine.miner.impls.result;


import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.db.attribute.variableselection.VariableSelectionResult;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.utility.db.TableColumnMetaInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VisualAdapterHadoopVariableSelection extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {


    public static final VisualAdapterHadoopVariableSelection INSTANCE = new VisualAdapterHadoopVariableSelection();

    @Override
    public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale)
            throws RuntimeException {
        if(!(outPut instanceof AnalyzerOutPutObject))return null;
        Object objR=((AnalyzerOutPutObject)outPut).getOutPutObject();
        if(!(objR instanceof VariableSelectionResult))return null;
        VariableSelectionResult result=(VariableSelectionResult)objR;

        VisualizationModelDataTable visualModel = generateTableModel(outPut.getDataAnalyzer().getName(),result,  locale);
        return visualModel;
    }



    /**
     * @param name - name of the result tab
     * @param result - results to display
     * @param locale - user's locale, for localization purposes
     * @return table visualization model
     */
    private VisualizationModelDataTable generateTableModel(String name,VariableSelectionResult result,Locale locale) {

        String[] attributeNames=result.getColumnNames();
        double[] scores=result.getScores();

        List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
        columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.VS_COLUMN_NAME,locale), DBUtil.TYPE_CATE)) ;
        columns.add(new TableColumnMetaInfo("R2", DBUtil.TYPE_NUMBER)) ;

        List<DataRow> rows = new ArrayList<DataRow>();
        for(int i=0;i<attributeNames.length;i++){

            DataRow row= new DataRow();
            row.setData(new String[]{attributeNames[i],
                    AlpineMath.doubleExpression(scores[i])});
            rows.add(row) ;


        }
        DataTable dataTable = new DataTable();
        dataTable.setColumns(columns) ;
        dataTable.setRows(rows) ;

        VisualizationModelDataTable tableModel=new VisualizationModelDataTable(name,
                dataTable) ;


        return tableModel;
    }


}

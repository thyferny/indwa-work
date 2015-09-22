package com.alpine.miner.impls.result;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.db.evaluator.ConfusionOutput;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.operator.evaluator.ConfusionMatrix;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.tools.AlpineMath;

import java.util.*;

public class VisualAdapterConfusionMatrix extends AbstractOutPutVisualAdapter  implements OutPutVisualAdapter {


    public static final OutPutVisualAdapter INSTANCE = new VisualAdapterConfusionMatrix();
    @Override
    public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale) {

        if (analyzerOutPut instanceof ConfusionOutput) {
            List<VisualizationModel> models = new ArrayList<VisualizationModel>();
            List<ConfusionMatrix> resultList = ((ConfusionOutput) analyzerOutPut)
                    .getResultList();
            System.out.println("yo");
            for (ConfusionMatrix matrix : resultList) {
                DataTable table = toTableOutput(matrix);
                VisualizationModelDataTable dataTable =   new VisualizationModelDataTable(matrix.getName(), table);
                models.add(dataTable);
            }

            VisualizationModelComposite visualModel = new VisualizationModelComposite(
                    analyzerOutPut.getAnalyticNode().getName(), models);

        return visualModel;
    }
        return null;
    }

    public DataTable toTableOutput(ConfusionMatrix matrix)
    {
        ArrayList<String> columnNames =  matrix.getClassIndexes_();
        DataTable te = new DataTable();
        int totalSize = columnNames.size() + 2;

        List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>(totalSize);
        List<DataRow> rows = new ArrayList<DataRow>();
        columns.add(new TableColumnMetaInfo(" ",DBUtil.TYPE_CATE)) ;
         String[] finalRow = new String[totalSize];
        finalRow[0] ="Class recall";
        for (int j = 0; j < columnNames.size(); j++) {
            String columnName = columnNames.get(j);

            columns.add(new TableColumnMetaInfo(columnName, DBUtil.TYPE_CATE));

            String[] items = new String[columnNames.size() + 2];

            items[0] = "Pred " + columnName;
            for (int i = 0 ; i < columnNames.size(); i++)
            {
               items[i+1]  = new Long(matrix.getConfMX_()[j][i]).toString();
            }

             finalRow[j + 1] = AlpineMath.doubleExpression(matrix.getClassRecall_().get(columnName));

            items[columnNames.size() + 1] = AlpineMath.doubleExpression(matrix.getClassPrecision_().get(columnName));

            DataRow row = new DataRow();
            row.setData(items);
            rows.add(row) ;

        }
        finalRow[columnNames.size() + 1] = "Accuracy: " + AlpineMath.doubleExpression(matrix.getAccuracy_());
        rows.add(new DataRow(finalRow));
        columns.add(new TableColumnMetaInfo("Class Precision", DBUtil.TYPE_CATE));
        te.setColumns(columns );
        te.setRows(rows);
        return te;


    }

}
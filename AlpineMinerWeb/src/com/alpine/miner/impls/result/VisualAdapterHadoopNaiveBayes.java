package com.alpine.miner.impls.result;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.hadoop.models.NaiveBayesHadoopModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.operator.bayes.NBModel;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.utility.tools.AlpineMath;

import java.util.*;

public class VisualAdapterHadoopNaiveBayes extends AbstractOutPutVisualAdapter
        implements OutPutVisualAdapter {

    public static final VisualAdapterHadoopNaiveBayes INSTANCE = new VisualAdapterHadoopNaiveBayes();

    @Override
    public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut, Locale locale) throws Exception {

        EngineModel model = null;
        if (analyzerOutPut instanceof AnalyzerOutPutTrainModel) {
            model = ((AnalyzerOutPutTrainModel) analyzerOutPut).getEngineModel();
        }
        if (model == null){
            return null;
        }

        String message = createPriorsString((NaiveBayesHadoopModel) model.getModel());

        List<VisualizationModel> models = new ArrayList<VisualizationModel>();

        String name = analyzerOutPut.getAnalyticNode().getName();
        VisualizationModelText textModel = new VisualizationModelText(VisualNLS.getMessage(VisualNLS.MESSAGE_TITLE, locale),
                message);// message);
        models.add(textModel); //TODO this will be the priors, eventually.
        List dataTables = getDataTable((NaiveBayesHadoopModel) model.getModel(),  locale);
        for (Iterator iterator = dataTables.iterator(); iterator.hasNext();) {
            DataTable dataTable = (DataTable) iterator.next();
            VisualizationModelDataTable tableModel = new VisualizationModelDataTable(
                    dataTable.getTableName(), dataTable);
            models.add(tableModel);

        }

        VisualizationModelComposite visualModel = new VisualizationModelComposite(
                name, models);

        return visualModel;

    }

    // numeric type have only one table
    private List<DataTable> getDataTable(NaiveBayesHadoopModel model,Locale locale) {
        List<DataTable> tables = new ArrayList<DataTable>();
        DataTable numericAttrTable = new DataTable();
        numericAttrTable.setTableName(VisualNLS.getMessage(VisualNLS.DATA_TITLE,locale));
        int numberOfRows = model.getClassName_().size();
        List<String> classValues = model.getClassValue_();
       // double[][][] distributionProperties = model.getDistributionProperties();
        List<String> attributeNames = model.getFeatureName_();
        List<String>nominal = model.getRealValue_();
        setColumns(numericAttrTable, new String[] { ATTRIBUTE, CLASS,
                MEAN, STANDARD_DEVIANTION },new String[]{ DBUtil.TYPE_CATE,DBUtil.TYPE_CATE,DBUtil.TYPE_NUMBER,DBUtil.TYPE_NUMBER });


        List<DataRow> numericRos = new ArrayList<DataRow>();
        HashMap<String,ArrayList<DataRow>> catRows = new HashMap<String, ArrayList<DataRow>>();

        for (int i = 0; i < numberOfRows; i++) {
            if ("1".equals(nominal.get(i))) {
                double var = new Double(model.getVariance_().get(i));
                double stDev = Math.sqrt(var);

                String[] items = new String[4];
                    items[0] = attributeNames.get(i);
                    items[1] = classValues.get(i);
                    items[2] = AlpineMath
                            .doubleExpression(new Double(model.getMean_().get(i)));
                    items[3] = AlpineMath
                            .doubleExpression(stDev);
                    DataRow row = new DataRow();
                    row.setData(items);
                    numericRos.add(row);
            } else {// categry type attribute...
                //tables.add(createCateGoryTable(model, i));
                String attName = attributeNames.get(i);
                ArrayList<DataRow> attList = catRows.get(attName);
                if (attList == null)
                {
                    attList = new ArrayList<DataRow>();
                }
                DataRow catRow = new DataRow();
                String[] items = new String[3];
                items[0] = model.getFeatureValue_().get(i);
                items[1] = classValues.get(i);
                items[2] = model.getFeatureValueCount_().get(i);
                catRow.setData(items);
                attList.add(catRow);

                catRows.put(attName, attList);

            }
        }
        numericAttrTable.setRows(numericRos);
        tables.add(numericAttrTable);

        for (Map.Entry<String, ArrayList<DataRow>> entry : catRows.entrySet()) {
            String key = entry.getKey();
            ArrayList<DataRow> value = entry.getValue();
            DataTable table = new DataTable();
            table.setRows(value);
            table.setTableName(key);
            setColumns(table, new String[]{ATTRIBUTE, CLASS, "feature count"}, new String[]{DBUtil.TYPE_CATE, DBUtil.TYPE_CATE, DBUtil.TYPE_NUMBER});
             tables.add(table);
        }



        return tables;
    }

    private String createPriorsString(NaiveBayesHadoopModel model) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(CLASS_PRIORS).append(Tools.getLineSeparator());

        HashMap<String, Long> distinctClassValues = model.getDistinctClassValues_();
        for (Map.Entry<String, Long> entry : distinctClassValues.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue();
            float prior = ((float)value )/model.getNumSamples_();
            buffer.append(PRIORS +
                    "(" + key + "):")
                    .append(AlpineMath.doubleExpression(prior))
                    .append(Tools.getLineSeparator());


        }


        return buffer.toString();


    }

}
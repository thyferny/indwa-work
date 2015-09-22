package com.alpine.miner.impls.result;

import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.workflow.output.visual.VisualLine;
import com.alpine.miner.workflow.output.visual.VisualPoint;
import com.alpine.miner.workflow.output.visual.VisualizationModelLine;
import com.alpine.util.VisualUtils;
import org.apache.log4j.Logger;

import java.util.*;

public abstract class VisualAdapterBaseARIMARPredictor extends AbstractOutPutVisualAdapter{
    private static Logger itsLogger = Logger.getLogger(VisualAdapterBaseARIMARPredictor.class);

    public static final int MAX_LABEL = 12;
    public int MAX_POINT = 30;//default value


    public VisualAdapterBaseARIMARPredictor(){
        try {
            MAX_POINT = Integer.valueOf(ResourceManager.getInstance().getPreferenceProp(
                    PreferenceInfo.GROUP_UI, PreferenceInfo.KEY_MAX_TIMESERIES_POINTS));
        } catch (Exception e) {
            itsLogger.error(e.getMessage(),e) ;
            e.printStackTrace();
        }
    }

    protected HashMap<String,String> xLabelMap=new HashMap<String,String>();


    protected void fillTimeLines(List<String[]> xLabels, List<String[]> yLabels,
                                 Object[] ids, double[] values, VisualLine line) {
        int length = ids.length;
        int step =1;
//		if(length >MAX_POINT ){
//			step = length / MAX_POINT ;
//		}
        for (int i = 0; i < ids.length ; i++) {
//			if(length>MAX_POINT&&(i%step)!= 0){
//				continue;
//			}
//			if(line.getPoints()!=null&&line.getPoints().size()>MAX_POINT){
//				break;
//			}
            java.util.Date d = (java.util.Date) ids[i];
            long timemills = d.getTime();
            String xValue=String.valueOf(timemills);
            VisualPoint point = new VisualPoint(xValue,
                    String.valueOf(values[i]));
            line.addVisualPoint(point);
            xLabelMap.put(xValue, d.toString()) ;
            xLabels.add(new String[]{String.valueOf(timemills),d.toString()});
            yLabels.add(new String[]{String.valueOf(values[i]),String.valueOf(values[i])});
        }
    }


    /**
     * @param precisionList
     * @return
     */
    protected String[] getPrecisionTitles(List<float[]> precisionList) {
        String[] precisionTitles = new String[2];
        precisionTitles[0] = "";
        precisionTitles[1] = "";
        if(null!=precisionList && precisionList.size()>0){
            float avgXPrecision = 0.0f;
            float avgYPrecision = 0.0f;
            float sumX = 0.0f;
            float sumY = 0.0f;
            for (int i = 0; i < precisionList.size(); i++) {
                sumX += precisionList.get(i)[0];
                sumY += precisionList.get(i)[1];
            }
            avgXPrecision = sumX/precisionList.size();
            avgYPrecision = sumY/precisionList.size();
            precisionTitles[0] = setPrecisionTitle(avgXPrecision);
            precisionTitles[1] = setPrecisionTitle(avgYPrecision);

        }
        return precisionTitles;
    }

    private String setPrecisionTitle(float precision)
    {
        String title = VisualUtils.getScientificNumber(precision) ;
        if (title.equals("1"))
        {
            return "";
        } else
        {
            return " ("+ title +")";
        }
    }

    /**
     * @param lineModel
     */
    public static void  handleNumericModelLabel(VisualizationModelLine lineModel,
                                                List<String[]> xLabels) {
        List<String[]> newXLabels = new ArrayList<String[]>();
        if (xLabels.size() > MAX_LABEL) {
            int length = xLabels.size();
            int step =1;
            if(length >MAX_LABEL ){
                step = length / MAX_LABEL ;
            }

            double minX = (double)Double.parseDouble(xLabels.get(0)[0]);
            double maxX = (double)Double.parseDouble(xLabels.get(0)[0]);
            List<String> xList = new ArrayList<String>();
            int i =0;
            for (Iterator iterator = xLabels.iterator(); iterator.hasNext();) {
                i=i+1;
                String[] strings = (String[]) iterator.next();
                String x = strings[0];
                double value = Double.parseDouble(x);
                if (minX > value) {
                    minX = value;
                }
                if (maxX < value) {
                    maxX = value;
                }

                if (xList.contains(x) == false) {
                    if((i%step)!= 0){
                        continue;
                    }
                    xList.add(x);
                    // avoid the duplicated xlabels
                    newXLabels.add(strings);

                }


            }

            lineModel.setMaxX(String.valueOf(maxX));
            lineModel.setMinX(String.valueOf(minX));

        } else {
            newXLabels = xLabels;
        }


        lineModel.setxLabels(newXLabels);

    }



    public static void handleDateLineModelLabel(VisualizationModelLine lineModel, HashMap<String, String> labelMap) {
        List<VisualLine> lines = lineModel.getLines();
        List<Long> xValueList = new ArrayList<Long>();
        for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
            VisualLine line = (VisualLine) iterator.next();
            if(null!=line){
                List<VisualPoint> points = line.getPoints();
                if(points!=null){
                    for (Iterator iterator2 = points.iterator(); iterator2.hasNext();) {
                        VisualPoint visualPoint = (VisualPoint) iterator2.next();
                        Long longX=Long.valueOf(visualPoint.getX());
                        if(xValueList.contains(longX)==false){
                            xValueList.add(longX) ;
                        }
                    }
                }
            }
        }
        Long[] valueArray=xValueList.toArray(new Long[xValueList.size()]);
        Arrays.sort(valueArray);
        ArrayList<Long> list = new ArrayList<Long>();
        for (int i = 0; i < valueArray.length; i++) {
            list.add(valueArray[i]) ;
        }
//		for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
//			VisualLine line = (VisualLine) iterator.next();
//			if(null!=line){
//				List<VisualPoint> points = line.getPoints();
//				if(null!=points){
//					for (Iterator iterator2 = points.iterator(); iterator2.hasNext();) {
//						VisualPoint visualPoint = (VisualPoint) iterator2.next();
//						Long longX=Long.valueOf(visualPoint.getX());
//						visualPoint.setX(String.valueOf(list.indexOf(longX))) ;
//					}
//				}
//			}
//		}
        ArrayList<String[]> xLabels = new ArrayList<String[]>();
        for (int i = 0; i < list.size(); i++) {
            String key=list.get(i).toString();
            //xLabels.add(new String[]{Integer.toString(i),labelMap.get(key), key}) ;
            xLabels.add(new String[]{Integer.toString(i),new Date(list.get(i)).toString(), key}) ;
        }

        lineModel.setxLabels(xLabels) ;
    }


    protected void fillNoneTimeLines(List<String[]> xLabels, List<String[]> yLabels,
                                     Object[] trainIds, double[] trainValues,
                                     Object[] predictIds, double[] predictValues,
                                     VisualLine trainLine,
                                     VisualLine predictLine,
                                     List<float[]> precisionList)
    {
        //first figure out amount to scale x and y by
        //long idScale =  getScalingFactorForIds(trainIds,predictIds);
        //long valueScale = getScalingFactorForValues(trainValues,predictValues);

        //For now, don't do the scaling.
        long idScale = 1;
        long valueScale = 1;

        precisionList.add(new float[]{idScale,valueScale});

        Number[] trainNumericalIds = new Number[trainIds.length];
        Number[] predictNumericalIds = new Number[predictIds.length];

        fillAndScaleObjectArray(trainIds,trainNumericalIds,idScale);
        fillAndScaleObjectArray(predictIds,predictNumericalIds,idScale);

        fillAndScaleDoubleArray(trainValues,valueScale);
        fillAndScaleDoubleArray(predictValues,valueScale);

        addIdsAndValuesToLine(trainNumericalIds,trainValues,trainLine, xLabels,yLabels);
        addIdsAndValuesToLine(predictNumericalIds,predictValues,predictLine, xLabels,yLabels);
    }

    private long getScalingFactorForValues(double[] trainValues, double[] predictValues)
    {
        double maxValue = 0.0f;
        double minValue = 0.0f;
        for (int i = 0; i < trainValues.length; i++) {
            if (maxValue<trainValues[i]) {
                maxValue = trainValues[i];
            }
            if(minValue>trainValues[i]){
                minValue = trainValues[i];
            }
        }
        for (int j = 0; j < predictValues.length; j++) {
            if (maxValue<predictValues[j]) {
                maxValue = predictValues[j];
            }
            if(minValue>predictValues[j]){
                minValue = predictValues[j];
            }
        }
        return com.alpine.utility.tools.AlpineMath.adjustUnitsOverAMillion(minValue, maxValue);

    }

    private long getScalingFactorForIds(Object[] trainIds,Object[] predictIds)
    {
        float maxId = 0.0f;
        float minId = 0.0f;

        for (int i = 0; i < trainIds.length; i++) {
            float tmpId = 0.0f;
            try {
                tmpId = Float.valueOf(String.valueOf(trainIds[i]));
            } catch (NumberFormatException e){
            }
            if(maxId<tmpId){
                maxId=tmpId;
            }
            if (minId>tmpId) {
                minId = tmpId;
            }
        }
        for (int i = 0; i < predictIds.length; i++) {
            float tmpId = 0.0f;
            try {
                tmpId = Float.valueOf(String.valueOf(predictIds[i]));
            } catch (NumberFormatException e){
            }
            if(maxId<tmpId){
                maxId=tmpId;
            }
            if (minId>tmpId) {
                minId = tmpId;
            }
        }
        return com.alpine.utility.tools.AlpineMath.adjustUnitsOverAMillion(minId, maxId);
    }

    private void fillAndScaleObjectArray(Object[] orig, Number[] scaled, long scale)
    {
        for (int i=0; i < orig.length;i ++)
        {
            float tmpId = 0.0f;
            try {
                tmpId = Float.valueOf(String.valueOf(orig[i]));
            } catch (NumberFormatException e){
            }
            scaled[i] = tmpId/scale;
        }

    }


    private void fillAndScaleDoubleArray(double[] values, long scale)
    {
        for (int l = 0; l < values.length; l++) {
            values[l] = values[l]/scale;
        }
    }

    private void addIdsAndValuesToLine(Object[] ids, double[] values,VisualLine line, List<String[]> xLabels, List<String[]> yLabels )
    {
        for (int i = 0; i < ids.length; i++) {
            String val = Double.isNaN(values[i]) ? "0" : String.valueOf(values[i]);//FIXED MINERWEB-1063
            VisualPoint point = new VisualPoint(
                    String.valueOf(ids[i]),
                    String.valueOf(val));
            line.addVisualPoint(point);
            xLabels.add(new String[]{String.valueOf(ids[i]),String.valueOf(ids[i])});
            yLabels.add(new String[]{val,val});

        }
    }


}

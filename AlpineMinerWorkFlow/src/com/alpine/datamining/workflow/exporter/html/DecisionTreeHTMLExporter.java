/**
 * ClassName  DataTableHTMLExporter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-4
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.exporter.html;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.TreeVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.dataset.ImageSizeConfig;
import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.datamining.api.impl.visual.tree.ActivityFigure;
import com.alpine.datamining.api.impl.visual.tree.ConditionLabel;
import com.alpine.datamining.api.impl.visual.tree.NeuralFigure;
import com.alpine.datamining.api.impl.visual.tree.ToolTip;
import com.alpine.datamining.operator.neuralnet.sequential.NNModel;
import com.alpine.datamining.utility.Tools;
import com.alpine.datamining.workflow.resources.WorkFlowLanguagePack;
import com.alpine.datamining.workflow.util.ToHtmlWriter;
import org.apache.log4j.Logger;



/**
 * @author John Zhao
 *
 */
public class DecisionTreeHTMLExporter implements VisualOutPutHTMLExporter {
    private static final Logger itsLogger =Logger.getLogger(DecisionTreeHTMLExporter.class);

    private final static Locale locale=Locale.getDefault();

	public StringBuffer  export(VisualizationOutPut visualizationOutPut,List<String> tempFileList, String rootPath)  throws  Exception  {
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
	
		TreeVisualizationOutPut out=( TreeVisualizationOutPut)visualizationOutPut;
	 	
		List<IFigure> listChild = out.getTreeModel().getChildren();
		
		
		String imageSuffix=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Output_Image_3Layer,locale);
			
		if(listChild!=null&&listChild.get(0)!=null
				&&out.getType().equals(TreeVisualizationOutPut.TYPE_NEAURAL_NETWORK)){
			AnalyzerOutPutTrainModel  modelout = (AnalyzerOutPutTrainModel )out.getAnalyzer().getOutPut();
			NNModel model= (NNModel)(modelout.getEngineModel()).getModel();
			StringBuffer baseInfo=new StringBuffer();
			if (model.getLabel().isNumerical())
			{
				baseInfo.append(Tools.getLineSeparator() + "R2: " + (model.getRSquare()) + Tools.getLineSeparator());
			}
			else if (model.getLabel().isNominal())// && getLabel().getMapping().size() == 2)
			{
				baseInfo.append(Tools.getLineSeparator() + "null Deviance: " 
						+ (model.getNullDeviance()) + "; deviance: " 
						+  model.getDeviance() + Tools.getLineSeparator());
			}
			htmlWriter.writeP(baseInfo.toString());
//			result.append("<p>").append(baseInfo).append("</p>");
			htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Output_Nodes_Description,locale));
//			result.append("<p>").append(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Output_Nodes_Description).append("</p>");
			
			StringBuffer table = createNNDescriptionTable(listChild);
			htmlWriter.writeTable(table.toString());
//			result.append(table);
			imageSuffix=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Output_Image,locale);
		}else if(listChild!=null&&listChild.get(0)!=null
				&&out.getType().equals(TreeVisualizationOutPut.TYPE_DECISION_TREE)){
			  ActivityFigure root = getRooTNodes( listChild);
			if(root!=null){
			
				List<ActivityFigure> listChilds =getAllChildList( root);
				listChilds.add(root);
				StringBuffer table = createDTDescriptionTable(listChilds,out.getDepth());
//				result.append(table);
				htmlWriter.writeTable(table.toString());
			}
		}
		htmlWriter.writeP(imageSuffix);
//		result.append("<p>").append(imageSuffix).append("</p>"); 
				
		String image = getImage(out,tempFileList,rootPath ); 
		htmlWriter.writeImg(image);
//		result.append(image);
	 
		return htmlWriter.toStringBuffer();//new Paragraph("DecisionTreeHTMLExporter",  HTMLExporterFactory.getInstance().getContentFont());
	}
	 
 

	/**
	 * @param root
	 * @return
	 */
	private List<ActivityFigure> getAllChildList(ActivityFigure root) {
		List<ActivityFigure> list= new ArrayList<ActivityFigure>();
 
		List<ActivityFigure> children = root.getChildList();
		if(children!=null&&children.size()>0){
			
			list.addAll(children);
			
			
			for (Iterator<ActivityFigure> iterator = children.iterator(); iterator.hasNext();) {
				ActivityFigure activityFigure = iterator
						.next();
				
				list.addAll(getAllChildList(activityFigure));
			}
		}
		return list;
	}



	/**
	 * @param i
	 * @param listChild
	 * @return
	 */
	private  ActivityFigure  getRooTNodes( List<IFigure> listChild) {
		for (Iterator<IFigure> iterator = listChild.iterator(); iterator.hasNext();) {
			IFigure figure = iterator.next();
	 
			if(figure instanceof ActivityFigure&&((ActivityFigure)figure).getLayer()==0){
				return (ActivityFigure)figure;
			}
			
		}
		return null;
	}



	/**
	 * @param listChild
	 * @return
	 */
	private StringBuffer createDTDescriptionTable(List<ActivityFigure>  listChild,int depth)  {
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		ToHtmlWriter tdWriter=new ToHtmlWriter();
		tdWriter.writeTD(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Parent_Node,locale));
		tdWriter.writeTD(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Link_Condition,locale));
		tdWriter.writeTD(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Child_Node,locale));
		htmlWriter.writeTR(tdWriter.toString());
//		t.append("<td>");
//		t.append("<tr>").append(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Parent_Node).append("</tr>");
//		t.append("<tr>").append(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Link_Condition).append("</tr>");
//		t.append("<tr>").append(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Child_Node).append("</tr>");
//		t.append("</td>");
	 
		
		for (int i = 0; i < depth; i++) {
			addLayerDescription(i,htmlWriter);
			addDTRow(getLayerNodes(i,listChild),htmlWriter);  
		}
		return htmlWriter.toStringBuffer();
 
	}

	/**
	 * @param i
	 * @param t
	 * @throws BadElementException 
	 */
	private void addLayerDescription(int i, ToHtmlWriter htmlWriter)  {
		String desc=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Layer_Description,locale)+(i+1);
		ToHtmlWriter tdWriter=new ToHtmlWriter();
//		t.append("<td>").append(desc).append("</td>");
		tdWriter.writeTD(desc);
		htmlWriter.writeTR(tdWriter.toString());
		
	}

	/**
	 * @param layerNodes
	 * @param t 
	 */
	private void addDTRow(List<ActivityFigure> layerNodes, ToHtmlWriter htmlWriter) {
		for (Iterator<ActivityFigure> iterator = layerNodes.iterator(); iterator.hasNext();) {
			ActivityFigure af = iterator.next();
			addDTRow(af,htmlWriter);
			 
		}	
	}

	/**
	 * @param af
	 * @param t
	 */
	private void addDTRow(ActivityFigure af, ToHtmlWriter htmlWriter) {
		String parentName=af.getMessage();
		List<PolylineConnection> paths = af.getPathList();
		for (Iterator<PolylineConnection> iterator = paths.iterator(); iterator.hasNext();) {
			PolylineConnection conn =iterator
					.next();
			String condition="";
			List children = conn.getChildren();
			for (Iterator iterator2 = children.iterator(); iterator2.hasNext();) {
				Object object = (Object) iterator2.next();
				if(object instanceof ConditionLabel){
					condition=((ConditionLabel)object).getText();
				}
			}

			
			createDTDescRow(parentName,condition,(ActivityFigure)conn.getTargetAnchor().getOwner(),htmlWriter);
			
			
		}
		
		
		List<ActivityFigure> children = af.getChildList();
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			ActivityFigure child = (ActivityFigure) iterator.next();
			String childName=child.getMessage();		
		}
		
	}

	/**
	 * @param parentName
	 * @param condition
	 * @param activityFigure
	 * @param t
	 */
	private void createDTDescRow(String parentName, String condition,
			ActivityFigure activityFigure, ToHtmlWriter htmlWriter) {
		ToHtmlWriter tdWriter=new ToHtmlWriter();
		tdWriter.writeTD(parentName);
		tdWriter.writeTD(condition);
		tdWriter.writeTD(activityFigure.getMessage());
		htmlWriter.writeTR(tdWriter.toString());
//		t.append("<td>");		
//		t.append("<tr>").append(parentName).append("</tr>");		
//		t.append("<tr>").append(activityFigure.getMessage()).append("</tr>");
//		t.append("</td>");
	}

	/**
	 * @param i
	 * @param listChild
	 * @return
	 */
	private List<ActivityFigure> getLayerNodes(int i, List<ActivityFigure>  listChild) {
		List<ActivityFigure> result=new ArrayList<ActivityFigure>();
		for (Iterator iterator = listChild.iterator(); iterator.hasNext();) {
			IFigure figure = (IFigure) iterator.next();
	 
			if(figure instanceof ActivityFigure&&((ActivityFigure)figure).getLayer()==i){
			
				result.add((ActivityFigure)figure);
			}
			
		}
		return result;
	}

	/**
	 * @param listChild
	 * @return
	 * @throws BadElementException 
	 */
	private StringBuffer createNNDescriptionTable(List<IFigure> listChild)  {
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		ToHtmlWriter tdWriter=new ToHtmlWriter();
//		t.append("<td>");
//		t.append("<tr>").append( WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Node_Name).append("</tr>");
//		t.append("<tr>").append( WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Node_Description).append("</tr>");		
//		t.append("</td>");
		tdWriter.writeTD(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Node_Name,locale));
		tdWriter.writeTD(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Node_Description,locale));
		htmlWriter.writeTR(tdWriter.toString());
		for (int i = 0; i < listChild.size(); i++) {
			
			addNNRow(listChild.get(i),htmlWriter);
		}
		return htmlWriter.toStringBuffer();
	}

	/**
	 * @param neuralFigure
	 * @param t
	 * @throws BadElementException 
	 */
	private void addNNRow(  Object neuralFigure, ToHtmlWriter htmlWriter)  {
		if(neuralFigure instanceof NeuralFigure){
			ToHtmlWriter tdWriter=new ToHtmlWriter();
//			t.append("<td>");
			String nodeName =((NeuralFigure)neuralFigure).getNodeName();
			tdWriter.writeTD(nodeName);
//			t.append("<tr>").append(nodeName).append("</tr>");
		    String nodeDesc=((ToolTip)((NeuralFigure)neuralFigure).getToolTip()).getMessage();
			nodeDesc=nodeDesc.replace(":",": ");
//			t.append("<tr>").append(nodeDesc).append("</tr>");
			tdWriter.writeTD(nodeDesc);
//			t.append("</td>");
			htmlWriter.writeTR(tdWriter.toString());
		}
	}

	public String getImage(VisualizationOutPut visualizationOutPut, List<String> tempFileList, String rootPath )   throws  Exception {
		 TreeVisualizationOutPut out=( TreeVisualizationOutPut)visualizationOutPut;

		int i = rootPath.lastIndexOf(File.separator);
		String curdir = rootPath.substring(0, i);
		String name = System.currentTimeMillis()+".jpg";
		String fileName=curdir+File.separator+name;
		VisualUtility. saveFigureAsJPG(out,fileName);
	 
		itsLogger.debug("DecisionTreeHTMLExporter export to:"+fileName);
		String imageFile = "."+File.separator+name;
		return imageFile;
	}

	/**
	 * @param visualizationObject
	 * @param type 
	 * @return
	 */
	private ImageSizeConfig countSize(FreeformLayeredPane visualizationObject, String type) {
		
		List<Figure> list = visualizationObject.getChildren();
		ImageSizeConfig size=new ImageSizeConfig();
		 
		int width=800;
		int height=600;
		
		if(list!=null&&list.size()>0){
			if(type.equals(TreeVisualizationOutPut.TYPE_NEAURAL_NETWORK)){
				width=countFigureWidth(list);//
				height=countFigureHeight(list);
				
			}else if(type.equals(TreeVisualizationOutPut.TYPE_DECISION_TREE)){
				width=countFigureWidth(list);
				height=countFigureHeight(list);
			 
			
			}
		}
		size.setOutputHeight(height);
		size.setOutputWidth(width);
		
		return size;
	}

	
	private int countFigureWidth(List<Figure> list) {
		int max=0;
		for(int i =0;i<list.size();i++){
			Figure figure=list.get(i);
			if(max<figure.getBounds().x){						
				max=figure.getBounds().x;
			}
		}
		return max+100;
	}
	/**
	 * @param list
	 * @return
	 */
	protected int countFigureHeight(List<Figure> list) {
		int max=0;
		for(int i =0;i<list.size();i++){
			Figure figure=list.get(i);
			if(max<figure.getBounds().y){						
				max=figure.getBounds().y;
			}
		}
		return max+60;
	}
 
}

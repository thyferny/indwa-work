package com.alpine.datamining.api.impl.hadoop.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModel;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.NominalColumn;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.operator.tree.threshold.DevideCond;
import com.alpine.datamining.operator.tree.threshold.GreaterDevideCond;
import com.alpine.datamining.operator.tree.threshold.LessEqualDevideCond;
import com.alpine.datamining.operator.tree.threshold.NorminalDevideCond;
import com.alpine.datamining.operator.tree.threshold.Side;
import com.alpine.datamining.operator.tree.threshold.Tree;
import com.alpine.datamining.utility.DataType;
import com.alpine.hadoop.tree.mapper.DecisionTreePredictMapper;
import com.alpine.hadoop.tree.model.HadoopTree;

public class DecisionTreeHadoopModel extends AbstractHadoopModel{
	private static final long serialVersionUID = 6047261092664473196L;
	 
	private HadoopTree hadoopTree;
	private String dependent;
	public String getDependent() {
		return dependent;
	}

	public void setDependent(String dependent) {
		this.dependent = dependent;
	}

	private List<String> leafValues=new ArrayList<String>();
	
	public List<String> getLeafValues() {
		String[] finalLeafValues=new String[this.hadoopTree.getDependentMap().size()];
		for(Entry<String, Integer> ditinctValue: this.hadoopTree.getDependentMap().entrySet())
		{
			finalLeafValues[ditinctValue.getValue()]=ditinctValue.getKey();
//			leafValues.add(ditinctValue.getValue(), ditinctValue.getKey());
		}
//		finalLeafValues.
		leafValues=Arrays.asList(finalLeafValues);
//		leafValues.addAll(finalLeafValues);
		
		
//		distinctLeafValues(this.hadoopTree);
		return leafValues;
	}

	private void distinctLeafValues(HadoopTree tree){
		if(tree.isLeaf()){
			try{
				if(leafValues.contains(tree.getLabel())==false){
					leafValues.add(tree.getLabel());
					return;
				}
			}catch(Exception e){
				System.out.println("for temp use:"+e.getMessage()) ;
			}
		}
		else if(tree.getChildren().size()>0){
			for(HadoopTree ht:tree.getChildren()){
				distinctLeafValues(ht);
			}
			return;
		}
	}
	
     
	public HadoopTree getHadoopTree() {
		return hadoopTree;
	}

	public void setHadoopTree(HadoopTree hadoopTree) {
		this.hadoopTree = hadoopTree;
	}

	public DecisionTreeHadoopModel(HadoopTree root) {
		this.hadoopTree = root;
	}
 
 
    public String toString() {
        return this.hadoopTree.toString();
    }
    
    public Tree toVisualTree(){
//		hadoopTree.getChildren();
		
		Tree visualTree = new Tree(null);
		
		addVisualChild(visualTree,hadoopTree.getChildren());
		if(hadoopTree.getChildren().size()==0)
		{
			for(String classValue:hadoopTree.getDependentMap().keySet())
			{
				visualTree.addCount(classValue, hadoopTree.getCount()[hadoopTree.getDependentMap().get(classValue).intValue()]);
			}
		}
		visualTree.setLabel(hadoopTree.getLabel()) ;
	//	handleLeafChildLabel(visualTree,hadoopTree);
		return visualTree;
    }
    
    private static void addVisualChild(Tree visualTree,
			List<HadoopTree> children) {
		for (HadoopTree hadoopTree : children) {
			Tree child = new Tree(null);
			if(hadoopTree.getColumnMap()==null){
				
				continue;//ignore this null node, 
			}
			DevideCond condition = null;
			if(hadoopTree.isCategorical()==true){
				Column column = new NominalColumn(getColumnName(hadoopTree),DataType.NOMINAL); 
				condition = new NorminalDevideCond("", column, hadoopTree.getValue());
			}else if (hadoopTree.getComparison()==true){
				Column column = new NumericColumn(getColumnName(hadoopTree),DataType.NUMERICAL ); 
				
				Double d = Double.parseDouble(String.format("%.2f", Double.parseDouble(hadoopTree.getValue())));
				
				condition = new LessEqualDevideCond(column, d);
			}else{
				Column column = new NumericColumn(getColumnName(hadoopTree),DataType.NUMERICAL ); 
				Double d = Double.parseDouble(String.format("%.2f", Double.parseDouble(hadoopTree.getValue())));
				
				condition = new GreaterDevideCond(column, d);

			}
			
			visualTree.addChild(child, condition) ;
			
 			if(hadoopTree.getChildren()!=null){
 				addVisualChild(child,hadoopTree.getChildren());
 				
 			}
 			 
			if(hadoopTree.isLeaf()==true){
				child.setLabel(hadoopTree.getLabel()) ;
				
				int[] counts = hadoopTree.getCount();
				hadoopTree.getColumnMap();
				
				for(Entry<String, Integer> mapEl : hadoopTree.getDependentMap().entrySet()) {
					child.addCount(mapEl.getKey(), counts[mapEl.getValue().intValue()]);
				}
				
				
		
				//hadoopTree.getCount();
				//child.addCount(hadoopTree.getLabel(), hadoopTree.getCount()[0]) ;
			}
			
	//		handleLeafChildLabel(child,hadoopTree );
			 
		}
		
		
	}

	private static void handleLeafChildLabel(Tree tree, HadoopTree hadoopTree) {
		Iterator<Side> sides = tree.childIterator();
		List<HadoopTree> hadoopChild = hadoopTree.getChildren(); 
		int[] countArray = hadoopTree.getCount() ;
		if(sides.hasNext()==false){
			return;
		}
// 
//		ArrayList<String> newLabelList = new ArrayList<String>();
//		while(sides.hasNext()){
//			Side side = sides.next();
//			Tree childNode = side.getChild() ;
//			if(childNode.isLeaf()==true){
//				newLabelList.add(e)
//			}
//			
//		}		
//				
//				
//				
				
				
				
				
				
				
	//+++++++++++++++++++++++++++++++++++			
		while(sides.hasNext()){
			Side side = sides.next();
			Tree childNode = side.getChild() ;
			if(childNode.isLeaf()==true){
				int index = findLableIndex(hadoopChild,childNode.getLabel());
				if(index>=0){
					childNode.addCount(childNode.getLabel(), countArray[index]);
				 }
			}
			
		}
		
		
	}

 

	private static int findLableIndex(List<HadoopTree> childList, String label) {
		int index = 0;
		for (Iterator iterator = childList.iterator(); iterator.hasNext();) {
			HadoopTree hadoopTree = (HadoopTree) iterator.next();
			if(hadoopTree.isLeaf()==true) {
				if(label.equals( hadoopTree.getLabel() )==true){
					return index ; 
				}else{
					index = index + 1;
				}
			}
		}
		return -1;
	}

	private static String getColumnName(HadoopTree hadoopTree) {
		return hadoopTree.getColumnMap().get(hadoopTree.getPureFeature());
	 
	}
 
}

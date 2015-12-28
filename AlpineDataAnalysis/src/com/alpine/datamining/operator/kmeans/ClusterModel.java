
package com.alpine.datamining.operator.kmeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.OutputObject;
import com.alpine.datamining.utility.Tools;


public class ClusterModel extends OutputObject {

	
	private static final long serialVersionUID = 3503104282041912663L;
	public static final int UNASSIGNABLE = -1;
	private String schemaName;
	private String tableName;
	private String resultTableName;
	private String clusterColumn = "";
	private ArrayList<Cluster> clusters;
	private ArrayList<String[]> clustersArrays;
	private Boolean isStable=true;
	private String stableInformation;
	private ArrayList<ArrayList<String>> centerPoint;
	private DataSet dataSet;
	private double measureAvg; 
	private boolean sortResult=false;
	
	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public double getMeasureAvg() {
		return measureAvg;
	}

	public void setMeasureAvg(double measureAvg) {
		this.measureAvg = measureAvg;
	}

	public DataSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<ArrayList<String>> getCenterPoint() {
		if(centerPoint!=null&&centerPoint.size()>1&&sortResult==false){
			ArrayList<String>[] cPointArrays=new ArrayList[centerPoint.size()-1];
			for(int i =1;i<centerPoint.size();i++){
				cPointArrays[i-1]=centerPoint.get(i);
			}
			Arrays.sort(cPointArrays, new Comparator<List<String>>() {

				@Override
				public int compare(List<String> o1, List<String> o2) {
					if(o1!=null&&o1.size()>0
							&&o2!=null&&o2.size()>0){
						if(Integer.parseInt(o1.get(0))<Integer.parseInt(o2.get(0))){
							return -1;
						}else if(Integer.parseInt(o1.get(0))>Integer.parseInt(o2.get(0))){
							return 1;
						}else{
							return 0;
						}
					}
					return 0;
				}
			});
			ArrayList<ArrayList<String>> newCenterPoint = new ArrayList<ArrayList<String>>();
			newCenterPoint.add(centerPoint.get(0));
			newCenterPoint.addAll(Arrays.asList(cPointArrays));
			centerPoint=newCenterPoint;
			sortResult=true;
		}
		return centerPoint;
	}

	public void setCenterPoint(ArrayList<ArrayList<String>> centerPoint) {
		this.centerPoint = centerPoint;
	}

	public String getStableInformation() {
		return stableInformation;
	}

	public void setStableInformation(String stableInformation) {
		if(this.stableInformation==null)
		{
			this.stableInformation = stableInformation;
		}else
		{
			this.stableInformation += stableInformation;
		}

	}

	public Boolean getIsStable() {
		return isStable;
	}

	public void setIsStable(Boolean isStable) {
		this.isStable = isStable;
	}
	
	public String getResultTableName() {
		return resultTableName;
	}

	public void setResultTableName(String resultTableName) {
		this.resultTableName = resultTableName;
	}
	
	public ClusterModel(int k) {
		this.clusters = new ArrayList<Cluster>(k);
	}

	public void setClusterColumn(String clusterColumn)
	{
		this.clusterColumn = clusterColumn;
	}
	public String getClusterColumn()
	{
		return clusterColumn;
	}
	public int getNumberOfClusters() {
		return clusters.size();
	}
	

	public Cluster getCluster(int i) {
		return clusters.get(i);
	}
	public void setCluster(Cluster cluster)
	{
		clusters.add(cluster);
	}
	public Collection<Cluster> getClusters() {
		return clusters;
	}
	
    public void generateClustersArrays(int n)
	{
		clustersArrays=new ArrayList<String []>(n);
	}
	
	public ArrayList<String []> getClustersArrays()
	{
		return clustersArrays;
	}
	public void setClustersArrays(String[] array)
	{
		clustersArrays.add(array);
	}

	public String getName() {
		return "Cluster Model";
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		int sum = 0;
		for (int i = 0; i < getNumberOfClusters(); i++) {
			Cluster cl = getCluster(i);
			long numObjects = cl.getNumberOfData();
			result.append("Cluster " + cl.getClusterId() + ": " + numObjects + " items" + Tools.getLineSeparator());
			sum += numObjects;
		}
		result.append("Total number of items: " + sum + Tools.getLineSeparator());
		return result.toString();
	}
}

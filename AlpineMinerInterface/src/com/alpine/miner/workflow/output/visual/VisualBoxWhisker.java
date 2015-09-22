/**
 * ClassName VisualBoxWhisker.java
 *
 * Version information: 3.00
 *
 * Data: 2011-7-11
 * @author zhaoyong
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

 
public class VisualBoxWhisker  {
	
	private Number mean;

	//lwhisker: 1.4, 
	//lbox: 2.9,
	//median: 3.35,
	//ubox: 5.1, 
	//uwhisker: 7.5, 
	//outliers: [0.5, 7.9]

	public Number getMean() {
		return mean;
	}
	public void setMean(Number mean) {
		this.mean = mean;
	}
	public VisualBoxWhisker(String type, Number max, Number min, Number q1,
			Number q3, Number mean, Number median) {
 //x value...
		this.type = type;
		this.uwhisker = max;
		this.lwhisker = min;
		this.lbox = q1;
		this.ubox = q3;
		//currently is an array...
		this.mean = mean;
		this.median = median;
	}
	private String type;
 

	private Number uwhisker,lwhisker,lbox,ubox,median;
	Number[] outliers= new Number[0];
    private Number xIndex;
    private Number seriesIndex;

	private double xValue;
	
	public double getxValue() {
		return xValue;
	}

    public Number getxIndex() {
        return xIndex;
    }

    public Number getSeriesIndex()
    {
        return seriesIndex;
    }
	public Number getUwhisker() {
		return uwhisker;
	}
	public void setUwhisker(Number uwhisker) {
		this.uwhisker = uwhisker;
	}
	public Number getLwhisker() {
		return lwhisker;
	}
	public void setLwhisker(Number lwhisker) {
		this.lwhisker = lwhisker;
	}
	public Number getLbox() {
		return lbox;
	}
	public void setLbox(Number lbox) {
		this.lbox = lbox;
	}
	public Number getUbox() {
		return ubox;
	}
	public void setUbox(Number ubox) {
		this.ubox = ubox;
	}
	public Number[] getOutliers() {
		return outliers;
	}
	public void setOutliers(Number[] outliers) {
		this.outliers = outliers;
	}
	 
	public Number getMedian() {
		return median;
	}
	public void setMedian(Number median) {
		this.median = median;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	//count in the offset
	public void setxValue(double xValue) { 
		this.xValue=xValue;
		
	}

   public void setxIndex(int xIndex)
   {
       this.xIndex = xIndex;
   }
    public void setSeriesIndex(int seriesIndex)
    {
        this.seriesIndex = seriesIndex;
    }
}

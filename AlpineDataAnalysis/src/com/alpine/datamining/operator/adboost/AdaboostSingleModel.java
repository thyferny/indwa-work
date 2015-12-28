
package com.alpine.datamining.operator.adboost;

import java.io.Serializable;

import com.alpine.datamining.operator.Model;

public class AdaboostSingleModel implements Serializable {

	
	private static final long serialVersionUID = -157500056206117067L;
	private Model model;
	private String type;
	public AdaboostSingleModel(){
		
	}

	public AdaboostSingleModel(Model model, double peoso, String name,String type) {
		super();
		this.model = model;
		this.peoso = peoso;
		this.name = name;
		this.type = type;
	}

	private double peoso = 0;
	private String name;
	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public double getPeoso() {
		return peoso;
	}

	public void setPeoso(double peoso) {
		this.peoso = peoso;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


}

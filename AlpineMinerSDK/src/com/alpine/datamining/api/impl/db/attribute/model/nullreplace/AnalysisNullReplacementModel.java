package com.alpine.datamining.api.impl.db.attribute.model.nullreplace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.db.Resources;

public class AnalysisNullReplacementModel{

	public static final String TAG_NAME = "NullReplacementModel";

    public static final String ATTR_GROUP_BY = "groupBy";

	private List<AnalysisNullReplacementItem> nullReplacements = null;

    private String groupBy;

    protected static final String DEFAULT_GROUP = "";

	public List<AnalysisNullReplacementItem> getNullReplacements() {
		return nullReplacements;
	}

	public void setNullReplacements(List<AnalysisNullReplacementItem> nullReplacements) {
		this.nullReplacements = nullReplacements;
	}

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

	public AnalysisNullReplacementModel() {
		this.nullReplacements = new ArrayList<AnalysisNullReplacementItem>();
        this.groupBy = DEFAULT_GROUP;
	}

	public AnalysisNullReplacementModel(List<AnalysisNullReplacementItem> nullReplacements) {
		this.nullReplacements = nullReplacements;
        this.groupBy = DEFAULT_GROUP;
	}

    public AnalysisNullReplacementModel(List<AnalysisNullReplacementItem> nullReplacements, String groupBy) {
        this.nullReplacements = nullReplacements;
        this.groupBy = groupBy;
    }

	public boolean equals(Object obj) {
		if (obj instanceof AnalysisNullReplacementModel) {
			return ListUtility.equalsIgnoreOrder(nullReplacements,
					((AnalysisNullReplacementModel) obj).getNullReplacements())
                    && groupBy.equals(((AnalysisNullReplacementModel) obj).getGroupBy());
		} else {
			return false;
		}

	}
	public void addNullReplacement (AnalysisNullReplacementItem layer) {
		if(nullReplacements==null){
			nullReplacements= new ArrayList<AnalysisNullReplacementItem>();
		}
		nullReplacements.add(layer);
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
        sb.append(ATTR_GROUP_BY).append(":").append(getGroupBy()).append(";");
		int i=0;
		for (Iterator<AnalysisNullReplacementItem> iterator = getNullReplacements().iterator(); iterator.hasNext();) {
			 AnalysisNullReplacementItem item = iterator.next();
			 sb.append(item.toString());
			 if(i!=getNullReplacements().size()-1){
				 sb.append(Resources.FieldSeparator);
			 }
			 i++;
		}
		return sb.toString();
	}
}

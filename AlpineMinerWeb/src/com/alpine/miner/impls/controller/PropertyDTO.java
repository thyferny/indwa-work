/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * PropertyDTO.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Aug 20, 2011
 */

package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.flowvariables.model.FlowVariable;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.operator.dataset.DataSetWebModel;
import com.alpine.miner.impls.web.resource.operator.ordinary.OutputCreationParameterWebModel;
import com.alpine.miner.impls.web.resource.operator.pigexecute.PigExecuteOperatorUIModel;
import com.alpine.miner.impls.web.resource.operator.sampling.SampleSizeModelUI;
import com.alpine.miner.impls.woe.WOEModelUI;
import com.alpine.miner.workflow.operator.parameter.*;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateFieldsModel;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowFieldsModel;
import com.alpine.miner.workflow.operator.parameter.association.ExpressionModel;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBinsModel;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinModel;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModel;
import com.alpine.miner.workflow.operator.parameter.hiddenlayer.HiddenLayersModel;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionColumnsModel;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementModel;
import com.alpine.miner.workflow.operator.parameter.subflow.TableMappingModel;
import com.alpine.miner.workflow.operator.parameter.tablejoin.TableJoinModel;
import com.alpine.miner.workflow.operator.parameter.univariate.UnivariateModel;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldsModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author sam_zang
 *
 */
public class PropertyDTO {
    public enum PropertyType {
		PT_STRING,
		PT_INT,
		PT_DOUBLE,
		PT_SINGLE_SELECT,
		PT_MULTI_SELECT,
		PT_BOOLEAN,
		PT_CHOICE,
		PT_CUSTOM_WHERECLAUSE,
		PT_CUSTOM_TABLEJOIN,
		PT_CUSTOM_AGG_COLUMN,
		PT_CUSTOM_AGG_GROUPBY,
		PT_CUSTOM_AGG_WINDOW,
		PT_CUSTOM_VAR_FIELDLIST,
		PT_CUSTOM_VAR_QUANTILE,
		PT_CUSTOM_NEURAL_HIDDEN_LAYERS,
		PT_CUSTOM_INTERACTION_COLUMNS,
		PT_CUSTOM_BIN,
		PT_CUSTOM_REPLACEMENT,
		PT_CUSTOM_COHORTS,
		PT_CUSTOM_ADABOOST,
		PT_CUSTOM_WOE,
		PT_CUSTOM_TABLESET,
        PT_CUSTOM_SAMPLE_SIZE,
		
		PT_CUSTOM_SUBFLOWPATH, 
		PT_CUSTOM_TABLEMAPING,
	    PT_CUSTOM_EXITOPERATOR,
	    PT_CUSTOM_SUBFLOWVARIABLE,
	    PT_CUSTOM_UNIVARIATE_MODEL,
        PT_CUSTOM_NOTE,

        //hadoopFileOperator
        PT_CUSTOM_NAME_HD_CONNECTIONNAME,
        PT_CUSTOM_NAME_HD_FILENAME,
        PT_CUSTOM_NAME_HD_FORMAT,
        PT_CUSTOM_NAME_HD_CSVFILESTRUCTURE,
        PT_CUSTOM_NAME_HD_ROWFILTER_CONDITION,
        PT_CUSTOM_NAME_HD_JOIN,
        PT_CUSTOM_HD_TABLESET,
        PT_CUSTOM_PIG_EXEC_FILESTRUCTURE,
        PT_CUSTOM_PIG_EXEC_SCRIPT,
        
        PT_HD_FILE_EXPLORER,//common type for hadoop file explorer

        PT_CUSTOM_HIDDEN,
	    PT_TEXT,
		PT_NOT_IN_LIST,
		PT_HISTOGRAM,
		
		PT_OUTPUT_CREATION_PARAMETER,
		PT_UNKNOWN // it is bad to get this one.
	}

	private static final String FALSE_VALUE = "false";
	
	public PropertyDTO(String value, OperatorParameter parameter, String userName, ResourceType dbType,Locale locale){
		this.name = parameter.getName();
		this.type = PropertyUtil.getType(name);
		this.displayName = PropertyUtil.getDisplayName(name,locale);
		this.fullSelection = PropertyUtil.getDefaultValues(parameter,  userName,  dbType,locale);
		this.value = value;
		
		initDefaults();
	}
	
	PropertyDTO(String name, String value,Locale locale){
		this.name = name;
		this.type = PropertyUtil.getType(name);	
		this.displayName = PropertyUtil.getDisplayName(name,locale);
		this.fullSelection = PropertyUtil.getSlection(name);
		this.value = value;
		
		initDefaults();
		
	}
	
	private void initDefaults() {
		if (type == PropertyType.PT_BOOLEAN) {
			if (value == null || value.length() == 0) {
				this.value = FALSE_VALUE;
			}
		}
		//not used any more ...   MINERWEB - 593 
		if (this.name.equals(OperatorParameter.NAME_kernel_type)) {
			this.value = PropertyUtil.kernel_type_label(value);
		}
		else if (this.name.equals(OperatorParameter.NAME_scoreType)) {
			this.value = PropertyUtil.score_type_label(value);
		}
		 
												
		if (this.fullSelection != null) {
			this.store = new DataStore();
			for (String str : this.fullSelection) {
				this.store.add(new DataItem(str));
			}
		} 
		
		if (this.type == PropertyType.PT_MULTI_SELECT
				|| this.type == PropertyType.PT_CUSTOM_BIN) {
			if (value != null) {
				this.selected = value.split(",");
			}
			else {
				this.selected = new String[0];
			}						
		}
	}



	class DataItem {
		String name;
		DataItem(String name) {
			this.name = name;
		}
		@SuppressWarnings("unused")
		public DataItem() {}
	}
	
	class DataStore {
		String identifier;
		String label;
		List<DataItem> items;
		DataStore() {
			identifier = "name";
			label = "name";
			items = new LinkedList<DataItem>();
		}
		
		void add(DataItem item) {
			this.items.add(item);
		}
	}
	
	@SuppressWarnings("unused")
	private PropertyDTO() {}

	private String name;
	private String displayName;
	private String value;
	private PropertyType type;
	
	private String[] fullSelection;
	private String[] selected;

    public DataStore getStore() {
        return store;
    }

    public void setStore(DataStore store) {
        this.store = store;
    }

    private DataStore store;
	
	//this is for special dialog
	private TableJoinModel tableJoinModel;
	private HiddenLayersModel hiddenLayersModel;
	private ColumnBinsModel columnBinsModel;
	private AggregateFieldsModel aggregateFieldsModel =null;
	private WindowFieldsModel windowFieldsModel;
	private InterActionColumnsModel interActionModel;
	private DerivedFieldsModel derivedFieldsModel;
	private QuantileFieldsModelUI quantileFieldsModel;
	private AdaboostModelUI adaboostModel;
	private NullReplacementModel nullReplacementModel;
	private ExpressionModel expressionModel;	
	private WOEModelUI woeModel;
    private SampleSizeModelUI sampleSizeModelUI;
	private DataSetWebModel tableSetModel;
	private OutputCreationParameterWebModel outputCreationParamModel;
	
	private TableMappingModel subflowTableMappingModel;
	private FlowVariable flowVariable;
	
	private UnivariateModel univariateModel;

    private CSVFileStructureModel csvFileStructureModel;
    

    private XMLFileStructureModel xmlFileStructureModel;

    private AlpineLogFileStructureModel alpineLogFileStructureModel;


    private JSONFileStructureModel jsonFileStructureModel;

    private HadoopJoinModel hadoopJoinModel;

    private HadoopUnionModel hadoopUnionModel;
    
    private PigExecuteOperatorUIModel hadoopPigExecuteScriptModel;

    public HadoopUnionModel getHadoopUnionModel() {
        return hadoopUnionModel;
    }

    public void setHadoopUnionModel(HadoopUnionModel hadoopUnionModel) {
        this.hadoopUnionModel = hadoopUnionModel;
    }

    public HadoopJoinModel getHadoopJoinModel() {
        return hadoopJoinModel;
    }

    public void setHadoopJoinModel(HadoopJoinModel hadoopJoinModel) {
        this.hadoopJoinModel = hadoopJoinModel;
    }

	public FlowVariable getFlowVariable() {
		return flowVariable;
	}

	public void setFlowVariable(FlowVariable flowVariable) {
		this.flowVariable = flowVariable;
	}

	public TableMappingModel getSubflowTableMappingModel() {
		return subflowTableMappingModel;
	}

	public void setSubflowTableMappingModel(
			TableMappingModel subflowTableMappingModel) {
		this.subflowTableMappingModel = subflowTableMappingModel;
	}

	/**
	 * @return the tableJoinModel
	 */
	public TableJoinModel getTableJoinModel() {
		return tableJoinModel;
	}

	/**
	 * @param tableJoinModel the tableJoinModel to set
	 */
	public void setTableJoinModel(TableJoinModel tableJoinModel) {
		this.tableJoinModel = tableJoinModel;
	}

	/**
	 * @return the hiddenLayersModel
	 */
	public HiddenLayersModel getHiddenLayersModel() {
		return hiddenLayersModel;
	}

	/**
	 * @param hiddenLayersModel the hiddenLayersModel to set
	 */
	public void setHiddenLayersModel(HiddenLayersModel hiddenLayersModel) {
		this.hiddenLayersModel = hiddenLayersModel;
	}

	/**
	 * @return the columnBinsModel
	 */
	public ColumnBinsModel getColumnBinsModel() {
		return columnBinsModel;
	}

	/**
	 * @param columnBinsModel the columnBinsModel to set
	 */
	public void setColumnBinsModel(ColumnBinsModel columnBinsModel) {
		this.columnBinsModel = columnBinsModel;
	}

	/**
	 * @return the aggregateFieldsModel
	 */
	public AggregateFieldsModel getAggregateFieldsModel() {
		return aggregateFieldsModel;
	}

	/**
	 * @param aggregateFieldsModel the aggregateFieldsModel to set
	 */
	public void setAggregateFieldsModel(AggregateFieldsModel aggregateFieldsModel) {
		this.aggregateFieldsModel = aggregateFieldsModel;
	}

	/**
	 * @return the windowFieldsModel
	 */
	public WindowFieldsModel getWindowFieldsModel() {
		return windowFieldsModel;
	}

	/**
	 * @param windowFieldsModel the windowFieldsModel to set
	 */
	public void setWindowFieldsModel(WindowFieldsModel windowFieldsModel) {
		this.windowFieldsModel = windowFieldsModel;
	}

	/**
	 * @return the interActionModel
	 */
	public InterActionColumnsModel getInterActionModel() {
		return interActionModel;
	}

	/**
	 * @param interActionModel the interActionModel to set
	 */
	public void setInterActionModel(InterActionColumnsModel interActionModel) {
		this.interActionModel = interActionModel;
	}

	/**
	 * @return the derivedFieldsModel
	 */
	public DerivedFieldsModel getDerivedFieldsModel() {
		return derivedFieldsModel;
	}

	/**
	 * @param derivedFieldsModel the derivedFieldsModel to set
	 */
	public void setDerivedFieldsModel(DerivedFieldsModel derivedFieldsModel) {
		this.derivedFieldsModel = derivedFieldsModel;
	}

	/**
	 * @return the quantileFieldsModel
	 */
	public QuantileFieldsModelUI getQuantileFieldsModel() {
		return quantileFieldsModel;
	}

	/**
	 * @param uiObj the quantileFieldsModel to set
	 */
	public void setQuantileFieldsModel(QuantileFieldsModelUI uiObj) {
		this.quantileFieldsModel = uiObj;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the type
	 */
	public PropertyType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(PropertyType type) {
		this.type = type;
	}

	/**
	 * @return the fullSelection
	 */
	public String[] getFullSelection() {
		return fullSelection;
	}

	/**
	 * @param fullSelection the fullSelection to set
	 */
	public void setFullSelection(String[] fullSelection) {
		this.fullSelection = fullSelection;
	}

	/**
	 * @return the selected
	 */
	public String[] getSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(String[] selected) {
		this.selected = selected;
	}

	/**
	 * @param valueList
	 */
	public void setAvailableValues(List<String> valueList) {
		if (valueList == null || valueList.size() == 0) {
            if(this.name.equals("valueDomain")==true || "pivotColumn".equals(this.name)==true){
                this.fullSelection = null;
                this.store = null;
                this.value=null;
            }
			return;
		}
		this.fullSelection = new String[valueList.size()];
		this.store = new DataStore();
		int idx = 0;

		for (String str : valueList) {
			this.fullSelection[idx++] = str;
			this.store.add(new DataItem(str));
		}
	}


	public void setAdaboostPersistenceModel(AdaboostModelUI obj) {
		this.adaboostModel = obj;
	}

	/**
	 * @return the derivedFieldsModel
	 */
	public AdaboostModelUI getAdaboostPersistenceModel() {
		return this.adaboostModel;
	}

	/**
	 * @param obj
	 */
	public void setExpressionModel(ExpressionModel obj) {
		this.expressionModel = obj;		
	}

	/**
	 * @param obj
	 */
	public void setNullReplacementModel(NullReplacementModel obj) {
		this.nullReplacementModel = obj;				
	}

	/**
	 * @return
	 */
	public NullReplacementModel getNullReplacementModel() {
		return this.nullReplacementModel;
	}

	/**
	 * @return
	 */
	public ExpressionModel getExpressionModel() {
		return this.expressionModel;
	}

	public WOEModelUI getWoeModel() {
		return woeModel;
	}

	public void setWoeModel(WOEModelUI woeModel) {
		this.woeModel = woeModel;
	}

	public DataSetWebModel getTableSetModel() {
		return tableSetModel;
	}

	public void setTableSetModel(DataSetWebModel tableSetModel) {
		this.tableSetModel = tableSetModel;
	}

	public OutputCreationParameterWebModel getOutputCreationParamModel() {
		return outputCreationParamModel;
	}

	public void setOutputCreationParamModel(
			OutputCreationParameterWebModel outputCreationParamModel) {
		this.outputCreationParamModel = outputCreationParamModel;
	}

	public UnivariateModel getUnivariateModel() {
		return univariateModel;
	}

	public void setUnivariateModel(UnivariateModel univariateModel) {
		this.univariateModel = univariateModel;
	}

    public SampleSizeModelUI getSampleSizeModelUI() {
        return sampleSizeModelUI;
    }

    public void setSampleSizeModelUI(SampleSizeModelUI sampleSizeModelUI) {
        this.sampleSizeModelUI = sampleSizeModelUI;
    }

	public PigExecuteOperatorUIModel getHadoopPigExecuteScriptModel() {
		return hadoopPigExecuteScriptModel;
	}

	public void setHadoopPigExecuteScriptModel(
			PigExecuteOperatorUIModel hadoopPigExecuteScriptModel) {
		this.hadoopPigExecuteScriptModel = hadoopPigExecuteScriptModel;
	}

	public CSVFileStructureModel getCsvFileStructureModel() {
		return csvFileStructureModel;
	}

	public void setCsvFileStructureModel(CSVFileStructureModel csvFileStructureModel) {
		this.csvFileStructureModel = csvFileStructureModel;
	}

	public XMLFileStructureModel getXmlFileStructureModel() {
		return xmlFileStructureModel;
	}

	public void setXmlFileStructureModel(XMLFileStructureModel xmlFileStructureModel) {
		this.xmlFileStructureModel = xmlFileStructureModel;
	}

    public AlpineLogFileStructureModel getAlpineLogFileStructureModel() {
        return alpineLogFileStructureModel;
    }

    public void setAlpineLogFileStructureModel(AlpineLogFileStructureModel alpineLogFileStructureModel) {
        this.alpineLogFileStructureModel = alpineLogFileStructureModel;
    }

    public JSONFileStructureModel getJsonFileStructureModel() {
        return jsonFileStructureModel;
    }

    public void setJsonFileStructureModel(JSONFileStructureModel jsonFileStructureModel) {
        this.jsonFileStructureModel = jsonFileStructureModel;
    }

}

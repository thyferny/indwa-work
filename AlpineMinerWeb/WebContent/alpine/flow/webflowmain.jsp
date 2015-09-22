<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/alpine/commons/config.jsp" %>
<%@page import="com.alpine.miner.impls.mail.MailConfiguration"%>
<%
    String host = request.getRemoteHost();
    String port = String.valueOf(request.getServerPort());
    String baseURL = request.getContextPath();//"http://" + request.getHeader("Host")+ request.getContextPath();
    String progressImage = baseURL + "/images/progressBar.gif";

%>
<html id="htmlRootContainer">
<fmt:bundle basename="app">
<script type="text/javascript">
//move here in order to every modules can load NLS in onload function
dojo.ready(function(){
    if(!alpine.nls){
        alpine.nls = {};
    }
    alpine.nls.OK = "<fmt:message key='OK'/>";
    alpine.nls.Yes = "<fmt:message key='Yes'/>";
    alpine.nls.No = "<fmt:message key='No'/>";
    alpine.nls.Cancel = "<fmt:message key='Cancel'/>";
    alpine.nls.leak_datasource_to_dataexplorer = "<fmt:message key='leak_datasource_to_dataexplorer'/>";
    alpine.nls.value_not_valid = "<fmt:message key='value_not_valid'/>";
    alpine.nls.message_unknow_error = "<fmt:message key='message_unknow_error'/>";
    alpine.nls.flow_exist_error = "<fmt:message key='flow_exist_error'/>";
    alpine.nls.publish_flow_error = "<fmt:message key='publish_flow_error'/>";
    alpine.nls.already_in_personal = "<fmt:message key='already_in_personal'/>";
    alpine.nls.select_columns = "<fmt:message key='select_columns'/>";
    alpine.nls.columns = "<fmt:message key='columns'/>";
	
    alpine.nls.operator_add_error_invalid_name = "<fmt:message key='operator_add_error_invalid_name'/>";
    alpine.nls.select_dep_column_first = "<fmt:message key='dependent_column_required'/>";
    alpine.nls.edit_filter = "<fmt:message key='edit_filter'/>";
    alpine.nls.avaibale_columns = "<fmt:message key='available_columns'/>";
    alpine.nls.edit_aggregate_columns = "<fmt:message key='edit_agg_columns'/>";
    alpine.nls.edit_agg_win_columns = "<fmt:message key='edit_agg_win_columns'/>";
    alpine.nls.edit_agg_groupby = "<fmt:message key='edit_agg_groupby'/>";
    alpine.nls.agg_column_expression = "<fmt:message key='agg_column_expression'/>";
    alpine.nls.agg_column_alias = "<fmt:message key='agg_column_alias'/>";
    alpine.nls.agg_column_alias_exist = "<fmt:message key='agg_column_alias_exist'/>";
    alpine.nls.agg_win_column_exist = "<fmt:message key='agg_win_column_exist'/>";
    alpine.nls.agg_column_validation_name_exist = "<fmt:message key='agg_column_validation_name_exist'/>";
    alpine.nls.join_column_same = "<fmt:message key='join_column_same'/>";
    alpine.nls.table_alias_same = "<fmt:message key='table_alias_same'/>";
    alpine.nls.agg_win_result_column = "<fmt:message key='agg_win_result_column'/>";
    alpine.nls.agg_win_function = "<fmt:message key='agg_win_function'/>";
    alpine.nls.agg_win_spec = "<fmt:message key='agg_win_spec'/>";
    alpine.nls.agg_win_data_type = "<fmt:message key='agg_win_data_type'/>";
    alpine.nls.already_exist = "<fmt:message key='already_exist'/>";
    alpine.nls.Test_Connection_Successful = "<fmt:message key='Test_Connection_Successful'/>";
    alpine.nls.Test_Connection_error = "<fmt:message key='Test_Connection_error'/>";
    alpine.nls.barchart_Series_Category_set_tip = "<fmt:message key='barchart_Series_Category_set_tip'/>";

    alpine.nls.workbench_op_recent_used_label = "<fmt:message key='workbench_op_recent_used_label'/>";

    alpine.nls.right_table_empty = "<fmt:message key='right_table_empty'/>";
    alpine.nls.agg_groupby_column_exist = "<fmt:message key='agg_groupby_column_exist'/>";
    alpine.nls.agg_groupby_column = "<fmt:message key='agg_groupby_column'/>";
    alpine.nls.agg_groupby_edit_title = "<fmt:message key='agg_groupby_edit_title'/>";
    alpine.nls.agg_groupby_column_invalid = "<fmt:message key='agg_groupby_column_invalid'/>";
    alpine.nls.agg_groupby_column_one =  "<fmt:message key='agg_groupby_column_one'/>";
    alpine.nls.cohorst_selected_operators =  "<fmt:message key='cohorst_selected_operators'/>";
    alpine.nls.cohorts_index =  "<fmt:message key='cohorts_index'/>";
    alpine.nls.cohorts_max =  "<fmt:message key='cohorts_max'/>";

    alpine.nls.nvr_select_one_or_more = "<fmt:message key='nvr_select_one_or_more'/>";
    alpine.nls.nvr_cannot_replace_groupby = "<fmt:message key='nvr_cannot_replace_groupby'/>";
    alpine.nls.nvr_placeholder_no_group = "<fmt:message key='nvr_placeholder_no_group'/>";
    alpine.nls.nvr_with_string = "<fmt:message key='nvr_with_string'/>";
    alpine.nls.nvr_with_value = "<fmt:message key='nvr_with_value'/>";
    alpine.nls.nvr_avg = "<fmt:message key='nvr_avg'/>";
    alpine.nls.nvr_min = "<fmt:message key='nvr_min'/>";
    alpine.nls.nvr_max = "<fmt:message key='nvr_max'/>";
    alpine.nls.nvr_column_name = "<fmt:message key='nvr_column_name'/>";
    alpine.nls.nvr_method = "<fmt:message key='nvr_method'/>";

    alpine.nls.var_dev_result_column = "<fmt:message key='var_dev_result_column'/>";
    alpine.nls.var_dev_data_type = "<fmt:message key='var_dev_data_type'/>";
    alpine.nls.var_dev_column_exist = "<fmt:message key='var_dev_column_exist'/>";
    alpine.nls.var_dev_available_column = "<fmt:message key='var_dev_available_column'/>";
    alpine.nls.var_sql_spec = "<fmt:message key='var_sql_spec'/>";
    alpine.nls.var_derived_edit_title = "<fmt:message key='var_derived_edit_title'/>";
    alpine.nls.variable_quantile_edit_title = "<fmt:message key='variable_quantile_edit_title'/>";
    alpine.nls.variable_quantile_edit_empty_tip = "<fmt:message key='variable_quantile_edit_empty_tip'/>";

    alpine.nls.var_dev_column_create_tip = "<fmt:message key='var_dev_column_create_tip'/>";
    alpine.nls.var_dev_column_update_tip = "<fmt:message key='var_dev_column_update_tip'/>";
    alpine.nls.var_dev_column_create_title = "<fmt:message key='var_dev_column_create_title'/>";
    alpine.nls.var_dev_column_update_title = "<fmt:message key='var_dev_column_update_title'/>";
    alpine.nls.var_dev_column_btn_cancel = "<fmt:message key='var_dev_column_btn_cancel'/>";
    alpine.nls.var_dev_column_btn_update = "<fmt:message key='var_dev_column_btn_update'/>";
    alpine.nls.var_dev_column_btn_create = "<fmt:message key='var_dev_column_btn_create'/>";

    alpine.nls.var_quan_result_column = "<fmt:message key='var_quan_result_column'/>";
    alpine.nls.var_quan_data_type = "<fmt:message key='var_quan_data_type'/>";
    alpine.nls.var_quan_create_new = "<fmt:message key='var_quan_create_new'/>";
    alpine.nls.var_quantile_bins = "<fmt:message key='var_quantile_bins'/>";
    alpine.nls.quantile_listinput_hint = "<fmt:message key='quantile_listinput_hint'/>";

    alpine.nls.ConnectionValid = "<fmt:message key='ConnectionValid'/>";

    alpine.nls.flow_update_failed = "<fmt:message key='flow_update_failed'/>";

    alpine.nls.bin_index = "<fmt:message key='bin_index'/>";
    alpine.nls.bin_value_range = "<fmt:message key='bin_value_range'/>";
    alpine.nls.bin_value_from = "<fmt:message key='bin_value_from'/>";
    alpine.nls.bin_value_to = "<fmt:message key='bin_value_to'/>";
    alpine.nls.bin_values = "<fmt:message key='bin_values'/>";

    //these are for data_explorer
    alpine.nls.Replace_Model = "<fmt:message key='Replace_Model'/>";

    alpine.nls.step_run =  "<fmt:message key='step_run_flow_tip'/>";
    alpine.nls.clear_step_run_result = "<fmt:message key='clear_step_run_result'/>";
    alpine.nls.interaction_column_already_exists =  "<fmt:message key='interaction_column_already_exists'/>";
    alpine.nls.create_interaction_columns_tip =  "<fmt:message key='create_interaction_columns_tip'/>";

    alpine.nls.Data_Explorer = "<fmt:message key='Data_Explorer'/>";
    alpine.nls.Scatter_Plot_Chart = "<fmt:message key='Scatter_Plot_Chart'/>";
    alpine.nls.Scat_Plot_Martix = "<fmt:message key='Scat_Plot_Martix'/>";
    alpine.nls.Univariate_Plot_Chart = "<fmt:message key='Univariate_Plot_Chart'/>";
    alpine.nls.Box_and_Wisker_Chart = "<fmt:message key='Box_and_Wisker_Chart'/>";
    alpine.nls.Bar_Chart = "<fmt:message key='Bar_Chart'/>";
    alpine.nls.TimeSeries_Chart = "<fmt:message key='TimeSeries_Chart'/>";
    alpine.nls.time_series_date_format = "<fmt:message key='time_series_date_format'/>";
    alpine.nls.Correlation_Analysis = "<fmt:message key='Correlation_Analysis'/>";
    alpine.nls.Summary_Statistics = "<fmt:message key='Summary_Statistics'/>";
    alpine.nls.Frequency_Analysis = "<fmt:message key='Frequency_Analysis'/>";
    alpine.nls.Histogram_Chart = "<fmt:message key='Histogram_Chart'/>";
    alpine.nls.no_explore_actions = "<fmt:message key='no_explore_actions'/>";

    //these are for import
    alpine.nls.MSG_Please_select_file = "<fmt:message key='MSG_Please_select_file'/>";
    alpine.nls.MSG_Please_selectFlow_file = "<fmt:message key='MSG_Please_selectFlow_file'/>";
    alpine.nls.MSG_Same_file_existed = "<fmt:message key='MSG_Same_file_existed'/>";
    alpine.nls.MSG_Upload_Successful = "<fmt:message key='MSG_Upload_Successful'/>";
    alpine.nls.MSG_Upload_Error = "<fmt:message key='MSG_Upload_Error'/>";

    alpine.nls.QuantileBinInvalid = "<fmt:message key='QuantileBinInvalid'/>";
    alpine.nls.Properties_popup = "<fmt:message key='Properties_popup'/>";
    alpine.nls.invalid_flow = "<fmt:message key='invalid_flow'/>";
    alpine.nls.running_error_tip_no_operator = "<fmt:message key='running_error_tip_no_operator'/>";
    alpine.nls.no_login = "<fmt:message key='no_login'/>";
    alpine.nls.invalid_properties = "<fmt:message key='invalid_properties'/>";
    alpine.nls.flow_not_found = "<fmt:message key='flow_not_found'/>";

    alpine.nls.group_name = "<fmt:message key='group_name'/>";
    alpine.nls.group_desc = "<fmt:message key='group_desc'/>";
    alpine.nls.ConnectionValid = "<fmt:message key='ConnectionValid'/>";
    alpine.nls.createGroup = "<fmt:message key='createGroup'/>";
    alpine.nls.updateGroup = "<fmt:message key='updateGroup'/>";
    alpine.nls.deleteGroup = "<fmt:message key='deleteGroup'/>";
    alpine.nls.user_name = "<fmt:message key='user_name'/>";
    alpine.nls.user_email = "<fmt:message key='user_email'/>";
    alpine.nls.user_last = "<fmt:message key='user_last'/>";
    alpine.nls.user_first = "<fmt:message key='user_first'/>";
    alpine.nls.user_roles = "<fmt:message key='user_roles'/>";

    alpine.nls.roles_admin = "<fmt:message key='roles_admin'/>";
    alpine.nls.roles_modeler = "<fmt:message key='roles_modeler'/>";
    alpine.nls.roles_analyst = "<fmt:message key='roles_analyst'/>";

    alpine.nls.createUser = "<fmt:message key='createUser'/>";
    alpine.nls.updateUser = "<fmt:message key='updateUser'/>";
    alpine.nls.deleteUser = "<fmt:message key='deleteUser'/>";

    alpine.nls.MSG_Please_selectDBDriver_file = "<fmt:message key='MSG_Please_selectDBDriver_file'/>";
    alpine.nls.MSG_Upload_Driver_Invalid = "<fmt:message key='MSG_Upload_Driver_Invalid'/>";
    alpine.nls.MSG_Upload_Driver_Successful = "<fmt:message key='MSG_Upload_Driver_Successful'/>";
    alpine.nls.MSG_Upload_Driver_Error = "<fmt:message key='MSG_Upload_Driver_Error'/>";

    alpine.nls.update_not_saved = "<fmt:message key='update_not_saved'/>";
    alpine.nls.revert_not_revert = "<fmt:message key='revert_not_revert'/>";

    alpine.nls.MSG_Please_finish_input	= "<fmt:message key='MSG_Please_finish_input'/>";
    alpine.nls.MSG_TEST_Connenction_OK= "<fmt:message key='MSG_TEST_Connenction_OK'/>";
    alpine.nls.MSG_TEST_Connection_Failure = "<fmt:message key='MSG_TEST_Connection_Failure'/>";
    alpine.nls.MSG_TEST_Connenction_Error = "<fmt:message key='MSG_TEST_Connenction_Error'/>";
    alpine.nls.MSG_Copy_Conn_confirmation = "<fmt:message key='MSG_Copy_Conn_confirmation'/>";

    alpine.nls.MSG_save_change_confirmation = "<fmt:message key='MSG_save_change_confirmation'/>";
    alpine.nls.MSG_Restore_successful = "<fmt:message key='MSG_Restore_successful'/>";
    alpine.nls.MSG_Save_successful = "<fmt:message key='MSG_Save_successful'/>";

    alpine.nls.result_not_found = "<fmt:message key='result_not_found'/>";

    alpine.nls.select_rsult_first= "<fmt:message key='select_rsult_first'/>";

    alpine.nls.same_name_exists = "<fmt:message key='same_name_exists'/>";
    alpine.nls.same_name_exists_inGroup = "<fmt:message key='same_name_exists_inGroup'/>";

    alpine.nls.share_alert_already_exist = "<fmt:message key='share_alert_already_exist'/>";

    alpine.nls.save_as = "<fmt:message key='save_as'/> ";
    alpine.nls.delete_flow_tip = "<fmt:message key='delete_flow_tip'/> ";
    alpine.nls.delete_flow_confirm_tip = "<fmt:message key='delete_flow_confirm_tip'/> ";
    alpine.nls.open_flow_tip="<fmt:message key='open_flow_tip'/> ";
    alpine.nls.open_flow_history_tip="<fmt:message key='open_flow_history_tip'/> ";

    alpine.nls.clear_output_table = "<fmt:message key='clear_output_table'/>";
    alpine.nls.clear_output_table_completed = "<fmt:message key='clear_output_table_completed'/>";
    alpine.nls.table_clean_operatorName = "<fmt:message key='table_clean_operatorName'/>";
    alpine.nls.table_clean_tableName = "<fmt:message key='table_clean_tableName'/>";
    alpine.nls.show_table_metadata = "<fmt:message key='show_table_metadata'/>";
    alpine.nls.table_column_grid_head_columnName = "<fmt:message key='table_column_grid_head_columnName'/>";
    alpine.nls.table_column_grid_head_columnType = "<fmt:message key='table_column_grid_head_columnType'/>";
    alpine.nls.table_clean_connectionName = "<fmt:message key='table_clean_connectionName'/>";
    alpine.nls.table_clean_outputType = "<fmt:message key='table_clean_outputType'/>";
    alpine.nls.table_clean_confirm = "<fmt:message key='table_clean_confirm'/>";
    alpine.nls.table_clean_nochoise = "<fmt:message key='table_clean_nochoise'/>";

    alpine.nls.cant_empty_condition =  "<fmt:message key='cant_empty_condition'/>";

    alpine.nls.condition_exists =  "<fmt:message key='condition_exists'/>";
    alpine.nls.all_columns_added =  "<fmt:message key='all_columns_added'/>";
    alpine.nls.hidden_layer_size_error =  "<fmt:message key='hidden_layer_size_error'/>";
    alpine.nls.hidden_layer_msg_validateFailed = "<fmt:message key='hidden_layer_msg_validateFailed'/>"
    alpine.nls.same_interaction_column =  "<fmt:message key='same_interaction_column'/>";

    alpine.nls.udf_file = "<fmt:message key='UDF_File'/> ";
    alpine.nls.MSG_Please_select_udf_file = "<fmt:message key='MSG_Please_select_udf_file'/> ";

    alpine.nls.duplicateflow_alert_nochoose = "<fmt:message key='duplicateflow_alert_nochoose'/>";
    alpine.nls.duplicateflow_alert_newflowisexist = "<fmt:message key='duplicateflow_alert_newflowisexist'/>";
    alpine.nls.duplicateflow_alert_copyerror = "<fmt:message key='duplicateflow_alert_copyerror'/>";
    alpine.nls.duplicateflow_alert_noNewName = "<fmt:message key='duplicateflow_alert_noNewName'/>";
    alpine.nls.history_clear_menu = "<fmt:message key='history_clear_menu'/>";
    alpine.nls.recent_history_empty_item = "<fmt:message key='recent_history_empty_item'/>";

    alpine.nls.renameflow_dialog_title="<fmt:message key='renameflow_dialog_title'/>";
    alpine.nls.renameflow_dialog_okbutton= "<fmt:message key='renameflow_dialog_okbutton'/>";
    alpine.nls.renamecat_dialog_title="<fmt:message key='renamecat_dialog_title'/>";
    alpine.nls.renamecat_dialog_okbutton= "<fmt:message key='renamecat_dialog_okbutton'/>";
    alpine.nls.newcat_dialog_title="<fmt:message key='newcat_dialog_title'/>";
    alpine.nls.newcat_dialog_okbutton= "<fmt:message key='newcat_dialog_okbutton'/>";

    alpine.nls.scheduler_button_remove = "<fmt:message key='scheduler_button_remove'/>";
    alpine.nls.scheduler_validation_cannot_turn_enable = "<fmt:message key='scheduler_validation_cannot_turn_enable'/>";
    alpine.nls.scheduler_grid_item_workflow_count = "<fmt:message key='scheduler_grid_item_workflow_count'/>";
    alpine.nls.scheduler_validation_duplicate_value = "<fmt:message key='scheduler_validation_duplicate_value'/>";
    alpine.nls.onlinehelp_menu_title = "<fmt:message key='onlinehelp_menu_title'/>";

    alpine.nls.audit_dialog_title = "<fmt:message key='audit_dialog_title'/>";
    alpine.nls.audit_button_remove = "<fmt:message key='audit_button_remove'/>";
    alpine.nls.audit_grid_title_category = "<fmt:message key='audit_grid_title_category'/>";
    alpine.nls.audit_grid_title_time = "<fmt:message key='audit_grid_title_time'/>";
    alpine.nls.audit_grid_title_action = "<fmt:message key='audit_grid_title_action'/>";
    alpine.nls.audit_grid_title_detail = "<fmt:message key='audit_grid_title_detail'/>";
    alpine.nls.adaboost_param_title = "<fmt:message key='adaboost_param_title'/>";
    alpine.nls.adaboost_param_button = "<fmt:message key='adaboost_param_button'/>";
    alpine.nls.cohort_edit_button = "<fmt:message key='cohort_edit_button'/>";

    alpine.nls.ic_not_selected ="<fmt:message key='ic_not_selected'/>";

    alpine.nls.please_finish_input  ="<fmt:message key='please_finish_input'/>";

    alpine.nls.want_to_delete_conn  ="<fmt:message key='want_to_delete_conn'/>";

    alpine.nls.please_select_a_Flow = "<fmt:message key='please_select_a_Flow'/>";
    alpine.nls.anlysis_column_same = "<fmt:message key='anlysis_column_same'/>";
    alpine.nls.anlysis_column_same_Category_Series = "<fmt:message key='anlysis_column_same_Category_Series'/>";
    alpine.nls.error ="<fmt:message key='Error'/>";

    alpine.nls.model_not_found ="<fmt:message key='model_not_found'/>";

    alpine.nls.please_select_model ="<fmt:message key='please_select_model'/>";

    alpine.nls.Edit_Interaction_Columns ="<fmt:message key='Edit_Interaction_Columns'/>";
    alpine.nls.Edit_Hidden_Layer ="<fmt:message key='Edit_Hidden_Layer'/>";

    alpine.nls.scheduler_edit_trigger_type_head_interval = "<fmt:message key='scheduler_edit_trigger_type_head_interval'/>";
    alpine.nls.scheduler_edit_trigger_type_head_schedule = "<fmt:message key='scheduler_edit_trigger_type_head_schedule'/>";
    alpine.nls.Mail_Test_Success = "<fmt:message key='Mail_Test_Success'/>";

    alpine.nls.Edit_Table_Join = "<fmt:message key='Edit_Table_Join'/>";

    alpine.nls.Table_Join_Input_Tables = "<fmt:message key='Table_Join_Input_Tables'/>";
    alpine.nls.Table_Join_Output_Table = "<fmt:message key='Table_Join_Output_Table'/>";
    alpine.nls.Table_Join_Alias = "<fmt:message key='Table_Join_Alias'/>";
    alpine.nls.Table_Join_Fields = "<fmt:message key='Table_Join_Fields'/>";
    alpine.nls.Table_Join_Fields_One_Field = "<fmt:message key='Table_Join_Fields_One_Field'/>";
    alpine.nls.Table_Join_Error_Join_Column_Empty = "<fmt:message key='Table_Join_Error_Join_Column_Empty'/>";
    alpine.nls.Table_Join_Error_No_Input_Tables = "<fmt:message key='Table_Join_Error_No_Input_Tables'/>";
    alpine.nls.Table_Join_Error_Invalid_Alias = "<fmt:message key='Table_Join_Error_Invalid_Alias'/>";
    alpine.nls.Table_Join_All_Button = "<fmt:message key='Table_Join_All_Button'/>";
    alpine.nls.Table_Join_None_Button = "<fmt:message key='Table_Join_None_Button'/>";
    alpine.nls.Table_Join_Join_Edit_Title = "<fmt:message key='Table_Join_Join_Edit_Title'/>";
    alpine.nls.Table_Join_Join_Create_Title = "<fmt:message key='Table_Join_Join_Create_Title'/>";
    alpine.nls.Table_Join_Join_Conditions = "<fmt:message key='Table_Join_Join_Conditions'/>";
    alpine.nls.Table_Join_Join_Number =  "<fmt:message key='Table_Join_Join_Number'/>";

    alpine.nls.table_alias_empty =  "<fmt:message key='table_alias_empty'/>";
    alpine.nls.join_column_empty =  "<fmt:message key='join_column_empty'/>";
    alpine.nls.join_condition_empty=  "<fmt:message key='join_condition_empty'/>";
    alpine.nls.join_table_added =   "<fmt:message key='join_table_added'/>";
    alpine.nls.join_table_removed = "<fmt:message key='join_table_removed'/>";
    alpine.nls.table_join_config_removed = "<fmt:message key='table_join_config_removed'/>";

    alpine.nls.key_columns = "<fmt:message key='key_columns'/>";
    alpine.nls.file_name = "<fmt:message key='file_name'/>";

    alpine.nls.Naive_Bayes = "<fmt:message key='Naive_Bayes'/>";
    alpine.nls.Decision_Tree = "<fmt:message key='Decision_Tree'/>";
    alpine.nls.Logistic_Regression = "<fmt:message key='Logistic_Regression'/>";
    alpine.nls.Logistic_Regression_Group_By_Value = "<fmt:message key='Logistic_Regression_Group_By_Value'/>";
    alpine.nls.Logistic_Regression_Group_By_Result_Select_Tip = "<fmt:message key='Logistic_Regression_Group_By_Result_Select_Tip'/>";

    alpine.nls.SVM_Classification = "<fmt:message key='SVM_Classification'/>";
    alpine.nls.Cart_Tree = "<fmt:message key='Cart_Tree'/>";
    alpine.nls.Neural_Network = "<fmt:message key='Neural_Network'/>";
    alpine.nls.Reference_Column = "<fmt:message key='Reference_Column'/>";
    alpine.nls.alert_same_reference_column = "<fmt:message key='alert_same_reference_column'/>";
    alpine.nls.session_ended = "<fmt:message key='session_ended'/>";
    alpine.nls.security = "<fmt:message key='security'/>";

    alpine.nls.woe_setting_button = "<fmt:message key='woe_setting_button'/>";
    alpine.nls.woe_setting_text = "<fmt:message key='woe_setting_text'/>";
    alpine.nls.woe_setting_alert_delete_norecord = "<fmt:message key='woe_setting_alert_delete_norecord'/>";
    alpine.nls.woe_setting_confirm_delete = "<fmt:message key='woe_setting_confirm_delete'/>";
    alpine.nls.woe_setting_column_noselected = "<fmt:message key='woe_setting_column_noselected'/>";
    alpine.nls.woe_setting_numeric_grid_id = "<fmt:message key='woe_setting_numeric_grid_id'/>";
    alpine.nls.woe_setting_numeric_grid_bottomVal = "<fmt:message key='woe_setting_numeric_grid_bottomVal'/>";
    alpine.nls.woe_setting_numeric_grid_upperVal = "<fmt:message key='woe_setting_numeric_grid_upperVal'/>";
    alpine.nls.woe_setting_numeric_grid_woeVal = "<fmt:message key='woe_setting_numeric_grid_woeVal'/>";
    alpine.nls.woe_setting_nominal_grid_id = "<fmt:message key='woe_setting_nominal_grid_id'/>";
    alpine.nls.woe_setting_nominal_grid_optionalVal = "<fmt:message key='woe_setting_nominal_grid_optionalVal'/>";
    alpine.nls.woe_setting_nominal_grid_woeVal = "<fmt:message key='woe_setting_nominal_grid_woeVal'/>";
    alpine.nls.woe_setting_nominal_alert_noAvailableValue = "<fmt:message key='woe_setting_nominal_alert_noAvailableValue'/>";
    alpine.nls.woe_setting_nominal_button_editOpVal = "<fmt:message key='woe_setting_nominal_button_editOpVal'/>";
    alpine.nls.woe_setting_grid_validate_false = "<fmt:message key='woe_setting_grid_validate_false'/>";
    alpine.nls.woe_setting_alert_nocolumn = "<fmt:message key='woe_setting_alert_nocolumn'/>";
    alpine.nls.woe_setting_alert_nocolumn_to_calculate = "<fmt:message key='woe_setting_alert_nocolumn_to_calculate'/>";
    alpine.nls.woe_setting_alert_invalidcolumns = "<fmt:message key='woe_setting_alert_invalidcolumns'/>";

    alpine.nls.histogram_widget_binnumber = "<fmt:message key='histogram_widget_binnumber'/>";
    alpine.nls.histogram_widget_binwidth = "<fmt:message key='histogram_widget_binwidth'/>";
    alpine.nls.histogram_widget_or = "<fmt:message key='histogram_widget_or'/>";
    alpine.nls.histogram_header_for = "<fmt:message key='histogram_header_for'/>";
    alpine.nls.histogram_header_type = "<fmt:message key='histogram_header_type'/>";
    alpine.nls.histogram_type_bins = "<fmt:message key='histogram_type_bins'/>";
    alpine.nls.histogram_type_width = "<fmt:message key='histogram_type_width'/>";
    alpine.nls.histogram_header_min = "<fmt:message key='histogram_header_min'/>";
    alpine.nls.histogram_header_max = "<fmt:message key='histogram_header_max'/>";
    alpine.nls.histogram_inline_nomin = "<fmt:message key='histogram_inline_nomin'/>";
    alpine.nls.histogram_inline_nomax = "<fmt:message key='histogram_inline_nomax'/>";
    alpine.nls.histogram_form_error = "<fmt:message key='histogram_form_error'/>";
    alpine.nls.histogram_alert_nocolumn = "<fmt:message key='histogram_alert_nocolumn'/>";

    alpine.nls.set_op_union_msg = "<fmt:message key='set_op_union_msg'/>";
    alpine.nls.set_op_unionall_msg = "<fmt:message key='set_op_unionall_msg'/>";
    alpine.nls.set_op_intersect_msg = "<fmt:message key='set_op_intersect_msg'/>";
    alpine.nls.set_op_except_msg = "<fmt:message key='set_op_except_msg'/>";
    alpine.nls.set_op_form_error = "<fmt:message key='set_op_form_error'/>";
    alpine.nls.set_op_datatype_error = "<fmt:message key='set_op_datatype_error'/>";
    alpine.nls.set_op_magic_label = "<fmt:message key='set_op_magic_label'/>";
    alpine.nls.set_op_magic_order = "<fmt:message key='set_op_magic_order'/>";
    alpine.nls.set_op_magic_name = "<fmt:message key='set_op_magic_name'/>";
    alpine.nls.set_op_magic_name_all = "<fmt:message key='set_op_magic_name_all'/>";
    alpine.nls.set_op_alias_validate_msg_invalid = "<fmt:message key='set_op_alias_validate_msg_invalid'/>";
    alpine.nls.set_op_alias_validate_msg_duplicate = "<fmt:message key='set_op_alias_validate_msg_duplicate'/>";

    alpine.nls.multi_file_2plus_files = "<fmt:message key='multi_file_2plus_files'/>";
    alpine.nls.multi_file_config_struc = "<fmt:message key='multi_file_config_struc'/>";
    alpine.nls.multi_file_2plus_different_from = "<fmt:message key='multi_file_2plus_different_from'/>";

    alpine.nls.inline_edit_choose_column = "<fmt:message key='inline_edit_choose_column'/>";
    alpine.nls.inline_edit_delete_row = "<fmt:message key='inline_edit_delete_row'/>";

    alpine.nls.err_empty_name = "<fmt:message key='err_empty_name'/>";
    alpine.nls.err_invalid_char_inname= "<fmt:message key='err_invalid_char_inname'/>";
    alpine.nls.can_not_connect_server = "<fmt:message key='can_not_connect_server'/>";

    alpine.nls.range_erro_tip = "<fmt:message key='range_erro_tip'/>";
    alpine.nls.column_name_select_tip = "<fmt:message key='column_name_select_tip'/>";

    alpine.nls.security_LocalProvider = "<fmt:message key='security_LocalProvider'/>";
    alpine.nls.security_LDAPProvider = "<fmt:message key='security_LDAPProvider'/>";
    alpine.nls.security_CustomProvider = "<fmt:message key='security_CustomProvider'/>";
    alpine.nls.LDAP_TEST_CONNECT = "<fmt:message key='LDAP_TEST_CONNECT'/>";

    alpine.nls.dbresource_menu_name = "<fmt:message key='dbresource_menu_name'/>";
    alpine.nls.dbresource_tree_menu_refresh = "<fmt:message key='dbresource_tree_menu_refresh'/>";

    alpine.nls.var_sql_text_tip = "<fmt:message key='var_sql_text_tip'/>";

    alpine.nls.flow_category_menu_create = "<fmt:message key='flow_category_menu_create'/>";
    alpine.nls.flow_category_menu_refresh = "<fmt:message key='flow_category_menu_refresh'/>";
    alpine.nls.flow_category_menu_rename = "<fmt:message key='flow_category_menu_rename'/>";
    alpine.nls.flow_category_menu_remove = "<fmt:message key='flow_category_menu_remove'/>";
    alpine.nls.flow_category_menu_change = "<fmt:message key='flow_category_menu_change'/>";
    alpine.nls.flow_category_menu_create_flow = "<fmt:message key='flow_category_menu_create_flow'/>";
    alpine.nls.flow_category_menu_rename_flow = "<fmt:message key='flow_category_menu_rename_flow'/>";
    alpine.nls.Flow_History = "<fmt:message key='Flow_History'/>";
    alpine.nls.delete_button = "<fmt:message key='delete_button'/>";
    alpine.nls.flow_category_message_remove_cagegory = "<fmt:message key='flow_category_message_remove_cagegory'/>";
    alpine.nls.flow_category_message_move_cover = "<fmt:message key='flow_category_message_move_cover'/>";
    alpine.nls.flow_category_message_no_category_to_move = "<fmt:message key='flow_category_message_no_category_to_move'/>";
    alpine.nls.flow_category_message_move_opened = "<fmt:message key='flow_category_message_move_opened'/>";
    alpine.nls.createflow_alert_flowname_empty = "<fmt:message key ='createflow_alert_flowname_empty'/>";
    alpine.nls.createflow_alert_invalidcharacters =  "<fmt:message key ='createflow_alert_invalidcharacters'/>";
    alpine.nls.createflow_alert_invaliddefaultflow =     "<fmt:message key ='createflow_alert_invaliddefaultflow'/>";

    alpine.nls.message_unknow_error = "<fmt:message key='message_unknow_error'/>";

    alpine.nls.nullValueChangeGrid_col_value = "<fmt:message key='Properties_Value'/>";
    alpine.nls.nullValueChangeGrid_col_selectAll = "<fmt:message key='nullValueChangeGrid_col_selectAll'/>";

    alpine.nls.tableset_config_title = "<fmt:message key='tableset_config_title'/>";
    alpine.nls.tableset_config_btn_label = "<fmt:message key='tableset_config_btn_label'/>";

    alpine.nls.plda_column_same = "<fmt:message key='plda_column_same'/>";
    alpine.nls.plda_output_table_same = "<fmt:message key='plda_output_table_same'/>";

    alpine.nls.Flow_Name = "<fmt:message key='Flow_Name'/>";
    alpine.nls.Oprator_Name = "<fmt:message key='Oprator_Name'/>";
    alpine.nls.Parameter_Name = "<fmt:message key='Parameter_Name'/>";
    alpine.nls.Parameter_Value = "<fmt:message key='Parameter_Value'/>";
    alpine.nls.Find_Value_alert_msg = "<fmt:message key='Find_Value_alert_msg'/>";
    alpine.nls.Replace_with_alert_msg = "<fmt:message key='Replace_with_alert_msg'/>";
    alpine.nls.Parameter_Name_alert_msg = "<fmt:message key='Parameter_Name_alert_msg'/>";
    alpine.nls.Parameter_replace_alert_tip = "<fmt:message key='Parameter_replace_alert_tip'/>";
    alpine.nls.Search_Result_NORESULT = "<fmt:message key='Search_Result_NORESULT'/>";
    alpine.nls.Search_Result_Tip = "<fmt:message key='Search_Result_Tip'/>";
    alpine.nls.Search_Result_Lable_Tip = "<fmt:message key='Search_Result_Lable_Tip'/>";
    alpine.nls.Parameter_replaceValue_not_exist = "<fmt:message key='Parameter_replaceValue_not_exist'/>";

    alpine.nls.find_and_replace_param_dbConnectionName = "<fmt:message key='find_and_replace_param_dbConnectionName'/>";
    alpine.nls.find_and_replace_param_schemaName = "<fmt:message key='find_and_replace_param_schemaName'/>";
    alpine.nls.find_and_replace_param_tableName = "<fmt:message key='find_and_replace_param_tableName'/>";
    alpine.nls.find_and_replace_param_outputSchema = "<fmt:message key='find_and_replace_param_outputSchema'/>";

    alpine.nls.Copy_to_Group = "<fmt:message key='Copy_to_Group' />";
    alpine.nls.Copy_to_Personal = "<fmt:message key='Copy_to_Personal' />";
    alpine.nls.Data_Sources = "<fmt:message key='Data_Sources' />";
    alpine.nls.CHOOSE_FILE = "<fmt:message key='CHOOSE_FILE' />";

    alpine.nls.output_creation_param_title = "<fmt:message key='output_creation_param_title'/>";

    alpine.nls.split_model_group_by_column_error_tip = "<fmt:message key='split_model_group_by_column_error_tip'/>";
    alpine.nls.split_model_group_by_column_error_tip_1 = "<fmt:message key='split_model_group_by_column_error_tip_1'/>";

    alpine.nls.flowvariable_grid_title_flow = "<fmt:message key='flowvariable_grid_title_flow'/>";
    alpine.nls.flowvariable_grid_title_variable = "<fmt:message key='flowvariable_grid_title_variable'/>";
    alpine.nls.flowvariable_grid_title_variable_summary = "<fmt:message key='flowvariable_grid_title_variable_summary'/>";
    alpine.nls.flowvariable_grid_title_value = "<fmt:message key='flowvariable_grid_title_value'/>";
    alpine.nls.flowvariable_editor_variable_check_msg = "<fmt:message key='flowvariable_editor_variable_check_msg'/>";
    alpine.nls.flowvariable_message_delete = "<fmt:message key='flowvariable_message_delete'/>";
    alpine.nls.flowvariable_message_conflict = "<fmt:message key='flowvariable_message_conflict'/>";
    alpine.nls.flowvariable_editor_validate_failure = "<fmt:message key='flowvariable_editor_validate_failure'/>";

    alpine.nls.subflow_parameter_name = "<fmt:message key='subflow_parameter_name'/>";
    alpine.nls.subflow_parameter_value = "<fmt:message key='subflow_parameter_value'/>";
    alpine.nls.subflow_need_select_alert = "<fmt:message key='subflow_need_select_alert'/>";
    alpine.nls.subflow_tableMapping_invalidte = "<fmt:message key='subflow_tableMapping_invalidte'/>";
    alpine.nls.subflow_menu_edit_subflow = "<fmt:message key='subflow_menu_edit_subflow'/>";
    alpine.nls.subflow_inputtable_same = "<fmt:message key='subflow_inputtable_same'/>";
    alpine.nls.subflow_tableMapping_count_invalidte = "<fmt:message key='subflow_tableMapping_count_invalidte'/>";

    alpine.nls.license_displayer_expire_alert = "<fmt:message key='license_displayer_expire_alert'/>";

    alpine.nls.session_manager_grid_loginname = "<fmt:message key='session_manager_grid_loginname'/>";
    alpine.nls.session_manager_grid_logintime = "<fmt:message key='session_manager_grid_logintime'/>";
    alpine.nls.session_manager_delete = "<fmt:message key='session_manager_delete'/>";
    alpine.nls.Possible_reasons_dbconnect = "<fmt:message key='Possible_reasons_dbconnect'/>";

    alpine.nls.import_file_maxfile_num = "<fmt:message key='import_file_maxfile_num'/>";
    alpine.nls.import_file_error_tip_duplicate = "<fmt:message key='import_file_error_tip_duplicate'/>";
    alpine.nls.import_file_delete_tip = "<fmt:message key='import_file_delete_tip'/>";
    alpine.nls.hadoop_prop_choose_file_select_conn_tip = "<fmt:message key='hadoop_prop_choose_file_select_conn_tip'/>";
    alpine.nls.hadoop_prop_choose_file_select_file_tip = "<fmt:message key='hadoop_prop_choose_file_select_file_tip'/>";
    alpine.nls.hadoop_prop_choose_file_select_file_name_tip = "<fmt:message key='hadoop_prop_choose_file_select_file_name_tip'/>";
    
    alpine.nls.hadoop_prop_choose_file_select_no_permission = "<fmt:message key='hadoop_prop_choose_file_select_no_permission'/>";
    alpine.nls.hadoop_prop_right_menu_file_explorer = "<fmt:message key='hadoop_prop_right_menu_file_explorer'/>";
    alpine.nls.hadoop_prop_choose_file_btn = "<fmt:message key='hadoop_prop_choose_file_btn'/>";

    alpine.nls.hadoop_prop_file_structure_tip_file_load_error = "<fmt:message key='hadoop_prop_file_structure_tip_file_load_error'/>";
    alpine.nls.hadoop_prop_choose_file_tip = "<fmt:message key='hadoop_prop_choose_file_tip'/>";
    alpine.nls.hadoop_prop_file_structure_tip_columnHeader_is_invalid = "<fmt:message key='hadoop_prop_file_structure_tip_columnHeader_is_invalid'/>";
    alpine.nls.hadoop_prop_file_structure_tip_columnHeader_repleat = "<fmt:message key='hadoop_prop_file_structure_tip_columnHeader_repleat'/>";
    alpine.nls.hadoop_prop_file_structure_need_config_tip = "<fmt:message key='hadoop_prop_file_structure_need_config_tip'/>";
    alpine.nls.hadoop_prop_file_structure_split_error_tip = "<fmt:message key='hadoop_prop_file_structure_split_error_tip'/>";
    alpine.nls.hadoop_prop_file_structure_split_error_inner_tip = "<fmt:message key='hadoop_prop_file_structure_split_error_inner_tip'/>";
    alpine.nls.hadoop_prop_file_structure_split_max_column_tip = "<fmt:message key='hadoop_prop_file_structure_split_max_column_tip'/>";
    alpine.nls.hadoop_prop_file_structure_escap_quote_char_tip = "<fmt:message key='hadoop_prop_file_structure_escap_quote_char_tip'/>";
    alpine.nls.hadoop_prop_file_structure_escap_quote_equal_tip = "<fmt:message key='hadoop_prop_file_structure_escap_quote_equal_tip'/>";
    alpine.nls.hadoop_prop_file_structure_xml_grid_column_xpath = "<fmt:message key='hadoop_prop_file_structure_xml_grid_column_xpath'/>";
    alpine.nls.hadoop_prop_file_structure_json_grid_column_xpath = "<fmt:message key='hadoop_prop_file_structure_json_grid_column_xpath'/>";
    alpine.nls.hadoop_prop_file_structure_xml_root_tip = "<fmt:message key='hadoop_prop_file_structure_xml_root_tip'/>";
    alpine.nls.hadoop_prop_file_structure_xml_container_tip = "<fmt:message key='hadoop_prop_file_structure_xml_container_tip'/>";
    alpine.nls.hadoop_prop_file_structure_xml_grid_column_tip = "<fmt:message key='hadoop_prop_file_structure_xml_grid_column_tip'/>";
    alpine.nls.hadoop_prop_file_structure_xml_column_exist_tip = "<fmt:message key='hadoop_prop_file_structure_xml_column_exist_tip'/>";
    alpine.nls.hadoop_prop_file_structure_xml_column_repeat_tip = "<fmt:message key='hadoop_prop_file_structure_xml_column_repeat_tip'/>";
    alpine.nls.hadoop_prop_file_structure_xml_column_origin_tip = "<fmt:message key='hadoop_prop_file_structure_xml_column_origin_tip'/>";
    alpine.nls.hadoop_prop_file_structure_xml_node_select_tip = "<fmt:message key='hadoop_prop_file_structure_xml_node_select_tip'/>";
    alpine.nls.hadoop_prop_file_structure_xml_load_structure_error_tip = "<fmt:message key='hadoop_prop_file_structure_xml_load_structure_error_tip'/>";
    alpine.nls.hadoop_prop_file_structure_column_name_invalid_tip = "<fmt:message key='hadoop_prop_file_structure_column_name_invalid_tip'/>";
    alpine.nls.hadoop_prop_file_structure_column_container_value_invalid_tip = "<fmt:message key='hadoop_prop_file_structure_column_container_value_invalid_tip'/>";
    alpine.nls.hadoop_prop_file_structure_column_xpath_value_invalid_tip = "<fmt:message key='hadoop_prop_file_structure_column_xpath_value_invalid_tip'/>";
    alpine.nls.hadoop_prop_file_structure_column_4json_empty_tip = "<fmt:message key='hadoop_prop_file_structure_column_4json_empty_tip'/>";
    alpine.nls.hadoop_prop_file_structure_column_4json_define_tip = "<fmt:message key='hadoop_prop_file_structure_column_4json_define_tip'/>";
    alpine.nls.hadoop_prop_file_structure_tree_click_4json_tip = "<fmt:message key='hadoop_prop_file_structure_tree_click_4json_tip'/>";
    alpine.nls.hadoop_prop_file_structure_column_name_keyword_tip = "<fmt:message key='hadoop_prop_file_structure_column_name_keyword_tip'/>";
    alpine.nls.hadoop_prop_file_structure_json_select_level_tip = "<fmt:message key='hadoop_prop_file_structure_json_select_level_tip'/>";
    alpine.nls.hadoop_prop_file_structure_log_file_preview_error_tip = "<fmt:message key='hadoop_prop_file_structure_log_file_preview_error_tip'/>";
    alpine.nls.hadoop_prop_file_xml_json_guess_node_tip = "<fmt:message key='hadoop_prop_file_xml_json_guess_node_tip'/>";

    alpine.nls.hadoop_props_file_explorer_message_no_select_file = "<fmt:message key='hadoop_props_file_explorer_message_no_select_file'/>";
    alpine.nls.hadoop_props_file_explorer_message_no_select_folder = "<fmt:message key='hadoop_props_file_explorer_message_no_select_folder'/>";
    alpine.nls.hadoop_props_file_explorer_message_select_too_much = "<fmt:message key='hadoop_props_file_explorer_message_select_too_much'/>";
    alpine.nls.hadoop_props_file_explorer_message_select_file = "<fmt:message key='hadoop_props_file_explorer_message_select_file'/>";
    alpine.nls.hadoop_prop_file_structure_tip_Other = "<fmt:message key='hadoop_prop_file_structure_tip_Other'/>";
    alpine.nls.hadoop_aggregate_define_agg_dlg_title = "<fmt:message key='hadoop_aggregate_define_agg_dlg_title'/>";
    alpine.nls.hadoop_aggregate_method_grid_column1 = "<fmt:message key='hadoop_aggregate_method_grid_column1'/>";
    alpine.nls.hadoop_aggregate_method_grid_column2 = "<fmt:message key='hadoop_aggregate_method_grid_column2'/>";
    alpine.nls.hadoop_aggregate_method_grid_column2_repeat = "<fmt:message key='hadoop_aggregate_method_grid_column2_repeat'/>";

    alpine.nls.hadoop_join_condition_grid_column_hadoop_file = "<fmt:message key='hadoop_join_condition_grid_column_hadoop_file'/>";
    alpine.nls.hadoop_join_condition_grid_column_key_column = "<fmt:message key='hadoop_join_condition_grid_column_key_column'/>";
    alpine.nls.hadoop_select_grid_column_availible_column = "<fmt:message key='hadoop_select_grid_column_availible_column'/>";
    alpine.nls.hadoop_select_grid_column_alias = "<fmt:message key='hadoop_select_grid_column_alias'/>";
    alpine.nls.hadoop_join_type_select_tip = "<fmt:message key='hadoop_join_type_select_tip'/>";
    alpine.nls.hadoop_join_select_grid_tip = "<fmt:message key='hadoop_join_select_grid_tip'/>";
    alpine.nls.hadoop_join_condition_tip = "<fmt:message key='hadoop_join_condition_tip'/>";
    alpine.nls.hadoop_join_table_num_error = "<fmt:message key='hadoop_join_table_num_error'/>";
    alpine.nls.hadoop_define_join_condition_btn = "<fmt:message key='hadoop_define_join_condition_btn'/>";
    alpine.nls.hadoop_join_select_column_alias_empty_tip = "<fmt:message key='hadoop_join_select_column_alias_empty_tip'/>";
    alpine.nls.hadoop_join_condition_column_type_error_tip = "<fmt:message key='hadoop_join_condition_column_type_error_tip'/>";
    alpine.nls.hadoop_join_condition_key_column_same_error_tip = "<fmt:message key='hadoop_join_condition_key_column_same_error_tip'/>";
    alpine.nls.hadoop_join_condition_key_column_start_number_error_tip = "<fmt:message key='hadoop_join_condition_key_column_start_number_error_tip'/>";
    alpine.nls.hadoop_join_option = "<fmt:message key='hadoop_join_option'/>";
    alpine.nls.hadoop_join_option_fillbothtables = "<fmt:message key='hadoop_join_option_fillbothtables'/>";
    alpine.nls.hadoop_join_option_with_prev_join_left = "<fmt:message key='hadoop_join_option_with_prev_join_left'/>";
    alpine.nls.hadoop_join_option_with_prev_join_right = "<fmt:message key='hadoop_join_option_with_prev_join_right'/>";
    alpine.nls.hadoop_join_need_alias = "<fmt:message key='hadoop_join_need_alias'/>";

    alpine.nls.hadoop_null_value_replace_data_type_error = "<fmt:message key='hadoop_null_value_replace_data_type_error'/>";
    
    alpine.nls.hadoop_copytohadoop_connName = "<fmt:message key='hadoop_copytohadoop_connName'/>";
    alpine.nls.hadoop_copytohadoop_resultsLocation = "<fmt:message key='hadoop_copytohadoop_resultsLocation'/>";

    alpine.nls.hadoop_pig_execute_label = "<fmt:message key='hadoop_pig_execute_label'/>";
    alpine.nls.hadoop_pig_execute_pigScript = "<fmt:message key='hadoop_pig_execute_pigScript'/>";
    alpine.nls.hadoop_pig_execute_pigScript_dialog_title = "<fmt:message key='hadoop_pig_execute_pigScript_dialog_title'/>";
    alpine.nls.hadoop_pig_execute_pigExecuteFileStructure = "<fmt:message key='hadoop_pig_execute_pigExecuteFileStructure'/>";
    alpine.nls.hadoop_pig_execute_pigscript_invalid_tip = "<fmt:message key='hadoop_pig_execute_pigscript_invalid_tip'/>";
    alpine.nls.hadoop_pigexecute_grid_empty_tip = "<fmt:message key='hadoop_pigexecute_grid_empty_tip'/>";
    alpine.nls.hadoop_pigexecute_grid_column_same_tip = "<fmt:message key='hadoop_pigexecute_grid_column_same_tip'/>";
    alpine.nls.hadoop_pigexecute_choose_file_tip = "<fmt:message key='hadoop_pigexecute_choose_file_tip'/>";
    alpine.nls.pig_edit_scirpt_button = "<fmt:message key='pig_edit_scirpt_button'/>";
    alpine.nls.pig_define_struct_button = "<fmt:message key='pig_define_struct_button'/>";

    alpine.nls.download_result_groupby_tip = "<fmt:message key='download_result_groupby_tip'/>";
    alpine.nls.hadoop_data_mgr_download_config_wholedownload_tip = "<fmt:message key='hadoop_data_mgr_download_config_wholedownload_tip'/>";

    alpine.nls.datasource_config_msg_delete = "<fmt:message key='datasource_config_msg_delete'/>";

    alpine.nls.openselectedflow = "<fmt:message key='openselectedflow'/>";
    alpine.nls.open_properties_dialog = "<fmt:message key='open_operator_properties'/>";

    alpine.nls.mobile_msg_open_flow = "<fmt:message key='mobile_msg_open_flow'/>";

    alpine.nls.sample_size_config_grid_column_1 = "<fmt:message key='sample_size_config_grid_column_1'/>";
    alpine.nls.sample_size_config_grid_column_percent = "<fmt:message key='sample_size_config_grid_column_percent'/>";
    alpine.nls.sample_size_config_grid_column_rownumber = "<fmt:message key='sample_size_config_grid_column_rownumber'/>";
    alpine.nls.sample_size_config_error_tip_size_null = "<fmt:message key='sample_size_config_error_tip_size_null'/>";
    alpine.nls.sample_size_config_error_tip_percent_error_tip1 = "<fmt:message key='sample_size_config_error_tip_percent_error_tip1'/>";
    alpine.nls.sample_size_config_error_tip_percent_error_tip2 = "<fmt:message key='sample_size_config_error_tip_percent_error_tip2'/>";
    alpine.nls.sample_size_btn4_define = "<fmt:message key='sample_size_btn4_define'/>";
    alpine.nls.sample_size_model_need_config = "<fmt:message key='sample_size_model_need_config'/>";

    alpine.nls.sqlExpression_title_sql = "<fmt:message key='var_sql_expression'/>";
    alpine.nls.sqlExpression_title_java = "<fmt:message key='var_java_expression'/>";

    alpine.nls.outputtitlebar_delete = "<fmt:message key='outputtitlebar_delete'/>";
    alpine.nls.outputtitlebar_edit_operator = "<fmt:message key='outputtitlebar_edit_operator'/>";
    alpine.nls.outputtitlebar_explore = "<fmt:message key='outputtitlebar_explore'/>";
    alpine.nls.outputtitlebar_rename = "<fmt:message key='outputtitlebar_rename'/>";

    alpine.nls.copy_paste_menu_copy = "<fmt:message key='copy_paste_menu_copy'/>";
    alpine.nls.copy_paste_menu_paste = "<fmt:message key='copy_paste_menu_paste'/>";
    
	alpine.nls.import_data_pane_upload_button_UPLOADING = "<fmt:message key='import_data_pane_upload_button_UPLOADING'/>";
    alpine.nls.import_data_pane_upload_button_next = "<fmt:message key='import_data_pane_upload_button_next'/>";
    alpine.nls.import_data_pane_format_label_show_additional_option = "<fmt:message key='import_data_pane_format_label_show_additional_option'/>";
    alpine.nls.import_data_pane_format_label_hide_additional_option = "<fmt:message key='import_data_pane_format_label_hide_additional_option'/>";
    alpine.nls.import_data_pane_format_label_include = "<fmt:message key='import_data_pane_format_label_include'/>";
    alpine.nls.import_data_pane_format_label_allow_empty = "<fmt:message key='import_data_pane_format_label_allow_empty'/>";
    alpine.nls.import_data_message_error = "<fmt:message key='import_data_message_error'/>";
    alpine.nls.import_data_message_failure = "<fmt:message key='import_data_message_failure'/>";
    alpine.nls.import_data_message_error100 = "<fmt:message key='import_data_message_error100'/>";
    alpine.nls.import_data_message_success = "<fmt:message key='import_data_message_success'/>";
    alpine.nls.import_data_error_grid_header_rownum = "<fmt:message key='import_data_error_grid_header_rownum'/>";
    alpine.nls.import_data_error_grid_header_message = "<fmt:message key='import_data_error_grid_header_message'/>";
    alpine.nls.import_data_message_abort = "<fmt:message key='import_data_message_abort'/>";
    alpine.nls.import_data_message_starting = "<fmt:message key='import_data_message_starting'/>";

    alpine.nls.export_exec_flow_tip = "<fmt:message key='export_exec_flow_tip'/>";
    alpine.nls.export_exec_flow_name_same_tip = "<fmt:message key='export_exec_flow_name_same_tip'/>";
    alpine.nls.export_flow_tip = "<fmt:message key='export_flow_tip'/>";

    alpine.nls.hadoop_enabled = "<fmt:message key='hadoop_enabled'/>";
    alpine.nls.workbench_op_category_all = "<fmt:message key='workbench_op_category_all'/>";
    alpine.nls.workbench_op_category_extraction = "<fmt:message key="workbench_op_category_extraction"/>";
    alpine.nls.workbench_op_category_exploration = "<fmt:message key='workbench_op_category_exploration'/>";
    alpine.nls.workbench_op_category_transformation = "<fmt:message key='workbench_op_category_transformation'/>";
    alpine.nls.workbench_op_category_sampling = "<fmt:message key='workbench_op_category_sampling'/>";
    alpine.nls.workbench_op_category_modeling = "<fmt:message key='workbench_op_category_modeling'/>";
    alpine.nls.workbench_op_category_scoring = "<fmt:message key='workbench_op_category_scoring'/>";
    alpine.nls.workbench_op_category_other = "<fmt:message key='workbench_op_category_other'/>";
    
    alpine.nls.step_run_single_operator_error_message = "<fmt:message key='step_run_single_operator_error_message'/>";
    
    alpine.nls.hadoop_data_mgr_create_folder_title = "<fmt:message key='hadoop_data_mgr_create_folder_title'/>";
    alpine.nls.hadoop_data_mgr_confirm_delete_directory = "<fmt:message key='hadoop_data_mgr_confirm_delete_directory'/>";
    alpine.nls.hadoop_data_mgr_confirm_delete_file = "<fmt:message key='hadoop_data_mgr_confirm_delete_file'/>";
    
    alpine.nls.random_forest_operator_tip_no_column_selected = "<fmt:message key='random_forest_operator_tip_no_column_selected'/>";
    alpine.nls.random_forest_operator_tip_validate_nodeColumnNumber_fail = "<fmt:message key='random_forest_operator_tip_validate_nodeColumnNumber_fail'/>";

    alpine.nls.row_filter_title =  "<fmt:message key='where_clause_edit'/>";
    alpine.nls.sql_editor_title = "<fmt:message key='sql_editor_title'/>";

    alpine.nls.flowdraft_message_opendraft = "<fmt:message key='flowdraft_message_opendraft'/>";
    alpine.nls.flowdraft_button_reevert_label = "<fmt:message key='flowdraft_button_reevert_label'/>";
    alpine.nls.flowdraft_button_opendraft_label = "<fmt:message key='flowdraft_button_opendraft_label'/>";
    
    alpine.nls.sql_execute_prop_button = "<fmt:message key='sql_execute_prop_button'/>";
    
    alpine.nls.import_data_hd_validate_file_name = "<fmt:message key='import_data_hd_validate_file_name'/>";

    alpine.nls.delete_open_flow_prompt = "<fmt:message key='delete_open_flow_prompt'/>";
    alpine.nls.action_delete_open_flow = "<fmt:message key='action_delete_open_flow'/>";

    alpine.nls.system_update_tip_button_ok = "<fmt:message key='system_update_tip_button_ok'/>";
    alpine.nls.system_update_tip_button_cancel = "<fmt:message key='system_update_tip_button_cancel'/>";
    alpine.nls.system_update_tip_info = "<fmt:message key='system_update_tip_info'/>";
    alpine.nls.system_update_select_item_tip = "<fmt:message key='system_update_select_item_tip'/>";
    alpine.nls.system_update_fail_tip_alert = "<fmt:message key='system_update_fail_tip_alert'/>";
    alpine.nls.system_update_tip_none_update = "<fmt:message key='system_update_tip_none_update'/>";


    if(dojo.isAndroid || dojo.isIos){
        var openFlowButton =  new dijit.form.Button({
            id: "button_open_flow",
            baseClass: "workflowPanelButton",
            onClick: openSelectedFlow,
            label: alpine.nls.openselectedflow
        });

        dojo.byId("open_button_placeholder").appendChild(openFlowButton.domNode);

    }
});




var invalidateResourceNames = ['\\', '/', ':', '*', '?', '"', '<', '>', '|','/', '\0'];

var baseURL = "<%=baseURL%>";
var progressImage = "<%=progressImage%>" ;
var iconpath = baseURL + "/images/icons/";
var flowBaseURL = baseURL + "/main/flow.do";
var preferenceBaseURL = baseURL + "/main/preference.do";
var connBaseURL = baseURL + "/main/dbconnection.do";
var udfBaseURL = baseURL + "/main/udf.do";
var loginURL = alpine.WEB_APP_NAME + "/index.jsp";
var logoutURL = alpine.WEB_APP_NAME + "/alpine/login/weblogout.jsp";

var ds = new httpService();
var login = null;
var error_msg = null;
var y_offset = 40;
var x_offset = 80;
var y_link_off = 0;
var current_flow_id;


function openSelectedFlow()
{
    console.log("need to open flow _pre");
    console.log(alpine.flow.FlowCategoryUIHelper);

    alpine.flow.FlowCategoryUIHelper.openSelectedFlow();
}


function createCategoryFromButton()
{
    alpine.flow.FlowCategoryUIHelper.createCategory();
}
function showHelpWindow(){
    var operatorPrimaryInfo = alpine.flow.OperatorManagementUIHelper.getSelectedOperator(true);
    if(operatorPrimaryInfo){
        alpine.onlinehelp.OnlineHelp.showOperatorHelp(baseURL, operatorPrimaryInfo.classname);
    } else {
        alpine.onlinehelp.OnlineHelp.showGeneralHelp();
    }
}


//dojo.ready(function(){
//    if(dojo.isIE){
//        //avoid F1 default function
//        window.onhelp = function(event){
//            define(["alpine/flow/onlinehelp"], function (OnlineHelp) {
//                OnlineHelp.showGeneralHelp();
//            });
//            return false;
//        };
//    }else{
//        dojo.connect(window,"keydown",window,loginF1KeyDownHandler);
//        //FF&&
//    }
//
//});


//function loginF1KeyDownHandler(eventObj){
//    switch (eventObj.keyCode) {
//        case dojo.keys.F1:
//            //default
//            define(["alpine/flow/onlinehelp"], function (OnlineHelp) {
//                OnlineHelp.showGeneralHelp();
//            });
//
//            break;
//
//        default:
//            break;
//    }
//}
</script>

<head>

    <title><fmt:message key='alpine_illuminator_entry_page_title'/></title>


    <style type="text/css">
        #propertyFormDialog{
            border:1px solid #b7b7b7;
            top: 50px;
            width: 1px;
            height: 500px;
            position:absolute;
            right:0;
            background: #ffffff;
        }

        #propertyFormDialogCont {
            width: 100%;
            height: 90%;
            border: 1px;
            position: relative;
        }

        #whereClauseBorderContainer {
            width: 100%;
            height: 90%;
            border: 0px;
        }

        #PublicGroupFlowContainer {
            width: 100%;
            height: 93%;
            border: 0px;
        }
            /*
              For sub-flow begin
            */
        .tablemapping-row{
            margin: 3px 0;
            padding: 0;
            height: 20px;
        }
        .tablemapping-row-title{
            margin: 0;
            padding: 0px 0px 0px 5px;
            text-align: center;
            width: 160px;
            overflow: hidden;
            float: left;
        }
        .tablemapping-row-map-to{
            margin: 0;
            padding: 0;
            text-align:center;
            width: 170px;
            overflow: hidden;
            float: left;
        }
        .tablemapping-row-subflow-table{
            margin: 0;
            padding: 0 0 0 10px;
            text-align:left;
            width: 160px;
            overflow: hidden;
            float: left;
        }
        .tablemapping-row-subflow-table-list{
        }
            /*for sub-flow end*/
            /*for hadoop agg button*/
    </style>
    <script type="text/javascript">

        dojo.ready(function() {

            login = get_user();

            // disable_all();
            getGroupList();
            //enable_flow_buttons(false);
            //setDirty(false);
            // TODO resize_buttons();
            //dijit.byId("cancel_flow_button").set("disabled", true);
        });
        dojo.require("alpine.system.security_config");
        dojo.require("alpine.onlinehelp.OnlineHelp");
        dojo.require("alpine.flow.WelcomeHelper");
        dojo.require("alpine.flow.FlowCategoryUIHelper");
        dojo.require("alpine.flow.HadoopResourceFetcher");
        dojo.require("alpine.props.HadoopCommonPropertyHelper");
        dojo.require("alpine.flow.HadoopOperatorsDataExplorerHelper");
        dojo.require("alpine.props.HadoopDataTypeUtil");
        dojo.require("alpine.layout.WorkBenchNavigation");
        dojo.require("alpine.dataexplorer.DataExplorerHelper");
        dojo.require("alpine.datasourceexplorer.DataSourceExplorerUIHelper");
        dojo.require("alpine.datasourceexplorer.HadoopExplorerUIHelper");
        dojo.require("alpine.datasourceexplorer.DataSourceExplorerManager");
        dojo.require("alpine.operatorexplorer.OperatorExplorerUIHelper");
        dojo.require("alpine.layout.BreadcrumbNavigation");
        dojo.require("alpine.layout.ClearableTextBox");
        dojo.require("alpine.flow.WorkFlowPainter");
        dojo.require("alpine.flow.WorkFlowUIHelper");
        dojo.require("alpine.flow.WorkFlowManager");
        dojo.require("alpine.flow.OperatorManagementUIHelper");
        dojo.require("alpine.flow.OperatorManagementManager");
        dojo.require("alpine.flow.OperatorLinkUIHelper");
        dojo.require("alpine.flow.OperatorLinkManager");
        dojo.require("alpine.flow.WorkFlowEditToolbarHelper");
        dojo.require("alpine.flow.CopyManager");
        dojo.require("alpine.flow.OperatorMenuHelper");
        dojo.require("alpine.flow.FlowVersionHistoryUIHelper");
        dojo.require("alpine.flow.DeleteFlowUIHelper");
        dojo.require("alpine.flow.WorkFlowRunnerUIHelper");
        dojo.require("alpine.flow.WorkFlowVariableReplacer");
        dojo.require("alpine.layout.SelectedOperatorMenuDropDown");
        //----------Below are just support orignal flowDisply.js-----------------
        //Remove them if change orignal js to module pattern
        dojo.require("alpine.flow.RecentlyHistoryFlowManager");
        dojo.require("alpine.operatorexplorer.OperatorUtil");//remove it if make flowdisplay to module.
        dojo.require("alpine.dbconnection.dbMetadataCacheManager");

        //----------Below are just support dataexplorer.js-----------------
        dojo.require("alpine.layout.ColumnSelect.ColumnSelect");

        //--variable define for variable operator--//
        dojo.require("alpine.props.VariablesDefine4VarOPHelper");
    </script>

    <script type="text/javascript"
            src="../../js/alpine/SideBar.js" charset="utf-8">
    </script>

    <script type="text/javascript"
            src="../../js/alpine/flow/flowdisplay.js" charset="utf-8">
    </script>

    <script type="text/javascript"
            src="../../js/alpine/flow/flowcontrol.js" charset="utf-8">
    </script>

    <script type="text/javascript"
            src="../../js/alpine/props/property.js" charset="utf-8">
    </script>

    <script type="text/javascript"
            src="../../js/alpine/resize.js" charset="utf-8">
    </script>

    <script type="text/javascript"
            src="../../js/alpine/props/plda.js" charset="utf-8">
    </script>

    <script type="text/javascript"
            src="../../js/alpine/props/subflow.js" charset="utf-8">
    </script>
    <script type="text/javascript"
            src="../../js/alpine/system/licenseInfo.js" charset="utf-8">
    </script>
    <script type="text/javascript" src="../../js/alpine/visual/datagrid.js"		charset="utf-8"></script>
</head>

<%@ include file="/alpine/flow/history.jsp"%>

<%@ include file="/alpine/flow/aggregate.jsp"%>
<%@ include file="/alpine/flow/hiddenlayer.jsp"%>
<%@ include file="/alpine/flow/dataexplorer.jsp"%>
<%@ include file="/alpine/flow/var_derived.jsp"%>
<%@ include file="/alpine/flow/interactioncolumns.jsp"%>
<%@ include file="/alpine/flow/var_quantile.jsp"%>
<%@ include file="/alpine/flow/myflowResult.jsp"%>
<%@ include file="/alpine/flow/modelReplace.jsp"%>
<%@ include file="/alpine/flow/tableJoinNew.jsp"%>
<%@ include file="/alpine/flow/cohorts.jsp"%>
<%@ include file="/alpine/flow/tableColumn.jsp"%>
<%@ include file="/alpine/flow/adaboost.jsp"%>
<%@ include file="/alpine/flow/clearIntermediateTable.jsp"%>

<%@ include file="/alpine/admin/group_user.jsp"%>
<%@ include file="/alpine/admin/user_profile.jsp"%>
<%@ include file="/alpine/admin/user_preference.jsp"%>
<%@ include file="/alpine/admin/udf_manager.jsp"%>
<%@ include file="/alpine/admin/mail_server_configuration.jsp"%>
<%@ include file="/alpine/admin/security_config.jsp"%>
<%@ include file="/alpine/chorus/chorusConfig.jsp" %>

<%@ include file="/alpine/flow/new_flow.jsp"%>
<%@ include file="/alpine/flow/import_flow.jsp"%>
<%@ include file="/alpine/flow/export_flow.jsp"%>
<%@ include file="/alpine/chorus/publishToChorus.jsp"%>

<%@ include file="/alpine/flow/textfield_dialog.jsp"%>

<%@ include file="/alpine/flow/publicGroupFlowDialog.jsp"%>

<%@ include file="/alpine/flow/recentlyHistory_flow.jsp"%>
<%@ include file="/alpine/flow/share_flow.jsp"%>
<%@ include file="/alpine/flow/duplicate_flow.jsp"%>
<%@ include file="/alpine/flow/schedulerManager.jsp"%>
<%@ include file="/alpine/audit/auditManager.jsp"%>
<%@ include file="/alpine/flow/woeOperator.jsp" %>
<%@ include file="/alpine/flow/histogramDialog.jsp" %>
<%--<%@ include file="/alpine/flow/tableSet.jsp" %>--%>
<%@ include file="/alpine/flow/setOperatorDialog.jsp" %>
<%@ include file="/alpine/flow/storageParameters.jsp" %>
<%@ include file="/alpine/dbconnection/dbMetadataCacheManager.jsp" %>
<%@ include file="/alpine/flow/flowVariable.jsp" %>
<%@ include file="/alpine/flow/findandreplaceparameter.jsp" %>
<%@ include file="/alpine/flow/subflow.jsp" %>
<%@ include file="/alpine/admin/cleanupSessionManager.jsp" %>

<%@ include file="/alpine/flow/hadoopFileOperatorPropertysView.jsp" %>
<%@ include file="/alpine/flow/hadoopPigExecuteFileStructure.jsp" %>
<%@ include file="/alpine/datasourcemgr/dataSourceManager.jsp" %>
<%@ include file="/alpine/flow/hadoopFileExplorerProperty.jsp" %>
<%@ include file="/alpine/flow/hadoopAggregateOperatorDefineAgg.jsp" %>
<%@ include file="/alpine/flow/sampleSizeModelConfig.jsp" %>
<%@ include file="/alpine/flow/hadoopJoin.jsp" %>
<%--<%@ include file="/alpine/flow/hadoopTableSet.jsp" %>--%>
<%@ include file="/alpine/flow/rowFilter.jsp" %>
<%@ include file="/alpine/import/importData.jsp" %>
<%@ include file="/alpine/flow/columnNamePropertySelect.jsp" %>
<%@ include file="/alpine/flow/nullValueReplacePropertyConfig.jsp" %>
<%@ include file="/alpine/flow/nullValueReplacementDialog.jsp" %>
<%@ include file="/alpine/datasourceexplorer/datasourceExplorer.jsp" %>
<%@ include file="/alpine/flow/pigExecuteScript.jsp" %>
<%@ include file="/alpine/admin/SystemUpdata.jsp" %>


<body class="soria" onresize="autoResize()" id="bodyRootContainer"><!-- this is for css control -->
<div dojoType="dijit.layout.LayoutContainer" style="width:100%; height: 100%;" id="rootContainer">
<div dojoType="dijit.layout.ContentPane" region="top">
    <table id="banner" width="100%" >
        <tr>
            <td>
                <img style="padding: 5px;"
                     src="../../images/interface/banner_logo.png" />
            </td>
            <td align="right">
                <div class="userPanelButtonPanel">
                <div dojoType="dijit.form.DropDownButton" id="settings_button" baseClass="userPanelDropdownButton">
                    <span><fmt:message key='helloUser' /></span>
                    <div dojoType="dijit.Menu" baseClass="userPanelDropdownButton">
                        <div dojoType="dijit.MenuItem" id="datasourceConnections_button">
                            <fmt:message key='datasource_config_menu_title' />
                        </div>
                        <div dojoType="dijit.MenuItem" id="scheduler_button">
                            <fmt:message key='scheduler_configuration_button_title' />
                        </div>
                        <div dojoType="dijit.MenuSeparator"></div>

                        <alpine:permissionChecker permission="UPDATE_PREFERENCE,
					                                        				SECURITY_UPDATE,
					                                        				USER_MANAGEMENT_EDIT,
					                                        				GROUP_MANAGEMENT_EDIT,
					                                        				MAIL_SERVER_CONFIG_UPDATE,
					                                        				UDF_MANAGEMENT_EDIT,
					                                        				SESSION_MANAGEMENT_KILL">
                            <alpine:permissionChecker permission="UPDATE_PREFERENCE">
                                <div dojoType="dijit.MenuItem" id="prefrenece_button">
                                    <fmt:message key='prefrenece' />
                                </div>
                            </alpine:permissionChecker>
                            <alpine:permissionChecker permission="SECURITY_UPDATE">
                                <div dojoType="dijit.MenuItem" id="security_button" onClick="alpine.system.security_config.showSecurityDialog();">
                                    <fmt:message key='security' />
                                </div>
                            </alpine:permissionChecker>
                            <alpine:permissionChecker permission="USER_MANAGEMENT_EDIT,GROUP_MANAGEMENT_EDIT">
                                <div dojoType="dijit.MenuItem" id="groupusers_button" onClick="alpine.system.UserGroupManagementUIHelper.showGroupUserDialog();">
                                    <fmt:message key='groupusers' />
                                </div>
                            </alpine:permissionChecker>
                            <alpine:permissionChecker permission="MAIL_SERVER_CONFIG_UPDATE">
                                <div dojoType="dijit.MenuItem" id="btn_edit_mail_config">
                                    <fmt:message key='Mail_Configuration' />
                                </div>
                            </alpine:permissionChecker>
                            <alpine:permissionChecker permission="UDF_MANAGEMENT_EDIT">
                                <div dojoType="dijit.MenuItem" id="udf_button" onClick="show_udf_dialog();">
                                    <fmt:message key='UDF' />
                                </div>
                            </alpine:permissionChecker>
                            <alpine:permissionChecker permission="SESSION_MANAGEMENT_KILL">
                                <div dojoType="dijit.MenuItem" id="alpine_system_sessionMgr_menu">
                                    <fmt:message key='session_manager_title' />
                                </div>
                            </alpine:permissionChecker>
                            <div dojoType="dijit.MenuItem" id="chorusConfigurationMenuItem">Chorus Configuration</div>
                            <div dojoType="dijit.MenuSeparator"></div>
                        </alpine:permissionChecker>
                        <div dojoType="dijit.MenuItem" id="reset_passworod_button">
                            <fmt:message key='restpassword' />
                        </div>
                        <div dojoType="dijit.MenuItem" id="user_logs_button">
                            <fmt:message key='audit_button_entrance' />
                        </div>
                        <div dojoType="dijit.MenuSeparator"></div>
                        <alpine:permissionChecker permission="UPDATE_SYSTEM">
                            <div dojoType="dijit.MenuItem" id="alpine_system_systemupdate_menu">
                                <fmt:message key="system_updata_menu_label" />
                            </div>
                        <div dojoType="dijit.MenuSeparator"></div>
                        </alpine:permissionChecker>
                        <div dojoType="dijit.MenuItem" id="alpine_system_licenseInfo_menu">
                            <fmt:message key='license_displayer_about' />
                        </div>
                    </div>
                </div>
                <button id="logout_button" data-dojo-type="dijit.form.Button" baseClass="userPanelButton" type="button">
                    <fmt:message key='signout' />
                </button>
                <button id="general_help_button" data-dojo-type="dijit.form.Button" baseClass="userPanelButton" type="button">
                    <fmt:message key='outputtitlebar_help'/>
                </button>
                </div>
            </td>
        </tr>
    </table>
</div>

<div dojoType="dijit.layout.ContentPane" region="center">
    <div dojoType="dijit.layout.LayoutContainer">
        <div dojoType="dijit.layout.ContentPane" region="top" class="subheader">
            <div class="subheaderButtonPanel">
                <img src="../../images/interface/subheaderSpacer.png" style="padding-bottom:11px"
                        /><button dojoType="dijit.form.Button" baseClass="subheaderButton" id="run_flow" type="button">
                <fmt:message key='run_flow_tip'/>
            </button><img src="../../images/interface/subheaderSpacer.png" style="padding-bottom:11px"
                    /><button dojoType="dijit.form.Button" baseClass="subheaderButton" id="stop_flow" type="button">
                <fmt:message key='stop_flow_tip'/>
            </button><img src="../../images/interface/subheaderSpacer.png" style="padding-bottom:11px"
                    /><button dojoType="dijit.form.Button" baseClass="subheaderButton" id="save_flow_button" type="button">
                <fmt:message key='Subheader_Save'/>
            </button><img src="../../images/interface/subheaderSpacer.png" style="padding-bottom:11px"
                    /><button dojoType="dijit.form.Button" baseClass="subheaderButton" id="revert_flow_button" type="button">
                <fmt:message key='Subheader_Revert'/>
            </button><img src="../../images/interface/subheaderSpacer.png" style="padding-bottom:11px"
                    /><button dojoType="dijit.form.Button"  baseClass="subheaderButton" id="cancel_flow_button"  type="button"><fmt:message key='Subheader_Cancel'/>
            </button><img src="../../images/interface/subheaderSpacer.png" style="padding-bottom:11px"
                    /><div dojoType="dijit.form.DropDownButton" id="action_button" baseClass="subheaderDropdownButton">
                <span><fmt:message key='Subheader_Actions'/> </span>
                <div dojoType="dijit.Menu" id="actionMenu" style="width: 100%; font-size: 13px;" baseClass="subMenuDropDown">
                    <div dojoType="dijit.MenuItem" id="find_replace_parameter_value_btn">
                        <fmt:message key='find_replace_parameter_value_btn' />
                    </div>
                    <div dojoType="dijit.MenuItem" id="my_result_button">
                        <fmt:message key='My_Flow_Results' />
                    </div>
                    <div dojoType="dijit.MenuItem" id="refresh_metadata_btn" onclick="alpine.dbconnection.dbMetadataCacheManager.startup()">
                        <fmt:message key='dbresource_menu_name' />
                    </div>
                    <div dojoType="dijit.MenuSeparator"></div>
                    <div dojoType="dijit.MenuItem" id="btn_show_flow_history">
                        <fmt:message key='Flow_History' />
                    </div>
                    <div dojoType="dijit.MenuItem" id="rename_flow_action_button">
                        <fmt:message key='flow_category_menu_rename_flow' />
                    </div>
                    <div dojoType="dijit.MenuItem" id="btn_tree_duplicate_flow">
                        <fmt:message key='duplicateflow_dialog_title' />
                    </div>
                    <div dojoType="dijit.MenuItem" id="export_flow_button" onClick="open_export_flow_dlg()">
                        <fmt:message key='export_flow_tip' />
                    </div>
                    <div dojoType="dijit.MenuItem" id="export_exec_flow_button" onClick="open_export_exe_flow_dlg()">
                        <fmt:message key='export_exec_flow_tip' />
                    </div>
                    <div dojoType="dijit.MenuItem" id="share_flow_button">
                        <fmt:message key='share_flow_tip' />
                    </div>
                    <div dojoType="dijit.MenuItem" id="publish_chorus_button">Publish to Chorus</div>
                    <div dojoType="dijit.MenuItem" id="delete_flow_action_button">
                        <fmt:message key='action_delete_open_flow' />
                    </div>
                    <div dojoType="dijit.MenuItem" id="flowVariable_setting_button">
                        <fmt:message key='flowvariable_menu_title' />
                    </div>
                    <div dojoType="dijit.MenuItem" id="clear_temp_table" onclick="alpine.flow.ClearIntermediateTableUIHelper.showClearTableDialog(null)">
                        <fmt:message key='clear_output_table' />
                    </div>
                    <div dojoType="dijit.MenuSeparator"></div>
                    <div dojoType="dijit.PopupMenuItem" id="recently_history_button">
                        <span><fmt:message key='flowHistory_button_title'/></span>
                        <div id="recently_history_menu" dojoType="dijit.Menu">
                            <!-- dynamic fill on shown -->
                        </div>
                    </div>
                </div>
            </div>
            </div>
            <div class="left">
                <img src= "../../images/interface/flowicon.png" style="padding-left:11px;padding-top:4px;padding-right:10px"/><span id="current_flow_label" class=" subheaderLabel" style="max-width:300px;"></span>
            </div>
        </div>
        <div dojoType="dijit.layout.ContentPane" region="bottom" class="outputTitleBar" width="100%" id="alpine_operator_toolbar_pane">
            <div id="current_operator_titlebar" class="left">
                <img id="current_operator_img" style="padding-left:5px;padding-top:4px;padding-right:0px;margin-bottom:1px"/>
                <label id="current_operator_label" class="outputTitleBarLabel"></label>
                <button dojoType="dijit.form.Button" baseClass="outputTitleBarHelpButton" id="btn_operator_help" type="button" onClick="showHelpWindow()">
                    <fmt:message key='outputtitlebar_help'/>
                </button>
            </div>
            <div class="outputTitleBarButtonPanel">
                <button dojoType="dijit.form.Button" baseClass="outputTitleBarButton" id="edit_operator" type="button" onClick="return open_operator_properties();">
                    <fmt:message key='outputtitlebar_edit_operator'/>
                </button>
                <alpine:permissionChecker permission="OPERATOR_EDIT">
                    <button dojoType="dijit.form.Button" baseClass="outputTitleBarButton" id="alpine_flow_operator_copy_btn" type="button" onClick="return alpine.flow.CopyManager.copy();" >
                        <fmt:message key='copy_paste_menu_copy'/>
                    </button>
                    <button dojoType="dijit.form.Button" baseClass="outputTitleBarButton" id="alpine_flow_operator_paste_btn" type="button" onClick="return alpine.flow.CopyManager.paste();" >
                        <fmt:message key='copy_paste_menu_paste'/>
                    </button>
                    <button dojoType="dijit.form.Button" baseClass="outputTitleBarButton" id="alpine_flow_operator_rename_btn" type="button" >
                        <fmt:message key='outputtitlebar_rename'/>
                    </button>
                </alpine:permissionChecker>
                <alpine:permissionChecker permission="OPERATOR_EDIT,CONNECT_EDIT">
	                <button dojoType="dijit.form.Button" baseClass="outputTitleBarButton" id="alpine_flow_operator_delete_btn" type="button">
	                    <fmt:message key='outputtitlebar_delete'/>
	                </button>
                </alpine:permissionChecker>
                <button dojoType="dijit.form.Button" baseClass="outputTitleBarButton" id="step_run_flow" type="button">
                    <fmt:message key='step_run_flow_tip'/>
                </button>
                <div dojoType="alpine.layout.SelectedOperatorMenuDropDown" id="operator_explore_button" baseClass="outputTitleBarDropdownButton">
                    <span><fmt:message key='outputtitlebar_explore'/> </span>
                    <div dojoType="dijit.Menu" id="operatorExplore" style="width: 100%; font-size: 13px;" baseClass="subMenuDropDown">
                    </div>
                </div>
            </div>
        </div>

        <div dojoType="alpine.layout.WorkBenchNavigation"
             workflowButtonLabel="<fmt:message key='navigation_tab_workflow_label'/>"
             dataSourceButtonLabel="<fmt:message key='navigation_tab_datasource_label'/>"
             operatorButtonLabel="<fmt:message key='navigation_tab_operator_label'/>"
             region="left" style="width:300px; border-right: 1px solid #999999; background-color:#E7E7E9" id="personalFlowTree">

            <div dojoType="dijit.layout.StackContainer" id="alpine_layout_navigation_tab_container">
                <div dojoType="dijit.layout.ContentPane" id="alpine_layout_navigation_workflow_pane" selected="true">
                    <div dojoType="dijit.layout.LayoutContainer" style="">
                        <div dojoType="dijit.layout.ContentPane" region="top">
                            <div class="workflowHeader">
                                <div class="workflowPanelButtonPanel">
                                    <span id="open_button_placeholder"></span>
                                    <alpine:permissionChecker permission="CREATE_FLOW">
                                        <button id="button_new_flow" baseClass="workflowPanelButton" dojoType=dijit.form.Button type="button" name="button_new_flow">
                                            <fmt:message key='createflow_button_title'/>
                                        </button>
                                    </alpine:permissionChecker>
                                    <button id="button_new_category" baseClass="workflowPanelButton" dojoType=dijit.form.Button type="button" name="button_open_public_group_flow" onclick='createCategoryFromButton()'>
                                        <fmt:message key='flow_category_menu_create'/>
                                    </button>
                                    <button id="button_open_public_group_flow"  baseClass="workflowPanelButton" dojoType=dijit.form.Button type="button" name="button_open_public_group_flow">
                                        <fmt:message key='addflows'/>
                                    </button>

                                    <button dojoType="dijit.form.Button"  baseClass="workflowPanelButton" type="button"
                                            id="import_flow_button2">
                                        <fmt:message key="import_flow_tip" />
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div dojoType="dijit.layout.ContentPane" region="center">
                            <div dojoType="dijit.layout.BorderContainer"
                                 style="width: 300px; height: 100%;">
                                <div id="flow_category_tree_outerContainer" dojoType="dijit.layout.ContentPane" region="center">
                                    <div id="flow_category_tree_container" style="height: 100%"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div dojoType="dijit.layout.ContentPane" id="alpine_layout_navigation_datasource_pane">
                    <div dojoType="dijit.layout.LayoutContainer" style="background-color: transparent">
                        <div dojoType="dijit.layout.ContentPane" region="top">
                            <div class="workbenchHeader">
                                <div id="alpine_datasourceexplorer_breadcrumb" style="width: 10%"></div>
                                <div>
                                    <label id="alpine_datasourceexplorer_current_location">&nbsp;</label>
                                    <button dojoType="dijit.form.Button" baseClass="dataUploadButton" class="right" id="alpine_datasourceexplorer_import_btn"  type="button" title="<fmt:message key='workbench_btn_upload'/>" disabled="true">&nbsp</button>
                                    <button dojoType="dijit.form.Button" baseClass="dataCreateSubFolderButton" class="right" id="alpine_datasourceexplorer_create_btn" type="button" title="<fmt:message key='workbench_btn_folder'/>" disabled="true">&nbsp</button>
                                    <button dojoType="dijit.form.Button" baseClass="dataCreateConnectButton" class="right" id="alpine_datasourceexplorer_create_connection_btn"  type="button" title="<fmt:message key='workbench_btn_connect'/>">&nbsp</button>
                                    <button dojoType="dijit.form.Button" baseClass="dataRefreshButton" class="right" id="alpine_datasourceexplorer_refresh_btn"  type="button" title="<fmt:message key='workbench_btn_refresh'/>">&nbsp</button>
                                </div>
                                <div class="textBoxFancyBorder">
                                    <div dojoType="alpine.layout.ClearableTextBox" id="alpine_datasourceexplorer_filter" style="width:255px;" placeHolder="<fmt:message key='workbench_filterbox_placeholder'/>"></div>
                                </div>
                            </div>
                        </div>
                        <div dojoType="dijit.layout.ContentPane" region="center" id="alpine_datasourceexplorer_display_pane_holder" >
                            <div id="alpine_datasourceexplorer_display_pane" style="height: 100%;width:300px"></div>
                        </div>
                    </div>
                </div>
                <div dojoType="dijit.layout.ContentPane" id="alpine_layout_navigation_operator_pane">
                    <div dojoType="dijit.layout.LayoutContainer" style="background-color: transparent">
                        <div dojoType="dijit.layout.ContentPane" region="top">
                            <div class="workbenchHeader">
                                <div class="transparentDropdownButtonExtLabel"><fmt:message key='workbench_op_filter_label'/></div>
                                <select id="alpine_operatorexplorer_filterselect"></select>

                                <div class="textBoxFancyBorder">
                                    <div dojoType="alpine.layout.ClearableTextBox" id="alpine_operatorexplorer_filter" style="width: 255px;"  placeHolder="<fmt:message key='workbench_filterbox_placeholder'/>"></div>
                                </div>
                            </div>
                        </div>

                        <div dojoType="dijit.layout.ContentPane" region="center" >
                            <div id="alpine_operatorexplorer_display_pane" style="height: 100%;overflow-y: auto;"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div dojoType="dijit.layout.ContentPane" region="center">
            <div dojoType="dijit.layout.LayoutContainer"
                 style="width: 100%; height: 94%;" >
                <div dojoType="dijit.layout.ContentPane"
                     layoutAlign="client" id="FlowDisplayPanelPersonal" style="background: #E7E7E9 url(../../images/interface/noflow.png) center no-repeat;">
                </div>
            </div>
        </div>
    </div>
</div>
</div>


    <%-- ====================================================Below are widget declare======================================================================== --%>

    <%-------------------------------------------- For Flow Category Tree --------------------------------------------%>
<div id ="standbyPersonalTreePane" target="personalFlowTree" dojoType="dojox.widget.Standby"></div>

    <%-------------------------------------------- For Flow Category Tree --------------------------------------------%>


<div id ="rootStandBy" target="rootContainer" dojoType="dojox.widget.Standby" centerIndicator="text" text="" zIndex="450"></div>

<div id="propertyFormDialog" style="display: none;">
    <div dojoType="dijit.layout.LayoutContainer"
         style="width: 434px; height: 500px;" id="propertyFormDialogCont">
        <div dojoType="dijit.layout.ContentPane" id="property_title_pane_top" region="top">
            <div class="titleBar">
                <img src="<%=path%>/images/user-group-icon.png" width="40" height="40" border="1" id="property_operator_icon" />
                <span class="operatorHeaderText" id="property_operator_name"></span></td>
            </div>
        </div>

        <div dojoType="dijit.layout.ContentPane" region="center" id="sidePropertyContainer">
            <br/>
            <div dojoType="dijit.form.Form" id="propertyForm"
                 jsId="propertyForm">
                <table cellspacing="6" id="propertyTable" style="width: 100%; height: 100%">
                    <!-- build the from at run time -->
                </table>
            </div>
        </div>

        <div dojoType="dijit.layout.ContentPane"
             id="property_title_pane_bottom" region="bottom">
            <div class="whiteDialogFooter" style="text-align:center;">
                <button dojoType="dijit.form.Button" type="button"   baseClass="cancelButton"
                        onClick="alpine.flow.WorkFlowVariableReplacer.finalize();alpine.flow.WorkFlowUIHelper.bindDeleteKeyBoardEvent(true);propSideBar.close();">
                    <fmt:message key="Cancel" />
                </button>
                <button dojoType="dijit.form.Button" type="button"  baseClass="primaryButton"
                        onClick="update_property_data();">
                    <fmt:message key="OK" />
                </button>
            </div>

        </div>
    </div>
</div>
<%--
<div dojoType="dijit.Dialog" id="sql_executor_dialog" draggable="false" title="">
    <div dojoType="dijit.layout.ContentPane" style="width: 300px; height: 200px;">
        <fieldset style="margin: auto;border:1px solid #cecdcd; width: 90%; height: 95%; padding-left:2px;">
            <legend style="margin-left:8px;"><label class="valueLabel" id="sql_executor_label"></label></legend>
            <textarea trim="true" dojoType="dijit.form.SimpleTextarea" id="sql_executor_text" style="height:170px;width:265px;resize:false"></textarea>
        </fieldset>
    </div>
    <div class="dialogFooter">
        <button dojoType="dijit.form.Button" baseClass="dialogButton" id="sql_executor_text_submit"><fmt:message key="OK"/></button>
        <button dojoType="dijit.form.Button" baseClass="dialogButton" id="sql_executor_text_cancel"><fmt:message key="Cancel" /></button>
    </div>
</div>
 --%>


<div dojoType="dijit.Dialog" id="alpine_flow_editing_saveform_dialog" >
    <div class="titleBar">
        <fmt:message key='Add_Save_Comments'/>
    </div>
    <div class="innerPadding" >
        <div class="paddedTopDiv" >
        <%--<label class="valueLabel"> <fmt:message key="COMMENTS"/></label>
            <br>--%>
            <textarea id="alpine_flow_editing_saveform_comments"
                      rows=5 cols=50
                      style="width: auto; resize:false">
            </textarea>
        </div>
    </div>
    <div class="whiteDialogFooter">
        <button valign = "bottom" dojoType="dijit.form.Button"    type="button" baseClass="cancelButton"
                onClick="dijit.byId('alpine_flow_editing_saveform_dialog').hide();"><fmt:message key="Cancel"/>
        </button>
        <button valign = "bottom" dojoType="dijit.form.Button" type="button" baseClass="primaryButton" id="alpine_flow_editing_saveform_submit"><fmt:message key="SAVE"/>
        </button>
    </div>
</div>

<div style="display: none;">
    <form action="<%= path%>/alpine/result/dataexplorer.jsp" method="post" id="dataExplorerForm">
        <input id="dataExplorerForm_dbConnectionName" name="dbConnectionName"/>
        <input id="dataExplorerForm_schemaName" name="schemaName"/>
        <input id="dataExplorerForm_tableName" name="tableName"/>
        <input id="dataExplorerForm_columnNameIndex" name="columnNameIndex"/>
        <input id="dataExplorerForm_columnBins" name="columnBins"/>
        <input id="dataExplorerForm_resourceType" name="resourceType"/>
        <input id="dataExplorerForm_isGeneratedTable" name="isGeneratedTable"/>
        <input id="dataExplorerForm_title_key" name="title_key"/>
        <input id="dataExplorerForm_load_method" name="load_method"/>

        <input id="dataExplorerForm_load_hadoopConnectKey" name="hadoopConnectKey"/>
        <input id="dataExplorerForm_load_hadoopFilePath" name="hadoopFilePath"/>
        <input id="dataExplorerForm_load_operatorUID" name="operatorUID"/>
        <input id="dataExplorerForm_load_flowInfoKey" name="flowInfoKey"/>
        <input id="dataExplorerForm_load_isHDFileOperator" name="isHDFileOperator"/>
    </form>
</div>

<div dojoType="alpine.layout.PopupDialog" id="alpine_system_licenseInfo_dialog" draggable="false" title="<fmt:message key='license_displayer_about' />">
    <div dojoType="dijit.layout.LayoutContainer" style="width: 400px; height: 200px;overflow: hidden;">
        <div dojoType="dijit.layout.ContentPane" region="center">
            <table width="100%" height="100%" cellspacing="5">
                <tr>
                    <td>
                        <B><fmt:message key="license_displayer_product_version"/></B>
                    </td>
                    <td>
                        <label id="license_displayer_product_version_value"></label>
                    </td>
                </tr>
                <tr>
                    <td>
                        <B><fmt:message key="license_displayer_expire_date"/></B>
                    </td>
                    <td>
                        <label id="license_displayer_expire_date_value"></label>
                    </td>
                </tr>
                <tr>
                    <td>
                        <B><fmt:message key="license_displayer_limit_user_count"/></B>
                    </td>
                    <td>
                        <label id="license_displayer_limit_user_count_value"></label>
                    </td>
                </tr>
                <tr>
                    <td>
                        <B><fmt:message key="license_displayer_limit_modeler_count"/></B>
                    </td>
                    <td>
                        <label id="license_displayer_limit_modeler_count_value"></label>
                    </td>
                </tr>
                <tr>
                    <td>
                        <B><fmt:message key="license_displayer_customer_UUID"/></B>
                    </td>
                    <td>
                        <label id="license_displayer_customer_UUID"></label>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <div class="whiteDialogFooter">
        <button   baseClass="primaryButton" valign = "bottom" onclick="dijit.byId('alpine_system_licenseInfo_dialog').hide();" dojoType="dijit.form.Button" type="button"><fmt:message key="Done"/></button>
    </div> 
</div>

<div dojoType="dojox.widget.Toaster" positionDirection="br-left" messageTopic="toasterMessage"></div>
<div dojoType="dojox.widget.Toaster" positionDirection="br-up" messageTopic="toasterMessage_ru"></div>
</body>
</fmt:bundle>
</html>
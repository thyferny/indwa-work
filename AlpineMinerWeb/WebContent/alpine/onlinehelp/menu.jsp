<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
	OperatorHelpConvertion convert = OperatorHelpConvertion.getInstance(request.getLocale());
%>
<%@ include file="/alpine/commons/jstl.jsp"%>

<%@page import="com.alpine.miner.impls.onlinehelp.OperatorHelpConvertion"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<style type="text/css" >
		a:link,a:visited {
			text-decoration: none
		}
		 
		#category {
			font-family: Verdana, Geneva, Arial, Helvetica, sans-serif;
			white-space: nowrap;
		}

		#category li {
			font-size: 18px;
		}

		#category li li {
			font-size: 16px;
		}

		#category li li li {
			font-size: 14px;
		}

		#category li li li li {
			font-size: 12px;
		}

		#category ul {
			margin: 0px;
			margin-left: 0px;
			padding: 0px 0px 0px 5px 5px;
		}
	</style>
</head>
<body>
<fmt:bundle basename="onlineHelp">
	<ul id="category">
        <li><fmt:message key="menu.overview"/>
            <ul>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("Overview") %>"><fmt:message key="menu.overview"/></A></li>
            </ul>
        </li>
        <li><a target="content"  href="tools/<%=convert.getHelpVal("administrative_overview") %>"><fmt:message key="menu.tool.Administrative_Options"/></a>
            <ul>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("preference_management") %>"><fmt:message key="menu.tool.Preference_Management"/></A></li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("security_config") %>"><fmt:message key="menu.tool.Security_Config"/></A></li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("group_user_management") %>"><fmt:message key="menu.tool.Group_User_Management"/></A></li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("email_server_configuration") %>"><fmt:message key="menu.tool.Email_Server_Configuration"/></A></li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("customized_operator_definition") %>"><fmt:message key="menu.tool.Customized_Operator_Definition"/></A></li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("login_session_management") %>"><fmt:message key="menu.tool.login_session_management"/></A></li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("user_logs") %>"><fmt:message key="menu.tool.User_Logs"/></A></li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("license_info") %>"><fmt:message key="menu.tool.license_info"/></A></li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("license_update") %>"><fmt:message key="menu.tool.license_update"/></A></li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("delete_temporary_file") %>"><fmt:message key="menu.tool.Delete_Temporary_File"/></A></li>
            </ul>
        </li>
        <li><fmt:message key="menu.tool.User_Options"/>
            <ul>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("my_profile") %>"><fmt:message key="menu.tool.My_Profile"/></A></li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("scheduler_config") %>"><fmt:message key="menu.tool.Scheduler_Configuration"/></A></li>
            </ul>
        </li>
        <li><fmt:message key="menu.tool"/>
			<ul>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("analyze_flow_management") %>"><fmt:message key="menu.tool.Analyze_Flow_Management"/></A>
                    <ul>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("personal_Flow_Manager") %>"><fmt:message key="menu.tool.personal_category_manager"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("create_new_flow") %>"><fmt:message key="menu.tool.create_new_flow"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("add_flow_to_personal") %>"><fmt:message key="menu.tool.Add_Flow_to_My_Workflows"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("drop_flow") %>"><fmt:message key="menu.tool.Drop_Flow"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("inspect_flow_edit_history") %>"><fmt:message key="menu.tool.Inspect_Edit_History_of_Flow"/></A></li>
                        <%--<li><A target="content"  href="tools/<%=convert.getHelpVal("inspect_content_of_flow") %>"><fmt:message key="menu.tool.Inspect_Content_of_Flow"/></A></li>--%>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("import_flow") %>"><fmt:message key="menu.tool.Import_Flow"/></A></li>
                    </ul>
                </li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("database_connection_management") %>"><fmt:message key="menu.tool.Database_Connection_Management"/></A>
                    <ul>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("data_source_explorer") %>"><fmt:message key="menu.tool.Data_Source_Explorer"/></A></li>
                    </ul>
                </li>
                <li><A target="content"  href="tools/<%=convert.getHelpVal("operator_explorer") %>"><fmt:message key="menu.tool.Operators"/></A>
                    <ul>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("operator_explorer") %>"><fmt:message key="menu.tool.Operator_Explorer"/></A></li>
                    </ul>
                </li>
                <li><fmt:message key="menu.tool.Editing_Workflows"/>
                    <ul>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("properties_of_flow_editor") %>"><fmt:message key="menu.tool.Properties_of_Flow_Editor"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("connect_delete_ops") %>"><fmt:message key="menu.tool.connect_delete_ops"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("data_preview") %>"><fmt:message key="menu.tool.Data_Preview"/></A>
                            <ul>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("data_explorer") %>"><fmt:message key="menu.tool.Data_Explorer"/></A></li>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("scatter_plot_chart") %>"><fmt:message key="menu.tool.Scatter_Plot_Chart"/></A></li>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("scatter_plot_matrix_chart") %>"><fmt:message key="menu.tool.Scatter_Plot_Matrix_Chart"/></A></li>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("univariate_plot_chart") %>"><fmt:message key="menu.tool.Univariate_Plot_Chart"/></A></li>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("box_and_whisker_chart") %>"><fmt:message key="menu.tool.Box_and_Whisker_Chart"/></A></li>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("histogram_chart") %>"><fmt:message key="menu.tool.Histogram_Chart"/></A></li>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("bar_chart") %>"><fmt:message key="menu.tool.Bar_Chart"/></A></li>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("time_series_chart") %>"><fmt:message key="menu.tool.Time_Series_Chart"/></A></li>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("summary_statistics") %>"><fmt:message key="menu.tool.Summary_Statistics"/></A></li>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("correlation_analysis") %>"><fmt:message key="menu.tool.Correlation_Analysis"/></A></li>
                                <li><A target="content"  href="tools/<%=convert.getHelpVal("frequency_analysis") %>"><fmt:message key="menu.tool.Frequency_Analysis"/></A></li>
                            </ul>
                        </li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("show_table_metadata") %>"><fmt:message key="menu.tool.Show_Table_Metadata"/></A></li>
                    </ul>
                </li>
                <li><fmt:message key="menu.tool.Running_Workflows"/>
                    <ul>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("flow_run") %>"><fmt:message key="menu.tool.Run"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("flow_stop") %>"><fmt:message key="menu.tool.Stop"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("flow_step_run") %>"><fmt:message key="menu.tool.Step_run"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("save_flow_output") %>"><fmt:message key="menu.tool.save_report"/></A></li>
                    </ul>
                </li>
                <li><fmt:message key="menu.tool.ACTIONS"/>
                    <ul>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("find_and_replace") %>"><fmt:message key="menu.tool.flow_findandreplace"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("flow_result_management") %>"><fmt:message key="menu.tool.Flow_Result_Management"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("db_resource_sync") %>"><fmt:message key="menu.tool.db_resource_sync"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("duplicate_flow") %>"><fmt:message key="menu.tool.Duplicate_Flow"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("export_flow") %>"><fmt:message key="menu.tool.Export_Flow"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("share_flow") %>"><fmt:message key="menu.tool.Share_Flow"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("flow_variable") %>"><fmt:message key="menu.tool.flow_variable"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("clear_temporary_tv") %>"><fmt:message key="menu.tool.Clear_temporary_table_and_view"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("open_flow_from_inspect_history") %>"><fmt:message key="menu.tool.Open_Flow_from_Inspect_History"/></A></li>
                    </ul>
                </li>
                <li><fmt:message key="menu.tool.data_import"/>
                    <ul>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("import_to_database") %>"><fmt:message key="menu.tool.data_import_database"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("import_to_hadoop") %>"><fmt:message key="menu.tool.data_import_hadoop"/></A></li>
                    </ul>
                </li>
                <li><fmt:message key="menu.tool.Other_Options"/>
                    <ul>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("storeage_parameter") %>"><fmt:message key="menu.tool.storeage_parameter"/></A></li>
                        <li><A target="content"  href="tools/<%=convert.getHelpVal("model_management") %>"><fmt:message key="menu.tool.Model_Management"/></A></li>
                    </ul>
                </li>
            </ul>
        </li>
        <li><fmt:message key="menu.tool.Operators"/>
            <ul>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("AdaboostOperator") %>"><fmt:message key="menu.operator.adaboost"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("AdaboostPredictOperator") %>"><fmt:message key="menu.operator.adaboost_prediction"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("AggregateOperator") %>"><fmt:message key="menu.operator.aggregate"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("AssociationOperator") %>"><fmt:message key="menu.operator.association"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("BarChartAnalysisOperator") %>"><fmt:message key="menu.operator.bar_chart_preview"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("CartOperator") %>"><fmt:message key="menu.operator.cart_tree"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("CopyToDBOperator") %>"><fmt:message key="menu.operator.CopyToDatabase"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("CopytoHadoopOperator") %>"><fmt:message key="menu.operator.CopyToHadoop"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("CorrelationAnalysisOperator") %>"><fmt:message key="menu.operator.correlation_analysis"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("DbTableOperator") %>"><fmt:message key="menu.operator.db_table"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("DecisionTreeOperator") %>"><fmt:message key="menu.operator.decision_tree"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("TreePredictOperator") %>"><fmt:message key="menu.operator.tree_prediction"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("FilterOperator") %>"><fmt:message key="menu.operator.filter_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("ColumnFilterOperator") %>"><fmt:message key="menu.operator.column_filter_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("FrequencyAnalysisOperator") %>"><fmt:message key="menu.operator.frequency_analysis"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("HistogramOperator") %>"><fmt:message key="menu.operator.histogram"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("InformationValueAnalysisOperator") %>"><fmt:message key="menu.operator.information_value_analysis"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("TableJoinOperator") %>"><fmt:message key="menu.operator.join"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("GoodnessOfFitOperator") %>"><fmt:message key="menu.operator.goodness_of_fit_model_evaluator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("HadoopFileOperator") %>"><fmt:message key="menu.operator.HadoopFile"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("KMeansOperator") %>"><fmt:message key="menu.operator.k_means_clustering"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("LIFTOperator") %>"><fmt:message key="menu.operator.lift_model_evaluator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("ModelOperator") %>"><fmt:message key="menu.operator.model"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("LinearRegressionOperator") %>"><fmt:message key="menu.operator.linear_regression"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("LinearRegressionPredictOperator") %>"><fmt:message key="menu.operator.linear_regression_prediction"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("LogisticRegressionOperator") %>"><fmt:message key="menu.operator.logistic_regression"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("LogisticRegressionPredictOperator") %>"><fmt:message key="menu.operator.logistic_regression_prediction"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("NaiveBayesOperator") %>"><fmt:message key="menu.operator.naive_bayes"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("NaiveBayesPredictOperator") %>"><fmt:message key="menu.operator.naive_bayes_prediction"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("NeuralNetworkOperator") %>"><fmt:message key="menu.operator.neural_network"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("NeuralNetworkPredictOperator") %>"><fmt:message key="menu.operator.neural_network_prediction"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("NormalizationOperator") %>"><fmt:message key="menu.operator.normalization"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("NoteOperator") %>"><fmt:message key="menu.operator.notes_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("ReplaceNullOperator") %>"><fmt:message key="menu.operator.null_value_replacement_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("IntegerToTextOperator") %>"><fmt:message key="menu.operator.numeric_to_text_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("PCAOperator") %>"><fmt:message key="menu.operator.pca"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("PivotOperator") %>"><fmt:message key="menu.operator.pivot_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("RandomSamplingOperator") %>"><fmt:message key="menu.operator.random_sampling"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("TimeSeriesOperator") %>"><fmt:message key="menu.operator.time_series"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("TimeSeriesPredictOperator") %>"><fmt:message key="menu.operator.time_series_prediction"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("ROCOperator") %>"><fmt:message key="menu.operator.roc_auc_model_evaluator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("SampleSelectorOperator") %>"><fmt:message key="menu.operator.sample_selector"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("ScatterMatrixOperator") %>"><fmt:message key="menu.operator.scatter_plot_matrix"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("SQLExecuteOperator") %>"><fmt:message key="menu.operator.sql_execute_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("StratifiedSamplingOperator") %>"><fmt:message key="menu.operator.stratified_sampling"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("SVMClassificationOperator") %>"><fmt:message key="menu.operator.svm_classification"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("SVMRegressionOperator") %>"><fmt:message key="menu.operator.svm_regression"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("SVMNoveltyDetectionOperator") %>"><fmt:message key="menu.operator.svm_novelty_detection"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("SVMPredictOperator") %>"><fmt:message key="menu.operator.svm_prediction"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("SVDLanczosOperator") %>"><fmt:message key="menu.operator.svd"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("SVDLanczosCalculatorOperator") %>"><fmt:message key="menu.operator.svd_calculator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("ValueAnalysisOperator") %>"><fmt:message key="menu.operator.value_analysis"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("VariableOperator") %>"><fmt:message key="menu.operator.variable_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("UnivariateOperator") %>"><fmt:message key="menu.operator.univariate"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("VariableSelectionAnalysisOperator") %>"><fmt:message key="menu.operator.variable_selection"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("WOEOperator") %>"><fmt:message key="menu.operator.woe"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("WOETableGeneratorOperator") %>"><fmt:message key="menu.operator.woe_table"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("CustomizedOperator") %>"><fmt:message key="menu.operator.define_customzied_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("TableSetOperator") %>"><fmt:message key="menu.operator.tableset"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("PLDATrainerOperator") %>"><fmt:message key="menu.operator.plda_trainer_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("PLDAPredictOperator") %>"><fmt:message key="menu.operator.plda_predict_operator"/></A></li>
                <li><A target="content"  href="operators/<%=convert.getHelpVal("SubFlowOperator") %>"><fmt:message key="menu.operator.subflow_operator"/></A></li>
            </ul>
        </li>

<%--						<li><A target="content"  href="tools/<%=convert.getHelpVal("online_help") %>"><fmt:message key="menu.tool.Online_Help"/></A></li>

					</ul>
				<li><fmt:message key="menu.tool.User_Options"/>
					<ul>
						<li><A target="content"  href="tools/<%=convert.getHelpVal("database_connection_management_ordinary") %>"><fmt:message key="menu.tool.Database_Connection_Management"/></A></li>

						<li>
							<fmt:message key="menu.tool.Flow_Analysis"/>
							<ul>
								<li><A target="content"  href="tools/<%=convert.getHelpVal("flow_run") %>"><fmt:message key="menu.tool.Run"/></A></li>
								<li><A target="content"  href="tools/<%=convert.getHelpVal("flow_stop") %>"><fmt:message key="menu.tool.Stop"/></A></li>
								<li><A target="content"  href="tools/<%=convert.getHelpVal("flow_step_run") %>"><fmt:message key="menu.tool.Step_run"/></A></li>
								<li><A target="content"  href="tools/<%=convert.getHelpVal("storeage_parameter") %>"><fmt:message key="menu.tool.storeage_parameter"/></A></li>
								<li><A target="content"  href="tools/<%=convert.getHelpVal("save_flow_output") %>"><fmt:message key="menu.tool.save_report"/></A></li>
							</ul>
						</li>
						<li>
							<A target="content"  href="tools/<%=convert.getHelpVal("data_preview") %>"><fmt:message key="menu.tool.Data_Preview"/></A>

						</li>

                    </ul>
				</li>
			</ul>
		<li>
			

			

		</li>
	</ul>   --%>
</fmt:bundle>
</body>
</html>
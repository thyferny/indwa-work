<%@page import="com.alpine.utility.hadoop.HadoopConstants"%>
<%@page import="com.alpine.utility.hadoop.HadoopConnection"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<style type="text/css">
#variableDerivedConfigBorderContainer {
	width: 100%;
	height: 90%;
}
</style>

    <style>.variableCodeMirror .CodeMirror-scroll {height: 100px}</style>
</head>
<fmt:bundle basename="app">


	<body>

		<div dojoType="dijit.Dialog" draggable="false" id="variableDerivedConfigEditDialog"
			title="<fmt:message key='var_derived_edit_title'/>">
            <div class="titleBar">
                <fmt:message key='var_derived_edit_title'/>
            </div>
			<div dojoType="dijit.layout.BorderContainer" design="sidebar"
			style="width: 900px; height: 480px;"
				gutters="true" liveSplitters="true"
				id="variableDerivedConfigBorderContainer">

				<div dojoType="dijit.layout.ContentPane" splitter="true" style="width: 30%;"
					region="center">
					<div dojoType="dijit.layout.LayoutContainer">
					
					<div dojoType="dijit.layout.ContentPane" id="var_derived_gridpane"
						layoutAlign="client" style="width: 100%; height: 150;"></div>
					<div dojoType="dijit.layout.ContentPane" 
						layoutAlign="bottom" style="width: 100%;">

					<div dojoType="dijit.form.Form" id="var_derivedForm"
						jsId="var_derivedForm" encType="multipart/form-data" action=""
						method="">

						<div width="100%">
							<div style="overflow: auto; padding:10px;">
                                <div><input style="width:75%; float:right; margin-left:10px;" type="text" id="var_dev_result_column"
                                            name="resultColumnName" required="false" trim="true"
                                            dojoType="dijit.form.ValidationTextBox" regExp="^[\w]+" onfocus="return false;"
                                        />
                                </div>
                                <div align="right"><label class="valueLabel">
										<fmt:message key="var_dev_result_column" /> </label>
								</div>
							</div>
							<div style="overflow: auto; padding:10px;">
                                <div style="width:75%; float:right;padding-left:10px"><select dojoType="dijit.form.Select"
                                             id="var_dev_data_type" name="dataType"
                                             baseClass="greyDropdownButton" style="width:auto;"   >
                                    <option value="BIGINT" selected>BIGINT</option>
                                    <option value="BOOLEAN">BOOLEAN</option>
                                    <option value="BIT">BIT</option>
                                    <option value="BIT VARYING">BIT VARYING</option>
                                    <option value="CHAR">CHAR</option>
                                    <option value="DATE">DATE</option>
                                    <option value="DOUBLE PRECISION">DOUBLE PRECISION</option>
                                    <option value="NUMERIC">NUMERIC</option>
                                    <option value="INTEGER">INTEGER</option>
                                    <option value="VARCHAR">VARCHAR</option>

                                </select></div>
								<div align="right"><label class="valueLabel">
										<fmt:message key="var_dev_data_type" /> </label>
								</div>

							</div>

							<div style="overflow: auto; padding:10px;">
                                <div style="width:75%; float:right;padding-left:10px"><div class="variableCodeMirror"><textarea id="var_sql_spec" name="sqlExpression"
                                                                               dojoType="dijit.form.SimpleTextarea" rows="3" cols="65"
                                                                               style="width: 450px;"></textarea>  </div>
                                </div>
                                <div align="right"><label class="valueLabel" id="sqlExpression_title">
										<fmt:message key="var_sql_spec" /> </label>
								</div>

							</div>
							<div style="width:75%; float:right;padding-left:10px">
								<div >
									<button dojoType="dijit.form.Button" type="button" baseClass="workflowButton"
										id="var_derived_create_button"
										jsId="var_derived_create_button">
										<fmt:message key="create_button" />
									</button>
									<button dojoType="dijit.form.Button" type="button" baseClass="workflowButton"
										id="var_derived_update_button"
										jsId="var_derived_update_button">
										<fmt:message key="update_button" />
									</button>
                                    <button dojoType="dijit.form.Button" baseClass="workflowButton"
                                            id="var_derived_delete_button"
                                            jsId="var_derived_delete_button" type="reset">
                                        <fmt:message key="delete_button" />
                                    </button>
									<button dojoType="dijit.form.Button" baseClass="workflowButton"
										id="var_derived_reset_button"
										jsId="var_derived_reset_button" type="reset">
										<fmt:message key="clear_button" />
									</button>
                                    <span style="float:right">
                                        <button dojoType="dijit.form.Button" baseClass="workflowButton"
                                                id="pigSyntaxHelperButton"
                                                onClick="return dijit.byId('pigSyntaxHelperDialog').show();" style="display: none;">
                                            <fmt:message key="pig_syntax_button" />
                                        </button>
                                    </span>
                                </div>
							</div>
						</div>

					</div>
					</div>
					</div>

				</div>

				<div dojoType="dijit.layout.ContentPane" splitter="true"
					region="right"  style="width: 30%;" id="var_derived_list">
					<div dojoType="dijit.layout.LayoutContainer"
						style="width: 100%; height: 100%">
						<div dojoType="dijit.layout.ContentPane" layoutAlign="client">
							<div id="var_derived_list_table" style="width:100%;height:100%;"></div>
						</div>
					</div>

				</div>
			</div>
			<div class="whiteDialogFooter" style="text-align: right;">
				<button dojoType="dijit.form.Button" type="button" baseClass="cancelButton" id="btn_close_4defineVariableProperty">
					<fmt:message key="Cancel" />
				</button>
                <button dojoType="dijit.form.Button" type="button" baseClass="primaryButton" id="btn_save_4defineVariableProperty">
                    <fmt:message key="OK" />
                </button>
			</div>
		</div>

    <div dojoType="dijit.Dialog" draggable="false" id="pigSyntaxHelperDialog">
        <div class="titleBar">
            <fmt:message key='pig_expressions'/>
        </div>
        <div dojoType="dijit.layout.ContentPane" region="center" style="width:800px; height:400px;">
            <div class="innerPadding">
                <div class="pigSyntaxGroup">
                    <span class="pigSyntaxSubheader"><fmt:message key='pig_syntax_numbers'/></span>
                    <table class="pigSyntaxTable">
                        <tr>
                            <td><span class="pigSyntaxBold">ABS</span>(-3.2) <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> 3.2</td>
                            <td><span class="pigSyntaxBold">CEIL</span>(2.4) <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> 3</td>
                            <td><span class="pigSyntaxBold">EXP</span>(1) <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> 2.71828</td>
                        </tr>
                        <tr>
                            <td><span class="pigSyntaxBold">FLOOR</span>(4.6) <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> 4</td>
                            <td><span class="pigSyntaxBold">LOG</span>(1) <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> 0</td>
                            <td><span class="pigSyntaxBold">RANDOM</span>() <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> [0,1)</td>
                        </tr>
                        <tr>
                            <td><span class="pigSyntaxBold">ROUND</span>(2.4) <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> 2</td>
                            <td><span class="pigSyntaxBold">SQRT</span>(4) <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> 2</td>
                            <td>19 % 10 <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> 9</td>
                        </tr>
                    </table>
                </div>
                <div class="pigSyntaxGroup">
                    <span class="pigSyntaxSubheader"><fmt:message key='pig_syntax_strings'/></span>
                    <table class="pigSyntaxTable">
                        <tr>
                            <td><span class="pigSyntaxBold">CONCAT</span>('a', 'b') <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> ab</td>
                            <td><span class="pigSyntaxBold">INDEXOF</span>('entree', 'e', 1) <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> 4</td>
                            <td><span class="pigSyntaxBold">LAST_INDEX_OF</span>('entree', 'e') <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> 5</td>
                        </tr>
                        <tr>
                            <td><span class="pigSyntaxBold">LOWER</span>('Alpine') <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> alpine</td>
                            <td><span class="pigSyntaxBold">REGEX_EXTRACT</span>('this:that', '(.*)\\:(.*)', 2) <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> that</td>
                            <td><span class="pigSyntaxBold">REPLACE</span>('Alpine', 'Alp', 'F') <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> Fine</td>
                        </tr>
                        <tr>
                            <td><span class="pigSyntaxBold">SUBSTRING</span>('Alpine', 2, 5) <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> pin</td>
                            <td><span class="pigSyntaxBold">TRIM</span>(' a b ') <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> a b</td>
                            <td><span class="pigSyntaxBold">UPPER</span>('Alpine') <span class="pigSyntaxReturns"><fmt:message key='pig_syntax_returns'/></span> ALPINE</td>
                        </tr>
                    </table>
                </div>
                <div class="pigSyntaxGroup">
                    <span class="pigSyntaxSubheader"><fmt:message key='pig_syntax_conditionals'/></span>
                    <table class="pigSyntaxTable">
                        <tr>
                            <td>
                                (LOWER(referring_url) MATCHES '.*\\.org.*' ? 'Non-Profit' : 'Other')
                            </td>
                        </tr>
                    </table>
                </div>
                <div>
                <%
                	String helpLink = HadoopConstants.VERSION_APACHE_HADOOP_0_20_2.equals(HadoopConnection.CURRENT_HADOOP_VERSION) ? "http://pig.apache.org/docs/r0.10.0/" : "http://pig.apache.org/docs/r0.8.1/";
                %>
                    <fmt:message key='pig_syntax_details'/> <a href="<%=helpLink %>" style="outline:none;" target="_blank"><fmt:message key='pig_syntax_online_doc'/></a>
                </div>
            </div>
        </div>
        <div  dojoType="dijit.layout.ContentPane"  region="bottom">
            <div class="whiteDialogFooter">
                <button dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
                        onClick="return dijit.byId('pigSyntaxHelperDialog').hide();">
                    <fmt:message key="OK" />
                </button>
            </div>
        </div>
    </div>

	</body>
</fmt:bundle>
</html>
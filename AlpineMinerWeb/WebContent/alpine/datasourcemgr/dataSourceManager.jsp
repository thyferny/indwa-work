<%@page import="com.alpine.utility.hadoop.HadoopConstants"%>
<%@page import="com.alpine.utility.hadoop.HadoopConnection"%>
<script type="text/javascript">
<!--
	dojo.require("alpine.datasourcemgr.DataSourceUIHelper");
	dojo.require("alpine.datasourcemgr.DataSourceCreateUIHelper");
//-->
</script>
<fmt:bundle basename="app">
<!-- main data source window -->
	<div dojoType="dijit.Dialog" draggable="false" id="alpine_datasource_config_Dialog"
		title="<fmt:message key='datasource_config_dialog_title'/>">
		<div class="titleBar">
            <fmt:message key='datasource_config_dialog_title'/>
        </div>
        <div class="innerPadding">
            <div dojoType="dijit.layout.AccordionContainer" style="width: 350px; height: 450px" id="alpine_datasource_config_container">
                <div dojoType="dijit.layout.ContentPane" title="<fmt:message key='Public'/>" id="alpine_datasource_config_public">
                </div>
                <div dojoType="dijit.layout.ContentPane" title="<fmt:message key='Group'/>" id="alpine_datasource_config_group">
                </div>
                <div dojoType="dijit.layout.ContentPane" title="<fmt:message key='Personal'/>" id="alpine_datasource_config_personal">
                </div>
            </div>
            <table width="100%">
                <tr>
                    <td align="left" valign="middle">
                        <button type="button" dojoType="dijit.form.Button" baseclass="workflowButton" id="alpine_datasource_config_button_create"><fmt:message key="create_button"/></button>
                        <button type="button" dojoType="dijit.form.Button" baseclass="workflowButton" id="alpine_datasource_config_button_update"><fmt:message key="edit_button"/></button>
                        <button type="button" dojoType="dijit.form.Button" baseclass="workflowButton" id="alpine_datasource_config_button_delete"><fmt:message key="delete_button"/></button>
                        <button type="button" dojoType="dijit.form.Button" baseclass="workflowButton" id="alpine_datasource_config_button_duplicate"><fmt:message key="datasource_config_button_duplicate"/></button>
                        <alpine:permissionChecker permission="IMPORT_JDBC_DRIVER">
                            <button type="button" dojoType="dijit.form.Button" baseclass="workflowButton" id="db_connect_button_import_db_driver"><fmt:message key='Import_JDBC_Driver' /></button>
                        </alpine:permissionChecker>
                    </td>
                </tr>
            </table>
        </div>
        <div class="whiteDialogFooter">
                <button type="button" dojoType="dijit.form.Button" baseclass="primaryButton" id="alpine_datasource_config_button_close"><fmt:message key="Done"/></button>
            </td>

        </div>
	</div>


    <!-- editor dialog -->
	<div dojoType="dijit.Dialog" id="alpine_datasource_config_editor_Dialog" title="<fmt:message key='datasource_config_editor_title'/>">
        <div class="titleBar">
            <fmt:message key='datasource_config_editor_title'/>
        </div>
        <div class="innerPadding">
            <div dojoType="dijit.layout.LayoutContainer" style="width: 350px; height: 350px" id="alpine_datasource_config_editor_mainContainer">
                <div dojoType="dijit.layout.ContentPane" region="top">
                    <table width="100%" class="innerPadding">
                        <tr>
                            <td width="120px">
                                <label class="valueLabel"><fmt:message key="datasource_config_editor_type"/> </label>
                            </td>
                            <td>
                                <select dojoType="dijit.form.Select" id="alpine_datasource_config_editor_datasourceType">
                                    <option value="DATABASE">Database</option>
                                    <option value="HADOOP">Hadoop</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label class="valueLabel"><fmt:message key="TYPE" /> </label>
                            </td>
                            <td>
                                <select dojoType="dijit.form.Select" id="alpine_datasource_config_editor_type">
                                    <option value="Public">Public</option>
                                    <option value="Group">Group</option>
                                    <option value="Personal">Personal</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label class="valueLabel"><fmt:message key="GroupName" /></label>
                            </td>
                            <td>
                                <select id="alpine_datasource_config_editor_group" dojoType="dijit.form.Select" disabled="true"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label class="valueLabel"><fmt:message key="datasource_config_edit_connname"/></label>
                            </td>
                            <td>
                                <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_config_editor_connection_name" style="width:150px">
                            </td>
                        </tr>
                    </table>
                </div>
                <div dojoType="dijit.layout.ContentPane" region="center">
                    <div dojoType="dijit.layout.StackContainer" id="alpine_datasource_config_editor_contentSwitcher">
                        <div dojoType="dijit.layout.ContentPane" id="alpine_datasource_config_editor_db_content">
                            <div dojoType="dijit.form.Form" id="alpine_datasource_config_editor_db_form">
                                <table width="100%" class="innerPadding">
                                    <tr>
                                        <td width="125px">
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_dbtype"/></label>
                                        </td>
                                        <td>
                                            <select dojoType="dijit.form.Select" id="alpine_datasource_db_config_editor_dbType">
                                                <option value="PostgreSQL">PostgreSQL</option>
                                                <option value="Greenplum">Greenplum</option>
                                                <option value="Oracle">Oracle</option>
                                                <option value="DB2">DB2</option>
                                                <option value="Netezza">Netezza</option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_host"/> </label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_db_config_editor_host" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_port"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_db_config_editor_port" style="width:150px"
                                                   data-dojo-props="regExp:'[\\d]+', invalidMessage:'<fmt:message key="dbconn_config_edit_port_tip" />'">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_dbname"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_db_config_editor_db_name" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_username"/> </label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_db_config_editor_username" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_password"/> </label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_db_config_editor_password" type="password" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td></td>
                                        <td>
                                            <input dojoType="dijit.form.CheckBox" id="alpine_datasource_db_config_editor_usessl" disabled>
                                            <fmt:message key='dbconn_config_edit_useSSL'/>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>

                        <div dojoType="dijit.layout.ContentPane" id="alpine_datasource_config_editor_hadoop_content">
                            <div dojoType="dijit.form.Form" id="alpine_datasource_config_editor_hadoop_form">
                                <table width="100%" class="innerPadding">
                                    <tr>
                                        <td width="125px">
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_hdfs_hostname"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_hadoop_config_editor_hdfs_host" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_hdfs_port"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_hadoop_config_editor_hdfs_port" regExp="^[0-9]*$" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_job_hostname"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_hadoop_config_editor_job_host" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_job_port"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_hadoop_config_editor_job_port" regExp="^[0-9]*$" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_version"/></label>
                                        </td>
                                        <td>
                                            <select dojoType="dijit.form.Select" required="true" id="alpine_datasource_hadoop_config_editor_version"></select>
                                        </td>
                                    </tr>
                                    <tbody style="display : <%=(HadoopConnection.CURRENT_HADOOP_VERSION.equals(HadoopConstants.VERSION_APACHE_1_0_2)||HadoopConnection.CURRENT_HADOOP_VERSION.equals(HadoopConstants.VERSION_APACHE_1_0_4)) ? "''" : "none"%>">
	                                    <tr>
	                                        <td>
	                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_securityMode"/></label>
	                                        </td>
	                                        <td>
	                                            <select dojoType="dijit.form.Select" id="alpine_datasource_hadoop_config_editor_securityMode">
	                                            	<option value="simple">Simple</option>
	                                            	<option value="kerberos">Kerberos</option>
	                                            </select>
	                                        </td>
	                                    </tr>
	                               		<tr>
	                               			<td>
	                               				<label class="valueLabel"><fmt:message key="hadoopconn_config_edit_hdfsPrincipal"/></label>
	                               			</td>
	                               			<td>
	                               				<input dojoType="dijit.form.ValidationTextBox" disabled="true" required="true" trim="true" id="alpine_datasource_hadoop_config_editor_hdfsPrincipal" style="width:150px">
	                               			</td>
	                               		</tr>
	                               		<tr>
	                               			<td>
	                               				<label class="valueLabel"><fmt:message key="hadoopconn_config_edit_hdfsKeytab"/></label>
	                               			</td>
	                               			<td>
	                               				<input dojoType="dijit.form.ValidationTextBox" disabled="true" required="true" trim="true" id="alpine_datasource_hadoop_config_editor_hdfskeytab" style="width:150px">
	                               			</td>
	                               		</tr>
	                               		<tr>
	                               			<td>
	                               				<label class="valueLabel"><fmt:message key="hadoopconn_config_edit_mapredPrincipal"/></label>
	                               			</td>
	                               			<td>
	                               				<input dojoType="dijit.form.ValidationTextBox" disabled="true" required="true" trim="true" id="alpine_datasource_hadoop_config_editor_mapredPrincipal" style="width:150px">
	                               			</td>
	                               		</tr>
	                               		<tr>
	                               			<td>
	                               				<label class="valueLabel"><fmt:message key="hadoopconn_config_edit_mapredKeytab"/></label>
	                               			</td>
	                               			<td>
	                               				<input dojoType="dijit.form.ValidationTextBox" disabled="true" required="true" trim="true" id="alpine_datasource_hadoop_config_editor_mapredkeytab" style="width:150px">
	                               			</td>
	                               		</tr>
                               		</tbody>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_username"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_hadoop_config_editor_userName" regExp="^[\w]+" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_groupName"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_datasource_hadoop_config_editor_groupName" regExp="^[\w]+" style="width:150px">
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="whiteDialogFooter">
			<input type="hidden" id="alpine_datasource_config_editor_createUser">
            <button type="button" baseclass="cancelButton" dojoType="dijit.form.Button" id="alpine_datasource_config_editor_button_cancel"><fmt:message key="Cancel"/></button>
            <button type="button" baseclass="secondaryButton" dojoType="dijit.form.Button" id="alpine_datasource_config_editor_button_test"><fmt:message key="datasource_config_button_test_connect"/></button>
			<button type="button" baseclass="primaryButton" dojoType="dijit.form.Button" id="alpine_datasource_config_editor_button_save"><fmt:message key="OK"/></button>
        </div>
	</div>

    <!-- editor dialog for tool button -->
	<div dojoType="dijit.Dialog" id="alpine_create_datasource_4toolbutton_Dialog" title="<fmt:message key='datasource_config4toolbutton_create_title'/>">
        <div class="titleBar">
            <fmt:message key='datasource_config4toolbutton_create_title'/>
        </div>
        <div class="innerPadding">
            <div dojoType="dijit.layout.LayoutContainer" style="width: 350px; height: 310px" id="alpine_create_datasource_4toolbutton_mainContainer">
                <div dojoType="dijit.layout.ContentPane" region="top">
                    <table width="100%" class="innerPadding">
                        <tr>
                            <td width="120px">
                                <label class="valueLabel"><fmt:message key="datasource_config_editor_type"/> </label>
                            </td>
                            <td>
                                <select dojoType="dijit.form.Select" id="alpine_create_datasource_4toolbutton_datasourceType">
                                    <option value="DATABASE">Database</option>
                                    <option value="HADOOP">Hadoop</option>
                                </select>
                            </td>
                        </tr>
                        <tr style="display:none;">
                            <td>
                                <label class="valueLabel"><fmt:message key="TYPE" /> </label>
                            </td>
                            <td>
                                <select dojoType="dijit.form.Select" id="alpine_create_datasource_4toolbutton_editor_type">
                                    <option value="Public">Public</option>
                                    <option value="Group">Group</option>
                                    <option value="Personal">Personal</option>
                                </select>
                            </td>
                        </tr>
                        <tr style="display: none;">
                            <td>
                                <label class="valueLabel"><fmt:message key="GroupName" /></label>
                            </td>
                            <td>
                                <select id="alpine_create_datasource_4toolbutton_group" dojoType="dijit.form.Select" disabled="true"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label class="valueLabel"><fmt:message key="datasource_config_edit_connname"/></label>
                            </td>
                            <td>
                                <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_connection_name" style="width:150px">
                            </td>
                        </tr>
                    </table>
                </div>
                <div dojoType="dijit.layout.ContentPane" region="center">
                    <div dojoType="dijit.layout.StackContainer" id="alpine_create_datasource_4toolbutton_contentSwitcher">
                        <div dojoType="dijit.layout.ContentPane" id="alpine_create_datasource_4toolbutton_db_content">
                            <div dojoType="dijit.form.Form" id="alpine_create_datasource_4toolbutton_db_form">
                                <table width="100%" class="innerPadding">
                                    <tr>
                                        <td width="125px">
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_dbtype"/></label>
                                        </td>
                                        <td>
                                            <select dojoType="dijit.form.Select" id="alpine_create_datasource_4toolbutton_dbType">
                                                <option value="PostgreSQL">PostgreSQL</option>
                                                <option value="Greenplum">Greenplum</option>
                                                <option value="Oracle">Oracle</option>
                                                <option value="DB2">DB2</option>
                                                <option value="Netezza">Netezza</option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_host"/> </label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_db_host" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_port"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_db_port" style="width:150px"
                                                   data-dojo-props="regExp:'[\\d]+', invalidMessage:'<fmt:message key="dbconn_config_edit_port_tip" />'">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_dbname"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_db_name" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_username"/> </label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_db_username" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="dbconn_config_edit_password"/> </label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_db_password" type="password" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td></td>
                                        <td>
                                            <input dojoType="dijit.form.CheckBox" id="alpine_create_datasource_4toolbutton_db_usessl" disabled="disabled">
                                            <fmt:message key='dbconn_config_edit_useSSL'/>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>

                        <div dojoType="dijit.layout.ContentPane" id="alpine_create_datasource_4toolbutton_hadoop_content" style="overflow: auto;">
                            <div dojoType="dijit.form.Form" id="alpine_create_datasource_4toolbutton_hadoop_form">
                                <table width="100%" class="innerPadding">
                                    <tr>
                                        <td width="125px">
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_hdfs_hostname"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_hadoop_hdfs_host" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_hdfs_port"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_hadoop_hdfs_port" regExp="^[0-9]*$" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_job_hostname"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_hadoop_job_host" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_job_port"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_hadoop_job_port" regExp="^[0-9]*$" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_version"/></label>
                                        </td>
                                        <td>
                                            <select dojoType="dijit.form.Select" required="true" id="alpine_create_datasource_4toolbutton_hadoop_version"></select>
                                        </td>
                                    </tr>
                                    <tbody style="display : <%=(HadoopConnection.CURRENT_HADOOP_VERSION.equals(HadoopConstants.VERSION_APACHE_1_0_2)||HadoopConnection.CURRENT_HADOOP_VERSION.equals(HadoopConstants.VERSION_APACHE_1_0_4)) ? "''" : "none"%>">
                                    <%--<tbody>--%>
	                                    <tr>
	                                        <td>
                                                <input type="hidden" value="<%=HadoopConnection.CURRENT_HADOOP_VERSION%>" id="toolbutton_current_hadoop_version" />
                                                <%--<input type="hidden" value="Apache Hadoop 1.0.2" id="toolbutton_current_hadoop_version" />--%>
	                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_securityMode"/></label>
	                                        </td>
	                                        <td>
	                                            <select dojoType="dijit.form.Select" id="alpine_create_datasource_4toolbutton_hadoop_securityMode">
	                                            	<option value="simple">Simple</option>
	                                            	<option value="kerberos">Kerberos</option>
	                                            </select>
	                                        </td>
	                                    </tr>
	                               		<tr>
	                               			<td>
	                               				<label class="valueLabel"><fmt:message key="hadoopconn_config_edit_hdfsPrincipal"/></label>
	                               			</td>
	                               			<td>
	                               				<input dojoType="dijit.form.ValidationTextBox" disabled="true" required="true" trim="true" id="alpine_create_datasource_4toolbutton_hadoop_hdfsPrincipal" style="width:150px">
	                               			</td>
	                               		</tr>
	                               		<tr>
	                               			<td>
	                               				<label class="valueLabel"><fmt:message key="hadoopconn_config_edit_hdfsKeytab"/></label>
	                               			</td>
	                               			<td>
	                               				<input dojoType="dijit.form.ValidationTextBox" disabled="true" required="true" trim="true" id="alpine_create_datasource_4toolbutton_hadoop_hdfskeytab" style="width:150px">
	                               			</td>
	                               		</tr>
	                               		<tr>
	                               			<td>
	                               				<label class="valueLabel"><fmt:message key="hadoopconn_config_edit_mapredPrincipal"/></label>
	                               			</td>
	                               			<td>
	                               				<input dojoType="dijit.form.ValidationTextBox" disabled="true" required="true" trim="true" id="alpine_create_datasource_4toolbutton_hadoop_mapredPrincipal" style="width:150px">
	                               			</td>
	                               		</tr>
	                               		<tr>
	                               			<td>
	                               				<label class="valueLabel"><fmt:message key="hadoopconn_config_edit_mapredKeytab"/></label>
	                               			</td>
	                               			<td>
	                               				<input dojoType="dijit.form.ValidationTextBox" disabled="true" required="true" trim="true" id="alpine_create_datasource_4toolbutton_hadoop_mapredkeytab" style="width:150px">
	                               			</td>
	                               		</tr>
                               		</tbody>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_username"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_hadoop_userName" regExp="^[\w]+" style="width:150px">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label class="valueLabel"><fmt:message key="hadoopconn_config_edit_groupName"/></label>
                                        </td>
                                        <td>
                                            <input dojoType="dijit.form.ValidationTextBox" required="true" trim="true" id="alpine_create_datasource_4toolbutton_hadoop_groupName" regExp="^[\w]+" style="width:150px">
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="whiteDialogFooter">
			<input type="hidden" id="alpine_create_datasource_4toolbutton_hadoop_createUser">
            <button type="button" baseclass="cancelButton" dojoType="dijit.form.Button" id="alpine_create_datasource_4toolbutton_button_cancel"><fmt:message key="Cancel"/></button>
            <button type="button" baseclass="secondaryButton" dojoType="dijit.form.Button" id="alpine_create_datasource_4toolbutton_button_test"><fmt:message key="datasource_config_button_test_connect"/></button>
			<button type="button" baseclass="primaryButton" dojoType="dijit.form.Button" id="alpine_create_datasource_4toolbutton_button_save"><fmt:message key="OK"/></button>
        </div>
	</div>

	<!-- import dialog -->
    <div dojoType="dijit.Dialog" draggable="false" id="import_dbDriver_dlg" title="<fmt:message key='Import_JDBC_Driver'/>"
		 style="width: 360px;">
        <div class="titleBar">
            <fmt:message key='Import_JDBC_Driver'/>
        </div>
        <div class="innerPadding">
            <div dojoType="dijit.layout.ContentPane" id="fimport_dbDriver_content"
                 selected="true"  style="width: 340px; height: 138px">
                <form  class="innerPadding" name="frmIO_dbdriver" id="frmIO_dbdriver" enctype="multipart/form-data"
                       method="post">
                    <font id="upload_DBDriver_Error" color="red" ></font> <br/>
                    <input type="file" name="dbDriverFile" id="dbDriverFile" SIZE="30" style="height: 22px"><br/>
                    <div id ="db_progress_db_driver" dojoType="dijit.layout.ContentPane"  region="center" height =20px width =320px>
                    </div><br/>
                </form>
            </div>
        </div>
        <div class="whiteDialogFooter">
            <button dojoType="dijit.form.Button" type="button" baseclass="cancelButton" align="right" id="import_driver_cancel_btn_id" onclick="dijit.byId('import_dbDriver_dlg').hide();"><fmt:message key='Cancel'/></button>
            <button dojoType="dijit.form.Button" type="button" baseclass="primaryButton" align="right" id="upload_driver_btn_id"><fmt:message key='Upload'/></button>
        </div>
	</div>
</fmt:bundle>
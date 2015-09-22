<script type="text/javascript"
	src="../../js/alpine/system/udf.js" charset="utf-8"></script>

<fmt:bundle basename="app">

<div dojoType="dijit.Dialog" draggable="false"
		id="udf_dialog" title="<fmt:message key='UDF_TITLE' />">
    <div class="titleBar">
        <fmt:message key='UDF_TITLE'/>
    </div>
    <div class="innerPadding">
        <div dojoType="dijit.layout.LayoutContainer" style="width: 540px; height: 300px">
            <div dojoType="dijit.layout.ContentPane" style="width: 100%; height: 78%" region="center">

                <table id="udf_table" dojoType="dojox.grid.DataGrid"
                        query="{ operatorname: '*' }" clientSort="true"

                        onSelected= "select_udf"
                        style="width: 100%; height: 100%">
                    <thead>
                        <tr >
                            <th width="40%" field="operatorName"><fmt:message key="Operator_Name" /></th>
                            <th width="40%" field="udfSchema"><fmt:message key="UDF_SCHEMA" /></th>
                            <th width="40%" field="udfName"><fmt:message key="UDF_NAME"/></th>

                        </tr>
                    </thead>
                    <div dojoType="dijit.Menu" id="udf_rowMenu" jsId="udf_rowMenu"  style="display: none;">
                        <div id ="menu_udf_delete" dojoType="dijit.MenuItem" onClick="perform_delete_udf()"><fmt:message key="delete_button" /></div>

                    </div>
                </table>
            </div>
            <div dojoType="dijit.layout.ContentPane" style="width: 100%; height: 19%" region="bottom">
                <form name="frmIO_udf" id="frmIO_udf" enctype="multipart/form-data" 	method="POST">
                    <label> <fmt:message key="UDF_File"/> </label>
                    <input type="file" name="fFile_udf"  id="fFile_udf"  size= "48"/>

                </form>
            </div>
        </div>
    </div>
    <div class="whiteDialogFooter">
        <button   dojoType="dijit.form.Button" type="button"     baseclass="cancelButton"
            onclick="dijit.byId('udf_dialog').hide();">
            <fmt:message key="Done" />
        </button>
        <button   id="btn_udf_delete"   baseclass="secondaryButton"
            dojoType="dijit.form.Button" type="button"
            disabled = true
            onclick="perform_delete_udf()">
            <fmt:message key="delete_button" />
        </button>
        <button   dojoType="dijit.form.Button" type="button" id = "btn_do_udf_upload"  baseclass="primaryButton"
                onClick="do_udf_upload(1);"><fmt:message key="import_udf_tip" />
        </button>
     </div>
</div>
	 
</fmt:bundle>

<script type="text/javascript">
    dojo.require("alpine.flow.HadoopDefineAggregateHelper");
</script>
<fmt:bundle basename="app">
    <div dojoType="dijit.Dialog" draggable="false" title="<fmt:message key='hadoop_aggregate_define_agg_dlg_title'/>" id="alpine_props_hadoop_define_agg_dialog">
		<div class="titleBar">
      		<fmt:message key='hadoop_aggregate_define_agg_dlg_title'/>
      	</div>
      <div dojoType="dijit.layout.ContentPane" style="width:620px;height: 560px;overflow:hidden;">
         <div style="height:230px;width:100%;">
             <fieldset style="height:210px;margin:10px 10px 5px; padding:5px;border:1px solid #d0d0bf;"><legend style="margin-left:15px;"><fmt:message key="hadoop_agg_groupby_title" /> </legend>
                   <div id="hadoop_agg_groupby_src_container" style="float:left;height:190px;width:220px;overflow: hidden;margin: 0px 12px;">
                       <select id="hadoop_agg_groupby_src_select" name="select" size="10" style="height: 190px;width:220px"></select>
                   </div>
                   <div style="float:left;height:190px;width:40px;overflow: hidden;margin: 0px 0px;vertical-align:middle;position:relative;">
                       <span style="position: absolute;left: 0px;top: 50px">
                       <div dojoType="dijit.form.Button" type="button" id="alpine_props_hadoop_agg_to_right" baseClass="arrowButton">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>
                       </span>
                       <span style="position: absolute;left: 0px;top: 110px">
                       <div dojoType="dijit.form.Button" type="button" id="alpine_props_hadoop_agg_to_left"  baseClass="arrowButton">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>
                       </span>

                   </div>
                   <div id="hadoop_agg_groupby_dst_container" style="float:left;height:190px;width:220px;overflow: hidden;margin: 0px 12px;">
                       <select id="hadoop_agg_groupby_dst_select" name="select" size="10" style="height: 190px;width:220px" multiple="multiple"></select>
                   </div>
                   <div style="float:left;height:190px;width:40px;overflow: hidden;margin: 0px 0px;position: relative;">
                     <span style="position: absolute;left: 0px;top: 30px;">
                       <div dojoType="dijit.form.Button" type="button" id="alpine_props_hadoop_agg_group_moveup"  baseClass="arrowButton">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>
                       </span>
                     <span style="position: absolute;left: 0px;top:80px;">
                       <div dojoType="dijit.form.Button" type="button" id="alpine_props_hadoop_agg_group_movedown"  baseClass="arrowButton">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>
                       </span>
                     <span style="position: absolute;bottom:0px;left: 0px">
                       <div dojoType="dijit.form.Button" type="button" id="alpine_props_hadoop_agg_group_clear"  baseClass="arrowButton">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>
                       </span>
                   </div>
             </fieldset>
         </div>
         <div style="height:275px;clear: both;">
             <fieldset style="height:255px;margin:0px 10px 10px 10px; padding:5px;border:1px solid #d0d0bf;"><legend style="margin-left:15px;"><fmt:message key="hadoop_aggregate_create_agg_title" /> </legend>
                  <div style="float: left;height:235px;width: 230px;float: left;margin:0px 10px;">
                      <select name="hadoop_aggFunctionSelect" id="hadoop_aggFunctionSelect" dojoType="dijit.form.Select" style="width:225px;">
                          <option value="COUNT" selected="selected">COUNT</option>
                          <option value="SUM" >SUM</option>
                          <option value="AVG">AVG</option>
                          <option value="MIN">MIN</option>
                          <option value="MAX">MAX</option>
                      </select>
                      <div style="height: 195px;margin:6px 0px" id="hadoop_aggFunctionSelect_columList_container">
                          <select id="hadoop_aggFunctionSelect_column_name" name="hadoop_aggFunctionSelect" size="10" style="height:195px;width:227px"></select>
                      </div>
                  </div>
                 <div style="float: left;height:235px;width: 45px;float: left;margin:0px 0px;position: relative;">
                     <span style="position: absolute;left: 0px;top: 60px">
                       <div dojoType="dijit.form.Button" type="button" id="alpine_props_hadoop_agg_method_to_right"  baseClass="arrowButton">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>
                    </span>
                 </div>
                 <div style="float: left;height:235px;width: 230px;float: left;margin:0px 10px;">
                     <input type="text" name="hadoop_agg_Alias" id="hadoop_agg_Alias"  dojoType="dijit.form.ValidationTextBox" style="width:225px;height:20px">
                     <input type="hidden" id="hadoop_agg_selected_row_idx" />
                     <div style="height: 220px;margin:5px 0px" id="hadoop_agg_method_grid_container"></div>
                 </div>
                 <div style="float: left;height:230px;width: 35px;float: left;margin:0px 0px;position: relative;">
                     <span style="position: absolute;bottom:0px;left: 0px">
                       <div dojoType="dijit.form.Button" type="button" id="alpine_props_hadoop_agg_method_clear"  baseClass="arrowButton">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>
                      </span>
                 </div>
             </fieldset>
         </div>
          <div style="width:100%;height:40px;" class="whiteDialogFooter">
              <div baseClass="cancelButton" dojoType="dijit.form.Button" type="button" id="alpine_props_hadoop_define_agg_dialog_Btn_Cancel"><fmt:message key="Cancel"/></div>
              <div baseClass="primaryButton" dojoType="dijit.form.Button" type="button" id="alpine_props_hadoop_define_agg_dialog_Btn_Done"><fmt:message key="OK"/></div>
          </div>
      </div>
    </div>
</fmt:bundle>
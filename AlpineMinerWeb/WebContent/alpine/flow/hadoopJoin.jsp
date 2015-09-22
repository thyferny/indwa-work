<script type="text/javascript">
    dojo.require("alpine.props.JoinHadoopPropertyHelper");
</script>
<fmt:bundle basename="app">
    <div dojoType="alpine.layout.PopupDialog" draggable="false"		id="hadoopJoinDialog" title='<fmt:message key="Edit_Table_Join" />'>
        <div dojoType="dijit.layout.ContentPane" style="width: 900px;height:300px">
            <div dojoType="dijit.layout.LayoutContainer" class="bottomborder">
                <div dojoType="dijit.layout.ContentPane" style="width:40%;height:100%" id="hadoop_join_tables_toggle" region="left">
                </div>
                <div dojoType="dijit.layout.ContentPane" style="width:60%; height:100%; border-left: 1px solid #cecdcd; padding-left: 3px;" region="center">
                    <div dojoType="dijit.layout.LayoutContainer" style="width: 100%; height: 100%;">
                        <div dojoType="dijit.layout.ContentPane" region="top">
                            <div  class="tablejointableheader"><fmt:message key="Table_Join_Selected_Columns" /></div>
                        </div>
                        <div dojoType="dijit.layout.ContentPane" region=center  id="hadoop_join_selected_columns_holder"></div>
                        <div dojoType="dijit.layout.ContentPane" region=bottom  id=""></div>
                    </div>
                </div>
            </div>
    </div>

    <div dojoType="dijit.layout.ContentPane" style="width:900px;height:300px;position:relative;">



        <div id="hj_join_options">
            <div class="tablejointableheader"><fmt:message key="hadoop_join_set_condition_choose_type" /> </div>
            <div class="checkboxList"><input id="hj_left" data-dojo-type="dijit.form.CheckBox" value="LEFT_JOIN">
                <label for="hj_left" id="hj_left_label"></label>
            </div>
            <div class="checkboxList"><input id="hj_right" data-dojo-type="dijit.form.CheckBox" value="RIGHT_JOIN"> <label for="hj_right"  id="hj_right_label"></label>
            </div>
        </div>


        <div style="width: 100%">
                <div class="tablejointableheader"><!--<fmt:message key="hadoop_join_set_condition_legend" />--><fmt:message key="hadoop_join_choose_columns_for_matching" /></div>
            <!--<span class="operatorSubText" id="hadoop_join_helper_text">Match rows across tables using the selected columns.</span>-->
            <div id="joinSelectors">

                </div>


            </div>
            <div></div>
            <div style="position: absolute;bottom:0px;left:0;width:880px;height:30px;padding-top:10px;"
                 class="whiteDialogFooter">
                <button type="button" baseClass="cancelButton" dojoType="dijit.form.Button"
                        id="hadoopJoin_dlg_btn_Cancel"><fmt:message key="Cancel"/></button>
                <button type="button" baseClass="primaryButton" dojoType="dijit.form.Button" id="hadoopJoin_dlg_btn_OK">
                    <fmt:message key="OK"/></button>
            </div>
        </div>
    </div>

</fmt:bundle>

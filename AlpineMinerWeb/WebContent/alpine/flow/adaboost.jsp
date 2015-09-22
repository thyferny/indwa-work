
<style type="text/css">

#adaboostDialogContainer {
	width: 260px;
	height: 80%;
}
</style>

<script type="text/javascript">
    dojo.require("alpine.props.adaboost");
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" id="adaboostDialog"
		title="<fmt:message key='adaboost_param_edit_title'/>">
		<div class="titleBar">
            <fmt:message key='adaboost_param_edit_title'/>
        </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width: 800px; height: 520px;">
			
			<div dojoType="dijit.layout.ContentPane" 
					style="width: 280px; height: 100%" layoutAlign="left">
				
				<div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%;">
					<table width="100%" >
						<tr><td height="10px"></td></tr>
						<tr><td>
							<label><fmt:message key="Add" />&nbsp;&nbsp;</label> 
								
							<button data-dojo-type="dijit.form.ComboButton"
								id="add_ada_op_button"
								baseClass="greyDropdownButton"
								type="button"
                                style="width:auto;"
								data-dojo-props='
								onClick:function(){ 
								var op = dijit.byId("add_ada_op_button").label;
								alpine.props.adaboost.add_adaboost_operator(op);
						}
								'>
					<span><fmt:message key="Naive_Bayes" /></span> 
					<span data-dojo-type="dijit.Menu"> 
					
					<span data-dojo-type="dijit.MenuItem"
							data-dojo-props=' onClick:function(){ 
							alpine.props.adaboost.add_adaboost_operator(alpine.nls.Naive_Bayes);
							dijit.byId("add_ada_op_button").set("label", alpine.nls.Naive_Bayes);
		               	}
		               '><fmt:message key="Naive_Bayes" /></span> 
		               
		               <span data-dojo-type="dijit.MenuItem"
							data-dojo-props=' onClick:function(){ 
							alpine.props.adaboost.add_adaboost_operator(alpine.nls.Decision_Tree);
							dijit.byId("add_ada_op_button").set("label", alpine.nls.Decision_Tree);
							
		               	}
		               '><fmt:message key="Decision_Tree" /> </span>
		               <span data-dojo-type="dijit.MenuItem"
							data-dojo-props=' onClick:function(){ 
							alpine.props.adaboost.add_adaboost_operator(alpine.nls.Logistic_Regression);
							dijit.byId("add_ada_op_button").set("label", alpine.nls.Logistic_Regression);
		               	}
		               '><fmt:message key="Logistic_Regression" /> </span>
		               <span data-dojo-type="dijit.MenuItem"
							data-dojo-props=' onClick:function(){ 
							alpine.props.adaboost.add_adaboost_operator(alpine.nls.SVM_Classification);
							dijit.byId("add_ada_op_button").set("label", alpine.nls.SVM_Classification);
		               	}
		               '><fmt:message key="SVM_Classification" />  </span>
		               <span data-dojo-type="dijit.MenuItem"
		               		id="adaboost_cart_tree"
							data-dojo-props=' onClick:function(){ 
							alpine.props.adaboost.add_adaboost_operator(alpine.nls.Cart_Tree);
							dijit.byId("add_ada_op_button").set("label", alpine.nls.Cart_Tree);
		               	}
		               '><fmt:message key="Cart_Tree" /></span>
		               <span data-dojo-type="dijit.MenuItem"
		               		id="adaboost_neural_network"
							data-dojo-props=' onClick:function(){ 
							alpine.props.adaboost.add_adaboost_operator(alpine.nls.Neural_Network);
							dijit.byId("add_ada_op_button").set("label", alpine.nls.Neural_Network);
		               	}
		               '><fmt:message key="Neural_Network" /> </span>
		            
		               </span>
					</button></td></tr>
					
						<tr><td height="10px"></td></tr>
							
					</table>
				</div>
				
				<div dojoType="dijit.layout.BorderContainer" design="sidebar"
					gutters="true" liveSplitters="true" 
					id="adaboostDialogContainer">
							
					<div dojoType="dijit.layout.ContentPane" splitter="true"
						region="center">
		
						<div dojoType="dijit.layout.LayoutContainer"
							style="width: 100%; height: 100%">
		
							<div dojoType="dijit.layout.ContentPane" id="adaboost_param_gridpane"
								layoutAlign="client" style="height: 100%;"></div>
		
							<div dojoType="dijit.layout.ContentPane"
								id="adaboost_param_buttonpane" layoutAlign="right"
								style="width: 70px;">
								<div dojoType="dijit.form.Form" id="adaboost_paramForm"
									jsId="adaboost_paramForm" encType="multipart/form-data" action=""
									method="">
		
									<table cellspacing="20" width="100%">
										<tr>
											<td align="center">
											<a onClick="alpine.props.adaboost.move_up_adaboost_param()" >
												
												<img src="<%=path%>/images/up_arrow.png"
														width="24" height="24" border="1"  /> 
												</a>											
											</td>
										</tr>
										<tr>
											<td align="center">
												<a onClick="alpine.props.adaboost.move_down_adaboost_param()" >
												<img src="<%=path%>/images/down_arrow.png"
														width="24" height="24" border="1"  /> 
												</a>											
											</td>
										</tr>
										<tr>
											<td align="center">
												<a onClick="alpine.props.adaboost.delete_adaboost_param()" >
													<img src="<%=path%>/images/delete-icon.png"
														width="24" height="24"   /> 
												</a>
											</td>
										</tr>
									</table>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
				
			<div dojoType="dijit.layout.ContentPane" layoutAlign="center" style="height: 100%" >
				<div dojoType="dijit.layout.LayoutContainer" id="adaboostFormDialogCont">
	
					<div dojoType="dijit.layout.ContentPane"
						id="adaboost_title_pane_top" layoutAlign="top" style="height: 50px">
						<table align="left">
							<tr>
								<td align="center"><img
									src="<%=path%>/images/icons/adaboost.png" width="40"
									height="40" border="1" id="adaboost_operator_icon" />
								</td>
								<td width="10px" />
								<td align="right"><label
									id="adaboost_operator_name" for="">
									<fmt:message key="adaboost_param_title"/>
									</label></td>
							</tr>
						</table>	
								
					</div>
	
					<div dojoType="dijit.layout.ContentPane" layoutAlign="client">
						<hr>
						<div dojoType="dijit.form.Form" id="adaboostForm"
							jsId="adaboostForm">
							<table cellspacing="6" id="adaboostTable">
								<!-- build the from at run time -->
							</table>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="whiteDialogFooter">
			<button dojoType="dijit.form.Button" type="button" baseClass="cancelButton"
				onClick="dijit.byId('adaboostDialog').hide();">
				<fmt:message key="Cancel" />
			</button>
			<button dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
				onClick="alpine.props.adaboost.update_adaboost_param_data()">
				<fmt:message key="OK" />
			</button>
		</div>
	</div>
</fmt:bundle>
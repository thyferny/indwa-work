/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * RecentlyHistoryFlowUIHelper
 * Author Gary
 */
define(["alpine/flow/RecentlyHistoryFlowManager",
        "alpine/flow/WorkFlowUIHelper"], function(recentlyHistoryFlowManager, workFlowUIHelper){
	var userName = alpine.USER;
	dojo.ready(function(){
		dojo.connect(dijit.byId("recently_history_button"), "onFocus", function(){
			var historyMenu = dijit.byId("recently_history_menu");
			historyMenu.getChildren().forEach(function(item, i){
				historyMenu.removeChild(item);
			});

			var records = recentlyHistoryFlowManager.loadHistoryByUser(userName);
			var clearHistory = new dijit.MenuItem({
				label: alpine.nls.history_clear_menu,
				style: "font-size: 12px;backgound: #777777",
				onClick: function(){
					recentlyHistoryFlowManager.removeAllHistoryFromUser(userName, function(){
						// nothing to do.
					}, function(msg){
//						popupComponent.alert(text);
					});
				}
			});
			if(records == null || records.length == 0){
				var emptyMenuItem = new dijit.MenuItem({
					label: alpine.nls.recent_history_empty_item,
					style: "font-size: 12px;backgound: #777777"
				});
				historyMenu.addChild(emptyMenuItem);
			}else{
				for(var i = records.length - 1;i >= 0; i--){
					var item = records[i];
					var menuItem = new dijit.MenuItem({
						label: item.displayText,
						title: item.displayText,
						style: "font-size: 12px;backgound: #777777",
						showTitle: true,
						data: item,
						onClick: function(){
							workFlowUIHelper.openWorkFlow(this.data);
						}
					});
					historyMenu.addChild(menuItem);
				}
				historyMenu.addChild(new dijit.MenuSeparator());
				historyMenu.addChild(clearHistory);
			}
			historyMenu.startup();
		});
	});
});
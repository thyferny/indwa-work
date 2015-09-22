/**
 *  Spinner.js
 */

define(["alpine/layout/StandbySpinner","dojo/query"], function(StandbySpinner, query)
{

    /**
     * will show the spinner on top of parent panel, given the parent's id, assuming there's not already a spinner there
     * @param parentPanelId
     * @private
     */
    function _showSpinner(parentPanelId)
    {
        var alreadySpinning = false;
        query("[id^='alpine_layout_StandbySpinner']")
            .forEach(function (node) {
                var parentId  =  dijit.byId(node.id).target.id;
                if (parentId == parentPanelId)
                {
                    alreadySpinning = true;
                }
            });
        if (!alreadySpinning)
        {
            console.debug("creating new spinner");
            var standby =  new StandbySpinner({target: parentPanelId});
            standby.startup();
            document.body.appendChild(standby.domNode);
            standby.show();
        } else
        {
            console.debug("spinner already there!");
        }
    }

    /**
     * will hide any spinners currently showing on the parent Panel
     * @param parentPanelId
     * @private
     */
    function _hideSpinner(parentPanelId)
    {
        query("[id^='alpine_layout_StandbySpinner']")
            .forEach(function (node) {
                var currentNode = dijit.byId(node.id);
                var parentId  =  currentNode.target.id;
                if (parentId == parentPanelId)
                {
                    console.debug("found the spinner - ready to remove it!");
                    currentNode.hide();
                    currentNode.destroy();
                }
            });
    }

    function _spinnerExists(parentPanelId)
    {
        var alreadySpinning = false;
        query("[id^='alpine_layout_StandbySpinner']")
            .forEach(function (node) {
                var parentId  =  dijit.byId(node.id).target.id;
                if (parentId == parentPanelId)
                {
                    alreadySpinning = true;
                }
            });

        return alreadySpinning;
    }

    function _hideAllSpinners()
    {
        query("[id^='alpine_layout_StandbySpinner']")
            .forEach(function (node) {
                var currentNode = dijit.byId(node.id);
                var parentId  =  currentNode.target.id;
                    currentNode.hide();
                    currentNode.destroy();
            });
    }


    return {
        showSpinner: _showSpinner,
        hideSpinner: _hideSpinner,
        spinnerExists: _spinnerExists,
        hideAllSpinners: _hideAllSpinners
    };
});

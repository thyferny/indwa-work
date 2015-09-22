/**
 * User: robbie
 * Date: 1/15/13
 */


define([], function () {

    var chorusDO = baseURL + "/main/chorus.do";

    var constants = {
        URL_POSTBACK: chorusDO + "?method=postComment"
    };

    function _postBack(info, callback, errorCallback, callbackPaneId) {
        var url = constants.URL_POSTBACK;
        if (info['checked']) { url += "&is_insight=1"; } else { url += "&isInsight=0"; }
        if (info['entity_type']) { url += "&entity_type=" + encodeURIComponent(info['entity_type']); }
        if (info['entity_id']) { url += "&entity_id=" + encodeURIComponent(info['entity_id']); }
        ds.post(url, info, callback, errorCallback, false, callbackPaneId);
    }

    return {
        postBack: _postBack
    };
});
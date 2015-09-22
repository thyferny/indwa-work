/**
 * User: sara/gary
 * Date: 6/29/12
 * Time: 10:39 AM
 */

define(["dojo/_base/fx"],function(fx){

//    function _slideToLeft(containerDivId, contentDivId, callback) {
//        var box = dojo.byId(containerDivId);
//        var content = dojo.byId(contentDivId);
//        //     dojo.fadeOut({
//          dojo.animateProperty({
//            node:content,
//            properties:{
//                marginLeft:{end:0 - box.offsetWidth },
//                marginRight:{end:box.offsetWidth }
//            },
//            duration:800 ,
//            onEnd: function(){
//                console.log("finished animation");
//                callback();
//            }
//        }).play();
//    }



    function _slideFromRight(contentDijitId, callback) {
        var content = dojo.byId(contentDijitId);
        var toploc = dijit.byId(contentDijitId).t;
        var contentwidth = dijit.byId(contentDijitId).w;
        fx.slideTo({
           node:content,
            duration:800,
            top: toploc,
            left:{start: contentwidth, end:0},
            onEnd: function(){
                if (callback) callback();
            }
        }).play();
    }

    return {
//        slideToLeft:_slideToLeft,
        slideFromRight:_slideFromRight
    };
});

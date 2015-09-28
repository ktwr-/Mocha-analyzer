/*イベントリスナのセット関数オブジェクト*/
var addListener = function(elm, type, func){
	if (! elm){ return false; }
	if(elm.addEventListener) {
		elm.addEventListener(type, func, false);
	}else if(elm.attachEvent) { 
		elm.attachEvent('on'+type, func);
	}else {
		return false;
	}
	return true;
};

vat init = function() {
	var div = document.getElemetByID('box');
	var popup = function () { alert("clicked!"); };
	addListener(div,"click",popup);
};

addListener(window,"load",init);

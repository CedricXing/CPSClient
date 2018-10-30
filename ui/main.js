function Resize() {
    var curWidth = document.documentElement.clientWidth;
    var curHeight = document.documentElement.clientHeight;
    //document.getElementById("DEBUG").innerHTML=curHeight+","+curWidth;
    $("#ROW").css("height", curHeight);
    $("#myCanvas").css("width", $("#COL2").width());
}

var source = new EventSource('http://127.0.0.1:8844/stream');
var div = document.getElementById('StatusContent');
var cnt = 0;
const kMaxStatus = 10;

source.onopen = function(event) {
    div.innerHTML = '<p>Connection open ...</p>';
};

source.onerror = function(event) {
    div.innerHTML += '<p>Connection close.</p>';
};

source.addEventListener('connecttime', function(event) {
    div.innerHTML += ('<p>Start time: ' + event.data + '</p>');
}, false);

source.onmessage = function(event) {
	if (cnt == kMaxStatus) {
		cnt = 0;
	}
	++cnt;
    name = 'Position' + cnt;
    var div_cur = document.getElementById('Position_'+cnt);
    div_cur.innerHTML = 'Ping: ' + event.data;
};
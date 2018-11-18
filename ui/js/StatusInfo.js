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

source.addEventListener('position', function(event) {
    if (cnt == kMaxStatus) {
        cnt = 0;
        clearTable();
    }
    ++cnt;
    var div_cur = document.getElementById('Position_' + cnt);
    var position = event.data.toString();
    var position_list = position.split(" ");
    var 
        period = position_list[0],
        offset = position_list[2];
    div_cur.innerHTML = "(" + period + "," + offset + ")";
    updateCarPosition(parseInt(period), parseInt(offset));
}, false);

source.addEventListener('velocity', function(event) {
    var div_cur = document.getElementById('Velocity_' + cnt);
    div_cur.innerHTML = event.data;
    updateData(parseInt(event.data));
}, false);

/*source.addEventListener('status', function(event) {
    var div_cur = document.getElementById('Status_' + cnt);
    div_cur.innerHTML = event.data;
}, false);*/

// source.onmessage = function(event) {
// 	if (cnt == kMaxStatus) {
// 		cnt = 0;
// 	}
// 	++cnt;
//     var div_cur = document.getElementById('Position_'+cnt);
//     div_cur.innerHTML = event.data;
//     updateCarPosition();
// };


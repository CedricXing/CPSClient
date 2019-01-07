var source_status = new EventSource('http://127.0.0.1:8844/stream');

var div = document.getElementById('StatusContent');
var cnt = 0;
const kMaxStatus = 10;

var position;
var velocity;

source_status.onopen = function(event) {
    console.log("status source open");
};

source_status.onerror = function(event) {
    console.log("status source error");
};

source_status.addEventListener('initialization', function(event) {
    //  Get car number
    var div_car_number = document.getElementById('CarNumber');

    car_number = parseInt(event.data);

    //  Draw the dropdown box
    drawDropdownBox(car_number);

    //  Create arrays which hold status information
    position = new Array(car_number + 1);
    velocity = new Array(car_number + 1);

    div_car_number.innerHTML = car_number.toString();
}, false);

source_status.addEventListener('start', function(event) {
    if (cnt == kMaxStatus) {
        cnt = 0;
        clearTable();
    }
    ++cnt;
}, false);

var cur_cnt = 1;

source_status.addEventListener('carid', function(event) {
    car_id_update = parseInt(event.data);
}, false);

source_status.addEventListener('position', function(event) {
    position[car_id_update] = event.data.toString();

    var position_list = position[car_id_update].split(" ");
    var 
        period = position_list[0],
        offset = position_list[2];
    updateCarPosition(car_id_update, parseInt(period), parseInt(offset));
}, false);

source_status.addEventListener('velocity', function(event) {
    velocity[car_id_update] = event.data.toString();
    updateData(car_id_update,parseInt(event.data));
}, false);

source_status.addEventListener('finish', function(event) {
    //  update table info of current car after
    //  having received one whole round of information
    updateTableValue();
}, false);

const width_threshold = 480;
const
    y = 92,
    x = 50,
    radius = [70, 100],
    width = radius[0] - radius[1];
    r = (radius[0] + radius[1]) / 2,
    arclen = Math.PI / 2 * r;

var
    y_lvl_1 = [0, 0],
    y_lvl_2 = [0, 0],
    y_lvl_3 = [0, 0],
    y_lvl_4 = [0, 0],
    x_lvl_1 = [0, 0],
    x_lvl_2 = [0, 0],
    x_lvl_3 = [0, 0],
    x_lvl_4 = [0, 0];

x_lvl_1[0] = 265;
x_lvl_1[1] = x_lvl_1[0]+width;
y_lvl_1[0] = 60;
y_lvl_1[1] = y_lvl_1[0]+width;

var
    car_id_update = 1,
    car_id_display = 1;

for (var i = 0; i < 2; ++i) {
    y_lvl_2[i] = y_lvl_1[i] + radius[i];
    x_lvl_2[i] = x_lvl_1[i] + radius[i];
    y_lvl_3[i] = y_lvl_2[i] + y;
    x_lvl_3[i] = x_lvl_2[i] + x;
    y_lvl_4[i] = y_lvl_3[i] + radius[i];
    x_lvl_4[i] = x_lvl_3[i] + radius[i];
}

var
    x_left = (x_lvl_1[0] + x_lvl_1[1]) / 2,
    x_right = (x_lvl_4[0] + x_lvl_4[1]) / 2,
    y_up = (y_lvl_1[0] + y_lvl_1[1]) / 2,
    y_down = (y_lvl_4[0] + y_lvl_4[1]) / 2;

function drawTrack() {
    pos = new Array(2 * car_number);
    for (var i = 0; i < 2 * car_number; ++i) pos[i] = 0;

    var context = document.getElementById("track").getContext("2d");

    context.strokeStyle = "rgb(250,0,0)";
    context.fillStyle = "rgb(250,0,0)"

    context.beginPath();
    for (var i = 0; i < 2; ++i) {
        context.moveTo(x_lvl_1[i], y_lvl_2[i]);
        context.lineTo(x_lvl_1[i], y_lvl_3[i]);
        context.arcTo(x_lvl_1[i], y_lvl_4[i],
            x_lvl_2[i], y_lvl_4[i],
            radius[i]);
        context.lineTo(x_lvl_3[i], y_lvl_4[i]);
        context.arcTo(x_lvl_4[i], y_lvl_4[i],
            x_lvl_4[i], y_lvl_3[i],
            radius[i]);
        context.lineTo(x_lvl_4[i], y_lvl_2[i]);
        context.arcTo(x_lvl_4[i], y_lvl_1[i],
            x_lvl_3[i], y_lvl_1[i],
            radius[i]);
        context.lineTo(x_lvl_2[i], y_lvl_1[i]);
        context.arcTo(x_lvl_1[i], y_lvl_1[i],
            x_lvl_1[i], y_lvl_2[i],
            radius[i]);
    }
    context.stroke();
}

function updateTrack() {
    if (track) {
        track.options = optionsBar;
        track.update();
    }
}

function reloadPage() {
    setTimeout(function() {
        window.location.reload();
    }); // Reload the page so that charts will display correctly
}

var pos;

var color = ["", "Blue", "Green", "Red", "Black", "Yellow",
            "Navy", "Violet", "Olive", "Lime", "PaleGodenrod"];

function updateCarPosition(car_id, period, offset) {
    var context = document.getElementById("track").getContext("2d");

    var position = pos.slice(car_id * 2 - 2, car_id * 2);

    context.fillStyle = "white";
    context.strokeStyle = "white";
    context.fillRect(position[0]-2, position[1]-2, 8, 8);

    var ratio, sita;
    if (period & 1) {
        ratio = offset / arclen;
        sita = Math.PI / 2 * ratio;
        offset_cos = r * Math.cos(sita);
        offset_sin = r * Math.sin(sita);
    }

    switch (period) {
        case 0:
            position[0] = x_lvl_2[0] + offset;
            position[1] = y_down;
            break;
        case 1:
            position[0] = x_lvl_3[0] + offset_sin;
            position[1] = y_lvl_3[0] + offset_cos;
            break;
        case 2:
            position[0] = x_right;
            position[1] = y_lvl_3[0] - offset;
            break;
        case 3:
            position[0] = x_lvl_3[0] + offset_cos;
            position[1] = y_lvl_2[0] - offset_sin;
            break;
        case 4:
            position[0] = x_lvl_3[0] - offset;
            position[1] = y_up;
            break;
        case 5:
            position[0] = x_lvl_2[0] - offset_sin;
            position[1] = y_lvl_2[0] - offset_cos;
            break;
        case 6:
            position[0] = x_left;
            position[1] = y_lvl_2[0] + offset;
            break;
        case 7:
            position[0] = x_lvl_2[0] - offset_cos;
            position[1] = y_lvl_3[0] + offset_sin;
            break;
    }

    context.fillStyle = color[car_id];
    context.strokeStyle = color[car_id];
    context.fillRect(position[0], position[1], 4, 4);

    for (var i = 0; i < 2; ++i) pos[car_id * 2 - (2 - i)] = position[i];
}

function clearTable() {
    for (var i = 1; i <= 10; ++i) {
        var div_position = document.getElementById('Position_' + i);
        var div_velocity = document.getElementById('Velocity_' + i);
        div_position.innerHTML = '';
        div_velocity.innerHTML = '';
    }
}

function changeCurrentCar() {
    clearTable();
    drawDropdownBox();
    console.log("hello " + document.getElementById("car_id").value);
    car_id_display = parseInt(document.getElementById("car_id").value);
}

function drawDropdownBox(car_number) {
    for (var car_id = 1; car_id <= car_number; ++car_id) {
        var option = document.createElement("option");
        option.text = car_id.toString();
        option.value = car_id.toString();
        var div_dropdown_box = document.getElementById("car_id");
        div_dropdown_box.options.add(option);
    }
}

function updateTableValue() {
    var div_position = document.getElementById('Position_' + cnt);
    var position_list = position[car_id_display].split(" ");
    var 
        period = position_list[0],
        offset = position_list[2];
    div_position.innerHTML = "(" + period + "," + offset + ")";

    var div_velocity = document.getElementById('Velocity_' + cnt);
    div_velocity.innerHTML = velocity[car_id_display];
}
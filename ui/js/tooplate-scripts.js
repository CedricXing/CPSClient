const width_threshold = 480;
const
    y = 210,
    x = 170,
    radius = [55, 40],
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

y_lvl_1[0] = x_lvl_1[0] = 0;
y_lvl_1[1] = x_lvl_1[1] = width;

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

function drawLineChart() {
    if ($("#lineChart").length) {
        ctxLine = document.getElementById("lineChart").getContext("2d");
        optionsLine = {
            scales: {
                yAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: "Hits"
                    }
                }]
            }
        };

        // Set aspect ratio based on window width
        optionsLine.maintainAspectRatio =
            $(window).width() < width_threshold ? false : true;

        configLine = {
            type: "line",
            data: {
                labels: [
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July"
                ],
                datasets: [{
                        label: "Latest Hits",
                        data: [88, 68, 79, 57, 56, 55, 70],
                        fill: false,
                        borderColor: "rgb(75, 192, 192)",
                        lineTension: 0.1
                    },
                    {
                        label: "Popular Hits",
                        data: [33, 45, 37, 21, 55, 74, 69],
                        fill: false,
                        borderColor: "rgba(255,99,132,1)",
                        lineTension: 0.1
                    },
                    {
                        label: "Featured",
                        data: [44, 19, 38, 46, 85, 66, 79],
                        fill: false,
                        borderColor: "rgba(153, 102, 255, 1)",
                        lineTension: 0.1
                    }
                ]
            },
            options: optionsLine
        };

        lineChart = new Chart(ctxLine, configLine);
    }
}

function drawTrack() {
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

function drawPieChart() {
    if ($("#pieChart").length) {
        ctxPie = document.getElementById("pieChart").getContext("2d");
        optionsPie = {
            responsive: true,
            maintainAspectRatio: false
        };

        configPie = {
            type: "pie",
            data: {
                datasets: [{
                    data: [4600, 5400],
                    backgroundColor: [
                        window.chartColors.purple,
                        window.chartColors.green
                    ],
                    label: "Storage"
                }],
                labels: ["Used: 4,600 GB", "Available: 5,400 GB"]
            },
            options: optionsPie
        };

        pieChart = new Chart(ctxPie, configPie);
    }
}

function updateChartOptions() {
    if ($(window).width() < width_threshold) {
        if (optionsLine) {
            optionsLine.maintainAspectRatio = false;
        }
        if (optionsBar) {
            optionsBar.maintainAspectRatio = false;
        }
    } else {
        if (optionsLine) {
            optionsLine.maintainAspectRatio = true;
        }
        if (optionsBar) {
            optionsBar.maintainAspectRatio = true;
        }
    }
}

function updateLineChart() {
    if (lineChart) {
        lineChart.options = optionsLine;
        lineChart.update();
    }
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

function drawCalendar() {
    if ($("#calendar").length) {
        $("#calendar").fullCalendar({
            height: 400,
            events: [{
                    title: "Meeting",
                    start: "2018-09-1",
                    end: "2018-09-2"
                },
                {
                    title: "Marketing trip",
                    start: "2018-09-6",
                    end: "2018-09-8"
                },
                {
                    title: "Follow up",
                    start: "2018-10-12"
                },
                {
                    title: "Team",
                    start: "2018-10-17"
                },
                {
                    title: "Company Trip",
                    start: "2018-10-25",
                    end: "2018-10-27"
                },
                {
                    title: "Review",
                    start: "2018-11-12"
                },
                {
                    title: "Plan",
                    start: "2018-11-18"
                }
            ],
            eventColor: "rgba(54, 162, 235, 0.4)"
        });
    }
}

var pos = [6, 55, 4, 4];

function updateCarPosition(period, offset) {
    var context = document.getElementById("track").getContext("2d");

    context.fillStyle = "white";
    context.strokeStyle = "white";
    context.fillRect(pos[0]-2, pos[1]-2, pos[2]+4, pos[3]+4);

    var ratio, sita;
    if (period & 1) {
        ratio = offset / arclen;
        sita = Math.PI / 2 * ratio;
        offset_cos = r * Math.cos(sita);
        offset_sin = r * Math.sin(sita);
    }

    switch (period) {
        case 0:
            pos[0] = x_lvl_2[0] + offset;
            pos[1] = y_down;
            break;
        case 1:
            pos[0] = x_lvl_3[0] + offset_sin;
            pos[1] = y_lvl_3[0] + offset_cos;
            break;
        case 2:
            pos[0] = x_right;
            pos[1] = y_lvl_3[0] - offset;
            break;
        case 3:
            pos[0] = x_lvl_3[0] + offset_cos;
            pos[1] = y_lvl_2[0] - offset_sin;
            break;
        case 4:
            pos[0] = x_lvl_3[0] - offset;
            pos[1] = y_up;
            break;
        case 5:
            pos[0] = x_lvl_2[0] - offset_sin;
            pos[1] = y_lvl_2[0] - offset_cos;
            break;
        case 6:
            pos[0] = x_left;
            pos[1] = y_lvl_2[0] + offset;
            break;
        case 7:
            pos[0] = x_lvl_2[0] - offset_cos;
            pos[1] = y_lvl_3[0] + offset_sin;
            break;
    }

    context.fillStyle = "blue";
    context.strokeStyle = "blue";
    context.fillRect(pos[0], pos[1], pos[2], pos[3]);
}

function clearTable() {
    for (var i = 1; i <= 10; ++i) {
        var div_position = document.getElementById('Position_' + i);
        var div_velocity = document.getElementById('Velocity_' + i);
        var div_status = document.getElementById('Status_' + i);
        div_position.innerHTML = '';
        div_velocity.innerHTML = '';
        div_status.innerHTML = '';
    }
}
var http = require("http");
var fs = require("fs");
var car_number;

http.createServer(function(req, res) {
    var fileName = "." + req.url;

    if (fileName === "./stream") {
        res.writeHead(200, {
            "Content-Type": "text/event-stream",
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "Access-Control-Allow-Origin": '*',
        });

        res.write("retry: 10000\n");

        fs.exists("CarNumber.txt", function(exists) {
            if (!exists) {
                console.log("Lack CarNubmer.txt");
                interval = setInterval(function() {
                    res.write("data: no car number\n\n");
                }, 1000);
            } else {
                fs.readFile("CarNumber.txt", "utf8", function(err, data) {
                    if (err) {
                        console.log("error in reading CarNumber.txt");
                    } else {
                        console.log("car number initialized: " + data);
                        car_number = parseInt(data);
                        res.write("event: initialization\n");
                        res.write("data: " + data + "\n\n");
                    }
                })
            }
        });
        
        res.write("event: connecttime\n");
        res.write("data: " + (new Date()) + "\n\n");

        fs.exists("StatusInfo.txt", function(exists) {
            if (!exists) {
                var i = 0;
                console.log("no");
                interval = setInterval(function() {
                    res.write("data: not connected\n\n");
                }, 1000);
            } else {
                console.log("yes");
                interval = setInterval(function() {
                    fs.readFile("StatusInfo.txt", "utf8", function(err, data) {
                        if (err) {
                            res.write("error: no data\n\n");
                        } else {
                            var car_list = data.trim().split(/[ \r\n]/);
                            res.write("event: start\n");
                            res.write("data: \n\n");
                            for (var car_id = 1; car_id <= car_number; ++car_id) {
                                var status_list = car_list.slice(car_id * 4 - 4, car_id * 4 - 1);
                                res.write("event: carid\n");
                                res.write("data: " + car_id + "\n\n");
                                res.write("event: position\n");
                                res.write("data: " + status_list[0] + "  " + status_list[1] + "\n\n");
                                res.write("event: velocity\n");
                                res.write("data: " + status_list[2] + "\n\n");
                            }
                            res.write("event: finish\n");
                            res.write("data: \n\n");
                        }
                    });
                }, 1000);
            }
        });

        req.connection.addListener("close", function() {
            clearInterval(interval);
        }, false);
    }
}).listen(8844, "127.0.0.1");
var http = require("http");
var fs = require("fs");

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
                            var status_list = data.trim().split(" ");
                            res.write("event: position\n");
                            res.write("data: " + status_list[0] + "  " + status_list[1] + "\n\n");
                            res.write("event: velocity\n");
                            res.write("data: " + status_list[2] + "\n\n");
                            //res.write("event: status\n");
                            //res.write("data: " + status_list[3] + "\n\n");
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
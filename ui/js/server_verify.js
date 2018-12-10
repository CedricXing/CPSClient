var http = require("http");
var fs = require("fs");
var vInterval=5000;

http.createServer(function(req, res) {
    var fileName = "." + req.url;

    if (fileName === "./stream") {
        res.writeHead(200, {
            "Content-Type": "text/event-stream",
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "Access-Control-Allow-Origin": '*',
        });

        fs.exists("VerifyInfo.txt", function(exists) {
            if (!exists) {
                var i = 0;
                console.log("no");
                interval = setInterval(function() {
                    res.write("data: not connected\n\n");
                }, vInterval);
            } else {
                console.log("yes");
                interval = setInterval(function() {
                    fs.readFile("VerifyInfo.txt", "utf8", function(err, data) {
                        if (err) {
                            res.write("error: no data\n\n");
                        } else {
                            var vrfy_list = data.trim().split(" ");
                            res.write("event: danger\n");
                            res.write("data: " + vrfy_list[0] + "\n\n");
                            res.write("event: ma\n");
                            res.write("data: " + vrfy_list[1] + "\n\n");
                        }
                    });
                }, vInterval);
            }
        });

        req.connection.addListener("close", function() {
            clearInterval(interval);
        }, false);
    }
}).listen(8844, "127.0.0.2");
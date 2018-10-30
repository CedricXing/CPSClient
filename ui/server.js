var http = require("http");
var fs = require("fs");

http.createServer(function (req, res) {
  var fileName = "." + req.url;

  var v = [1,2,3,4,5,6,7,8,9,10];

  if (fileName === "./stream") {
    res.writeHead(200, {
      "Content-Type":"text/event-stream",
      "Cache-Control":"no-cache",
      "Connection":"keep-alive",
      "Access-Control-Allow-Origin": '*',
    });

    res.write("retry: 10000\n");
    res.write("event: connecttime\n");
    res.write("data: " + (new Date()) + "\n\n");
    
    fs.exists("hello.txt", function(exists) {
	  	if (!exists) {
		    var i = 0;
		    console.log("no");
		    interval = setInterval(function () {
		      res.write("data: " + v[i++] + "\n\n");
		    }, 1000);	  		
	  	} else {
	  		console.log("yes");
	  		interval = setInterval(function() {
	  			fs.readFile("hello.txt", "utf8", function(err, data) {
	  				if (err) {
	  					res.write("error: no data\n\n");
	  				} else {
	  					res.write("data: " + data + "\n\n");
	  				}
	  			});
	  		}, 1000);
	  	}
  	});

    req.connection.addListener("close", function () {
      clearInterval(interval);
    }, false);
  }
}).listen(8844, "127.0.0.1");
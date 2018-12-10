var source_verify = new EventSource('http://127.0.0.2:8844/stream');
var danger;
var ma;

source_verify.onopen = function(event) {
    console.log("verify source open");
};

source_verify.onerror = function(event) {
    console.log("verify source error");
};

source_verify.addEventListener('danger', function(event) {
    danger = parseInt(event.data);
    // TODO: display corresponding picture (1: danger; 0: safety)
    console.log(danger);
    if(danger==1)
   		document.getElementById("PSAFE").src="img/danger.png";
    else
   		document.getElementById("PSAFE").src="img/safety.png";
}, false);

source_verify.addEventListener('ma', function(event) {
    ma = parseFloat(event.data);
    document.getElementById("PMA").innerHTML=ma+"";
}, false);
var fs = require('fs');
var https = require('https');
var apiKey = fs.readFileSync('./APIKEY.txt', 'utf8');

exports.padUpdate = function (hook_name, context, cb) {
    var data = JSON.stringify({
        'authorId': '2',
        'padId': '7',
        'apiKey': apiKey
    });

    var options = {
        host: 'localhost',
        port: '9000',
        path: '/api/documentEtherpadInfo/etherpad/update',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json; charset=utf-8',
            'Content-Length': data.length,
            'accept': '*/*'
        }
    };

    try {
        if (options.host == "localhost") {
            process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";
        }
        var req = https.request(options, function (res) {
            var msg = '';

            res.setEncoding('utf8');
            res.on('data', function (chunk) {
                msg += chunk;
            });
            res.on('end', function () {
                try {
                    console.log(JSON.parse(msg));
                } catch (e) {
                    console.log(e);
                }
            });
        });
        req.on('error', function (err) {
            console.error(err)
        });
        req.on('timeout', function () {
            console.log('timeout');
            req.abort();
        });
        req.write(data);
        req.end();
    } catch (e) {
        console.log(e);
    }
    return cb();
};
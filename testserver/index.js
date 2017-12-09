var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var ds = require('datasync-js');
var port = 4321;

var server = new ds.DataStoreServer().serveGlobal('store');
var store = server.getStore('store');

var refCount = store.ref('/count');
refCount.update(1);
refCount.on('update', function (value, path, flags) {
    if (flags.indexOf('lol') >= 0)return;
    refCount.update(value + 1, ['lol']);
});

var refWord = store.ref('/word');
refWord.update("Ahello");
refWord.on('update', function (value, path, flags) {
    if (flags.indexOf('lol') >= 0)return;
    refWord.update(String.fromCharCode(value.charCodeAt(0) + 1) + value.substring(1), ['lol']);
});

setInterval(function () {
    store.ref('/time').update(new Date().getTime());
}, 1000);

app.get('*', function (req, res) {
    res.send('Hello World!');
});

io.on('connection', function (socket) {
    var dsock = ds.DataSocket.fromSocket(socket);

    server.addSocket(dsock);

    socket.on('disconnect', function () {
        server.removeSocket(dsock);
    });
    console.log('someone connected!');
});

//Start http server
http.listen(port, function () {
    console.log('Listening on *:' + port);
});
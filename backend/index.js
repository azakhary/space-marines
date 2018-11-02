var io = require('socket.io').listen(5555);

var MongoClient = require('mongodb').MongoClient;

var players = [];

var dbclient;

var roomIdInc = 0;
var connectionRooms = {};
var currentConnections = {};

/*
MongoClient.connect("mongodb://localhost:27017/space-marines", function (err, db) {
    if (!err) {
        console.log('Mongodb Connected');
        dbclient = db;
        initSocketIO();
    } else {
        console.error(err);
    }
});*/
  initSocketIO();


function initSocketIO() {
    io.on('connection', function (socket) {

        console.log("Client connected");

        currentConnections[socket.id] = {socket: socket};

        socket.on('join', function(data) {
            console.log(data);
            console.log(socket.id);
            join(socket, data);
        });
        socket.on('send_emoji', function (data) {
            console.log(data);
            sendEmoji(socket, data);
        });
        socket.on('disconnect', function() {
            room_id = currentConnections[socket.id]['room_id'];
            if(room_id) {
                connectionRooms[room_id].forEach(function (cl) {
                   cl.disconnect();
                });
                delete connectionRooms[room_id];
            }
            delete currentConnections[socket.id];
        });
    });
}

function join(socket, data) {
    client_id = data.user_id;
    if(players.length > 0) {
        var player = players.pop();
        var room_id = roomIdInc;
        roomIdInc++;
        socket.join(room_id);
        player.socket.join(room_id);
        socket.emit("game_started", {user_id:player.id, room_id:room_id});
        player.socket.emit("game_started", {user_id:client_id, room_id:room_id});
        console.log(room_id);

        connectionRooms[room_id] = [socket, player.socket];
        currentConnections[socket.id]['room_id'] = room_id;
        currentConnections[player.socket.id]['room_id'] = room_id;
    }
    else {
        players.push({socket:socket, id:client_id});
        socket.emit("wait", {});
    }
}

function sendEmoji(socket, data) {
    var emojiCode = data.emoji_code;

    console.log(emojiCode);
    var room_id = currentConnections[socket.id]['room_id'];
    connectionRooms[room_id].forEach(function (client) {
       if(client.id != socket.id) {
           client.emit("show_emoji", {emoji_code:emojiCode});
       }
    });
}
var io = require('socket.io').listen(5555);

var fs = require("fs");

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


// init data config
var cards = JSON.parse(fs.readFileSync("cards.json"));
var cardMap = new Array();
for(var i = 0; i < cards.length; i++) {
    cardMap[cards[i].id+''] = cards[i];
}

//var card = createCard("argturus");
//console.log(card);


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
        socket.on('play_card', function (data) {
            console.log(data);
            playCard(socket, data);
        });
        socket.on('minion_attack', function (data) {
            minionAttackCommand(socket, data);
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

        currentConnections[socket.id]['room_id'] = room_id;
        currentConnections[player.socket.id]['room_id'] = room_id;

        var playerOne = initPlayer(socket, client_id);
        var playerTwo = initPlayer(player.socket, player.id);

        connectionRooms[room_id] = [playerOne, playerTwo];
        console.log(connectionRooms);
    }
    else {
        players.push({socket:socket, id:client_id});
        socket.emit("wait", {});
    }
}

function sendEmoji(socket, data) {
    var emojiCode = data.emoji_code;

    var room_id = currentConnections[socket.id]['room_id'];
    connectionRooms[room_id].forEach(function (client) {
       if(client.id != socket.id) {
           client.emit("show_emoji", {emoji_code:emojiCode});
       }
    });
}

function playCard(socket, data) {
    var slotId = data.card_slot;
    var room_id = currentConnections[socket.id]['room_id'];

    connectionRooms[room_id].forEach(function (player) {
         if(player.socket.id == socket.id) {
            //player that played this card
           var card = player.hand[slotId];
           player.hand.splice(slotId, slotId+1);

           if(card.type == "minion") {
               summonMinion( connectionRooms[room_id], player, card)
               //console.log("summoning the minion hohoho");
           }

           if(player.deckLock == false) {
                tryToDraw(player)
           }
       }

    });

}

function minionAttackCommand(socket, data) {
    var from_slot = data.from_slot;
    var target_slot = data.target_slot;
    var room_id = currentConnections[socket.id]['room_id'];

    var fromPlayer;
    var toPlayer;

    connectionRooms[room_id].forEach(function (player) {
         if(player.socket.id == socket.id) {
            //player that played this minion
            fromPlayer = player;
         } else {
            toPlayer = player;
         }
    });

    toPlayer.board[target_slot].hp -= fromPlayer.board[from_slot].atk;
    fromPlayer.board[from_slot].hp -= toPlayer.board[target_slot].atk;

    if(toPlayer.board[target_slot].hp <= 0) {
        toPlayer.board[target_slot].destroyed = true;
    }
    if(fromPlayer.board[from_slot].hp <= 0) {
        fromPlayer.board[from_slot].destroyed = true;
    }

    toPlayer.socket.emit("minion_update", {user_id:toPlayer.id, slot_id: target_slot, minion: toPlayer.board[target_slot]});
    toPlayer.socket.emit("minion_update", {user_id:fromPlayer.id, slot_id: from_slot, minion: fromPlayer.board[from_slot]});

    fromPlayer.socket.emit("minion_update", {user_id:fromPlayer.id, slot_id: from_slot, minion: fromPlayer.board[from_slot]});
    fromPlayer.socket.emit("minion_update", {user_id:toPlayer.id, slot_id: target_slot, minion: toPlayer.board[target_slot]});

    if(toPlayer.board[target_slot].destroyed == true) {
        toPlayer.board.splice(target_slot, target_slot + 1);
    }
    if(fromPlayer.board[from_slot].destroyed == true) {
        fromPlayer.board.splice(from_slot, from_slot + 1);
    }
}

function Player() {
}

function Card() {
    this.id = "";
    this.type = 'minion';
    this.title = "";
    this.cost = 0;
    this.minion = {};
    this.spell = {};
}

function summonMinion(room, player, card) {
     var minion = {};

     minion.atk = card.minion.atk;
     minion.hp = card.minion.hp;
     minion.destroyed = false;

     minion.id = card.id;

     minion.card = card;

     player.board.push(minion);

     minion.slot = player.board.indexOf(minion);

     room[0].socket.emit("summon_minion", {user_id:player.id, 'minion': minion});
     room[1].socket.emit("summon_minion", {user_id:player.id, 'minion': minion});
}

function createCard(id) {
    var card = new Card();
    var obj = cardMap[id];
    card.id = id;
    card.type = obj.type;
    card.title = obj.title;
    card.cost = obj.cost;
    card.minion = obj.minion;
    card.spell = obj.spell;

    return card;
}

function initPlayer(socket, id) {
    var player = new Player();
    player.socket = socket;
    player.id = id;

    // creating a deck of 30
    player.deck = [];
    player.hand = [];

    player.mana = 0;
    player.maxMana = 10;

    player.deckLock = true;
    setTimeout(function() {
        player.deckLock = false;
        tryToDraw(player);
    }, 10000);

    player.board = [];
    for(var i = 0; i < 30; i++) {

        var keys = Object.keys(cardMap);
        var name = cardMap[keys[Math.floor(keys.length * Math.random())]].id;
        var card = createCard(name);
        player.deck.push(card);
    }

    // sending deck size
    player.socket.emit("init_deck", {user_id:player.id, deck_size: player.deck.length});

    // drawing first 4 cards
    drawCard(player);
    drawCard(player);
    drawCard(player);
    drawCard(player);

    return player;
}

function tryToDraw(player) {
    if(player.hand.length < 4) {
        drawCard(player);

        setTimeout(function() {
                player.deckLock = false;
                tryToDraw(player);
        }, 10000);
    }
}

function drawCard(player) {
    var crd = player.deck.pop();
    player.hand.push(crd);
    crd.slot = player.hand.indexOf(crd);
    player.socket.emit("draw_card", {user_id:player.id, card: crd});
}
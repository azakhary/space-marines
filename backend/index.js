var io = require('socket.io').listen(5555);

var fs = require("fs");

var MongoClient = require('mongodb').MongoClient;

var players = [];

var dbclient;

var roomIdInc = 0;
var connectionRooms = {};
var currentConnections = {};

const MANA_SPEED = 0.2;
const MINON_COOLDOWN = 10;
const DRAW_CARD_COOLDOWN = 10;


console.log("server started");

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
var drawableCards = new Array();
for(var i = 0; i < cards.length; i++) {
    cardMap[cards[i].id+''] = cards[i];
    if( cards[i].deck == true) {
        drawableCards.push(cards[i]);
    }
}

cardMap["ach"].battlecry = function(room, player, card) {
    // custom battlecry for ach
    console.log("ach battlecry");
    summonMinion(room, player, cardMap["nane"]);
    summonMinion(room, player, cardMap["dave"]);
}

cardMap["eduard"].on_board_change = function(board, thisMinion, isSameBoard, player1, player2, minionOwner) {
    if(!isSameBoard) return;
    for(key in minionOwner.board) {
        var minion = board[key];
        if(minion.card.id == "hayk") {

            // gain +2 atk
            if(thisMinion.buffed != true) {
                thisMinion.atk += 2;
                thisMinion.buffed = true;

                player1.socket.emit("minion_update", {user_id:minionOwner.id, slot_id: thisMinion.slot, minion: thisMinion});
                player2.socket.emit("minion_update", {user_id:minionOwner.id, slot_id: thisMinion.slot, minion: thisMinion});

                console.log("buff achieved");
            }

            return;
        }
    }

    if(thisMinion.buffed == true) {
       thisMinion.buffed = false;
       thisMinion.atk -= 2;

       player1.socket.emit("minion_update", {user_id:minionOwner.id, slot_id: thisMinion.slot, minion: thisMinion});
       player2.socket.emit("minion_update", {user_id:minionOwner.id, slot_id: thisMinion.slot, minion: thisMinion});

       console.log("debuff happened");
    }

}

cardMap["grachik"].on_attack_minion = function(minionOwner, thisMinion, player1, player2, rooms) {
    // custom battlecry for ach
    console.log("grach attacked");

    if(Math.random() < 0.5) {
        // grach needs to explode
        console.log("grachik will explode")

        destroyMinion(thisMinion, player1, player2, minionOwner, rooms);
    }
}

cardMap["gayush"].repeat = function(minion, player, room) {

    console.log("gayush wotakuu!");

    // iterate through neighbours and heal one of them
    var board = player.board;

    var minArr = [];

    for(key in board) {
        var currMinion = board[key];
        if(currMinion.hp < currMinion.max_hp) {
            minArr.push(currMinion);
        }
    }
    var randMinion;
    console.log(board);
    if(minArr.length > 0) {
        randMinion = minArr[Math.floor(Math.random() * minArr.length)];

        // heal randMinion
       randMinion.hp += 1;
       if(randMinion.hp > randMinion.max_hp) randMinion.hp = randMinion.max_hp;
       room[0].socket.emit("minion_update", {user_id:player.id, slot_id: randMinion.slot, minion: randMinion});
       room[1].socket.emit("minion_update", {user_id:player.id, slot_id: randMinion.slot, minion: randMinion});
    }
}

cardMap["avik"].on_board_change = function(board, thisMinion, isSameBoard, player1, player2, minionOwner) {
    if(!isSameBoard) return;

    if(minionOwner.board.length > 1) {
         // gain +2 atk
        if(thisMinion.buffed != true) {
            thisMinion.atk += 5;
            thisMinion.hp += 5;
            thisMinion.buffed = true;

            player1.socket.emit("minion_update", {user_id:minionOwner.id, slot_id: thisMinion.slot, minion: thisMinion});
            player2.socket.emit("minion_update", {user_id:minionOwner.id, slot_id: thisMinion.slot, minion: thisMinion});

            console.log("buff achieved");
        }
    } else {
        if(thisMinion.buffed == true) {
           thisMinion.buffed = false;
           thisMinion.atk -= 5;
           thisMinion.hp -= 5;

           player1.socket.emit("minion_update", {user_id:minionOwner.id, slot_id: thisMinion.slot, minion: thisMinion});
           player2.socket.emit("minion_update", {user_id:minionOwner.id, slot_id: thisMinion.slot, minion: thisMinion});

           console.log("debuff happened");
        }
    }

}

//var card = createCard("argturus");
//console.log(card);


function destroyMinion(thisMinion, player1, player2, minionOwner, rooms) {
    thisMinion.hp = 0;
    thisMinion.destroyed = true;
    player1.socket.emit("minion_update", {user_id:minionOwner.id, slot_id: thisMinion.slot, minion: thisMinion});
    player2.socket.emit("minion_update", {user_id:minionOwner.id, slot_id: thisMinion.slot, minion: thisMinion});
    if(thisMinion.card.repeat && thisMinion.card.repeat.timer) clearInterval(thisMinion.card.repeat.timer);
    delete minionOwner.board[minionOwner.board.indexOf(thisMinion)];

    onBoardChange(rooms, thisMinion.board);
}


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
           var room_id = currentConnections[socket.id]['room_id'];
           if(room_id != undefined && room_id != null && connectionRooms[room_id]) {
               connectionRooms[room_id].forEach(function (player) {
                   player.socket.leave(room_id);
               });
               delete connectionRooms[room_id];
           }
           players = [];
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
        console.log(room_id);

        currentConnections[socket.id]['room_id'] = room_id;
        currentConnections[player.socket.id]['room_id'] = room_id;

        var playerOne = initPlayer(socket, client_id);
        var playerTwo = initPlayer(player.socket, player.id);

        connectionRooms[room_id] = [playerOne, playerTwo];
        console.log(connectionRooms);

        var plrOne = {hero: playerOne.hero, hp: playerOne.hp, max_hp: playerOne.max_hp};
        var plrTwo = {hero: playerTwo.hero, hp: playerTwo.hp, max_hp: playerTwo.max_hp};

        socket.emit("game_started", {user_id:player.id, room_id:room_id, mana_speed:MANA_SPEED, cooldown: MINON_COOLDOWN, player_data: plrTwo, opponent_data: plrOne});
        player.socket.emit("game_started", {user_id:client_id, room_id:room_id, mana_speed:MANA_SPEED, cooldown: MINON_COOLDOWN, player_data: plrOne, opponent_data: plrTwo});
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

    var targetSlot = data.target_slot;

    var room_id = currentConnections[socket.id]['room_id'];

    connectionRooms[room_id].forEach(function (player) {
         if(player.socket.id == socket.id) {
            //player that played this card
           var card = player.hand[slotId];


           var spent = spendMana(room_id, player, card);

           if(spent) {
               if (card.type == "minion") {
                   summonMinion(connectionRooms[room_id], player, card, targetSlot);
                   //console.log("summoning the minion hohoho");
               }

               // remove the card from hand
               player.hand.splice(slotId, 1);
               player.socket.emit("hand_update", {user_id: player.id, slot_to_remove: slotId});


               if (player.deckLock == false) {
                   tryToDraw(player)
               }
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

    var fromCooldown = getMinionCooldown(fromPlayer.board[from_slot]);

    if(fromCooldown > 0) {
        // Can't attack should not get there
        console.error("Error cannot attack, minion is still on it's cooldown, Client should check, shoud not get here");
        return;
    }

    fromPlayer.board[from_slot].cooldown = MINON_COOLDOWN; // Reset attackers cooldown
    fromPlayer.board[from_slot].time = new Date().getTime();

    if(data.is_target_hero == true) {
        // hero being attacked
        toPlayer.hp -= fromPlayer.board[from_slot].atk;
        if(toPlayer.hp <= 0) {
            toPlayer.hp = 0;
        }

        // initiate attack anim
        fromPlayer.socket.emit("minion_attack_animation", {user_id:fromPlayer.id, from_slot_id: from_slot, to_slot_id: target_slot, target_hero: true});
        toPlayer.socket.emit("minion_attack_animation", {user_id:fromPlayer.id, from_slot_id: from_slot, to_slot_id: target_slot, target_hero: true});


        fromPlayer.socket.emit("minion_update", {user_id:fromPlayer.id, slot_id: from_slot, minion: fromPlayer.board[from_slot]});
        toPlayer.socket.emit("minion_update", {user_id:fromPlayer.id, slot_id: from_slot, minion: fromPlayer.board[from_slot]});

        console.log("new hp: " + toPlayer.hp);
        syncHero(toPlayer, toPlayer);
        syncHero(fromPlayer, toPlayer);

    } else {
        // minion being attacked

        var toCooldown = getMinionCooldown(toPlayer.board[target_slot]);
        toPlayer.board[target_slot].cooldown = toCooldown;

        toPlayer.board[target_slot].hp -= fromPlayer.board[from_slot].atk;
        fromPlayer.board[from_slot].hp -= toPlayer.board[target_slot].atk;

        if(toPlayer.board[target_slot].hp <= 0) {
            toPlayer.board[target_slot].destroyed = true;
            if(toPlayer.board[target_slot].card.repeat && toPlayer.board[target_slot].card.repeat.timer) clearInterval(toPlayer.board[target_slot].card.repeat.timer);
        }
        if(fromPlayer.board[from_slot].hp <= 0) {
            fromPlayer.board[from_slot].destroyed = true;
            if(fromPlayer.board[from_slot].card.repeat && fromPlayer.board[from_slot].card.repeat.timer) clearInterval(fromPlayer.board[from_slot].card.repeat.timer);
        }

        // initiate attack anim
        fromPlayer.socket.emit("minion_attack_animation", {user_id:fromPlayer.id, from_slot_id: from_slot, to_slot_id: target_slot, target_hero: false});
        toPlayer.socket.emit("minion_attack_animation", {user_id:fromPlayer.id, from_slot_id: from_slot, to_slot_id: target_slot, target_hero: false});

        toPlayer.socket.emit("minion_update", {user_id:toPlayer.id, slot_id: target_slot, minion: toPlayer.board[target_slot]});
        toPlayer.socket.emit("minion_update", {user_id:fromPlayer.id, slot_id: from_slot, minion: fromPlayer.board[from_slot]});

        fromPlayer.socket.emit("minion_update", {user_id:fromPlayer.id, slot_id: from_slot, minion: fromPlayer.board[from_slot]});
        fromPlayer.socket.emit("minion_update", {user_id:toPlayer.id, slot_id: target_slot, minion: toPlayer.board[target_slot]});


        if(!fromPlayer.board[from_slot].destroyed && fromPlayer.board[from_slot].card.on_attack_minion) {
            fromPlayer.board[from_slot].card.on_attack_minion(fromPlayer, fromPlayer.board[from_slot], toPlayer, fromPlayer, connectionRooms[room_id]);
        }

        if(toPlayer.board[target_slot] && toPlayer.board[target_slot].destroyed == true) {
            delete toPlayer.board[target_slot];
            onBoardChange(connectionRooms[room_id], toPlayer.board);
        }
        if(fromPlayer.board[from_slot] && fromPlayer.board[from_slot].destroyed == true) {
            delete fromPlayer.board[from_slot];
            onBoardChange(connectionRooms[room_id], fromPlayer.board);
        }


    }

}

function getMinionCooldown(minion) {
    var curr = new Date().getTime();

    var time = minion.time;

    var timePassedSec = ( curr - time ) / 1000;

    var cooldown = MINON_COOLDOWN - timePassedSec;

    if(cooldown <= 0) {
        cooldown = 0;
    }

    return cooldown;
}

function Player() {
}

function Card() {
    this.id = "";
    this.type = 'minion';
    this.title = "";
    this.description = "";
    this.cost = 0;
    this.minion = {};
    this.spell = {};
}

function isSlotEmpty(board, slot) {
    for(key in board) {
        if(board[key].slot == slot) {
            return false;
        }
    }
    return true;
}

function findEmptySlot(board) {
    for(var i = 0; i < 4; i++) {
        if(isSlotEmpty(board, i)) {
            return i;
        }
    }
    return -1;
}

function getBoardMinionCount(board) {
    var c = 0;
    for(key in board) {
        c++;
    }

    return c;
}

function summonMinion(room, player, card, target_slot = -1) {
     var minion = {};

     if( getBoardMinionCount(player.board) > 4) return;

     minion.atk = card.minion.atk;
     minion.max_atk = card.minion.atk;
     minion.hp = card.minion.hp;
     minion.max_hp = card.minion.hp;
     minion.destroyed = false;

     minion.id = card.id;

     minion.card = card;

     minion.time = new Date().getTime();
     minion.cooldown = MINON_COOLDOWN;


     if(target_slot >= 0) {
         //slot was specified
         // check if slot is empty;
         if(isSlotEmpty(player.board, target_slot)) {
             minion.slot = target_slot;
         } else {
             console.log("client tried to position minion on occupied slot");
             return;
         }
     } else {
         // choose an empty slot
         target_slot = findEmptySlot(player.board);
         if(target_slot >= 0) {
             minion.slot = target_slot;
         } else {
             console.log("no empty slots were found to position");
             return;
         }
     }

     player.board[target_slot] = minion;

     room[0].socket.emit("summon_minion", {user_id:player.id, 'minion': minion});
     room[1].socket.emit("summon_minion", {user_id:player.id, 'minion': minion});


     if(card.battlecry) {
        card.battlecry(room, player, card);
     }

     // go through all minions on board and summon their cards on_board_change
     onBoardChange(room, player.board);


     if(minion.card.repeat) {
        minion.card.repeat.timer = setInterval(function() { minion.card.repeat(minion, player, room); }, 10000);
     }

}

function onBoardChange(rooms, board) {
    for(key in rooms[0].board) {
        var informedMinion = rooms[0].board[key];
        if(informedMinion.card.on_board_change) {
            informedMinion.card.on_board_change(board, informedMinion, board == rooms[0].board, rooms[0], rooms[1], rooms[0]);
        }
    }
    for(key in rooms[1].board) {
        var informedMinion = rooms[1].board[key];
        if(informedMinion.card.on_board_change) {
            informedMinion.card.on_board_change(board, informedMinion, board == rooms[1].board, rooms[0], rooms[1], rooms[1]);
        }
    }
}

function spendMana(room, player, card) {
    var curr = new Date().getTime();

    var mana = getMana(player, curr);

    if(mana >= card.cost) {
        mana -= card.cost;

        player.mana = mana;
        player.lastSpentTime = curr;

        syncHero(player, player, true);

        return true;
    }
    else {
        return false;
    }
}

function createCard(id) {
    var card = new Card();
    var obj = cardMap[id];
    card.id = id;
    card.type = obj.type;
    card.title = obj.title;
    card.description = obj.description;
    card.cost = obj.cost;
    card.deck = obj.deck;
    card.battlecry = obj.battlecry;
    card.on_board_change = obj.on_board_change;
    card.on_attack_minion = obj.on_attack_minion;
    card.repeat = obj.repeat;
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

    //player character
    if(Math.random() < 0.5) {
        player.hero = "one";
    } else {
        player.hero = "two";
    }

    player.hp = 30;
    player.max_hp = 30;

    player.mana = 0;
    player.maxMana = 10;

    player.lastSpentTime = new Date().getTime();

    player.deckLock = true;
    setTimeout(function() {
        player.deckLock = false;
        tryToDraw(player);
    }, 10000);

    player.board = [];
    for(var i = 0; i < 30; i++) {
        var keys = Object.keys(drawableCards);
        var name = drawableCards[keys[Math.floor(keys.length * Math.random())]].id;
        var card = createCard(name);
        if(card.deck == false) {
            i++;
            continue;
        }
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
    console.log(player.hand.length + " length of hand");
    if(player.hand.length < 4) {
        drawCard(player);

        setTimeout(function() {
                player.deckLock = false;
                tryToDraw(player);
        }, DRAW_CARD_COOLDOWN);
    }
}

function drawCard(player) {
    var crd = player.deck.pop();
    player.hand.push(crd);
    crd.slot = player.hand.length-1;
    player.socket.emit("draw_card", {user_id:player.id, card: crd});
}

function syncHero(sendToPlayer, player, manaCalculated) {
    var mana = manaCalculated ? player.mana : getMana(player, new Date().getTime());
    var data = {user_id:player.id, 'mana': mana, 'hp': player.hp, 'max_hp': player.max_hp};
    console.log("hero sync: " + JSON.stringify(data));
    sendToPlayer.socket.emit("hero_sync", data);
}

function getMana(player, curr) {
    var time = player.lastSpentTime ? player.lastSpentTime : curr;

    var passedTimeSec = ( curr - time ) / 1000;

    var mana = player.mana;

    mana += ( passedTimeSec * MANA_SPEED );

    if(mana > player.maxMana) {
        mana = player.maxMana;
    }
    /*
    else {
        mana = Math.floor(mana);
    }*/

    return mana;
}
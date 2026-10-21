const express = require('express');
const http = require('http');
const { Server } = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
});

const PORT = process.env.PORT || 3000;

// Rooms storage
// roomId -> { roomId, state, players: [p0, p1, p2, p3], lastAction: null }
const rooms = new Map();
const queue = []; // Array of socket IDs waiting for match

io.on('connection', (socket) => {
  console.log(`[Ludo Server] Player Connected: ${socket.id}`);

  // 1. Join Matchmaking Queue
  socket.on('findMatch', (userData) => {
    socket.userData = {
      socketId: socket.id,
      uid: userData.uid || socket.id,
      name: userData.name || 'Player',
      avatar: userData.avatar || '',
      isBot: false
    };

    if (!queue.includes(socket.id)) {
      queue.push(socket.id);
    }

    console.log(`[Ludo Server] Player ${socket.userData.name} in queue. Queue size: ${queue.length}`);

    checkAndCreateMatch();
  });

  // 2. Game Actions (Dice Roll, Token Move)
  socket.on('gameAction', (data) => {
    const roomId = socket.roomId;
    if (!roomId || !rooms.has(roomId)) return;

    const room = rooms.get(roomId);
    room.lastAction = {
      type: data.type,
      slot: data.slot,
      diceVal: data.diceVal,
      color: data.color,
      tokenId: data.tokenId,
      timestamp: Date.now()
    };

    // Broadcast action to all connected players in room
    io.to(roomId).emit('roomUpdated', room);
  });

  // 3. Disconnect
  socket.on('disconnect', () => {
    console.log(`[Ludo Server] Player Disconnected: ${socket.id}`);
    const qIndex = queue.indexOf(socket.id);
    if (qIndex !== -1) queue.splice(qIndex, 1);

    if (socket.roomId && rooms.has(socket.roomId)) {
      const room = rooms.get(socket.roomId);
      // Mark player slot as bot or empty
      room.players.forEach(p => {
        if (p && p.socketId === socket.id) {
          p.isBot = true;
          p.name += ' (Bot)';
        }
      });
      io.to(socket.roomId).emit('roomUpdated', room);
    }
  });
});

function checkAndCreateMatch() {
  while (queue.length >= 4) {
    const matchSockets = queue.splice(0, 4);
    const roomId = 'room_' + Date.now() + '_' + Math.floor(Math.random() * 1000);

    const roomData = {
      roomId: roomId,
      state: 'PLAYING',
      players: [],
      lastAction: null
    };

    matchSockets.forEach((sId, index) => {
      const sock = io.sockets.sockets.get(sId);
      if (sock) {
        sock.join(roomId);
        sock.roomId = roomId;

        roomData.players.push({
          slot: index,
          socketId: sId,
          uid: sock.userData.uid,
          name: sock.userData.name,
          avatar: sock.userData.avatar,
          isBot: false
        });
      }
    });

    rooms.set(roomId, roomData);

    // Notify all 4 players
    matchSockets.forEach((sId, index) => {
      const sock = io.sockets.sockets.get(sId);
      if (sock) {
        sock.emit('roomUpdated', {
          ...roomData,
          mySlot: index
        });
      }
    });

    console.log(`[Ludo Server] Match Created: ${roomId} with 4 players!`);
  }
}

app.get('/', (req, res) => {
  res.send('Ludo Online Multiplayer Socket Server is Running!');
});

server.listen(PORT, () => {
  console.log(`🚀 Ludo Server running on http://localhost:${PORT}`);
});

const express = require('express');
const http = require('http');
const { Server } = require("socket.io");
const cors = require('cors');
const cookieParser = require('cookie-parser');

const authRoutes = require('./routes/auth');
const gameRoutes = require('./routes/game');
const gameHandler = require('./handlers/gameHandler');

const app = express();
app.use(cors({
    origin: 'http://localhost:5173', // Vue frontend
    credentials: true
}));
app.use(express.json());
app.use(cookieParser());

app.use('/api/auth', authRoutes);
app.use('/api/game', gameRoutes);

app.use(express.static('public'));

const server = http.createServer(app);
const io = new Server(server, {
    cors: {
        origin: "http://localhost:5173",
        methods: ["GET", "POST"],
        credentials: true
    }
});

io.on('connection', (socket) => gameHandler(io, socket));

server.listen(3000, () => {
    console.log('Serwer działa na porcie http://localhost:3000');
});

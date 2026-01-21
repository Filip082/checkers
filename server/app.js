const express = require('express');
const http = require('http');
const { Server } = require("socket.io");
const cors = require('cors');
const cookieParser = require('cookie-parser');
const cookie = require('cookie');
const jwt = require('jsonwebtoken');

const authRoutes = require('./routes/auth');
const gameRoutes = require('./routes/game');
const gameHandler = require('./handlers/gameHandler');

const app = express();
app.use(express.json());
app.use(cookieParser());

app.use('/api/auth', authRoutes);
app.use('/api/game', gameRoutes);

app.use(express.static('public'));

const server = http.createServer(app);
const io = new Server(server);

io.use((socket, next) => {
    try {
        const cookies = cookie.parseCookie(socket.request.headers.cookie || "");
        const token = cookies.token;

        if (!token)
            return next(new Error('Brak tokena'));
        const result = jwt.verify(token, process.env.JWT_SECRET);

        socket.user = result;
        next();
    } catch (err) {
        next(new Error('Niezalogowany'));
    }
});

io.on('connection', (socket) => gameHandler(io, socket));

server.listen(3000, () => {
    console.log('Serwer działa na porcie http://localhost:3000');
});

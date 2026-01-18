require('dotenv').config();
const jwt = require('jsonwebtoken');
const db = require("../config/db");

if (!global.self) global.self = global;

const warcaby = require('./teavm/checkers-engine.js');
const verifyMove = warcaby.verifyMove;
const applyMove = warcaby.applyMove;
const restoreGame = warcaby.restoreGame;

const activeGames = new Map();

module.exports = (io, socket) => {
    socket.on('connect_to_game', async ({id_gra}) => {
        try {
            if (socket.id_gracz_gra || socket.id_gra)
                return socket.emit('error', 'Nie można dołączyć do więcej niż jednej gry naraz');

            const id_gracz = socket.user.id;

            const result = await db.query(
                'SELECT gg.id_gracz_gra, k.nazwa FROM warcaby.gracz_gra gg JOIN warcaby.kolor k ON gg.kolor = k.id_kolor WHERE gg.id_gra = $1 AND gg.id_gracz = $2',
                [id_gra, id_gracz]
            );
            if (result.rowCount != 1)
                return socket.emit('error', 'Nie jesteś uczestnikiem tej gry');

            const movesResult = await db.query(
                'SELECT * FROM warcaby.ruch r JOIN warcaby.gracz_gra gg ON r.id_gracz_gra = gg.id_gracz_gra WHERE gg.id_gra = $1 ORDER BY r.czas_ruchu ASC',
                [id_gra]
            );

            let gameInstance;
            if (activeGames.has(id_gra))
                gameInstance = activeGames.get(id_gra);
            else {
                gameInstance = restoreGame(movesResult.rows.map(r => r.ruch));
                activeGames.set(id_gra, gameInstance);
            }

            socket.emit('connected_to_game', { 
                moveHistory: movesResult.rows.map(r => r.ruch),
                color: result.rows[0].nazwa, 
            });
            socket.id_gracz_gra = result.rows[0].id_gracz_gra;
            socket.id_gra = id_gra;
            socket.join(id_gra);
            console.log(`Gracz ${socket.user.login} (Socket ${socket.id}) dołączył do pokoju ${id_gra}`);
        } catch (err) {
            console.error(err);
            socket.emit('error', 'Błąd podczas dołączania do gry');
        }
    });

    socket.on('make_move', async (ruch) => {
        try {
            if (!socket.id_gracz_gra || !socket.id_gra)
                return socket.emit('error', 'Nie jesteś połączony z żadną grą');

            const gameInstance = activeGames.get(socket.id_gra);
            if (!gameInstance)
                return socket.emit('error', 'Gra nie jest aktywna na serwerze');

            verifyMove(gameInstance, ruch);

            await db.query(
                'INSERT INTO warcaby.ruch (id_gracz_gra, ruch) VALUES ($1, $2)',
                [socket.id_gracz_gra, ruch]
            );

            applyMove(gameInstance);

            socket.emit('move_made', {
                ruch: ruch,
                twoja_kolej: false
            });
            io.to(socket.id_gra).except(socket.id).emit('move_made', {
                ruch: ruch,
                twoja_kolej: true
            });

            const zwyciezcaResult = await db.query(
                'SELECT gg.id_gracz, p.login FROM warcaby.gra g \
                LEFT JOIN warcaby.gracz_gra gg ON g.id_gra = gg.id_gra AND g.zwyciezca = gg.kolor \
                LEFT JOIN warcaby.gracz p on gg.id_gracz = p.id_gracz \
                WHERE g.id_gra = $1',
                [socket.id_gra]
            );
            if (zwyciezcaResult.rows[0].id_gracz) {
                io.to(socket.id_gra).emit('game_over', {
                    zwyciezca: zwyciezcaResult.rows[0].id_gracz,
                    zwyciezca_login: zwyciezcaResult.rows[0].login
                });
                activeGames.delete(socket.id_gra);
                console.log(`Gra ${socket.id_gra} zakończona. \
                    Wygrał kolor: ${zwyciezcaResult.rows[0].login}`);
            }

        } catch (err) {
            console.error(err);
            socket.emit('error', 'Błąd podczas wykonywania ruchu');
        }
    });

    socket.on('disconnect_from_game', () => {
        if (!socket.id_gra)
            return;
        socket.leave(socket.id_gra);
        console.log(`Gracz ${socket.user.login} (Socket ${socket.id}) opuścił pokój ${socket.id_gra}`);
        socket.id_gracz_gra = null;
        socket.id_gra = null;
    });
};

const db = require("../config/db");
const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
require('dotenv').config();
const jwt = require('jsonwebtoken');
const auth = require('../middleware/auth');

const saltRounds = 10;

router.post('/register', async (req, res) => {
    const { login, password } = req.body; // Changed username to login
    try {
        const hash = await bcrypt.hash(password, saltRounds);
        db.query(
            'INSERT INTO warcaby.gracz (login, haslo) VALUES ($1, $2);',
            [login, hash],
            (err, result) => {
                if (err) {
                    console.error(err);
                    return res.status(500).json({ error: "Błąd rejestracji" });
                }
                console.log('Gracz dodany: ' + login);
                res.status(201).json({ response: 'Dodano gracza' });
            });
    } catch (e) {
         res.status(500).json({ error: "Błąd serwera" });
    }
});

router.post('/login', async (req, res) => {
    try {
        const { login, password } = req.body; // Changed username to login
        const result = await db.query(
            'SELECT g.id_gracz, g.haslo FROM warcaby.gracz g WHERE g.login = $1',
            [login]
        );

        if (result.rowCount != 1)
            return res.status(404).json({ error: "Użytkownik nie istnieje" });

        if (await bcrypt.compare(password, result.rows[0].haslo))
            return res.status(200)
                .cookie('token',
                    jwt.sign(
                        { id: result.rows[0].id_gracz, login: login }, // Added login to token
                        process.env.JWT_SECRET,
                        { expiresIn: '1w' }
                    ),
                    { httpOnly: true, sameSite: 'lax' } // Added sameSite for security
                ).json({ response: "Zalogowany", username: login }); // Return username for frontend
        return res.status(401).json({ response: "Złe hasło" });
    } catch (err) {
        console.error(err);
        return res.status(500).json({ error: "Błąd logowania" });
    }
});

router.get('/me', auth, (req, res) => {
    // req.user comes from auth middleware
    res.json({ username: req.user.login, id: req.user.id });
});

router.post('/logout', (req, res) => {
    res.clearCookie('token').json({ response: "Wylogowano" });
});

module.exports = router;

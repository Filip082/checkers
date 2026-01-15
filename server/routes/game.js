const db = require("../config/db");
const express = require('express');
const auth = require('../middleware/auth');
const router = express.Router();

router.post('/new', auth, async (req, res) => {
    try {
        const id_gracz = req.user.id;
        const result = await db.query(
            'SELECT warcaby.utworz_gre($1) AS id_gra',
            [id_gracz]
        );

        const id_gra = result.rows[0].id_gra;
        return res.status(201).json({ id_gra: id_gra });
    } catch (err) {
        return res.status(500).json({ error: "Błąd serwera" });
    }
});

router.post('/:id_gra/join', auth, async (req, res) => {
    try {
        const id_gracz = req.user.id;
        const id_gra = req.params.id_gra;

        await db.query(
            'SELECT warcaby.dolacz_do_gry($1, $2)',
            [id_gra, id_gracz]
        );

        return res.status(200).json({ response: "Dołączono do gry" });
    } catch (err) {
        if (err.message.includes("Błędna gra") ||
            err.message.includes("Gracz już uczestniczy w tej grze"))
            return res.status(400).json({ error: err.message });
        return res.status(500).json({ error: "Błąd serwera" });
    }
});

router.get('/open', auth, async (req, res) => {
    try {
        const result = await db.query(
            'SELECT * FROM warcaby.otwarte_gry WHERE id_gracz != $1',
            [req.user.id]
        );
        return res.status(200).json({ games: result.rows });
    } catch (err) {
        return res.status(500).json({ error: "Błąd serwera" });
    }
});

router.get('/my', auth, async (req, res) => {
    try {
        const login = req.user.login;
        const result = await db.query(
            'SELECT id_gra, moj_kolor, przeciwnik, status_gry, wynik, moja_kolej FROM warcaby.lista_gier l WHERE l.login = $1',
            [login]
        );
        return res.status(200).json({ games: result.rows });
    } catch (err) {
        return res.status(500).json({ error: "Błąd serwera" });
    }
});

router.get('/ranking', auth, async (req, res) => {
    try {
        const result = await db.query(
            'SELECT * FROM warcaby.ranking'
        );
        return res.status(200).json({ ranking: result.rows });
    } catch (err) {
        return res.status(500).json({ error: "Błąd serwera" });
    }
});

module.exports = router;

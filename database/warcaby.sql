create schema if not exists warcaby;

create table warcaby.kolor
(
    id_kolor serial  not null PRIMARY KEY,
    nazwa    varchar not null unique
);
insert into warcaby.kolor(nazwa)
values ('Biały'),
       ('Czarny');

create table warcaby.status
(
    id_status serial  not null PRIMARY KEY,
    nazwa     varchar not null unique
);
insert into warcaby.status(nazwa)
values ('Utworzona'),
       ('Trwająca'),
       ('Zakończona');


create table warcaby.gracz
(
    id_gracz serial primary key,
    login    varchar unique,
    haslo    varchar(255)
);

create table warcaby.admin
(
    id_gracz int not null,
    foreign key (id_gracz) references warcaby.gracz (id_gracz)
);

create table warcaby.gra
(
    id_gra    serial primary key,
    status    int not null references warcaby.status (id_status),
    zwyciezca int references warcaby.kolor (id_kolor)
);

create table warcaby.gracz_gra
(
    id_gracz_gra serial not null primary key,
    id_gracz     int    not null references warcaby.gracz (id_gracz),
    id_gra       int    not null references warcaby.gra (id_gra),
    unique (id_gracz, id_gra),
    wynik        int default 0,
    kolor        int    not null references warcaby.kolor (id_kolor)
);

create table warcaby.ruch
(
    id_ruch      serial not null primary key,
    id_gracz_gra int    not null references warcaby.gracz_gra (id_gracz_gra),
    ruch         varchar check ( ruch ~ '^[A-H][1-8]( [A-H][1-8])+$' ),
    czas_ruchu   timestamp default current_timestamp
);


CREATE OR REPLACE VIEW warcaby.ranking AS
SELECT z.login,
       COUNT(g.id_gra) as liczba_zwyciestw
FROM warcaby.gra g
         JOIN warcaby.gracz_gra gg ON g.id_gra = gg.id_gra AND g.zwyciezca = gg.kolor
         JOIN warcaby.gracz z ON gg.id_gracz = z.id_gracz
GROUP BY z.login
ORDER BY liczba_zwyciestw DESC;

CREATE OR REPLACE VIEW warcaby.otwarte_gry AS
WITH gry AS (SELECT g.id_gra
             FROM warcaby.gra g
                      JOIN warcaby.gracz_gra gg ON g.id_gra = gg.id_gra
             WHERE g.status = (SELECT s.id_status FROM warcaby.status s WHERE nazwa = 'Utworzona')
             GROUP BY g.id_gra
             HAVING count(*) < 2)
SELECT p.login, gry.id_gra, p.id_gracz
FROM gry JOIN warcaby.gracz_gra gg ON gry.id_gra = gg.id_gra
         JOIN warcaby.gracz p ON gg.id_gracz = p.id_gracz;

CREATE OR REPLACE VIEW warcaby.lista_gier AS
WITH czyja_kolej AS (SELECT row_number() over (PARTITION BY gg.id_gra ORDER BY r.czas_ruchu DESC) rn,
       gg.id_gra,
       gg.kolor
FROM warcaby.ruch r
    JOIN warcaby.gracz_gra gg ON r.id_gracz_gra = gg.id_gracz_gra)
SELECT g1.login                             AS login,
       g.id_gra                             AS id_gra,
       kg.nazwa                             AS moj_kolor,
       COALESCE(g2.login, 'Oczekiwanie...') AS przeciwnik,
       s.nazwa                              AS status_gry,

       CASE
           WHEN g.zwyciezca IS NULL THEN '-'
           WHEN g.zwyciezca = gg1.kolor THEN 'Wygrałeś'
           ELSE 'Przegrałeś'
           END                              AS wynik,
       COALESCE(c.kolor, 2) != gg1.kolor AS moja_kolej
FROM warcaby.gracz_gra gg1
         JOIN warcaby.gra g on gg1.id_gra = g.id_gra
         JOIN warcaby.kolor kg on kg.id_kolor = gg1.kolor
         LEFT JOIN warcaby.kolor kz on kz.id_kolor = g.zwyciezca
         JOIN warcaby.gracz g1 on g1.id_gracz = gg1.id_gracz
         JOIN warcaby.status s on s.id_status = g.status
         LEFT JOIN warcaby.gracz_gra gg2 on gg1.id_gra = gg2.id_gra AND gg1.id_gracz != gg2.id_gracz
         LEFT JOIN warcaby.gracz g2 on g2.id_gracz = gg2.id_gracz
         LEFT JOIN czyja_kolej c ON g.id_gra = c.id_gra
WHERE COALESCE(c.rn, 1) = 1;


CREATE OR REPLACE FUNCTION warcaby.utworz_gre(_id_gracz integer)
    RETURNS integer AS
$$
DECLARE
    _id_gra           integer;
    _status_utworzona integer;
BEGIN
    SELECT id_status INTO _status_utworzona FROM warcaby.status WHERE nazwa = 'Utworzona';

    SELECT g.id_gra
    INTO _id_gra
    FROM warcaby.gra g
        JOIN warcaby.gracz_gra gg ON g.id_gra = gg.id_gra
    WHERE g.status = _status_utworzona
    AND gg.id_gracz = _id_gracz;

    IF _id_gra IS NOT NULL THEN
        RETURN _id_gra;
    END IF;

    INSERT INTO warcaby.gra (status)
    VALUES (_status_utworzona)
    RETURNING id_gra INTO _id_gra;

    INSERT INTO warcaby.gracz_gra (id_gracz, id_gra, kolor)
    VALUES (_id_gracz,
            _id_gra,
            (SELECT k.id_kolor FROM warcaby.kolor k WHERE k.nazwa = 'Biały'));

    RETURN _id_gra;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION warcaby.dolacz_do_gry(_id_gry integer, _id_gracza integer)
    RETURNS integer AS
$$
DECLARE
    _status_utworzona integer;
    _id_gracz_gra integer;
BEGIN
    SELECT s.id_status
    INTO _status_utworzona
    FROM warcaby.status s
    WHERE s.nazwa = 'Utworzona';

    IF NOT EXISTS (SELECT 1
                   FROM warcaby.gra g
                   WHERE g.id_gra = _id_gry
                     AND g.status = _status_utworzona) THEN
        RAISE EXCEPTION 'Błędna gra';
    END IF;

    IF EXISTS (SELECT gg.id_gracz
               FROM warcaby.gracz_gra gg
               WHERE gg.id_gra = _id_gry
                 AND gg.id_gracz = _id_gracza) THEN
        RAISE EXCEPTION 'Gracz już uczestniczy w tej grze';
    END IF;

    INSERT INTO warcaby.gracz_gra (id_gracz, id_gra, kolor)
    VALUES (_id_gracza,
            _id_gry,
            (SELECT k.id_kolor FROM warcaby.kolor k WHERE k.nazwa = 'Czarny'))
    RETURNING id_gracz_gra INTO _id_gracz_gra;

    RETURN _id_gracz_gra;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION warcaby.rozpoczecie_gry()
    RETURNS TRIGGER AS
$$
DECLARE
    _status_trawajaca integer;
BEGIN
    IF 2 = (SELECT COUNT(*)
            FROM warcaby.gracz_gra gg
            WHERE gg.id_gra = NEW.id_gra) THEN
        SELECT s.id_status
        INTO _status_trawajaca
        FROM warcaby.status s
        WHERE s.nazwa = 'Trwająca';

        UPDATE warcaby.gra
        SET status = _status_trawajaca
        WHERE gra.id_gra = NEW.id_gra;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_rozpoczecie_gry
    AFTER INSERT
    ON warcaby.gracz_gra
    FOR EACH ROW
EXECUTE FUNCTION warcaby.rozpoczecie_gry();

CREATE OR REPLACE FUNCTION warcaby.sprawdz_ruch_gracza()
    RETURNS TRIGGER AS
$$
DECLARE
    gra           integer;
    kolor         integer;
    ostatni_kolor integer;
BEGIN
    SELECT gg.id_gra, gg.kolor
    FROM warcaby.gracz_gra gg
    WHERE gg.id_gracz_gra = NEW.id_gracz_gra
    INTO gra, kolor;

    SELECT gg.kolor
    INTO ostatni_kolor
    FROM warcaby.ruch r
             JOIN warcaby.gracz_gra gg ON r.id_gracz_gra = gg.id_gracz_gra
    WHERE gg.id_gra = gra
    ORDER BY czas_ruchu DESC
    LIMIT 1;

    IF (ostatni_kolor IS NULL AND (SELECT k.nazwa FROM warcaby.kolor k WHERE k.id_kolor = kolor) != 'Biały') THEN
        RAISE EXCEPTION 'Biały zaczyna';
    END IF;
    IF (kolor = ostatni_kolor) THEN
        RAISE EXCEPTION 'Zły gracz.';
    END IF;

    NEW.ruch := regexp_replace(NEW.ruch, '\s+', ' ', 'g');
    NEW.ruch := upper(btrim(NEW.ruch));

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sprawdz_ruch
    BEFORE INSERT
    ON warcaby.ruch
    FOR EACH ROW
EXECUTE FUNCTION warcaby.sprawdz_ruch_gracza();

CREATE OR REPLACE FUNCTION warcaby.wykonaj_ruch()
    RETURNS TRIGGER AS
$$
DECLARE
    _bicia integer;
BEGIN
    IF (abs(ascii(left(NEW.ruch, 2)) - ascii(split_part(NEW.ruch, ' ', 2))) = 2) THEN
        _bicia := regexp_count(NEW.ruch, '[A-H][1-8]') - 1;
    ELSE
        _bicia := 0;
    END IF;

    UPDATE warcaby.gracz_gra
    SET wynik = COALESCE(wynik, 0) + _bicia
    WHERE id_gracz_gra = NEW.id_gracz_gra;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_wykonaj_ruch
    AFTER INSERT
    ON warcaby.ruch
    FOR EACH ROW
EXECUTE FUNCTION warcaby.wykonaj_ruch();

CREATE OR REPLACE FUNCTION warcaby.zakoncz_gre()
    RETURNS TRIGGER AS
$$
DECLARE
    _status_zakonczona integer;
BEGIN
    SELECT s.id_status
    INTO _status_zakonczona
    FROM warcaby.status s
    WHERE s.nazwa = 'Zakończona';

    UPDATE warcaby.gra
    SET status = _status_zakonczona, zwyciezca = NEW.kolor
    WHERE id_gra = NEW.id_gra;

    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_zakoncz_gre
    BEFORE UPDATE OF wynik
    ON warcaby.gracz_gra
    FOR EACH ROW
    WHEN ( NEW.wynik = 12 )
EXECUTE FUNCTION warcaby.zakoncz_gre();

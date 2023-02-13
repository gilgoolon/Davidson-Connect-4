drop table if exists leaderboard;

CREATE TABLE leaderboard (
    ip VARCHAR(15) NOT NULL UNIQUE PRIMARY KEY,
    wins INT NOT NULL DEFAULT 0,
    losses INT NOT NULL DEFAULT 0,
    ties INT NOT NULL DEFAULT 0
);
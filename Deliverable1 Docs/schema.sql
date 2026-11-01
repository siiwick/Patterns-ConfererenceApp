PRAGMA foreign_keys = ON; --foreign keys enabled on each connection automatically

--if already created, recreate the tables, triggers, and indexes
DROP TRIGGER IF EXISTS trg_registration_capacity_insert;
DROP TRIGGER IF EXISTS trg_registration_capacity_update;
DROP TRIGGER IF EXISTS trg_capacity_reduction;
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS credentials;
DROP TABLE IF EXISTS conferences;
DROP TABLE IF EXISTS users;

--table creation
CREATE TABLE users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL CHECK (length(trim(full_name)) > 0),
    email TEXT NOT NULL COLLATE NOCASE UNIQUE,
    age INTEGER NOT NULL CHECK (age BETWEEN 16 AND 120),
    role TEXT NOT NULL DEFAULT 'PARTICIPANT' CHECK (role IN ('ADMIN', 'PARTICIPANT')),
    created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))
);

CREATE TABLE credentials (
    user_id INTEGER PRIMARY KEY,
    password_hash TEXT NOT NULL,
    updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE conferences (
    conference_id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL CHECK (length(trim(title)) > 0),
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    start_datetime TEXT NOT NULL,
    end_datetime TEXT NOT NULL,
    venue TEXT,
    created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
    CHECK (datetime(start_datetime) IS NOT NULL AND datetime(end_datetime) IS NOT NULL AND datetime(end_datetime) > datetime(start_datetime))
);

CREATE TABLE registrations (
    registration_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    conference_id INTEGER NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('CONFIRMED', 'WAITLISTED')),
    waitlist_position INTEGER,
    registered_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT,
    FOREIGN KEY (conference_id) REFERENCES conferences (conference_id) ON DELETE RESTRICT,
    UNIQUE (user_id, conference_id),
    UNIQUE (conference_id, waitlist_position),
    CHECK ((status = 'CONFIRMED'  AND waitlist_position IS NULL) OR (status = 'WAITLISTED' AND waitlist_position IS NOT NULL AND waitlist_position > 0))
);

--indexes
CREATE INDEX idx_registrations_queue ON registrations (conference_id, status, waitlist_position);
CREATE INDEX idx_registrations_user  ON registrations (user_id);

--triggers
CREATE TRIGGER trg_registration_capacity_insert
BEFORE INSERT ON registrations
WHEN NEW.status = 'CONFIRMED'
BEGIN
    SELECT RAISE(ABORT, 'Conference capacity exceeded')
    WHERE (SELECT COUNT(*) 
           FROM registrations
           WHERE conference_id = NEW.conference_id AND status = 'CONFIRMED') >= (SELECT capacity 
                                                                                 FROM conferences 
                                                                                 WHERE conference_id = NEW.conference_id);
END;

CREATE TRIGGER trg_registration_capacity_update
BEFORE UPDATE OF status ON registrations
WHEN NEW.status = 'CONFIRMED' AND OLD.status != 'CONFIRMED'
BEGIN
    SELECT RAISE(ABORT, 'Conference capacity exceeded')
    WHERE (SELECT COUNT(*) 
           FROM registrations
           WHERE conference_id = NEW.conference_id AND status = 'CONFIRMED') >= (SELECT capacity 
                                                                                 FROM conferences 
                                                                                 WHERE conference_id = NEW.conference_id);
END;

CREATE TRIGGER trg_capacity_reduction
BEFORE UPDATE OF capacity ON conferences
WHEN NEW.capacity < (SELECT COUNT(*) 
                     FROM registrations
                     WHERE conference_id = NEW.conference_id AND status = 'CONFIRMED')
BEGIN
    SELECT RAISE(ABORT, 'Capacity cannot be lower than the number of confirmed participants');
END;

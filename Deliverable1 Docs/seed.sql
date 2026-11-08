INSERT INTO users(full_name,email,age,role) VALUES
    ('John Doe','johndoe@gmail.com',22,'PARTICIPANT'),
    ('Jane Doe','jane@gmail.com',34,'PARTICIPANT'),
    ('Mike Tremblay','miketremblay@icloud.com',40,'PARTICIPANT'),
    ('Sara Jackson','sarajackson@gmail.com',19,'PARTICIPANT'),
    ('Liam SmithS','liamsmith@conference.com',56,'ADMIN');

INSERT INTO credentials(user_id,password_hash) VALUES 
    (1,'passwordHashForJohn'),
    (2,'passwordHashForJane'),
    (3,'paswordHashForMike'),
    (4,'passwordHashForSara'),
    (5,'passwordHashForLiam');

INSERT INTO conferences(title,capacity,start_datetime,end_datetime,venue) VALUES 
    ('Generic Conference',1000,'2026-11-11T10:20:30','2026-11-13T17:20:30','Conference Venue'),
    ('Tiny Conference',2,'2027-01-08T08:20:30','2027-01-10T17:00:00','Tiny Conference Venue');

INSERT INTO registrations(user_id,conference_id,status,waitlist_position) VALUES
    (1,1,'CONFIRMED',NULL),
    (3,1,'CONFIRMED',NULL),
    (5,1,'CONFIRMED',NULL),
    (1,2,'CONFIRMED',NULL),
    (4,2,'CONFIRMED',NULL),
    (2,2,'WAITLISTED',1),
    (3,2,'WAITLISTED',2);
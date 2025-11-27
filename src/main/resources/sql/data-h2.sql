INSERT INTO EMPLOYEES (BIRTH_DATE, EMAIL, NAME, PASSWORD, PHONE)
VALUES ('1990-05-15', 'john.doe@email.com', 'John Doe', '$2a$12$d5ilWhrfWzrZZCdHe6UEVOkvoq3/AxubOzCmJEqGnVLg322GEQtBm', '555-123-4567'), -- pass123
       ('1985-09-20', 'jane.smith@email.com', 'Jane Smith', '$2a$12$ZLT4ASYvwWqFF5U7BITFd.xoeO7QXl6P1uQCZd8gx972bH2Jm4myu', '555-987-6543'), --abc456
       ('1978-03-08', 'bob.jones@email.com', 'Bob Jones', '$2a$12$SM7PotbxMMHTER4V/WskJugM46DnxbO9FQVDOlGLUoRQPPsRFawAm', '555-321-6789'), --qwerty789
       ('1982-11-25', 'alice.white@email.com', 'Alice White', '$2a$12$unouJcXm5/jMUAOqflQJ8OhDXm8OshkfP.scPZ/E1q0V.j0octuNi', '555-876-5432'), --secret567
       ('1995-07-12', 'mike.wilson@email.com', 'Mike Wilson', '$2a$12$Y/ok5K5aBf8iiF4h81u8V.J.gku/OsL2QXV61REod.S4pSbLwG7Ii', '555-234-5678'), --mypassword
       ('1989-01-30', 'sara.brown@email.com', 'Sara Brown', '$2a$12$xqwH5yLM2LgqucFACwtmduG1AmJIZ/3Qq3IAOuG49AWhJXSrwdT76', '555-876-5433'), --letmein123
       ('1975-06-18', 'tom.jenkins@email.com', 'Tom Jenkins', '$2a$12$1frkJM5ZFzA69jNsOKZc3OYqCCnz9UKnpR/CSA9kq8fL1HGghw4YG', '555-345-6789'), --pass4321
       ('1987-12-04', 'lisa.taylor@email.com', 'Lisa Taylor', '$2a$12$JSAi0froA6tI8qMEvB0cR.RqQa39E1Dva7V7knPa5qmUMjDxnqdr2', '555-789-0123'), --securepwd
       ('1992-08-22', 'david.wright@email.com', 'David Wright', '$2a$12$OGrknp3WqmV2t9kDqhwwF.pGgLhBnwisM0hWO4DOdhWhnfrr10P3S', '555-456-7890'), --access123
       ('1980-04-10', 'emily.harris@email.com', 'Emily Harris', '$2a$12$fvXRv8egCwUyKzjjLamu0Or.UtUgg8cDhTuMVr7/.D7YVfgyMqpye', '555-098-7654'); --1234abcd

INSERT INTO CLIENTS (BALANCE, EMAIL, NAME, PASSWORD)
VALUES (1000.00, 'client1@example.com', 'Medelyn Wright', '$2a$12$hvF0GlIlJklNhKg3rRjOW.IuEzNZZbnSmzWlW2MCDUF5lxC8Vb5bC'), --password123
       (1500.50, 'client2@example.com', 'Landon Phillips', '$2a$12$kHoxl.IcncNt/1xeV3XSAeOie.DQN6PKCRjwqIf0Q4Xt4D5mG2Yc2'), --securepass
       (800.75, 'client3@example.com', 'Harmony Mason', '$2a$12$0C1Xq3505lTbTuvDSGUek.WJS9u5FxS9bBxabMOetFj7zSdWUXZGO'), --abc123
       (1200.25, 'client4@example.com', 'Archer Harper', '$2a$12$U9G0Y.7wHk4r38SONc9DNOYa8V2TFZPCLF9Tr7glG61qloP.zXjmS'), --pass456
       (900.80, 'client5@example.com', 'Kira Jacobs', '$2a$12$3m.mykdyDxIGnZNzLUTdq.4PL/R5BfhC4wA3nOyZ/..4meIjLMwT6'), --letmein789
       (1100.60, 'client6@example.com', 'Maximus Kelly', '$2a$12$QnZEnlWJVvCcwZgAbs./ceojDWytpeYY5YgRWHKcAlb1heG8G8Uhy'), --adminpass
       (1300.45, 'client7@example.com', 'Sierra Mitchell', '$2a$12$lXqdOebf.TbKDlysW0L2leM636l9Y3bWzyN7KDUw.YQF8Csmq23Za'), --mypassword
       (950.30, 'client8@example.com', 'Quinton Saunders', '$2a$12$CD0eG22aGmzVGlx.W5mlf.cVnyMsMJo62jCM5n9leoakgtrpARM0i'), --test123
       (1050.90, 'client9@example.com', 'Amina Clarke', '$2a$12$kp.Wu35sSmCjcGyAna0zaOA1OdLbmObXZcTPCFrqs1Xb07jpEujVm'), --qwerty123
       (880.20, 'client10@example.com', 'Bryson Chavez', '$2a$12$2TjxAFGulUpdDoNE1SL0W.2Ue6BuMlw239fm13w.k56aHmesYOQGC'); --pass789

INSERT INTO BOOKS (name, genre, age_group, price, publication_date, author, number_of_pages, characteristics,description, language)
VALUES ('The Hidden Treasure', 'Adventure', 'ADULT', 24.99, '2018-05-15', 'Emily White', 400, 'Mysterious journey','An enthralling adventure of discovery', 'ENGLISH'),
       ('Echoes of Eternity', 'Fantasy', 'TEEN', 16.50, '2011-01-15', 'Daniel Black', 350, 'Magical realms', 'A spellbinding tale of magic and destiny', 'ENGLISH'),
       ('Whispers in the Shadows', 'Mystery', 'ADULT', 29.95, '2018-08-11', 'Sophia Green', 450, 'Intriguing suspense','A gripping mystery that keeps you guessing', 'ENGLISH'),
       ('The Starlight Sonata', 'Romance', 'ADULT', 21.75, '2011-05-15', 'Michael Rose', 320, 'Heartwarming love story','A beautiful journey of love and passion', 'ENGLISH'),
       ('Beyond the Horizon', 'Science Fiction', 'CHILD', 18.99, '2004-05-15', 'Alex Carter', 280,'Interstellar adventure', 'An epic sci-fi adventure beyond the stars', 'ENGLISH'),
       ('Dancing with Shadows', 'Thriller', 'ADULT', 26.50, '2015-05-15', 'Olivia Smith', 380, 'Suspenseful twists','A thrilling tale of danger and intrigue', 'ENGLISH'),
       ('Voices in the Wind', 'Historical Fiction', 'ADULT', 32.00, '2017-05-15', 'William Turner', 500,'Rich historical setting', 'A compelling journey through time', 'ENGLISH'),
       ('Serenade of Souls', 'Fantasy', 'TEEN', 15.99, '2013-05-15', 'Isabella Reed', 330, 'Enchanting realms','A magical fantasy filled with wonder', 'ENGLISH'),
       ('Silent Whispers', 'Mystery', 'ADULT', 27.50, '2021-05-15', 'Benjamin Hall', 420, 'Intricate detective work','A mystery that keeps you on the edge', 'ENGLISH'),
       ('Whirlwind Romance', 'Romance', 'OTHER', 23.25, '2022-05-15', 'Emma Turner', 360, 'Passionate love affair','A romance that sweeps you off your feet', 'ENGLISH');

INSERT INTO CLIENT_BLOCK_STATUS (client_email, is_blocked)
VALUES ('client1@example.com', false),
       ('client2@example.com', false),
       ('client3@example.com', false),
       ('client4@example.com', false),
       ('client5@example.com', false),
       ('client6@example.com', false),
       ('client7@example.com', false),
       ('client8@example.com', false),
       ('client9@example.com', false),
       ('client10@example.com', false);

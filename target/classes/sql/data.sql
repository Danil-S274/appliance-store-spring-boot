INSERT INTO manufacturer(name)
VALUES ('Samsung'),
       ('LG');

INSERT INTO client(name, email, password, enabled, card_last4, card_hash, balance)
    VALUES ('Mercury', 'mercury@gmail.com', '$2a$10$eCAa/G8u09w.lyzc4u/jI.Tn4J2sJVAk/HGi8FVZrfNZ.lBluc3gi', true,
        '1111', NULL, 500.00);

INSERT INTO employee(name, email, password, enabled, department)
VALUES ('Danil', 'danil@epam.com', '$2a$10$eCAa/G8u09w.lyzc4u/jI.Tn4J2sJVAk/HGi8FVZrfNZ.lBluc3gi', true,
        'Sales');

INSERT INTO appliance(name, category, model, manufacturer_id, power_type, characteristic, description, power, price)
VALUES ('Vacuum Cleaner', 'BIG', 'VC-100', 1, 'AC220', 'Good suction', 'Nice vacuum', 1200, 99.99);

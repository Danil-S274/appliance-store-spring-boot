INSERT INTO manufacturer(name)
VALUES
    ('Samsung'),
    ('LG'),
    ('Bosch'),
    ('Philips'),
    ('Electrolux');


INSERT INTO client(name, email, password, enabled, card_last4, card_hash, balance)
VALUES
    ('Mercury', 'mercury@gmail.com',
     '$2a$10$eCAa/G8u09w.lyzc4u/jI.Tn4J2sJVAk/HGi8FVZrfNZ.lBluc3gi',
     true, '1111', NULL, 500.00),

    ('Alice', 'alice@gmail.com',
     '$2a$10$eCAa/G8u09w.lyzc4u/jI.Tn4J2sJVAk/HGi8FVZrfNZ.lBluc3gi',
     true, '2222', NULL, 1200.00),

    ('Bob', 'bob@gmail.com',
     '$2a$10$eCAa/G8u09w.lyzc4u/jI.Tn4J2sJVAk/HGi8FVZrfNZ.lBluc3gi',
     true, '3333', NULL, 300.00),

    ('Charlie', 'charlie@gmail.com',
     '$2a$10$eCAa/G8u09w.lyzc4u/jI.Tn4J2sJVAk/HGi8FVZrfNZ.lBluc3gi',
     true, '4444', NULL, 800.00);


INSERT INTO employee(name, email, password, enabled, department)
VALUES
    ('Danylo', 'danylo@epam.com',
     '$2a$10$eCAa/G8u09w.lyzc4u/jI.Tn4J2sJVAk/HGi8FVZrfNZ.lBluc3gi',
     true, 'Sales'),

    ('Anna', 'anna@epam.com',
     '$2a$10$eCAa/G8u09w.lyzc4u/jI.Tn4J2sJVAk/HGi8FVZrfNZ.lBluc3gi',
     true, 'Support');


INSERT INTO appliance
(name, category, model, manufacturer_id, power_type, characteristic, description, power, price)
VALUES
-- VACUUM CLEANERS
('Vacuum Cleaner Pro', 'VACUUM_CLEANERS', 'VC-100', 1, 'AC220',
 'Strong suction', 'Powerful vacuum cleaner', 1200, 99.99),

('Robot Vacuum', 'VACUUM_CLEANERS', 'RV-20', 4, 'ACCUMULATOR',
 'Smart navigation', 'Robot vacuum cleaner', 60, 249.99),

-- WASHING MACHINES
('Washing Machine 7kg', 'WASHING_MACHINES', 'WM-700', 2, 'AC220',
 'Energy efficient', 'Front load washing machine', 2000, 399.99),

-- DISHWASHERS
('Dishwasher Slim', 'DISHWASHERS', 'DW-45', 3, 'AC220',
 'Quiet operation', 'Slim dishwasher', 1800, 449.99),

-- REFRIGERATORS
('Refrigerator XL', 'REFRIGERATORS', 'RF-500', 1, 'AC220',
 'No frost', 'Large refrigerator', 150, 899.99),

-- MICROWAVES
('Microwave Oven', 'MICROWAVES', 'MW-800', 5, 'AC220',
 'Quick heating', '800W microwave oven', 800, 129.99),

-- OVENS
('Electric Oven', 'OVENS', 'OV-60', 3, 'AC220',
 'Multi-mode', 'Electric built-in oven', 2500, 599.99),

-- COOKTOPS
('Gas Cooktop', 'COOKTOPS', 'GC-4', 2, 'GAS',
 '4 burners', 'Gas cooktop', 0, 299.99),

-- AIR CONDITIONERS
('Air Conditioner', 'AIR_CONDITIONERS', 'AC-12', 1, 'AC220',
 'Inverter', 'Split air conditioner', 3500, 699.99),

-- SMALL APPLIANCES
('Iron Steam', 'IRONS', 'IR-300', 4, 'AC220',
 'Steam boost', 'Steam iron', 2400, 59.99),

('Coffee Maker', 'COFFEE_MAKERS', 'CM-10', 5, 'AC220',
 'Espresso', 'Coffee machine', 1500, 199.99),

('Electric Kettle', 'KETTLES', 'KT-2L', 4, 'AC220',
 'Auto shut-off', '2L electric kettle', 2200, 39.99);


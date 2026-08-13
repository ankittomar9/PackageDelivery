-- 1. High-Priority Return (H2 will auto-assign request_id = 1)
INSERT INTO process_request (user_name, contact_number, credit_card_number, component_type, component_name, quantity_of_defective, is_priority_request)
VALUES ('john_doe', 9876543210, 4532890123456789, 'Integral', 'MacBook Pro M3 Display Assembly', 1, true);

-- 2. Standard Return (H2 will auto-assign request_id = 2)
INSERT INTO process_request (user_name, contact_number, credit_card_number, component_type, component_name, quantity_of_defective, is_priority_request)
VALUES ('sarah_connor', 9823019283, 5412759081234567, 'Accessory', 'Sony WH-1000XM5 USB-C Charging Cable', 2, false);

-- 3. High-Priority Return (H2 will auto-assign request_id = 3)
INSERT INTO process_request (user_name, contact_number, credit_card_number, component_type, component_name, quantity_of_defective, is_priority_request)
VALUES ('david_miller', 9123456780, 4000123456789010, 'Integral', 'Samsung Galaxy S24 Ultra Main Logic Board', 1, true);

-- 4. Standard Return (H2 will auto-assign request_id = 4)
INSERT INTO process_request (user_name, contact_number, credit_card_number, component_type, component_name, quantity_of_defective, is_priority_request)
VALUES ('emily_watson', 9711223344, 3782822463100055, 'Accessory', 'Dell 130W USB-C Power Adapter', 1, false);
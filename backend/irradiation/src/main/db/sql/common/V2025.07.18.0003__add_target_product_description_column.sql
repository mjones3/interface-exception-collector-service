-- Add target_product_description column to lk_product_determination table
ALTER TABLE irradiation.lk_product_determination 
ADD COLUMN IF NOT EXISTS target_product_description VARCHAR(100);

-- Replace all records with updated data
TRUNCATE TABLE irradiation.lk_product_determination RESTART IDENTITY;

INSERT INTO irradiation.lk_product_determination (source_product_code, target_product_code, target_product_description) VALUES
('E003300', 'E003200', 'IRR CPD LR WB'),
('E018100', 'E017900', 'IRR CPD LR RBC'),
('E033600', 'E033200', 'IRR AS1 LR RBC'),
('E038200', 'E037900', 'AS3 IRR LR RBC'),
('E042400', 'E042000', 'AS5 IRR LR RBC'),
('E068500', 'E066800', 'APH AS3 IRR LR RBC C1'),
('E068600', 'E066900', 'APH AS3 IRR LR RBC C2'),
('E070100', 'E070400', 'IRR FFP'),
('E071300', 'E071600', 'IRR FFP'),
('E355600', 'E354100', 'WSH IRR PLT BM 36'),
('E355800', 'E354300', 'WSH IRR PLT BM36 C1'),
('E355900', 'E354400', 'WSH IRR PLT BM36 C2'),
('E453100', 'E452600', 'IRR AS1 LR RBC'),
('E453200', 'E452700', 'IRR AS1 LR RBC C1'),
('E453300', 'E452800', 'IRR AS1 LR RBC C2'),
('E414000', 'E456200', 'WSH IRR RBC C1'),
('E456600', 'E456200', 'WSH IRR RBC C1'),
('E456700', 'E456300', 'WSH IRR RBC C2'),
('E356000', 'E640800', 'WSH IRR PLT BM36 C3'),
('EA00700', 'EA01500', 'IRR APH PLT BM36'),
('EA00800', 'EA01600', 'IRR APH PLT BM36 C1'),
('EA00900', 'EA01700', 'IRR APH PLT BM36 C2'),
('EA01000', 'EA01800', 'IRR APH PLT BM36 C3'),
('EA01100', 'EA01900', 'IRR APH PLT LR VC BM36'),
('EA01200', 'EA02000', 'IRR APH PLT LR VC BM36 C1'),
('EA01300', 'EA02100', 'IRR APH PLT VC BM36 C2'),
('EA01400', 'EA02200', 'IRR APH PLT VC BM36 C3'),
('EA13600', 'EA15200', 'IRR APH PASPLT BM36');

-- Update sequence to prevent ID conflicts
SELECT setval('lk_product_determination_id_seq', (SELECT MAX(id) FROM irradiation.lk_product_determination));
ALTER TABLE `parking_charge_rule`
    ADD COLUMN `follow_unit_price` decimal(10, 2) NOT NULL DEFAULT 2.00 COMMENT '后续计费单位价格' AFTER `unit_price`;

UPDATE `parking_charge_rule`
SET `follow_unit_price` = 2.00
WHERE `follow_unit_price` IS NULL OR `follow_unit_price` = 0.00;

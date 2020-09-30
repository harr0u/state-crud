CREATE TABLE `entities`
(
    `id`    VARCHAR(100) PRIMARY KEY AUTO_INCREMENT,
    `name`  VARCHAR(100) NOT NUll,
    `state` INT         NOT NULL
);


CREATE TABLE `entity_events`
(
    `id` VARCHAR(100) PRIMARY KEY AUTO_INCREMENT,
    `entity_id` VARCHAR(100) NOT NULL,
    `from` INT NOT NULL,
    `to` INT NOT NULL,
    FOREIGN KEY (entity_id)  REFERENCES entities (id) ON DELETE CASCADE
);

CREATE TABLE `transitions`
(
    `from` INT NOT NULL,
    `to` INT NOT NULL,
    PRIMARY KEY (`from`, `to`)
);
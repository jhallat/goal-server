CREATE TABLE status (
    id int PRIMARY KEY,
    key VARCHAR ( 15 ) UNIQUE NOT NULL,
    description VARCHAR ( 35 ) UNIQUE NOT NULL
);

INSERT INTO status (id, key, description) VALUES (1, 'PENDING', 'Pending');
INSERT INTO status (id, key, description) VALUES (2, 'IN_PROGRESS', 'In Progress');
INSERT INTO status (id, key, description) VALUES (3, 'COMPLETED', 'Completed');

ALTER TABLE tasks
ADD COLUMN status_id int DEFAULT 1;

UPDATE tasks
SET status_id = 1
WHERE completed = false;

UPDATE tasks
SET status_id = 3
WHERE completed = true;

ALTER TABLE tasks
DROP COLUMN completed;

ALTER TABLE tasks
ADD FOREIGN KEY (status_id)
    REFERENCES status (id);
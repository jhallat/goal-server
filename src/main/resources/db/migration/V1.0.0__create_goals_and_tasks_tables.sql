CREATE TABLE goals (
  id serial PRIMARY KEY,
  description VARCHAR ( 255 ) UNIQUE NOT NULL
);

CREATE TABLE tasks (
  id serial PRIMARY KEY,
  goal_id int,
  description VARCHAR ( 255 ) UNIQUE NOT NULL,
  completed boolean,
  FOREIGN KEY (goal_id)
      REFERENCES goals (id)
);
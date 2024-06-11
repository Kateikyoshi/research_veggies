CREATE TABLE IF NOT EXISTS UZER (
   n_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
   c_name VARCHAR(30),
   c_password VARCHAR(100),
   c_role VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS VEGGIE_TYPE (
    n_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    c_name VARCHAR(20)
);

INSERT INTO VEGGIE_TYPE(n_id, c_name)
VALUES(0, 'TOMATO');
INSERT INTO VEGGIE_TYPE(n_id, c_name)
VALUES(1, 'CUCUMBER');
INSERT INTO VEGGIE_TYPE(n_id, c_name)
VALUES(2, 'BANANA');
INSERT INTO VEGGIE_TYPE(n_id, c_name)
VALUES(3, 'ONION');

CREATE TABLE IF NOT EXISTS VEGGIES (
    n_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    c_type INT,
    c_name VARCHAR(30),
    FOREIGN KEY(c_type) REFERENCES veggie_type(n_id)
);
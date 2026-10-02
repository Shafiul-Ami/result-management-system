CREATE TABLE IF NOT EXISTS teachers (
     id  SERIAL PRIMARY KEY,
name  VARCHAR(100) NOT NULL, 
email VARCHAR(150) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

CREATE TABLE IF NOT EXISTS students(
    id   SERIAL PRIMARY KEY,
    roll_no VARCHAR(150) NOT NULL UNIQUE,
     name       VARCHAR(100) NOT NULL,
    class_name VARCHAR(255) NOT NULL,
    dob DATE NOT NULL,
    created_by int references teachers(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

CREATE TABLE IF NOT EXISTS marks(
    student_id INT PRIMARY KEY REFERENCES students(id) ON DELETE CASCADE,
    sub1 INT NOT NULL CHECK (sub1 BETWEEN 0 AND 100),
    sub2 INT NOT NULL CHECK (sub2 BETWEEN 0 AND 100),
    sub3 INT NOT NULL CHECK (sub3 BETWEEN 0 AND 100),
    sub4 INT NOT NULL CHECK (sub4 BETWEEN 0 AND 100),
    sub5 INT NOT NULL CHECK (sub5 BETWEEN 0 AND 100),
    updated_by int references teachers(id) ON DELETE SET NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

    CREATE TABLE IF NOT EXISTS notices ( 
         id          SERIAL PRIMARY KEY, 
         title       VARCHAR(200) NOT NULL, 
         message     TEXT         NOT NULL,
         result_date DATE, 
          class_name  VARCHAR(50),
         created_by  INT REFERENCES teachers(id) ON DELETE SET NULL,
          created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP);


# LifeGPS – database & how to check it

You do NOT need to create tables. Spring Boot creates them automatically
(`spring.jpa.hibernate.ddl-auto=update`). You only need MySQL running.

## 1. Create the database (optional – the app also does it)
    CREATE DATABASE IF NOT EXISTS lifegps_db;

## 2. Set your MySQL password and start the backend (PowerShell, in this folder)
    $env:DB_PASSWORD='YOUR_MYSQL_PASSWORD'
    .\mvnw.cmd spring-boot:run

Check http://localhost:8080/api/health  ->  {"status":"ok",...}

## 3. Look inside the database
Using MySQL Workbench, or the command line:
    mysql -u root -p
    USE lifegps_db;
    SHOW TABLES;                       -- users, documents, life_events, reminders
    SELECT id, name, email, phone FROM users;
    SELECT * FROM documents;
    SELECT * FROM life_events;
    SELECT * FROM reminders;

Passwords are stored as BCrypt hashes (never plain text) - that is normal.
Uploaded files are saved in the `uploads/lifegps` folder next to the backend.

## 4. Reset everything (deletes all data!)
    DROP DATABASE lifegps_db;
Then restart the backend – it recreates empty tables.

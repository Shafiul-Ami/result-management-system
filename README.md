# MGM – School Result Management System

A web application where college teachers manage students and marks for 5 subjects, and publish a result date. On that date, students can check their marksheet online with their roll number and date of birth.

Built as a learning project to practise the full stack without frameworks: **HTML, CSS, JavaScript, Java Servlets, JDBC and PostgreSQL**.

## Features

**Teachers** (login required)
- Register with a college access code, then log in (passwords are hashed with salted PBKDF2)
- Add, edit and delete students (roll number, name, class, date of birth)
- Enter marks for 5 subjects, with a live preview of total, percentage, grade and pass/fail
- Publish notices, including **result notices** with a release date for one class or all classes

**Students** (no login)
- Read the notice board
- Check their result with roll number + date of birth
- Before the release date they see when results will be published; after it, the full marksheet (printable / save as PDF)

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | HTML, CSS, vanilla JavaScript (`fetch` + JSON) |
| Backend | Java 17+, Jakarta Servlets 6 (Tomcat 10.1) |
| Database | PostgreSQL via JDBC |
| Build | `build.ps1` (javac + jar, no Maven) or `Dockerfile` |

## How it works

```
Browser (HTML/JS) ──fetch──► Servlet ──► DAO ──JDBC──► PostgreSQL
        ▲                       │
        └─────── JSON ◄─────────┘
```

- `/api/public/*` – anyone (notices, result check)
- `/api/auth/*` – login, register, logout
- `/api/teacher/*` – teachers only, protected by `AuthFilter` (session check)

**Grading:** each subject is out of 100 and the pass mark is 40. Failing any subject means FAIL (grade F). Otherwise: A+ ≥ 90%, A ≥ 80, B+ ≥ 70, B ≥ 60, C ≥ 50, D below 50.

## Project structure

```
src/main/java/com/resultmanage/
  util/      AppConfig, DBUtil, JsonUtil, PasswordUtil, ResultCalc
  model/     Teacher, Student, Notice
  dao/       TeacherDao, StudentDao, MarksDao, NoticeDao   (all SQL)
  servlet/   Auth, Student, Marks, Notice, PublicNotice, PublicResult (+ BaseServlet)
  filter/    AuthFilter
  listener/  AppInitListener  (creates tables at startup)
src/main/resources/app.properties.example
src/main/webapp/  index, notices, result, login, dashboard (+ css/, js/, WEB-INF/)
```

## Run it locally

1. Install **JDK 17+**, **PostgreSQL** and **Apache Tomcat 10.1**.
2. Create the database:
   ```sql
   CREATE DATABASE result_management;
   ```
   The tables are created automatically when the app starts (`WEB-INF/schema.sql`).
3. Copy `src/main/resources/app.properties.example` to `app.properties` in the same folder and fill in your database password and a teacher access code.
4. Build and deploy (PowerShell):
   ```powershell
   $env:CATALINA_HOME = "C:\path\to\apache-tomcat-10.1.x"
   powershell -ExecutionPolicy Bypass -File build.ps1
   ```
5. Start Tomcat (`bin\startup.bat`) and open http://localhost:8080/ResultManagementSystem/
6. Register a teacher with your access code.

## Deploy online

The `Dockerfile` builds the app and runs it on Tomcat 10.1 as `ROOT.war`. On a host such as Render, set these environment variables:

`DB_URL` (e.g. `jdbc:postgresql://HOST/DB?sslmode=require`), `DB_USER`, `DB_PASSWORD`, `TEACHER_ACCESS_CODE`, and `PORT=8080`.

## Security notes

- Passwords are stored as salted PBKDF2 hashes, never in plain text.
- All SQL uses `PreparedStatement` (SQL injection safe); all output is escaped in the browser (XSS safe).
- Results are released by the **server** only after the result date.
- Secrets live in `app.properties` / environment variables, never in the code.
- Roll number + date of birth is a simple check suitable for a learning project. Use demo data only.

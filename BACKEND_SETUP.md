# LifeGPS backend — local setup

## 1. Requirements
- Java 17 or newer
- MySQL Server running
- This folder's Maven wrapper (`mvnw.cmd`)

## 2. Create the database (optional)
The JDBC URL creates `lifegps_db` automatically if your MySQL account has permission. You can also run:
```sql
CREATE DATABASE lifegps_db;
```

## 3. Configure MySQL on Windows
Open PowerShell in this folder and set your session values (change the password to your own MySQL root password):
```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/lifegps_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USERNAME='root'
$env:DB_PASSWORD='YOUR_MYSQL_PASSWORD'
$env:LIFEGPS_JWT_SECRET='ReplaceWithYourOwnLongRandomSecretBeforeDeployment123456789'
.\mvnw.cmd spring-boot:run
```
If your MySQL root account has no password, set `$env:DB_PASSWORD=''`. These variables apply to that PowerShell window only. Never commit real passwords or JWT secrets.

## 4. Check the API
Open `http://localhost:8080/api/health`. Expected response:
```json
{"status":"ok","application":"LifeGPS API"}
```

## 5. Main API endpoints
- `POST /api/auth/register` — JSON: `name`, `email`, `password`, optional `phone`
- `POST /api/auth/login` — JSON: `email`, `password`; response includes `token` and safe user details
- `GET /api/profile` — requires `Authorization: Bearer <token>`
- `GET/POST/PUT/DELETE /api/documents` — requires token; file upload is `POST /api/documents/upload`
- `GET/POST/PUT/DELETE /api/life-events` — requires token
- `GET/POST /api/reminders`, `PUT /api/reminders/{id}/status`, `DELETE /api/reminders/{id}` — requires token
- `GET/PUT /api/profile` — view / update name, phone, notification preferences
- `POST /api/assistant/ask` — JSON: `question`; returns guidance based on your saved data

Use `Authorization: Bearer <token>` for protected endpoints.

## Important status notes
- The frontend (LifeGPS folder) is now connected to every endpoint below.
- Email-based forgot-password delivery is not implemented in this backend yet; it requires a configured mail provider and a secure one-time reset-token flow. Do not show a success message claiming an email was sent until that flow is added.
- The default JWT secret is only for local development. Set `LIFEGPS_JWT_SECRET` to a long random secret before any deployment.
- Do not deploy with development database credentials.

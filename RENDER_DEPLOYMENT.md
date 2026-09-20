# EventFlow Backend - Render Deployment Guide

This guide provides complete, step-by-step instructions for deploying your **EventFlow Spring Boot backend** on **Render** using Docker, connecting it to an external MySQL database, and integrating it with your **Vercel React frontend**.

---

## Table of Contents
1. [Prerequisites & External MySQL Setup](#1-prerequisites--external-mysql-setup)
2. [Step 1: Push Backend Changes to GitHub](#step-1-push-backend-changes-to-github)
3. [Step 2: Create a New Web Service on Render](#step-2-create-a-new-web-service-on-render)
4. [Step 3: Connect GitHub Repository](#step-3-connect-github-repository)
5. [Step 4: Configure Web Service Settings & Docker](#step-4-configure-web-service-settings--docker)
6. [Step 5: Configure Health Check Path](#step-5-configure-health-check-path)
7. [Step 6: Configure Environment Variables in Render](#step-6-configure-environment-variables-in-render)
8. [Step 7: Deploy the Backend](#step-7-deploy-the-backend)
9. [Step 8: First-Organizer Setup for Fresh Database](#step-8-first-organizer-setup-for-fresh-database)
10. [Step 9: Test Deployed Backend APIs with Postman](#step-9-test-deployed-backend-apis-with-postman)
11. [Step 10: Connect Vercel React Frontend to Render Backend](#step-10-connect-vercel-react-frontend-to-render-backend)
12. [Troubleshooting Guide](#troubleshooting-guide)

---

## 1. Prerequisites & External MySQL Setup

Because Render does not provide a managed MySQL service natively, your Spring Boot backend connects to a remote cloud-hosted MySQL database.

Recommended Free/Cloud MySQL Providers:
- **TiDB Serverless** (Free tier with generous limits): [https://tidbcloud.com](https://tidbcloud.com)
- **Aiven for MySQL** (Free trial / developer tier): [https://aiven.io](https://aiven.io)
- **Clever Cloud MySQL** (Free 20MB addon): [https://www.clever-cloud.com](https://www.clever-cloud.com)
- **Railway MySQL** (Developer plan): [https://railway.app](https://railway.app)

When creating your remote MySQL database:
1. Create a database named `eventflow_db` (or note your provider's database name).
2. Note down your credentials:
   - **Host** (e.g., `gateway01.us-east-1.prod.aws.tidbcloud.com`)
   - **Port** (usually `3306` or `4000`)
   - **Database Name** (e.g., `eventflow_db`)
   - **User** (e.g., `root` or `xxxx.root`)
   - **Password** (your database password)

---

## Step 1: Push Backend Changes to GitHub

Open a terminal in your project directory and run:

```bash
# Check status of modified and created files
git status

# Stage all files
git add .

# Commit changes
git commit -m "feat: configure Dockerfile, health check, and environment variables for Render deployment"

# Push to your GitHub repository
git push origin main
```

*(If your default branch is `master`, use `git push origin master`)*.

---

## Step 2: Create a New Web Service on Render

1. Log in to your [Render Dashboard](https://dashboard.render.com/).
2. Click the **"New +"** button in the top navigation bar.
3. Select **"Web Service"**.

---

## Step 3: Connect GitHub Repository

1. Choose **"Build and deploy from a Git repository"** and click **Next**.
2. Under "Connect a repository", search for and select your `eventflow-backend` repository.

---

## Step 4: Configure Web Service Settings & Docker

Fill in the Web Service configuration fields:

| Setting | Recommended Value | Description |
| :--- | :--- | :--- |
| **Name** | `eventflow-backend` | Name of your web service on Render |
| **Region** | Closest to your database (e.g., *Singapore*, *Oregon (US West)*, *Frankfurt*) | Minimizes database query latency |
| **Branch** | `main` (or `master`) | Branch to trigger auto-deployments |
| **Root Directory** | Leave blank (or `eventflow-backend` if repo has nested folder) | Path containing `Dockerfile` and `pom.xml` |
| **Runtime** | **Docker** | Uses the multi-stage `Dockerfile` |
| **Dockerfile Path** | `./Dockerfile` | Relative to the Root Directory |
| **Instance Type** | **Free** (or Starter/Standard) | Free tier includes 512MB RAM |

---

## Step 5: Configure Health Check Path

1. Under the **"Advanced"** settings dropdown in Render:
2. Find the **Health Check Path** field.
3. Enter:
   ```
   /api/health
   ```

---

## Step 6: Configure Environment Variables in Render

In the **"Environment Variables"** section, click **"Add Environment Variable"** for each of the following:

| Key | Example Value | Description |
| :--- | :--- | :--- |
| `MYSQLHOST` | `gateway01.us-east-1.prod.aws.tidbcloud.com` | Remote MySQL server hostname |
| `MYSQLPORT` | `3306` | Remote MySQL server port (`3306` or `4000`) |
| `MYSQLDATABASE` | `eventflow_db` | Remote database name |
| `MYSQLUSER` | `xxxx.root` | Remote MySQL username |
| `MYSQLPASSWORD` | `your_actual_db_password` | Remote MySQL password |
| `JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` | 256-bit secure secret key (min 32 characters) |
| `JWT_EXPIRATION` | `86400000` | Token expiration in ms (`86400000` = 24 hours) |
| `FRONTEND_URL` | `https://your-eventflow-app.vercel.app` | Your deployed Vercel frontend URL (no trailing slash) |
| `JAVA_TOOL_OPTIONS` | `-Xms256m -Xmx400m -XX:+UseSerialGC` | *(Optional)* Optimizes JVM memory for Render 512MB RAM |

> [!NOTE]
> `PORT` is automatically injected by Render.

---

## Step 7: Deploy the Backend

1. Click **"Create Web Service"**.
2. Render will build the Docker container and start the Spring Boot application.
3. Once the logs show `Started EventflowBackendApplication` and `Your service is live 🎉`, your backend is ready.

---

## Step 8: First-Organizer Setup for Fresh Database

Because organizer registration (`POST /api/auth/register/organizer`) is securely restricted to existing organizers (`hasRole("ORGANIZER")`), follow this standard procedure to create your first organizer in a fresh database:

1. Register your initial account via public registration:
   - **Endpoint**: `POST /api/auth/register`
   - **Body**:
     ```json
     {
       "name": "Admin Organizer",
       "email": "admin@eventflow.com",
       "password": "SecurePassword123!"
     }
     ```
2. Log in to your remote MySQL database console (TiDB Cloud SQL Editor, Aiven Console, or MySQL Workbench) and promote the user to `ORGANIZER`:
   ```sql
   UPDATE users SET role = 'ORGANIZER' WHERE email = 'admin@eventflow.com';
   ```
3. Log in via `POST /api/auth/login` to obtain an `ORGANIZER` JWT token.
4. From now on, this organizer can create events and create additional organizers using `POST /api/auth/register/organizer`.

---

## Step 9: Test Deployed Backend APIs with Postman

Replace `https://eventflow-backend-xxxx.onrender.com` with your deployed Render URL.

### 1. Health Check (Public)
- **Method**: `GET`
- **URL**: `https://eventflow-backend-xxxx.onrender.com/api/health`
- **Expected Response**: `200 OK`
  ```json
  {
    "status": "UP",
    "application": "EventFlow Backend"
  }
  ```

---

### 2. User Registration (Public)
- **Method**: `POST`
- **URL**: `https://eventflow-backend-xxxx.onrender.com/api/auth/register`
- **Headers**: `Content-Type: application/json`
- **Body** (Raw JSON):
  ```json
  {
    "name": "Jane Doe",
    "email": "jane@example.com",
    "password": "Password123!"
  }
  ```
- **Expected Response**: `200 OK`

---

### 3. User / Organizer Login (Public)
- **Method**: `POST`
- **URL**: `https://eventflow-backend-xxxx.onrender.com/api/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body** (Raw JSON):
  ```json
  {
    "email": "jane@example.com",
    "password": "Password123!"
  }
  ```
- **Expected Response**: `200 OK`
  ```json
  {
    "id": 1,
    "name": "Jane Doe",
    "email": "jane@example.com",
    "role": "USER",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
  }
  ```
- *Save the returned `token` for subsequent authenticated requests.*

---

### 4. Create Event (Organizer Role)
- **Method**: `POST`
- **URL**: `https://eventflow-backend-xxxx.onrender.com/api/events`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <ORGANIZER_JWT_TOKEN>`
- **Body** (Raw JSON):
  ```json
  {
    "title": "Tech Conference 2026",
    "description": "Annual technology and software development summit",
    "location": "San Francisco Convention Center",
    "eventDate": "2026-11-15",
    "startTime": "09:30:00",
    "capacity": 500
  }
  ```
- **Expected Response**: `200 OK` with created event JSON containing `id: 1`.

---

### 5. Fetch Public Events (Public)
- **Method**: `GET`
- **URL**: `https://eventflow-backend-xxxx.onrender.com/api/events`
- **Expected Response**: `200 OK` with array of events.

---

### 6. Event Registration (Authenticated User)
- **Method**: `POST`
- **URL**: `https://eventflow-backend-xxxx.onrender.com/api/registrations/events/1`
- **Headers**:
  - `Authorization: Bearer <USER_JWT_TOKEN>`
- **Expected Response**: `200 OK` with registration object (contains `id`, `registrationDate`, `status`, `event`, `user`).

---

### 7. Generate Ticket (Authenticated User)
- **Method**: `POST`
- **URL**: `https://eventflow-backend-xxxx.onrender.com/api/tickets/generate/1`
- **Headers**:
  - `Authorization: Bearer <USER_JWT_TOKEN>`
- **Expected Response**: `200 OK` with ticket details containing `qrToken` (e.g. UUID string).

---

### 8. View Ticket QR Code Image (Authenticated User)
- **Method**: `GET`
- **URL**: `https://eventflow-backend-xxxx.onrender.com/api/tickets/1/qr`
- **Headers**:
  - `Authorization: Bearer <USER_JWT_TOKEN>`
- **Expected Response**: `200 OK` with `image/png` binary preview of the QR code.

---

### 9. Verify QR Token (Organizer Role)
- **Method**: `POST`
- **URL**: `https://eventflow-backend-xxxx.onrender.com/api/tickets/verify`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <ORGANIZER_JWT_TOKEN>`
- **Body** (Raw JSON):
  ```json
  {
    "qrToken": "550e8400-e29b-41d4-a716-446655440000"
  }
  ```
- **Expected Response**: `200 OK` with ticket validation object.

---

### 10. Attendee Check-In (Organizer Role)
- **Method**: `POST`
- **URL**: `https://eventflow-backend-xxxx.onrender.com/api/tickets/check-in`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <ORGANIZER_JWT_TOKEN>`
- **Body** (Raw JSON):
  ```json
  {
    "qrToken": "550e8400-e29b-41d4-a716-446655440000"
  }
  ```
- **Expected Response**: `200 OK` with ticket object updated to `checkedIn: true`.

---

## Step 10: Connect Vercel React Frontend to Render Backend

1. Go to your [Vercel Dashboard](https://vercel.com).
2. Select your `EventFlow Frontend` project.
3. Navigate to **Settings** > **Environment Variables**.
4. Add or update:
   - For **Vite React**:
     - **Key**: `VITE_API_BASE_URL`
     - **Value**: `https://eventflow-backend-xxxx.onrender.com`
   - For **Create React App**:
     - **Key**: `REACT_APP_API_BASE_URL`
     - **Value**: `https://eventflow-backend-xxxx.onrender.com`
5. Go to **Deployments** in Vercel and click **Redeploy** on the latest deployment.
6. Verify on Render that `FRONTEND_URL` matches your Vercel URL (e.g. `https://your-app.vercel.app`) without trailing slashes.

---

## Troubleshooting Guide

### 1. Docker Build Failure
- **Symptom**: `mvn clean package failed` or `Exit code 1 during build`.
- **Fix**:
  - Check Render build logs.
  - Verify Java 17 is configured in `pom.xml` (`<java.version>17</java.version>`).
  - The Dockerfile uses `mvn clean package -DskipTests -B` to bypass tests during image creation.

### 2. Database Connection Refused
- **Symptom**: `Communications link failure` or `Connection refused`.
- **Fix**:
  - Ensure `MYSQLHOST` is **not** set to `localhost` in Render. It must be your remote cloud MySQL hostname.
  - Ensure `MYSQLPORT` matches your provider's port (`3306` or `4000`).
  - Verify your database allows connections from all IP addresses (`0.0.0.0/0`).

### 3. Access Denied for MySQL User
- **Symptom**: `Access denied for user 'xxx'@'%'`.
- **Fix**:
  - Check for typos in `MYSQLUSER` and `MYSQLPASSWORD` in Render environment settings.
  - For TiDB Cloud or Aiven, usernames include cluster prefixes (e.g., `2AbcDeF.root`).

### 4. JWT Authentication Errors
- **Symptom**: `SignatureException: JWT signature does not match` or `IllegalArgumentException`.
- **Fix**:
  - Ensure `JWT_SECRET` is set in the Render environment variables and is at least 32 characters long.
  - Ensure the secret does not change between restarts.
  - Ensure your frontend sends requests with `Authorization: Bearer <token>`.

### 5. CORS Errors
- **Symptom**: `Access to fetch at ... has been blocked by CORS policy`.
- **Fix**:
  - Ensure `FRONTEND_URL` in Render matches your Vercel URL (e.g., `https://eventflow.vercel.app`) with no trailing slash.
  - Multiple origins can be comma-separated if you use preview deployments.

### 6. Render Port Detection Errors
- **Symptom**: `Web service failed to listen on assigned port`.
- **Fix**:
  - `server.port=${PORT:8080}` and `server.address=0.0.0.0` are set in `application.properties`.
  - The Dockerfile entrypoint `ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]` explicitly passes the port.

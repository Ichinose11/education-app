# 📊 User Dashboard App

A secure, scalable web application featuring role-based user management. Built with **Angular** (Frontend), **Spring Boot** (Backend), and **PostgreSQL** (Database).

---

## 🚀 Current Progress & Features
- **User Authentication**: Secure signup and login powered by **JSON Web Tokens (JWT)** and **bcrypt** password encoding.
- **Role-Based Access Control**: Strict access boundaries separating `ADMIN`, `MODERATOR`, and `USER` roles.
  - **ADMIN**: Full control (create, view, edit, delete users, and view statistics).
  - **MODERATOR**: Management control (view, edit users, view statistics). Can *not* create or delete users.
  - **USER**: Accesses a personalized home dashboard displaying only their own profile details.
- **Admin Stats Panel**: Displays live registration counts and detailed breakdowns by roles.
- **Database Schema**: Modern database layer leveraging PostgreSQL, UUID identifiers, optimized indexes, and automated update triggers.

---

## 🛠️ Installation & Setup

### Prerequisites
Make sure you have the following installed:
* Java Development Kit (JDK 17 or higher)
* Node.js (v18.x or higher) and npm
* PostgreSQL (v13 or higher)
* Python 3 (required for running E2E automation scripts)

---

### 1. Database Setup
1. Log in to your PostgreSQL console:
   ```bash
   psql -U postgres
   ```
2. Create the target database:
   ```sql
   CREATE DATABASE user_db;
   ```
3. Initialize the schema and seed mock data:
   ```bash
   psql -U postgres -d user_db -f database/schema.sql
   psql -U postgres -d user_db -f database/seed.sql
   ```
   *(Ensure the username/password in [application.properties](file:///home/Anand9401/user-dashboard-app/backend/src/main/resources/application.properties) matches your PostgreSQL credentials)*

---

### 2. Backend Setup (Spring Boot)
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build the project and download Maven dependencies:
   ```bash
   ./mvnw clean install
   ```
3. Start the server (runs on port `8085` by default):
   ```bash
   ./mvnw spring-boot:run
   ```

---

### 3. Frontend Setup (Angular)
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install npm dependencies:
   ```bash
   npm install
   ```
3. Start the Angular dev server (runs on port `4200` by default):
   ```bash
   npm run start
   ```
4. Access the web app in your browser at `http://localhost:4200`.

---

## 🧪 Testing

### 1. Backend Testing
You can run Spring Boot integration and unit tests using Maven:
```bash
cd backend
./mvnw test
```

### 2. End-to-End (E2E) Integration Testing
We provide an automated Python integration script that runs tests against a live server context. It cleans test records, runs a backend instance, performs registration, signs in, obtains JWT tokens, validates authorization barriers, fetches stats, lists users, and executes administrative deletions.

Run the E2E script from the root directory:
```bash
python3 test_e2e.py
```

---

## 📤 Pushing to GitHub

To store this repository on GitHub, execute the following commands in the root directory:

1. **Initialize Git Repository**:
   ```bash
   git init
   ```
2. **Add all files**:
   ```bash
   git add .
   ```
3. **Commit your changes**:
   ```bash
   git commit -m "Initial commit: Set up Angular + Spring Boot User Dashboard App"
   ```
4. **Link to your GitHub Repository**:
   * Create a new empty repository on your GitHub account.
   * Add the remote origin URL:
     ```bash
     git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY_NAME.git
     ```
5. **Rename branch and Push**:
   ```bash
   git branch -M main
   git push -u origin main
   ```

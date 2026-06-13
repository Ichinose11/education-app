import os
import sys
import time
import subprocess
import socket
import requests

BACKEND_DIR = "/home/Anand9401/user-dashboard-app/backend"
PORT = 8085
API_BASE = f"http://localhost:{PORT}/api"

def is_port_open(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        try:
            s.connect(("127.0.0.1", port))
            return True
        except socket.error:
            return False

def run_tests():
    print("======================================================================")
    print("                STARTING E2E INTEGRATION TESTS                         ")
    print("======================================================================")
    
    # Check if port is already open
    if is_port_open(PORT):
        print(f"[ERROR] Port {PORT} is already open. Cannot start test.")
        sys.exit(1)
        
    # 0. Clean up existing test users from DB
    print("\n[0] Cleaning up existing test users in local PostgreSQL...")
    try:
        subprocess.run(
            ["psql", "-h", "localhost", "-p", "5432", "-U", "Anand9401", "-d", "user_db", "-c", "delete from users where email in ('dev.admin@example.com', 'test.user@example.com');"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )
        print("[PASS] Local database cleaned and ready!")
    except Exception as e:
        print(f"[WARN] Failed to clean DB: {e}. Attempting to proceed anyway...")

    # 1. Start Spring Boot Backend in the background on port 8085
    print(f"\n[1] Launching Spring Boot backend on port {PORT} from {BACKEND_DIR}...")
    proc = subprocess.Popen(
        ["./mvnw", "spring-boot:run", f"-Dspring-boot.run.arguments=--server.port={PORT}"],
        cwd=BACKEND_DIR,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )
    
    # 2. Wait for backend port to open (up to 90s)
    print(f"Waiting for server to bind to port {PORT}...")
    start_time = time.time()
    server_ready = False
    while time.time() - start_time < 90:
        if is_port_open(PORT):
            server_ready = True
            break
        time.sleep(2)
        
    if not server_ready:
        print(f"[ERROR] Spring Boot backend failed to start on port {PORT} within 90 seconds.")
        # Kill process and exit
        proc.terminate()
        sys.exit(1)
        
    print(f"[SUCCESS] Backend is up and listening on port {PORT}! (took {int(time.time() - start_time)}s)")
    
    # Small buffer for Spring context to fully load controllers
    time.sleep(3)
    
    try:
        # 3. Test 1: Accessing admin stats without authentication should fail (401/403)
        print("\n[TEST 1] Accessing protected admin stats WITHOUT token...")
        res = requests.get(f"{API_BASE}/admin/stats")
        print(f"Status Code: {res.status_code}")
        assert res.status_code in [401, 403], f"Expected 401/403 but got {res.status_code}"
        print("[PASS] Unauthorized access correctly blocked!")

        # 4. Test 2: Register a new ADMIN user
        print("\n[TEST 2] Registering a new ADMIN user...")
        admin_payload = {
            "firstName": "Developer",
            "lastName": "Admin",
            "email": "dev.admin@example.com",
            "username": "dev_admin",
            "password": "SecretPassword123",
            "role": "ADMIN"
        }
        res = requests.post(f"{API_BASE}/auth/register", json=admin_payload)
        print(f"Status Code: {res.status_code}")
        assert res.status_code in [200, 201], f"Expected 200/201 but got {res.status_code}"
        print("[PASS] Admin user registration succeeded!")

        # 5. Test 3: Log in as the registered admin
        print("\n[TEST 3] Logging in with Admin credentials...")
        login_payload = {
            "usernameOrEmail": "dev.admin@example.com",
            "password": "SecretPassword123"
        }
        res = requests.post(f"{API_BASE}/auth/login", json=login_payload)
        print(f"Status Code: {res.status_code}")
        assert res.status_code == 200, f"Expected 200 but got {res.status_code}"
        data = res.json()
        token = data.get("token")
        assert token, "Token not found in login response!"
        print(f"[PASS] Admin login succeeded! Token: {token[:15]}...{token[-15:]}")

        headers = {"Authorization": f"Bearer {token}"}

        # 6. Test 4: Register a separate regular USER
        print("\n[TEST 4] Registering a separate regular USER...")
        user_payload = {
            "firstName": "Test",
            "lastName": "User",
            "email": "test.user@example.com",
            "username": "test_user",
            "password": "UserPassword123",
            "role": "USER"
        }
        res = requests.post(f"{API_BASE}/auth/register", json=user_payload)
        print(f"Status Code: {res.status_code}")
        assert res.status_code in [200, 201], f"Expected 200/201 but got {res.status_code}"
        print("[PASS] Regular user registration succeeded!")

        # 7. Test 5: Fetch admin stats
        print("\n[TEST 5] Fetching admin stats with token...")
        res = requests.get(f"{API_BASE}/admin/stats", headers=headers)
        print(f"Status Code: {res.status_code}")
        print(f"Response: {res.text}")
        assert res.status_code == 200, f"Expected 200 but got {res.status_code}"
        stats = res.json()
        assert "totalUsers" in stats or "total_users" in stats, "Total users not found in stats response!"
        print("[PASS] Stats retrieved successfully!")

        # 8. Test 6: List all users to find the new user's ID
        print("\n[TEST 6] Listing users table with token...")
        res = requests.get(f"{API_BASE}/users", headers=headers)
        print(f"Status Code: {res.status_code}")
        assert res.status_code == 200, f"Expected 200 but got {res.status_code}"
        users = res.json()
        print(f"Total Users: {len(users)}")
        
        target_user_id = None
        for u in users:
            if u.get("email") == "test.user@example.com":
                target_user_id = u.get("id")
                break
        assert target_user_id, "Target user test.user@example.com not found in the users list!"
        print(f"[PASS] Users table retrieved! Found target user ID: {target_user_id}")

        # 9. Test 7: Admin deletes the regular user
        print(f"\n[TEST 7] Deleting user {target_user_id} using Admin token...")
        res = requests.delete(f"{API_BASE}/users/{target_user_id}", headers=headers)
        print(f"Status Code: {res.status_code}")
        assert res.status_code in [200, 204], f"Expected 200/204 but got {res.status_code}"
        print("[PASS] Regular user deleted successfully!")

        # 10. Verify deletion from users list
        print("\n[TEST 8] Verifying user deletion from users list...")
        res = requests.get(f"{API_BASE}/users", headers=headers)
        users = res.json()
        found = False
        for u in users:
            if u.get("email") == "test.user@example.com":
                found = True
                break
        assert not found, "User was still present in database after deletion!"
        print("[PASS] Deletion verified successfully!")

        print("\n======================================================================")
        print("          ALL E2E INTEGRATION TEST CASES PASSED SUCCESSFULLY!          ")
        print("======================================================================")

    except Exception as e:
        print(f"\n[FAILURE] Exception occurred during tests: {e}")
        raise e
    finally:
        # Cleanly terminate the Spring Boot process
        print("\nCleaning up: Terminating backend process...")
        proc.terminate()
        try:
            proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            proc.kill()
        print("Backend process stopped.")

if __name__ == "__main__":
    run_tests()

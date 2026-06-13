import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8085/api/v1/auth';

  register(username: string, password: string, firstName: string, lastName: string, phoneNo: string, address: string, role: string): Observable<any> {
    const payload = {
      username,
      password,
      firstName,
      lastName,
      phoneNo,
      address,
      Address: address, // Map to capitalized Address for the backend JSON binding
      role
    };
    return this.http.post<any>(`${this.apiUrl}/register`, payload);
  }

  login(username: string, password: string): Observable<any> {
    const payload = {
      username,
      password
    };
    return this.http.post<any>(`${this.apiUrl}/login`, payload).pipe(
      tap(response => {
        if (response && response.token) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('role', response.role || '');
          localStorage.setItem('user', JSON.stringify({
            id: response.id,
            username: response.username,
            firstName: response.firstName || '',
            lastName: response.lastName || '',
            phoneNo: response.phoneNo || '',
            address: response.Address || response.address || '',
            role: response.role,
            isActive: response.isActive !== undefined ? response.isActive : (response.active !== undefined ? response.active : true),
            createdBy: response.createdBy || 'System',
            createdAt: response.createdAt || new Date().toISOString(),
            updatedAt: response.updatedAt || new Date().toISOString()
          }));
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('user');
  }

  get token(): string | null {
    return localStorage.getItem('token');
  }

  get role(): string | null {
    return localStorage.getItem('role');
  }

  get currentUser(): any {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }

  isLoggedIn(): boolean {
    return !!this.token;
  }
}

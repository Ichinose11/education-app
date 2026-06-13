import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';

interface User {
  id?: string;
  username: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  phoneNo?: string;
  Address?: string; // Maps to capitalized Address for the backend JSON binding
  address?: string; // fallback
  active?: boolean;
  role: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface AdminStats {
  totalUsers: number;
  usersByRole: { [key: string]: number };
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private router = inject(Router);

  users: User[] = [];
  stats: AdminStats = { totalUsers: 0, usersByRole: {} };
  activeUsersCount = 0;
  inactiveUsersCount = 0;
  
  currentUser: any = null;
  role: string | null = null;
  isUser = false;
  isAdmin = false;
  isModerator = false;
  
  isLoading = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  
  // Create User Modal State
  isCreateModalOpen = false;
  newUser: any = {
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    phoneNo: '',
    Address: '',
    role: 'USER',
    active: true
  };
  isCreating = false;

  // Edit User Modal State
  isEditModalOpen = false;
  editingUser: any = null;
  isUpdating = false;

  // Delete Confirmation Modal State
  isDeleteModalOpen = false;
  userToDelete: User | null = null;
  isDeleting = false;

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser;
    this.role = this.authService.role;
    
    this.isUser = this.role === 'USER';
    this.isAdmin = this.role === 'ADMIN';
    this.isModerator = this.role === 'MODERATOR';

    if (this.isUser) {
      // Users only see their profile homepage (stored in local storage / currentUser)
      this.isLoading = false;
      return;
    }

    if (this.isAdmin || this.isModerator) {
      this.fetchData();
    } else {
      this.isLoading = false;
      this.errorMessage = 'Access Denied: Unknown role.';
    }
  }

  fetchData(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // Fetch stats
    this.http.get<AdminStats>('http://localhost:8085/api/admin/stats').subscribe({
      next: (statsData) => {
        this.stats = statsData;
        
        // After stats, fetch users
        this.http.get<User[]>('http://localhost:8085/api/users').subscribe({
          next: (usersData) => {
            this.users = usersData;
            
            // Calculate active and inactive users count dynamically
            this.activeUsersCount = this.users.filter(u => u.active !== false).length;
            this.inactiveUsersCount = this.users.filter(u => u.active === false).length;
            
            this.isLoading = false;
          },
          error: (err) => {
            this.isLoading = false;
            this.errorMessage = 'Failed to load users list. Please check your privileges.';
          }
        });
      },
      error: (err) => {
        // Fallback: try loading users anyway if stats fail
        this.http.get<User[]>('http://localhost:8085/api/users').subscribe({
          next: (usersData) => {
            this.users = usersData;
            this.activeUsersCount = this.users.filter(u => u.active !== false).length;
            this.inactiveUsersCount = this.users.filter(u => u.active === false).length;
            this.stats = {
              totalUsers: this.users.length,
              usersByRole: {
                ADMIN: this.users.filter(u => u.role === 'ADMIN').length,
                MODERATOR: this.users.filter(u => u.role === 'MODERATOR').length,
                USER: this.users.filter(u => u.role === 'USER').length
              }
            };
            this.isLoading = false;
          },
          error: (err) => {
            this.isLoading = false;
            this.errorMessage = 'Failed to load dashboard statistics and users.';
          }
        });
      }
    });
  }

  // Create User Action
  openCreateModal(): void {
    this.resetNewUserForm();
    this.isCreateModalOpen = true;
  }

  closeCreateModal(): void {
    this.isCreateModalOpen = false;
  }

  resetNewUserForm(): void {
    this.newUser = {
      username: '',
      password: '',
      firstName: '',
      lastName: '',
      phoneNo: '',
      Address: '',
      role: 'USER',
      active: true
    };
  }

  onCreateUserSubmit(): void {
    if (!this.newUser.username || !this.newUser.password || !this.newUser.firstName || !this.newUser.lastName || !this.newUser.phoneNo || !this.newUser.Address) {
      alert('All fields are required.');
      return;
    }

    this.isCreating = true;
    this.http.post<User>('http://localhost:8085/api/users', this.newUser).subscribe({
      next: () => {
        this.isCreating = false;
        this.closeCreateModal();
        this.fetchData();
      },
      error: (err) => {
        this.isCreating = false;
        alert(err.error?.message || 'Failed to create user.');
      }
    });
  }

  // Edit User Action
  openEditModal(user: User): void {
    this.editingUser = {
      id: user.id,
      username: user.username,
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      phoneNo: user.phoneNo || '',
      Address: user.Address || user.address || '',
      active: user.active !== false,
      role: user.role
    };
    this.isEditModalOpen = true;
  }

  closeEditModal(): void {
    this.isEditModalOpen = false;
    this.editingUser = null;
  }

  onEditUserSubmit(): void {
    if (!this.editingUser.firstName || !this.editingUser.lastName || !this.editingUser.phoneNo || !this.editingUser.Address) {
      alert('First Name, Last Name, Phone Number, and Address are required.');
      return;
    }

    this.isUpdating = true;
    const userId = this.editingUser.id;
    
    // Structure payload with capital Address for Jackson parsing on backend
    const payload = {
      firstName: this.editingUser.firstName,
      lastName: this.editingUser.lastName,
      phoneNo: this.editingUser.phoneNo,
      Address: this.editingUser.Address,
      active: this.editingUser.active,
      role: this.editingUser.role
    };

    this.http.put<User>(`http://localhost:8085/api/users/${userId}`, payload).subscribe({
      next: () => {
        this.isUpdating = false;
        this.closeEditModal();
        this.fetchData();
      },
      error: (err) => {
        this.isUpdating = false;
        alert(err.error?.message || 'Failed to update user.');
      }
    });
  }

  // Delete User Action
  openDeleteModal(user: User): void {
    this.userToDelete = user;
    this.isDeleteModalOpen = true;
  }

  closeDeleteModal(): void {
    this.isDeleteModalOpen = false;
    this.userToDelete = null;
  }

  confirmDelete(): void {
    if (!this.userToDelete) return;

    this.isDeleting = true;
    const userId = this.userToDelete.id;

    this.http.delete(`http://localhost:8085/api/users/${userId}`).subscribe({
      next: () => {
        this.isDeleting = false;
        this.closeDeleteModal();
        this.fetchData();
      },
      error: (err) => {
        this.isDeleting = false;
        alert(err.error?.message || 'Failed to delete user.');
        this.closeDeleteModal();
      }
    });
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

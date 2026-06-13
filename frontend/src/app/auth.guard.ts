import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  
  // Check localStorage/sessionStorage for auth token or state
  const token = localStorage.getItem('token') || sessionStorage.getItem('token');
  
  if (token) {
    return true;
  }

  // Redirect to login page for unauthorized users
  router.navigate(['/login']);
  return false;
};

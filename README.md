# Appliance Store — Spring Boot Application

Appliance Store is a Spring Boot MVC application that simulates an online appliance shop.
The project focuses on backend architecture, stateless authentication, and Spring Security best practices.

This project is educational and demonstrates how to build a production-like backend using JWT stored in cookies.

---

## Features

- User registration and authentication
- Stateless JWT authentication (Access + Refresh tokens)
- JWT stored in HttpOnly cookies
- Refresh token rotation with database persistence
- CSRF protection using cookies
- Login rate limiting and brute-force protection
- Role-based access control (RBAC)
- Shopping cart and checkout flow
- Client and employee dashboards
- Account deactivation

---

## User Roles

### Guest
- Browse product catalog
- View product details
- Register and login

### Client (ROLE_CLIENT)
- Add products to cart
- Manage cart items
- Checkout orders
- View and update profile
- Change password
- Top up account balance
- Deactivate account

### Employee (ROLE_EMPLOYEE)
- Access employee/admin pages
- Manage orders

---

## Security Overview

### Authentication & Authorization
- Stateless JWT Authentication**: No server-side HTTP sessions - fully stateless architecture
- Dual Token System**: 
  - Access Token** (15 min TTL) - Used for API authorization, stored in HttpOnly cookie
  - Refresh Token** (14 days TTL) - Used for token renewal, stored in HttpOnly cookie
- HttpOnly Cookies: Prevents JavaScript access to tokens (XSS protection)
- Secure Cookies: 
  - `SameSite=Lax` - CSRF mitigation
  - `Secure` flag - HTTPS-only transmission
  - Domain-scoped cookies
- Role-Based Access Control (RBAC): 
  - `ROLE_CLIENT` - Shopping and account management
  - `ROLE_EMPLOYEE` - Admin operations and order management
- OAuth2/OIDC Integration: Google Sign-In with automatic account creation/linking

### Security Mechanisms
- CSRF Protection: 
  - `CookieCsrfTokenRepository` with `XSRF-TOKEN` cookie
  - Enabled for all state-changing operations
  - Exempted endpoints: `/login`, `/auth/refresh`, `/logout`
- Login Rate Limiting: 
  - Per-username and per-IP tracking
  - Temporary lockout after failed attempts
  - Prevents credential stuffing and brute-force attacks
- Password Security:
  - BCrypt hashing (default strength: 10 rounds)
  - Salted hashes (automatic with BCrypt)
  - No plain-text passwords stored
- SSL/TLS: 
  - HTTPS enabled on port 8443 (development)
  - Self-signed certificate (for local development)
  - Production should use valid CA-signed certificates
- Security Filter Chain:
  1. `LoginPreCheckFilter` - Rate limit validation before login
  2. `JwtAuthenticationFilter` - Token extraction and validation
  3. `CsrfFilter` - CSRF token verification

### OAuth2 Security
- Provider Google (OpenID Connect)
- Scopes: `openid`, `profile`, `email`
- Flow: Authorization Code Grant with PKCE
- Custom User Service: 
  - Loads existing user by email
  - Creates new account if first-time login
  - Links OAuth identity to existing email if found
- Security Measures:
  - Email verification required (provided by Google)
  - Random secure password generated for OAuth users
  - Provider tracking (`AuthProvider.GOOGLE` vs `AuthProvider.LOCAL`)
  - Automatic account enabling on successful OAuth login

---

## Technology Stack

- Java 17
- Spring Boot 3
- Spring Security
- Spring MVC + Thymeleaf
- Spring Data JPA (Hibernate)
- H2 in-memory database
- JWT (jjwt)
- Lombok
- Maven

---



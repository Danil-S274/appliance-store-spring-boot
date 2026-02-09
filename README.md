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

- Stateless authentication (no HTTP sessions)
- Access token with short TTL
- Refresh token with long TTL
- Refresh tokens stored in database (hashed JTI)
- Refresh token rotation on each refresh
- CSRF protection using XSRF-TOKEN cookie
- Login attempt rate limiting
- Secure logout and account deletion

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



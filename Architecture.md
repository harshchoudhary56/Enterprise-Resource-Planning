# Enterprise Educational ERP Platform - Microservices Architecture

This document outlines the architectural blueprint for a highly scalable, metadata-driven Educational ERP. The system is designed using Domain-Driven Design (DDD) principles, dividing the platform into isolated microservices.

It covers the complete lifecycle of all stakeholders: from a prospective student lead to alumni, from a job applicant to a terminated employee, and from asset procurement to depreciation.

---

## 1. Core Platform Services (The Foundation)

### Identity & Access Management (IAM) Service
**Purpose:** The central gatekeeper for the entire platform. It handles "who you are" and "what you can do."
*   **Key Features:** Authentication (JWT/OAuth), Role-Based Access Control (RBAC), user session management, and cross-service identity mapping.
*   **Core Entities:** Users, Roles, Permissions, User-Role Mappings.

### Workflow & Approval Service
**Purpose:** The global Maker-Checker (4-Eyes Principle) state machine ensuring enterprise compliance and fraud prevention.
*   **Key Features:** Intercepts sensitive actions (e.g., fee changes, payroll disbursement, inventory write-offs), creates a "Draft," and routes it to the designated "Checker" (e.g., Principal, Finance Controller) for approval or rejection.
*   **Core Entities:** Approval Workflows, Draft States, Audit Logs.

### Notification Service
**Purpose:** The centralized communication hub for the entire ecosystem.
*   **Key Features:** Dispatches automated Emails, SMS, WhatsApp messages, and in-app push notifications triggered by events from other services (e.g., absence alerts, pending approvals, library due dates).
*   **Core Entities:** Notification Templates, Delivery Logs, Webhook Listeners.

---

## 2. Academics & Student Lifecycle

### Admissions & CRM Service
**Purpose:** Manages the pre-enrollment pipeline, treating prospective students as leads.
*   **Key Features:** Inquiry tracking, application form sales, entrance exam scheduling, and merit list generation. Automatically hands off data to the Academic Service upon enrollment.
*   **Core Entities:** Leads, Applications, Merit Lists, Follow-up Tasks.

### Academic Service
**Purpose:** The operational heart of the school's daily learning environment.
*   **Key Features:** Manages departments, classes, sections, daily timetables, student attendance (with strict retroactive edit limits), assignments, and standard grading.
*   **Core Entities:** Classes, Sections, Subjects, Enrollments, Daily Attendance.

### Exam & Seating Engine
**Purpose:** A highly algorithmic service dedicated to high-stakes examinations (Mid-Terms, Boards).
*   **Key Features:** Date sheet generation, algorithmic seat assignments (preventing students of the same class from sitting adjacent to prevent cheating), clash detection with daily timetables, and anonymous grading pipelines.
*   **Core Entities:** Exam Schedules, Seat Allocations, Hall Tickets, Bell-Curve Results.

### Clinic & Health Service
**Purpose:** Manages student and staff medical safety and liability.
*   **Key Features:** Tracks allergies, chronic conditions, and emergency contacts. Logs infirmary visits and generates Maker-Checker gated "Medical Gate Passes" for students needing to go home early.
*   **Core Entities:** Health Profiles, Infirmary Logs, Medical Gate Passes.

---

## 3. Human Resources & Workforce

### ATS & Recruitment Service
**Purpose:** Manages the external candidate pipeline before they become employees.
*   **Key Features:** Public job portal, applicant tracking (Kanban stages), and a robust Employee Referral Engine.
*   **Advanced Logic:** Enforces the "3-Strike Re-Interview Penalty" (if 3 referred candidates leave before 1 year, the referring employee is forced to re-interview) and dynamically triggers referral clawbacks.
*   **Core Entities:** Job Postings, Candidates, Interview Pipelines, Referral Logs.

### HR & Payroll Service
**Purpose:** Manages the financial and professional lifecycle of active staff.
*   **Key Features:** Staff profiles, leave balances (Sick, Casual), Loss of Pay (LOP) calculations, performance appraisals, and monthly salary generation. Integrates dynamically with the ATS for referral bonuses or clawback deductions.
*   **Core Entities:** Staff Profiles, Leave Ledgers, Appraisals, Salary Slips.

---

## 4. Finance & Infrastructure

### Finance & Revenue Service
**Purpose:** Manages all money coming IN to the institution.
*   **Key Features:** Dynamic student fee structures, payment gateway integration, invoicing, and maintaining the student "Virtual Wallet" ledger used across the campus.
*   **Core Entities:** Fee Structures, Invoices, Transactions, Virtual Wallets.

### Asset & Procurement Service
**Purpose:** Manages CapEx (Capital) and OpEx (Operational) money going OUT.
*   **Key Features:** Fixed asset registers (calculating depreciation), Purchase Orders (POs) for new equipment, vendor management, and department budget tracking.
*   **Core Entities:** Assets, Purchase Requests, Vendors, Department Budgets.

### Facility & Operations Service
**Purpose:** Spatial management and physical infrastructure maintenance.
*   **Key Features:** Maps the exact physical layout of the school (Buildings -> Floors -> Rooms -> Unique Seats). Manages hostel allocations, school bus routes, and Techforce maintenance ticketing.
*   **Core Entities:** Buildings, Rooms, Seats, Transport Routes, Maintenance Tickets.

---

## 5. Auxiliary Operations

### Library Management Service
**Purpose:** Tracks physical reading materials and associated financial penalties.
*   **Key Features:** Digital catalog, dynamic borrowing rules (customizable by role), issue/return tracking, and automated fine calculation that syncs with the student's Virtual Wallet.
*   **Core Entities:** Books, Authors, Issue Logs, Fine Ledgers.

### Commerce Service (Cafeteria/Store)
**Purpose:** E-commerce and Point of Sale (POS) backend for campus purchases.
*   **Key Features:** Manages cafeteria/uniform store inventory. Processes transactions by deducting funds directly from the student or staff Virtual Wallet.
*   **Core Entities:** Products, Inventory Levels, POS Orders.
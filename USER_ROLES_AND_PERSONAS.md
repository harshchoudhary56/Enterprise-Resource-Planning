# Ecosystem User Roles & Persona Dictionary

## Overview

This document defines the **8 distinct user roles** across the **Enterprise ERP & Campus FinTech Ecosystem**. It outlines each persona's primary objectives, responsibilities, and the primary applications they interact with.

---

# User Role Classification

The ecosystem consists of four major user groups:

1. **End Users & Primary Consumers**
2. **Academic & Operations Staff**
3. **Financial & Executive Management**
4. **External Operational Partners**

---

# 1. End Users & Primary Consumers

These users interact with the platform daily for education, transportation, payments, and campus services.

---

## 1.1 Parent (Financial & Safety User)

### Primary Objective

Provide a seamless experience for:

- Fee payments
- Student safety
- Daily spending visibility
- Academic progress tracking

---

### Key Responsibilities

#### Financial Management

- Pay school tuition fees
- Pay transport fees
- Pay hostel fees
- Pay examination fees
- Download payment receipts
- View payment history

Supported payment methods include:

- UPI
- Net Banking
- Credit Card
- Debit Card
- School Wallet (if enabled)

---

#### Student Wallet Management

- Top up student virtual wallet
- Configure daily spending limits
- Configure weekly spending limits
- Freeze student wallet
- View wallet transactions

---

#### Student Transportation

- Track school bus live
- View current bus location
- Receive ETA notifications
- Receive arrival alerts
- View assigned pickup and drop locations

---

#### Academic Monitoring

- View attendance
- View academic reports
- Download report cards
- Receive school announcements
- Monitor homework and assignments

---

### Primary Interface

- Parent Mobile App (iOS)
- Parent Mobile App (Android)

---

## 1.2 Student (Daily Campus Consumer)

### Primary Objective

Enable a frictionless campus experience through digital identity and cashless transactions.

---

### Key Responsibilities

#### Cashless Campus Payments

- Purchase cafeteria meals
- Purchase stationery
- Purchase books
- Purchase uniforms
- Scan QR codes
- Tap RFID student card

---

#### Academic Access

- View timetable
- View classroom schedule
- View examination timetable
- Check examination room
- View seat allocation
- View row and column coordinates

---

#### Campus Services

- View bus schedule
- Receive school notices
- Track library borrowings
- View due dates
- Renew library books (if permitted)

---

### Primary Interface

- Student Mobile App
- Smart RFID Student ID Card

---

# 2. Academic & Operations Staff

These users ensure smooth academic operations, inventory management, transport, and campus maintenance.

---

## 2.1 Teacher / Educator (Classroom Operator)

### Primary Objective

Reduce administrative workload while improving classroom efficiency and student engagement.

---

### Key Responsibilities

#### Attendance

- Mark classroom attendance
- Mark laboratory attendance
- Mark examination attendance
- Correct attendance records

---

#### Academic Evaluation

- Enter examination marks
- Enter assignment grades
- Record behavioural feedback
- Generate report cards
- Publish academic progress

---

#### Procurement

Raise Purchase Requests (PRs) for:

- Laboratory equipment
- Classroom furniture
- Office supplies
- Teaching materials
- Stationery

Items are selected from:

```
tbl_item_master
```

---

#### Facility Management

Report maintenance issues including:

- Broken AC
- Faulty projector
- Damaged desks
- Electrical failures
- Plumbing issues

Automatically generates maintenance tickets.

---

### Primary Interface

- Teacher Web Portal
- Teacher Mobile App

---

## 2.2 Loading Dock & Asset Staff (Inventory & Facility)

### Primary Objective

Maintain complete accountability for inventory, warehouse operations, and institutional assets.

---

### Key Responsibilities

#### Goods Receiving

- Inspect deliveries
- Verify Purchase Orders
- Create Goods Receipt Notes (GRNs)
- Record received quantities
- Record rejected quantities

---

#### Quality Inspection

Flag:

- Damaged goods
- Short shipments
- Wrong items
- Supplier discrepancies

Uses:

```
quantity_rejected
```

---

#### Asset Management

- Generate QR Codes
- Generate Barcodes
- Print Asset Tags
- Assign assets to rooms
- Assign assets to staff
- Record asset ownership

Asset identifier:

```
asset_tag
```

---

#### Inventory Control

- Perform stock counts
- Cycle counting
- Physical inventory audits
- Update consumable stock

---

### Primary Interface

- Handheld Scanner Application
- Inventory Management Web Portal

---

## 2.3 Transport & Fleet Manager (Logistics Operator)

### Primary Objective

Deliver safe, efficient transportation while ensuring accurate vendor billing and regulatory compliance.

---

### Key Responsibilities

#### Route Management

- Create routes
- Configure stops
- Define stop sequence
- Assign students to stops
- Manage pickup locations
- Manage drop locations

---

#### Fleet Management

Manage:

- Vendor contracts
- Vehicle roster
- Driver assignments
- Bus fitness certificates
- Insurance renewals
- Permit renewals

---

#### Trip Audit

Review disputed trips where:

- GPS distance
- Odometer distance

do not match.

Investigate:

- GPS anomalies
- Driver errors
- Vendor disputes

---

#### Vendor Settlement

- Review monthly settlements
- Audit vendor invoices
- Approve transport payments

---

### Primary Interface

- Fleet Management Dashboard

---

# 3. Financial & Executive Management

Responsible for governance, budgeting, accounting, and institutional oversight.

---

## 3.1 School Accountant / Finance Manager (Money Gatekeeper)

### Primary Objective

Maintain complete financial accuracy while ensuring compliance, reconciliation, and controlled spending.

---

### Key Responsibilities

#### Fee Administration

Configure:

- Tuition fees
- Hostel fees
- Transport fees
- Activity fees
- Installments
- Late fee penalties

---

#### Payment Reconciliation

Match:

- Cash payments
- Cheques
- Bank transfers
- Offline receipts

against student ledger accounts.

---

#### Budget Monitoring

Track:

- Department budgets
- Encumbered amounts
- Available balances
- Actual expenditures

---

#### Vendor Payments

Approve payments for:

- Transport vendors
- Cafeteria vendors
- Maintenance contractors
- Equipment suppliers

---

### Primary Interface

- Finance Dashboard
- General Ledger Portal

---

## 3.2 Principal / Trustee / Super Admin (Executive Approver)

### Primary Objective

Provide strategic oversight, institutional governance, compliance, and executive approvals.

---

### Key Responsibilities

#### Executive Analytics

Monitor:

- Fee collection
- Student attendance
- Budget health
- Procurement status
- Vendor liabilities
- Academic performance

---

#### Workflow Approvals

Final approver for:

- Purchase Requests
- Budget approvals
- Fee waivers
- Staff leave requests
- High-value procurement

---

#### Administrative Overrides

Override system restrictions during:

- Emergency student release
- Emergency transport changes
- Budget reallocations
- Critical operational events

---

### Primary Interface

- Executive Mobile App
- Administrative Dashboard

---

# 4. External Operational Partners

These users interact with the institution through integrated operational systems.

---

## 4.1 Service Providers (Cafeteria / Retail Vendors)

### Primary Objective

Provide fast, reliable point-of-sale transactions during high-volume campus operations.

---

### Key Responsibilities

#### POS Transactions

Accept payments via:

- RFID Student Card
- Dynamic QR Code
- Campus Wallet

Automatically deduct student wallet balance.

---

#### Store Management

Manage:

- Daily menu
- Product catalogue
- Stock availability
- Pricing updates

---

#### Financial Reporting

View:

- Daily sales
- Transaction summaries
- Pending settlements

Request payout from the school's finance department.

---

### Primary Interface

- POS Counter Application
- Tablet POS Interface

---

## 4.2 Bus Driver & Attendant (Transit Operators)

### Primary Objective

Ensure safe transportation while accurately recording trips and student boarding activity.

---

### Key Responsibilities

#### Trip Operations

- Start trip
- End trip
- Record start odometer
- Record end odometer

---

#### GPS Tracking

Transmit:

- Live GPS coordinates
- Vehicle movement
- Route progress
- ETA updates

---

#### Student Boarding

Scan RFID cards during:

- Boarding
- Drop-off

Automatically records:

- Boarding time
- Exit time
- Stop location

---

### Primary Interface

- Driver Smart Application
- Simplified Large-Button Mobile Interface

---

# User Role Summary Matrix

| User Role | Primary Objective | Main Responsibilities | Primary Interface |
|------------|------------------|-----------------------|-------------------|
| **Parent** | Payments & Student Safety | Fee payments, wallet top-up, live bus tracking, attendance | Parent Mobile App |
| **Student** | Cashless Campus Experience | Wallet payments, timetable, exams, library, transport | Student App / RFID Card |
| **Teacher** | Classroom Management | Attendance, grading, purchase requests, maintenance reporting | Teacher Portal / Mobile App |
| **Loading Dock & Asset Staff** | Inventory & Asset Accountability | GRNs, asset tagging, stock counts, warehouse operations | Scanner App / Inventory Portal |
| **Transport & Fleet Manager** | Fleet Operations | Route planning, vendor management, transport audit, settlements | Fleet Management Dashboard |
| **School Accountant / Finance Manager** | Financial Governance | Fee management, reconciliation, budgeting, vendor payments | Finance Dashboard |
| **Principal / Trustee / Super Admin** | Executive Oversight | Analytics, approvals, policy enforcement, emergency overrides | Executive App / Admin Dashboard |
| **Service Providers** | Point-of-Sale Operations | Student purchases, inventory management, settlement requests | POS Counter App |
| **Bus Driver & Attendant** | Safe Transit Operations | Trip execution, GPS tracking, RFID boarding, odometer logging | Driver Smart App |

---

# Ecosystem Interaction Overview

```text
                           +----------------------+
                           |   Principal / Admin  |
                           +----------+-----------+
                                      |
        ---------------------------------------------------------
        |                       |                      |
        ▼                       ▼                      ▼
 Finance Manager        Transport Manager       Teacher Portal
        |                       |                      |
        |                       |                      |
        ▼                       ▼                      ▼
 Vendor Payments        Fleet Operations       Student Academics
        |                       |                      |
        -------------------------                      |
                    |                                 |
                    ▼                                 ▼
             Service Providers                  Student App
                    |                                 |
                    ▼                                 ▼
             Cashless Purchases               Attendance, Timetable,
                                               Library & Notifications
                    ▲
                    |
              Parent Mobile App
         (Payments, Tracking & Wallet)
```

---

# Key Design Principles

- Role-Based Access Control (RBAC)
- Mobile-first user experience
- Cashless campus ecosystem
- Real-time transportation tracking
- End-to-end financial transparency
- Digital procurement workflows
- Automated inventory accountability
- Executive approval hierarchy
- Event-driven enterprise architecture
- Scalable multi-campus deployment
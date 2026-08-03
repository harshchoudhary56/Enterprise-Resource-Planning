# Finance & Revenue Service Architecture

## 1. Overview & Core Principles

The **Finance & Revenue Service** is a **Fintech-grade microservice** responsible for managing all inbound cash flow, invoicing, and the closed-loop campus economy through **Digital Wallets**.

Rather than relying on a simple flat-table design, this service is built upon three enterprise financial principles.

### Double-Entry Accounting

Every movement of money consists of **two balanced ledger entries**:

- One **Debit**
- One **Credit**

Money is never created or destroyed—it simply moves between:

- System Accounts
- Vendor Accounts
- Student Wallets

---

### Unified Transactions

Internal wallet transfers and external payment gateway transactions are processed through a **single transaction engine**, similar to modern digital wallets such as **Freecharge** or **Venmo**.

Supported transaction types include:

- Wallet Top-Up
- Student-to-Student (P2P) Transfer
- Cafeteria Payments
- Shop Purchases
- UPI Payments
- Credit/Debit Cards
- Bank Transfers

---

### Immutability & Idempotency

Financial records must always remain auditable.

Key principles:

- Ledger entries are **append-only**.
- Existing ledger records are **never modified or deleted**.
- Network retries from mobile clients cannot cause duplicate payments because every transaction requires a unique **Idempotency Key**.

---

# 2. Database Schema (DBML)

```dbml
// ==========================================
// 1. FINANCE & REVENUE ENUMS
// ==========================================

Enum invoice_status {
  DRAFT
  PENDING
  PARTIALLY_PAID
  PAID
  OVERDUE
  CANCELLED
}

Enum payment_method {
  CREDIT_CARD
  UPI
  BANK_TRANSFER
  CASH
  VIRTUAL_WALLET
    [note: 'Used for internal Freecharge-style payments']
}

Enum transaction_status {
  PENDING
  SUCCESS
  FAILED
  REFUNDED
}

Enum transaction_purpose {
  INVOICE_PAYMENT
  WALLET_TOP_UP
  P2P_TRANSFER
    [note: 'Student to Student']
  VENDOR_PURCHASE
    [note: 'Student to Cafeteria/Shop']
  SYSTEM_ADJUSTMENT
    [note: 'Refunds or Admin corrections']
}

Enum account_type {
  STUDENT_WALLET
  VENDOR_ACCOUNT
  SYSTEM_REVENUE
  SYSTEM_BANK
}

Enum ledger_entry_type {
  CREDIT
    [note: 'Money flowing INTO the account']
  DEBIT
    [note: 'Money flowing OUT OF the account']
}

Enum waiver_status {
  PENDING
  APPROVED
  REJECTED
}

// ==========================================
// 2. INVOICING & WAIVER ENGINE
// ==========================================

Table tbl_fee_master {
  id integer [pk]
  fee_code string [unique]
  name string
  default_amount decimal
  is_taxable boolean
}

Table tbl_student_invoice {
  id integer [pk]
  invoice_number string [unique]

  student_id integer
    [note: 'Soft reference to IAM Service']

  academic_term string

  total_amount decimal
  amount_paid decimal [default: 0]

  due_date date

  status invoice_status [default: 'DRAFT']

  created_at datetime
  updated_at datetime
}

Table tbl_invoice_line_item {
  id integer [pk]

  invoice_id integer
  fee_master_id integer

  description string

  amount decimal
  discount_amount decimal [default: 0]
  net_amount decimal
}

Table tbl_fee_waiver_request {
  id integer [pk]

  invoice_id integer
  fee_master_id integer

  amount_to_waive decimal

  reason text

  requested_by_id integer

  approved_by_id integer [null]

  status waiver_status [default: 'PENDING']

  created_at datetime
  resolved_at datetime
}

// ==========================================
// 3. THE UNIFIED TRANSACTION ENGINE
// ==========================================

Table tbl_transactions {
  id integer [pk]

  initiator_user_id integer
    [note: 'Student or Parent who initiated the transaction']

  invoice_id integer
    [null, note: 'Null if Wallet Top-Up or P2P Transfer']

  purpose transaction_purpose

  amount decimal

  method payment_method

  status transaction_status [default: 'PENDING']

  bank_reference_id string
    [unique, null, note: 'Null for Internal Wallet Transfers']

  idempotency_key string
    [unique, note: 'Prevents Double Charging']

  created_at datetime
  updated_at datetime
}

// ==========================================
// 4. INTERNAL FINTECH ENGINE
// ==========================================

Table tbl_financial_account {
  id integer [pk]

  account_type account_type

  name string

  owner_id integer
    [null, note: 'Student ID or Vendor ID. Null for System Accounts']

  balance decimal
    [default: 0, note: 'Cached Real-Time Balance']

  is_active boolean [default: true]
}

Table tbl_ledger_entry {
  id integer [pk]

  transaction_id integer

  account_id integer

  entry_type ledger_entry_type

  amount decimal
}

// ==========================================
// 5. RELATIONSHIPS
// ==========================================

Ref: tbl_invoice_line_item.invoice_id > tbl_student_invoice.id

Ref: tbl_invoice_line_item.fee_master_id > tbl_fee_master.id

Ref: tbl_fee_waiver_request.invoice_id > tbl_student_invoice.id

Ref: tbl_fee_waiver_request.fee_master_id > tbl_fee_master.id

Ref: tbl_transactions.invoice_id > tbl_student_invoice.id

Ref: tbl_ledger_entry.transaction_id > tbl_transactions.id

Ref: tbl_ledger_entry.account_id > tbl_financial_account.id
```

---

# 3. Core Component Breakdown

## The Invoicing Engine

The invoicing engine separates the **Price Book** from the **Actual Student Invoice**.

### Fee Master (`tbl_fee_master`)

Acts as the organization's master pricing catalog.

Examples:

- Tuition Fee
- Bus Fee
- Hostel Fee
- Lab Fee

---

### Student Invoice (`tbl_student_invoice`)

Represents a student's complete invoice.

Each invoice contains multiple **Invoice Line Items**.

Example:

```
Invoice #2026-001

• Tuition Fee
• Laboratory Fee
• Library Fee
• Bus Fee
```

Parents receive a single consolidated invoice instead of multiple bills.

---

## The Maker-Checker Waiver Engine

Financial records should never be deleted because doing so destroys the audit trail.

Instead:

1. An administrator requests a waiver.
2. Finance Head or Principal approves the request.
3. The system automatically creates a **negative invoice line item** to offset the original charge.

Example:

```
Original Fine        : ₹1,000
Approved Waiver      : -₹1,000

Outstanding Balance  : ₹0
```

This preserves a complete financial history.

---

## The Unified Transaction Layer

All movement of monetary value passes through a single transaction table.

### External Transactions

Examples:

- UPI
- Credit Card
- Bank Transfer

Characteristics:

- Uses `bank_reference_id`
- Matches payment gateway or bank statements

---

### Internal Transactions

Examples:

- Wallet Payments
- Student-to-Student Transfers
- Cafeteria Purchases
- Campus Shop Purchases

Characteristics:

- `bank_reference_id` remains **NULL**
- Payment method is **VIRTUAL_WALLET**

---

# 4. The Double-Entry Ledger

The accounting engine maintains four categories of financial accounts.

| Account Type | Purpose |
|--------------|---------|
| STUDENT_WALLET | Student Digital Wallet |
| VENDOR_ACCOUNT | Cafeteria and Shop Accounts |
| SYSTEM_REVENUE | Institution Revenue |
| SYSTEM_BANK | External Bank Account |

---

## Ledger Rule

Every transaction creates **exactly two ledger entries**.

### Example: Student-to-Student Transfer

John sends **₹500** to Sarah.

Ledger Entries:

| Account | Entry Type | Amount |
|----------|------------|--------|
| John's Wallet | DEBIT | ₹500 |
| Sarah's Wallet | CREDIT | ₹500 |

Money simply moves between accounts.

Total Debits = Total Credits

---

# 5. Cross-Service Boundaries & Rules

## No Hard Foreign Keys to IAM

The Finance Service never creates foreign key constraints to the User Service.

Instead, tables store **Soft References**.

Example:

```
owner_id

initiator_user_id

student_id
```

These IDs refer to records managed by the IAM/User Service.

Because each microservice owns its own database, **cross-service SQL joins are prohibited**.

---

## Event-Driven Billing

The Finance Service never polls other services for updates.

Instead, it subscribes to events from Kafka or RabbitMQ.

Example:

```
Academic Service
        │
        ▼
penalty.approved
        │
        ▼
Finance Service
        │
        ▼
Generate Invoice
```

Examples of bill-generating events include:

- Laboratory Damage
- Hostel Fine
- Library Penalty
- Transport Fine

---

## Fast-Read Wallet Balances

Calculating wallet balances by summing the entire ledger history is expensive.

Instead:

```
New Ledger Entry
        │
        ▼
Internal Domain Event
        │
        ▼
Update Cached Balance
        │
        ▼
tbl_financial_account.balance
```

The frontend queries this cached balance, while the ledger remains the immutable source of truth.

---

# Architecture Summary

| Component | Responsibility |
|------------|----------------|
| Fee Master | Master Price Catalog |
| Student Invoice | Parent Billing Document |
| Invoice Line Item | Individual Charges |
| Waiver Engine | Controlled Financial Adjustments |
| Transaction Engine | Unified Payment Processing |
| Financial Account | Wallet & System Accounts |
| Ledger | Immutable Double-Entry Accounting |
| Event Broker | Automatic Invoice Generation |
| Cached Balance | Fast Mobile App Reads |

---

# Key Design Principles

- Double-entry accounting for every financial transaction.
- Immutable, append-only ledger for complete auditability.
- Unified transaction engine for both internal and external payments.
- Strict idempotency to prevent duplicate charges.
- Soft references instead of cross-service foreign keys.
- Event-driven billing using Kafka or RabbitMQ.
- Distributed wallet balances with cached real-time reads.
- Financial history is never deleted—adjustments are represented through new ledger entries or negative invoice items.

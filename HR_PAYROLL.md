# HR & Payroll Service

The **HR & Payroll Service** manages the complete professional and financial lifecycle of employees, including employment contracts, leave management, performance appraisals, payroll processing, and salary disbursement.

---

# 1. HR & Payroll Enums

```dbml
// ==========================================
// HR & PAYROLL ENUMS
// ==========================================
Enum leave_type {
  SICK
  CASUAL
  UNPAID_LOP
  MATERNITY
}

Enum leave_status {
  PENDING
  APPROVED
  REJECTED
  CANCELLED
}

Enum payroll_status {
  DRAFT
  PROCESSING
  PAID
  FAILED
}
```

---

# 2. Table Group

```dbml
// ==========================================
// TABLE GROUP
// ==========================================
TableGroup HRPayrollService {
  tbl_employee_contract
  tbl_leave_balance
  tbl_leave_request
  tbl_payroll_adjustment
  tbl_salary_slip
  tbl_appraisal
}
```

---

# 3. HR & Payroll Database

```dbml
// ==========================================
// 8. HR & PAYROLL DATABASE
// ==========================================

// ------------------------------------------
// CORE HR
// ------------------------------------------
Table tbl_employee_contract {
  id integer [pk]
  user_id integer [unique, note: 'Soft ref: tbl_user.id (IAM Service)']

  hire_date date
  base_salary decimal [note: 'Annual or Monthly base pay']
  bank_account_no string
  tax_identification_number string

  is_active boolean [default: true]
  terminated_at date [note: 'Triggers ATS clawback logic if < 1 year']
}

Table tbl_appraisal {
  id integer [pk]
  employee_id integer [note: 'Soft ref: tbl_user.id']
  evaluator_id integer [note: 'Soft ref: tbl_user.id']

  review_period string [note: 'e.g., Q3 2026']
  performance_score integer [note: 'Scale 1-5']
  comments text

  created_at datetime
}

// ------------------------------------------
// LEAVE MANAGEMENT
// ------------------------------------------
Table tbl_leave_balance {
  id integer [pk]
  employee_id integer
  type leave_type
  academic_year string

  allocated_days decimal
  used_days decimal [default: 0]

  updated_at datetime
}

Table tbl_leave_request {
  id integer [pk]
  employee_id integer
  type leave_type

  start_date date
  end_date date
  total_days decimal

  reason text
  status leave_status [default: 'PENDING']

  approved_by_id integer [note: 'Populated via Workflow Service']
  created_at datetime
}

// ------------------------------------------
// PAYROLL ENGINE
// ------------------------------------------
Table tbl_payroll_adjustment {
  id integer [pk]
  employee_id integer

  month string [note: 'e.g., 2026-07']
  type string [note: 'BONUS, DEDUCTION, LOP']
  amount decimal [note: 'Positive for Bonus, Negative for Deduction']
  reason string [note: 'e.g., ATS Referral Clawback or Overtime']

  created_at datetime
}

Table tbl_salary_slip {
  id integer [pk]
  employee_id integer
  month string [note: 'e.g., 2026-07']

  base_pay decimal
  total_allowances decimal
  total_deductions decimal [note: 'Taxes, LOPs, Clawbacks']
  net_pay decimal

  status payroll_status [default: 'DRAFT']
  transaction_ref string [note: 'Bank wire transfer ID']

  generated_at datetime
  paid_at datetime
}

// ==========================================
// RELATIONSHIPS
// ==========================================
Ref: tbl_leave_balance.employee_id > tbl_employee_contract.user_id
Ref: tbl_leave_request.employee_id > tbl_employee_contract.user_id
Ref: tbl_appraisal.employee_id > tbl_employee_contract.user_id
Ref: tbl_payroll_adjustment.employee_id > tbl_employee_contract.user_id
Ref: tbl_salary_slip.employee_id > tbl_employee_contract.user_id
```

---

# 4. Payroll Architecture

To maintain flexibility and a complete audit trail, payroll processing is intentionally divided into two separate tables.

## `tbl_payroll_adjustment` (The Running Ledger)

This table acts as a **running payroll ledger** for the current payroll cycle.

Throughout the month, multiple ERP services may generate financial adjustments that affect an employee's upcoming salary. Instead of immediately modifying the salary slip, these adjustments are accumulated here until the payroll run.

### Characteristics

- One employee can have **multiple adjustments** within the same month.
- Supports both positive and negative entries.
- Acts as the source of truth for variable payroll components.

### Example Entries

- ATS Referral Bonus
- ATS Referral Clawback
- Loss of Pay (LOP)
- Overtime
- Library Fine
- Manual Bonus
- Manual Deduction

---

## `tbl_salary_slip` (The Final Receipt)

This table stores the finalized payroll for a specific employee and payroll period.

During the payroll run, the engine gathers all adjustments from the ledger, combines them with the employee's fixed salary components, calculates the final payable amount, and generates an immutable salary slip.

### Characteristics

- One employee has **exactly one salary slip per payroll month**.
- Represents the official payroll record.
- Once paid, the record should never be modified.

---

# 5. Month-End Payroll Execution Lifecycle

On the configured payroll run date (for example, the **28th of every month**), a scheduled payroll job executes the following sequence.

---

## Step 1 — Base Salary Retrieval

The payroll engine retrieves the employee's `base_salary` from `tbl_employee_contract`.

---

## Step 2 — Leave & Loss of Pay (LOP) Calculation

The payroll engine evaluates employee attendance and approved leave records.

If unpaid leave exists, the system calculates the corresponding **Loss of Pay (LOP)** and inserts a negative adjustment into `tbl_payroll_adjustment`.

---

## Step 3 — Cross-Service Adjustment Collection

The payroll engine gathers all pending payroll adjustments for the payroll month.

Typical adjustment sources include:

- ATS Referral Bonuses
- ATS Referral Clawbacks
- Overtime
- Manual Adjustments
- Library Fines
- Other payroll deductions

---

## Step 4 — Salary Slip Generation

The engine combines:

- Base Salary
- Allowances
- All Payroll Adjustments

It then calculates:

- Total Allowances
- Total Deductions
- Net Pay

Finally, it generates a new record in `tbl_salary_slip` with the status:

```text
DRAFT
```

---

## Step 5 — Maker-Checker Approval

Once payroll generation is complete, the HR & Payroll Service publishes an approval request to the **Workflow Service**.

The designated Finance Controller (or authorized payroll approver) reviews the payroll batch.

- If approved, the salary slip status changes to:

```text
PAID
```

- If rejected, the payroll returns for correction before payment.

---

## Step 6 — Salary Disbursement

After approval, the HR & Payroll Service initiates the bank transfer process.

The returned bank transaction reference is stored in:

```text
transaction_ref
```

The payroll record is updated with:

- `status = PAID`
- `paid_at = Current Timestamp`

This marks the payroll cycle as complete and provides a permanent audit trail for employee salary payments.

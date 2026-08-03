# ATS & Recruitment Service

## ATS & Recruitment Enums

```dbml
// ==========================================
// ATS & RECRUITMENT ENUMS
// ==========================================
Enum job_status {
  DRAFT
  PUBLISHED
  CLOSED
}

Enum application_status {
  APPLIED
  SCREENING
  INTERVIEWING
  OFFER_EXTENDED
  HIRED
  REJECTED
}

Enum referral_payout_status {
  PENDING_HIRE
  PAID
  CLAWED_BACK
}
```

---

## Table Group

```dbml
// ==========================================
// TABLE GROUP
// ==========================================
TableGroup ATSService {
  tbl_job_posting
  tbl_candidate
  tbl_job_application
  tbl_interview_round
  tbl_referral
}
```

---

## ATS & Recruitment Database

```dbml
// ==========================================
// 7. ATS & RECRUITMENT DATABASE
// ==========================================

Table tbl_job_posting {
  id integer [pk]
  department_id integer [note: 'Soft ref: tbl_department.id (Academic Service)']
  title string
  description text
  status job_status [default: 'DRAFT']
  vacancies integer
  created_at datetime
}

Table tbl_candidate {
  id integer [pk]
  first_name string
  last_name string
  email string [unique]
  phone string
  resume_url string [note: 'Link to secure S3 vault']
  linkedin_url string
  created_at datetime
}

Table tbl_job_application {
  id integer [pk]
  job_id integer
  candidate_id integer
  status application_status [default: 'APPLIED']
  applied_at datetime

  indexes {
    (job_id, candidate_id) [unique, note: 'Strict Rule: Apply ONLY ONCE per job posting']
  }
}

Table tbl_interview_round {
  id integer [pk]
  application_id integer
  round_name string [note: 'e.g., Demo Class, Principal Round']
  interviewer_id integer [note: 'Soft ref: tbl_user.id (IAM Service)']
  scheduled_at datetime
  score integer [note: '1 to 10 rating']
  feedback text
  passed boolean
}

// ------------------------------------------
// THE REFERRAL & PENALTY ENGINE
// ------------------------------------------
Table tbl_referral {
  id integer [pk]
  application_id integer [unique]
  referrer_user_id integer [note: 'Soft ref: tbl_user.id (IAM Service) - Who referred them?']

  bonus_amount decimal
  payout_status referral_payout_status [default: 'PENDING_HIRE']

  candidate_hired_date date
  candidate_left_date date

  is_strike_counted boolean [default: false, note: 'True if they left < 1 year']
}

// ==========================================
// RELATIONSHIPS
// ==========================================
Ref: tbl_job_application.job_id > tbl_job_posting.id
Ref: tbl_job_application.candidate_id > tbl_candidate.id
Ref: tbl_interview_round.application_id > tbl_job_application.id
Ref: tbl_referral.application_id - tbl_job_application.id
```

---

# Cross-Service Logic: Event Consumer Pattern

Because this is a microservices architecture, **database triggers are strictly prohibited** for cross-module actions (such as ATS updating HR Payroll). Database triggers lock transactions, break database isolation, and are difficult to test.

Instead, the **Referral Clawback** and **3-Strike Penalty** are implemented using the **Event Consumer Pattern**.

## Architecture Flow

**Publisher (HR Service)**

When an employee resigns or is terminated, the HR Service publishes an `hr.employee_terminated` event to the message broker.

**Message Broker (RabbitMQ/Kafka)**

The broker reliably stores and delivers the event.

**Consumer (ATS Service)**

The ATS Service listens for termination events and executes all referral-related business logic within the application layer.

---

# Execution Lifecycle

## Step 1 — Termination Event

The HR Service marks an employee as terminated after **8 months** of employment and publishes the following event to RabbitMQ/Kafka.

The event includes the employee's original ATS application ID.

---

## Step 2 — Tenure Evaluation

The ATS Service consumes the event and looks up the corresponding record in `tbl_referral`.

It calculates:

> **Tenure = Candidate Left Date − Candidate Hired Date**

Since the tenure is **less than 365 days**, the referral is considered a violation.

---

## Step 3 — Financial Clawback

The ATS Service updates:

```text
payout_status = CLAWED_BACK
```

It then publishes a new event:

```text
payroll.deduction_requested
```

The HR/Payroll Service processes this event and deducts the referral bonus from the referrer's next payroll.

---

## Step 4 — Strike Assessment

The ATS Service marks:

```text
is_strike_counted = true
```

It then counts the total number of recorded strikes for the referring employee.

---

## Step 5 — Penalty Trigger

If the employee accumulates **three strikes**, the ATS Service sends a request to the **Workflow Service**.

The Workflow Service initiates the **Probation / Re-Interview Protocol**, notifying the Principal and creating the required approval workflow.

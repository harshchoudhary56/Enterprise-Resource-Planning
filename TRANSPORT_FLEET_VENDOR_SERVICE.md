# Transport Vendor Logistics & Settlement Module

## Overview

This document defines the database schema, business logic, billing engine, fraud detection workflow, and service integration lifecycle for the **Transport Vendor Logistics & Settlement Module**.

---

# 1. Transport Vendor Logistics Enums

## Billing Model

| Enum | Description |
|------|-------------|
| `PER_KM` | Charged strictly based on total kilometres run |
| `PER_DAY` | Fixed daily rate per bus regardless of distance |
| `PER_TRIP` | Fixed rate per individual pickup or drop run |
| `HYBRID_MIN_KM` | Base daily rate up to a configured kilometre limit plus extra fee per additional kilometre |

---

## Trip Type

| Enum | Description |
|------|-------------|
| `REGULAR_PICKUP_DROP` | Daily student school commute |
| `SPECIAL_EVENT` | Sports day, picnic, educational excursion |
| `EXTRA_RUN` | Late bus for remedial classes, sports practice, etc. |

---

## Trip Status

| Enum | Description |
|------|-------------|
| `SCHEDULED` | Trip scheduled |
| `IN_PROGRESS` | Trip currently running |
| `COMPLETED` | Trip successfully completed |
| `CANCELLED` | Trip cancelled |
| `DISPUTED` | GPS distance differs significantly from odometer reading |

---

## Vendor Invoice Status

| Enum | Description |
|------|-------------|
| `DRAFT` | Invoice generated but not submitted |
| `PENDING_AUDIT` | Awaiting transport audit |
| `APPROVED_FOR_PAYMENT` | Approved by transport manager |
| `PAID` | Payment completed |
| `REJECTED` | Invoice rejected |

---

# 2. Database Schema

---

## 2.1 Transport Vendor Contract

### `tbl_transport_vendor_contract`

| Column | Type | Notes |
|---------|------|------|
| id | Integer (PK) | Primary Key |
| vendor_id | Integer | Soft reference to Procurement Service Vendor |
| contract_number | String | Unique Contract Number |
| start_date | Date | Contract start |
| end_date | Date | Contract end |
| is_active | Boolean | Default = true |

---

## 2.2 Vendor Rate Card

### `tbl_vendor_rate_card`

| Column | Type | Notes |
|---------|------|------|
| id | Integer (PK) | |
| contract_id | Integer | FK → Vendor Contract |
| vehicle_capacity_type | Integer | Example: 32 Seater, 50 Seater, AC/Non-AC |
| model | Billing Model Enum | Billing strategy |
| base_daily_rate | Decimal | Fixed daily charge |
| base_included_kms | Decimal | Included kilometres in hybrid model |
| rate_per_km | Decimal | Per KM rate or hybrid overage rate |
| rate_per_trip | Decimal | Used in PER_TRIP model |
| fuel_surcharge_rate | Decimal | Dynamic surcharge based on fuel price |

---

## 2.3 Vendor Vehicle

### `tbl_vendor_vehicle`

| Column | Type | Notes |
|---------|------|------|
| id | Integer (PK) | |
| contract_id | Integer | FK → Vendor Contract |
| registration_number | String | Unique vehicle registration |
| seating_capacity | Integer | Number of seats |
| is_ac | Boolean | Air-conditioned bus |
| fitness_cert_expiry | Date | Fitness certificate expiry |
| insurance_expiry | Date | Insurance expiry |
| permit_expiry | Date | Permit expiry |
| is_active | Boolean | Default = true |

---

## 2.4 Daily Trip Log

### `tbl_daily_trip_log`

| Column | Type | Notes |
|---------|------|------|
| id | Integer (PK) | |
| trip_number | String | Unique Trip Number |
| vehicle_id | Integer | FK → Vendor Vehicle |
| route_id | Integer | Soft reference to Facility Service Route |
| driver_user_id | Integer | Soft reference to IAM Driver |
| trip_type | Enum | Default = REGULAR_PICKUP_DROP |
| date | Date | Trip Date |
| start_time | DateTime | Trip Start |
| end_time | DateTime | Trip End |
| start_odometer | Decimal | Starting reading |
| end_odometer | Decimal | Ending reading |
| claimed_distance_km | Decimal | End − Start |
| gps_verified_distance_km | Decimal | Calculated via GPS |
| status | Trip Status Enum | Default = SCHEDULED |
| discrepancy_flag | Boolean | True if GPS variance > 10% |

---

## 2.5 Vendor Settlement Period

### `tbl_vendor_settlement_period`

| Column | Type | Notes |
|---------|------|------|
| id | Integer (PK) | |
| contract_id | Integer | FK → Vendor Contract |
| period_start_date | Date | Settlement Start |
| period_end_date | Date | Settlement End |
| total_trips | Integer | Number of trips |
| total_kms_allowed | Decimal | Billable kilometres |
| total_payable_amount | Decimal | Final invoice amount |
| status | Vendor Invoice Status | Default = DRAFT |
| generated_at | DateTime | Invoice generation time |
| approved_by_id | Integer | Transport Manager |

---

## 2.6 Vendor Invoice Line Item

### `tbl_vendor_invoice_line_item`

| Column | Type | Notes |
|---------|------|------|
| id | Integer (PK) | |
| settlement_period_id | Integer | FK → Settlement Period |
| trip_log_id | Integer | FK → Daily Trip |
| description | String | Human-readable calculation |
| calculated_amount | Decimal | Final line amount |

---

# 3. Entity Relationships

```
tbl_transport_vendor_contract
            │
            ├──────────────┐
            │              │
            ▼              ▼
tbl_vendor_rate_card   tbl_vendor_vehicle
                               │
                               ▼
                     tbl_daily_trip_log
                               │
                               ▼
                  tbl_vendor_invoice_line_item
                               ▲
                               │
                 tbl_vendor_settlement_period
                               ▲
                               │
                 tbl_transport_vendor_contract
```

---

# 4. Core Components & Business Logic

## 4.1 Flexible Rate Engine

The pricing engine is entirely configuration-driven through the **Vendor Rate Card**.

Different schools can use completely different pricing models without changing application code.

---

### Model 1 — Per KM

```
Billing Model = PER_KM
Rate = ₹40/km

Trip Distance = 30 km

Cost = 30 × ₹40

Total = ₹1,200
```

---

### Model 2 — Per Day

```
Billing Model = PER_DAY

Daily Bus Rate = ₹3,500

Unlimited trips during the day

Total = ₹3,500
```

---

### Model 3 — Hybrid Guaranteed Minimum

```
Billing Model = HYBRID_MIN_KM

Base Daily Rate = ₹2,500
Included Distance = 60 km
Extra Rate = ₹35/km

Actual Distance = 75 km

Cost =
₹2,500
+
(75 − 60) × ₹35

= ₹3,025
```

---

## 4.2 Rate Calculation Logic

### PER_KM

```
Payable Amount =
Claimed Distance × Rate Per KM
```

---

### PER_DAY

```
Payable Amount =
Base Daily Rate
```

---

### PER_TRIP

```
Payable Amount =
Total Trips × Rate Per Trip
```

---

### HYBRID_MIN_KM

```
If Distance <= Included KM

    Pay = Base Daily Rate

Else

    Pay =
    Base Daily Rate
    +
    (Distance - Included KM)
    × Rate Per KM
```

---

# 5. Anti-Fraud Verification Pipeline

To prevent inflated vendor claims, the module verifies every completed trip using both:

- Driver-entered odometer readings
- GPS telemetry captured during the trip

---

## Step 1

Driver enters:

- Start Odometer
- End Odometer

System calculates:

```
Claimed Distance

=
End Odometer
-
Start Odometer
```

---

## Step 2

Backend computes:

```
GPS Verified Distance
```

using the recorded GPS coordinate stream.

---

## Step 3

Variance Calculation

```
Difference

=

| Claimed Distance

-

GPS Distance |
```

---

## Step 4

Audit Rule

If

```
Difference

>

10%
```

Then

```
discrepancy_flag = TRUE

status = DISPUTED
```

---

## Result

The Transport Manager receives an alert before invoice generation.

Disputed trips require manual approval.

---

# 6. Settlement Generation Workflow

At the end of the billing period:

1. Collect all completed trips
2. Ignore cancelled trips
3. Flag disputed trips
4. Apply contract billing model
5. Generate invoice line items
6. Compute total payable amount
7. Create settlement record
8. Submit for transport audit

---

# 7. Cross-Service Integration Lifecycle

```text
+--------------------+
|     Driver App     |
+--------------------+
          │
          │
          │ Start / End Odometer
          ▼
+-----------------------------+
| Fleet Vendor Service        |
| - Stores Trip Log           |
| - GPS Verification          |
| - Fraud Detection           |
+-----------------------------+
          │
          │
          ▼
+-----------------------------+
| Rate Card Engine            |
| - Billing Model             |
| - Invoice Calculation       |
| - Settlement Generation     |
+-----------------------------+
          │
          ▼
+-----------------------------+
| Transport Manager           |
| - Audit                     |
| - Approve                   |
+-----------------------------+
          │
          │ Event
          │ vendor.transport_invoice.approved
          ▼
+-----------------------------+
| Finance Service             |
| - Debit System Bank         |
| - Credit Vendor Account     |
| - Double Entry Ledger       |
+-----------------------------+
```

---

# 8. Foreign Key Relationships

```text
tbl_vendor_rate_card.contract_id
    → tbl_transport_vendor_contract.id

tbl_vendor_vehicle.contract_id
    → tbl_transport_vendor_contract.id

tbl_daily_trip_log.vehicle_id
    → tbl_vendor_vehicle.id

tbl_vendor_settlement_period.contract_id
    → tbl_transport_vendor_contract.id

tbl_vendor_invoice_line_item.settlement_period_id
    → tbl_vendor_settlement_period.id

tbl_vendor_invoice_line_item.trip_log_id
    → tbl_daily_trip_log.id
```

---

# 9. End-to-End Business Flow

```text
Vendor Contract
        │
        ▼
Vehicle Assignment
        │
        ▼
Daily Trip Execution
        │
        ▼
GPS Verification
        │
        ▼
Distance Validation
        │
        ▼
Billing Calculation
        │
        ▼
Settlement Generation
        │
        ▼
Transport Approval
        │
        ▼
Finance Payment
        │
        ▼
Vendor Paid
```

---

# 10. Key Features

- Configurable billing models
- Contract-specific pricing
- Per-KM, Per-Day, Per-Trip, and Hybrid billing
- GPS vs Odometer fraud detection
- Automated invoice generation
- Settlement period management
- Detailed invoice line items
- Audit and approval workflow
- Finance service integration
- Event-driven payment processing
- Scalable multi-school configuration
- Complete vendor lifecycle management
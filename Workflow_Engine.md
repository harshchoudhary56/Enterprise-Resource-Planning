# Workflow & Approval Service - Engine Mechanics

This document explains the internal mechanics of the Workflow & Approval Service (The Maker-Checker Engine).

Because this is a **metadata-driven** microservice, it does not store hardcoded business logic. Instead, it uses four core tables to handle agnostic JSON payloads, allowing it to process approvals for any other microservice in the ERP seamlessly.

## The Scenario
To understand the architecture, we use a high-stakes transaction:
**The IT Manager (Maker) wants to buy 50 new MacBooks for $50,000.**
Because the amount exceeds standard budgets, school policy dictates a multi-tier approval:
1.  **Principal** (To verify academic need)
2.  **Finance Controller** (To approve the budget release)

Here is exactly how the 4 core tables process this transaction.

---

## Phase 1: The Configuration (Set up once by Admin)
Before any requests can be made, the System Admin defines the rules. This data lives permanently in the configuration tables.

### 1. `tbl_workflow_rule` (The Event Dictionary)
The Admin registers that a "High Value Purchase" event exists in the ecosystem.

| id | event_name | origin_service | is_active | description |
| :--- | :--- | :--- | :--- | :--- |
| **10** | `asset.high_value_purchase` | ASSET | true | Requires multi-tier approval for >$10k |

### 2. `tbl_workflow_step` (The Multi-Tier Ladder)
The Admin configures *who* needs to approve Rule 10, and in *what order*. Notice there are two rows tied to Rule 10.

| id | rule_id | step_order | required_role_id | is_final_step |
| :--- | :--- | :--- | :--- | :--- |
| 1 | 10 | **1** | 5 (Role: Principal) | false |
| 2 | 10 | **2** | 9 (Role: Finance Controller) | **true** |

*Result: The engine now knows that event `asset.high_value_purchase` must go to Role 5 first, and Role 9 second. Role 9 is the final gatekeeper.*

---

## Phase 2: The Ticket Creation

### 3. `tbl_approval_request` (The Inbox Ticket)
The IT Manager goes into the Asset Service and submits the request. The Asset Service pauses, bundles the data into JSON, and asks the Workflow Service to create a ticket.

| id | rule_id | maker_user_id | payload (JSONB) | current_step_order | status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **884** | 10 | 42 (IT Manager) | `{"item": "MacBook", "cost": 50000}` | **1** | **PENDING** |

*Result: Because `current_step_order` is **1**, the system looks at `tbl_workflow_step`, sees Step 1 requires the Principal, and alerts them.*

---

## Phase 3: The Approvals & Audit Trail

### 4. `tbl_approval_log` (Step 1: Principal Approves)
The Principal logs into their Unified Inbox, reviews Ticket 884, and clicks "Approve." An immutable record is stamped into the log table:

| id | request_id | checker_user_id | step_order | action | comment |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 501 | **884** | 8 (Principal) | 1 | APPROVE | "Academic need confirmed." |

*Result:* The system updates the main ticket (`tbl_approval_request`):
*   `current_step_order` increments to **2**.
*   `status` remains **PENDING**.
*   It alerts the Finance Controller.

### 4. `tbl_approval_log` (Step 2: Finance Approves)
The Finance Controller reviews the same ticket, checks the budget, and approves it.

| id | request_id | checker_user_id | step_order | action | comment |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 502 | **884** | 12 (Finance) | 2 | APPROVE | "Funds allocated from Q3 budget." |

*Result:* The system checks `tbl_workflow_step` for Step 2 and sees `is_final_step = true`.
*   `status` changes to **APPROVED**.
*   The Workflow Service fires a webhook to the Asset Service: *"Ticket 884 is Approved. Execute your JSON payload."*

---

## Summary of Responsibilities
*   **`tbl_workflow_rule`** = **What** is the event? (The Dictionary)
*   **`tbl_workflow_step`** = **Who** needs to approve it and in what order? (The Ladder)
*   **`tbl_approval_request`** = The actual **Ticket** holding the JSON payload and current state.
*   **`tbl_approval_log`** = The **Receipt** showing exactly who clicked the button and why.
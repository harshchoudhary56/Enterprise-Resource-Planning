# Notification Service - Engine Mechanics & Schema

This document outlines the architecture and database schema of the central Notification Service.

In a distributed microservices environment, individual services (like Academic or Finance) should never send emails or SMS directly. Instead, they publish an event, and this dedicated Notification Service handles the delivery. This provides two massive enterprise benefits:

1. **Dynamic Templates:** Administrators can edit email/SMS text via a UI without requiring code deployments.
2. **Immutable Audit Trails:** The school maintains a strict legal log of exactly what was sent, to whom, and whether it was successfully delivered.

---

# 1. Core Tables & Responsibilities

## `tbl_template` (The Content Engine)

Stores the dynamic text for every system event. Instead of hardcoding "Your fee is due," the system uses templates like `"Dear {{name}}, your fee of {{amount}} is due."` This allows the school's marketing or admin team to control messaging without developer intervention.

## `tbl_user_preference` (The Opt-Out Manager)

Allows users to control their communication channels. A parent might want attendance alerts via SMS, but fee receipts via Email. Before sending any message, the engine checks this table to ensure the user has not opted out of the specified channel.

## `tbl_notification_log` (The Audit Trail)

The most critical table for compliance. It stores the exact, finalized text that was sent out, the timestamp, and the delivery receipt from the third-party provider (e.g., Twilio, AWS SES). If a parent claims they were never notified of a suspension, this table provides the proof of delivery.

---

# 2. Database Schema (DBML)

```dbml
// ==========================================
// NOTIFICATION SERVICE ENUMS
// ==========================================
Enum channel_type {
  EMAIL
  SMS
  WHATSAPP
  PUSH
}

Enum notification_status {
  PENDING
  SENT
  FAILED
  RETRYING
}

// ==========================================
// TABLE GROUP
// ==========================================
TableGroup NotificationService {
  tbl_template
  tbl_notification_log
  tbl_user_preference
}

// ==========================================
// NOTIFICATION SERVICE DATABASE
// ==========================================

Table tbl_template {
  id integer [pk]
  event_name string [unique, note: 'e.g., fee.reminder, attendance.absent']
  channel channel_type
  subject_template string [note: 'For emails. e.g., Alert: {{student_name}} is absent']
  body_template text [note: 'The actual message containing {{variables}}']
  is_active boolean [default: true]
  created_at datetime
  updated_at datetime
}

Table tbl_notification_log {
  id integer [pk]
  recipient_user_id integer [note: 'Soft ref: tbl_user.id (IAM Service)']
  event_name string [note: 'Which event triggered this?']
  channel channel_type
  contact_endpoint string [note: 'The actual phone no. or email address used']

  subject string
  body text [note: 'The finalized message sent to the user']

  status notification_status [default: 'PENDING']
  provider_response string [note: 'Error codes or success IDs from Twilio/AWS/SendGrid']

  created_at datetime
  sent_at datetime
}

Table tbl_user_preference {
  id integer [pk]
  user_id integer [note: 'Soft ref: tbl_user.id (IAM Service)']
  channel channel_type
  is_enabled boolean [default: true]
  updated_at datetime
}
```

---

# 3. The Execution Lifecycle (Step-by-Step)

To understand how the Notification Service remains decoupled from the rest of the ERP, here is the exact lifecycle of a single message from trigger to delivery.

**Scenario:** A teacher marks a student absent in the Academic Service.

## Step 1: Event Publication (The Origin)

The Academic Service updates the database to mark the student absent. It does not send an SMS. Instead, it publishes an agnostic JSON payload to the message broker (RabbitMQ/Kafka):

```json
{
  "event_name": "attendance.absent",
  "recipient_user_id": 105,
  "contact_endpoint": "+1234567890",
  "variables": {
    "student_name": "John Doe",
    "date": "2026-07-22"
  }
}
```

## Step 2: Interception & Validation (The Notification Engine)

The Notification Service constantly listens to the message broker. It picks up the event and immediately checks `tbl_user_preference` to see if User ID `105` has disabled SMS alerts.

- If disabled: The process stops here.
- If enabled: It proceeds to Step 3.

## Step 3: Template Hydration (The Content Construction)

The service queries `tbl_template` for the active SMS template linked to the event `attendance.absent`.

**Template found:**

```text
Alert: {{student_name}} has been marked absent for {{date}}.
```

The engine injects the JSON variables into the string, creating the final text:

**Final Text:**

```text
Alert: John Doe has been marked absent for 2026-07-22.
```

## Step 4: Dispatch (The Provider Handoff)

The service makes an HTTP API call to the third-party telecom provider (e.g., Twilio for SMS, AWS SES for Email), passing the `contact_endpoint` and the final text.

## Step 5: Immutable Logging (The Audit Trail)

Regardless of whether Twilio succeeds or fails, the service writes a permanent record into `tbl_notification_log`.

It records the exact final text, the timestamp, and the `provider_response` (e.g., `Status: SENT`, `MessageID: SM12345`). This guarantees the school has legal proof that the message was processed and handed off to the carrier.

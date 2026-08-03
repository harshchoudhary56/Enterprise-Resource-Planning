# Asset & Procurement: Detailed Table Architecture & Business Logic

This document provides a deep architectural breakdown of the tables driving the Asset & Procurement Service. It explains not just *what* the tables store, but *how* they interact to enforce strict financial compliance, inventory tracking, and budget control.

---

## 1. Master Catalog & Inventory Engine

### `tbl_item_master`
**The Centralized Procurement Catalog**
* **Business Purpose:** In a school without an ERP, a Science teacher might order "Expo Markers," an Art teacher might order "Whiteboard Pens," and IT might order "Dry Erase Markers." The school ends up paying three different prices to three different vendors for the exact same item. This table stops that. It is a strict, standardized menu of every approved item the school buys.
* **Operational Mechanics:** Before anyone can request an item, it must exist here. The `type` column dictates system behavior: if an item is a `CONSUMABLE` (e.g., A4 Paper), the system tracks it in bulk. If it is a `FIXED_ASSET` (e.g., a MacBook), the system knows it must generate unique barcodes for every individual unit purchased.
* **Lifecycle:** Managed exclusively by the Procurement Admin. Teachers can browse it, but cannot add to it directly.

### `tbl_inventory_stock`
**The Real-Time OpEx Ledger**
* **Business Purpose:** This table manages the school's operational expenditure (OpEx) reality. It tracks exactly how much of a `CONSUMABLE` item is sitting in a specific supply closet.
* **Operational Mechanics:** This table is highly dynamic. It is **incremented** automatically when the loading dock logs a Goods Receipt (GRN) for consumables. It is **decremented** when a teacher successfully checks out a box of paper.
* **Automation:** The `reorder_level` field in the master table works with this table. A nightly cron job checks: *If `quantity_on_hand` < `reorder_level`*, it automatically drafts a new Purchase Request to restock the item.

---

## 2. Vendor & Budget Security

### `tbl_vendor`
**The Approved Supplier Matrix**
* **Business Purpose:** A core compliance table. Schools cannot legally pay unverified entities. This table holds the legal tax identifiers (`tax_id`) and contact details for approved suppliers.
* **Operational Mechanics:** A Purchase Order (`tbl_purchase_order`) fundamentally cannot be generated unless a valid `vendor_id` is attached.
* **Audit Rule:** Vendors are never deleted (`DELETE FROM`). If a vendor provides bad service or goes out of business, `is_active` is simply flipped to `false`. This preserves the historical audit trail of past purchases.

### `tbl_department_budget`
**The Encumbrance Accounting Engine (Financial Guardrail)**
* **Business Purpose:** This is the most critical financial table in the module. It prevents departments from spending money they don't have.
* **Operational Mechanics:** It tracks three distinct buckets of money:
    1. `total_allocated`: The yearly budget (e.g., $10,000).
    2. `encumbered_amount`: Money tied up in approved requests that haven't been billed yet.
    3. `spent_amount`: Money actually paid to the vendor.
* **The Trigger:** When the Principal approves a $2,000 Purchase Request, the Workflow Service fires an event. The Asset Service catches it and instantly adds $2,000 to `encumbered_amount`. If the department tries to buy a $9,000 smartboard the next day, the system blocks it, calculating: `$10,000 (Total) - $2,000 (Encumbered) = $8,000 (Available)`.

---

## 3. The Procurement Pipeline (Money Out)

### `tbl_purchase_request` (PR)
**The Internal Requisition**
* **Business Purpose:** The "Maker" step in the procurement process. It captures the initial intent: *Who wants what, why do they want it, and how much will it roughly cost?*
* **Cross-Service Integration:** This table relies heavily on soft references. `requester_user_id` points to the IAM (Identity) Service, and `department_id` points to the HR Service.
* **Lifecycle:** It starts as `DRAFT`. Once submitted, it moves to `PENDING_APPROVAL` and is handed off to the Workflow Service. No budget is consumed while it is pending.

### `tbl_pr_line_item`
**The Granular Wishlist**
* **Business Purpose:** A teacher rarely requests just one thing. A "Science Lab Upgrade" PR might contain 50 separate items. This table maps those individual items to the parent PR.
* **Operational Mechanics:** It links to `tbl_item_master`. If an item is totally new (e.g., a custom-built robotics kit), `item_master_id` is left null, and `custom_item_name` is used. The Procurement Admin will later formalize this item if it becomes a recurring purchase.

### `tbl_purchase_order` (PO)
**The Legally Binding Contract**
* **Business Purpose:** The PR is internal; the PO is external. This is the document actually emailed to Dell, Apple, or the textbook supplier. It commits the school's funds legally.
* **Operational Mechanics:** While a PR uses *estimated* prices, the PO locks in the *actual* negotiated prices (`actual_total`). When a PO is marked `ISSUED_TO_VENDOR`, the school is legally on the hook for the delivery.

---

## 4. Receiving & Verification (Loading Dock)

### `tbl_goods_receipt` (GRN)
**The Three-Way Matching Bridge**
* **Business Purpose:** In enterprise finance, you never pay a vendor just because they sent an invoice. You require **Three-Way Matching**: The PO (what we asked for) + The GRN (what physically arrived) + The Invoice (what they charged us). This table represents the physical arrival of boxes on campus.
* **Lifecycle:** A logistics staff member counts the boxes on the truck, enters the `vendor_invoice_number` handed to them by the driver, and signs off using their `received_by_id`.

### `tbl_grn_line_item`
**The Discrepancy & Quality Check**
* **Business Purpose:** Vendors make mistakes. They short-ship, or items break in transit. This table logs the exact reality of the delivery.
* **The Trigger Mechanism:** This table is the engine that spawns physical reality in the database.
    * If `quantity_received = 10` for laptops, the system dynamically inserts 10 distinct rows into `tbl_asset`.
    * If `quantity_rejected = 2` (broken screens), the system flags the Procurement Admin to demand a refund or replacement from the vendor.

---

## 5. Fixed Asset Lifecycle & TCO

### `tbl_asset`
**The Immutable Physical Register**
* **Business Purpose:** Tracks high-value Capital Expenditure (CapEx). Every row is a single, physical object with a printed QR code (`asset_tag`).
* **Operational Mechanics:** It maintains a real-time mapping of *where* the item is (`location_id` pointing to the Infrastructure Service) and *who* has it (`assigned_to_user_id` pointing to IAM).
* **Financial Role:** It tracks `purchase_price` and uses `depreciation_method` to automatically lower the `current_book_value` every fiscal quarter, a critical requirement for school tax audits.

### `tbl_asset_maintenance_log`
**The Total Cost of Ownership (TCO) Ledger**
* **Business Purpose:** Buying an asset is only half the cost; maintaining it is the other half. If the school buys a $5,000 commercial printer, but spends $1,000 every six months replacing broken gears, this table tracks it.
* **Analytical Power:** The CFO can run a query aggregating `tbl_asset.purchase_price` + `SUM(tbl_asset_maintenance_log.cost)`. If the TCO of repairing an old asset exceeds the cost of a new one, the system provides the data to justify a replacement.
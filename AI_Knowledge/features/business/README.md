# Business & Branches

Business legal lifecycle, public profile (BusinessProfile), branch CRUD, staff access, and invite codes. Each registration creates one concrete branch/store; more branches may be added later.

## Key decisions
- Business is the legal and lifecycle root. BusinessProfile stores brand color, logo, description, contact info (number, email, social URLs).
- Business owns branches. Branch = concrete store/establishment.
- A business membership has a default OWNER, MANAGER, or WORKER role for every present and future branch.
- A branch access override may assign another role or deny access for one branch. Multiple owners are allowed.
- OWNER can manage every role; MANAGER can manage workers only; WORKER cannot manage staff.
- Invite codes: secondary self-service path, optional maxUses + expiry.
- Delivery is per-branch. Each branch sets its own delivery mode. No business-level delivery profile.

## Catalog setup deadline
- Business users cannot complete setup manually.
- After seven days, 5 active products or 2 active services completes setup automatically; a non-empty smaller catalog requires moderation; an empty catalog is restricted from search.
- Branch coordinates selected in 2GIS provide the stored city and address. `addressDetails` remains available for entrance, floor, and office notes.
- Seller onboarding records whether the business offers PRODUCTS, SERVICES, or BOTH before choosing manual setup or managed import.

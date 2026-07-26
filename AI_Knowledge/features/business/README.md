# Business & Branches

Business is the legal root for a company, its profile, memberships, optional branches, invitations, business category, and unique offers. Items and services belong to a business and do not require a branch.

## Key decisions
- Business is the legal and lifecycle root. BusinessProfile stores brand color, logo, description, contact info (number, email, social URLs).
- Public BusinessProfile data hydrates Item/Service search rows and their detail modal; personal login email/password/2FA data is never Business profile data.
- Business owns optional branches. A branch is a concrete store/establishment or an additional fulfilment location; it is never required to create an Item or Service.
- A branch stores the business-selected latitude and longitude. Address search and map selection are provider-independent; 2GIS is not a persistence or runtime dependency.
- A business membership has a default OWNER, MANAGER, or WORKER role for every present and future branch.
- A branch access override may assign another role or deny access for one branch. Multiple owners are allowed.
- OWNER can manage every role; MANAGER can manage workers only; WORKER cannot manage staff.
- Invite codes: secondary self-service path, optional maxUses + expiry.
- Delivery is per-branch. Each branch sets its own delivery mode. No business-level delivery profile.
- Business scope is `ITEM`, `SERVICE`, or `BOTH`. It only states what the business offers; it is not a separate setup lifecycle.
- A business has one flat category that describes who the company is. Items and services have their own flat categories that describe what the business sells or does.
- Category suggestions can be selected or a user can create a new category. System and user-created categories are both searchable.

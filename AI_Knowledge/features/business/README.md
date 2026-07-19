# Business & Branches

Business account management, branch CRUD, staff management, contacts, and invite codes. Each registration creates one concrete branch/store; multi-branch management is deferred.

## Key decisions
- Business owns branches. Branch = concrete store/establishment.
- BusinessMember (business-level): OWNER, MANAGER, WORKER. Hierarchy: OWNER > MANAGER > WORKER.
- BranchMember (branch-level): OWNER, MANAGER, WORKER synced with BusinessMember.
- MANAGER can manage products/services for all branches. WORKER has branch-limited access.
- Staff management: owner creates staff → BranchMember + BusinessMember created.
- Invite codes: secondary self-service path, optional maxUses + expiry.
- Contacts: business_contact per branch, contactValue + primaryContact.
- BusinessExternalLink: provider (2GIS/INSTAGRAM/TELEGRAM/SITE/WHATSAPP), confidence, visibility.

## Catalog setup deadline
- Business users cannot complete setup manually.
- After seven days, 5 active products or 2 active services completes setup automatically; a non-empty smaller catalog requires moderation; an empty catalog is restricted from search.
- Branch coordinates selected in 2GIS provide the stored city and address. `addressDetails` remains available for entrance, floor, and office notes.
- Seller onboarding records whether the business offers PRODUCTS, SERVICES, or BOTH before choosing manual setup or managed import.

# Service — Feature Locks

LOCKED | Service discovery is chat-first, not automatic slot reservation | Search returns published service offers; customer contact is explicit business-wide chat | ServiceBranchOffer, ChatConversation, search flow
LOCKED | ServiceBranchOffer.active is the live-search toggle | No availability scoring or freshness tracking | ServiceBranchOffer, SearchDocument sync
LOCKED | Chat is explicit and business-wide | A service offer opens or resumes its business conversation; no service request is created by search | ChatServiceImpl, ServiceBranchOffer
LOCKED | ServiceResource is optional abstract capacity — NOT specialist accounts | No specialist login, payroll, or dedicated UI | ServiceResource entity

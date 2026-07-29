# Service — Feature Locks

LOCKED | Service discovery is chat-first, not automatic slot reservation | Search returns published service offers; customer contact is explicit business-wide chat | ServiceBranchOffer, ChatConversation, search flow
LOCKED | Service creation does not require a branch | A branch association is optional and carries location-specific facts only | Service creation, branch association, SearchDocument sync
LOCKED | Chat is explicit and business-wide | A service offer opens or resumes its business conversation; no service request is created by search | ChatServiceImpl, ServiceBranchOffer
LOCKED | ServiceResource is optional abstract capacity — NOT specialist accounts | No specialist login, payroll, or dedicated UI | ServiceResource entity
LOCKED | Semantic search metadata is stored only in SearchDocument | AI aliases, concepts, use cases, and embeddings are derived search data, not business-supplied Service facts | Service, SearchDocument

# Platform — Locks

LOCKED | Permission checks test PlatformPermission sets, never PlatformRole | Role is a base profile only; permissions are the authority | all platform-gated processors
LOCKED | Platform chat endpoints reject non-MANAGED_IMPORT conversations | Platform staff must not read private customer↔business GENERAL_SUPPORT chats | PlatformChatProcessor.requireManagedImportConversation
LOCKED | Platform catalog access requires EDIT_CATALOG_DURING_IMPORT AND an active managed-import grant with the matching item/service scope | Permission alone or a grant for the other catalog type must not open arbitrary catalog mutations | CatalogCapabilityService

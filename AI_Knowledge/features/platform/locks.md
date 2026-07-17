# Platform — Locks

LOCKED | Permission checks test PlatformPermission sets, never PlatformRole | Role is a base profile only; permissions are the authority | all platform-gated processors
LOCKED | Platform chat endpoints reject non-MANAGED_IMPORT conversations | Platform staff must not read private customer↔business GENERAL_SUPPORT chats | PlatformChatProcessor.requireManagedImportConversation
LOCKED | Platform catalog access requires EDIT_CATALOG_DURING_IMPORT AND active managed-import grant | Permission alone must not open arbitrary business catalogs | CatalogCapabilityService.hasPlatformCatalogAccess

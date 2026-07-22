# Platform — Locks

LOCKED | Permission checks test PlatformPermission sets, never PlatformRole, except AI enrichment | Role is a base profile only; permissions are the authority except where a more specific approved lock says otherwise | all platform-gated processors except PlatformAiEnrichmentProcessor
LOCKED | AI enrichment is available to every active platform member, not a separate permission | Platform membership is the approved entitlement for this internal tool | PlatformAiEnrichmentProcessor, /api/v1/platform/ai-enrichment, platform catalog UI
LOCKED | Platform chat endpoints reject non-MANAGED_IMPORT conversations | Platform staff must not read private customer↔business GENERAL_SUPPORT chats | PlatformChatProcessor.requireManagedImportConversation
LOCKED | Platform Item/Service access requires EDIT_ITEMS_SERVICES_DURING_IMPORT AND an active managed-import grant with the matching scope | Permission alone or a grant for the other type must not open arbitrary mutations | managed-import access and Item/Service processors

# Platform — Locks

LOCKED | Permission checks test PlatformPermission sets, never PlatformRole, except AI enrichment | Role is a base profile only; permissions are the authority except where a more specific approved lock says otherwise | all platform-gated processors except PlatformAiEnrichmentProcessor
LOCKED | AI enrichment is available to every active platform member, not a separate permission | Platform membership is the approved entitlement for this internal tool | PlatformAiEnrichmentProcessor, /api/v1/platform/ai-enrichment, platform catalog UI
LOCKED | Platform support staff may inspect customer↔business and platform-support conversations | `MANAGE_SUPPORT_CHATS` authorizes GENERAL_SUPPORT and PLATFORM_SUPPORT; MANAGED_IMPORT still requires its own permission and assigned active grant | PlatformChatProcessor
LOCKED | Managed-import activation grants catalog access per platform member and Business for seven days | The assigned active grant and matching ITEM/SERVICE/BOTH scope are the authority; no global edit permission opens arbitrary businesses | managed-import access and Item/Service processors

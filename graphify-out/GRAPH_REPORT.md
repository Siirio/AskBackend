# Graph Report - C:/MyProjects/Team/Ask/AskBackend  (2026-07-12)

## Corpus Check
- 453 files · ~95,856 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3285 nodes · 8327 edges · 192 communities (170 shown, 22 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 547 edges (avg confidence: 0.8)
- Token cost: 275,231 input · 0 output

## Community Hubs (Navigation)
- Brandexperienceprocessor Brandexperienceprocessor
- Business Card API
- Brandpageblocktype Brandpageblocktype
- Autodump Import Processor
- Autodump Audit Service
- Brand Drop Service
- Shared Error Codes
- Product Controller
- Autodump Import
- Public Search Processor
- Staffmanagementprocessor Staffmanagementprocessor
- Identityservice Identityservice
- Productimportprocessor Productimportprocessor
- Search V2 Processor
- Branch Invite Service
- Businessserviceprocessor Businessserviceprocessor
- Supplier Task Processor
- Catalog Import Service
- Autodump Draft Service
- Branch Controller
- Datasourcetype Datasourcetype
- Product DTOs
- Public Search Processor
- Raw Catalog Row Entity
- Autodump Import API
- Catalog Import Entity
- Catalog Import Service
- Userstatus Userstatus
- Serviceserviceimpl Java
- Business Branch Service
- Record Status Enum
- Autodumpimportservice Autodumpimportservice
- Importsessionstatus Importsessionstatus
- Branchmemberserviceimpl Branchmemberserviceimpl
- Identityserviceimpl Identityserviceimpl
- Structuredsearchprocessor Structuredsearchprocessor
- Oauth2Authsuccesshandler Java
- Businessserviceimpl Businessserviceimpl
- Product Entity
- Auth Processor
- Businesschatcontroller Businesschatcontroller
- Shared Error Codes
- Autodumpimportserviceimpl Autodumpimportserviceimpl
- Auth Controller
- Invitecontroller Java
- Createproductofferdto Java
- Auth Processor
- Contacttype Contacttype
- Globalexceptionhandler Globalexceptionhandler
- Autodumpmapper Autodumpmapper
- Searchdocumentrepository Searchdocumentrepository
- Public Search Processor
- Chatservice Chatservice
- Customerrequeststatus Customerrequeststatus
- Chat Conversations
- Businessproductcreaterequest Java
- Authchallengedto Java
- Supplierresponse Java
- Brandprofileserviceimpl Java
- Categoryserviceimpl Categoryserviceimpl
- Anti Marketplace
- Chatfilecontroller Java
- City Java
- Productserviceimpl Java
- Chat Messages
- Auth Challenge Entity
- Business Entity
- Aijobstatus Aijobstatus
- Categorycontroller Java
- Citycontroller Java
- Targetfield Targetfield
- Public Search Processor
- Booking Java
- Servicebranchoffer Java
- Auth Backend
- Autodump Draft Entity
- Requesttargetstatus Requesttargetstatus
- Autodumprawinput Java
- Contactactionservice Contactactionservice
- Customerrequestcontroller Java
- Searchsnapshot Java
- Geocontroller Java
- Previewresponse Java
- Productoffermapper Productoffermapper
- Chat Service
- Customer Request Processor
- Searchtermenricher Searchtermenricher
- Business Candidate
- Deepseekautodumpclient Java
- Catalogimportcontroller Java
- Auth Session Entity
- Loggingsmscodesender Java
- Deepseeksearchintentstructurer Java
- Servicebranchofferdto Java
- Seed Rationale
- Baseuuidv7Entity Java
- Excelparser Excelparser
- Searchsessionstatus Searchsessionstatus
- Serviceresource Java
- Identityserviceimpl Java
- Catalogimportmapper Catalogimportmapper
- Contactactioncontroller Java
- Businessregisterrequest Java
- Conversationparticipant Java
- Jwtauthfilter Java
- Business Entity
- Customerregisterrequest Java
- Conversation Java
- Conversationmessage Java
- Businessregistrationresult Java
- Systemnotifyrequest Java
- Contactcryptoservice Contactcryptoservice
- Authsessiondto Java
- Intentcategorymapper Intentcategorymapper
- Searchqueryalias Java
- Searchresultsnapshot Java
- Servicewindow Java
- Smtpemailcodesender Java
- Storefrontblockrequest Java
- Businesscontactdto Java
- Businessloginstartrequest Java
- Customerloginstartrequest Java
- Corsconfig Corsconfig
- Contactactionsummaryresponse Java
- Oauth2Googleconfig Java
- Errordetail Java
- Publishresponse Java
- Updatedraftrequest Java
- Deepseekautodumpconfig Java
- Approveresponse Java
- Businessproductupdaterequest Java
- Cancelresponse Java
- Columninfo Java
- Columnmappinginfo Java
- Mappingentry Java
- Mappingrequest Java
- Rowpreview Java
- Uploadresponse Java
- Chatconversationlistresponse Java
- Chatmessagedto Java
- Chatmessagelistresponse Java
- Sendmessagerequest Java
- Changetemporarypasswordrequest Java
- Loginrequest Java
- Verifycoderequest Java
- Loggingemailcodesender Java
- Createcustomerrequestrequest Java
- Deepseeksearchconfig Java
- Ai Query
- Rules Controller
- Openapiconfig Java
- Customerprofile Java
- Passwordencoderconfig Java
- Customerrequestdetailresponse Java
- Resourceserviceassignment Java
- Serviceschedule Java
- Agents Universal
- Storefrontblockresponse Java
- Rownormalizer Java
- Logoutresponse Java
- Customerrequesthistoryitem Java
- Result Card
- Business External
- Erd Branch
- Stock In
- Kz Ask
- Chat First
- Anti Marketplace
- Foundation Erd
- Search Query
- Readme Vps
- Compose App
- Vps Ssh
- Search Result
- External Link
- Concrete Product
- Service Impl
- Cert And
- Vps Install
- Vps Postgres
- Deploy Vps
- Supplier Response DTOs
- Erd Catalog
- Erd Service
- Readme
- Foundation Foundation
- Rules Entity
- Rules Error
- No Tests
- Rules Type
- Ask Ask

## God Nodes (most connected - your core abstractions)
1. `AskPrincipal` - 153 edges
2. `BaseUuidV7Entity` - 99 edges
3. `PublicSearchProcessor` - 90 edges
4. `NotFoundException` - 61 edges
5. `RecordStatus` - 59 edges
6. `ErrorCode` - 58 edges
7. `AppUser` - 55 edges
8. `SearchDocument` - 55 edges
9. `BusinessBranch` - 52 edges
10. `Business` - 50 edges

## Surprising Connections (you probably didn't know these)
- `Storefront Draft/Published Versioning (PUT draft -> POST publish copies draft to published)` --semantically_similar_to--> `Business Card Draft/Published Snapshot Separation`  [INFERRED] [semantically similar]
  AI_Knowledge/backend_tasks/10_brand_storefront_builder_backend.md → AGENTS.logic-locks.md
- `Concrete Product Variations Are Separate Product Entities` --semantically_similar_to--> `Product MVP Rule (one concrete sellable variation = one Product)`  [INFERRED] [semantically similar]
  AGENTS.md → AI_Knowledge/CODE_RULES.md
- `Data Model Normalization (No stockQuantity, availabilityConfidence, freshnessAt in MVP)` --semantically_similar_to--> `No Stock/Delivery/Logistics Tracking in MVP`  [INFERRED] [semantically similar]
  AI_Knowledge/CHANGELOG_FOUNDATION.md → AGENTS.md
- `Local Docker Compose` --semantically_similar_to--> `VPS Docker Compose Configuration`  [INFERRED] [semantically similar]
  docker-compose.yml → deploy/vps/compose.yml
- `Ask Platform` --conceptually_related_to--> `Architecture Narrative`  [INFERRED]
  README.md → AI_Knowledge/data_architecture/ARCHITECTURE_NARRATIVE.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Three-Layer Search Architecture (PostgreSQL -> Meilisearch -> AI)** — agents_postgresql_source_of_truth, agents_meilisearch_search_projection, agents_ai_query_structuring, agents_search_orchestrator [EXTRACTED 1.00]
- **Contact Privacy Vault (HMAC Dedup + AES Encryption + contactActionId Tokens)** — backend_tasks_09_hmac_sha256_dedup, backend_tasks_09_aes_256_gcm_encrypted_vault, backend_tasks_09_contact_action_id_pattern, agents_contact_privacy_architecture [EXTRACTED 1.00]
- **Anti-Marketplace Search Result Card (Brand Layer + Decision Layer, No Uniform Commodity Cards)** — agents_anti_marketplace_guardrails, agents_intent_layer_concept, backend_tasks_07_search_result_card, backend_tasks_07_default_sort_intent_match, agents_brand_profile_model [EXTRACTED 1.00]
- **Search Flow Pipeline** — AI_Knowledge_data_architecture_ARCHITECTURE_NARRATIVE_three_layer_search_architecture, src_main_resources_prompts_search-intent-structurer_search_intent_structurer_prompt, AI_Knowledge_data_architecture_ARCHITECTURE_NARRATIVE_search_plan_json, AI_Knowledge_data_architecture_ARCHITECTURE_NARRATIVE_search_orchestrator, AI_Knowledge_data_architecture_PRODUCT_SERVICE_FOUNDATION_ERD_search_v2_pipeline [EXTRACTED 1.00]
- **AI Autodump Import System** — AI_Knowledge_data_architecture_AI_AUTODUMP_IMPORT_ARCHITECTURE_ACTUALIZED_autodump_import_controller, AI_Knowledge_data_architecture_AI_AUTODUMP_IMPORT_ARCHITECTURE_ACTUALIZED_autodump_import_processor, AI_Knowledge_data_architecture_AI_AUTODUMP_IMPORT_ARCHITECTURE_ACTUALIZED_deepseek_autodump_client, src_main_resources_prompts_autodump-extraction_autodump_extraction_prompt, AI_Knowledge_data_architecture_AI_AUTODUMP_IMPORT_ARCHITECTURE_ACTUALIZED_autodump_import_session, AI_Knowledge_data_architecture_AI_AUTODUMP_IMPORT_ARCHITECTURE_ACTUALIZED_autodump_draft_item [EXTRACTED 1.00]
- **Service Booking Architecture (Chat-First)** — AI_Knowledge_client_contracts_UX_UI_BACKEND_CONTRACT_service_chat_first_model, AI_Knowledge_client_contracts_UX_UI_BACKEND_CONTRACT_three_tier_service_maturity, AI_Knowledge_client_contracts_UX_UI_BACKEND_CONTRACT_three_level_time_model, AI_Knowledge_client_contracts_UX_UI_BACKEND_CONTRACT_activity_display_status, AI_Knowledge_client_contracts_UX_UI_BACKEND_CONTRACT_supplier_response_status [EXTRACTED 1.00]

## Communities (192 total, 22 thin omitted)

### Community 0 - "Brandexperienceprocessor Brandexperienceprocessor"
Cohesion: 0.07
Nodes (37): BrandExperienceController, DeleteMapping, GetMapping, PatchMapping, PostMapping, PutMapping, RequestMapping, RequiredArgsConstructor (+29 more)

### Community 1 - "Business Card API"
Cohesion: 0.07
Nodes (41): BusinessCardController, GetMapping, PostMapping, PutMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+33 more)

### Community 2 - "Brandpageblocktype Brandpageblocktype"
Cohesion: 0.07
Nodes (32): BrandPageBlockService, BrandPageBlockServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, BrandPageBlockDto, Builder (+24 more)

### Community 3 - "Autodump Import Processor"
Cohesion: 0.09
Nodes (17): AutodumpSessionStatusResponse, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, AutodumpImportProcessor, Component (+9 more)

### Community 4 - "Autodump Audit Service"
Cohesion: 0.07
Nodes (35): AutodumpAuditService, AutodumpAuditServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, AutodumpAuditEvent, Entity (+27 more)

### Community 5 - "Brand Drop Service"
Cohesion: 0.08
Nodes (26): BrandDropService, BrandDropServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, BrandDropDto, Builder (+18 more)

### Community 6 - "Shared Error Codes"
Cohesion: 0.04
Nodes (48): ErrorCode, ACCESS_DENIED, ACCOUNT_NOT_ACTIVE, AI_INTENT_STRUCTURE_FAILED, AI_SEARCH_API_KEY_MISSING, AUTODUMP_AI_JOB_NOT_FOUND, AUTODUMP_DRAFT_NOT_FOUND, AUTODUMP_INPUT_READ_FAILED (+40 more)

### Community 7 - "Product Controller"
Cohesion: 0.09
Nodes (26): BusinessProductController, DeleteMapping, GetMapping, PatchMapping, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+18 more)

### Community 8 - "Autodump Import"
Cohesion: 0.05
Nodes (47): AuthChallengeResponse, AuthSessionResponse, ErrorResponse, Frontend API Tasks, Session TTL Configuration, StaffResponse, Staff Status Lifecycle, ActivityDisplayStatus (+39 more)

### Community 9 - "Public Search Processor"
Cohesion: 0.13
Nodes (7): SafeVarargs, Getter, Setter, SearchIntentStructureRequest, JsonNode, Pattern, PublicSearchProcessor

### Community 10 - "Staffmanagementprocessor Staffmanagementprocessor"
Cohesion: 0.11
Nodes (27): CreateStaffRequest, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, Builder, Getter (+19 more)

### Community 11 - "Identityservice Identityservice"
Cohesion: 0.09
Nodes (13): AuthUserResponse, Builder, Getter, Setter, Component, RequiredArgsConstructor, Transactional, LoginProcessor (+5 more)

### Community 12 - "Productimportprocessor Productimportprocessor"
Cohesion: 0.11
Nodes (14): InviteProcessor, Component, RequiredArgsConstructor, Transactional, BranchMemberService, BusinessService, Component, MultipartFile (+6 more)

### Community 13 - "Search V2 Processor"
Cohesion: 0.10
Nodes (23): Builder, Getter, SearchV2CardResponse, Getter, Setter, SearchV2Request, Builder, Getter (+15 more)

### Community 14 - "Branch Invite Service"
Cohesion: 0.10
Nodes (23): BranchInviteService, BranchInviteServiceImpl, Override, RequiredArgsConstructor, SecureRandom, Service, Transactional, BranchInviteDto (+15 more)

### Community 15 - "Businessserviceprocessor Businessserviceprocessor"
Cohesion: 0.11
Nodes (24): BusinessServiceController, GetMapping, PatchMapping, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+16 more)

### Community 16 - "Supplier Task Processor"
Cohesion: 0.12
Nodes (24): BusinessBranchService, Getter, Setter, SupplierRespondRequest, Builder, Getter, Setter, ResponseMessage (+16 more)

### Community 17 - "Catalog Import Service"
Cohesion: 0.11
Nodes (23): CatalogImportDto, Builder, Getter, Setter, CatalogImport, Entity, Getter, Setter (+15 more)

### Community 18 - "Autodump Draft Service"
Cohesion: 0.11
Nodes (19): AutodumpDraftServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, AutodumpDraftItem, Entity, Getter (+11 more)

### Community 19 - "Branch Controller"
Cohesion: 0.12
Nodes (21): BranchController, GetMapping, PatchMapping, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+13 more)

### Community 20 - "Datasourcetype Datasourcetype"
Cohesion: 0.10
Nodes (26): DataSourceService, DataSourceServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, DataSourceDto, Builder (+18 more)

### Community 21 - "Product DTOs"
Cohesion: 0.11
Nodes (24): CreateProductDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, AllArgsConstructor, Builder (+16 more)

### Community 22 - "Public Search Processor"
Cohesion: 0.14
Nodes (11): Builder, Getter, ScoredSearchDocument, Builder, Getter, SearchPlan, Entity, Getter (+3 more)

### Community 23 - "Raw Catalog Row Entity"
Cohesion: 0.11
Nodes (21): Builder, Getter, Setter, RawCatalogRowDto, Entity, Getter, Setter, Table (+13 more)

### Community 24 - "Autodump Import API"
Cohesion: 0.13
Nodes (21): AutodumpImportController, GetMapping, MultipartFile, PostMapping, PutMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+13 more)

### Community 25 - "Catalog Import Entity"
Cohesion: 0.12
Nodes (17): CatalogImportColumnMappingDto, Builder, Getter, Setter, CatalogImportColumnMapping, Entity, Getter, Setter (+9 more)

### Community 26 - "Catalog Import Service"
Cohesion: 0.11
Nodes (10): CatalogImportService, MultipartFile, ObjectMapper, Override, RequiredArgsConstructor, Service, Transactional, ProductImportServiceImpl (+2 more)

### Community 27 - "Userstatus Userstatus"
Cohesion: 0.09
Nodes (18): AppUser, Entity, Getter, Setter, Table, AppRole, BUSINESS, CUSTOMER (+10 more)

### Community 28 - "Serviceserviceimpl Java"
Cohesion: 0.12
Nodes (21): BusinessServiceCreateRequest, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, BusinessServiceUpdateRequest, AllArgsConstructor (+13 more)

### Community 29 - "Business Branch Service"
Cohesion: 0.15
Nodes (16): BusinessBranchServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, BusinessBranchDto, Builder, Getter (+8 more)

### Community 30 - "Record Status Enum"
Cohesion: 0.12
Nodes (21): BusinessMemberServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, BusinessMember, Entity, Getter (+13 more)

### Community 31 - "Autodumpimportservice Autodumpimportservice"
Cohesion: 0.11
Nodes (11): AutodumpExtractionClient, JsonNode, AutodumpImportService, AiJobDto, Builder, Getter, Setter, Builder (+3 more)

### Community 32 - "Importsessionstatus Importsessionstatus"
Cohesion: 0.09
Nodes (24): AutodumpImportSession, Entity, Getter, Setter, Table, ImportSessionStatus, CANCELLED, CREATED (+16 more)

### Community 33 - "Branchmemberserviceimpl Branchmemberserviceimpl"
Cohesion: 0.13
Nodes (16): BranchMemberServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, BranchMemberDto, Builder, Getter (+8 more)

### Community 34 - "Identityserviceimpl Identityserviceimpl"
Cohesion: 0.14
Nodes (3): IdentityServiceImpl, Override, Transactional

### Community 35 - "Structuredsearchprocessor Structuredsearchprocessor"
Cohesion: 0.12
Nodes (19): Builder, Getter, Setter, SearchResultCardResponse, Builder, Getter, Setter, SearchResultSectionResponse (+11 more)

### Community 36 - "Oauth2Authsuccesshandler Java"
Cohesion: 0.12
Nodes (22): Authentication, AuthenticationSuccessHandler, DefaultOAuth2UserService, EnableWebSecurity, HttpSecurity, OAuth2User, OAuth2UserRequest, SecurityFilterChain (+14 more)

### Community 37 - "Businessserviceimpl Businessserviceimpl"
Cohesion: 0.11
Nodes (14): BusinessMemberService, BusinessServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, BusinessDto, Builder (+6 more)

### Community 38 - "Product Entity"
Cohesion: 0.13
Nodes (19): Entity, Getter, Setter, Table, ProductOffer, Page, Pageable, Query (+11 more)

### Community 39 - "Auth Processor"
Cohesion: 0.13
Nodes (8): AuthChallengeResponse, Builder, Getter, Setter, Transactional, BusinessRegistrationPayload, Getter, Setter

### Community 40 - "Businesschatcontroller Businesschatcontroller"
Cohesion: 0.16
Nodes (10): ResponseStatus, BusinessChatController, GetMapping, PatchMapping, PostMapping, RequestMapping, RequiredArgsConstructor, RestController (+2 more)

### Community 41 - "Shared Error Codes"
Cohesion: 0.10
Nodes (14): AccessDeniedException, DataIntegrityViolationException, HttpMessageNotReadableException, HttpStatus, MaxUploadSizeExceededException, MethodArgumentNotValidException, MethodArgumentTypeMismatchException, RestControllerAdvice (+6 more)

### Community 42 - "Autodumpimportserviceimpl Autodumpimportserviceimpl"
Cohesion: 0.17
Nodes (10): EntityManager, AutodumpImportServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, ImportSessionDto, Builder (+2 more)

### Community 43 - "Auth Controller"
Cohesion: 0.21
Nodes (14): Operation, SecurityRequirement, AuthController, GetMapping, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+6 more)

### Community 44 - "Invitecontroller Java"
Cohesion: 0.15
Nodes (18): CreateInviteRequest, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, InviteResponse, Builder (+10 more)

### Community 45 - "Createproductofferdto Java"
Cohesion: 0.14
Nodes (18): CreateProductOfferDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, AllArgsConstructor, Builder (+10 more)

### Community 46 - "Auth Processor"
Cohesion: 0.13
Nodes (13): AuthBusinessContextResponse, Builder, Getter, Setter, Getter, Setter, UpdateProfileRequest, AuthProcessor (+5 more)

### Community 47 - "Contacttype Contacttype"
Cohesion: 0.11
Nodes (19): BusinessContactServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, ContactType, ASK_CHAT, EMAIL (+11 more)

### Community 48 - "Globalexceptionhandler Globalexceptionhandler"
Cohesion: 0.28
Nodes (7): ExceptionHandler, ErrorResponse, Builder, Getter, Setter, GlobalExceptionHandler, ResponseEntity

### Community 49 - "Autodumpmapper Autodumpmapper"
Cohesion: 0.15
Nodes (14): AuditEventDto, Builder, Getter, Setter, DraftAttributeDto, Builder, Getter, Setter (+6 more)

### Community 50 - "Searchdocumentrepository Searchdocumentrepository"
Cohesion: 0.16
Nodes (10): Override, RequiredArgsConstructor, Service, Transactional, SearchDocumentServiceImpl, Page, Pageable, Query (+2 more)

### Community 51 - "Public Search Processor"
Cohesion: 0.13
Nodes (5): BrandProfileService, BrandProfileDto, Builder, Getter, DistanceCalculator

### Community 52 - "Chatservice Chatservice"
Cohesion: 0.16
Nodes (12): ChatController, GetMapping, RequestMapping, RequiredArgsConstructor, RestController, ChatConversationDto, AllArgsConstructor, Builder (+4 more)

### Community 53 - "Customerrequeststatus Customerrequeststatus"
Cohesion: 0.14
Nodes (18): ChatRequestBridge, Component, RequiredArgsConstructor, Transactional, CustomerRequest, Entity, Getter, Setter (+10 more)

### Community 54 - "Chat Conversations"
Cohesion: 0.16
Nodes (15): ChatConversation, Entity, Getter, NoArgsConstructor, PrePersist, PreUpdate, Setter, Table (+7 more)

### Community 55 - "Businessproductcreaterequest Java"
Cohesion: 0.16
Nodes (15): BusinessProductCreateRequest, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, AllArgsConstructor, Builder (+7 more)

### Community 56 - "Authchallengedto Java"
Cohesion: 0.17
Nodes (12): AuthChallengeDto, Builder, Getter, Setter, AuthChallengeChannel, EMAIL, SMS, AuthChallengePurpose (+4 more)

### Community 57 - "Supplierresponse Java"
Cohesion: 0.12
Nodes (19): Entity, Getter, Setter, Table, SupplierResponse, SupplierResponseSource, AUTO_REPLY, BUSINESS_CONFIRMED (+11 more)

### Community 58 - "Brandprofileserviceimpl Java"
Cohesion: 0.19
Nodes (12): BrandProfileServiceImpl, Override, RequiredArgsConstructor, Service, Transactional, BrandProfile, Entity, Getter (+4 more)

### Community 59 - "Categoryserviceimpl Categoryserviceimpl"
Cohesion: 0.20
Nodes (11): CategoryServiceImpl, Override, RequiredArgsConstructor, Service, Category, Entity, Getter, Setter (+3 more)

### Community 60 - "Anti Marketplace"
Cohesion: 0.10
Nodes (20): Anti-Marketplace Guardrails, AskBackend, BrandProfile Data Model, Drop Types (NEW_COLLECTION, LIMITED_RELEASE, RESTOCK, CAPSULE, SEASONAL, COLLAB, PREORDER), Intent Layer (Not Marketplace), Business Card Draft/Published Snapshot Separation, Modular Monolith Architecture, Feature-First Packages (+12 more)

### Community 61 - "Chatfilecontroller Java"
Cohesion: 0.18
Nodes (15): connect(), ensure_dir(), list_dir(), main(), upload_dir(), FTP, Path, ChatFileController (+7 more)

### Community 62 - "City Java"
Cohesion: 0.19
Nodes (11): CityServiceImpl, Override, RequiredArgsConstructor, Service, City, Entity, Getter, Setter (+3 more)

### Community 63 - "Productserviceimpl Java"
Cohesion: 0.21
Nodes (9): Override, Page, Pageable, RequiredArgsConstructor, Service, Transactional, ProductServiceImpl, Repository (+1 more)

### Community 64 - "Chat Messages"
Cohesion: 0.15
Nodes (14): RequiredArgsConstructor, Service, ChatMessage, Entity, Getter, NoArgsConstructor, PrePersist, Setter (+6 more)

### Community 65 - "Auth Challenge Entity"
Cohesion: 0.17
Nodes (14): AuthChallenge, Entity, Getter, Setter, Table, AuthChallengeStatus, EXPIRED, FAILED (+6 more)

### Community 66 - "Business Entity"
Cohesion: 0.20
Nodes (15): JpaRepository, Business, Entity, Getter, Setter, Table, BusinessRepository, Repository (+7 more)

### Community 67 - "Aijobstatus Aijobstatus"
Cohesion: 0.16
Nodes (13): AutodumpAiJob, Entity, Getter, Setter, Table, AiJobStatus, CANCELLED, FAILED (+5 more)

### Community 68 - "Categorycontroller Java"
Cohesion: 0.20
Nodes (10): CategoryController, GetMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, CategoryResponse, Builder (+2 more)

### Community 69 - "Citycontroller Java"
Cohesion: 0.23
Nodes (11): CityController, GetMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, CityService, CityDto (+3 more)

### Community 70 - "Targetfield Targetfield"
Cohesion: 0.14
Nodes (13): TargetField, APPEND_TO_DESCRIPTION, CATEGORY_LABEL, CHARACTERISTIC, DESCRIPTION, IGNORE, NAME, PRICE (+5 more)

### Community 71 - "Public Search Processor"
Cohesion: 0.16
Nodes (10): Getter, Setter, SearchLocationRequest, Component, RequiredArgsConstructor, Transactional, SearchDocumentType, DROP (+2 more)

### Community 72 - "Booking Java"
Cohesion: 0.15
Nodes (15): Booking, Entity, Getter, Setter, Table, BookingSource, CUSTOMER_REQUEST, INTEGRATION (+7 more)

### Community 73 - "Servicebranchoffer Java"
Cohesion: 0.20
Nodes (13): Entity, Getter, Setter, Table, ServiceBranchOffer, ServiceMode, ON_DEMAND, SCHEDULED (+5 more)

### Community 74 - "Auth Backend"
Cohesion: 0.12
Nodes (17): Auth Backend Contract (AUTH_BACKEND_CONTRACT.md), Business Cabinet Product Endpoints (Task 03), Owner and Staff Unified Access (No Manager/Operator Split), Business Registration Creates AppUser, Business, BusinessBranch, BusinessMember, BusinessContact, Email-or-Phone Authentication Model, Identity Auth and Business Onboarding (Task 05), AutoMappingEngine (30+ Russian/English patterns per TargetField), ExcelParser (fastexcel streaming) (+9 more)

### Community 75 - "Autodump Draft Entity"
Cohesion: 0.18
Nodes (11): AutodumpDraftAttribute, Entity, Getter, Setter, Table, DraftAttributeSource, AI, SYSTEM (+3 more)

### Community 76 - "Requesttargetstatus Requesttargetstatus"
Cohesion: 0.18
Nodes (13): Entity, Getter, Setter, Table, RequestTarget, RequestTargetStatus, ANSWERED, EXPIRED (+5 more)

### Community 77 - "Autodumprawinput Java"
Cohesion: 0.20
Nodes (10): AutodumpRawInput, Entity, Getter, Setter, Table, StorageKind, DATABASE_TEXT, OBJECT_STORAGE (+2 more)

### Community 79 - "Customerrequestcontroller Java"
Cohesion: 0.23
Nodes (11): CustomerRequestController, GetMapping, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, CustomerRequestResponse (+3 more)

### Community 80 - "Searchsnapshot Java"
Cohesion: 0.17
Nodes (13): Entity, Getter, Setter, Table, SearchSnapshot, SearchScope, ALL, PRODUCT (+5 more)

### Community 81 - "Geocontroller Java"
Cohesion: 0.23
Nodes (8): GeoController, PostMapping, RequestMapping, ResponseEntity, RestController, Pattern, ParsedCoordinates, TwoGisLinkParser

### Community 82 - "Previewresponse Java"
Cohesion: 0.21
Nodes (8): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, PreviewResponse, MultipartFile, ProductImportService

### Community 83 - "Productoffermapper Productoffermapper"
Cohesion: 0.26
Nodes (7): Entity, Getter, Setter, Table, Product, Component, ProductOfferMapper

### Community 84 - "Chat Service"
Cohesion: 0.33
Nodes (3): ChatServiceImpl, Override, Transactional

### Community 85 - "Customer Request Processor"
Cohesion: 0.23
Nodes (6): CustomerRequestProcessor, Component, RequiredArgsConstructor, Transactional, Repository, SupplierResponseRepository

### Community 87 - "Business Candidate"
Cohesion: 0.14
Nodes (14): Backend Task Specification Format, Scope Locked After Submit (PRODUCT or SERVICE only), SearchSession Entity, Auto Supplier Check (Fallback Mechanism), Client Product Search Endpoint (Task 01), Product Request Fallback (POST /client/product-requests), Client Service Search Endpoint (Task 02), Service Request-to-Book Model (MVP) (+6 more)

### Community 88 - "Deepseekautodumpclient Java"
Cohesion: 0.27
Nodes (9): DeepSeekAutodumpClient, Component, JsonNode, ObjectMapper, Override, RequiredArgsConstructor, Resource, RestClient (+1 more)

### Community 89 - "Catalogimportcontroller Java"
Cohesion: 0.30
Nodes (8): CatalogImportController, GetMapping, MultipartFile, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController

### Community 90 - "Auth Session Entity"
Cohesion: 0.27
Nodes (9): AuthSession, Entity, Getter, Setter, Table, AuthSessionRepository, Modifying, Query (+1 more)

### Community 91 - "Loggingsmscodesender Java"
Cohesion: 0.20
Nodes (10): ConditionalOnProperty, Logger, Override, Service, LoggingSmsCodeSender, ConditionalOnProperty, Logger, Override (+2 more)

### Community 92 - "Deepseeksearchintentstructurer Java"
Cohesion: 0.27
Nodes (8): DeepSeekSearchIntentStructurer, Component, JsonNode, ObjectMapper, Override, RequiredArgsConstructor, Resource, RestClient

### Community 93 - "Servicebranchofferdto Java"
Cohesion: 0.25
Nodes (9): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ServiceBranchOfferDto, Page, Pageable (+1 more)

### Community 94 - "Seed Rationale"
Cohesion: 0.24
Nodes (12): _cat_of_product(), _cat_of_service(), gen_company(), mkid(), product_for_category(), Pick a product from the pool matching the category, cycling through., Infer category from product characteristics., Simple Russian word tokenizer. (+4 more)

### Community 95 - "Baseuuidv7Entity Java"
Cohesion: 0.23
Nodes (7): MappedSuperclass, BaseUuidV7Entity, Getter, PrePersist, PreUpdate, SecureRandom, UuidV7

### Community 96 - "Excelparser Excelparser"
Cohesion: 0.28
Nodes (4): Row, ExcelParser, Component, ExcelParseResult

### Community 97 - "Searchsessionstatus Searchsessionstatus"
Cohesion: 0.22
Nodes (11): Entity, Getter, Setter, Table, SearchSession, SearchSessionStatus, ACTIVE, CANCELLED (+3 more)

### Community 98 - "Serviceresource Java"
Cohesion: 0.22
Nodes (11): Entity, Getter, Setter, Table, ServiceResource, ResourceType, CHAIR, EQUIPMENT (+3 more)

### Community 99 - "Identityserviceimpl Java"
Cohesion: 0.18
Nodes (7): PostConstruct, PasswordEncoder, RequiredArgsConstructor, SecretKeySpec, SecureRandom, Service, InternalServerException

### Community 100 - "Catalogimportmapper Catalogimportmapper"
Cohesion: 0.27
Nodes (4): CatalogImportMapper, Component, ObjectMapper, RequiredArgsConstructor

### Community 101 - "Contactactioncontroller Java"
Cohesion: 0.29
Nodes (9): ContactActionController, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, ContactResolveResponse, Builder (+1 more)

### Community 102 - "Businessregisterrequest Java"
Cohesion: 0.30
Nodes (7): BusinessRegisterRequest, AllArgsConstructor, AssertTrue, Builder, Getter, NoArgsConstructor, Setter

### Community 103 - "Conversationparticipant Java"
Cohesion: 0.24
Nodes (10): ConversationParticipant, Entity, Getter, Setter, Table, ConversationParticipantType, BRANCH, BUSINESS (+2 more)

### Community 104 - "Jwtauthfilter Java"
Cohesion: 0.27
Nodes (7): FilterChain, OncePerRequestFilter, Component, HttpServletRequest, HttpServletResponse, Override, JwtAuthFilter

### Community 105 - "Business Entity"
Cohesion: 0.31
Nodes (7): BusinessContact, Entity, Getter, Setter, Table, BusinessContactRepository, Repository

### Community 106 - "Customerregisterrequest Java"
Cohesion: 0.33
Nodes (7): CustomerRegisterRequest, AllArgsConstructor, AssertTrue, Builder, Getter, NoArgsConstructor, Setter

### Community 107 - "Conversation Java"
Cohesion: 0.27
Nodes (9): Conversation, Entity, Getter, Setter, Table, ConversationStatus, ARCHIVED, CLOSED (+1 more)

### Community 108 - "Conversationmessage Java"
Cohesion: 0.27
Nodes (9): ConversationMessage, Entity, Getter, Setter, Table, MessageStatus, DELETED, READ (+1 more)

### Community 109 - "Businessregistrationresult Java"
Cohesion: 0.33
Nodes (6): BusinessRegistrationResult, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 110 - "Systemnotifyrequest Java"
Cohesion: 0.31
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, SystemNotifyRequest

### Community 111 - "Contactcryptoservice Contactcryptoservice"
Cohesion: 0.36
Nodes (4): ContactCryptoService, SecretKeySpec, SecureRandom, Service

### Community 112 - "Authsessiondto Java"
Cohesion: 0.29
Nodes (4): AuthSessionDto, Builder, Getter, Setter

### Community 113 - "Intentcategorymapper Intentcategorymapper"
Cohesion: 0.31
Nodes (5): IntentCategoryMapper, Component, Builder, Getter, StructuredCategorySignal

### Community 114 - "Searchqueryalias Java"
Cohesion: 0.36
Nodes (7): Entity, Getter, Setter, Table, SearchQueryAlias, Repository, SearchQueryAliasRepository

### Community 115 - "Searchresultsnapshot Java"
Cohesion: 0.31
Nodes (8): Entity, Getter, Setter, Table, SearchResultSnapshot, SearchResultSnapshotType, PRODUCT, SERVICE

### Community 116 - "Servicewindow Java"
Cohesion: 0.31
Nodes (8): Entity, Getter, Setter, Table, ServiceWindow, ServiceWindowType, AVAILABLE, BLOCKED

### Community 117 - "Smtpemailcodesender Java"
Cohesion: 0.36
Nodes (6): JavaMailSender, ConditionalOnProperty, Logger, Override, Service, SmtpEmailCodeSender

### Community 118 - "Storefrontblockrequest Java"
Cohesion: 0.36
Nodes (7): Getter, JsonNode, Setter, StorefrontBlockRequest, Getter, Setter, StorefrontDraftRequest

### Community 119 - "Businesscontactdto Java"
Cohesion: 0.33
Nodes (5): BusinessContactService, BusinessContactDto, Builder, Getter, Setter

### Community 120 - "Businessloginstartrequest Java"
Cohesion: 0.39
Nodes (7): BusinessLoginStartRequest, AllArgsConstructor, AssertTrue, Builder, Getter, NoArgsConstructor, Setter

### Community 121 - "Customerloginstartrequest Java"
Cohesion: 0.39
Nodes (7): CustomerLoginStartRequest, AllArgsConstructor, AssertTrue, Builder, Getter, NoArgsConstructor, Setter

### Community 122 - "Corsconfig Corsconfig"
Cohesion: 0.43
Nodes (5): CorsConfigurationSource, CorsConfig, Bean, Configuration, WebMvcConfigurer

### Community 123 - "Contactactionsummaryresponse Java"
Cohesion: 0.32
Nodes (5): ContactActionSummaryResponse, Builder, Getter, RequiredArgsConstructor, Service

### Community 124 - "Oauth2Googleconfig Java"
Cohesion: 0.48
Nodes (5): ClientRegistrationRepository, ConditionalOnExpression, Bean, Configuration, OAuth2GoogleConfig

### Community 125 - "Errordetail Java"
Cohesion: 0.43
Nodes (5): FieldError, ErrorDetail, Builder, Getter, Setter

### Community 126 - "Publishresponse Java"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, PublishResponse

### Community 127 - "Updatedraftrequest Java"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UpdateDraftRequest

### Community 128 - "Deepseekautodumpconfig Java"
Cohesion: 0.43
Nodes (5): DeepSeekAutodumpConfig, Bean, Builder, Configuration, RestClient

### Community 129 - "Approveresponse Java"
Cohesion: 0.52
Nodes (6): ApproveResponse, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 130 - "Businessproductupdaterequest Java"
Cohesion: 0.52
Nodes (6): BusinessProductUpdateRequest, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 131 - "Cancelresponse Java"
Cohesion: 0.52
Nodes (6): CancelResponse, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 132 - "Columninfo Java"
Cohesion: 0.52
Nodes (6): ColumnInfo, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 133 - "Columnmappinginfo Java"
Cohesion: 0.52
Nodes (6): ColumnMappingInfo, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 134 - "Mappingentry Java"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, MappingEntry

### Community 135 - "Mappingrequest Java"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, MappingRequest

### Community 136 - "Rowpreview Java"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RowPreview

### Community 137 - "Uploadresponse Java"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UploadResponse

### Community 138 - "Chatconversationlistresponse Java"
Cohesion: 0.52
Nodes (6): ChatConversationListResponse, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 139 - "Chatmessagedto Java"
Cohesion: 0.52
Nodes (6): ChatMessageDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 140 - "Chatmessagelistresponse Java"
Cohesion: 0.52
Nodes (6): ChatMessageListResponse, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 141 - "Sendmessagerequest Java"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, SendMessageRequest

### Community 142 - "Changetemporarypasswordrequest Java"
Cohesion: 0.52
Nodes (6): ChangeTemporaryPasswordRequest, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 143 - "Loginrequest Java"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, LoginRequest

### Community 144 - "Verifycoderequest Java"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, VerifyCodeRequest

### Community 145 - "Loggingemailcodesender Java"
Cohesion: 0.43
Nodes (5): ConditionalOnProperty, Logger, Override, Service, LoggingEmailCodeSender

### Community 146 - "Createcustomerrequestrequest Java"
Cohesion: 0.52
Nodes (6): CreateCustomerRequestRequest, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 147 - "Deepseeksearchconfig Java"
Cohesion: 0.43
Nodes (5): DeepSeekSearchConfig, Bean, Builder, Configuration, RestClient

### Community 148 - "Ai Query"
Cohesion: 0.33
Nodes (6): AI Query Structuring Helper, Meilisearch Fast Search Projection, PostgreSQL Source of Truth, Search Orchestrator, SearchPlan JSON Contract, DeepSeek Structured Search Integration

### Community 149 - "Rules Controller"
Cohesion: 0.33
Nodes (6): Controller Rules (ResponseEntity only, no @ResponseStatus), DTO Families (Request, Response, Dto), Forbidden Layer Calls (Controller->Repository, Mapper->Service, etc.), Layer Direction: Controller -> Processor -> DomainService -> Repository, Mapper Rules (hand-written only, called only by ServiceImpl), Jackson snake_case / Frontend camelCase Contract

### Community 150 - "Openapiconfig Java"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, OpenApiConfig

### Community 151 - "Customerprofile Java"
Cohesion: 0.60
Nodes (5): CustomerProfile, Entity, Getter, Setter, Table

### Community 152 - "Passwordencoderconfig Java"
Cohesion: 0.53
Nodes (4): Bean, Configuration, PasswordEncoder, PasswordEncoderConfig

### Community 153 - "Customerrequestdetailresponse Java"
Cohesion: 0.80
Nodes (5): CustomerRequestDetailResponse, Builder, Getter, Setter, SupplierReplyItem

### Community 154 - "Resourceserviceassignment Java"
Cohesion: 0.60
Nodes (5): Entity, Getter, Setter, Table, ResourceServiceAssignment

### Community 155 - "Serviceschedule Java"
Cohesion: 0.60
Nodes (5): Entity, Getter, Setter, Table, ServiceSchedule

### Community 156 - "Agents Universal"
Cohesion: 0.40
Nodes (5): Universal Conversations (linked to request, booking, product, service), ActivityDisplayStatus (Computed, Not Stored), Business Cabinet Service Endpoints (Task 04), Chat-First, Button-for-Fixation Philosophy, SupplierResponseStatus Enum (CAN_PROVIDE, CANNOT_PROVIDE, NEED_CLARIFICATION, SUGGEST_OTHER_TIME)

### Community 157 - "Storefrontblockresponse Java"
Cohesion: 0.70
Nodes (4): Builder, Getter, JsonNode, StorefrontBlockResponse

### Community 158 - "Rownormalizer Java"
Cohesion: 0.70
Nodes (4): Component, ObjectMapper, RequiredArgsConstructor, RowNormalizer

### Community 159 - "Logoutresponse Java"
Cohesion: 0.70
Nodes (4): Builder, Getter, Setter, LogoutResponse

### Community 160 - "Customerrequesthistoryitem Java"
Cohesion: 0.70
Nodes (4): CustomerRequestHistoryItem, Builder, Getter, Setter

### Community 161 - "Result Card"
Cohesion: 0.50
Nodes (4): SearchResultCardResponse, BrandPageBlock, BrandProfile, Drop (Time-Limited Brand Event)

### Community 162 - "Business External"
Cohesion: 0.50
Nodes (4): BusinessExternalLink, Contact Privacy Model, Public Business Discovery (Astana Import), Ask Business Outreach Leads (Astana)

### Community 163 - "Erd Branch"
Cohesion: 0.67
Nodes (4): Membership and Authority Model, BranchInvite Entity, BranchMember Entity, BusinessMember Entity

### Community 164 - "Stock In"
Cohesion: 0.50
Nodes (4): No Stock/Delivery/Logistics Tracking in MVP, SearchResultSnapshot Entity, SearchSnapshot Entity, Data Model Normalization (No stockQuantity, availabilityConfidence, freshnessAt in MVP)

### Community 166 - "Chat First"
Cohesion: 1.00
Nodes (3): Chat-First Service Booking Model, Three-Level Time Model, Three-Tier Service Maturity Model

### Community 167 - "Anti Marketplace"
Cohesion: 0.67
Nodes (3): Anti-Marketplace Ranking, Supplier Quality Signals, UserPreferenceProfile

### Community 168 - "Foundation Erd"
Cohesion: 0.67
Nodes (3): Conversation Entity, CustomerRequest Entity, SupplierResponse Entity

### Community 169 - "Search Query"
Cohesion: 0.67
Nodes (3): Search Query Expansion for Smartphone Wording, search_query_alias Table for Broad Customer Wording Expansion, SearchTermEnricher for Synonym Expansion (Sports Nutrition, Bike Rental)

### Community 170 - "Readme Vps"
Cohesion: 0.67
Nodes (3): VPS Deployment (Ubuntu 24.04), VPS Docker Compose Configuration, Local Docker Compose

### Community 171 - "Compose App"
Cohesion: 0.67
Nodes (3): AskBackend Production Application (VPS), Caddy Reverse Proxy (VPS), PostgreSQL Production Database (VPS)

## Knowledge Gaps
- **318 isolated node(s):** `install-ubuntu.sh script`, `vps_cert_and_service.sh script`, `vps_postgres_setup.sh script`, `vps_provision.sh script`, `kz.ask:ask-backend` (+313 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AskPrincipal` connect `Businesschatcontroller Businesschatcontroller` to `Brandexperienceprocessor Brandexperienceprocessor`, `Business Card API`, `Autodump Import Processor`, `Product Controller`, `Staffmanagementprocessor Staffmanagementprocessor`, `Identityservice Identityservice`, `Productimportprocessor Productimportprocessor`, `Businessserviceprocessor Businessserviceprocessor`, `Supplier Task Processor`, `Branch Controller`, `Autodump Import API`, `Autodumpimportservice Autodumpimportservice`, `Auth Controller`, `Invitecontroller Java`, `Auth Processor`, `Chatservice Chatservice`, `Chatfilecontroller Java`, `Customerrequestcontroller Java`, `Customer Request Processor`, `Catalogimportcontroller Java`, `Systemnotifyrequest Java`?**
  _High betweenness centrality (0.174) - this node is a cross-community bridge._
- **Why does `BaseUuidV7Entity` connect `Baseuuidv7Entity Java` to `Business Card API`, `Brandpageblocktype Brandpageblocktype`, `Autodump Audit Service`, `Brand Drop Service`, `Branch Invite Service`, `Catalog Import Service`, `Autodump Draft Service`, `Datasourcetype Datasourcetype`, `Public Search Processor`, `Raw Catalog Row Entity`, `Customerprofile Java`, `Catalog Import Entity`, `Resourceserviceassignment Java`, `Userstatus Userstatus`, `Serviceschedule Java`, `Business Branch Service`, `Record Status Enum`, `Importsessionstatus Importsessionstatus`, `Branchmemberserviceimpl Branchmemberserviceimpl`, `Product Entity`, `Customerrequeststatus Customerrequeststatus`, `Supplierresponse Java`, `Brandprofileserviceimpl Java`, `Categoryserviceimpl Categoryserviceimpl`, `City Java`, `Auth Challenge Entity`, `Business Entity`, `Aijobstatus Aijobstatus`, `Booking Java`, `Servicebranchoffer Java`, `Autodump Draft Entity`, `Requesttargetstatus Requesttargetstatus`, `Autodumprawinput Java`, `Searchsnapshot Java`, `Productoffermapper Productoffermapper`, `Auth Session Entity`, `Searchsessionstatus Searchsessionstatus`, `Serviceresource Java`, `Conversationparticipant Java`, `Business Entity`, `Conversation Java`, `Conversationmessage Java`, `Searchqueryalias Java`, `Searchresultsnapshot Java`, `Servicewindow Java`?**
  _High betweenness centrality (0.102) - this node is a cross-community bridge._
- **Why does `NotFoundException` connect `Productimportprocessor Productimportprocessor` to `Brandexperienceprocessor Brandexperienceprocessor`, `Autodump Import Processor`, `Brand Drop Service`, `Shared Error Codes`, `Product Controller`, `Staffmanagementprocessor Staffmanagementprocessor`, `Businessserviceprocessor Businessserviceprocessor`, `Supplier Task Processor`, `Catalog Import Service`, `Autodump Draft Service`, `Serviceserviceimpl Java`, `Auth Processor`, `Shared Error Codes`, `Autodumpimportserviceimpl Autodumpimportserviceimpl`, `Auth Processor`, `Globalexceptionhandler Globalexceptionhandler`, `Chatservice Chatservice`, `Authchallengedto Java`, `Categoryserviceimpl Categoryserviceimpl`, `City Java`, `Productserviceimpl Java`, `Chat Messages`, `Contactactionservice Contactactionservice`, `Chat Service`, `Customer Request Processor`, `Identityserviceimpl Java`, `Contactactionsummaryresponse Java`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **What connects `install-ubuntu.sh script`, `vps_cert_and_service.sh script`, `vps_postgres_setup.sh script` to the rest of the system?**
  _342 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Brandexperienceprocessor Brandexperienceprocessor` be split into smaller, more focused modules?**
  _Cohesion score 0.06518987341772152 - nodes in this community are weakly interconnected._
- **Should `Business Card API` be split into smaller, more focused modules?**
  _Cohesion score 0.06777493606138107 - nodes in this community are weakly interconnected._
- **Should `Brandpageblocktype Brandpageblocktype` be split into smaller, more focused modules?**
  _Cohesion score 0.07080200501253132 - nodes in this community are weakly interconnected._
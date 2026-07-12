# Task 12: Backend Restructuring V2 — Unified Catalog, Roles, Chats, Search AI

|   |   |
|---|---|
|**Описание**|Полная реструктуризация бекенда: many-to-many товары/услуги ↔ филиалы, UniqueOffers как бустеры, flat role hierarchy (5 бизнес-ролей + 3 платформенных), unified AI-powered search, упрощённые чаты (только текст), email-only auth с 2FA, удаление storefront builder/phone auth/legacy messaging.|
|**Модуль системы**|catalog, service, business, chat, search, identity, request|

## Зависимости

- Все предыдущие Task-и (00-11) заменяются этой спецификацией в части Goods, Services, UniqueOffers, Chats, Roles, Search
- Затрагивает сущности: `Product`, `ServiceOffering`, `BrandDrop` → `UniqueOffer`, `Business`, `BusinessBranch`, `BusinessMember`, `BranchMember`, `ChatConversation`, `ChatMessage`, `AppUser`, `CustomerProfile`, `BrandProfile`, `BusinessCard`, `BrandPageBlock`, `BrandExperienceController`, `Conversation`/`ConversationMessage`/`ConversationParticipant` (legacy messaging)
- Удаляемые модули: `messaging/` (весь пакет), `BrandExperienceController`, `BrandPageBlock`, `BrandPageBlockType`, `StorefrontPageStatus`, `BusinessCard`, `BusinessCardBuilder` (весь)
- Инфраструктура: DeepSeek AI (поиск), PostgreSQL (поиск + ранжирование), возможно Meilisearch в будущем

---

## Общие правила задачи

### 1. Goods & Services — Shared Entities с Many-to-Many Branch Linkage

- Товары и услуги больше не привязаны к одному бизнесу напрямую. Вместо этого:
  - `Product` и `ServiceOffering` теряют прямую связь `@ManyToOne Business`
  - Добавляется join-таблица `product_branch` (product_id, branch_id) и `service_branch` (service_id, branch_id)
  - Один товар может быть привязан к нескольким филиалам одного бизнеса
  - При обновлении товара — изменения отражаются во всех привязанных филиалах (shared entity)
  - При удалении товара из одного филиала — удаляется только связь в join-таблице
  - При полном удалении товара — удаляется сама сущность + все связи
- Бизнес остаётся владельцем товара/услуги (поле `business_id` на `Product`/`ServiceOffering` остаётся, но не используется для филиальной привязки)
- Товар можно создать без привязки к филиалу, затем shift+click назначить на несколько филиалов
- Онлайн-бизнесы могут существовать без филиалов (для них branch не обязателен)

### 2. UniqueOffers — Бустеры, НЕ Самостоятельные Сущности

- `BrandDrop` реструктурируется в `UniqueOffer`
- UniqueOffer НЕ появляется в результатах поиска как самостоятельная карточка
- UniqueOffer бустит связанные товары/услуги в поиске: если товар привязан к активному UniqueOffer, он поднимается в результатах
- UniqueOffer может быть привязан к нескольким товарам И услугам одновременно (many-to-many: `unique_offer_product`, `unique_offer_service`)
- UniqueOffer может быть привязан к нескольким филиалам (many-to-many: `unique_offer_branch`)
- Система вычисляет эффективную цену: 30% off на товар за 10000₸ → показывается 7000₸
- Типы UniqueOffer (переименованные из BrandDropType):
  ```text
  DISCOUNT      — скидка (процент или фиксированная сумма)
  NEW_COLLECTION — новая коллекция
  LIMITED_RELEASE — ограниченный релиз
  RESTOCK       — ресток
  CAPSULE       — капсула
  SEASONAL      — сезонный запуск
  COLLAB        — коллаборация
  PREORDER      — предзаказ
  ```
- Статусы:
  ```text
  UPCOMING  — анонсирован, ещё не начался
  ACTIVE    — активен сейчас
  ENDED     — завершён
  CANCELLED — отменён
  ```
- Offer можно toggle (включить/выключить) без удаления
- Cross-branch linking: один offer может быть привязан к товарам из разных филиалов

### 3. Flat Role Hierarchy

**Бизнес-роли (в порядке убывания власти):**
```text
BUSINESS_OWNER     — владелец бизнеса, может всё
BUSINESS_MANAGER   — менеджер, назначается владельцем
BUSINESS_WORKER    — работник, назначается владельцем или менеджером
```
- OWNER может изменять/удалять MANAGER и WORKER
- MANAGER может изменять/удалять WORKER но не других MANAGER и не OWNER
- Same-level не может модифицировать друг друга
- WORKER не может никого изменять

**Платформенные роли (в порядке убывания власти):**
```text
PLATFORM_SUPER_ADMIN  — супер-админ, может всё. ТОЛЬКО через БД (нет frontend-интерфейса)
PLATFORM_ADMIN        — админ платформы
PLATFORM_MODERATOR    — модератор платформы
```
- PLATFORM_SUPER_ADMIN создаётся только прямым INSERT в БД
- PLATFORM_ADMIN управляет PLATFORM_MODERATOR
- Платформенные роли не имеют доступа к бизнес-операциям (не могут управлять товарами/услугами конкретного бизнеса)
- Платформенные роли управляют: категориями, городами, пользователями, жалобами, модерацией контента

**Клиентская роль:**
```text
CUSTOMER  — обычный пользователь, ищет товары/услуги, общается в чатах
```

**Текущий `AppRole` (CUSTOMER, BUSINESS) ЗАМЕНЯЕТСЯ на:**
```text
CUSTOMER, BUSINESS_OWNER, BUSINESS_MANAGER, BUSINESS_WORKER, PLATFORM_SUPER_ADMIN, PLATFORM_ADMIN, PLATFORM_MODERATOR
```

**Текущий `BusinessMemberRole` (OWNER) РАСШИРЯЕТСЯ до:**
```text
OWNER, MANAGER, WORKER
```

**Текущий `BranchMemberRole` синхронизируется с BusinessMemberRole:**
```text
OWNER, MANAGER, WORKER
```

### 4. Unified AI-Powered Search

- Один search endpoint возвращает товары И услуги в одном ответе
- Больше нет переключателя PRODUCT/SERVICE/AUTO — AI сам понимает, что искать
- AI (DeepSeek) получает сырой пользовательский запрос (любой язык, сленг, опечатки, эссе) и структурирует его в JSON
- Система запрашивает все три каталога (goods, services, unique_offers) и объединяет результаты
- UniqueOffers бустят связанные товары/услуги в результатах поиска
- Скоринг учитывает: текстовое совпадение, категорию, цену, город/расстояние, активные UniqueOffers
- **Принцип надёжности поиска**: одинаковый запрос = одинаковые результаты. Уточнённый запрос (добавлена спецификация) = подмножество результатов.
- История поиска НЕ хранится (результаты живут до следующего запроса)
- Поиск полностью PostgreSQL-based (Meilisearch рекомендуется на будущее для fuzzy full-text, но не обязателен для MVP)

### 5. Simplified Chats

- Только текст + emoji. Никаких файлов, вложений, картинок.
- Никаких статусов у чата (удаляются `ConversationStatus`, `NEW_REQUEST`, `IN_PROGRESS`, `CLOSED`)
- Только customer может инициировать чат
- Unread count отслеживается per-conversation независимо для каждой стороны:
  - `customer_unread_count` — сколько непрочитанных сообщений у customer
  - `business_unread_count` — сколько непрочитанных сообщений у business
- Инкрементируются при отправке сообщения противоположной стороной
- Сбрасываются при отметке прочитанным (markRead)
- Никаких request_target_id (связь чата с заявками удаляется)
- Удаляется поле `attachmentUrl`
- Удаляется поле `searchQuery` (чат больше не инициирует поиск)
- Удаляется поле `source` (CUSTOMER/SYSTEM — механика system-чатов удаляется)

### 6. Shipping / Availability Model

- На уровне бизнеса задаётся список городов/районов доставки
- Режим: "all except X" (доставляем везде кроме указанных) ИЛИ "only X" (доставляем только в указанные)
- Для онлайн-бизнесов доставка может быть не задана
- MVP: один режим на бизнес (позже можно расширить до per-branch)

### 7. Multi-Currency Architecture

- MVP: только KZT (тенге)
- Валюта хранится на `Business` (`currency` поле, default KZT)
- Методы калькуляции готовы для расширения на другие валюты
- Цены на товары/услуги хранятся в базовой валюте бизнеса

### 8. Email-Only Auth + 2FA

- Удаляется ВЕСЬ код phone-авторизации:
  - Поле `phone` в `AppUser`
  - Поле `phone` в `AuthChallenge`
  - `AuthChallengeChannel.PHONE`
  - Все SMS-адаптеры и интеграции
- Email — единственный канал аутентификации
- Один объединённый verification code для всех целей (login + registration)
- Верификация через email code (6 цифр)
- 2FA через тот же email (опционально, настраивается пользователем)

### 9. Что Удаляется Полностью

| Удаляемое | Причина |
|---|---|
| `BrandExperienceController` (12 endpoints) | Storefront builder заменён на простой brand profile |
| `BrandPageBlock` entity + таблица | Больше нет кастомных страниц магазина |
| `BrandPageBlockType` enum | Связан с BrandPageBlock |
| `StorefrontPageStatus` enum | Связан с BrandPageBlock |
| `BusinessCard` entity + таблица | Canva-like builder удалён |
| `BusinessCardBuilder` widget (frontend) | Уже удалён в плане |
| `Conversation`, `ConversationMessage`, `ConversationParticipant` | Старый messaging модуль |
| `messaging/` пакет целиком | Заменён на упрощённый chat/ |
| Phone auth: `AppUser.phone`, `AuthChallenge.phone`, `AuthChallengeChannel.PHONE` | Только email auth |
| `BusinessContact` — если не используется | Проверить использование |
| `DataSource` + `DataSourceType` — если autodump может работать без них | Проверить |

### 10. Что Сохраняется

- `BrandProfile` entity (данные бренда: цвета, лого, описание, соцсети)
- Autodump модуль целиком
- `CustomerProfile` (обновляется: +iconUrl, -phone)
- Contact privacy: HMAC-SHA256 dedup + AES-256-GCM encryption с contactActionId tokens
- `SearchSession` / `SearchSnapshot` механика
- `BranchInvite` система (приглашения по коду)

### 11. Customer Profile

- Добавляется `icon_url` для аватарки (загрузка файла, то же хранилище что и для чатов)
- Удаляется поле `phone`
- Профиль привязан к `AppUser` (1:1)

---

## Endpoints

### 1. Unified Search — `POST /api/v1/search`

|   |   |
|---|---|
|**Описание**|Единый поиск по товарам, услугам и UniqueOffer-ам. AI структурирует сырой запрос пользователя, система запрашивает все три каталога и возвращает объединённый ранжированный результат.|
|**Доступ только авторизованным пользователям**|-|
|**Endpoint URL**|`/api/v1/search`|
|**Метод запроса**|POST|
|**Content-Type**|application/json|

#### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|Сырой пользовательский запрос|`query`|string body|+|—|Любой язык, сленг, опечатки|
|2|Город поиска|`cityId`|uuid body|-|null|Если не указан — поиск по всем городам|
|3|Максимум результатов|`limit`|integer body|-|20|1-100|

#### SearchResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`results`|[SearchResultItem]|Отсортированный список результатов|
|2|`aiStructuredQuery`|AiStructuredQuery|Как AI понял запрос (для отладки)|
|3|`totalFound`|integer|Общее количество найденного|

#### SearchResultItem

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`type`|enum|`GOODS`, `SERVICE`|
|2|`id`|UUID|ID товара или услуги|
|3|`name`|String|Название|
|4|`description`|String|Описание|
|5|`effectivePrice`|Decimal|nullable — цена с учётом активных UniqueOffers|
|6|`originalPrice`|Decimal|nullable — исходная цена|
|7|`imageUrl`|String||
|8|`categoryName`|String|Название категории|
|9|`businessName`|String|Название бизнеса|
|10|`branchIds`|[UUID]|ID филиалов, где доступен товар/услуга|
|11|`activeOfferId`|UUID|nullable — ID активного UniqueOffer если есть|
|12|`offerLabel`|String|nullable — текст оффера ("-30%", "New Collection")|
|13|`score`|Decimal|Итоговый скоринговый балл|

#### AiStructuredQuery

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`intent`|String|Что ищет пользователь (goods/service/both)|
|2|`searchTerms`|[String]|Ключевые слова из запроса|
|3|`categoryHints`|[String]|Предполагаемые категории|
|4|`priceRange`|PriceRange|nullable|
|5|`brands`|[String]|Упомянутые бренды|
|6|`originalQuery`|String|Исходный запрос|

#### Правила

- Один вызов DeepSeek AI для структурирования запроса (prompt: "преврати сырой поисковый запрос в структурированный JSON")
- PostgreSQL запрос по товарам + услугам одновременно (UNION или раздельные запросы с merge)
- UniqueOffers проверяются отдельно: для каждого товара/услуги ищется активный (status=ACTIVE, startDate <= now, endDate >= now или null) UniqueOffer, привязанный к этому товару/услуге
- Если UniqueOffer типа DISCOUNT — вычисляется `effectivePrice`
- Скоринг в памяти (in-memory) — текущий подход сохраняется
- Результаты кешируются в рамках одной search session
- Одинаковый запрос = одинаковые результаты (детерминированность)

---

### 2. UniqueOffer CRUD — Business Admin

Базовый путь: `/api/v1/businesses/{businessId}/offers`

#### `GET /api/v1/businesses/{businessId}/offers`

|   |   |
|---|---|
|**Описание**|Список всех UniqueOffer бизнеса|
|**Доступ**|BUSINESS_OWNER, BUSINESS_MANAGER|

#### UniqueOfferResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`offerId`|UUID||
|2|`name`|String||
|3|`description`|String||
|4|`type`|enum|UniqueOfferType|
|5|`status`|enum|UniqueOfferStatus|
|6|`discountPercent`|Integer|nullable — процент скидки (только для DISCOUNT)|
|7|`discountAmount`|Decimal|nullable — фиксированная сумма скидки (только для DISCOUNT)|
|8|`startDate`|Instant||
|9|`endDate`|Instant|nullable|
|10|`coverUrl`|String||
|11|`productIds`|[UUID]|Связанные товары|
|12|`serviceIds`|[UUID]|Связанные услуги|
|13|`branchIds`|[UUID]|Филиалы, где действует|
|14|`tags`|[String]||
|15|`enabled`|Boolean|Включён/выключен|

#### `POST /api/v1/businesses/{businessId}/offers`

Создание UniqueOffer. Параметры: name (обязательно), type (обязательно), discountPercent/discountAmount, startDate, endDate, coverUrl, productIds, serviceIds, branchIds, tags.

#### `PATCH /api/v1/businesses/{businessId}/offers/{offerId}`

Обновление UniqueOffer. Те же поля что и при создании, все опциональные.

#### `DELETE /api/v1/businesses/{businessId}/offers/{offerId}`

Удаление UniqueOffer и всех связей (product_offer, service_offer, branch_offer).

#### `POST /api/v1/businesses/{businessId}/offers/{offerId}/toggle`

Переключение enabled (вкл/выкл). Не удаляет offer, только меняет флаг.

---

### 3. Goods (Product) CRUD — Many-to-Many Branches

Базовый путь: `/api/v1/businesses/{businessId}/goods`

#### `GET /api/v1/businesses/{businessId}/goods`

Список товаров бизнеса. Дополнительно: для каждого товара возвращается список branchIds где он доступен.

#### `POST /api/v1/businesses/{businessId}/goods`

Создание товара. Параметры: name, description, categoryId, price, imageUrl, tags, branchIds (опционально — можно создать без привязки).

#### `PATCH /api/v1/businesses/{businessId}/goods/{productId}`

Обновление товара. Изменения отражаются во всех привязанных филиалах.

#### `DELETE /api/v1/businesses/{businessId}/goods/{productId}`

Полное удаление товара и всех связей с филиалами.

#### `POST /api/v1/businesses/{businessId}/goods/{productId}/branches`

Тело: `{ "branchIds": ["uuid1", "uuid2"] }` — привязать товар к филиалам.

#### `DELETE /api/v1/businesses/{businessId}/goods/{productId}/branches/{branchId}`

Отвязать товар от одного филиала (не удаляя сам товар).

---

### 4. Services CRUD — Many-to-Many Branches (аналогично Goods)

Базовый путь: `/api/v1/businesses/{businessId}/services`

Эндпоинты аналогичны Goods.

---

### 5. Staff / Members Management

Базовый путь: `/api/v1/businesses/{businessId}/staff`

#### `GET /api/v1/businesses/{businessId}/staff`

Список всех members бизнеса. Доступ: OWNER, MANAGER.

#### `POST /api/v1/businesses/{businessId}/staff`

Приглашение нового staff. Параметры: email, role (MANAGER или WORKER). OWNER не может быть создан через API (только первый создатель бизнеса). MANAGER не может создавать других MANAGER.

#### `PATCH /api/v1/businesses/{businessId}/staff/{memberId}`

Изменение роли. Правила: OWNER может изменить любого. MANAGER может изменить только WORKER.

#### `DELETE /api/v1/businesses/{businessId}/staff/{memberId}`

Удаление staff. Правила те же что и для PATCH.

---

### 6. Simplified Chats

Базовый путь: `/api/v1/chats`

#### `POST /api/v1/chats`

Создание чата customer-ом. Параметры: businessId (обязательно), initialMessage (текст первого сообщения).

#### `GET /api/v1/chats`

Список чатов текущего пользователя. Для customer — его чаты с бизнесами. Для business owner/manager/worker — чаты их бизнеса.

#### ChatConversationResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`conversationId`|UUID||
|2|`businessId`|UUID||
|3|`customerId`|UUID||
|4|`customerName`|String||
|5|`businessName`|String||
|6|`lastMessage`|ChatMessageResponse|nullable|
|7|`lastMessageAt`|Instant||
|8|`unreadCount`|Integer|Unread для текущего пользователя|
|9|`createdAt`|Instant||

#### `GET /api/v1/chats/{conversationId}/messages`

Сообщения чата (пагинация).

#### ChatMessageResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`messageId`|UUID||
|2|`conversationId`|UUID||
|3|`senderId`|UUID||
|4|`text`|String|Только текст|
|5|`createdAt`|Instant||

#### `POST /api/v1/chats/{conversationId}/messages`

Отправка сообщения. Тело: `{ "text": "..." }`.

#### `POST /api/v1/chats/{conversationId}/read`

Отметить чат прочитанным. Сбрасывает unread count для текущего пользователя.

#### Правила

- Только CUSTOMER может создать чат
- BUSINESS_OWNER, BUSINESS_MANAGER, BUSINESS_WORKER могут отвечать в чатах своего бизнеса
- Никаких статусов чата (NEW_REQUEST, IN_PROGRESS и т.д. удаляются)
- Никаких вложений (attachmentUrl удаляется)
- Никакой связи с request_target
- Unread count обновляется атомарно (в транзакции)

---

### 7. Shipping Settings

#### `GET /api/v1/businesses/{businessId}/shipping`

Получить настройки доставки бизнеса.

#### ShippingResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`mode`|enum|`ALL_EXCEPT`, `ONLY`|
|2|`cityIds`|[UUID]|Список городов (в зависимости от mode — включённые или исключённые)|

#### `PUT /api/v1/businesses/{businessId}/shipping`

Обновить настройки доставки. Тело: `{ "mode": "ALL_EXCEPT", "cityIds": ["uuid1"] }`.

---

### 8. Auth Changes

#### `POST /api/v1/auth/challenge`

Запрос кода верификации. Только email. Тело: `{ "email": "user@example.com", "purpose": "LOGIN" }`.

#### `POST /api/v1/auth/verify`

Верификация кода + получение сессии. Без изменений в контракте.

#### Удаляемые endpoints

- Все phone-related поля из DTO
- `AuthChallengeChannel.PHONE` значение (enum остаётся но только EMAIL)

---

### 9. Customer Profile

#### `GET /api/v1/profile`

Получить профиль текущего пользователя.

#### CustomerProfileResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`userId`|UUID||
|2|`displayName`|String||
|3|`email`|String||
|4|`iconUrl`|String|nullable|

#### `PATCH /api/v1/profile`

Обновить профиль. Поля: displayName, iconUrl.

#### `POST /api/v1/profile/icon`

Загрузка иконки (multipart/form-data, file). Возвращает URL.

---

## Миграции БД (V8+)

### V8: Переименование и реструктуризация

```sql
-- Roles: расширяем AppRole и BusinessMemberRole
-- (enum в Java, в БД это VARCHAR — новые значения допустимы без ALTER)

-- Product: добавляем business_id если его нет (сейчас он есть), добавляем price
ALTER TABLE product ADD COLUMN IF NOT EXISTS price NUMERIC;

-- Product-Branch many-to-many
CREATE TABLE product_branch (
    product_id UUID NOT NULL REFERENCES product(id),
    branch_id  UUID NOT NULL REFERENCES business_branch(id),
    PRIMARY KEY (product_id, branch_id)
);

-- Service-Branch many-to-many
CREATE TABLE service_branch (
    service_id UUID NOT NULL REFERENCES service_offering(id),
    branch_id  UUID NOT NULL REFERENCES business_branch(id),
    PRIMARY KEY (service_id, branch_id)
);

-- UniqueOffer (из BrandDrop)
ALTER TABLE brand_drop RENAME TO unique_offer;
ALTER TABLE unique_offer ADD COLUMN IF NOT EXISTS discount_percent INTEGER;
ALTER TABLE unique_offer ADD COLUMN IF NOT EXISTS discount_amount NUMERIC;
ALTER TABLE unique_offer ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE unique_offer ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'KZT';

-- UniqueOffer-Product many-to-many
CREATE TABLE unique_offer_product (
    offer_id   UUID NOT NULL REFERENCES unique_offer(id),
    product_id UUID NOT NULL REFERENCES product(id),
    PRIMARY KEY (offer_id, product_id)
);

-- UniqueOffer-Service many-to-many
CREATE TABLE unique_offer_service (
    offer_id  UUID NOT NULL REFERENCES unique_offer(id),
    service_id UUID NOT NULL REFERENCES service_offering(id),
    PRIMARY KEY (offer_id, service_id)
);

-- UniqueOffer-Branch many-to-many
CREATE TABLE unique_offer_branch (
    offer_id  UUID NOT NULL REFERENCES unique_offer(id),
    branch_id UUID NOT NULL REFERENCES business_branch(id),
    PRIMARY KEY (offer_id, branch_id)
);

-- Business: добавляем currency и shipping
ALTER TABLE business ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'KZT';
ALTER TABLE business ADD COLUMN IF NOT EXISTS shipping_mode VARCHAR(20);
ALTER TABLE business ADD COLUMN IF NOT EXISTS shipping_city_ids JSONB;

-- Chat: удаляем лишние поля
ALTER TABLE chat_conversation DROP COLUMN IF EXISTS status;
ALTER TABLE chat_conversation DROP COLUMN IF EXISTS source;
ALTER TABLE chat_conversation DROP COLUMN IF EXISTS search_query;
ALTER TABLE chat_conversation DROP COLUMN IF EXISTS request_target_id;
ALTER TABLE chat_message DROP COLUMN IF EXISTS attachment_url;

-- Chat: unread counts
ALTER TABLE chat_conversation ADD COLUMN IF NOT EXISTS customer_unread_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE chat_conversation ADD COLUMN IF NOT EXISTS business_unread_count INTEGER NOT NULL DEFAULT 0;

-- CustomerProfile: убираем phone, добавляем icon
ALTER TABLE customer_profile DROP COLUMN IF EXISTS phone;
ALTER TABLE customer_profile ADD COLUMN IF NOT EXISTS icon_url VARCHAR(2048);

-- AppUser: убираем phone
ALTER TABLE app_user DROP COLUMN IF EXISTS phone;

-- AuthChallenge: убираем phone
ALTER TABLE auth_challenge DROP COLUMN IF EXISTS phone;

-- Удаление старых таблиц
DROP TABLE IF EXISTS brand_page_block CASCADE;
DROP TABLE IF EXISTS business_card CASCADE;
DROP TABLE IF EXISTS conversation_message CASCADE;
DROP TABLE IF EXISTS conversation_participant CASCADE;
DROP TABLE IF EXISTS conversation CASCADE;
DROP TABLE IF EXISTS brand_drop_product CASCADE;
DROP TABLE IF EXISTS brand_drop_tag CASCADE;
-- brand_drop уже переименована в unique_offer
```

---

## Примеры

### Unified Search

```http
POST /api/v1/search
Content-Type: application/json

{
  "query": "чёрные кроссовки adidas недорого",
  "cityId": "city-uuid-almaty",
  "limit": 20
}
```

```json
{
  "results": [
    {
      "type": "GOODS",
      "id": "prod-uuid-001",
      "name": "Adidas Ultraboost Black",
      "description": "Чёрные кроссовки Adidas Ultraboost...",
      "effectivePrice": 35000,
      "originalPrice": 50000,
      "imageUrl": "https://...",
      "categoryName": "Обувь",
      "businessName": "SportMaster",
      "branchIds": ["branch-uuid-001", "branch-uuid-002"],
      "activeOfferId": "offer-uuid-001",
      "offerLabel": "-30%",
      "score": 92.5
    },
    {
      "type": "SERVICE",
      "id": "svc-uuid-001",
      "name": "Чистка кроссовок",
      "description": "Профессиональная чистка спортивной обуви",
      "effectivePrice": null,
      "originalPrice": 5000,
      "imageUrl": null,
      "categoryName": "Услуги",
      "businessName": "CleanPro",
      "branchIds": ["branch-uuid-003"],
      "activeOfferId": null,
      "offerLabel": null,
      "score": 68.0
    }
  ],
  "aiStructuredQuery": {
    "intent": "goods",
    "searchTerms": ["кроссовки", "чёрные", "adidas", "недорого"],
    "categoryHints": ["Обувь", "Спорт"],
    "priceRange": { "max": 40000 },
    "brands": ["Adidas"],
    "originalQuery": "чёрные кроссовки adidas недорого"
  },
  "totalFound": 12
}
```

### Create UniqueOffer

```http
POST /api/v1/businesses/biz-uuid-001/offers
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Летняя распродажа",
  "type": "DISCOUNT",
  "discountPercent": 30,
  "startDate": "2026-07-15T00:00:00Z",
  "endDate": "2026-08-15T00:00:00Z",
  "productIds": ["prod-uuid-001", "prod-uuid-002"],
  "serviceIds": [],
  "branchIds": ["branch-uuid-001"],
  "tags": ["лето", "распродажа"]
}
```

### Create Chat

```http
POST /api/v1/chats
Authorization: Bearer <token>
Content-Type: application/json

{
  "businessId": "biz-uuid-001",
  "initialMessage": "Здравствуйте, есть ли в наличии размер 42?"
}
```

---

## План реализации (порядок)

### Phase 1: Foundation — Deletions + Schema
1. Удалить `messaging/` пакет целиком
2. Удалить `BrandExperienceController` и все связанные endpoints
3. Удалить `BrandPageBlock` entity + таблицу
4. Удалить `BusinessCard` entity + таблицу
5. Удалить `BusinessCardBuilder` widget (фронтенд — уже в плане)
6. Удалить phone auth из `AppUser`, `AuthChallenge`, `AuthChallengeChannel`
7. Создать V8 миграцию (все ALTER, CREATE, DROP таблицы)
8. Обновить `AppRole` enum (расширить)
9. Обновить `BusinessMemberRole` enum (расширить)
10. Обновить `BranchMemberRole` enum (синхронизировать)

### Phase 2: Core Entities
11. Реструктурировать `Product` (добавить price, связь с ветками)
12. Реструктурировать `ServiceOffering` (связь с ветками)
13. Реструктурировать `BrandDrop` → `UniqueOffer` (добавить поля, связи M2M)
14. Обновить `Business` (currency, shipping)
15. Обновить `CustomerProfile` (-phone, +iconUrl)

### Phase 3: Chat Restructuring
16. Упростить `ChatConversation` (удалить статусы, source, searchQuery, requestTargetId)
17. Добавить unread count поля
18. Упростить `ChatMessage` (удалить attachmentUrl)
19. Переписать `ChatServiceImpl` без статусов, без bridge, с unread count

### Phase 4: Search AI
20. Создать `UnifiedSearchController` + `UnifiedSearchProcessor`
21. Интегрировать DeepSeek AI для структурирования запроса
22. Реализовать объединённый поиск по goods + services
23. Интегрировать UniqueOffer бустинг в скоринг
24. Обновить frontend DTO

### Phase 5: Roles & Permissions
25. Реализовать permission checking в процессорах
26. Обновить `AuthProcessor` для новых ролей
27. Обновить `SecurityConfig` для platform-ролей

### Phase 6: Shipping & Profile
28. Реализовать shipping settings CRUD
29. Реализовать customer profile update + icon upload
30. Финальное тестирование и верификация

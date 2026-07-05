# Task 11: Drops / Events Search Indexing

|   |   |
|---|---|
|**Описание**|Дропы и события бренда как индексируемые search-сигналы. Дроп попадает в Meilisearch и появляется в результатах поиска.|
|**Модуль системы**|business, search|

## Зависимости
- Task 03 (business admin product endpoints), Task 07 (Meilisearch), Task 10 (storefront builder)
- Сущности: `Drop`, `Business`, `SearchDocument`
- Инфраструктура: Meilisearch

## Общие правила задачи

- Дроп = событие бренда, НЕ скидка. Типы: NEW_COLLECTION, LIMITED_RELEASE, RESTOCK, CAPSULE, SEASONAL, COLLAB, PREORDER.
- Дропы индексируются в Meilisearch как отдельные search documents (type=`DROP`).
- Если пользователь ищет то, что покрывается активным дропом, результат показывает DropCard.
- DropCard появляется в секции `fresh_drops` результатов поиска.
- Бизнес управляет дропами через business cabinet.
- Активные дропы влияют на ranking: `hasActiveDrop` = true → boost в intent_match.

---

## Drop Entity

| Поле | Тип | Назначение |
|---|---|---|
|`id`|UUIDv7|Первичный ключ|
|`businessId`|UUID|Связь с бизнесом|
|`name`|String|Название дропа|
|`description`|String|Описание|
|`type`|enum|Тип дропа|
|`status`|enum|Статус|
|`startDate`|LocalDateTime|Начало|
|`endDate`|LocalDateTime|Окончание (nullable)|
|`coverUrl`|String|Обложка|
|`productIds`|[UUID]|Привязанные товары (JSON)|
|`tags`|[String]|Теги для поиска|
|`createdAt`|Timestamp||
|`updatedAt`|Timestamp||

### DropType Enum

```text
NEW_COLLECTION    — новая коллекция
LIMITED_RELEASE   — ограниченный релиз
RESTOCK           — ресток (пополнение)
CAPSULE           — капсула
SEASONAL          — сезонный запуск
COLLAB            — коллаборация
PREORDER          — предзаказ
```

### DropStatus Enum

```text
UPCOMING  — анонсирован, ещё не начался
ACTIVE    — активен сейчас
ENDED     — завершён
CANCELLED — отменён
```

---

## Endpoints

### `GET /api/v1/businesses/{businessId}/drops`

|   |   |
|---|---|
|**Описание**|Публичный список активных и upcoming дропов бизнеса|
|**Доступ только авторизованным пользователям**|-|
|**Endpoint URL**|`/api/v1/businesses/{businessId}/drops`|
|**Метод запроса**|GET|

### DropResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`dropId`|UUID||
|2|`businessId`|UUID||
|3|`businessName`|String||
|4|`brandColor`|String||
|5|`name`|String||
|6|`description`|String||
|7|`type`|String|DropType|
|8|`status`|String|DropStatus|
|9|`startDate`|Timestamp||
|10|`endDate`|Timestamp|nullable|
|11|`coverUrl`|String||
|12|`productCount`|Integer|Сколько товаров привязано|
|13|`tags`|[String]||

### `POST /api/v1/businesses/{businessId}/drops`

|   |   |
|---|---|
|**Описание**|Создать новый дроп|
|**Доступ только авторизованным пользователям**|+ (Owner)|
|**Endpoint URL**|`/api/v1/businesses/{businessId}/drops`|
|**Метод запроса**|POST|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | Комментарий |
|---|---|---|---|---|---|
|1|Название|`name`|string body|+||
|2|Описание|`description`|string body|-||
|3|Тип|`type`|string body|+|DropType|
|4|Дата начала|`startDate`|datetime body|+||
|5|Дата окончания|`endDate`|datetime body|-|nullable|
|6|Обложка|`coverUrl`|string body|-||
|7|ID товаров|`productIds`|[uuid] body|-|Привязанные товары|
|8|Теги|`tags`|[string] body|-|Для поиска|

### `PUT /api/v1/businesses/{businessId}/drops/{dropId}`

|   |   |
|---|---|
|**Описание**|Обновить дроп|
|**Доступ только авторизованным пользователям**|+ (Owner)|

### `DELETE /api/v1/businesses/{businessId}/drops/{dropId}`

|   |   |
|---|---|
|**Описание**|Удалить дроп|
|**Доступ только авторизованным пользователям**|+ (Owner)|

### `POST /api/v1/businesses/{businessId}/drops/{dropId}/cancel`

|   |   |
|---|---|
|**Описание**|Отменить дроп (статус → CANCELLED)|
|**Доступ только авторизованным пользователям**|+ (Owner)|

---

## Search Indexing

### Drop → SearchDocument → Meilisearch

При создании/обновлении дропа:

1. Создаётся/обновляется `SearchDocument` с `type = DROP`.
2. SearchDocument поля:
   - `title` = drop.name
   - `summary` = drop.description
   - `tags` = drop.tags + бренд + категории привязанных товаров
   - `business_id` = drop.businessId
   - `type` = `DROP`
3. Meilisearch документ обновляется через `SearchDocumentSyncService`.

### Drop в поисковой выдаче

Когда Search Orchestrator выполняет поиск:
1. Meilisearch возвращает документы всех типов (PRODUCT, SERVICE, DROP).
2. DROP документы маппятся в DropCard.
3. DropCard попадают в секцию `fresh_drops`.
4. Если у бренда есть активный дроп, его продуктовые карточки получают `hasActiveDrop: true` и бейдж "Новый дроп".

### Ranking Boost

Бренды с активными дропами получают boost в intent_match scoring:
- `hasActiveDrop = true` → +10% к freshnessScore.
- Если search query содержит слова "новый", "свежий", "дроп", "коллекция", "релиз" → дропы поднимаются выше.

---

## Endpoints Summary

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
|`GET`|`/api/v1/businesses/{businessId}/drops`|No|Публичный список дропов|
|`POST`|`/api/v1/businesses/{businessId}/drops`|Owner|Создать дроп|
|`PUT`|`/api/v1/businesses/{businessId}/drops/{dropId}`|Owner|Обновить дроп|
|`DELETE`|`/api/v1/businesses/{businessId}/drops/{dropId}`|Owner|Удалить дроп|
|`POST`|`/api/v1/businesses/{businessId}/drops/{dropId}/cancel`|Owner|Отменить дроп|

---

## Правила

- Только Owner управляет дропами.
- UPCOMING и ACTIVE дропы индексируются в Meilisearch.
- ENDED и CANCELLED удаляются из Meilisearch.
- Дроп не обязан иметь привязанные товары — может быть анонсом.
- При привязке товаров к дропу — товары получают связь с дропом в search document.
- Один товар может быть в нескольких дропах (например, в коллекции и в коллаборации).
- Отмена дропа не удаляет товары — только убирает связь и поисковый сигнал.

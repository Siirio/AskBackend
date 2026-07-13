# Task 07: Meilisearch Search Engine Integration

|   |   |
|---|---|
|**Описание**|Интеграция Meilisearch как fast search projection поверх PostgreSQL. PostgreSQL остаётся source of truth, Meilisearch — денормализованный поисковый индекс.|
|**Модуль системы**|search, catalog, service, business|

## Зависимости
- Task 00 (search sessions/snapshots), 01 (client product endpoints), 02 (client service endpoints)
- Сущности: `SearchDocument`, `Product`, `ProductOffer`, `Service`, `ServiceBranchOffer`, `Business`, `BusinessBranch`, `Drop`
- Инфраструктура: Meilisearch (локально или cloud), `meilisearch-java` client

## Общие правила задачи

- PostgreSQL = source of truth. Meilisearch = rebuildable search projection.
- Каждое создание/обновление/удаление ProductOffer, ServiceBranchOffer, Drop триггерит синхронизацию в Meilisearch.
- Индекс можно полностью пересобрать из PostgreSQL в любой момент (`rebuild-index` job).
- Outbox pattern или direct sync: на MVP допустима прямая синхронизация через `SearchDocumentSyncService`.
- Meilisearch не хранит business rules, approvals, permissions — только searchable/filterable/sortable атрибуты.
- Поисковые запросы идут через Meilisearch → hydration из PostgreSQL.

---

## Search Document Index Schema

### Meilisearch Index: `ask_products_services`

| Поле | Тип Meili | Источник PostgreSQL | Назначение |
|---|---|---|---|
|`id`|string|`search_document.id`|Первичный ключ|
|`type`|string|`PRODUCT` / `SERVICE` / `DROP`|Тип документа|
|`businessId`|string|`search_document.business_id`|ID бизнеса|
|`branchId`|string|`search_document.branch_id`|ID филиала|
|`title`|string|`search_document.title`|Поисковый заголовок|
|`summary`|string|`search_document.summary`|Поисковое описание|
|`tags`|[string]|`search_document.tags`|Теги для поиска|
|`categoryLabel`|string|`search_document.category_label`|Название категории|
|`price`|number|`search_document.price`|Цена (для сортировки/фильтрации)|
|`sku`|string|`search_document.sku`|Артикул|
|`brandName`|string|`business.name`|Название бренда|
|`brandColor`|string|`brand_profile.brand_color`|Цвет бренда|
|`brandLogoUrl`|string|`brand_profile.logo_url`|Лого бренда|
|`cityId`|string|`branch.city_id`|ID города|
|`_geo`|object|`branch.lat, branch.lng`|Координаты для geo-search|
|`freshnessScore`|number|computed|Оценка свежести данных|
|`activityLevel`|number|computed|Уровень активности бизнеса|
|`hasActiveDrop`|boolean|computed|Есть активный дроп|
|`enabled`|boolean|`product_offer.enabled` / `service_branch_offer.active`|Активен для поиска|
|`attributes`|object|`search_document.characteristics_json`|Кастомные атрибуты для faceting|

### Meilisearch Settings

```json
{
  "filterableAttributes": ["type", "businessId", "branchId", "categoryLabel", "cityId", "enabled", "hasActiveDrop", "price"],
  "sortableAttributes": ["price", "freshnessScore", "activityLevel", "_geo"],
  "searchableAttributes": ["title", "summary", "tags", "brandName", "categoryLabel", "attributes"],
  "typoTolerance": { "enabled": true, "minWordSizeForTypos": { "oneTypo": 4, "twoTypos": 8 } },
  "faceting": { "maxValuesPerFacet": 100 }
}
```

---

## Sync Jobs

### On Product/Service Create/Update/Delete

`SearchDocumentSyncService.syncToMeilisearch(searchDocument)`:
1. Строит Meilisearch document из `SearchDocument` + `Business` + `BrandProfile` + `BusinessBranch`.
2. Вычисляет computed поля: `freshnessScore`, `activityLevel`, `hasActiveDrop`.
3. Вызывает `meilisearchClient.index("ask_products_services").addDocuments([doc])`.
4. На delete: `meilisearchClient.index("ask_products_services").deleteDocument(id)`.

### Full Rebuild

`POST /api/v1/admin/search/rebuild-index` (admin-only):
1. Удаляет все документы из индекса.
2. Читает все активные `SearchDocument` из PostgreSQL.
3. Батчами по 1000 добавляет в Meilisearch.
4. Возвращает количество проиндексированных документов.

### Outbox (future)

Для production: async sync через outbox table + scheduled job. На MVP — прямая синхронизация.

---

## Search Orchestrator

### `POST /api/v1/search/v2` (replaces/extends current search)

|   |   |
|---|---|
|**Описание**|Structured search через Meilisearch + PostgreSQL hydration|
|**Доступ только авторизованным пользователям**|-|
|**Endpoint URL**|`/api/v1/search/v2`|
|**Метод запроса**|POST|
|**Content-Type**|application/json|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|Сырой запрос|`rawQuery`|string body|+|—|Текст как ввёл пользователь|
|2|Scope поиска|`scope`|string body|+|—|`PRODUCT` или `SERVICE`|
|3|ID города|`cityId`|uuid body|-|null|Фильтр по городу|
|4|ID категории|`categoryId`|uuid body|-|null|Фильтр по категории|
|5|Мин. цена|`priceMin`|decimal body|-|null||
|6|Макс. цена|`priceMax`|decimal body|-|null||
|7|Сортировка|`sort`|string body|-|`intent_match`|См. допустимые значения|
|8|Широта|`lat`|decimal body|-|null|Для geo-search|
|9|Долгота|`lng`|decimal body|-|null|Для geo-search|
|10|Радиус (метры)|`radiusM`|integer body|-|null|Для geo-search|

### Flow

1. Получить raw query + scope.
2. Вызвать AI intent structurer → `SearchPlan` JSON.
3. Валидировать `SearchPlan`.
4. Сформировать Meilisearch query: `mustHave` → AND filter, `softSignals` → search query, `categoryHints` → filter, `locationIntent` → geo-filter + sort по `_geo`.
5. Выполнить поиск в Meilisearch.
6. Получить IDs результатов → hydrate из PostgreSQL (Product, ProductOffer, Business, Branch, BrandProfile).
7. Применить hard gates: enabled=true, город (если указан), бюджет (если цена > max budget → отдельная секция).
8. Построить `MatchReason` для каждого результата.
9. Сгруппировать в sections: exact, similar, drops, storefronts, over-budget, needs-confirmation.
10. Создать `SearchSession` + `SearchSnapshot`.
11. Запустить auto supplier check для suitable branches.
12. Вернуть SearchResponse с sections.

### SearchResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`searchSessionId`|UUID|ID поисковой сессии|
|2|`rawQuery`|String|Оригинальный запрос|
|3|`scope`|String|`PRODUCT` / `SERVICE`|
|4|`understoodQuery`|String|Как AI понял запрос|
|5|`sections`|[SearchSection]|Секции результатов|
|6|`supplierCheckCount`|Integer|Сколько магазинов получили auto supplier check|

### SearchSection

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`type`|String|`exact_products`, `similar_products`, `fresh_drops`, `suitable_storefronts`, `over_budget`, `needs_confirmation`|
|2|`title`|String|Заголовок секции (русский)|
|3|`cards`|[SearchResultCard]|Карточки результатов|

### SearchResultCard

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`component`|String|`ProductCard`, `ServiceCard`, `DropCard`, `BusinessCandidateCard`|
|2|`resultId`|UUID|ID результата|
|3|`businessId`|UUID|ID бизнеса|
|4|`businessName`|String|Название бизнеса|
|5|`brandColor`|String|Цвет бренда|
|6|`brandLogoUrl`|String|URL логотипа|
|7|`title`|String|Заголовок карточки|
|8|`price`|Decimal|Цена|
|9|`availability`|String|`IN_STOCK` / `NEEDS_CONFIRMATION` / `UNKNOWN`|
|10|`matchReasons`|[String]|Причины релевантности|
|11|`badges`|[String]|Бейджи качества|
|12|`distanceMeters`|Integer|Расстояние (nullable)|
|13|`branchName`|String|Название филиала|
|14|`hasActiveDrop`|Boolean|Есть активный дроп|

### Правила

- Default sort = `intent_match` (Meilisearch default relevance + backend re-ranking). Никогда `price_asc`.
- `price_asc` и `price_desc` доступны как пользовательский выбор, но не по умолчанию.
- Geo-search: если указаны lat/lng, сортировка учитывает `_geo` и радиус.
- MatchReasons генерируются `MatchReasonBuilder` на основе SearchPlan и атрибутов результата.
- Max 4 match reasons на карточку.
- Supplier check запускается автоматически после поиска, не требует отдельного действия пользователя.

### Допустимые значения sort

```text
intent_match, price_asc, price_desc, distance, freshness, activity
```

---

## Пример: Search V2

```http
POST /api/v1/search/v2
Content-Type: application/json

{
  "rawQuery": "винтажная кожаная куртка 90s",
  "scope": "PRODUCT",
  "cityId": "city-uuid-astana",
  "lat": 51.1694,
  "lng": 71.4491,
  "radiusM": 15000
}
```

```json
{
  "searchSessionId": "session-uuid-001",
  "rawQuery": "винтажная кожаная куртка 90s",
  "scope": "PRODUCT",
  "understoodQuery": "Ищем винтажные кожаные куртки в стиле 90-х, рядом с вами",
  "sections": [
    {
      "type": "exact_products",
      "title": "Нашли похожее на твой запрос",
      "cards": [
        {
          "component": "ProductCard",
          "resultId": "prod-uuid-001",
          "businessId": "biz-uuid-001",
          "businessName": "Oldschool Market",
          "brandColor": "#2a1e15",
          "brandLogoUrl": "https://cdn.ask.kz/logos/oldschool.png",
          "title": "Кожаная куртка Levi's 90s",
          "price": 45000,
          "availability": "NEEDS_CONFIRMATION",
          "matchReasons": ["Levi's", "винтажный стиль", "в вашем районе"],
          "badges": ["Активный бизнес", "Данные обновлены сегодня"],
          "distanceMeters": 1200,
          "branchName": "Oldschool Market — ул. Пушкина",
          "hasActiveDrop": true
        }
      ]
    },
    {
      "type": "fresh_drops",
      "title": "Свежие поступления",
      "cards": [
        {
          "component": "DropCard",
          "resultId": "drop-uuid-001",
          "businessId": "biz-uuid-002",
          "businessName": "Vintage Room",
          "brandColor": "#1a1a2e",
          "brandLogoUrl": null,
          "title": "Дроп японских джинс Levi's 90s",
          "price": null,
          "availability": "NEEDS_CONFIRMATION",
          "matchReasons": ["сегодняшний дроп", "совпадает по бренду и стилю"],
          "badges": ["Новый дроп"],
          "distanceMeters": 2500,
          "branchName": "Vintage Room — ТРЦ Keruen",
          "hasActiveDrop": true
        }
      ]
    }
  ],
  "supplierCheckCount": 8
}
```

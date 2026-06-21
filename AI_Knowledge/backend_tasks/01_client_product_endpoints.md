# Task 01: Client Product Search And Request Endpoints

|   |   |
|---|---|
|**Описание**|Клиентские endpoint'ы для поиска товаров, просмотра результата, открытия контекста магазина, чата и fallback-запроса к подходящим филиалам.|
|**Модуль системы**|catalog, search, request, messaging|

## Зависимости

- `Task 00: Search Session And Snapshot Foundation`
- `Task 05: Identity Auth, Real Verification, And Business Onboarding`
- `Product`, `ProductOffer`, `Business`, `BusinessBranch`, `BusinessContact`
- `SearchDocument`, `SearchSession`, `SearchSnapshot`, `SearchResultSnapshot`
- `CustomerRequest`, `RequestTarget`, `SupplierResponse`, `ConversationLink`

## Общие правила задачи

- Поиск есть только по товарам и услугам. Отдельного поиска по бизнесам в MVP нет.
- Товар показывается в поиске, если он включен бизнесом и связан с активным филиалом.
- DTO описывает только актуальную MVP-модель: данные товара, бизнес/филиал, price, displayState, рассчитанную дистанцию и contactActions.
- Чат доступен всегда из конкретного product/request/business контекста.
- Инвентарный учет количества не входит в MVP.
- Актуальность не проверяется через timestamp: включенный товар считается текущим, пока бизнес его не выключил или не удалил.
- `distanceMeters` возвращается только если backend рассчитал расстояние от геолокации клиента до координат филиала.

---

## Поиск товаров - POST /api/v1/client/products/search

|   |   |
|---|---|
|**Описание**|Ищет включенные товары по raw query, категории и динамическим фильтрам.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/products/search`|
|**Метод запроса**|POST|

## 1. Задачи, в рамках которых вносятся изменения в метод

_-_

## 2. Функциональные требования

| Номер требования | Описание требования | Статус | Источник | Комментарий |
|---|---|---|---|---|
|PSEARCH-001|Raw query сохраняется без перезаписи внутренним названием товара.|Required|Frontend UX||
|PSEARCH-002|Категория только сужает поиск.|Required|Frontend UX||
|PSEARCH-003|Фильтры строятся динамически из найденных товаров.|Required|Frontend UX||
|PSEARCH-004|Если включенный товар подходит под smart search, он показывается без отдельного confidence поля.|Required|Product correction||
|PSEARCH-005|Если товаров нет, возвращается fallback block.|Required|Frontend UX||

## 3. Описание логики работы метода

### 3.1 Валидация

- Пользователь аутентифицирован как клиент.
- `rawQuery` обязателен.
- `scope` этой задачи всегда product.
- Если передана только одна координата клиента, distance не рассчитывается.

### 3.2 Поиск

- Искать по `SearchDocument` для включенных product offers.
- Учитывать название, описание, tags, категорию, бизнес, филиал и цену.
- Не делать отдельный business search.
- Если координаты клиента и филиала известны, рассчитать `distanceMeters`.
- Если координаты отсутствуют, вернуть `distanceMeters=null`.

### 3.3 Fallback

- `fallbackAvailable=true`, если подходящих включенных товаров нет или клиент явно хочет уточнить у магазинов.
- Fallback не означает, что товар отсутствует во всех магазинах; это означает, что search не дал достаточно полезный результат.

## 4. Разрешения доступа к методу

| Наименование разрешения | Описание разрешения |
|---|---|
|CUSTOMER_SESSION|Требуется активная сессия клиента из Task 05.|

## 5. Настройки системы, используемые в методе

| Наименование | Код | Тип значения |
|---|---|---|
|Максимальный размер страницы|`search.products.max-page-size`|integer|

## 6. Параметры метода

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID поисковой сессии|`searchSessionId`|uuid body|+|-||
|2|Сырой запрос|`rawQuery`|string body|+|-||
|3|ID города|`cityId`|uuid body|-|-||
|4|ID категории|`categoryId`|uuid body|-|-||
|5|Широта клиента|`customerLatitude`|decimal body|-|-|Для расчета расстояния|
|6|Долгота клиента|`customerLongitude`|decimal body|-|-|Для расчета расстояния|
|7|Фильтры|`filters`|object body|-|-|Динамические фильтры|
|8|Страница|`page`|integer body|-|0||
|9|Размер|`size`|integer body|-|20||

## 7. Возвращаемые данные

| № | Описание поля | Наименование | Тип | Этап | Источник данных | Комментарий |
|---|---|---|---|---|---|---|
|1|ID поисковой сессии|`searchSessionId`|uuid|Этап 1|search_session||
|2|Сырой запрос|`rawQuery`|string|Этап 1|search_session||
|3|Результаты|`results`|array|Этап 1|ClientProductResultResponse[]||
|4|Количество результатов|`resultCount`|integer|Этап 1|query result||
|5|Доступен fallback|`fallbackAvailable`|boolean|Этап 1|search logic||
|6|Причина fallback|`fallbackReason`|string|Этап 1|search logic|nullable|
|7|Динамические фильтры|`dynamicFilters`|array|Этап 1|result aggregation||

### ClientProductResultResponse

| № | Описание поля | Наименование | Тип | Этап | Источник данных | Комментарий |
|---|---|---|---|---|---|---|
|1|ID product offer|`productOfferId`|uuid|Этап 1|product_offer||
|2|ID продукта|`productId`|uuid|Этап 1|product||
|3|ID бизнеса|`businessId`|uuid|Этап 1|business||
|4|ID филиала|`branchId`|uuid|Этап 1|business_branch||
|5|Название бизнеса|`businessName`|string|Этап 1|business.name||
|6|Название филиала|`branchName`|string|Этап 1|business_branch.name||
|7|Название товара|`productName`|string|Этап 1|product.name||
|8|Описание|`description`|string|Этап 1|product.description|nullable|
|9|Изображение|`imageUrl`|string|Этап 1|product/category|nullable|
|10|Цена|`price`|decimal|Этап 1|product_offer.price|nullable|
|11|Категория|`categoryName`|string|Этап 1|category||
|12|Состояние показа|`displayState`|string|Этап 1|derived|`ENABLED`, `CLARIFY_AVAILABILITY`|
|13|Дистанция в метрах|`distanceMeters`|integer|Этап 1|calculated|nullable|
|14|Адрес|`address`|string|Этап 1|business_branch.address|nullable|
|15|Контактные действия|`contactActions`|array|Этап 1|branch contacts + Ask chat||

## Пример запроса

```http
POST /api/v1/client/products/search
Authorization: Bearer <token>
Content-Type: application/json

{
  "searchSessionId": "ss-uuid-001",
  "rawQuery": "чековый принтер для кассы",
  "cityId": "city-uuid-astana",
  "categoryId": "cat-uuid-pos",
  "customerLatitude": 51.1282,
  "customerLongitude": 71.4304,
  "filters": {
    "brand": ["Mercury"],
    "maxPrice": 120000
  },
  "page": 0,
  "size": 20
}
```

## Пример ответа

```json
{
  "searchSessionId": "ss-uuid-001",
  "rawQuery": "чековый принтер для кассы",
  "resultCount": 1,
  "fallbackAvailable": true,
  "fallbackReason": null,
  "dynamicFilters": [
    { "code": "brand", "title": "Бренд", "values": ["Mercury", "Штрих-М"] }
  ],
  "results": [
    {
      "productOfferId": "po-uuid-001",
      "productId": "prod-uuid-001",
      "businessId": "biz-uuid-001",
      "branchId": "branch-uuid-001",
      "businessName": "Kaspi POS Store",
      "branchName": "Kaspi POS Store Абая",
      "productName": "Mercury MPRINT G80",
      "description": "Чековый принтер для кассы",
      "imageUrl": null,
      "price": 99000,
      "categoryName": "POS оборудование",
      "displayState": "ENABLED",
      "distanceMeters": 350,
      "address": "ул. Абая 45",
      "contactActions": ["ASK_CHAT", "PHONE", "MAP"]
    }
  ]
}
```

---

## Детали товара - GET /api/v1/client/product-offers/{productOfferId}

|   |   |
|---|---|
|**Описание**|Возвращает детальную карточку товара и связанный магазин/филиал.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/product-offers/{productOfferId}`|
|**Метод запроса**|GET|

## 3. Описание логики работы метода

- Возвращать только включенный товар активного филиала.
- Возвращать только поля актуальной MVP-модели: товар, бизнес/филиал, price, displayState, address, contactActions.
- Ask chat всегда доступен через `contactActions`.
- Business page открывается из этого контекста, но не является отдельным search flow.

## 6. Параметры метода

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID product offer|`productOfferId`|uuid path|+|-||

## Пример запроса

```http
GET /api/v1/client/product-offers/po-uuid-001
Authorization: Bearer <token>
```

## Пример ответа

```json
{
  "productOfferId": "po-uuid-001",
  "productId": "prod-uuid-001",
  "businessId": "biz-uuid-001",
  "branchId": "branch-uuid-001",
  "productName": "Mercury MPRINT G80",
  "businessName": "Kaspi POS Store",
  "branchName": "Kaspi POS Store Абая",
  "price": 99000,
  "displayState": "ENABLED",
  "address": "ул. Абая 45",
  "contactActions": ["ASK_CHAT", "PHONE", "MAP"]
}
```

---

## Создание товарного запроса - POST /api/v1/client/product-requests

|   |   |
|---|---|
|**Описание**|Создает fallback-запрос, если клиент не нашел товар или хочет уточнить у магазинов.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/product-requests`|
|**Метод запроса**|POST|

## 6. Параметры метода

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID поисковой сессии|`searchSessionId`|uuid body|+|-||
|2|Сырой запрос|`rawQuery`|string body|+|-||
|3|ID города|`cityId`|uuid body|-|-||
|4|ID категории|`categoryId`|uuid body|-|-||
|5|Контекст товара|`productOfferId`|uuid body|-|-|Не обязательный выбор SKU|
|6|Комментарий клиента|`customerNote`|string body|-|-||
|7|Целевые филиалы|`targetBranchIds`|array body|-|-||
|8|Ключ идемпотентности|`idempotencyKey`|string body|+|-||

## 7. Возвращаемые данные

| № | Поле | Тип | Источник | Комментарий |
|---|---|---|---|---|
|1|`requestId`|uuid|customer_request||
|2|`searchSessionId`|uuid|search_session||
|3|`rawQuery`|string|customer_request||
|4|`status`|string|customer_request||
|5|`recipientCount`|integer|request_target||
|6|`responseCount`|integer|supplier_response||
|7|`expiresAt`|datetime|request policy||
|8|`progress`|object|derived||

## Пример запроса

```http
POST /api/v1/client/product-requests
Authorization: Bearer <token>
Content-Type: application/json

{
  "searchSessionId": "ss-uuid-001",
  "rawQuery": "чековый принтер с автоотрезчиком",
  "cityId": "city-uuid-astana",
  "categoryId": "cat-uuid-pos",
  "productOfferId": null,
  "customerNote": "Нужен для кассы, желательно сегодня",
  "targetBranchIds": ["branch-uuid-001", "branch-uuid-002"],
  "idempotencyKey": "client-key-001"
}
```

## Пример ответа

```json
{
  "requestId": "req-uuid-001",
  "searchSessionId": "ss-uuid-001",
  "rawQuery": "чековый принтер с автоотрезчиком",
  "status": "SENT",
  "recipientCount": 2,
  "responseCount": 0,
  "expiresAt": "2026-06-21T14:00:00Z",
  "progress": {
    "sent": true,
    "waitingForResponses": true
  }
}
```

---

## Лента ответов - GET /api/v1/client/product-requests/{requestId}/responses

|   |   |
|---|---|
|**Описание**|Возвращает ответы магазинов по товарному запросу.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/product-requests/{requestId}/responses`|
|**Метод запроса**|GET|

## Статусы ответа бизнеса

| Статус | Смысл |
|---|---|
|`HAS_ITEM`|Есть товар или подходящее совпадение.|
|`NO_ITEM`|Нет товара.|
|`NEED_CLARIFICATION`|Нужно уточнение.|
|`HAS_ANALOG`|Есть аналог.|

## Правила

- Новый бизнес-ответ добавляется в ленту.
- Обновление ответа того же филиала обновляет ту же строку.
- Чат открывается из строки ответа.
- Контакты филиала и Ask chat возвращаются как actions.

## Пример запроса

```http
GET /api/v1/client/product-requests/req-uuid-001/responses?page=0&size=20
Authorization: Bearer <token>
```

## Пример ответа

```json
{
  "requestId": "req-uuid-001",
  "items": [
    {
      "supplierResponseId": "resp-uuid-001",
      "businessId": "biz-uuid-001",
      "branchId": "branch-uuid-001",
      "businessName": "Kaspi POS Store",
      "status": "HAS_ITEM",
      "price": 99000,
      "productHint": "Mercury MPRINT G80",
      "distanceMeters": 350,
      "messageCount": 1,
      "updatedAt": "2026-06-21T10:30:00Z",
      "details": {
        "comment": "Есть в магазине на Абая",
        "address": "ул. Абая 45",
        "contactActions": ["ASK_CHAT", "PHONE", "MAP"],
        "chatThreadId": "conv-uuid-001"
      }
    }
  ],
  "page": 0
}
```

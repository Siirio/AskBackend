# Task 02: Client Service Search And Request Endpoints

|   |   |
|---|---|
|**Описание**|Клиентские endpoint'ы для поиска услуг, просмотра услуги, отправки заявки на желаемое время, бизнес-подтверждения через активность и чат.|
|**Модуль системы**|service, search, request, messaging|

## Зависимости

- `Task 00: Search Session And Snapshot Foundation`
- `Task 05: Identity Auth And Session Endpoints`
- `ServiceOffering`, `ServiceBranchOffer`, `Business`, `BusinessBranch`, `BusinessContact`
- `SearchDocument`, `SearchSession`, `SearchSnapshot`, `SearchResultSnapshot`
- `CustomerRequest`, `RequestTarget`, `SupplierResponse`, `ConversationLink`

## Общие правила задачи

- Услуги ищутся отдельно от товаров.
- Отдельного поиска по бизнесам нет.
- MVP услуга работает как заявка на запись, не как гарантированная бронь слота.
- DTO описывает только актуальную MVP-модель: данные услуги, бизнес/филиал, price, duration, displayState, рассчитанную дистанцию и contactActions.
- Чат доступен всегда из конкретного контекста.
- Активная услуга показывается клиентам, неактивная не показывается.
- `distanceMeters` возвращается только при реальных координатах клиента и филиала.

## Поиск услуг - POST /api/v1/client/services/search

|   |   |
|---|---|
|**Описание**|Ищет активные услуги по raw query, категории, желаемому времени и динамическим фильтрам.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/services/search`|
|**Метод запроса**|POST|

### 1. Функциональные требования

| Номер | Требование | Статус | Источник | Комментарий |
|---|---|---|---|---|
| SSEARCH-001 | Raw query сохраняется. | Required | Frontend UX | |
| SSEARCH-002 | Услуга не обещает гарантированное бронирование. | Required | Frontend UX | |
| SSEARCH-003 | Клиент выбирает желаемое время, бизнес подтверждает финальное. | Required | Frontend UX | |
| SSEARCH-004 | Динамические фильтры строятся из найденных услуг. | Required | Frontend UX | |

### 2. Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID поисковой сессии|`searchSessionId`|uuid|+|-||
|2|Сырой запрос|`rawQuery`|string|+|-||
|3|ID города|`cityId`|uuid|-|-||
|4|ID категории|`categoryId`|uuid|-|-||
|5|Желаемое начало|`desiredStartAt`|datetime|-|-|Не гарантированный слот|
|6|Широта клиента|`customerLatitude`|decimal|-|-|Для distanceMeters|
|7|Долгота клиента|`customerLongitude`|decimal|-|-|Для distanceMeters|
|8|Фильтры|`filters`|object|-|-||
|9|Страница|`page`|integer|-|0||
|10|Размер|`size`|integer|-|20||

### 3. Возвращаемые данные

| № | Поле | Тип | Источник | Комментарий |
|---|---|---|---|---|
|1|`searchSessionId`|uuid|search session||
|2|`rawQuery`|string|search session||
|3|`results`|array|`ClientServiceResultResponse[]`||
|4|`resultCount`|integer|query result||
|5|`fallbackAvailable`|boolean|search logic||
|6|`fallbackReason`|string|-|Например `NO_MATCH`|
|7|`dynamicFilters`|array|result aggregation||

### ClientServiceResultResponse

| № | Поле | Тип | Источник | Комментарий |
|---|---|---|---|---|
|1|`serviceBranchOfferId`|uuid|service branch offer||
|2|`serviceOfferingId`|uuid|service offering||
|3|`businessId`|uuid|business||
|4|`branchId`|uuid|branch||
|5|`businessName`|string|business||
|6|`branchName`|string|branch||
|7|`serviceName`|string|service offering||
|8|`description`|string|service offering||
|9|`imageUrl`|string|service/business||
|10|`basePrice`|decimal|service branch offer|nullable|
|11|`durationMinutes`|integer|service branch offer|nullable|
|12|`serviceMode`|string|service branch offer||
|13|`displayState`|string|derived|`ACTIVE`, `REQUEST_CONFIRMATION`|
|14|`distanceMeters`|integer|calculated|nullable|
|15|`address`|string|branch|nullable|
|16|`contactActions`|array|branch contacts + Ask chat||

## Детали услуги - GET /api/v1/client/service-offers/{serviceBranchOfferId}

|   |   |
|---|---|
|**Описание**|Возвращает услугу и филиал для карточки detail.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/service-offers/{serviceBranchOfferId}`|
|**Метод запроса**|GET|

### Правила

- Возвращать только активную услугу активного филиала.
- Возвращать только поля актуальной MVP-модели: услуга, бизнес/филиал, price, duration, displayState, address, contactActions.
- Ask chat всегда доступен через contact actions.
- Услуга может предлагать желаемое время, но не гарантирует бронь.

## Создание заявки на услугу - POST /api/v1/client/service-requests

|   |   |
|---|---|
|**Описание**|Создает заявку на услугу с желаемым временем. Бизнес подтверждает, отклоняет или предлагает другое время.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/service-requests`|
|**Метод запроса**|POST|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID поисковой сессии|`searchSessionId`|uuid|+|-||
|2|Сырой запрос|`rawQuery`|string|+|-||
|3|Контекст услуги|`serviceBranchOfferId`|uuid|-|-||
|4|Желаемое начало|`desiredStartAt`|datetime|-|-|Не гарантированный слот|
|5|Комментарий клиента|`customerNote`|string|-|-||
|6|Целевые филиалы|`targetBranchIds`|array|-|-||
|7|Ключ идемпотентности|`idempotencyKey`|string|+|-||

### Возвращаемые данные

| № | Поле | Тип | Источник | Комментарий |
|---|---|---|---|---|
|1|`requestId`|uuid|customer request||
|2|`searchSessionId`|uuid|search session||
|3|`status`|string|customer request||
|4|`desiredStartAt`|datetime|customer request|nullable|
|5|`recipientCount`|integer|request targets||
|6|`responseCount`|integer|supplier responses||
|7|`conversationId`|uuid|conversation|nullable until created|

## Ответы бизнеса по услуге

| Статус | Смысл |
|---|---|
|`CAN_PROVIDE`|Бизнес может оказать услугу.|
|`CANNOT_PROVIDE`|Бизнес не может оказать услугу.|
|`NEED_CLARIFICATION`|Нужны детали.|
|`SUGGEST_OTHER_TIME`|Бизнес предлагает другое время.|

## Подтверждение услуги бизнесом

- Бизнес видит заявку во вкладке Activity.
- Бизнес подтверждает финальную дату и время.
- Если договорились в чате о другом времени, бизнес указывает другое финальное время.
- Клиент видит подтвержденное время как результат бизнес-действия, не как автоматическую бронь.

## Пример запроса поиска

```http
POST /api/v1/client/services/search
Authorization: Bearer <token>
Content-Type: application/json

{
  "searchSessionId": "ss-uuid-002",
  "rawQuery": "ремонт кассового принтера сегодня",
  "cityId": "city-uuid-astana",
  "categoryId": "cat-uuid-repair",
  "desiredStartAt": "2026-06-21T15:00:00Z",
  "customerLatitude": 51.1282,
  "customerLongitude": 71.4304,
  "filters": {
    "maxPrice": 20000
  },
  "page": 0,
  "size": 20
}
```

## Пример ответа поиска

```json
{
  "searchSessionId": "ss-uuid-002",
  "rawQuery": "ремонт кассового принтера сегодня",
  "resultCount": 1,
  "fallbackAvailable": true,
  "fallbackReason": null,
  "dynamicFilters": [
    { "code": "price", "title": "Цена", "values": ["до 20000"] }
  ],
  "results": [
    {
      "serviceBranchOfferId": "sbo-uuid-001",
      "serviceOfferingId": "svc-uuid-001",
      "businessId": "biz-uuid-001",
      "branchId": "branch-uuid-001",
      "businessName": "Kaspi POS Store",
      "branchName": "Kaspi POS Store Абая",
      "serviceName": "Ремонт кассового принтера",
      "description": "Диагностика и ремонт POS-принтеров",
      "imageUrl": null,
      "basePrice": 15000,
      "durationMinutes": 60,
      "serviceMode": "REQUEST_TO_BOOK",
      "displayState": "ACTIVE",
      "distanceMeters": 500,
      "address": "ул. Абая 45",
      "contactActions": ["ASK_CHAT", "PHONE", "MAP"]
    }
  ]
}
```

## Пример запроса на услугу

```http
POST /api/v1/client/service-requests
Authorization: Bearer <token>
Content-Type: application/json

{
  "searchSessionId": "ss-uuid-002",
  "rawQuery": "ремонт кассового принтера сегодня",
  "serviceBranchOfferId": "sbo-uuid-001",
  "desiredStartAt": "2026-06-21T15:00:00Z",
  "customerNote": "Принтер не печатает чек",
  "targetBranchIds": ["branch-uuid-001"],
  "idempotencyKey": "service-key-001"
}
```

## Пример ответа на заявку

```json
{
  "requestId": "req-uuid-service-001",
  "searchSessionId": "ss-uuid-002",
  "status": "SENT",
  "desiredStartAt": "2026-06-21T15:00:00Z",
  "recipientCount": 1,
  "responseCount": 0,
  "conversationId": null
}
```

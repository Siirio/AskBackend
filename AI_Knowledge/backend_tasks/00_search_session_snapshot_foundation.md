# Task 00: Search Session And Snapshot Foundation

|   |   |
|---|---|
|**Описание**|Фундамент поисковых сессий и snapshots для product/service search. История должна открывать сохраненное состояние результатов без live-перепоиска и без устаревших confidence/stock/freshness полей.|
|**Модуль системы**|search|

## Зависимости

- `AppUser`
- `CustomerProfile`
- `ProductOffer`
- `ServiceBranchOffer`
- `Business`
- `BusinessBranch`
- `CustomerRequest`
- `ConversationLink`

## Общие правила задачи

- В MVP есть только product search и service search.
- Отдельного business search нет.
- Business page может открываться из product/service result, request, response, chat или history context.
- Scope хранит только `PRODUCT` или `SERVICE`.
- После submit search session scope считается locked. Изменение `PRODUCT` на `SERVICE` внутри той же submitted session запрещено; для другого scope создается новая search session.
- Auto supplier check/request может быть связан с search session, если backend нашел подходящих поставщиков для raw query.
- Search snapshot хранит только актуальную MVP-модель product/service rows: display data, context ids, price, displayState, sourceType and calculated distance.
- `distanceMeters` хранится в snapshot только если был реально рассчитан из координат клиента и филиала на момент поиска.
- Snapshot не доказывает live наличие, live цену или live доступность услуги.

## Сущность search_session

|   |   |
|---|---|
|**Описание**|Текущая поисковая сессия пользователя для product/service scope.|
|**Владелец**|`kz.ask.search`|

### Атрибуты

| № | Название | Тип | Обязательность | Описание |
|---|---|---|---|---|
|1|`id`|uuid|+|ID сессии|
|2|`user_id`|uuid|+|Владелец поиска|
|3|`city_id`|uuid|-|Город поиска|
|4|`category_id`|uuid|-|Категория поиска|
|5|`raw_query`|varchar|+|Точный ввод пользователя|
|6|`scope`|varchar|+|`PRODUCT` или `SERVICE`|
|6.1|`scope_locked`|boolean|+|После submit всегда true для active submitted search|
|7|`status`|varchar|+|`ACTIVE`, `SNAPSHOTTED`, `EXPIRED`, `CANCELLED`, `FAILED`|
|8|`started_at`|timestamp|+|Начало|
|9|`last_active_at`|timestamp|+|Последняя активность|
|10|`snapshot_expires_at`|timestamp|-|Истечение истории|
|11|`created_at`|timestamp|+|Создание|
|12|`updated_at`|timestamp|+|Обновление|

### Правила

- `raw_query` не переписывается нормализованным названием.
- Новый current search закрывает или snapshot'ит предыдущий active search пользователя.
- Scope определяет продуктовый или сервисный поиск.
- Scope можно менять до submit на стартовом Search UI, но нельзя менять внутри уже созданной submitted search session.
- Auto supplier check не меняет scope search session. Для product query он остается `PRODUCT`, для service query — `SERVICE`.

## Сущность search_snapshot

|   |   |
|---|---|
|**Описание**|Сохраненное состояние product/service search для истории клиента.|
|**Владелец**|`kz.ask.search`|

### Атрибуты

| № | Название | Тип | Обязательность | Описание |
|---|---|---|---|---|
|1|`id`|uuid|+|ID snapshot|
|2|`search_session_id`|uuid|+|ID сессии|
|3|`user_id`|uuid|+|Владелец|
|4|`raw_query`|varchar|+|Сырой запрос|
|5|`scope`|varchar|+|`PRODUCT` или `SERVICE`|
|6|`city_id`|uuid|-|Город|
|7|`category_id`|uuid|-|Категория|
|8|`status`|varchar|+|`ACTIVE`, `EXPIRED`, `DELETED`|
|9|`result_count`|integer|+|Всего результатов|
|10|`product_result_count`|integer|+|Количество product results|
|11|`service_result_count`|integer|+|Количество service results|
|12|`request_id`|uuid|-|Связанный fallback request|
|13|`expires_at`|timestamp|-|Истечение истории|
|14|`created_at`|timestamp|+|Создание|
|15|`updated_at`|timestamp|+|Обновление|

### Правила

- Snapshot хранит то, что пользователь видел.
- Snapshot не обновляется молча при изменении live данных.
- Если live товар или услуга выключены позже, historical snapshot остается, но live detail должен показать текущее состояние отдельно.

## Сущность search_result_snapshot

|   |   |
|---|---|
|**Описание**|Одна сохраненная строка product/service результата внутри snapshot.|
|**Владелец**|`kz.ask.search`|

### Атрибуты

| № | Название | Тип | Обязательность | Описание |
|---|---|---|---|---|
|1|`id`|uuid|+|ID строки|
|2|`search_snapshot_id`|uuid|+|ID snapshot|
|3|`result_type`|varchar|+|`PRODUCT` или `SERVICE`|
|4|`result_rank`|integer|+|Позиция|
|5|`product_offer_id`|uuid|-|Product context|
|6|`service_branch_offer_id`|uuid|-|Service context|
|7|`business_id`|uuid|+|Business context из результата|
|8|`branch_id`|uuid|+|Branch context из результата|
|9|`title`|varchar|+|Заголовок карточки|
|10|`summary`|varchar|-|Короткое описание|
|11|`price`|decimal|-|Цена на момент snapshot|
|12|`display_state`|varchar|-|Например `ENABLED`, `REQUEST_CONFIRMATION`, `HISTORICAL`|
|13|`source_type`|varchar|-|`MANUAL`, `IMPORT`, `ADMIN`|
|14|`distance_meters`|integer|-|Рассчитанная дистанция на момент поиска|
|15|`created_at`|timestamp|+|Создание|
|16|`updated_at`|timestamp|+|Обновление|

### Правила

- `business_id` и `branch_id` нужны как контекст результата, не как business search result.
- Не добавлять в snapshot отдельные поля вне актуальной MVP-модели результата.
- Не хранить stock quantity.
- Не хранить freshness.

## Создание поисковой сессии - POST /api/v1/client/search-sessions

|   |   |
|---|---|
|**Описание**|Создает current search session для product или service search.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/search-sessions`|
|**Метод запроса**|POST|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|Сырой запрос|`rawQuery`|string body|+|-||
|2|Scope|`scope`|string body|+|-|`PRODUCT` или `SERVICE`|
|3|ID города|`cityId`|uuid body|-|-||
|4|ID категории|`categoryId`|uuid body|-|-||
|5|Фильтры|`filters`|object body|-|-|Клиентское намерение|

### Возвращаемые данные

| № | Поле | Тип | Источник | Комментарий |
|---|---|---|---|---|
|1|`searchSessionId`|uuid|search_session||
|2|`rawQuery`|string|search_session||
|3|`scope`|string|search_session||
|4|`status`|string|search_session||
|5|`startedAt`|datetime|search_session||

## Создание snapshot - POST /api/v1/client/search-sessions/{searchSessionId}/snapshot

|   |   |
|---|---|
|**Описание**|Фиксирует текущее состояние product/service результатов для истории.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/search-sessions/{searchSessionId}/snapshot`|
|**Метод запроса**|POST|

### Возвращаемые данные

| № | Поле | Тип | Источник | Комментарий |
|---|---|---|---|---|
|1|`id`|uuid|search_snapshot||
|2|`rawQuery`|string|search_snapshot||
|3|`scope`|string|search_snapshot|`PRODUCT` or `SERVICE`|
|4|`counts`|object|search_snapshot|total/products/services|
|5|`products`|array|result snapshots|Only for product scope|
|6|`services`|array|result snapshots|Only for service scope|
|7|`linkedRequest`|object|customer_request|nullable|
|8|`expiresAt`|datetime|search_snapshot||

### SnapshotResultResponse

| № | Поле | Тип | Источник | Комментарий |
|---|---|---|---|---|
|1|`resultRank`|integer|search_result_snapshot||
|2|`resultType`|string|search_result_snapshot|PRODUCT/SERVICE|
|3|`productOfferId`|uuid|search_result_snapshot|nullable|
|4|`serviceBranchOfferId`|uuid|search_result_snapshot|nullable|
|5|`businessId`|uuid|search_result_snapshot|context|
|6|`branchId`|uuid|search_result_snapshot|context|
|7|`title`|string|search_result_snapshot||
|8|`summary`|string|search_result_snapshot||
|9|`price`|decimal|search_result_snapshot|nullable|
|10|`displayState`|string|search_result_snapshot||
|11|`sourceType`|string|search_result_snapshot|nullable|
|12|`distanceMeters`|integer|search_result_snapshot|nullable|

## Список snapshot - GET /api/v1/client/search-snapshots

|   |   |
|---|---|
|**Описание**|Возвращает историю product/service search текущего клиента.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/search-snapshots`|
|**Метод запроса**|GET|

## Детали snapshot - GET /api/v1/client/search-snapshots/{searchSnapshotId}

|   |   |
|---|---|
|**Описание**|Открывает сохраненный product/service snapshot без live-перепоиска.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/client/search-snapshots/{searchSnapshotId}`|
|**Метод запроса**|GET|

### Правила

- Snapshot принадлежит текущему customer.
- Historical values не заменяются live values silently.
- Если live entity выключена, detail может показать текущее состояние отдельно.

## Distance Snapshot Rule

- `distanceMeters` в snapshot сохраняет расстояние, которое было рассчитано в момент поиска.
- Расстояние считается только из `customerLatitude/customerLongitude` и `branch.latitude/branch.longitude`.
- Если координат не было, distance is null.

## Пример создания search session

```http
POST /api/v1/client/search-sessions
Authorization: Bearer <token>
Content-Type: application/json

{
  "rawQuery": "чековый принтер для кассы",
  "scope": "PRODUCT",
  "cityId": "city-uuid-astana",
  "categoryId": "cat-uuid-pos",
  "filters": {
    "brand": ["Mercury"]
  }
}
```

## Пример ответа search session

```json
{
  "searchSessionId": "ss-uuid-001",
  "rawQuery": "чековый принтер для кассы",
  "scope": "PRODUCT",
  "status": "ACTIVE",
  "startedAt": "2026-06-21T10:00:00Z"
}
```

## Пример snapshot response

```json
{
  "id": "snap-uuid-001",
  "rawQuery": "чековый принтер для кассы",
  "scope": "PRODUCT",
  "counts": {
    "total": 1,
    "products": 1,
    "services": 0
  },
  "products": [
    {
      "resultRank": 1,
      "resultType": "PRODUCT",
      "productOfferId": "po-uuid-001",
      "serviceBranchOfferId": null,
      "businessId": "biz-uuid-001",
      "branchId": "branch-uuid-001",
      "title": "Mercury MPRINT G80",
      "summary": "Kaspi POS Store, ул. Абая 45",
      "price": 99000,
      "displayState": "ENABLED",
      "sourceType": "ADMIN",
      "distanceMeters": 350
    }
  ],
  "services": [],
  "linkedRequest": null,
  "expiresAt": "2026-07-01T10:00:00Z"
}
```

## Implementation Boundaries

- Использовать `kz.ask.search`.
- Не создавать business search.
- Не создавать confidence/freshness/stock snapshot fields.
- Не создавать tests и не запускать Maven без явного запроса.

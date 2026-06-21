# Task 04: Business Cabinet Service And Activity Endpoints

|   |   |
|---|---|
|**Описание**|Endpoint'ы бизнес-кабинета для реального сохранения услуг филиала и обработки заявок на услуги через Activity и чат.|
|**Модуль системы**|service, business, request, messaging, search|

## Зависимости

- `Task 05: Identity Auth And Session Endpoints`
- `Business`, `BusinessMember`, `BusinessBranch`
- `Category`, `ServiceOffering`, `ServiceBranchOffer`
- `CustomerRequest`, `RequestTarget`, `SupplierResponse`, `ConversationLink`, `SearchDocument`

## Общие правила задачи

- Услуги сохраняются в реальной базе и становятся основой клиентского поиска.
- Каждая текущая регистрация создает конкретный филиал/заведение, услуги относятся к нему.
- У услуги есть состояние показа: активна или не активна.
- Активная услуга попадает в клиентский поиск.
- Неактивная услуга не попадает в live client search.
- Контракт услуг описывает только active/inactive visibility, price, duration, scheduleText и обработку заявок через Activity/chat.
- MVP не блокирует слоты и не является полноценным календарем.
- Заявка на услугу подтверждается бизнесом вручную.

## Список услуг филиала - GET /api/v1/business-admin/branches/{branchId}/services

|   |   |
|---|---|
|**Описание**|Возвращает услуги конкретного филиала.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/business-admin/branches/{branchId}/services`|
|**Метод запроса**|GET|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID филиала|`branchId`|uuid path|+|-||
|2|ID категории|`categoryId`|uuid query|-|-||
|3|Активна|`active`|boolean query|-|-||
|4|Поиск|`query`|string query|-|-||
|5|Страница|`page`|integer query|-|0||
|6|Размер|`size`|integer query|-|20||

### BusinessServiceRowResponse

| № | Поле | Тип | Источник | Комментарий |
|---|---|---|---|---|
|1|`serviceOfferingId`|uuid|service offering||
|2|`serviceBranchOfferId`|uuid|service branch offer||
|3|`branchId`|uuid|branch||
|4|`categoryId`|uuid|category||
|5|`name`|string|service offering||
|6|`description`|string|service offering||
|7|`basePrice`|decimal|service branch offer|nullable|
|8|`durationMinutes`|integer|service branch offer|nullable|
|9|`scheduleText`|string|service branch offer|nullable, display-only MVP|
|10|`active`|boolean|service branch offer|Controls live search visibility|
|11|`updatedAt`|datetime|entity audit||

## Создание услуги - POST /api/v1/business-admin/branches/{branchId}/services

|   |   |
|---|---|
|**Описание**|Создает услугу для конкретного филиала.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/business-admin/branches/{branchId}/services`|
|**Метод запроса**|POST|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID филиала|`branchId`|uuid path|+|-||
|2|ID категории|`categoryId`|uuid body|+|-||
|3|Название|`name`|string body|+|-||
|4|Описание|`description`|string body|-|-||
|5|Цена от|`basePrice`|decimal body|-|-||
|6|Примерная длительность|`durationMinutes`|integer body|-|-||
|7|График/условия показа|`scheduleText`|string body|-|-|Не календарная блокировка|
|8|Активна|`active`|boolean body|-|true||

### Правила

- Созданная активная услуга сразу становится доступна для клиентского поиска после обновления search document.
- Search document строится по названию, описанию, категории, бизнесу, филиалу, цене и описанию графика.
- Не создавать specialist accounts.
- Не создавать полноценный staff/calendar system в MVP.

## Обновление услуги - PATCH /api/v1/business-admin/branches/{branchId}/services/{serviceOfferingId}

|   |   |
|---|---|
|**Описание**|Обновляет услугу и состояние показа в филиале.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/business-admin/branches/{branchId}/services/{serviceOfferingId}`|
|**Метод запроса**|PATCH|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID филиала|`branchId`|uuid path|+|-||
|2|ID услуги|`serviceOfferingId`|uuid path|+|-||
|3|ID категории|`categoryId`|uuid body|-|-||
|4|Название|`name`|string body|-|-||
|5|Описание|`description`|string body|-|-||
|6|Цена от|`basePrice`|decimal body|-|-||
|7|Примерная длительность|`durationMinutes`|integer body|-|-||
|8|График/условия показа|`scheduleText`|string body|-|-||
|9|Активна|`active`|boolean body|-|-||

### Правила

- `active=false` выключает услугу из live client search.
- `active=true` возвращает услугу в live client search.
- Изменение данных услуги обновляет search document.

## Activity заявок на услуги - GET /api/v1/business-admin/branches/{branchId}/activity

|   |   |
|---|---|
|**Описание**|Возвращает рабочую таблицу Activity для заявок по товарам и услугам конкретного филиала.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/business-admin/branches/{branchId}/activity`|
|**Метод запроса**|GET|

### ActivityRowResponse

| № | Поле | Тип | Источник | Комментарий |
|---|---|---|---|---|
|1|`activityId`|uuid|request/target/response||
|2|`type`|string|derived|Only `PRODUCT` or `SERVICE`|
|3|`requestText`|string|customer request||
|4|`branchId`|uuid|branch||
|5|`branchAddress`|string|branch|Separate column|
|6|`customerName`|string|customer profile||
|7|`customerContact`|string|customer profile||
|8|`desiredStartAt`|datetime|service request|nullable|
|9|`status`|string|request/response||
|10|`unreadCount`|integer|messaging||
|11|`actions`|array|derived|Open chat, answer, confirm service|

## Подтверждение заявки на услугу - PATCH /api/v1/business-admin/branches/{branchId}/service-requests/{requestId}

|   |   |
|---|---|
|**Описание**|Бизнес подтверждает, отклоняет или предлагает другое время для заявки на услугу.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/business-admin/branches/{branchId}/service-requests/{requestId}`|
|**Метод запроса**|PATCH|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID филиала|`branchId`|uuid path|+|-||
|2|ID заявки|`requestId`|uuid path|+|-||
|3|Действие|`action`|string body|+|-|`CONFIRM`, `DECLINE`, `SUGGEST_OTHER_TIME`|
|4|Финальное начало|`confirmedStartAt`|datetime body|-|-|Обязательно для CONFIRM если есть время|
|5|Финальный конец|`confirmedEndAt`|datetime body|-|-||
|6|Комментарий бизнеса|`providerNote`|string body|-|-||

### Правила

- Подтверждение создает финальное согласованное время.
- Это не автоматическая бронь слота календаря.
- Если время менялось в чате, бизнес указывает итоговое время явно.

## Пример создания услуги

```http
POST /api/v1/business-admin/branches/branch-uuid-001/services
Authorization: Bearer <token>
Content-Type: application/json

{
  "categoryId": "cat-uuid-repair",
  "name": "Ремонт кассового принтера",
  "description": "Диагностика и ремонт POS-принтеров",
  "basePrice": 15000,
  "durationMinutes": 60,
  "scheduleText": "Ежедневно 10:00-18:00, по подтверждению",
  "active": true
}
```

## Пример ответа создания услуги

```json
{
  "serviceOfferingId": "svc-uuid-001",
  "serviceBranchOfferId": "sbo-uuid-001",
  "branchId": "branch-uuid-001",
  "categoryId": "cat-uuid-repair",
  "name": "Ремонт кассового принтера",
  "description": "Диагностика и ремонт POS-принтеров",
  "basePrice": 15000,
  "durationMinutes": 60,
  "scheduleText": "Ежедневно 10:00-18:00, по подтверждению",
  "active": true,
  "updatedAt": "2026-06-21T10:00:00Z"
}
```

## Пример подтверждения заявки

```http
PATCH /api/v1/business-admin/branches/branch-uuid-001/service-requests/req-uuid-service-001
Authorization: Bearer <token>
Content-Type: application/json

{
  "action": "CONFIRM",
  "confirmedStartAt": "2026-06-21T15:30:00Z",
  "confirmedEndAt": "2026-06-21T16:30:00Z",
  "providerNote": "Можем принять в 15:30"
}
```

## Пример ответа подтверждения

```json
{
  "requestId": "req-uuid-service-001",
  "branchId": "branch-uuid-001",
  "status": "CONFIRMED",
  "confirmedStartAt": "2026-06-21T15:30:00Z",
  "confirmedEndAt": "2026-06-21T16:30:00Z",
  "providerNote": "Можем принять в 15:30"
}
```

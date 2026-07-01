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

## Философия продукта: Chat-First, Button-for-Fixation

Ask Services MVP — это **не календарь бронирования**, а **чат + структурированная фиксация финальной договоренности**.

- Основной способ общения компании с клиентом — **обычный чат Ask**. Кнопки/actions внутри чата нужны не для замены общения, а для **служебной фиксации результата уже достигнутой в чате договоренности**.
- Клиент выбирает желаемое время при заявке → это **requested/desired time**, не гарантированная бронь.
- Компания и клиент общаются в чате. В процессе общения они могут договориться на другое время.
- Когда договоренность достигнута, компания внутри чата фиксирует финальное **confirmedStartAt / confirmedEndAt**.
- Подтверждение/изменение/отмена времени создает **system event в conversation** — видимый и клиенту, и бизнесу.

## Три уровня зрелости услуг

### Level 1: MVP Request-to-Book (текущий Task 04)

- Клиент отправляет заявку с желаемым временем.
- Время является **desired** — не гарантированный слот.
- Компания подтверждает, отклоняет или продолжает обсуждение в чате.
- Нет автоматической гарантии свободного слота.

### Level 2: Minimal Confirmed Appointment Tracking (текущий Task 04)

- После чата компания фиксирует финальное **confirmedStartAt / confirmedEndAt**.
- Это создает подтвержденную запись / confirmed appointment в таблице `booking`.
- Подтвержденный интервал **блокирует будущие suggested time options** для этой услуги/филиала (минимальная проверка пересечений).
- Это НЕ полноценная CRM — нет ресурсов, мастеров, смен, автоматического slot availability.

### Level 3: Future Calendar System (НЕ входит в Task 04)

- Мастера, ресурсы, расписания сотрудников, пересечения, интеграции, автоматическое slot availability.
- **Ничего из этого не реализуется сейчас.**

## Общие правила задачи

- Услуги сохраняются в реальной базе и становятся основой клиентского поиска.
- Каждая текущая регистрация создает конкретный филиал/заведение, услуги относятся к нему.
- У услуги есть состояние показа: активна или не активна.
- Активная услуга попадает в клиентский поиск.
- Неактивная услуга не попадает в live client search.
- Service management and service request handling are branch workspace actions.
- Owner can perform them after selecting a branch.
- Staff can perform them only for assigned branch.
- Do not use Manager/Operator role split for services, Activity, or chat actions.
- MVP не блокирует слоты и не является полноценным календарем.
- Заявка на услугу подтверждается бизнесом вручную после обсуждения в чате.
- Основной канал общения — чат. Actions — фиксация результата.
- Подтверждение/изменение/отмена времени создает system event в conversation.

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
|---|---|---|---|---|---|
|1|`activityId`|uuid|request/target/response||
|2|`type`|string|derived|`PRODUCT` or `SERVICE`|
|3|`requestText`|string|customer request||
|4|`branchId`|uuid|branch||
|5|`branchAddress`|string|branch|Separate column|
|6|`customerName`|string|customer profile||
|7|`customerContact`|string|customer profile||
|8|`requestedStartAt`|datetime|service request|nullable; время, которое клиент указал при заявке|
|9|`proposedStartAt`|datetime|supplier response|nullable; время, предложенное бизнесом через `SUGGEST_OTHER_TIME`|
|10|`confirmedStartAt`|datetime|supplier response|nullable; финально согласованное начало|
|11|`confirmedEndAt`|datetime|supplier response|nullable; финально согласованный конец|
|12|`activityDisplayStatus`|string|derived|`DISCUSSING`, `CONFIRMED`, or `CONFIRMATION_DECLINED` — ТОЛЬКО это поле видит UI|
|13|`customerRequestStatus`|string|raw lifecycle|`CREATED`, `SENT`, `PARTIALLY_RESPONDED`, `COMPLETED`, `EXPIRED`, `CANCELLED`, `FAILED`|
|14|`supplierResponseStatus`|string|raw response|`CAN_PROVIDE`, `CANNOT_PROVIDE`, `NEED_CLARIFICATION`, `SUGGEST_OTHER_TIME`|
|15|`unreadCount`|integer|messaging||
|16|`actions`|array|derived|Open chat, confirm, decline, suggest other time|

### ActivityDisplayStatus — Правила вычисления

`ActivityDisplayStatus` — это **единственный** статус, который видит Activity UI. Он **никогда не хранится** в базе и вычисляется на лету из lifecycle-статуса заявки и статуса ответа поставщика.

| ActivityDisplayStatus | Условие | Что видит бизнес |
|---|---|---|
|`DISCUSSING`|Все случаи кроме двух ниже|Заявка в процессе обсуждения. Бизнес может ответить, подтвердить, отклонить, предложить другое время.|
|`CONFIRMED`|`customerRequestStatus ∈ {COMPLETED, PARTIALLY_RESPONDED}` И `supplierResponseStatus = CAN_PROVIDE` И `confirmedStartAt != null`|Время согласовано. Создана запись в `booking`. Действия: открыть чат.|
|`CONFIRMATION_DECLINED`|`supplierResponseStatus = CANNOT_PROVIDE`|Бизнес отказал. Действия: открыть чат (чат остается доступным).|

**Важно:**
- `CAN_PROVIDE` без `confirmedStartAt` = `DISCUSSING` (бизнес сказал "можем", но время еще не зафиксировано).
- `SUGGEST_OTHER_TIME` = всегда `DISCUSSING` (бизнес предложил другое время — ждет ответа клиента в чате).
- `NEED_CLARIFICATION` = всегда `DISCUSSING` (бизнес задал уточняющий вопрос в чате).
- Подтверждение, изменение, или отмена времени создает **system event в conversation** — видимый и клиенту, и бизнесу.

### Actions

| Action | Когда показывать | Что делает |
|---|---|---|
|Open chat|Всегда|Открывает чат, привязанный к заявке. Основной канал общения.|
|Confirm (`CAN_PROVIDE`)|`supplierResponseStatus != CAN_PROVIDE` И `supplierResponseStatus != CANNOT_PROVIDE`|Бизнес подтверждает возможность оказания услуги и фиксирует `confirmedStartAt`/`confirmedEndAt`. Без времени = `DISCUSSING`. С временем = `CONFIRMED`.|
|Decline (`CANNOT_PROVIDE`)|`supplierResponseStatus != CANNOT_PROVIDE`|Бизнес отказывает. Статус → `CONFIRMATION_DECLINED`.|
|Suggest other time (`SUGGEST_OTHER_TIME`)|`supplierResponseStatus != CAN_PROVIDE` И `supplierResponseStatus != CANNOT_PROVIDE`|Бизнес предлагает другое время через `proposedStartAt`. Оставляет заявку в `DISCUSSING`. Открывает чат для обсуждения.|

## Подтверждение заявки на услугу - PATCH /api/v1/business-admin/branches/{branchId}/service-requests/{requestId}

|   |   |
|---|---|
|**Описание**|Бизнес подтверждает, отклоняет или предлагает другое время для заявки на услугу. Использует `SupplierResponseStatus` напрямую.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/business-admin/branches/{branchId}/service-requests/{requestId}`|
|**Метод запроса**|PATCH|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|---|
|1|ID филиала|`branchId`|uuid path|+|-||
|2|ID заявки|`requestId`|uuid path|+|-||
|3|Статус ответа|`status`|`SupplierResponseStatus` body|+|-|`CAN_PROVIDE`, `CANNOT_PROVIDE`, `NEED_CLARIFICATION`, `SUGGEST_OTHER_TIME`|
|4|Предложенное время|`proposedStartAt`|datetime body|-|-|Обязательно для `SUGGEST_OTHER_TIME`|
|5|Финальное начало|`confirmedStartAt`|datetime body|-|-|Обязательно для `CAN_PROVIDE` если фиксируется время|
|6|Финальный конец|`confirmedEndAt`|datetime body|-|-||
|7|Комментарий бизнеса|`providerNote`|string body|-|-||

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

## Пример подтверждения заявки (с фиксацией времени)

```http
PATCH /api/v1/business-admin/branches/branch-uuid-001/service-requests/req-uuid-service-001
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "CAN_PROVIDE",
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
  "customerRequestStatus": "COMPLETED",
  "supplierResponseStatus": "CAN_PROVIDE",
  "activityDisplayStatus": "CONFIRMED",
  "requestedStartAt": "2026-06-21T14:00:00Z",
  "confirmedStartAt": "2026-06-21T15:30:00Z",
  "confirmedEndAt": "2026-06-21T16:30:00Z",
  "providerNote": "Можем принять в 15:30"
}
```

## Пример предложения другого времени

```http
PATCH /api/v1/business-admin/branches/branch-uuid-001/service-requests/req-uuid-service-001
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "SUGGEST_OTHER_TIME",
  "proposedStartAt": "2026-06-21T17:00:00Z",
  "providerNote": "В 15:30 не можем, предлагаем 17:00"
}
```

## Пример ответа предложения другого времени

```json
{
  "requestId": "req-uuid-service-001",
  "branchId": "branch-uuid-001",
  "customerRequestStatus": "PARTIALLY_RESPONDED",
  "supplierResponseStatus": "SUGGEST_OTHER_TIME",
  "activityDisplayStatus": "DISCUSSING",
  "requestedStartAt": "2026-06-21T14:00:00Z",
  "proposedStartAt": "2026-06-21T17:00:00Z",
  "providerNote": "В 15:30 не можем, предлагаем 17:00"
}
```

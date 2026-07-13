# Backend Task Specifications

Задачи бекенда описываются в формате номерных markdown-файлов в `AI_Knowledge/backend_tasks/`. Каждый файл — это полная спецификация endpoint'ов одной функциональной области.

## Для кого

- **Бекенд-разработчики**: реализуют endpoint'ы по спецификации.
- **Фронтенд-разработчики**: понимают контракты API до начала реализации бекенда.
- **AI-агенты**: используют спецификации как source of truth для генерации кода.

## Структура файла задачи

```text
# Task NN: <Название задачи>

|   |   |
|---|---|
|**Описание**|<1-2 предложения — что делает эта группа endpoint'ов>|
|**Модуль системы**|<список feature-пакетов: catalog, business, search, ...>|

## Зависимости
- Task-и, от которых зависит эта задача
- Сущности: `Business`, `Product`, `ProductOffer`, ...
- Инфраструктурные зависимости: Apache POI, SMS adapter, ...

## Общие правила задачи
- Бизнес-правила, общие для всех endpoint'ов задачи.
- Ограничения и допущения.
- Перечисление, bullet list.

---

## <Endpoint Name> - <METHOD> <URL path>

|   |   |
|---|---|
|**Описание**|<Что делает этот endpoint>|
|**Доступ только авторизованным пользователям**|+ или -|
|**Endpoint URL**|`/api/v1/...`|
|**Метод запроса**|GET / POST / PATCH / DELETE|
|**Content-Type**|application/json или multipart/form-data (если не JSON)|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|<русское описание>|`fieldName`|тип + source|+/-|default|доп. инфо|

Типы параметров: `uuid path`, `string query`, `integer query`, `boolean query`, `string body`, `decimal body`, `array body`, `file form`.

### <ResponseType> (для GET/POST)

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`fieldName`|тип|описание|

### Правила

- Список бизнес-правил, специфичных для этого endpoint.
- Валидация, граничные случаи, сайд-эффекты.

---

## Пример: <Endpoint Name>

```http
METHOD /api/v1/...
Authorization: Bearer <token>
Content-Type: application/json

{ ... }
```

```json
{ ... }
```
```

## Правила написания задач

### Язык
- Заголовки endpoint'ов, описания, правила — **на русском**.
- Имена полей, типы, enum-значения — **на английском** (как в коде).
- HTTP-примеры — на английском.

### Нумерация
- `00` — foundation/infrastructure (поисковые сессии, снапшоты).
- `01-02` — клиентские endpoint'ы (поиск товаров, поиск услуг).
- `03-04` — бизнес-админ endpoint'ы (управление товарами, услугами).
- `05` — identity/auth.
- `06+` — дополнительные фичи (импорт, экспорт, ...).

### Таблицы параметров
- **Обязательно** поле: `№`, `Описание`, `Наименование`, `Тип`, `Обязательно`.
- **Опционально**: `По умолчанию`, `Комментарий`.
- Тип всегда включает source: `uuid path`, `string query`, `string body`, `file form`.

### Таблицы ответов
- Формат: `№`, `Поле`, `Тип`, `Комментарий`.
- Для вложенных объектов указывается отдельная таблица с заголовком типа.
- Enum-ы перечисляются в code block: `VALUE1, VALUE2, VALUE3`.

### HTTP примеры
- Каждый endpoint должен иметь хотя бы один пример запроса и ответа.
- Использовать реалистичные тестовые данные (не foo/bar).
- UUID-ы: `branch-uuid-001`, `prod-uuid-001`.

### Правила валидации и доступа
- Явно указывать, кто имеет доступ: `ANONYMOUS`, `ROLE_CUSTOMER`, `ROLE_BUSINESS_OWNER`, `ROLE_BUSINESS_STAFF`.
- Для бизнес-админ endpoint'ов: указать, что Owner (после выбора филиала) и Staff этого филиала имеют доступ.
- Валидацию описывать как список правил: "X обязательно", "Y должен быть числом".

## Отличия от клиентских контрактов

- **backend_tasks/** — спецификации для реализации. Детальные таблицы параметров, правила, HTTP примеры.
- **client_contracts/** — контракты для фронтенда. Описывают flow, статусы, authority, формат ошибок.
- Дублирование допустимо: клиентский контракт может ссылаться на backend task за деталями endpoint'ов.

## Актуальные задачи

| Task | Файл | Статус |
|---|---|---|
|00|`00_search_session_snapshot_foundation.md`|Спецификация|
|01|`01_client_product_endpoints.md`|Спецификация|
|02|`02_client_service_endpoints.md`|Спецификация|
|03|`03_business_admin_product_endpoints.md`|Спецификация|
|04|`04_business_admin_service_endpoints.md`|Спецификация|
|05|`05_identity_auth_and_session_endpoints.md`|Реализовано|
|06|`06_product_excel_import_endpoints.md`|Спецификация|
|07|`07_meilisearch_search_engine_integration.md`|Спецификация|
|08|`08_public_business_candidate_ingestion.md`|Спецификация|
|09|`09_contact_action_privacy_and_redirects.md`|Спецификация|
|10|`10_brand_storefront_builder_backend.md`|Спецификация|
|11|`11_drops_events_search_indexing.md`|Спецификация|
|12|`12_backend_restructuring_v2.md`|В реализации|

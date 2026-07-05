# Task 08: Public Business Candidate Ingestion (Astana)

|   |   |
|---|---|
|**Описание**|Слой discovery публичных бизнесов Астаны: сбор public business signals из Instagram, Telegram, 2GIS, сайтов без приватного скрапинга и копирования полной истории.|
|**Модуль системы**|business, search|

## Зависимости
- Task 07 (Meilisearch integration)
- Сущности: `Business`, `BusinessBranch`, `BusinessExternalLink`, `BusinessContact`
- Инфраструктура: нет внешних API (ручной/полуавтоматический сбор)

## Общие правила задачи

- Только **публичные бизнес-сигналы**: название, профиль, контакты, ссылки, примеры товаров/услуг.
- **НЕ копировать** полную историю Instagram/Telegram.
- **НЕ скрапить** приватные данные, переписки, закрытые аккаунты.
- Результат: `BusinessExternalLink` записи + business candidates для ручной верификации.
- Источники: 2GIS (публичные карточки), Instagram (публичные бизнес-аккаунты), Telegram (публичные каналы/витрины), сайты (публичные страницы).
- Каждый источник даёт `BusinessExternalLink` с provider type, public URL, confidence level.
- Business candidates создаются как `RecordStatus.PENDING_REVIEW` — не попадают в поиск до верификации.

---

## BusinessExternalLink Entity

### Поля

| Поле | Тип | Назначение |
|---|---|---|
|`id`|UUIDv7|Первичный ключ|
|`businessId`|UUID|Связь с Business (nullable для candidate)|
|`provider`|enum|`TWO_GIS`, `INSTAGRAM`, `TELEGRAM`, `SITE`, `WHATSAPP`|
|`url`|String|Публичный URL / deep-link|
|`displayLabel`|String|Как показывать в UI ("Instagram", "2GIS карточка")|
|`sourceDescription`|String|Откуда взяли ссылку ("публичный профиль Instagram", "карточка 2GIS")|
|`confidence`|enum|`VERIFIED` (подтверждено владельцем), `LIKELY` (высокая вероятность), `UNVERIFIED` (требует проверки)|
|`visibility`|enum|`PUBLIC` (видно всем), `AFTER_CONTACT` (после первого контакта), `INTERNAL` (только внутри платформы)|
|`createdAt`|Timestamp|Дата добавления|

### Provider Enum

```text
TWO_GIS, INSTAGRAM, TELEGRAM, SITE, WHATSAPP
```

### Confidence Enum

```text
VERIFIED, LIKELY, UNVERIFIED
```

### Visibility Enum

```text
PUBLIC, AFTER_CONTACT, INTERNAL
```

---

## Business Candidate Pipeline

### Stage 1: Discovery

Источники:
1. **2GIS** — поиск по категориям (одежда, косметика, спортпит, услуги) в Астане. Публичные карточки содержат: название, адрес, телефон, сайт, Instagram, категорию, фото.
2. **Instagram** — поиск публичных бизнес-аккаунтов по гео-тегам Астаны и хештегам категорий.
3. **Telegram** — публичные каналы-витрины астанинских магазинов.
4. **Сайты** — публичные страницы магазинов (контакты, каталог).

Выход: CSV/JSON файл с public business signals.

### Stage 2: Dedup & Match

`POST /api/v1/admin/business-candidates/ingest` (admin-only):

1. Принять JSON массив business signals.
2. Для каждого сигнала:
   - Проверить `contact_hash` (HMAC телефона/email) на совпадение с существующими бизнесами.
   - Проверить название + город на частичное совпадение.
3. Если найден существующий бизнес → добавить `BusinessExternalLink` к нему.
4. Если не найден → создать `BusinessCandidate` (PENDING_REVIEW).

### Stage 3: Review & Approve

`GET /api/v1/admin/business-candidates?status=PENDING_REVIEW` (admin-only):
- Список candidates для ручной верификации.
- Каждый candidate показывает: source, provider, public URL, suggested name, contacts, category hints.

`POST /api/v1/admin/business-candidates/{candidateId}/approve` (admin-only):
- Создаёт `Business` + `BusinessBranch` из candidate данных.
- Переносит `BusinessExternalLink` на новый бизнес.
- Меняет статус candidate на `APPROVED`.

`POST /api/v1/admin/business-candidates/{candidateId}/reject` (admin-only):
- Меняет статус на `REJECTED` с причиной.

---

## BusinessCandidate Entity

| Поле | Тип | Назначение |
|---|---|---|
|`id`|UUIDv7|Первичный ключ|
|`sourceProvider`|enum|Источник: `TWO_GIS`, `INSTAGRAM`, `TELEGRAM`, `SITE`, `MANUAL`|
|`sourceUrl`|String|URL источника|
|`suggestedName`|String|Предполагаемое название|
|`suggestedCityId`|UUID|Предполагаемый город|
|`suggestedAddress`|String|Предполагаемый адрес|
|`suggestedCategoryHints`|[String]|Предполагаемые категории|
|`contactsJson`|Text|JSON с контактами (телефоны, email, соцсети)|
|`rawSignalsJson`|Text|Полные сырые данные из источника|
|`status`|enum|`PENDING_REVIEW`, `APPROVED`, `REJECTED`, `DUPLICATE`|
|`reviewedBy`|UUID|Кто проверил|
|`reviewedAt`|Timestamp|Когда проверили|
|`rejectionReason`|String|Причина отклонения|

---

## Endpoints Summary

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
|`POST`|`/api/v1/admin/business-candidates/ingest`|Admin|Загрузить public signals|
|`GET`|`/api/v1/admin/business-candidates`|Admin|Список candidates|
|`POST`|`/api/v1/admin/business-candidates/{id}/approve`|Admin|Одобрить → создать Business|
|`POST`|`/api/v1/admin/business-candidates/{id}/reject`|Admin|Отклонить|
|`GET`|`/api/v1/admin/businesses/{id}/external-links`|Admin|Список внешних ссылок бизнеса|
|`POST`|`/api/v1/admin/businesses/{id}/external-links`|Admin|Добавить внешнюю ссылку|
|`DELETE`|`/api/v1/admin/businesses/{id}/external-links/{linkId}`|Admin|Удалить внешнюю ссылку|

---

## Правила

- Business candidate НЕ появляется в клиентском поиске до approve.
- Владелец бизнеса может верифицировать external links через `/api/v1/businesses/{id}/external-links/{linkId}/verify` (меняет confidence → VERIFIED).
- Public discovery не должен нарушать privacy: только публичные данные, никакого скрапинга закрытых профилей.
- Данные из источников — это hints, не truth. Финальный профиль бизнеса заполняет владелец или админ.

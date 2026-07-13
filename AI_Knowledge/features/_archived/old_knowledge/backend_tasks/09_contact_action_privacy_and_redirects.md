# Task 09: Contact Action Privacy And Redirects

|   |   |
|---|---|
|**Описание**|Privacy-safe модель контактов: HMAC для дедупликации, encrypted vault для хранения, contactActionId для client-safe раскрытия.|
|**Модуль системы**|business, identity, shared|

## Зависимости
- Task 05 (identity/auth), Task 08 (public business ingestion)
- Сущности: `BusinessContact`, `BusinessExternalLink`, `AppUser`
- Инфраструктура: AES encryption (JCE), HMAC-SHA256

## Общие правила задачи

- **`contact_hash` / HMAC** — только для дедупликации и safe matching. Математически необратим.
- **Encrypted contact vault** — реальное значение контакта хранится AES-encrypted. Расшифровывается только на сервере при авторизованном действии.
- **`contactActionId`** — что получает frontend. Одноразовый или короткоживущий токен, который backend резолвит в redirect/deep-link или safe display value.
- **Public URL / deep-link** — для Instagram, Telegram, 2GIS, WhatsApp, сайта. Хранится в `BusinessExternalLink`.
- Frontend НИКОГДА не получает raw phone/username, если только backend явно не маркирует его как публичное.
- Contact hash — внутренняя инфраструктура, никогда не показывается пользователям.

---

## Contact Vault Architecture

```text
User/Business provides contact (phone/email/username)
  → Backend computes HMAC-SHA256(contact_value, secret_key) → contact_hash
  → Backend encrypts contact_value with AES-256-GCM → encrypted_contact_value
  → Both stored in business_contact table

On contact action:
  → Frontend sends contactActionId
  → Backend resolves: decrypts contact_value OR builds redirect URL
  → Backend returns redirect/deep-link OR safe display value
  → Backend logs access (who, when, which contact, which action)
```

---

## BusinessContact Changes

### Current Fields
- `id`, `businessId`, `contactType` (PHONE/EMAIL/INSTAGRAM/TELEGRAM/WHATSAPP/SITE), `contactValue`, `isPrimary`

### New/Modified Fields

| Поле | Тип | Назначение |
|---|---|---|
|`contact_hash`|String(64)|HMAC-SHA256 хеш для дедупликации|
|`encrypted_value`|Text|AES-256-GCM зашифрованное значение|
|`display_value`|String|Публичное отображение (если разрешено)|
|`visibility`|enum|`PUBLIC`, `AFTER_CONTACT`, `INTERNAL`|
|`contactActionId`|UUID|Генерируется на лету для client-side действий|

### ContactType Enum (updated)

```text
PHONE, EMAIL, INSTAGRAM, TELEGRAM, WHATSAPP, SITE, TWO_GIS
```

### Visibility Enum

```text
PUBLIC           — видно всем в карточке бизнеса
AFTER_CONTACT    — видно после первого чата/заявки
INTERNAL         — только внутри платформы, никогда не показывается
```

---

## Contact Action Endpoints

### `POST /api/v1/contacts/{contactActionId}/resolve`

|   |   |
|---|---|
|**Описание**|Резолвит contactActionId в redirect URL или display value|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/contacts/{contactActionId}/resolve`|
|**Метод запроса**|POST|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID контактного действия|`contactActionId`|uuid path|+|—|Из карточки результата|

### ContactResolveResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`actionType`|String|`REDIRECT`, `DISPLAY`, `DEEP_LINK`, `CHAT`|
|2|`redirectUrl`|String|URL для редиректа (если REDIRECT)|
|3|`deepLink`|String|Deep link для приложения (если DEEP_LINK)|
|4|`displayValue`|String|Безопасное отображаемое значение (если DISPLAY)|
|5|`provider`|String|`TELEGRAM`, `INSTAGRAM`, `WHATSAPP`, `TWO_GIS`, `SITE`, `PHONE`, `ASK_CHAT`|
|6|`label`|String|Человекочитаемая метка ("Написать в WhatsApp", "Открыть 2GIS")|
|7|`expiresAt`|Timestamp|Когда токен истекает|

### Правила

- contactActionId генерируется на лету при формировании search response / business page.
- Токен живёт 30 минут.
- Один токен = одно действие (одноразовый или с ограничением повторов).
- Backend логирует resolve: who, when, which contact, which action.
- Для `ASK_CHAT`: создаёт conversation и возвращает chatId (или существующий).
- Для внешних каналов: если `encrypted_value` существует → расшифровывает → строит deep link (tg://, https://wa.me/, instagram://, 2gis://).
- Для `SITE`/`TWO_GIS`: просто возвращает public URL без расшифровки.

---

## BusinessExternalLink Management

### `POST /api/v1/businesses/{businessId}/external-links`

|   |   |
|---|---|
|**Описание**|Добавить внешнюю ссылку бизнеса|
|**Доступ только авторизованным пользователям**|+ (Owner)|
|**Endpoint URL**|`/api/v1/businesses/{businessId}/external-links`|
|**Метод запроса**|POST|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | Комментарий |
|---|---|---|---|---|---|
|1|Провайдер|`provider`|string body|+|`TWO_GIS`, `INSTAGRAM`, `TELEGRAM`, `SITE`, `WHATSAPP`|
|2|URL|`url`|string body|+|Публичная ссылка|
|3|Метка|`displayLabel`|string body|-|Как показывать в UI|
|4|Видимость|`visibility`|string body|+|`PUBLIC`, `AFTER_CONTACT`, `INTERNAL`|

### `POST /api/v1/businesses/{businessId}/external-links/{linkId}/verify`

|   |   |
|---|---|
|**Описание**|Верифицировать внешнюю ссылку (меняет confidence → VERIFIED)|
|**Доступ только авторизованным пользователям**|+ (Owner)|

---

## Конфигурация Encryption

```yaml
ask:
  contact:
    hmac-secret: ${ASK_CONTACT_HMAC_SECRET}  # min 32 bytes, from env
    encryption-key: ${ASK_CONTACT_ENCRYPTION_KEY}  # AES-256 key, from env
    action-token-ttl: 30m
```

---

## Миграция существующих контактов

При деплое:
1. Для каждого существующего `BusinessContact` с `contactValue`:
   - Вычислить `contact_hash = HMAC-SHA256(contactValue, hmacSecret)`.
   - Зашифровать `contactValue` → `encrypted_value`.
   - Если `contactType = PHONE` → `visibility = AFTER_CONTACT`.
   - Если `contactType = INSTAGRAM/TELEGRAM/SITE` → создать `BusinessExternalLink` с `url = contactValue`.
2. Старое поле `contactValue` оставить на переходный период, пометить `@Deprecated`.
3. Клиентские API возвращают только `displayValue` и `contactActionId`, никогда `contactValue`.

---

## Пример: Resolve Contact Action

```http
POST /api/v1/contacts/contact-action-uuid-001/resolve
Authorization: Bearer <customer-token>
```

```json
{
  "actionType": "DEEP_LINK",
  "redirectUrl": null,
  "deepLink": "tg://resolve?domain=vintage_room_astana",
  "displayValue": null,
  "provider": "TELEGRAM",
  "label": "Написать в Telegram",
  "expiresAt": "2026-07-04T12:30:00Z"
}
```

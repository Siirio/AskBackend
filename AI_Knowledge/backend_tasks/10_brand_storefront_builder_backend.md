# Task 10: Brand Storefront Builder Backend

|   |   |
|---|---|
|**Описание**|Storefront builder backend: BrandProfile, StorefrontPage, StorefrontBlock, BrandKit. Бренд собирает мини-сайт из готовых блоков, сохраняет как published/draft версию.|
|**Модуль системы**|business|

## Зависимости
- Task 03 (business admin product endpoints), Task 07 (Meilisearch), Task 11 (drops indexing)
- Сущности: `Business`, `BrandProfile`, `StorefrontPage`, `StorefrontBlock`
- Инфраструктура: нет (файлы изображений через общий file storage)

## Общие правила задачи

- Storefront = constrained Canva-like builder, NOT free-form Webflow.
- Бренд собирает страницу из готовых блоков: Hero, Products, Drops, About, Lookbook, Branches, Contacts, FAQ, Promo, "Why this matches".
- Две версии: `draft` (редактируется) и `published` (видна клиентам).
- Publish копирует draft → published. Клиенты видят только published.
- Блоки имеют `displayOrder`, `blockType`, `configJson` (блок-специфичная конфигурация).
- BrandKit: цвет, лого, обложка, tone of voice, описание, ссылки.

---

## BrandProfile Entity

| Поле | Тип | Назначение |
|---|---|---|
|`id`|UUIDv7|Первичный ключ|
|`businessId`|UUID|1:1 с Business|
|`brandColor`|String(7)|Hex цвет бренда|
|`logoUrl`|String|URL логотипа|
|`coverUrl`|String|URL обложки|
|`toneOfVoice`|String|Tone of voice|
|`description`|String|Описание бренда|
|`instagramUrl`|String|Instagram (nullable)|
|`telegramUrl`|String|Telegram (nullable)|
|`websiteUrl`|String|Сайт (nullable)|
|`createdAt`|Timestamp||
|`updatedAt`|Timestamp||

---

## StorefrontBlock Entity

| Поле | Тип | Назначение |
|---|---|---|
|`id`|UUIDv7|Первичный ключ|
|`storefrontPageId`|UUID|Привязка к странице|
|`blockType`|enum|Тип блока|
|`displayOrder`|Integer|Порядок отображения|
|`configJson`|Text|JSON конфигурация блока|
|`enabled`|Boolean|Показывать блок|

### BlockType Enum

```text
HERO, PRODUCTS, DROPS, ABOUT, LOOKBOOK, BRANCHES, CONTACTS, FAQ, PROMO, WHY_THIS_MATCHES
```

### configJson Examples

**HERO:**
```json
{
  "title": "Vintage Room",
  "subtitle": "Винтажная одежда из Европы и США",
  "coverUrl": "https://cdn.ask.kz/covers/vintage-room.jpg",
  "ctaText": "Смотреть товары",
  "ctaAction": "scroll_to_products"
}
```

**PRODUCTS:**
```json
{
  "title": "Популярные товары",
  "productIds": ["uuid-1", "uuid-2", "uuid-3"],
  "displayMode": "grid",
  "maxItems": 8
}
```

**DROPS:**
```json
{
  "title": "Актуальные дропы",
  "showActive": true,
  "showUpcoming": true,
  "maxItems": 4
}
```

**BRANCHES:**
```json
{
  "title": "Где нас найти",
  "showMap": true,
  "branchIds": ["uuid-1", "uuid-2"]
}
```

**WHY_THIS_MATCHES:**
```json
{
  "title": "Почему мы подходим под ваш запрос",
  "autoGenerate": true,
  "customReasons": ["Локальный бренд с 2018", "Только оригинальный винтаж"]
}
```

---

## StorefrontPage Entity

| Поле | Тип | Назначение |
|---|---|---|
|`id`|UUIDv7|Первичный ключ|
|`businessId`|UUID|Связь с бизнесом|
|`version`|String|`draft` или `published`|
|`publishedAt`|Timestamp|Когда опубликована|
|`createdAt`|Timestamp||
|`updatedAt`|Timestamp||

---

## Endpoints

### Brand Profile

### `GET /api/v1/businesses/{businessId}/brand-profile`

|   |   |
|---|---|
|**Описание**|Публичный профиль бренда|
|**Доступ только авторизованным пользователям**|-|
|**Endpoint URL**|`/api/v1/businesses/{businessId}/brand-profile`|
|**Метод запроса**|GET|

### BrandProfileResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`businessId`|UUID||
|2|`businessName`|String||
|3|`brandColor`|String|Hex цвет|
|4|`logoUrl`|String||
|5|`coverUrl`|String||
|6|`description`|String||
|7|`toneOfVoice`|String||
|8|`instagramUrl`|String|nullable|
|9|`telegramUrl`|String|nullable|
|10|`websiteUrl`|String|nullable|

### `PUT /api/v1/businesses/{businessId}/brand-profile`

|   |   |
|---|---|
|**Описание**|Обновить профиль бренда|
|**Доступ только авторизованным пользователям**|+ (Owner)|
|**Endpoint URL**|`/api/v1/businesses/{businessId}/brand-profile`|
|**Метод запроса**|PUT|

### Storefront

### `GET /api/v1/businesses/{businessId}/storefront`

|   |   |
|---|---|
|**Описание**|Публичная страница магазина (published version)|
|**Доступ только авторизованным пользователям**|-|
|**Endpoint URL**|`/api/v1/businesses/{businessId}/storefront`|
|**Метод запроса**|GET|

### StorefrontResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`businessId`|UUID||
|2|`brandProfile`|BrandProfileResponse||
|3|`blocks`|[StorefrontBlockResponse]|Упорядоченные enabled блоки|
|4|`publishedAt`|Timestamp||

### StorefrontBlockResponse

| № | Поле | Тип | Комментарий |
|---|---|---|---|
|1|`blockId`|UUID||
|2|`blockType`|String|HERO, PRODUCTS, DROPS, ...|
|3|`displayOrder`|Integer||
|4|`config`|Object|Блок-специфичная конфигурация|

### `GET /api/v1/businesses/{businessId}/storefront/draft`

|   |   |
|---|---|
|**Описание**|Черновик страницы магазина (для владельца)|
|**Доступ только авторизованным пользователям**|+ (Owner)|

### `PUT /api/v1/businesses/{businessId}/storefront/draft`

|   |   |
|---|---|
|**Описание**|Сохранить черновик (полная замена блоков)|
|**Доступ только авторизованным пользователям**|+ (Owner)|
|**Endpoint URL**|`/api/v1/businesses/{businessId}/storefront/draft`|
|**Метод запроса**|PUT|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | Комментарий |
|---|---|---|---|---|---|
|1|Блоки|`blocks`|array body|+|Массив блоков с blockType, displayOrder, configJson, enabled|

### `POST /api/v1/businesses/{businessId}/storefront/publish`

|   |   |
|---|---|
|**Описание**|Опубликовать draft → published|
|**Доступ только авторизованным пользователям**|+ (Owner)|
|**Endpoint URL**|`/api/v1/businesses/{businessId}/storefront/publish`|
|**Метод запроса**|POST|

---

## Endpoints Summary

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
|`GET`|`/api/v1/businesses/{businessId}/brand-profile`|No|Публичный профиль бренда|
|`PUT`|`/api/v1/businesses/{businessId}/brand-profile`|Owner|Обновить профиль бренда|
|`GET`|`/api/v1/businesses/{businessId}/storefront`|No|Опубликованная страница|
|`GET`|`/api/v1/businesses/{businessId}/storefront/draft`|Owner|Черновик страницы|
|`PUT`|`/api/v1/businesses/{businessId}/storefront/draft`|Owner|Сохранить черновик|
|`POST`|`/api/v1/businesses/{businessId}/storefront/publish`|Owner|Опубликовать|

---

## Правила

- Только Owner управляет brand profile и storefront.
- Staff видят storefront в read-only режиме (предпросмотр).
- Publish копирует все блоки из draft → published. Старые published блоки удаляются.
- Клиенты всегда видят published версию.
- Если published версии нет → `GET /storefront` возвращает 404.
- Brand color из BrandProfile используется в search result cards как accent.
- При изменении brandColor/logoUrl → триггерится обновление search документов в Meilisearch.

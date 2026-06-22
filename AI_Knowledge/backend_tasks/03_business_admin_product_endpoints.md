# Task 03: Business Cabinet Product Endpoints

|   |   |
|---|---|
|**Описание**|Endpoint'ы бизнес-кабинета для реального сохранения товаров конкретного зарегистрированного филиала/магазина.|
|**Модуль системы**|catalog, business, search|

## Зависимости

- `Task 05: Identity Auth And Session Endpoints`
- `Business`, `BusinessMember`, `BusinessBranch`, `BusinessContact`
- `Category`, `Product`, `ProductOffer`, `SearchDocument`

## Общие правила задачи

- Бизнес onboarding production-facing: добавленные товары сохраняются в реальной базе.
- Каждая текущая регистрация создает конкретный филиал/магазин, и товары относятся к нему.
- Товар не имеет отдельного бизнес-статуса вроде `Нужно обновить`.
- Управление товаром: `Редактировать`, `Выключить`, `Включить`, `Удалить`.
- Включенный товар попадает в клиентский поиск.
- Выключенный или удаленный товар не попадает в клиентский live search.
- Product management is a branch workspace action.
- Both Owner, after selecting a branch, and Staff assigned to that branch can manage products according to current branch workspace permissions.
- There is no Manager/Operator permission split for product management.
- Инвентарный учет количества не входит в MVP.
- Актуальность определяется действием бизнеса: пока товар включен, он актуален для показа.

## Список товаров филиала - GET /api/v1/business-admin/branches/{branchId}/products

|   |   |
|---|---|
|**Описание**|Возвращает товары конкретного филиала.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/business-admin/branches/{branchId}/products`|
|**Метод запроса**|GET|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID филиала|`branchId`|uuid path|+|-||
|2|ID категории|`categoryId`|uuid query|-|-||
|3|Состояние показа|`enabled`|boolean query|-|-||
|4|Поиск|`query`|string query|-|-|name, SKU, tags|
|5|Страница|`page`|integer query|-|0||
|6|Размер|`size`|integer query|-|20||

### BusinessProductRowResponse

| № | Поле | Тип | Источник | Комментарий |
|---|---|---|---|---|
|1|`productId`|uuid|product||
|2|`productOfferId`|uuid|product offer||
|3|`branchId`|uuid|branch||
|4|`categoryId`|uuid|category||
|5|`name`|string|product||
|6|`description`|string|product||
|7|`sku`|string|product|nullable|
|8|`tags`|array|product||
|9|`price`|decimal|product offer|nullable|
|10|`enabled`|boolean|product offer|Controls live search visibility|
|11|`updatedAt`|datetime|entity audit||

## Создание товара - POST /api/v1/business-admin/branches/{branchId}/products

|   |   |
|---|---|
|**Описание**|Создает товар и филиальный offer для текущего филиала.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/business-admin/branches/{branchId}/products`|
|**Метод запроса**|POST|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID филиала|`branchId`|uuid path|+|-||
|2|ID категории|`categoryId`|uuid body|+|-||
|3|Название|`name`|string body|+|-||
|4|Описание|`description`|string body|-|-||
|5|SKU|`sku`|string body|-|-||
|6|Теги|`tags`|array body|-|-|Для smart search|
|7|Цена|`price`|decimal body|-|-||
|8|Включен|`enabled`|boolean body|-|true||

### Правила

- Созданный включенный товар сразу становится доступен для клиентского поиска после обновления search document.
- Search document строится по названию, описанию, тегам, категории, бизнесу, филиалу и цене.
- Не создавать variant tables.
- Не запрашивать stock quantity.

## Обновление товара - PATCH /api/v1/business-admin/branches/{branchId}/products/{productId}

|   |   |
|---|---|
|**Описание**|Обновляет данные товара и цену/показ филиального offer.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/business-admin/branches/{branchId}/products/{productId}`|
|**Метод запроса**|PATCH|

### Параметры

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID филиала|`branchId`|uuid path|+|-||
|2|ID товара|`productId`|uuid path|+|-||
|3|ID категории|`categoryId`|uuid body|-|-||
|4|Название|`name`|string body|-|-||
|5|Описание|`description`|string body|-|-||
|6|SKU|`sku`|string body|-|-||
|7|Теги|`tags`|array body|-|-||
|8|Цена|`price`|decimal body|-|-||
|9|Включен|`enabled`|boolean body|-|-||

### Правила

- Изменение названия, описания, тегов, категории или цены обновляет search document.
- `enabled=false` выключает товар из live client search.
- `enabled=true` возвращает товар в live client search.

## Удаление товара - DELETE /api/v1/business-admin/branches/{branchId}/products/{productId}

|   |   |
|---|---|
|**Описание**|Мягко удаляет товар/offer из филиала и убирает из live search.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/business-admin/branches/{branchId}/products/{productId}`|
|**Метод запроса**|DELETE|

### Правила

- Не физически удалять историю, если товар участвовал в запросах или ответах.
- Live search больше не должен возвращать удаленный товар.
- Исторические snapshots продолжают показывать зафиксированные данные с маркировкой historical.

## Пример создания товара

```http
POST /api/v1/business-admin/branches/branch-uuid-001/products
Authorization: Bearer <token>
Content-Type: application/json

{
  "categoryId": "cat-uuid-pos",
  "name": "Mercury MPRINT G80",
  "description": "Чековый принтер для кассы",
  "sku": "MPRINT-G80",
  "tags": ["чековый принтер", "касса", "pos"],
  "price": 99000,
  "enabled": true
}
```

## Пример ответа создания товара

```json
{
  "productId": "prod-uuid-001",
  "productOfferId": "po-uuid-001",
  "branchId": "branch-uuid-001",
  "categoryId": "cat-uuid-pos",
  "name": "Mercury MPRINT G80",
  "description": "Чековый принтер для кассы",
  "sku": "MPRINT-G80",
  "tags": ["чековый принтер", "касса", "pos"],
  "price": 99000,
  "enabled": true,
  "updatedAt": "2026-06-21T10:00:00Z"
}
```

## Пример выключения товара

```http
PATCH /api/v1/business-admin/branches/branch-uuid-001/products/prod-uuid-001
Authorization: Bearer <token>
Content-Type: application/json

{
  "enabled": false
}
```

## Пример ответа выключения товара

```json
{
  "productId": "prod-uuid-001",
  "productOfferId": "po-uuid-001",
  "branchId": "branch-uuid-001",
  "enabled": false,
  "updatedAt": "2026-06-21T11:00:00Z"
}
```

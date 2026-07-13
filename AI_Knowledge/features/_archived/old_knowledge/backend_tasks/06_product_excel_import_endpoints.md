# Task 06: Product Excel Import Endpoints (Implemented)

|   |   |
|---|---|
|**Описание**|Endpoint'ы бизнес-кабинета для импорта товаров из Excel-файла. Бекенд владеет парсингом, auto-mapping, нормализацией, валидацией и созданием товаров. Фронтенд отправляет файл и mapping, получает готовые превью и результаты.|
|**Модуль системы**|catalog, business, search|
|**Статус**|Реализовано на `feature/T7-test-excel-import`|

## Зависимости

- `Task 05: Identity Auth And Session Endpoints`
- `Task 03: Business Cabinet Product Endpoints`
- `Business`, `BusinessBranch`, `BusinessMember`
- `Product`, `ProductOffer`, `SearchDocument`
- `fastexcel-reader` (org.dhatim:fastexcel-reader:0.18.4) для парсинга `.xlsx`

## Архитектура

Импорт следует цепочке слоев: `Controller` → `Processor` → `Service` → `Repository`.

- **CatalogImportController** — 5 REST endpoint'ов
- **ProductImportProcessor** — @Transactional границы, проверка доступа (`isOwnerOrStaffOfBranch`), разрешение branch/import
- **ProductImportServiceImpl** — оркестрация: parse→store→map→normalize→preview→approve/cancel
- **ExcelParser** — fastexcel-парсинг первого sheet, header row → columns, остальные строки → List<Map<String,String>>
- **AutoMappingEngine** — сопоставление названий колонок → TargetField с confidence (0.0–1.0)
- **RowNormalizer** — нормализация сырых строк через mapping, валидация, статус VALID/WARNING/INVALID
- **CatalogImportServiceImpl** — CRUD для CatalogImport, CatalogImportColumnMapping, RawCatalogRow

## Endpoints

Базовый путь: `/api/v1/business-admin/branches/{branchId}/product-imports`

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/product-imports` | Загрузка .xlsx, парсинг, auto-map, сохранение raw rows |
| `POST` | `/product-imports/{importId}/mapping` | Сохранение mapping, нормализация, возврат preview |
| `GET` | `/product-imports/{importId}/preview` | Получение текущего preview |
| `POST` | `/product-imports/{importId}/approve` | Создание Product + ProductOffer + SearchDocument |
| `POST` | `/product-imports/{importId}/cancel` | Отмена импорта |

Все endpoint'ы требуют авторизацию. Доступ: Owner и Staff филиала.

## Flow

1. **Upload** — фронтенд отправляет .xlsx файл. Бекенд парсит Excel, генерирует auto-mappings, сохраняет сырые строки в `raw_catalog_row`, возвращает `UploadResponse` с `importId`, колонками, sample rows (до 3) и статусом `MAPPING_REQUIRED`.
2. **Map** — фронтенд отправляет финальный `MappingRequest` (массив sourceColumn → targetField + characteristicName). Бекенд заменяет mapping'и, нормализует все строки, вычисляет статусы VALID/WARNING/INVALID, возвращает `PreviewResponse` со статусом `PREVIEW_READY`.
3. **Preview** — GET-эндпоинт для повторного получения preview без изменения mapping.
4. **Approve** — бекенд создает `Product` + `ProductOffer` для текущего филиала для каждой VALID/WARNING строки, обновляет `SearchDocument`, возвращает `ApproveResponse` со статусом `IMPORTED`.
5. **Cancel** — бекенд помечает импорт как `CANCELLED`.

## Upload — POST /product-imports

|   |   |
|---|---|
|**Content-Type**|multipart/form-data|
|**Response**|201 Created|

### Параметры

| № | Описание | Наименование | Тип | Обязательно |
|---|---|---|---|---|
|1|ID филиала|`branchId`|uuid path|+|
|2|Excel файл|`file`|file form|+ (.xlsx)|

### UploadResponse

| Поле | Тип | Комментарий |
|---|---|---|
|`importId`|UUID|ID созданного импорта|
|`originalFileName`|String|Имя загруженного файла|
|`status`|String|`MAPPING_REQUIRED`|
|`totalRows`|int|Количество строк данных|
|`columns`|ColumnInfo[]|Колонки с auto-mapping|
|`sampleRows`|Map<String,String>[]|До 3 первых строк|

### ColumnInfo

| Поле | Тип | Комментарий |
|---|---|---|
|`sourceColumn`|String|Название колонки из Excel|
|`suggestedTargetField`|String|Auto-suggested TargetField|
|`confidence`|double|0.0–1.0 уверенность сопоставления|

### Правила

- Принимаются только `.xlsx` файлы.
- Пустой файл → `IMPORT_EMPTY_FILE`.
- Читается первый sheet. Первая строка — заголовки.
- Пустые колонки и строки пропускаются.
- Auto-mapping применяется ко всем колонкам.

### Пример ответа

```json
{
  "importId": "019b4e8c-1234-7890-abcd-ef1234567890",
  "originalFileName": "products.xlsx",
  "status": "MAPPING_REQUIRED",
  "totalRows": 3,
  "columns": [
    { "sourceColumn": "Название", "suggestedTargetField": "NAME", "confidence": 1.0 },
    { "sourceColumn": "Категория", "suggestedTargetField": "CATEGORY_LABEL", "confidence": 1.0 },
    { "sourceColumn": "Цена", "suggestedTargetField": "PRICE", "confidence": 1.0 },
    { "sourceColumn": "Остаток", "suggestedTargetField": "IGNORE", "confidence": 1.0 }
  ],
  "sampleRows": [
    { "Название": "Mammut Whey Protein", "Категория": "Спортпит", "Цена": "32000", "Остаток": "15" }
  ]
}
```

## Mapping — POST /product-imports/{importId}/mapping

|   |   |
|---|---|
|**Content-Type**|application/json|

### MappingRequest

| Поле | Тип | Обязательно |
|---|---|---|
|`mappings`|MappingEntry[]|+|

### MappingEntry

| Поле | Тип | Комментарий |
|---|---|---|
|`sourceColumn`|String|Название колонки из Excel|
|`targetField`|TargetField|Выбранное пользователем поле|
|`characteristicName`|String|Имя характеристики (для CHARACTERISTIC)|

### PreviewResponse

| Поле | Тип | Комментарий |
|---|---|---|
|`importId`|UUID|ID импорта|
|`status`|String|`PREVIEW_READY`|
|`totalRows`|int|Всего строк|
|`validRows`|int|Готовы к импорту|
|`invalidRows`|int|Пропущены (нет названия)|
|`warningRows`|int|Импортированы с предупреждениями|
|`mappings`|ColumnMappingInfo[]|Примененные mapping'и|
|`rows`|RowPreview[]|Строки preview|

### ColumnMappingInfo

| Поле | Тип |
|---|---|
|`sourceColumn`|String|
|`targetField`|TargetField|
|`characteristicName`|String (nullable)|
|`approved`|boolean|
|`confidence`|double|

### RowPreview

| Поле | Тип |
|---|---|
|`rowId`|UUID|
|`rowNumber`|int|
|`status`|VALID / INVALID / WARNING|
|`normalizedData`|Map<String,String>|
|`errors`|String[]|
|`warnings`|String[]|

### Normalized Data Keys

| Key | Source |
|-----|--------|
|`NAME`|TargetField.NAME|
|`CATEGORY_LABEL`|TargetField.CATEGORY_LABEL|
|`DESCRIPTION`|TargetField.DESCRIPTION|
|`SKU`|TargetField.SKU|
|`PRICE`|TargetField.PRICE|
|`TAGS`|TargetField.TAGS (split by comma/semicolon)|
|`CHAR_<name>`|TargetField.CHARACTERISTIC (prefix CHAR_)|
|`APPEND_<name>`|TargetField.APPEND_TO_DESCRIPTION (prefix APPEND_)|

## Preview — GET /product-imports/{importId}/preview

Возвращает `PreviewResponse` (такой же как после mapping) без изменения данных.

## Approve — POST /product-imports/{importId}/approve

### ApproveResponse

| Поле | Тип |
|---|---|
|`importId`|UUID|
|`status`|String (`IMPORTED`)|
|`productsCreated`|int|
|`offersCreated`|int|
|`rowsSkipped`|int|

### Правила

- Только статус `PREVIEW_READY` → можно approve. Иначе `IMPORT_INVALID_STATUS`.
- VALID и WARNING строки создают Product + ProductOffer.
- INVALID и PENDING строки пропускаются.
- Строки без `NAME` в normalized data пропускаются.
- `Product.categoryLabel` = свободный текст, без FK.
- `Product.characteristicsJson` = JSON Map характеристик.
- `ProductOffer.enabled = true`.
- `SearchDocument` создается/обновляется для каждого offer с токенами из name, categoryLabel, description, SKU, tags, characteristics, business/branch name.
- Операция в одной транзакции.

## Cancel — POST /product-imports/{importId}/cancel

### CancelResponse

| Поле | Тип |
|---|---|
|`importId`|UUID|
|`status`|String (`CANCELLED`)|

### Правила

- Статус `IMPORTED` → нельзя отменить (`IMPORT_INVALID_STATUS`).

## TargetField Enum

```
NAME, CATEGORY_LABEL, DESCRIPTION, SKU, PRICE, TAGS, IGNORE, APPEND_TO_DESCRIPTION, CHARACTERISTIC
```

## CatalogImportStatus Enum

```
UPLOADED, MAPPING_REQUIRED, PREVIEW_READY, IMPORTED, FAILED, CANCELLED
```

Переходы: UPLOADED → MAPPING_REQUIRED (после parse) → PREVIEW_READY (после mapping) → IMPORTED (после approve) или CANCELLED (после cancel).

## RawRowStatus Enum

```
PENDING, VALID, WARNING, INVALID
```

## Auto-Mapping Правила

### Основные паттерны (30+ на каждый TargetField)

| TargetField | Примеры паттернов |
|---|---|
|`NAME`|название, наименование, название товара, наименование товара, продукт, имя товара, name, product, title|
|`CATEGORY_LABEL`|категория, категория товара, группа, группа товара, тип товара, раздел, category, group|
|`DESCRIPTION`|описание, описание товара, краткое описание, description, info|
|`SKU`|артикул, код товара, код, шк, штрихкод, sku, article, barcode, арт|
|`PRICE`|цена, цена продажи, розничная цена, розница, стоимость, price, cost|
|`TAGS`|теги, тэги, метки, ключевые слова, tags, labels, keywords|

### Forced IGNORE

Эти колонки всегда получают `IGNORE` независимо от частичного совпадения:

остаток, наличие, количество, склад, stock, quantity, warehouse, available, availability

### Алгоритм сопоставления

1. Точное совпадение с паттерном → confidence 1.0
2. Частичное совпадение → confidence по длине совпадения
3. Неопознанные колонки → `IGNORE` с confidence 0.0

## Валидация (RowNormalizer)

| Правило | Результат |
|---------|-----------|
|`NAME` отсутствует или пустой | Статус INVALID |
|`PRICE` не парсится как число | Статус WARNING, price = null |
|`TAGS` разбиваются по запятой или точке с запятой | |
|`CHARACTERISTIC` → ключ `CHAR_<name>` | |
|`APPEND_TO_DESCRIPTION` → ключ `APPEND_<name>` | |

## Доступ

- `isOwnerOrStaffOfBranch(branchId, userId)` — Owner бизнеса или Staff филиала.
- Проверка в каждом методе `ProductImportProcessor`.
- Approve доступен и Owner, и Staff (не ограничен Owner).

## Связанные файлы

### Новые (26 файлов)

- `V2__product_import.sql` — миграция
- `TargetField.java`, `RawRowStatus.java` — enums
- `ExcelParser.java`, `AutoMappingEngine.java`, `RowNormalizer.java` — domain utilities
- `CatalogImportService.java`, `CatalogImportServiceImpl.java` — CRUD сервис
- `ProductImportService.java`, `ProductImportServiceImpl.java` — оркестрация
- `CatalogImportMapper.java` — entity ↔ DTO
- `ProductImportProcessor.java` — @Transactional границы
- `CatalogImportController.java` — REST endpoint'ы
- 7 Repository interfaces
- 5 DTO files (UploadResponse, MappingRequest, PreviewResponse, ApproveResponse, CancelResponse)

### Измененные (9 файлов)

- `CatalogImportStatus.java`, `Product.java`, `CatalogImport.java`, `CatalogImportColumnMapping.java`, `RawCatalogRow.java`, `SearchDocument.java` — entity changes
- `ErrorCode.java` — 7 новых кодов
- `BusinessService.java` + `BusinessServiceImpl.java` — resolveDataSource
- `pom.xml` — fastexcel-reader dependency

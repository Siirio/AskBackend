# Task 05: Identity Auth, Real Verification, And Business Onboarding

|   |   |
|---|---|
|**Описание**|Production-facing авторизация и онбординг: клиент/бизнес регистрируются или входят по email или телефону, подтверждают контакт 6-значным кодом, получают сессию, а бизнес-регистрация создает реальный филиал/магазин для дальнейшего добавления товаров и услуг.|
|**Модуль системы**|identity, business|

## Зависимости

- `AppUser`, `CustomerProfile`
- `Business`, `BusinessMember`, `BusinessBranch`, `BusinessContact`
- auth challenge storage
- auth session/token storage
- password hashing
- real email verification adapter
- SMS adapter when phone verification is enabled
- persistent production database

## Общие правила задачи

- Клиент может регистрироваться и входить по email или телефону.
- Бизнес может регистрироваться и входить по email или телефону.
- Если указан email, телефон не обязателен.
- Если указан телефон, email не обязателен.
- Подтверждается выбранный login contact.
- Email verification должна быть реальной.
- Phone verification использует SMS, когда SMS adapter включен.
- WhatsApp/Telegram можно оставить будущими каналами, но не выдавать за работающие без адаптера.
- Business registration создает конкретный филиал/магазин.
- Регистрационный email или телефон становится начальным публичным контактом филиала.
- Один бизнес в будущем может управлять несколькими филиалами; у каждого филиала свои контакты.
- Данные onboarding не должны очищаться после демо или тестовой выдачи сайта.

---

## Регистрация клиента - POST /api/v1/auth/customer/register

|   |   |
|---|---|
|**Описание**|Создает customer account и challenge по email или телефону.|
|**Доступ только авторизованным пользователям**|-|
|**Endpoint URL**|`/api/v1/auth/customer/register`|
|**Метод запроса**|POST|

## 1. Задачи, в рамках которых вносятся изменения в метод

_-_

## 2. Функциональные требования

| Номер требования | Описание требования | Статус | Источник | Комментарий |
|---|---|---|---|---|
|AUTH-CREG-001|Клиент может зарегистрироваться по email без телефона.|Required|Product correction||
|AUTH-CREG-002|Клиент может зарегистрироваться по телефону без email.|Required|Product correction||
|AUTH-CREG-003|Email verification должен реально отправлять код.|Required|Product correction||
|AUTH-CREG-004|Пароль хранится только как hash.|Required|Security||

## 3. Описание логики работы метода

### 3.1 Валидация

- Ровно один основной идентификатор обязателен: `email` или `phone`.
- Пароль и подтверждение пароля совпадают.
- `acceptedUserAgreement=true`.

### 3.2 Создание challenge

- Создать pending account.
- Создать auth challenge на выбранный contact.
- Если contact email, отправить реальный email code.
- Если contact phone, использовать SMS adapter только когда он включен.

## 4. Разрешения доступа к методу

| Наименование разрешения | Описание разрешения |
|---|---|
|ANONYMOUS|Доступ без авторизации.|

## 5. Настройки системы, используемые в методе

| Наименование | Код | Тип значения |
|---|---|---|
|TTL challenge|`auth.challenge.ttl`|duration|
|Email verification enabled|`auth.verification.email.enabled`|boolean|
|SMS verification enabled|`auth.verification.sms.enabled`|boolean|

## 6. Параметры метода

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|Имя|`displayName`|string body|-|-||
|2|Email|`email`|string body|-|-|Обязателен, если phone пустой|
|3|Телефон|`phone`|string body|-|-|Обязателен, если email пустой|
|4|Пароль|`password`|string body|+|-||
|5|Подтверждение пароля|`passwordConfirmation`|string body|+|-||
|6|Принято соглашение|`acceptedUserAgreement`|boolean body|+|-|true|
|7|Запомнить|`rememberMe`|boolean body|-|false||

## 7. Возвращаемые данные

| № | Описание поля | Наименование | Тип | Этап | Источник данных | Комментарий |
|---|---|---|---|---|---|---|
|1|ID challenge|`authChallengeId`|uuid|Этап 1|auth_challenge||
|2|Роль|`role`|string|Этап 1|auth_challenge|CUSTOMER|
|3|Назначение|`purpose`|string|Этап 1|auth_challenge|REGISTER|
|4|Канал|`channel`|string|Этап 1|auth_challenge|EMAIL или SMS|
|5|Маскированный contact|`maskedDestination`|string|Этап 1|derived||
|6|Истекает|`expiresAt`|datetime|Этап 1|auth_challenge||

## Пример запроса

```http
POST /api/v1/auth/customer/register
Content-Type: application/json

{
  "displayName": "Aruzhan",
  "email": "aruzhan@example.kz",
  "phone": null,
  "password": "secret-password",
  "passwordConfirmation": "secret-password",
  "acceptedUserAgreement": true,
  "rememberMe": true
}
```

## Пример ответа

```json
{
  "authChallengeId": "ac-uuid-001",
  "role": "CUSTOMER",
  "purpose": "REGISTER",
  "channel": "EMAIL",
  "maskedDestination": "a***n@example.kz",
  "expiresAt": "2026-06-21T10:05:00Z"
}
```

---

## Вход клиента - POST /api/v1/auth/customer/login/start

|   |   |
|---|---|
|**Описание**|Создает login challenge для клиента по email или телефону.|
|**Доступ только авторизованным пользователям**|-|
|**Endpoint URL**|`/api/v1/auth/customer/login/start`|
|**Метод запроса**|POST|

## 6. Параметры метода

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|Email|`email`|string body|-|-|Обязателен, если phone пустой|
|2|Телефон|`phone`|string body|-|-|Обязателен, если email пустой|
|3|Запомнить|`rememberMe`|boolean body|-|false||

## Пример запроса

```http
POST /api/v1/auth/customer/login/start
Content-Type: application/json

{
  "email": "aruzhan@example.kz",
  "phone": null,
  "rememberMe": true
}
```

---

## Регистрация бизнес-филиала - POST /api/v1/auth/business/register

|   |   |
|---|---|
|**Описание**|Регистрирует конкретный филиал/магазин, владельца и начальный публичный контакт филиала.|
|**Доступ только авторизованным пользователям**|-|
|**Endpoint URL**|`/api/v1/auth/business/register`|
|**Метод запроса**|POST|

## 2. Функциональные требования

| Номер требования | Описание требования | Статус | Источник | Комментарий |
|---|---|---|---|---|
|AUTH-BREG-001|Регистрация создает конкретный филиал/магазин.|Required|Product correction||
|AUTH-BREG-002|Email или phone регистрации становится начальным публичным контактом филиала.|Required|Product correction||
|AUTH-BREG-003|Email registration не требует phone.|Required|Product correction||
|AUTH-BREG-004|Данные бизнеса сохраняются в постоянной базе.|Required|Product correction||

## 3. Описание логики работы метода

### 3.1 Валидация

- Ровно один основной идентификатор обязателен: `email` или `phone`.
- `businessName` обязателен.
- `branchName` обязателен.
- `branchAddress` обязателен, если `onlineOnly=false`.
- `acceptedBusinessRules=true`.

### 3.2 После успешного verify

- Создать `AppUser`.
- Создать `Business`.
- Создать `BusinessBranch`.
- Создать `BusinessMember` с ролью `OWNER`.
- Создать `BusinessContact` для выбранного registration contact.

## 6. Параметры метода

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|Email|`email`|string body|-|-|Login contact и начальный public contact, если phone пустой|
|2|Телефон|`phone`|string body|-|-|Login contact и начальный public contact, если email пустой|
|3|Пароль|`password`|string body|+|-||
|4|Подтверждение пароля|`passwordConfirmation`|string body|+|-||
|5|Название бизнеса|`businessName`|string body|+|-|Контейнер/бренд|
|6|Название филиала|`branchName`|string body|+|-|Конкретное заведение|
|7|Город филиала|`branchCityId`|uuid body|-|-|Желателен для локального поиска|
|8|Адрес филиала|`branchAddress`|string body|-|-|Обязателен, если onlineOnly=false|
|9|Онлайн-магазин|`onlineOnly`|boolean body|-|false||
|10|Приняты правила Ask|`acceptedBusinessRules`|boolean body|+|-|true|
|11|Запомнить|`rememberMe`|boolean body|-|false||

## Пример запроса

```http
POST /api/v1/auth/business/register
Content-Type: application/json

{
  "email": "owner@pos-store.kz",
  "phone": null,
  "password": "secret-password",
  "passwordConfirmation": "secret-password",
  "businessName": "Kaspi POS Store",
  "branchName": "Kaspi POS Store Абая",
  "branchCityId": "city-uuid-astana",
  "branchAddress": "ул. Абая 45",
  "onlineOnly": false,
  "acceptedBusinessRules": true,
  "rememberMe": true
}
```

## Пример ответа

```json
{
  "authChallengeId": "ac-uuid-002",
  "role": "BUSINESS",
  "purpose": "REGISTER",
  "channel": "EMAIL",
  "maskedDestination": "o***r@pos-store.kz",
  "expiresAt": "2026-06-21T10:05:00Z"
}
```

---

## Подтверждение кода - POST /api/v1/auth/verify

|   |   |
|---|---|
|**Описание**|Проверяет 6-значный код и создает customer или business session.|
|**Доступ только авторизованным пользователям**|-|
|**Endpoint URL**|`/api/v1/auth/verify`|
|**Метод запроса**|POST|

## 6. Параметры метода

| № | Описание | Наименование | Тип | Обязательно | По умолчанию | Комментарий |
|---|---|---|---|---|---|---|
|1|ID challenge|`authChallengeId`|uuid body|+|-||
|2|Код|`code`|string body|+|-|Ровно 6 цифр|

## 7. Возвращаемые данные

| № | Описание поля | Наименование | Тип | Этап | Источник данных | Комментарий |
|---|---|---|---|---|---|---|
|1|Токен доступа|`accessToken`|string|Этап 1|session/token||
|2|Тип токена|`tokenType`|string|Этап 1|constant|Bearer|
|3|Истекает|`expiresAt`|datetime|Этап 1|session||
|4|Запомнен|`remembered`|boolean|Этап 1|challenge||
|5|Роль|`role`|string|Этап 1|session|CUSTOMER/BUSINESS|
|6|Пользователь|`user`|object|Этап 1|AuthUserResponse||
|7|Бизнес|`business`|object|Этап 1|AuthBusinessContextResponse|Только BUSINESS|
|8|Стартовый маршрут|`startRoute`|string|Этап 1|derived|CLIENT_SEARCH/BUSINESS_ACTIVITY|

## Пример запроса

```http
POST /api/v1/auth/verify
Content-Type: application/json

{
  "authChallengeId": "ac-uuid-002",
  "code": "123456"
}
```

## Пример ответа бизнес-сессии

```json
{
  "accessToken": "token",
  "tokenType": "Bearer",
  "expiresAt": "2026-07-21T10:00:00Z",
  "remembered": true,
  "role": "BUSINESS",
  "user": {
    "userId": "user-uuid-001",
    "displayName": "Owner",
    "email": "owner@pos-store.kz",
    "phone": null,
    "status": "ACTIVE"
  },
  "business": {
    "businessId": "biz-uuid-001",
    "businessName": "Kaspi POS Store",
    "branchId": "branch-uuid-001",
    "branchName": "Kaspi POS Store Абая",
    "membershipId": "bm-uuid-001",
    "memberRole": "OWNER"
  },
  "startRoute": "BUSINESS_ACTIVITY"
}
```

---

## Текущая сессия - GET /api/v1/auth/session

|   |   |
|---|---|
|**Описание**|Возвращает безопасные данные текущего пользователя и business/branch context для бизнес-сессии.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/auth/session`|
|**Метод запроса**|GET|

## Пример запроса

```http
GET /api/v1/auth/session
Authorization: Bearer <token>
```

---

## Logout - POST /api/v1/auth/logout

|   |   |
|---|---|
|**Описание**|Отзывает активную сессию. Не удаляет клиента, бизнес, филиал, товары, услуги, заявки, чаты или историю.|
|**Доступ только авторизованным пользователям**|+|
|**Endpoint URL**|`/api/v1/auth/logout`|
|**Метод запроса**|POST|

## Пример ответа

```json
{
  "success": true
}
```

## Реальная инфраструктура

- Нужна постоянная база, которую нельзя дропать после выдачи сайта бизнесам.
- Frontend, backend и database должны быть готовы к deployment flow.
- Seed/demo данные не должны заменять реальные регистрации.
- Email provider должен быть подключен до production-facing onboarding.

## Implementation Boundaries

- Не проектировать как mock-only.
- Не требовать телефон при email-регистрации.
- Не требовать email при phone-регистрации.
- Не создавать tests и не запускать Maven без явного запроса.

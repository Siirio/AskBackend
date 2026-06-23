package kz.ask.shared.error;

public enum ErrorCode {

    USER_NOT_FOUND("Пользователь не найден"),
    CHALLENGE_NOT_FOUND("Код подтверждения не найден"),
    EMAIL_ALREADY_REGISTERED("Email уже зарегистрирован"),
    PHONE_ALREADY_REGISTERED("Телефон уже зарегистрирован"),
    EMAIL_ALREADY_EXISTS("Пользователь с таким email уже существует"),
    INVALID_CREDENTIALS("Неверный email или пароль"),
    ACCOUNT_NOT_ACTIVE("Аккаунт не активен"),
    ROLE_MISMATCH("Неверная роль аккаунта"),
    SESSION_INVALID("Сессия недействительна"),
    PASSWORD_CHANGE_NOT_REQUIRED("Смена пароля не требуется"),
    PASSWORDS_DO_NOT_MATCH("Пароли не совпадают"),
    CHALLENGE_EXPIRED("Код подтверждения истек: %s"),
    CHALLENGE_MAX_ATTEMPTS("Превышено количество попыток: %s"),
    CHALLENGE_INVALID_CODE("Неверный код подтверждения: %s"),
    REGISTRATION_PAYLOAD_ERROR("Ошибка данных регистрации"),
    CITY_NOT_FOUND("Город не найден"),
    BRANCH_NOT_FOUND("Филиал не найден"),
    STAFF_NOT_FOUND("Сотрудник не найден"),
    ACCESS_DENIED("Доступ запрещен"),
    DELIVERY_FAILED("Ошибка отправки кода подтверждения"),
    INTERNAL_ERROR("Внутренняя ошибка сервера"),
    IMPORT_NOT_FOUND("Импорт не найден"),
    IMPORT_NOT_XLSX("Файл должен быть в формате .xlsx"),
    IMPORT_EMPTY_FILE("Файл пуст"),
    IMPORT_INVALID_STATUS("Недопустимый статус импорта для этой операции"),
    IMPORT_NAME_REQUIRED("Название товара обязательно"),
    IMPORT_COLUMN_NOT_FOUND("Колонка не найдена: %s"),
    FILE_NOT_XLSX("Поддерживаются только файлы .xlsx"),
    CATEGORY_NOT_FOUND("Категория не найдена"),
    PRODUCT_NOT_FOUND("Товар не найден для этого филиала"),
    SKU_ALREADY_EXISTS("SKU уже используется другим товаром в этом бизнесе"),
    PRODUCT_NAME_BLANK("Название товара не может быть пустым"),
    OPERATOR_FORBIDDEN_ACTION("Оператору не разрешено выполнять действие: %s");

    private final String template;

    ErrorCode(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        if (args == null || args.length == 0) {
            return template;
        }
        return String.format(template, args);
    }
}

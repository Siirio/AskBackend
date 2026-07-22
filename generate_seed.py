#!/usr/bin/env python3
"""Generate V4__seed_showcase.sql with 60+ Kazakh companies with maximum data variability."""
import hashlib
import uuid

OUT = "src/main/resources/db/migration/V4__seed_showcase.sql"

# Deterministic UUIDv7-like IDs from a seed string
def mkid(seed: str) -> str:
    h = hashlib.sha256(seed.encode()).hexdigest()
    # Use a V4-like pattern in our reserved namespace: 00000000-0000-0000-0001-...
    return f"00000000-0000-0000-0001-{h[:12]}"

def ts() -> str:
    return "now()"

# ============================================================
# DATA DEFINITIONS
# ============================================================

NEW_CITIES = [
    ("Шымкент", "KZ"),
    ("Караганда", "KZ"),
]

NEW_CATEGORIES = [
    ("Продукты питания", "food"),
    ("Медицинские услуги", "medical"),
    ("Образование", "education"),
    ("Спорт и фитнес", "sport"),
    ("IT услуги", "it"),
]

# ALL categories (V3 + new) — for assigning to companies
ALL_CATEGORIES = {
    "Автозапчасти":   "00000000-0000-0000-0000-0000000000a1",
    "Бытовая техника": "00000000-0000-0000-0000-0000000000a2",
    "Услуги красоты":  "00000000-0000-0000-0000-0000000000a3",
    "Ремонт и сервис": "00000000-0000-0000-0000-0000000000a4",
    "Строительство":   "00000000-0000-0000-0000-0000000000a5",
    "Продукты питания": mkid("cat-food"),
    "Медицинские услуги": mkid("cat-medical"),
    "Образование":        mkid("cat-education"),
    "Спорт и фитнес":     mkid("cat-sport"),
    "IT услуги":          mkid("cat-it"),
}

ALL_CITIES = {
    "Кызылорда": "00000000-0000-0000-0000-0000000000c1",
    "Алматы":    "00000000-0000-0000-0000-0000000000c2",
    "Астана":    "00000000-0000-0000-0000-0000000000c3",
    "Шымкент":   mkid("city-shymkent"),
    "Караганда": mkid("city-karaganda"),
}

# ============================================================
# COMPANY GENERATORS
# ============================================================

# Product-only companies: name, category, city, products list [(name, desc, sku, price, characteristics_json)]
PRODUCT_COMPANIES = []
for i in range(1, 26):
    cat_name = list(ALL_CATEGORIES.keys())[i % len(ALL_CATEGORIES)]
    city_name = list(ALL_CITIES.keys())[i % len(ALL_CITIES)]
    legal = "ТОО" if i % 2 == 0 else "ИП"
    PRODUCT_COMPANIES.append((f"Компания-Товар-{i}", legal, cat_name, city_name, i))

# Service-only companies
SERVICE_COMPANIES = []
for i in range(1, 26):
    cat_name = list(ALL_CATEGORIES.keys())[(i + 5) % len(ALL_CATEGORIES)]
    city_name = list(ALL_CITIES.keys())[(i + 2) % len(ALL_CITIES)]
    legal = "ТОО" if i % 2 == 0 else "ИП"
    SERVICE_COMPANIES.append((f"Компания-Услуга-{i}", legal, cat_name, city_name, i))

# Mixed companies (both products + services)
MIXED_COMPANIES = []
for i in range(1, 13):
    cat_name = list(ALL_CATEGORIES.keys())[(i + 3) % len(ALL_CATEGORIES)]
    city_name = list(ALL_CITIES.keys())[(i + 4) % len(ALL_CITIES)]
    legal = "ТОО" if i % 2 == 0 else "ИП"
    MIXED_COMPANIES.append((f"Компания-Микс-{i}", legal, cat_name, city_name, i))

# ============================================================
# PRODUCT/SERVICE NAME POOLS
# ============================================================

PRODUCT_POOL = [
    # Автозапчасти
    ("Масляный фильтр Sakura", "Фильтр масляный для японских авто", "SAK-OF-001", 3500, '{"бренд":"Sakura","тип":"масляный"}'),
    ("Тормозные колодки TRW", "Передние колодки для европейских авто", "TRW-BP-202", 18500, '{"бренд":"TRW","ось":"перед","тип":"керамика"}'),
    ("Свечи зажигания NGK", "Иридиевые свечи, комплект 4 шт", "NGK-IR-4", 12000, '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}'),
    ("Амортизатор Kayaba", "Газовый амортизатор задний", "KYB-GS-001", 28000, '{"бренд":"Kayaba","тип":"газовый","ось":"зад"}'),
    ("Ремень ГРМ Gates", "Ремень ГРМ с роликом, комплект", "GTS-TB-KIT", 22000, '{"бренд":"Gates","тип":"комплект"}'),
    # Бытовая техника
    ("Холодильник LG 350L", "Двухкамерный холодильник с No Frost", "LG-RF-350", 189000, '{"бренд":"LG","объем":"350л","тип":"No Frost"}'),
    ("Стиральная машина Bosch", "Фронтальная загрузка 7кг, 1200 об/мин", "BOS-WM-7", 245000, '{"бренд":"Bosch","загрузка":"7кг","обороты":"1200"}'),
    ("Микроволновая печь Samsung", "СВЧ 23л, гриль, сенсорное управление", "SAM-MW-23", 45000, '{"бренд":"Samsung","объем":"23л","гриль":"да"}'),
    ("Пылесос Dyson V15", "Беспроводной вертикальный пылесос", "DYS-V15", 320000, '{"бренд":"Dyson","тип":"вертикальный","аккумулятор":"Li-Ion"}'),
    ("Утюг Philips Azur", "Паровой утюг с керамической подошвой", "PHI-AZ-3000", 28000, '{"бренд":"Philips","тип":"паровой"}'),
    # Строительство
    ("Гипсокартон Knauf 12.5мм", "Влагостойкий лист 2500x1200 мм", "KNF-GKL-12", 4500, '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}'),
    ("Профнастил С8 0.45мм", "Оцинкованный профнастил для забора", "PRF-C8-045", 2800, '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}'),
    ("Металлочерепица Монтеррей", "Полиэстер 0.5мм, коричневый", "MCH-MT-05", 5200, '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}'),
    ("Клей плиточный Ceresit CM11", "Для керамической плитки, 25 кг", "CER-CM11-25", 3200, '{"бренд":"Ceresit","вес":"25кг","назначение":"плитка"}'),
    ("Ламинат Classen 33 класс", "Влагостойкий, дуб, 8мм, 2.22м²", "CLS-LAM-33", 8500, '{"бренд":"Classen","класс":"33","толщина":"8мм","рисунок":"дуб"}'),
    # Продукты питания
    ("Мука пшеничная высший сорт", "Казахстанская мука, 50 кг мешок", "MUK-VS-50", 12000, '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}'),
    ("Масло подсолнечное 5л", "Рафинированное дезодорированное", "MAS-POD-5L", 4500, '{"тип":"рафинированное","объем":"5л"}'),
    ("Рис пропаренный 900г", "Длиннозерный пропаренный рис", "RIS-PR-900", 850, '{"тип":"пропаренный","вес":"900г"}'),
    ("Сахар-песок 1кг", "Белый свекловичный сахар", "SAH-BEL-1", 550, '{"тип":"свекловичный","вес":"1кг"}'),
    ("Макароны спираль 400г", "Из твердых сортов пшеницы", "MAK-SP-400", 380, '{"тип":"спираль","вес":"400г","сорт":"твердый"}'),
    # IT услуги — products (software licenses)
    ("Антивирус Kaspersky 1 год", "Защита на 3 устройства, лицензия", "KAS-AV-3D", 12000, '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}'),
    ("Microsoft Office 365", "Годовая подписка на 1 пользователя", "MS-O365-1Y", 35000, '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}'),
    ("Windows 11 Pro лицензия", "Цифровая лицензия, русский язык", "WIN-11-PRO", 55000, '{"версия":"Pro","тип":"цифровая"}'),
    # Спорт и фитнес — products
    ("Беговая дорожка CardioFit", "Электрическая, до 16 км/ч, складная", "CF-TM-001", 280000, '{"тип":"электрическая","скорость":"16 км/ч","складная":"да"}'),
    ("Гантели набор 20кг", "Пара гантелей с блинами, обрезиненные", "GNT-SET-20", 35000, '{"вес":"20кг","тип":"обрезиненные","в_комплекте":"2 шт"}'),
    ("Коврик для йоги 6мм", "Нескользящий, термопластичная резина", "MAT-YG-6", 6500, '{"толщина":"6мм","материал":"TPE","нескользящий":"да"}'),
    ("Фитнес-браслет Xiaomi Band", "Шагомер, пульсометр, SpO2, водозащита", "MI-BAND-8", 18000, '{"бренд":"Xiaomi","датчики":"пульс,SpO2","водозащита":"5ATM"}'),
    # Ремонт и сервис — products (parts/tools)
    ("Дрель ударная Bosch 750Вт", "Дрель ударная с реверсом, кейс", "BOS-DR-750", 28000, '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}'),
    ("Шуруповерт Makita 18В", "Аккумуляторный, 2 батареи, кейс", "MAK-SD-18", 45000, '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}'),
    ("Болгарка УШМ 125мм", "Угловая шлифмашина, 1100Вт", "USH-125-1100", 15000, '{"диаметр":"125мм","мощность":"1100Вт"}'),
]

SERVICE_POOL = [
    # Услуги красоты
    ("Стрижка женская модельная", "Стрижка с укладкой и уходом, 60 мин", 6000, 60, "Ежедневно 10:00-20:00"),
    ("Маникюр аппаратный", "Аппаратный маникюр + покрытие гель-лак", 5500, 75, "Пн-Сб 09:00-18:00"),
    ("Педикюр классический", "Классический педикюр с покрытием", 7000, 90, "Ежедневно 10:00-20:00"),
    ("Наращивание ресниц 2D", "Классическое наращивание, эффект 4 недели", 10000, 120, "Пн-Сб 09:00-19:00"),
    ("Массаж лица омолаживающий", "Лимфодренажный массаж + маска, 45 мин", 8000, 45, "Ежедневно 10:00-20:00"),
    # Медицинские услуги
    ("Консультация терапевта", "Первичный прием, осмотр, назначение", 5000, 30, "Пн-Пт 08:00-17:00"),
    ("УЗИ брюшной полости", "Комплексное УЗИ органов брюшной полости", 8000, 40, "Пн-Сб 08:00-14:00"),
    ("Анализ крови общий", "Забор крови + общий анализ, результат за 1 день", 3500, 15, "Пн-Пт 07:30-10:00"),
    ("Прием кардиолога", "Консультация, ЭКГ, расшифровка", 7000, 45, "Пн-Пт 09:00-16:00"),
    ("Чистка зубов ультразвуковая", "Профессиональная гигиена полости рта", 12000, 60, "Пн-Сб 09:00-18:00"),
    # Образование
    ("Курс Python базовый", "Основы программирования на Python, 16 часов", 45000, 960, "Группы: утро/вечер"),
    ("Английский язык Intermediate", "Разговорный курс, 24 занятия по 90 мин", 65000, 1440, "Пн-Ср-Пт или Вт-Чт-Сб"),
    ("Подготовка к IELTS", "Интенсивный курс, 20 занятий по 120 мин", 85000, 2400, "Утро 09:00 или вечер 18:00"),
    ("Репетиторство по математике", "Подготовка к ЕНТ, индивидуально, 60 мин", 4000, 60, "По согласованию"),
    ("Курс видеомонтажа DaVinci", "Основы цветокоррекции и монтажа, 10 занятий", 35000, 600, "Вт-Чт 18:00-20:00"),
    # Спорт и фитнес
    ("Персональная тренировка", "Индивидуальное занятие с тренером, 60 мин", 5000, 60, "Ежедневно 06:00-23:00"),
    ("Абонемент в тренажерный зал", "Месячный абонемент безлимит", 25000, 43200, "Ежедневно 06:00-23:00"),
    ("Занятие по кроссфиту", "Групповое занятие, 90 мин", 3500, 90, "Пн-Ср-Пт 07:00, 19:00"),
    ("Плавание дети 6-12 лет", "Групповое занятие с тренером, 45 мин", 3000, 45, "Сб-Вс 10:00-14:00"),
    ("Растяжка Stretching", "Групповое занятие, 60 мин", 2500, 60, "Вт-Чт 18:00, Сб 10:00"),
    # IT услуги
    ("Разработка сайта-визитки", "Лендинг до 5 страниц, адаптивный дизайн", 150000, 10080, "По согласованию"),
    ("Настройка таргет рекламы", "Настройка и ведение рекламы в Instagram/Facebook", 50000, 10080, "Рабочие дни"),
    ("SEO продвижение сайта", "Аудит + базовая оптимизация, ежемесячно", 80000, 43200, "По согласованию"),
    ("Ремонт ноутбука", "Диагностика + замена комплектующих", 5000, 60, "Пн-Сб 10:00-19:00"),
    ("Установка и настройка 1С", "Установка, настройка, обучение персонала", 75000, 7200, "По согласованию"),
    # Ремонт и сервис
    ("Ремонт стиральной машины", "Диагностика + ремонт на месте", 5000, 60, "Пн-Сб 09:00-18:00"),
    ("Установка кондиционера", "Монтаж сплит-системы под ключ", 25000, 180, "Ежедневно, по записи"),
    ("Замена электропроводки", "Полная замена в 2-комнатной квартире", 180000, 1440, "По согласованию"),
    ("Сантехнические работы", "Установка смесителя, ремонт бачка, прочистка", 7000, 90, "Ежедневно 08:00-20:00"),
    # Строительство - services
    ("Отделка квартиры под ключ", "Черновая + чистовая отделка, материал заказчика", 1500000, 43200, "По договору"),
    ("Монтаж гипсокартона", "Потолок, 1м², включая профиль и саморезы", 3500, 60, "Ежедневно 08:00-18:00"),
    ("Укладка ламината", "Укладка с подложкой, 1м²", 2500, 60, "Ежедневно 08:00-18:00"),
    # Продукты питания — services (catering)
    ("Кейтеринг на мероприятие", "Выездное обслуживание до 50 человек", 150000, 360, "По записи, за 3 дня"),
    ("Доставка продуктов на дом", "Доставка в течение 2 часов по городу", 1500, 120, "Ежедневно 08:00-22:00"),
]

# ============================================================
# ADDRESS POOLS
# ============================================================

ADDRESSES = {
    "Кызылорда": ["пр. Абая, 18", "ул. Айтеке би, 5", "ул. Толе би, 22", "ул. Жибек жолы, 10", "ул. Сыганак, 45",
                   "пр. Назарбаева, 120", "ул. Коркыт Ата, 34", "ул. Байконур, 8", "мкр. Мерей, 15"],
    "Алматы": ["пр. Аль-Фараби, 77", "ул. Сатпаева, 90", "ул. Розыбакиева, 210", "пр. Абая, 150",
               "ул. Тимирязева, 42", "ул. Жандосова, 55", "мкр. Орбита-3, 12", "пр. Достык, 88"],
    "Астана": ["пр. Кабанбай батыра, 58", "ул. Сарайшык, 13", "пр. Туран, 45", "ул. Сыганак, 25",
               "мкр. Чубары, 8", "ул. Бейбитшилик, 30", "пр. Республики, 60", "ул. Калдаякова, 17"],
    "Шымкент": ["пр. Тауке хана, 88", "ул. Казыбек би, 35", "мкр. Нурсат, 14", "пр. Кунаева, 120",
                "ул. Байтурсынова, 67", "ул. Кулышова, 99"],
    "Караганда": ["пр. Бухар жырау, 52", "ул. Гоголя, 25", "ул. Ермекова, 40", "пр. Нуркена Абдирова, 15",
                  "ул. Чкалова, 18", "мкр. Гульдер, 7"],
}

BINS = ["123456789012", "234567890123", "345678901234", "456789012345", "567890123456",
        "678901234567", "789012345678", "890123456789", "901234567890", "012345678901"]

PRODUCT_CATEGORIES = ["Автозапчасти", "Бытовая техника", "Строительство", "Продукты питания", "Спорт и фитнес", "Ремонт и сервис", "IT услуги"]
SERVICE_CATEGORIES = ["Услуги красоты", "Медицинские услуги", "Образование", "Спорт и фитнес", "IT услуги", "Ремонт и сервис", "Строительство", "Продукты питания"]

def product_for_category(cat: str, idx: int):
    """Pick a item from the pool matching the category, cycling through."""
    matching = [p for p in PRODUCT_POOL if _cat_of_product(p) == cat]
    if not matching:
        matching = PRODUCT_POOL
    return matching[idx % len(matching)]

def _cat_of_product(p):
    """Infer category from item characteristics."""
    text = f"{p[0]} {p[3]}"
    if any(w in text.lower() for w in ["фильтр", "колодк", "свеч", "амортизат", "ремень", "грм", "шины", "диски", "масло", "аккумулят"]):
        return "Автозапчасти"
    if any(w in text.lower() for w in ["холодильник", "стиральн", "микроволн", "пылесос", "утюг", "телевизор", "ноутбук", "смартфон"]):
        return "Бытовая техника"
    if any(w in text.lower() for w in ["цемент", "кирпич", "шпаклевк", "гипсокартон", "профнастил", "металлочерепиц", "клей плиточ", "ламинат"]):
        return "Строительство"
    if any(w in text.lower() for w in ["мука", "масло подсолнечн", "рис", "сахар", "макароны"]):
        return "Продукты питания"
    if any(w in text.lower() for w in ["беговая", "гантел", "коврик", "фитнес-браслет"]):
        return "Спорт и фитнес"
    if any(w in text.lower() for w in ["дрель", "шуруповерт", "болгарк"]):
        return "Ремонт и сервис"
    if any(w in text.lower() for w in ["антивирус", "microsoft", "windows"]):
        return "IT услуги"
    return "Автозапчасти"

def service_for_category(cat: str, idx: int):
    matching = [s for s in SERVICE_POOL if _cat_of_service(s) == cat]
    if not matching:
        matching = SERVICE_POOL
    return matching[idx % len(matching)]

def _cat_of_service(s):
    text = f"{s[0]}"
    if any(w in text.lower() for w in ["стрижка", "маникюр", "педикюр", "ресниц", "массаж лица"]):
        return "Услуги красоты"
    if any(w in text.lower() for w in ["терапевт", "узи", "анализ", "кардиолог", "чистка зуб"]):
        return "Медицинские услуги"
    if any(w in text.lower() for w in ["курс", "python", "английск", "ielts", "репетитор", "видеомонтаж"]):
        return "Образование"
    if any(w in text.lower() for w in ["тренировк", "абонемент", "кроссфит", "плавани", "растяжк"]):
        return "Спорт и фитнес"
    if any(w in text.lower() for w in ["сайт", "таргет", "seo", "ремонт ноутбук", "1с"]):
        return "IT услуги"
    if any(w in text.lower() for w in ["ремонт стиральн", "кондиционер", "электропроводк", "сантехническ"]):
        return "Ремонт и сервис"
    if any(w in text.lower() for w in ["отделк", "гипсокартон", "ламинат", "монтаж"]):
        return "Строительство"
    if any(w in text.lower() for w in ["кейтеринг", "доставк"]):
        return "Продукты питания"
    return "Услуги красоты"

def tokenize(text: str) -> list[str]:
    """Simple Russian word tokenizer."""
    words = text.lower().replace('"', '').replace('/', ' ').replace('-', ' ').replace(',', '').split()
    return [w for w in words if len(w) > 2 and w not in ('для', 'при', 'под', 'для', 'про', 'без', 'или', 'как', 'что', 'это', 'все', 'еще', 'уже', 'там', 'тут', 'для', 'над', 'под', 'при')]

# ============================================================
# SQL GENERATION
# ============================================================

lines = []
w = lines.append

w("-- =============================================================================")
w("-- V4: Seed showcase data — 60+ companies with maximum variability")
w("-- =============================================================================")
w("")

# ----- Cities -----
w("-- Cities (2 new)")
for name, code in NEW_CITIES:
    cid = ALL_CITIES[name]
    w(f"INSERT INTO city (id, created_at, updated_at, name, country_code, status) VALUES")
    w(f"  ('{cid}', {ts()}, {ts()}, '{name}', '{code}', 'ACTIVE');")
w("")

# ----- Categories -----
w("-- Categories (5 new)")
for name, slug in NEW_CATEGORIES:
    cid = ALL_CATEGORIES[name]
    w(f"INSERT INTO category (id, created_at, updated_at, parent_id, name, slug, status) VALUES")
    w(f"  ('{cid}', {ts()}, {ts()}, null, '{name}', '{slug}', 'ACTIVE');")
w("")

def gen_company(prefix, name, legal, cat_name, city_name, idx, has_products=True, has_services=True):
    """Generate SQL for one company with all related data."""
    bid = mkid(f"biz-{prefix}-{idx}")
    did = mkid(f"ds-{prefix}-{idx}")
    cat_id = ALL_CATEGORIES.get(cat_name, ALL_CATEGORIES["Автозапчасти"])
    city_id = ALL_CITIES.get(city_name, ALL_CITIES["Кызылорда"])
    bin_val = BINS[idx % len(BINS)]

    # Business
    w(f"-- {name} ({legal}, {cat_name}, {city_name})")
    w(f"INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES")
    w(f"  ('{bid}', {ts()}, {ts()}, '{name}', '{legal} \"{name}\"', '{bin_val}', 'ACTIVE');")

    # Determine branch count: most companies get 1, some get 2
    nbranches = 2 if idx % 6 == 0 else 1

    branch_ids = []
    for bi in range(nbranches):
        brid = mkid(f"br-{prefix}-{idx}-{bi}")
        branch_ids.append(brid)
        addr_list = ADDRESSES[city_name]
        addr = addr_list[idx % len(addr_list)]
        online_only = "true" if idx % 9 == 0 and bi == 0 else "false"
        lat = 44.8402 + (idx * 0.001) % 0.5
        lng = 65.5000 + (idx * 0.001) % 0.5
        branch_name = f"{name} - Филиал {bi + 1}" if nbranches > 1 else f"{name} - Офис"
        w(f"INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES")
        w(f"  ('{brid}', {ts()}, {ts()}, '{bid}', '{city_id}', '{branch_name}', '{addr}', {lat:.4f}, {lng:.4f}, {online_only}, 'ACTIVE');")

    # Data source
    w(f"INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES")
    w(f"  ('{did}', {ts()}, {ts()}, '{bid}', 'MANUAL', 'Ручной ввод {name}', 'ACTIVE');")

    product_count = 0
    service_count = 0

    if has_products:
        nprod = 2 + (idx % 4)  # 2-5 products
        for pi in range(nprod):
            pid = mkid(f"prod-{prefix}-{idx}-{pi}")
            prod = product_for_category(cat_name, idx * 10 + pi)
            pname, pdesc, psku, pprice, pchar = prod
            category_label = cat_name
            # Some products use category entity, some use only categoryLabel
            use_entity = (idx + pi) % 3 != 0
            cat_ref = f"'{cat_id}'" if use_entity else "null"
            actual_cat_label = category_label
            w(f"INSERT INTO item (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES")
            w(f"  ('{pid}', {ts()}, {ts()}, '{bid}', {cat_ref}, '{actual_cat_label}', '{pname}', '{pdesc}', '{psku}', '{pchar}', 'ACTIVE');")

            for brid in branch_ids:
                poid = mkid(f"po-{prefix}-{idx}-{pi}-{brid[-4:]}")
                enabled = "true" if (idx + pi) % 7 != 0 else "false"
                offer_status = "ACTIVE" if (idx + pi) % 10 != 0 else "ARCHIVED"
                w(f"INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES")
                w(f"  ('{poid}', {ts()}, {ts()}, '{pid}', '{brid}', '{did}', {pprice}, {enabled}, '{offer_status}');")

                # Search document only if enabled + ACTIVE
                if enabled == "true" and offer_status == "ACTIVE":
                    sdid = mkid(f"sd-prod-{prefix}-{idx}-{pi}-{brid[-4:]}")
                    w(f"INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES")
                    w(f"  ('{sdid}', {ts()}, {ts()}, 'PRODUCT', '{poid}', null, '{pname}', '{pdesc}', '{actual_cat_label}', '{psku}', '{pchar}', '{bid}', '{brid}', {pprice}, 'ACTIVE');")
                    tokens = tokenize(f"{pname} {actual_cat_label}")
                    if tokens:
                        tok_vals = ", ".join(f"('{sdid}', '{t}')" for t in tokens[:8])
                        w(f"INSERT INTO search_document_token (search_document_id, token) VALUES {tok_vals};")
                product_count += 1

    if has_services:
        nsvc = 2 + ((idx + 1) % 4)  # 2-5 services
        for si in range(nsvc):
            sid = mkid(f"svc-{prefix}-{idx}-{si}")
            svc = service_for_category(cat_name, idx * 10 + si)
            svc_name, svc_desc, svc_price, svc_dur, svc_sched = svc
            w(f"INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES")
            w(f"  ('{sid}', {ts()}, {ts()}, '{bid}', '{cat_id}', '{svc_name}', '{svc_desc}', 'ACTIVE');")

            for brid in branch_ids:
                soid = mkid(f"so-{prefix}-{idx}-{si}-{brid[-4:]}")
                active = "true" if (idx + si) % 8 != 0 else "false"
                offer_status = "ACTIVE" if (idx + si) % 11 != 0 else "ARCHIVED"
                svc_mode = "ON_SITE" if (idx + si) % 3 == 0 else "ONLINE"
                w(f"INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES")
                w(f"  ('{soid}', {ts()}, {ts()}, '{sid}', '{brid}', '{svc_mode}', {svc_price}, {svc_dur}, '{svc_sched}', {active}, '{offer_status}');")

                # Search document only if active + ACTIVE
                if active == "true" and offer_status == "ACTIVE":
                    sdid = mkid(f"sd-svc-{prefix}-{idx}-{si}-{brid[-4:]}")
                    w(f"INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES")
                    w(f"  ('{sdid}', {ts()}, {ts()}, 'SERVICE', null, '{soid}', '{svc_name}', '{svc_desc}', '{cat_name}', null, null, '{bid}', '{brid}', {svc_price}, 'ACTIVE');")
                    tokens = tokenize(f"{svc_name} {cat_name}")
                    if tokens:
                        tok_vals = ", ".join(f"('{sdid}', '{t}')" for t in tokens[:8])
                        w(f"INSERT INTO search_document_token (search_document_id, token) VALUES {tok_vals};")
                service_count += 1

    w("")
    return product_count, service_count

# ============================================================
# Generate all companies
# ============================================================

total_products = 0
total_services = 0

# Product-only (25)
w("-- ==============================")
w("-- PRODUCT-ONLY COMPANIES (25)")
w("-- ==============================")
w("")
for name, legal, cat, city, idx in PRODUCT_COMPANIES:
    pc, sc = gen_company("P", name, legal, cat, city, idx, has_products=True, has_services=False)
    total_products += pc

# Service-only (25)
w("-- ==============================")
w("-- SERVICE-ONLY COMPANIES (25)")
w("-- ==============================")
w("")
for name, legal, cat, city, idx in SERVICE_COMPANIES:
    pc, sc = gen_company("S", name, legal, cat, city, idx, has_products=False, has_services=True)
    total_services += sc

# Mixed (12)
w("-- ==============================")
w("-- MIXED COMPANIES — Products + Services (12)")
w("-- ==============================")
w("")
for name, legal, cat, city, idx in MIXED_COMPANIES:
    pc, sc = gen_company("M", name, legal, cat, city, idx, has_products=True, has_services=True)
    total_products += pc
    total_services += sc

# ============================================================
# Write file
# ============================================================

with open(OUT, "w", encoding="utf-8") as f:
    f.write("\n".join(lines))

print(f"Generated {OUT}")
print(f"Companies: {len(PRODUCT_COMPANIES) + len(SERVICE_COMPANIES) + len(MIXED_COMPANIES)}")
print(f"Products: {total_products}")
print(f"Services: {total_services}")
print(f"Total lines: {len(lines)}")

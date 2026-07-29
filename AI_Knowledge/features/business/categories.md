# Flat Category Flow

## Meaning

| Category type | Answers |
| --- | --- |
| `BUSINESS` | Who is the company? |
| `SERVICE` | What does it do? |
| `ITEM` | What does it sell? |

There are no nested categories.

## System categories

`BUSINESS`: Salon, Barbershop, Dental Clinic, Medical Center, Veterinary Clinic, Education Center, Computer Club, Cleaning Company, Service Center, Electronics Store, Clothing Store, Cosmetics Store, Home Goods Store, Children's Store, Auto Parts Store.

`SERVICE`: Beauty and Care, Cleaning and Dry Cleaning, Health, Veterinary Services, Education, Games and Entertainment, Appliance Repair.

`ITEM`: Electronics, Home Appliances, Clothing and Shoes, Beauty and Care, Home and Interior, Children's Goods, Sport and Recreation, Auto Goods.

## User flow

```text
type text -> receive suggestions for one category type
          -> select a SYSTEM or USER category
          -> or explicitly create a USER category
          -> save the category identity on Business, Item, or Service
```

System categories are the curated starter list. User categories are shared suggestions with `source=USER`; they are not free-form labels stranded on a single Item or Service.

The client calls `GET /api/v1/categories?q=<text>&type=<type>` for suggestions. If no suggestion fits,
it calls `POST /api/v1/categories` with `name` and `type`; the response contains the canonical category ID.

## Search flow

```text
raw query + immutable item/service scope
  -> AI reads all SYSTEM and USER categories of that type
  -> returns category hints only
  -> deterministic retrieval matches stored category identities and text
```

AI does not create categories, alter the selected search scope, or select a business.

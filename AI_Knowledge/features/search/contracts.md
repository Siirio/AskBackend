# Search REST API Contract

`POST /api/v1/search` is anonymous and creates no request, chat, notification, recipient, or supplier outreach.

## Request

```json
{
  "raw_query": "complete visible query",
  "mode": "ITEM",
  "explicit_filters": {
    "category": "optional",
    "city": "optional",
    "country": "optional",
    "min_price": 0,
    "max_price": 100000,
    "open_now": false,
    "radius_meters": 5000
  },
  "user_location": {
    "lat": 43.2389,
    "lng": 76.8897
  },
  "locale": "ru",
  "sort": "relevance",
  "page": 0,
  "page_size": 20
}
```

- `raw_query` is required and returned unchanged.
- `mode` is required and accepts only `ITEM` or `SERVICE`.
- `sort` accepts `relevance`, `distance`, or `price_asc`.
- `page` is 0 through 20; `page_size` is 1 through 50.
- Category, city, country, price, open-now, and radius values under `explicit_filters` are hard filters.
- `radius_meters` requires `user_location`.

Removed fields: `scope`, `selected_category`, top-level `city`, `filters.scope`, `overrides`, and `language`. `PRODUCT` is not a compatibility value.

## Response

- `raw_query`, `mode`, and `understood_query` preserve request context.
- `sections` contains relevant Item/Service results that satisfy every explicit filter.
- `result_id` is the canonical Item/Service ID.
- `result_type` is `ITEM` or `SERVICE`.
- `component` is exactly `ItemCard` or `ServiceCard`.
- Each card includes short and full Item/Service text, an ordered `images` gallery, price/currency, Business identity, public Business profile, optional branch context, availability truth, optional match reasons, badges, optional canonical coordinates, optional distance, and `has_active_offer`. The first image is the primary result image; clients may omit match reasons from presentation.
- `business_profile` contains public logo, cover, description, number, email, Instagram, Telegram, and website values.
- Public responses contain no engine, fallback, latency, exception, or infrastructure diagnostics. Operational details remain in server logs.
- `ambiguity` is `LOW`, `MEDIUM`, or `HIGH`.
- `suggestions` contains bounded clarification chips derived from validated interpretations. It is empty when no useful clarification exists.

The chat button uses `business_id` to open/resume the durable business conversation with the selected Item/Service as entry context. Search itself never creates the conversation.

## Frontend migration

Rename request `scope` to `mode`; send only `ITEM` or `SERVICE`; move category/city/price values under `explicit_filters`; rename `language` to `locale`; delete overrides and all `PRODUCT`/`ALL` compatibility handling. Treat the card as compact-row data plus modal data. Use `result_id` for Item/Service identity and `business_id` for chat identity.

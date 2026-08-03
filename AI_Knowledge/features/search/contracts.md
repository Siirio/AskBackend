# Search REST API Contract

`POST /api/v1/search` is anonymous.

```json
{
  "rawQuery": "visible query",
  "mode": "ITEM",
  "sort": "relevance",
  "page": 0,
  "pageSize": 20,
  "locale": "ru",
  "userLocation": { "lat": 43.2389, "lng": 76.8897 },
  "explicitFilters": {
    "category": "optional",
    "city": "optional",
    "country": "KZ",
    "minPrice": 0,
    "maxPrice": 100000,
    "radiusMeters": 5000,
    "businessIds": ["business-uuid"],
    "mapArea": { "north": 43.4, "south": 43.1, "east": 77.1, "west": 76.7 }
  }
}
```

- `mode`: `ITEM` or `SERVICE`.
- `sort`: `relevance`, `distance`, `price_asc`, `price_desc`, or `unique_offers`.
- `page`: any non-negative integer; `pageSize`: 1–50.
- Category, city, country, price, radius, companies, and map area are server-side hard filters across the full catalogue.
- City, radius, and map area are mutually exclusive. Radius and distance sort require `userLocation`.
- `openNow` is not accepted because search does not index a trustworthy current opening state.

The response exposes bounded `sections`, `page`, `pageSize`, global `total`, and `hasNext`. Cards contain canonical `resultId`, `businessId`, `resultType`, business presentation, images, ordered `purchaseDestinations: [{label,url}]`, price, availability metadata, stable badge tokens, coordinates/distance, optional match reasons, and `hasActiveOffer`. Match reasons are metadata only and must not be displayed.

Public search has no standalone Item/Service detail endpoint. Shareable `/app/product/{id}` remains unsupported; the loaded search card can drive the detail modal.

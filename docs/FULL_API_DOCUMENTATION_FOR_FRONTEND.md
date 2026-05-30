# Laundrify Backend — Full API Documentation for Frontend

This document is the single reference for frontend implementation. It lists every public endpoint, authentication requirements, request/response schemas, example cURL calls, and UI integration notes.

Base URL: `http://<HOST>:<PORT>` (default: `http://localhost:8080`)

Common headers
- `Content-Type: application/json`
- `Accept: application/json`
- `Authorization: Bearer <JWT_TOKEN>` (for authenticated routes)

Authentication & Session
- The application uses JWT for authentication. Login returns an access token used in the `Authorization` header for protected endpoints.

Index (by area)
- Authentication: signup (Customer/Driver/Laundry), login, password reset
- User: fetch profile, update profile
- Laundry: create, read, update, list, nearby, details
- Driver: create, read, update, list
- Orders: create, update, list
- Payment: create, callback
- Ratings & Reviews: add/get laundry and driver ratings
- File uploads: image upload notes

---

**1. Authentication**

1.1 POST /api/auth/signup/customer
- Auth: none
- Body (CustomerSignupRequest):
```json
{
  "firstName":"John",
  "lastName":"Doe",
  "email":"john@example.com",
  "password":"Password123!",
  "phoneNumber":"+94712345678",
  "address":"123 Main St",
  "city":"Colombo",
  "district":"Colombo",
  "postalCode":"00100",
  "province":"Western"
}
```
- Success: 201 Created
- Response: created `User` metadata including `id` and `role`.

1.7 POST /api/auth/complete-laundry-registration (multipart)
- Auth: Bearer token (recommended) or public if used after initial signup
- Consumes: `multipart/form-data`
- Request Parts:
  - `data` (JSON) — `LaundryRegistrationRequest` as a JSON string
  - `logo` (file) — required logo image file
  - `shopImage` (file) — required main shop image file
  - `additionalImage1` (file) — optional
  - `additionalImage2` (file) — optional
- Request example (curl):
```bash
curl -X POST http://localhost:8080/api/auth/complete-laundry-registration \
  -H "Authorization: Bearer $TOKEN" \
  -F 'data={"userId":"USER_ID","laundryName":"Fresh Wash Laundry","location":"Colombo","contactNumber":"+94112234567","contactEmail":"info@freshwash.lk","website":"www.freshwash.lk","description":"Professional laundry","openingHours":"08:00 AM","closingHours":"06:00 PM","servicesOffered":"Dry Cleaning, Washing"};type=application/json' \
  -F "logo=@/path/to/logo.png" \
  -F "shopImage=@/path/to/shop.jpg" \
  -F "additionalImage1=@/path/to/add1.jpg" \
  -F "additionalImage2=@/path/to/add2.jpg"
```
- Response: `AuthResponse` (created laundry id is returned in `userId` field of response)
- Description: Uploads media files (logo and business images) and completes laundry profile creation. Files are stored in the configured upload directory and their paths saved to the `Laundry` record (`logoPath`, `shopImagePath`, `additionalImagePaths`).

1.2 POST /api/auth/signup/driver
- Same as customer plus driver-specific fields
- Body (DriverSignupRequest): includes `licenseNumber`, `licenseExpiryDate`, `vehicleType`, `vehicleModel`, `vehicleNumber` etc.
- Success: 201 Created

1.3 POST /api/auth/signup/laundry
- Body (LaundrySignupRequest): includes business fields: `laundryName`, `businessRegistrationNumber`, `taxId`, `servicesOffered`, `ownerFirstName`, `ownerLastName`, `ownerEmail`, etc.
- Success: 201 Created

1.4 POST /api/auth/login
- Body (LoginRequest):
```json
{ "email":"john@example.com", "password":"Password123!" }
```
- Success: 200 OK
- Response (AuthResponse):
```json
{
  "token":"<JWT>",
  "expiresIn":3600,
  "userId":"...",
  "role":"CUSTOMER"
}
```
- Frontend: store token in memory or secure storage; send `Authorization: Bearer <JWT>` on subsequent requests.

1.5 POST /api/auth/password-reset/request
- Body: { "email": "user@example.com" }
- Action: sends a time-limited token via email
- Response: 200 OK with message

1.6 POST /api/auth/password-reset/verify
- Body: { "token": "<token>", "newPassword": "NewPass123!" }
- Response: 200 OK on success

Notes: All signup endpoints return created user id(s). Validate duplicate emails on frontend and show helpful messages.

---

**2. User**

2.1 GET /api/user/{userId}
- Auth: Bearer token (any role)
- Path param: `userId`
- Success: 200 OK
- Response example (UserResponse):
```json
{
  "id":"userId",
  "name":"John Doe",
  "email":"john@example.com",
  "role":"CUSTOMER",
  "enabled":true
}
```

2.2 PUT /api/user/{userId}
- Auth: Bearer token (user must be owner or admin)
- Body: partial User update DTO (fields allowed: name, phoneNumber, address, city, district, postalCode, province)
- Success: 200 OK with updated user

---

**3. Laundry**

3.1 POST /api/laundries
- Auth: Bearer token (role=LAUNDRY)
- Body: `Laundry` creation DTO (same fields as `LaundrySignupRequest` if creating from signup not available)
- Success: 201 Created
- Response: created `Laundry` id and profile

3.2 GET /api/laundries/{laundryId}
- Auth: optional
- Response: full `Laundry` object (profile, contact, services, images)
- Example response includes business fields and location (city, district, province)

3.3 PUT /api/laundries/{laundryId}
- Auth: Bearer token (owner or admin)
- Body: partial laundry update fields
- Response: 200 OK

3.4 GET /api/laundries
- Auth: optional
- Query params (optional): `city`, `district`, `province`, `service`, `verified`, `minRating`, `page`, `size`, `sort`
- Response: paginated list of laundries

3.5 Nearby discovery (hierarchical)
- GET /api/ratings/laundries/nearby?city=<>&district=<>&province=<>&radiusKm=<>
- POST /api/ratings/laundries/nearby (body: `NearbyLaundryRequest`)
- Search logic (frontend note): query with the user's city/district/province; backend cascades: City+District+Province → City+District → City → Province; returns `searchLevel` and `laundries` list.
- Response: `LaundryDetailsResponse[]` where each item includes `averageRating` and `totalRatings` computed by the server.

3.6 GET /api/ratings/laundries/{laundryId}
- This returns `LaundryDetailsResponse` (laundry profile + aggregated rating fields).

Frontend UI notes for laundry pages:
- List view: show `averageRating` (X.X/5), `totalRatings`, thumbnail image, `servicesOffered`, `verified` badge.
- Detail view: show full business information, opening hours, contact buttons, gallery of images, reviews list (paginated).

---

**4. Driver**

4.1 Driver registration covered in signup.

4.2 GET /api/drivers/{driverId}
- Response: `Driver` profile with vehicle info and location

4.3 GET /api/drivers
- Query params: `city`, `district`, `province`, `available`, `minRating`, `page`, `size`
- Response: paginated list of drivers

Frontend UI notes:
- Show aggregated rating and recent reviews on driver card.

---

**5. Orders**

5.1 POST /api/orders
- Auth: Bearer token (CUSTOMER)
- Body (OrderRequest):
```json
{
  "laundryId":"...",
  "pickupAddress": "123 Main St",
  "pickupDate": "2024-09-20T10:00:00",
  "items": [ { "service":"Washing","quantity":3, "notes":"delicate" } ],
  "paymentMethod": "ONLINE|CASH"
}
```
- Response: 201 Created with `OrderResponse` (order id, status=PENDING, estimatedCost)

5.2 GET /api/orders/{orderId}
- Auth: Bearer token (order owner or admin)
- Response: `OrderResponse` with full lifecycle info

5.3 GET /api/orders
- Auth: role-specific
- Query params: `userId`, `laundryId`, `driverId`, `status`, `page`, `size`
- Response: paginated orders

5.4 PUT /api/orders/{orderId}/status
- Auth: roles LAUNDRY or DRIVER or ADMIN depending on transition
- Body: { "status":"PICKED_UP|IN_PROGRESS|COMPLETED|CANCELLED" }
- Response: 200 OK with updated order

Frontend notes:
- Use websockets or polling to update order status.
- Show status timeline in UI.

---

**6. Payments**

6.1 POST /api/payments
- Auth: Bearer token
- Body (`PaymentRequest`): includes `orderId`, `amount`, `method`, `currency`, optional `providerRef`
- Response: 200 OK with payment intent or redirect URL (if third-party)

6.2 POST /api/payments/callback
- Public callback from payment gateway
- Validate provider signature and update order payment status

Frontend notes: handle redirect flows and show payment confirmation screen.

---

**7. Ratings & Reviews**

7.1 POST /api/ratings/laundry?userId=<customerId>
- Auth: Bearer token (recommended)
- Body (LaundryRatingRequest): { "laundryId":"...","rating":4.5,"review":"...","images":["path1","path2"] }
- Behavior: creates or updates a single rating per user per laundry
- Response: 201 Created with rating object

7.2 GET /api/ratings/laundry/{laundryId}
- Response: { "laundryId":"...","averageRating":4.2,"totalRatings":15,"ratings":[ ... ] }

7.3 POST /api/ratings/driver?userId=<customerId>
- Body (DriverRatingRequest): { "driverId":"...","rating":5.0,"review":"...","images":[] }
- Response: 201 Created

7.4 GET /api/ratings/driver/{driverId}
- Response: aggregated driver rating + reviews

Frontend notes:
- Star input: enforce 1-5 on client
- Images: UI should upload files via dedicated file-upload endpoint (see section 8), then pass stored paths in `images` array
- Show most recent reviews first, support pagination

---

**8. File Uploads (Images)**

Current status: endpoints accept image paths in DTOs but a multipart file upload endpoint is recommended.

Suggested endpoints (implement if not present):
- POST /api/uploads/images
  - Accept: multipart/form-data `file` parameter
  - Response: { "path": "uploads/ratings/<uuid>.jpg" }
  - Frontend: upload image first, then include returned path(s) in rating DTO.

Storage options: local filesystem (`uploads/`), or S3/GCS for production. Return absolute or relative path depending on static file serving setup.

---

**9. DTO Reference (fields summary)**

LoginRequest
- `email` (String)
- `password` (String)

AuthResponse
- `token` (String)
- `expiresIn` (Integer)
- `userId` (String)
- `role` (String)

CustomerSignupRequest / DriverSignupRequest / LaundrySignupRequest
- `firstName`, `lastName`, `email`, `password`, `phoneNumber`
- Location: `address`, `city`, `district`, `postalCode`, `province`
- Driver-specific: `licenseNumber`, `licenseExpiryDate`, `vehicleType`, `vehicleModel`, `vehicleNumber`
- Laundry-specific: `laundryName`, `businessRegistrationNumber`, `taxId`, `servicesOffered`, `ownerFirstName`, `ownerLastName`, `ownerEmail`

OrderRequest
- `laundryId`, `pickupAddress`, `pickupDate`, `items` (array with service + quantity + notes), `paymentMethod`

OrderResponse
- `orderId`, `status`, `laundryId`, `driverId`, `items`, `estimatedCost`, `createdAt`, `updatedAt`

PaymentRequest / PaymentResponse
- `orderId`, `amount`, `method`, `currency`, `providerRef`, `status`

LaundryRatingRequest
- `laundryId`, `rating` (Double 1.0-5.0), `review` (String), `images` (String[])

DriverRatingRequest
- `driverId`, `rating`, `review`, `images`

NearbyLaundryRequest
- `city` (required), `district` (optional), `province` (optional), `radiusKm` (optional)

LaundryDetailsResponse
- All `Laundry` fields + `averageRating` (Double) + `totalRatings` (Integer)

---

**10. Error Handling / Response Codes**
- `200 OK` — Successful GET/PUT
- `201 Created` — Successful POST creating resource
- `400 Bad Request` — Validation error (e.g., missing required field, invalid rating)
- `401 Unauthorized` — Missing/invalid token
- `403 Forbidden` — Insufficient role/permission
- `404 Not Found` — Resource not found
- `500 Internal Server Error` — Unexpected error

Error body example:
```json
{ "error": "Detailed error message" }
```

---

**11. Example cURL Snippets (copy-paste)**

Login and store token (bash):
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Password123!"}' \
  | jq -r '.token' > token.txt
```

Get nearby laundries (GET):
```bash
TOKEN=$(cat token.txt)
curl -X GET "http://localhost:8080/api/ratings/laundries/nearby?city=Colombo&district=Colombo&province=Western" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json"
```

Add laundry rating:
```bash
curl -X POST "http://localhost:8080/api/ratings/laundry?userId=USER_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"laundryId":"LAUNDRY_ID","rating":4.5,"review":"Nice service","images":[]}'
```

Create order:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"laundryId":"LAUNDRY_ID","pickupAddress":"123 Main","pickupDate":"2024-09-20T10:00:00","items":[{"service":"Washing","quantity":2}],"paymentMethod":"CASH"}'
```

---

**12. Frontend Integration Checklist**
- Implement secure storage for JWT (prefer in-memory or http-only cookies)
- Validate and sanitize user input before sending to backend
- Use star-picker component for ratings restricted to 1–5
- Upload images first (via file upload endpoint), then submit returned paths as `images` in rating DTO
- Implement pagination for lists (laundries, reviews, orders)
- Implement retries and user-friendly error messages for network failures
- Cache nearby search results for short TTL (e.g., 5 minutes)
- Show loader states for all network operations

---

**13. Where to find DTO & Controller code**
- Authentication controllers: `src/main/java/com/laundrify/server/controller/AuthController.java`
- User controller: `src/main/java/com/laundrify/server/controller/UserController.java`
- Laundry controller(s): `src/main/java/com/laundrify/server/controller/LaundryController.java`
- Order controller: `src/main/java/com/laundrify/server/controller/OrderController.java`
- Rating controller: `src/main/java/com/laundrify/server/controller/RatingController.java`
- DTOs: `src/main/java/com/laundrify/server/dto/`

(Use these paths to open implementation-specific details when wiring frontend models.)

---

**14. Next Actions (recommended)**
1. Confirm which file upload mechanism you prefer (local vs S3) so I can add exact endpoints.
2. If you want, I can generate a Postman collection based on these endpoints.
3. I can also produce typed TypeScript models (interfaces) from DTOs for direct frontend use.

---

_Last updated: May 30, 2026_

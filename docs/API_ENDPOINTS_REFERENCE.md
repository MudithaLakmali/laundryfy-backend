# Laundrify API Endpoints Reference

This file lists every public API endpoint with: HTTP method, path, authentication, request body type and example, response type and example, and a short description for frontend implementation.

Base URL: `http://<HOST>:<PORT>` (default `http://localhost:8080`)

-------------------------------------------------------------------------------
Authentication
-------------------------------------------------------------------------------

POST /api/auth/signup/customer
- Auth: none
- Request Type: `CustomerSignupRequest`
- Request Body Example:
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
- Response Type: `User` (or created user metadata)
- Response Example:
```json
{ "id": "userId123", "email": "john@example.com", "role": "CUSTOMER" }
```
- Description: Register a new customer user.

POST /api/auth/signup/driver
- Auth: none
- Request Type: `DriverSignupRequest`
- Request Body Example: (includes driver fields)
```json
{
  "firstName":"Ravi",
  "lastName":"Silva",
  "email":"ravi@example.com",
  "password":"DriverPass123",
  "phoneNumber":"+94787654321",
  "address":"Driver Quarters",
  "city":"Colombo",
  "district":"Colombo",
  "postalCode":"00150",
  "province":"Western",
  "licenseNumber":"DL-123456",
  "licenseExpiryDate":"2026-12-31",
  "vehicleType":"Van",
  "vehicleModel":"Toyota Hiace",
  "vehicleNumber":"WP-CD-1234"
}
```
- Response Type: `User` metadata
- Description: Register a driver account and profile.

POST /api/auth/signup/laundry
- Auth: none
- Request Type: `LaundrySignupRequest`
- Request Body Example:
```json
{
  "firstName":"Rajesh",
  "lastName":"Kumar",
  "email":"rajesh@freshwash.lk",
  "password":"LaundryPass123",
  "phoneNumber":"+94112234567",
  "laundryName":"Fresh Wash Laundry",
  "businessRegistrationNumber":"BRN123456",
  "taxId":"TAX987654",
  "description":"Professional laundry",
  "servicesOffered":"Dry Cleaning, Washing",
  "openingHours":"08:00 AM",
  "closingHours":"06:00 PM",
  "ownerFirstName":"Rajesh",
  "ownerLastName":"Kumar",
  "ownerEmail":"rajesh@freshwash.lk",
  "address":"123 Main Street",
  "city":"Colombo",
  "district":"Colombo",
  "postalCode":"00100",
  "province":"Western",
  "contactNumber":"+94112234567"
}
```
- Response Type: `User` / `Laundry` metadata
- Description: Register a laundry business owner and create laundry profile.

POST /api/auth/login
- Auth: none
- Request Type: `LoginRequest`
- Request Body Example:
```json
{ "email": "john@example.com", "password": "Password123!" }
```
- Response Type: `AuthResponse`
- Response Example:
```json
{
  "token": "<JWT_TOKEN>",
  "expiresIn": 3600,
  "userId": "userId123",
  "role": "CUSTOMER"
}
```
- Description: Authenticate user and return JWT token.

POST /api/auth/password-reset/request
- Auth: none
- Request Type: { "email": String }
- Response: 200 OK with message
- Description: Sends a password reset token to user's email.

POST /api/auth/password-reset/verify
- Auth: none
- Request Type: { "token": String, "newPassword": String }
- Response: 200 OK on success
- Description: Verify token and set new password.

POST /api/auth/complete-laundry-registration
- Auth: Bearer token recommended (or public if used after initial signup)
- Consumes: multipart/form-data
- Request Parts:
  - `data` (JSON) — `LaundryRegistrationRequest` as a JSON string (fields: `userId`, `laundryName`, `location`, `contactNumber`, `alternateContactNumber`, `contactEmail`, `website`, `description`, `openingHours`, `closingHours`, `servicesOffered`)
  - `logo` (file) — required logo image
  - `shopImage` (file) — required main shop image
  - `additionalImage1` (file) — optional
  - `additionalImage2` (file) — optional
- Response Type: `AuthResponse` (contains `userId` set to created laundry id on success)
- Example curl:
```bash
curl -X POST http://localhost:8080/api/auth/complete-laundry-registration \
  -H "Authorization: Bearer $TOKEN" \
  -F 'data={"userId":"USER_ID","laundryName":"Fresh Wash","location":"Colombo","contactNumber":"+94112234567"};type=application/json' \
  -F "logo=@/path/to/logo.png" \
  -F "shopImage=@/path/to/shop.jpg"
```
- Description: Uploads media and completes laundry profile creation; saves file paths to `Laundry.logoPath`, `Laundry.shopImagePath`, and `Laundry.additionalImagePaths`.

-------------------------------------------------------------------------------
User
-------------------------------------------------------------------------------

GET /api/user/{userId}
- Auth: Bearer token
- Path Param: `userId` (String)
- Response Type: `UserResponse`
- Response Example:
```json
{ "id":"userId123","name":"John Doe","email":"john@example.com","role":"CUSTOMER","enabled":true }
```
- Description: Retrieve user profile metadata.

PUT /api/user/{userId}
- Auth: Bearer token (user or admin)
- Request Type: partial `User` update fields
- Request Body Example:
```json
{ "name": "John D.", "phoneNumber": "+94712345678", "city":"Colombo" }
```
- Response Type: updated `UserResponse`
- Description: Update user's profile fields.

-------------------------------------------------------------------------------
Laundry
-------------------------------------------------------------------------------

POST /api/laundries
- Auth: Bearer token (role=LAUNDRY)
- Request Type: `Laundry` create DTO (same as signup fields)
- Request Body Example: see `LaundrySignupRequest` above
- Response Type: created `Laundry` object
- Description: Create a new laundry record (for multi-shop accounts).

GET /api/laundries/{laundryId}
- Auth: optional
- Path Param: `laundryId`
- Response Type: `Laundry` object
- Response Example (excerpt):
```json
{
  "id":"laundryId123",
  "laundryName":"Fresh Wash Laundry",
  "address":"123 Main Street",
  "city":"Colombo",
  "district":"Colombo",
  "province":"Western",
  "contactNumber":"+94112234567",
  "servicesOffered":"Dry Cleaning, Washing",
  "logoPath":"uploads/logo.jpg"
}
```
- Description: Get laundry profile and contact info.

PUT /api/laundries/{laundryId}
- Auth: Bearer token (owner or admin)
- Request Type: partial `Laundry` update DTO
- Response Type: updated `Laundry`
- Description: Update laundry profile.

GET /api/laundries
- Auth: optional
- Query Params: `city`, `district`, `province`, `service`, `verified`, `minRating`, `page`, `size`, `sort`
- Response Type: paginated list of `Laundry` objects
- Description: Search/filter laundries.

-------------------------------------------------------------------------------
Nearby Discovery (hierarchical)
-------------------------------------------------------------------------------

GET /api/ratings/laundries/nearby
- Auth: optional (recommended)
- Query Params: `city` (required), `district` (optional), `province` (optional), `radiusKm` (optional)
- Request Type: query parameters
- Response Type: object {
  "searchLevel": String, "totalResults": int, "laundries": [ `LaundryDetailsResponse` ]
}
- Response Example (excerpt):
```json
{
  "searchLevel":"CITY_DISTRICT_PROVINCE",
  "totalResults":2,
  "laundries":[ { "id":"...","laundryName":"Fresh Wash","averageRating":4.2,"totalRatings":15 }, ... ]
}
```
- Description: Cascading search with levels: City+District+Province → City+District → City → Province. Returns aggregated rating fields per laundry.

POST /api/ratings/laundries/nearby
- Auth: optional
- Request Type: `NearbyLaundryRequest`
- Request Body Example:
```json
{ "city":"Colombo","district":"Colombo","province":"Western","radiusKm":10 }
```
- Response Type: same as GET
- Description: Same search as GET but accepts JSON body.

GET /api/ratings/laundries/{laundryId}
- Auth: optional
- Response Type: `LaundryDetailsResponse`
- Response Example (excerpt):
```json
{
  "id":"laundryId123",
  "laundryName":"Fresh Wash Laundry",
  "averageRating":4.2,
  "totalRatings":15,
  "contactNumber":"+94112234567"
}
```
- Description: Get laundry profile including aggregated rating data.

-------------------------------------------------------------------------------
Drivers
-------------------------------------------------------------------------------

GET /api/drivers/{driverId}
- Auth: optional
- Response Type: `Driver` object
- Description: Get driver profile and vehicle info.

GET /api/drivers
- Auth: optional
- Query Params: `city`,`district`,`province`,`available`,`minRating`,`page`,`size`
- Response Type: paginated list of `Driver` objects
- Description: Search/filter drivers.

-------------------------------------------------------------------------------
Orders
-------------------------------------------------------------------------------

POST /api/orders
- Auth: Bearer token (CUSTOMER)
- Request Type: `OrderRequest`
- Request Body Example:
```json
{
  "laundryId":"laundryId123",
  "pickupAddress":"123 Main St",
  "pickupDate":"2024-09-20T10:00:00",
  "items":[ { "service":"Washing","quantity":2,"notes":"delicate" } ],
  "paymentMethod":"CASH"
}
```
- Response Type: `OrderResponse`
- Response Example (excerpt):
```json
{ "orderId":"order123","status":"PENDING","estimatedCost":1200 }
```
- Description: Create a new pickup/order for a laundry.

GET /api/orders/{orderId}
- Auth: Bearer token (owner/admin)
- Response Type: `OrderResponse` full
- Description: Retrieve full order details and status.

GET /api/orders
- Auth: Bearer token
- Query Params: `userId`,`laundryId`,`driverId`,`status`,`page`,`size`
- Response Type: paginated list of `OrderResponse`

PUT /api/orders/{orderId}/status
- Auth: Bearer token (LAUNDRY/DRIVER/ADMIN depending on transition)
- Request Type: { "status": String }
- Response Type: updated `OrderResponse`
- Description: Update order lifecycle status.

-------------------------------------------------------------------------------
Payments
-------------------------------------------------------------------------------

POST /api/payments
- Auth: Bearer token
- Request Type: `PaymentRequest`
- Request Body Example:
```json
{ "orderId":"order123","amount":1200,"method":"ONLINE","currency":"LKR" }
```
- Response Type: `PaymentResponse` (may include redirect URL or provider token)
- Description: Initiate payment; returns provider info or success.

POST /api/payments/callback
- Auth: public (provider)
- Request Type: provider callback payload
- Response: 200 OK
- Description: Webhook to update payment and order status.

-------------------------------------------------------------------------------
Ratings & Reviews
-------------------------------------------------------------------------------

POST /api/ratings/laundry
- Auth: Bearer token (recommended)
- Query Param: `userId` (String) OR derived from token
- Request Type: `LaundryRatingRequest`
- Request Body Example:
```json
{ "laundryId":"laundryId123","rating":4.5,"review":"Great!","images":[] }
```
- Response Type: created `LaundryRating` object
- Description: Add or update a customer's rating for a laundry.

GET /api/ratings/laundry/{laundryId}
- Auth: optional
- Response Type: { "laundryId":String, "averageRating":Double, "totalRatings":int, "ratings": [LaundryRating] }
- Description: List ratings and aggregated score for a laundry.

POST /api/ratings/driver
- Auth: Bearer token
- Request Type: `DriverRatingRequest`
- Description: Add/update driver rating.

GET /api/ratings/driver/{driverId}
- Auth: optional
- Response Type: aggregated driver ratings and list

Notes: Frontend should upload images first (see File Uploads) and pass stored image paths in `images` array.

-------------------------------------------------------------------------------
File Uploads (recommended)
-------------------------------------------------------------------------------

POST /api/uploads/images
- Auth: Bearer token
- Accept: multipart/form-data (field name `file`)
- Response Type: { "path": String }
- Description: Upload an image and receive path to include in DTOs.

-------------------------------------------------------------------------------
Error Format
-------------------------------------------------------------------------------

Errors return appropriate HTTP status and JSON body:
```json
{ "error": "Detailed message" }
```

Common codes: `200`, `201`, `400`, `401`, `403`, `404`, `500`.

-------------------------------------------------------------------------------
Frontend Integration Notes
-------------------------------------------------------------------------------
- Use `Authorization: Bearer <JWT>` for protected endpoints.
- Enforce client-side validation for required fields and rating range (1–5).
- Upload files via `POST /api/uploads/images`, then include returned `path` values in DTOs.
- Implement pagination for list endpoints (`page`, `size`).

-------------------------------------------------------------------------------
Document version: 1.1 — generated May 30, 2026


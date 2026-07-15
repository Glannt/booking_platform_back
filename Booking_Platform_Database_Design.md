# Booking Platform Database Design

> Version: Draft v1

## Modules

-   catalog
-   booking
-   pricing
-   fleet
-   customer
-   payment
-   airport_transfer
-   province_trip
-   tour
-   self_drive

------------------------------------------------------------------------

# Relationships

``` text
ServiceType 1 --- N Service

Service 1 --- N ServiceImage
Service 1 --- N ServicePolicy
Service 1 --- N ServiceOption
Service 1 --- N ServiceReview

Customer 1 --- N Booking
Booking 1 --- N BookingItem
BookingItem 1 --- N BookingVehicle

Service 1 --- N Price
VehicleCategory 1 --- N Price

VehicleCategory 1 --- N Vehicle

Service 1 --- 1 AirportTransfer
Service 1 --- 1 ProvinceTrip
Service 1 --- 1 Tour
Service 1 --- 1 Rental
```

# SQL DDL (core)

## service_type

``` sql
CREATE TABLE service_type (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(255),
    display_order INT DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## service

``` sql
CREATE TABLE service (
    id UUID PRIMARY KEY,
    service_type_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    thumbnail VARCHAR(255),
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_service_type
      FOREIGN KEY(service_type_id)
      REFERENCES service_type(id)
);
```

## service_translation

``` sql
CREATE TABLE service_translation (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    short_description TEXT,
    description TEXT,
    slug VARCHAR(255) NOT NULL,
    CONSTRAINT fk_service_translation_service
      FOREIGN KEY(service_id) REFERENCES service(id),
    CONSTRAINT uq_service_lang UNIQUE(service_id, language_code),
    CONSTRAINT uq_slug_lang UNIQUE(language_code, slug)
);
```

## vehicle_category

``` sql
CREATE TABLE vehicle_category (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    seat INT NOT NULL,
    luggage INT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

## vehicle

``` sql
CREATE TABLE vehicle (
    id UUID PRIMARY KEY,
    vehicle_category_id UUID NOT NULL,
    plate_number VARCHAR(20) UNIQUE,
    brand VARCHAR(100),
    model VARCHAR(100),
    year INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_vehicle_category
      FOREIGN KEY(vehicle_category_id)
      REFERENCES vehicle_category(id)
);
```

## customer

``` sql
CREATE TABLE customer (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    birthday DATE,
    gender VARCHAR(20),
    address TEXT
);
```

## booking

``` sql
CREATE TABLE booking (
    id UUID PRIMARY KEY,
    booking_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    booking_status VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    subtotal DECIMAL(18,2) NOT NULL,
    discount DECIMAL(18,2) DEFAULT 0,
    tax DECIMAL(18,2) DEFAULT 0,
    total DECIMAL(18,2) NOT NULL,
    note TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_booking_customer
      FOREIGN KEY(customer_id) REFERENCES customer(id)
);
```

## booking_item

``` sql
CREATE TABLE booking_item (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL,
    service_id UUID NOT NULL,
    quantity INT NOT NULL,
    price_snapshot JSONB NOT NULL,
    total DECIMAL(18,2) NOT NULL,
    CONSTRAINT fk_booking_item_booking FOREIGN KEY(booking_id) REFERENCES booking(id),
    CONSTRAINT fk_booking_item_service FOREIGN KEY(service_id) REFERENCES service(id)
);
```

## price

``` sql
CREATE TABLE price (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    vehicle_category_id UUID NOT NULL,
    pricing_unit VARCHAR(30) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'VND',
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_price_service FOREIGN KEY(service_id) REFERENCES service(id),
    CONSTRAINT fk_price_vehicle_category FOREIGN KEY(vehicle_category_id) REFERENCES vehicle_category(id)
);
```

## booking_vehicle

``` sql
CREATE TABLE booking_vehicle (
    id UUID PRIMARY KEY,
    booking_item_id UUID NOT NULL,
    vehicle_category_id UUID NOT NULL,
    price_id UUID NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    total DECIMAL(18,2) NOT NULL,
    CONSTRAINT fk_bv_item FOREIGN KEY(booking_item_id) REFERENCES booking_item(id),
    CONSTRAINT fk_bv_vehicle FOREIGN KEY(vehicle_category_id) REFERENCES vehicle_category(id),
    CONSTRAINT fk_bv_price FOREIGN KEY(price_id) REFERENCES price(id)
);
```

# Extension Tables

-   airport_route(id PK,...)

-   airport_transfer(id PK, service_id FK UNIQUE, airport_route_id FK)

-   airport_booking_detail(booking_item_id PK/FK,...)

-   province_route(id PK,...)

-   province_trip(id PK, service_id FK UNIQUE, province_route_id FK)

-   province_booking_detail(booking_item_id PK/FK,...)

-   tour(id PK, service_id FK UNIQUE, duration_days,
    duration_nights,...)

-   tour_day(id PK, tour_id FK, day_number, sort_order)

-   tour_day_translation(id PK, tour_day_id FK, language_code, title,
    description, UNIQUE(tour_day_id,language_code))

-   tour_activity(id PK, tour_day_id FK, start_time, end_time, location,
    sort_order)

-   tour_activity_translation(id PK, tour_activity_id FK, language_code,
    title, description, UNIQUE(tour_activity_id,language_code))

-   rental(id PK, service_id FK UNIQUE, vehicle_id FK, deposit)

-   rental_booking_detail(booking_item_id PK/FK,...)

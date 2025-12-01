-- Migration script for Shipment and Appointment features
-- Date: 2025-12-01

-- Update shipments table
ALTER TABLE shipments
ADD COLUMN shipping_address VARCHAR(500),
ADD COLUMN receiver_name VARCHAR(200),
ADD COLUMN receiver_phone VARCHAR(20),
ADD COLUMN shipping_fee DECIMAL(15,2),
ADD COLUMN weight DECIMAL(10,2),
ADD COLUMN pickup_date DATETIME;

-- Make tracking_number and carrier NOT NULL
UPDATE shipments SET tracking_number = CONCAT('TRACK-', id) WHERE tracking_number IS NULL;
UPDATE shipments SET carrier = 'Giao hàng nhanh' WHERE carrier IS NULL;

ALTER TABLE shipments
MODIFY COLUMN tracking_number VARCHAR(50) NOT NULL,
MODIFY COLUMN carrier VARCHAR(100) NOT NULL;

-- Add new shipment statuses (if needed - enum is in code)
-- PREPARING, CANCELLED are new statuses

-- Create shipment_tracking table
CREATE TABLE IF NOT EXISTS shipment_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    note TEXT,
    timestamp DATETIME NOT NULL,
    location VARCHAR(200),
    updated_by VARCHAR(100),
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    INDEX idx_shipment_tracking_shipment (shipment_id),
    INDEX idx_shipment_tracking_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create appointments table
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    appointment_code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    appointment_time DATETIME NOT NULL,
    end_time DATETIME,
    location VARCHAR(500),
    customer_name VARCHAR(200),
    customer_phone VARCHAR(20),
    assigned_staff VARCHAR(100),
    service_description TEXT,
    notes TEXT,
    cancellation_reason TEXT,
    completed_time DATETIME,
    confirmed_time DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_appointments_order (order_id),
    INDEX idx_appointments_status (status),
    INDEX idx_appointments_time (appointment_time),
    INDEX idx_appointments_staff (assigned_staff),
    INDEX idx_appointments_phone (customer_phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add indexes for better performance
CREATE INDEX idx_shipments_status ON shipments(status);
CREATE INDEX idx_shipments_carrier ON shipments(carrier);
CREATE INDEX idx_shipments_estimated_delivery ON shipments(estimated_delivery_at);

COMMIT;

INSERT INTO booking_status (status)
SELECT 'WAITING'
WHERE NOT EXISTS (SELECT 1 FROM booking_status WHERE status = 'WAITING');

INSERT INTO booking_status (status)
SELECT 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM booking_status WHERE status = 'APPROVED');

INSERT INTO booking_status (status)
SELECT 'REJECTED'
WHERE NOT EXISTS (SELECT 1 FROM booking_status WHERE status = 'REJECTED');
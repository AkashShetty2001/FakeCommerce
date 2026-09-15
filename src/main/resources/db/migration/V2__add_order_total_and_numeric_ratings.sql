-- Add total_amount column to orders table
ALTER TABLE orders ADD COLUMN total_amount DECIMAL(38, 2);

-- Convert ratings from VARCHAR to DECIMAL(2,1) for numeric rating values
-- Note: If any existing products have non-numeric ratings, this will fail.
-- In that case, consider cleaning the data first or wiping the local dev DB.
ALTER TABLE products MODIFY COLUMN ratings DECIMAL(2, 1);

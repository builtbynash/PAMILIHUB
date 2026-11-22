/**
 * Product.java
 * Stores the information of ONE grocery product.
 * Encapsulation: all fields are private and used through methods.
 */
public class Product {

    // Items with this stock or lower are shown as "low stock"
    public static final int LOW_STOCK_LIMIT = 5;

    // Private fields (encapsulation)
    private int productID;
    private String productName;
    private String category;
    private double price;
    private int stockQuantity;

    public Product(int productID, String productName, String category, double price, int stockQuantity) {
        this.productID = productID;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // ---------- Meaningful methods ----------

    /** Returns a readable summary of the product. */
    public String displayProductDetails() {
        return "Product ID: " + productID
                + "\nName: " + productName
                + "\nCategory: " + category
                + "\nPrice: \u20B1" + String.format("%,.2f", price)
                + "\nStock: " + stockQuantity;
    }

    /** True if there are at least 'quantity' pieces in stock. */
    public boolean hasEnoughStock(int quantity) {
        return quantity > 0 && stockQuantity >= quantity;
    }

    /** Removes pieces from stock (used when an order is completed). */
    public void reduceStock(int quantity) {
        if (!hasEnoughStock(quantity)) {
            throw new IllegalArgumentException("Not enough stock for " + productName
                    + ". Only " + stockQuantity + " left.");
        }
        stockQuantity -= quantity;
    }

    /** True when the stock is running low. */
    public boolean isLowStock() {
        return stockQuantity <= LOW_STOCK_LIMIT;
    }

    // ---------- Getters and setters ----------
    public int getProductID() { return productID; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
}
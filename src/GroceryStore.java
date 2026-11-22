import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * GroceryStore.java
 * Manages the whole system. Products, the shopping cart, and completed orders
 * are all stored in ArrayLists (temporary storage, no database or files).
 * Methods throw IllegalArgumentException with a friendly message when input is
 * invalid; Main catches it and shows the message in a dialog.
 */
public class GroceryStore {

    // Private fields (encapsulation)
    private String storeName;
    private ArrayList<Product> products = new ArrayList<>();
    private ArrayList<CartItem> cart = new ArrayList<>();
    private ArrayList<Order> orders = new ArrayList<>();
    private int nextProductID = 1;
    private int nextOrderID = 1;

    public GroceryStore(String storeName) {
        this.storeName = storeName;
    }

    // =====================================================
    // PRODUCT MANAGEMENT
    // =====================================================

    /** Adds a new product (unique ID generated automatically). */
    public Product addProduct(String name, String category, double price, int stock) {
        validateProductInfo(name, category, price, stock);
        if (isNameTaken(name, 0)) {
            throw new IllegalArgumentException("A product named \"" + name.trim() + "\" already exists.");
        }
        Product p = new Product(nextProductID++, name.trim(), category.trim(), price, stock);
        products.add(p);
        return p;
    }

    /** Updates name, category, price, and stock of a product. */
    public void updateProduct(int productID, String name, String category, double price, int stock) {
        Product p = findProductById(productID);
        validateProductInfo(name, category, price, stock);
        if (isNameTaken(name, productID)) {
            throw new IllegalArgumentException("A product named \"" + name.trim() + "\" already exists.");
        }
        p.setProductName(name.trim());
        p.setCategory(category.trim());
        p.setPrice(price);
        p.setStockQuantity(stock);
        // If this product is already in the cart, use the new price
        for (CartItem item : cart) {
            if (item.getProduct() == p) {
                item.refreshPrice();
            }
        }
    }

    /** Deletes a product (not allowed while it is inside the cart). */
    public void deleteProduct(int productID) {
        Product p = findProductById(productID);
        if (findCartItem(productID) != null) {
            throw new IllegalArgumentException("\"" + p.getProductName() + "\" is in the cart. Remove it from the cart first.");
        }
        products.remove(p);
    }

    /** Searches by ID (exact) or by name/category (partial, ignore case). */
    public ArrayList<Product> searchProduct(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a product name, category, or ID to search.");
        }
        String key = keyword.trim().toLowerCase();
        ArrayList<Product> results = new ArrayList<>();
        for (Product p : products) {
            boolean idMatch = String.valueOf(p.getProductID()).equals(key);
            boolean nameMatch = p.getProductName().toLowerCase().contains(key);
            boolean categoryMatch = p.getCategory().toLowerCase().contains(key);
            if (idMatch || nameMatch || categoryMatch) {
                results.add(p);
            }
        }
        return results;
    }

    // =====================================================
    // SHOPPING CART
    // =====================================================

    /** Adds a product to the cart. The total in the cart can never exceed the stock. */
    public void addToCart(int productID, int quantity) {
        Product p = findProductById(productID);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        CartItem existing = findCartItem(productID);
        int alreadyInCart = (existing == null) ? 0 : existing.getQuantity();
        if (!p.hasEnoughStock(alreadyInCart + quantity)) {
            throw new IllegalArgumentException("Not enough stock for " + p.getProductName()
                    + ". Stock: " + p.getStockQuantity() + ", already in cart: " + alreadyInCart + ".");
        }
        if (existing == null) {
            cart.add(new CartItem(p, quantity));
        } else {
            existing.increaseQuantity(quantity);
        }
    }

    /** Removes one product line from the cart. */
    public void removeFromCart(int productID) {
        CartItem item = findCartItem(productID);
        if (item == null) {
            throw new IllegalArgumentException("That item is not in the cart.");
        }
        cart.remove(item);
    }

    public void clearCart() {
        cart.clear();
    }

    /** Total price of everything in the cart. */
    public double getCartTotal() {
        double total = 0;
        for (CartItem item : cart) {
            total += item.getSubtotal();
        }
        return total;
    }

    /** Completes the purchase: checks payment, reduces stock, saves the order. */
    public Order checkout(double amountPaid) {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("The cart is empty. Add products first.");
        }
        // Check stock again (a product may have been edited after it was added)
        for (CartItem item : cart) {
            if (!item.getProduct().hasEnoughStock(item.getQuantity())) {
                throw new IllegalArgumentException("Not enough stock for " + item.getProduct().getProductName()
                        + ". Only " + item.getProduct().getStockQuantity() + " left.");
            }
        }
        double total = getCartTotal();
        if (Double.isNaN(amountPaid) || Double.isInfinite(amountPaid) || amountPaid <= 0) {
            throw new IllegalArgumentException("Amount paid must be greater than zero.");
        }
        if (amountPaid < total) {
            throw new IllegalArgumentException("Amount paid is not enough. Total is \u20B1" + String.format("%,.2f", total) + ".");
        }

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Order order = new Order(nextOrderID++, date, cart, amountPaid);
        for (CartItem item : cart) {
            item.getProduct().reduceStock(item.getQuantity());
        }
        orders.add(order);
        cart.clear();
        return order;
    }

    // =====================================================
    // ORDERS AND STATISTICS
    // =====================================================

    public ArrayList<Order> viewOrderHistory() {
        return new ArrayList<>(orders);
    }

    public Order findOrderById(int orderID) {
        for (Order o : orders) {
            if (o.getOrderID() == orderID) {
                return o;
            }
        }
        throw new IllegalArgumentException("Order ID " + orderID + " does not exist.");
    }

    public int getTotalProducts() { return products.size(); }

    public int getLowStockCount() {
        int count = 0;
        for (Product p : products) {
            if (p.isLowStock()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalOrders() { return orders.size(); }

    public double getTotalSales() {
        double sales = 0;
        for (Order o : orders) {
            sales += o.getTotalAmount();
        }
        return sales;
    }

    // =====================================================
    // LOOKUP HELPERS
    // =====================================================

    public ArrayList<Product> getProducts() { return products; }

    public ArrayList<CartItem> getCart() { return cart; }

    public String getStoreName() { return storeName; }

    /** Finds a product by ID or throws an error if it does not exist. */
    public Product findProductById(int productID) {
        for (Product p : products) {
            if (p.getProductID() == productID) {
                return p;
            }
        }
        throw new IllegalArgumentException("Product ID " + productID + " does not exist.");
    }

    private CartItem findCartItem(int productID) {
        for (CartItem item : cart) {
            if (item.getProduct().getProductID() == productID) {
                return item;
            }
        }
        return null;
    }

    private boolean isNameTaken(String name, int ignoreProductID) {
        for (Product p : products) {
            if (p.getProductID() != ignoreProductID && p.getProductName().equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    private void validateProductInfo(String name, String category, double price, int stock) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }
        if (Double.isNaN(price) || Double.isInfinite(price) || price <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
    }
}
/**
 * CartItem.java
 * ONE line in the shopping cart: a product and how many pieces were chosen.
 * The unit price is saved so a receipt never changes later.
 */
public class CartItem {

    // Private fields (encapsulation)
    private Product product;
    private int quantity;
    private double unitPrice;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    // ---------- Meaningful methods ----------

    /** Price of this line = unit price x quantity. */
    public double getSubtotal() {
        return unitPrice * quantity;
    }

    /** Adds more pieces of the same product to this line. */
    public void increaseQuantity(int extra) {
        if (extra <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        quantity += extra;
    }

    /** Copies the product's latest price (used if the price was edited). */
    public void refreshPrice() {
        unitPrice = product.getPrice();
    }

    /** One-line text used in receipts. */
    public String displayItem() {
        return product.getProductName() + " x" + quantity
                + " @ \u20B1" + String.format("%,.2f", unitPrice);
    }

    // ---------- Getters ----------
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
}
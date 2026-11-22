import java.util.ArrayList;

/**
 * Order.java
 * A COMPLETED purchase (what was bought, total, payment, and change).
 */
public class Order {

    // Private fields (encapsulation)
    private int orderID;
    private String orderDate;
    private ArrayList<CartItem> items;
    private double totalAmount;
    private double amountPaid;

    public Order(int orderID, String orderDate, ArrayList<CartItem> cartItems, double amountPaid) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.items = new ArrayList<>(cartItems);   // copy, so clearing the cart is safe
        this.amountPaid = amountPaid;
        this.totalAmount = 0;
        for (CartItem item : items) {              // loop to compute the total
            totalAmount += item.getSubtotal();
        }
    }

    // ---------- Meaningful methods ----------

    /** Change to give back to the customer. */
    public double getChange() {
        return amountPaid - totalAmount;
    }

    /** Total number of pieces bought in this order. */
    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    /** Builds the printable receipt text (uses PHP so any font can show it). */
    public String generateReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== GROCERIA ==========\n");
        sb.append("Order #").append(orderID).append("\n");
        sb.append("Date: ").append(orderDate).append("\n");
        sb.append("------------------------------\n");
        for (CartItem item : items) {
            sb.append(String.format("%-22s%10s%n",
                    item.getProduct().getProductName() + " x" + item.getQuantity(),
                    String.format("%,.2f", item.getSubtotal())));
        }
        sb.append("------------------------------\n");
        sb.append(String.format("%-16s PHP %9s%n", "TOTAL", String.format("%,.2f", totalAmount)));
        sb.append(String.format("%-16s PHP %9s%n", "PAID", String.format("%,.2f", amountPaid)));
        sb.append(String.format("%-16s PHP %9s%n", "CHANGE", String.format("%,.2f", getChange())));
        sb.append("==============================\n");
        sb.append("   Thank you for shopping!");
        return sb.toString();
    }

    // ---------- Getters ----------
    public int getOrderID() { return orderID; }
    public String getOrderDate() { return orderDate; }
    public ArrayList<CartItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public double getAmountPaid() { return amountPaid; }
}
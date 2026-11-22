import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Main.java
 * The Swing graphical interface of GROCERIA (cashier / point-of-sale style).
 * Layout: a header bar with live statistics, and 3 tabs: Shop, Inventory, Orders.
 * Every button calls a method of the GroceryStore object, then refreshAll()
 * updates the tables and the statistics.
 */
public class Main extends JFrame {

    // Colors (charcoal header + orange accent + cream background)
    private static final Color CHARCOAL = new Color(43, 45, 66);
    private static final Color ORANGE = new Color(242, 140, 40);
    private static final Color CREAM = new Color(255, 248, 240);
    private static final Color GREEN = new Color(46, 125, 80);
    private static final Color RED = new Color(190, 60, 60);
    private static final String PESO = "\u20B1";
    private static final String[] CATEGORIES = {"Canned Goods", "Beverages", "Dairy", "Snacks",
            "Staples", "Fruits & Vegetables", "Household", "Other"};

    // The "brain" of the program
    private GroceryStore store = new GroceryStore("GROCERIA");

    // Header statistics labels
    private JLabel lblProducts = new JLabel("0");
    private JLabel lblLowStock = new JLabel("0");
    private JLabel lblOrders = new JLabel("0");
    private JLabel lblSales = new JLabel("0");

    // Table models
    private DefaultTableModel shopModel = makeModel("Product ID", "Name", "Category", "Price", "Stock");
    private DefaultTableModel cartModel = makeModel("ID", "Product", "Price", "Qty", "Subtotal");
    private DefaultTableModel inventoryModel = makeModel("Product ID", "Name", "Category", "Price", "Stock");
    private DefaultTableModel orderModel = makeModel("Order ID", "Date", "Items", "Total", "Paid", "Change");

    private JTable shopTable = makeTable(shopModel);
    private JTable cartTable = makeTable(cartModel);
    private JTable inventoryTable = makeTable(inventoryModel);
    private JTable orderTable = makeTable(orderModel);

    // Search and filter controls
    private JTextField shopSearch = new JTextField(14);
    private JComboBox<String> categoryFilter = new JComboBox<>();
    private JTextField inventorySearch = new JTextField(16);
    private JLabel lblCartTotal = new JLabel(PESO + "0.00");

    // =====================================================
    // WINDOW SETUP
    // =====================================================
    public Main() {
        setTitle("GROCERIA - Grocery Shopping System");
        setSize(1100, 660);
        setMinimumSize(new Dimension(980, 580));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CREAM);

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.setBackground(CREAM);
        tabs.addTab("Shop", buildShopTab());
        tabs.addTab("Inventory", buildInventoryTab());
        tabs.addTab("Orders", buildOrdersTab());
        tabs.addChangeListener(e -> refreshAll()); // always show fresh data when switching tabs
        add(tabs, BorderLayout.CENTER);

        // Categories for the Shop filter ("All" first)
        categoryFilter.addItem("All");
        for (String c : CATEGORIES) categoryFilter.addItem(c);
        categoryFilter.addActionListener(e -> fillShopTable());

        refreshAll();
    }

    // =====================================================
    // HEADER: brand on the left, live statistics on the right
    // =====================================================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CHARCOAL);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel brand = new JPanel(new GridLayout(2, 1));
        brand.setOpaque(false);
        JLabel name = new JLabel("GROCERIA");
        name.setFont(new Font("SansSerif", Font.BOLD, 28));
        name.setForeground(ORANGE);
        JLabel sub = new JLabel("Grocery Shopping System");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(210, 212, 225));
        brand.add(name);
        brand.add(sub);
        header.add(brand, BorderLayout.WEST);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 0));
        stats.setOpaque(false);
        stats.add(statChip("Products", lblProducts));
        stats.add(statChip("Low Stock", lblLowStock));
        stats.add(statChip("Orders", lblOrders));
        stats.add(statChip("Sales", lblSales));
        header.add(stats, BorderLayout.EAST);
        return header;
    }

    private JPanel statChip(String caption, JLabel valueLabel) {
        JPanel chip = new JPanel(new GridLayout(2, 1));
        chip.setOpaque(false);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(new Font("SansSerif", Font.PLAIN, 11));
        cap.setForeground(new Color(210, 212, 225));
        chip.add(valueLabel);
        chip.add(cap);
        return chip;
    }

    // =====================================================
    // TAB 1: SHOP (products on the left, cart on the right)
    // =====================================================
    private JPanel buildShopTab() {
        // ----- Left side: product list -----
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setBackground(CREAM);
        left.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 6));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.add(new JLabel("Search:"));
        filters.add(shopSearch);
        filters.add(new JLabel("Category:"));
        filters.add(categoryFilter);
        left.add(filters, BorderLayout.NORTH);
        left.add(new JScrollPane(shopTable), BorderLayout.CENTER);

        JButton addCart = new JButton("Add to Cart");
        styleButton(addCart, ORANGE);
        addCart.addActionListener(e -> addToCartAction());
        JLabel tip = new JLabel("Tip: double-click a product to add it");
        tip.setForeground(Color.GRAY);
        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBottom.setOpaque(false);
        leftBottom.add(addCart);
        leftBottom.add(tip);
        left.add(leftBottom, BorderLayout.SOUTH);

        shopSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { fillShopTable(); }
            public void removeUpdate(DocumentEvent e) { fillShopTable(); }
            public void changedUpdate(DocumentEvent e) { fillShopTable(); }
        });
        shopTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) addToCartAction();
            }
        });

        // ----- Right side: cart -----
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 6, 12, 12),
                BorderFactory.createLineBorder(ORANGE, 2)));
        JLabel cartTitle = new JLabel("  Your Cart");
        cartTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        cartTitle.setForeground(CHARCOAL);
        cartTitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        right.add(cartTitle, BorderLayout.NORTH);
        right.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JLabel totalCaption = new JLabel("TOTAL");
        totalCaption.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblCartTotal.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblCartTotal.setForeground(CHARCOAL);
        JButton remove = new JButton("Remove Item");
        JButton checkout = new JButton("CHECKOUT");
        styleButton(remove, RED);
        styleButton(checkout, GREEN);
        checkout.setFont(new Font("SansSerif", Font.BOLD, 16));
        remove.addActionListener(e -> removeFromCartAction());
        checkout.addActionListener(e -> checkoutAction());

        JPanel totalBox = new JPanel(new GridLayout(2, 1));
        totalBox.setOpaque(false);
        totalBox.add(totalCaption);
        totalBox.add(lblCartTotal);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.add(remove);
        buttons.add(checkout);
        JPanel rightBottom = new JPanel(new BorderLayout(0, 8));
        rightBottom.setOpaque(false);
        rightBottom.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        rightBottom.add(totalBox, BorderLayout.NORTH);
        rightBottom.add(buttons, BorderLayout.SOUTH);
        right.add(rightBottom, BorderLayout.SOUTH);

        // ----- Combine -----
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.62);
        split.setBorder(null);
        split.setBackground(CREAM);
        JPanel tab = new JPanel(new BorderLayout());
        tab.add(split, BorderLayout.CENTER);
        return tab;
    }

    // =====================================================
    // TAB 2: INVENTORY (buttons on top, table below)
    // =====================================================
    private JPanel buildInventoryTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 8));
        tab.setBackground(CREAM);
        tab.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton add = new JButton("+ Add Product");
        JButton update = new JButton("Edit");
        JButton delete = new JButton("Delete");
        styleButton(add, GREEN);
        styleButton(update, ORANGE);
        styleButton(delete, RED);
        add.addActionListener(e -> addProductAction());
        update.addActionListener(e -> updateProductAction());
        delete.addActionListener(e -> deleteProductAction());

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftButtons.setOpaque(false);
        leftButtons.add(add);
        leftButtons.add(update);
        leftButtons.add(delete);
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBox.setOpaque(false);
        searchBox.add(new JLabel("Search name, category, or ID:"));
        searchBox.add(inventorySearch);
        toolbar.add(leftButtons, BorderLayout.WEST);
        toolbar.add(searchBox, BorderLayout.EAST);

        tab.add(toolbar, BorderLayout.NORTH);
        tab.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        inventorySearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { fillInventoryTable(); }
            public void removeUpdate(DocumentEvent e) { fillInventoryTable(); }
            public void changedUpdate(DocumentEvent e) { fillInventoryTable(); }
        });
        return tab;
    }

    // =====================================================
    // TAB 3: ORDERS (double-click an order to see its receipt)
    // =====================================================
    private JPanel buildOrdersTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 8));
        tab.setBackground(CREAM);
        tab.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton view = new JButton("View Receipt");
        styleButton(view, ORANGE);
        view.addActionListener(e -> viewReceiptAction());
        JLabel tip = new JLabel("Tip: double-click an order to see its receipt");
        tip.setForeground(Color.GRAY);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        top.add(view);
        top.add(tip);

        tab.add(top, BorderLayout.NORTH);
        tab.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewReceiptAction();
            }
        });
        return tab;
    }

    // =====================================================
    // BUTTON ACTIONS
    // =====================================================

    // ----- Inventory -----
    private void addProductAction() {
        JTextField name = new JTextField(12);
        JComboBox<String> category = new JComboBox<>(CATEGORIES);
        JTextField price = new JTextField(12);
        JTextField stock = new JTextField(12);
        JPanel form = productForm(name, category, price, stock);

        // Loop: if the input is invalid, show the message and reopen the form
        while (true) {
            int choice = JOptionPane.showConfirmDialog(this, form, "Add Product",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) return; // Cancel or closed
            try {
                double priceValue = parseAmount(price.getText(), "Price");
                int stockValue = parseWholeNumber(stock.getText(), "Stock", 0);
                Product p = store.addProduct(name.getText(), (String) category.getSelectedItem(), priceValue, stockValue);
                refreshAll();
                info("Product added (Product ID " + p.getProductID() + ").");
                return;
            } catch (IllegalArgumentException ex) {
                warn(ex.getMessage());
            }
        }
    }

    private void updateProductAction() {
        Product p = selectedProduct(inventoryTable, inventoryModel);
        if (p == null) return;

        JTextField name = new JTextField(p.getProductName(), 12);
        JComboBox<String> category = new JComboBox<>(CATEGORIES);
        category.setSelectedItem(p.getCategory());
        JTextField price = new JTextField(String.valueOf(p.getPrice()), 12);
        JTextField stock = new JTextField(String.valueOf(p.getStockQuantity()), 12);
        JPanel form = productForm(name, category, price, stock);

        while (true) {
            int choice = JOptionPane.showConfirmDialog(this, form, "Edit Product",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) return;
            try {
                double priceValue = parseAmount(price.getText(), "Price");
                int stockValue = parseWholeNumber(stock.getText(), "Stock", 0);
                store.updateProduct(p.getProductID(), name.getText(), (String) category.getSelectedItem(), priceValue, stockValue);
                refreshAll();
                info("Product updated successfully.");
                return;
            } catch (IllegalArgumentException ex) {
                warn(ex.getMessage());
            }
        }
    }

    private void deleteProductAction() {
        Product p = selectedProduct(inventoryTable, inventoryModel);
        if (p == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete \"" + p.getProductName() + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            store.deleteProduct(p.getProductID());
            refreshAll();
            info("Product deleted.");
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        }
    }

    // ----- Shop -----
    private void addToCartAction() {
        Product p = selectedProduct(shopTable, shopModel);
        if (p == null) return;

        // Input dialog: returns null when Cancel is pressed
        String input = JOptionPane.showInputDialog(this,
                "How many \"" + p.getProductName() + "\"? (Stock: " + p.getStockQuantity() + ")",
                "Add to Cart", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            int qty = parseWholeNumber(input, "Quantity", 1);
            store.addToCart(p.getProductID(), qty);
            refreshAll();
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        }
    }

    private void removeFromCartAction() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { warn("Please select an item in the cart first."); return; }
        try {
            int id = Integer.parseInt(String.valueOf(cartModel.getValueAt(row, 0)));
            store.removeFromCart(id);
            refreshAll();
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        }
    }

    private void checkoutAction() {
        if (store.getCart().isEmpty()) { warn("The cart is empty. Add products first."); return; }

        double total = store.getCartTotal();
        String input = JOptionPane.showInputDialog(this,
                "Total: " + PESO + String.format("%,.2f", total) + "\nEnter amount paid:",
                "Checkout", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return; // Cancel
        try {
            double paid = parseAmount(input, "Amount paid");
            Order order = store.checkout(paid);
            refreshAll();
            showReceipt(order);
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        }
    }

    // ----- Orders -----
    private void viewReceiptAction() {
        int row = orderTable.getSelectedRow();
        if (row < 0) { warn("Please select an order in the table first."); return; }
        try {
            int id = Integer.parseInt(String.valueOf(orderModel.getValueAt(row, 0)));
            showReceipt(store.findOrderById(id));
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        }
    }

    private void showReceipt(Order order) {
        JTextArea area = new JTextArea(order.generateReceipt());
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, area, "Receipt - Order #" + order.getOrderID(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    // =====================================================
    // REFRESH: reload every table and statistic
    // =====================================================
    private void refreshAll() {
        lblProducts.setText(String.valueOf(store.getTotalProducts()));
        lblLowStock.setText(String.valueOf(store.getLowStockCount()));
        lblOrders.setText(String.valueOf(store.getTotalOrders()));
        lblSales.setText(PESO + String.format("%,.2f", store.getTotalSales()));

        fillShopTable();
        fillInventoryTable();

        cartModel.setRowCount(0);
        for (CartItem item : store.getCart()) {
            cartModel.addRow(new Object[]{item.getProduct().getProductID(), item.getProduct().getProductName(),
                    PESO + String.format("%,.2f", item.getUnitPrice()), item.getQuantity(),
                    PESO + String.format("%,.2f", item.getSubtotal())});
        }
        lblCartTotal.setText(PESO + String.format("%,.2f", store.getCartTotal()));

        orderModel.setRowCount(0);
        for (Order o : store.viewOrderHistory()) {
            orderModel.addRow(new Object[]{o.getOrderID(), o.getOrderDate(), o.getTotalItemCount(),
                    PESO + String.format("%,.2f", o.getTotalAmount()),
                    PESO + String.format("%,.2f", o.getAmountPaid()),
                    PESO + String.format("%,.2f", o.getChange())});
        }
    }

    // Shop table: search box + category filter
    private void fillShopTable() {
        String key = shopSearch.getText().trim();
        String category = (String) categoryFilter.getSelectedItem();
        ArrayList<Product> list = key.isEmpty() ? store.getProducts() : store.searchProduct(key);
        shopModel.setRowCount(0);
        for (Product p : list) {
            if (category == null || category.equals("All") || category.equals(p.getCategory())) {
                shopModel.addRow(productRow(p));
            }
        }
    }

    // Inventory table: search box only
    private void fillInventoryTable() {
        String key = inventorySearch.getText().trim();
        ArrayList<Product> list = key.isEmpty() ? store.getProducts() : store.searchProduct(key);
        inventoryModel.setRowCount(0);
        for (Product p : list) {
            inventoryModel.addRow(productRow(p));
        }
    }

    private Object[] productRow(Product p) {
        return new Object[]{p.getProductID(), p.getProductName(), p.getCategory(),
                PESO + String.format("%,.2f", p.getPrice()), p.getStockQuantity()};
    }

    // =====================================================
    // SMALL HELPERS
    // =====================================================

    /** Returns the product selected in a table, or warns and returns null. */
    private Product selectedProduct(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Please select a product in the table first."); return null; }
        int id = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
        try {
            return store.findProductById(id);
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
            return null;
        }
    }

    /** Converts text to a positive decimal number or throws a friendly error. */
    private double parseAmount(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        double value;
        try {
            value = Double.parseDouble(text.trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a number (letters are not allowed).");
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
        return value;
    }

    /** Converts text to a whole number that is at least 'min'. */
    private int parseWholeNumber(String text, String fieldName, int min) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        int value;
        try {
            value = Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a whole number (no letters or decimals).");
        }
        if (value < min) {
            throw new IllegalArgumentException(fieldName + " must be at least " + min + ".");
        }
        return value;
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private JPanel productForm(JTextField name, JComboBox<String> category, JTextField price, JTextField stock) {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Product Name:")); form.add(name);
        form.add(new JLabel("Category:")); form.add(category);
        form.add(new JLabel("Price (" + PESO + "):")); form.add(price);
        form.add(new JLabel("Stock Quantity:")); form.add(stock);
        return form;
    }

    private static DefaultTableModel makeModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private static JTable makeTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionBackground(ORANGE);
        table.setSelectionForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(230, 225, 218));
        table.getTableHeader().setBackground(CHARCOAL);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        return table;
    }

    private static void styleButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    // =====================================================
    // PROGRAM ENTRY POINT
    // =====================================================
    public static void main(String[] args) {
        try {
            // Metal look-and-feel lets our colors show on every OS
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("TabbedPane.selected", ORANGE);
            UIManager.put("TabbedPane.contentAreaColor", CREAM);
        } catch (Exception ex) {
            // If it fails, the default look-and-feel is used
        }
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
import java.time.LocalDateTime;
import java.util.*;

class Order {
    enum OrderType {
        BUY, SELL
    }

    private static int orderIdCounter = 0;
    private int orderId;
    private OrderType type;
    private double price;
    private int quantity;
    private LocalDateTime timestamp;

    public Order(OrderType type, double price, int quantity) {
        this.orderId = ++orderIdCounter;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
    }

    public int getOrderId() {
        return orderId;
    }

    public OrderType getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("OrderID: %d, Type: %s, Price: %.2f, Quantity: %d, Timestamp: %s",
                orderId, type, price, quantity, timestamp);
    }
}

class MatchingEngine {
    private PriorityQueue<Order> buyOrders;
    private PriorityQueue<Order> sellOrders;

    public MatchingEngine() {
        // Buy orders sorted by highest price, earliest timestamp
        buyOrders = new PriorityQueue<>((o1, o2) -> {
            if (o1.getPrice() != o2.getPrice()) {
                return Double.compare(o2.getPrice(), o1.getPrice());
            }
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        });

        // Sell orders sorted by lowest price, earliest timestamp
        sellOrders = new PriorityQueue<>((o1, o2) -> {
            if (o1.getPrice() != o2.getPrice()) {
                return Double.compare(o1.getPrice(), o2.getPrice());
            }
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        });
    }

    public void placeOrder(Order order) {
        System.out.printf("Placing order: %s\n", order);
        if (order.getType() == Order.OrderType.BUY) {
            matchOrder(order, sellOrders);
            if (order.getQuantity() > 0) {
                buyOrders.add(order);
            }
        } else {
            matchOrder(order, buyOrders);
            if (order.getQuantity() > 0) {
                sellOrders.add(order);
            }
        }
    }

    private void matchOrder(Order incomingOrder, PriorityQueue<Order> opposingOrders) {
        while (!opposingOrders.isEmpty() && incomingOrder.getQuantity() > 0) {
            Order topOrder = opposingOrders.peek();

            if (incomingOrder.getType() == Order.OrderType.BUY && incomingOrder.getPrice() >= topOrder.getPrice() ||
                    incomingOrder.getType() == Order.OrderType.SELL
                            && incomingOrder.getPrice() <= topOrder.getPrice()) {

                int matchQuantity = Math.min(incomingOrder.getQuantity(), topOrder.getQuantity());
                System.out.printf("\nMatch Found:\nBuyer: %s\nSeller: %s\nMatched Quantity: %d\n\n",
                        incomingOrder, topOrder, matchQuantity);

                incomingOrder.setQuantity(incomingOrder.getQuantity() - matchQuantity);
                topOrder.setQuantity(topOrder.getQuantity() - matchQuantity);

                if (topOrder.getQuantity() == 0) {
                    opposingOrders.poll();
                }

            } else {
                break;
            }
        }
    }

    public void displayOrders() {
        System.out.println("\nRemaining Buy Orders:");
        if (buyOrders.isEmpty()) {
            System.out.println("No Buy Orders");
        } else {
            buyOrders.forEach(System.out::println);
        }

        System.out.println("\nRemaining Sell Orders:");
        if (sellOrders.isEmpty()) {
            System.out.println("No Sell Orders");
        } else {
            sellOrders.forEach(System.out::println);
        }
    }
}

public class MatchingEngineDemo {
    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();

        Random random = new Random();
        double basePrice = 75.0; // Set base price to reduce volatility

        for (int i = 0; i < 50; i++) {
            Order.OrderType type = random.nextBoolean() ? Order.OrderType.BUY : Order.OrderType.SELL;
            double price = basePrice + (random.nextDouble() * 10 - 5); // Price variation between -5 and +5
            int quantity = 1 + random.nextInt(100); // Quantity between 1 and 100
            // System.out.println(quantity);

            Order order = new Order(type, Math.round(price * 100.0) / 100.0, quantity); // Round price to 2 decimals
            // System.out.println("Qty: " + order.getQuantity());
            engine.placeOrder(order);

            try {
                Thread.sleep(1000); // Pause for 1 second
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
            }

        }

        engine.displayOrders();
    }
}

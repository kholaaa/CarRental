package car.rental.system;

import javafx.beans.property.*;

public class Car {

    private final IntegerProperty carId;
    private final StringProperty model;
    private final StringProperty type;
    private final StringProperty color;
    private final IntegerProperty price;
    private final StringProperty availability;

    public Car(int carId, String model, String type, String color, int price, String availability) {
        this.carId = new SimpleIntegerProperty(carId);
        this.model = new SimpleStringProperty(model);
        this.type = new SimpleStringProperty(type);
        this.color = new SimpleStringProperty(color);
        this.price = new SimpleIntegerProperty(price);
        this.availability = new SimpleStringProperty(availability);
    }

    public IntegerProperty carIdProperty() { return carId; }
    public StringProperty modelProperty() { return model; }
    public StringProperty typeProperty() { return type; }
    public StringProperty colorProperty() { return color; }
    public IntegerProperty priceProperty() { return price; }
    public StringProperty availabilityProperty() { return availability; }
}
package com.example.carrental;



import java.sql.Date;

public class RentalReport {

    private int bookingID, carID, customerID;
    private Date entryDate, returnDate;
    private double rentPerDay, totalAmount;
    private String status;

    public RentalReport(int bookingID, int carID, int customerID, Date entryDate, Date returnDate,
                        double rentPerDay, double totalAmount, String status) {
        this.bookingID = bookingID;
        this.carID = carID;
        this.customerID = customerID;
        this.entryDate = entryDate;
        this.returnDate = returnDate;
        this.rentPerDay = rentPerDay;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters
    public int getBookingID() { return bookingID; }
    public int getCarID() { return carID; }
    public int getCustomerID() { return customerID; }
    public Date getEntryDate() { return entryDate; }
    public Date getReturnDate() { return returnDate; }
    public double getRentPerDay() { return rentPerDay; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
}

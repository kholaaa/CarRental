# CarRental
"CAR RENTAL SYSTEM" using JavaFAX for GUI.
// insert ss
![Screenshot 2026-01-07 090450](https://github.com/user-attachments/assets/21010b6c-9abd-46a7-8787-14ff8fcc9de4)

Login / Signup screens
 Loading splash screens
 Dashboard
 Add/View Cars
 Book Car with Chalan
Generate Report.
**Features for users**
**User Authentication**
  - Login, Signup, Forgot Password
- **Splash & Loading Screens**
  - Professional multi-stage loading animation
  **Dashboard**
   Central navigation to all modules
  **Car Management**
   Add new cars
   View all cars (with availability status) in a TableView
  **Customer Management**
   Register customer details
  **Booking System**
    Select available cars via ComboBox
    Enter booking dates
    Generate instant rental chalan (invoice) with total rent calculation
    Update car availability to "No" on booking
 **Return System**
   Record return details (fuel level, condition, date)
   Automatically set car availability back to "Yes"
 **Report Generation**
    View all bookings with status (Pending / Returned)
   Calculate and display total revenue earned.
  **Technologies used**
 **Java**
 (JDK 8 or higher)
  **JavaFX**
   For rich desktop UI
 **MySQL**
  Relational database for persistent storage
 **Scene Builder**
 (optional) For designing FXML layouts
 **JDBC**
  For database connectivity
**Project Structure**
src/ com/example/carrental/

├── HelloApplication.java  

*Initial entry point (pics.fxml → login/loading)
├── login.java    * Login screen launcher
├── Dashboard.java                  * Dashboard launcher
├── controllers/                    * All FXML controllers

   ├── loginController.java
   ├── SignUpController.java
   ├── forget_passwordController.java
   ├── loadingController.java
   ├── loading2Controller.java
   ├── DashboardController.java
   ├── add_carController.java
   ├── ViewAvailableCarsController.java
   ├── customerController.java
   ├── bookcarController.java
   ├── return_carController.java
   └── generate_reportController.java
   
 **model**
      Car.java
      RentalReport.java
      DBConnection.java               * Database connection utility
  **resources**
       com/example/carrental/      * FXML files and images
       
  **DATABASE CODE**
    
CREATE TABLE login (
    name VARCHAR(50),
    email VARCHAR(100) PRIMARY KEY,
    password VARCHAR(50),
    PhoneNumber VARCHAR(20)
);

**Cars**
CREATE TABLE cars (
    carID INT PRIMARY KEY,
    carmodel VARCHAR(50),
    carType VARCHAR(50),
    colour VARCHAR(30),
    price_per_day INT,
    Availability VARCHAR(10) DEFAULT 'yes'
);

 **Customers**
CREATE TABLE customers (
    customerID INT PRIMARY KEY,
    name VARCHAR(50),
    phone VARCHAR(20)
);

**Bookings**
CREATE TABLE bookcar (
    bookcarID INT AUTO_INCREMENT PRIMARY KEY,
    customerID INT,
    carID INT,
    entrydate DATE,
    returndate DATE,
    FOREIGN KEY (customerID) REFERENCES customers(customerID),
    FOREIGN KEY (carID) REFERENCES cars(carID)
);

Returns
CREATE TABLE returncar (
    returnID INT AUTO_INCREMENT PRIMARY KEY,
    fuellevel INT,
    carcondition VARCHAR(100),
    carID INT,
    customerID INT,
    returndate DATE
);

**Steps includes**

Clone the repositoryBashgit clone https://github.com/yourusername/car-rental-system.git
Set up MySQL Database
Create database: CREATE DATABASE CarRental;
Run the table creation scripts above

Update Database Credentials (in DBConnection.java)Javastatic final String URL = "jdbc:mysql://127.0.0.1:3306/CarRental";
static final String USER = "root";
static final String PASSWORD = "your_password_here";
Add JavaFX to your project
If using IntelliJ/Eclipse: Add JavaFX library/modules
Or run with VM options:Bash--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml

**Run the Application**
Main class: com.example.carrental.HelloApplication (or login depending on entry point).



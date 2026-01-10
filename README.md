# CarRental
"CAR RENTAL SYSTEM" using JavaFAX for GUI.

![Screenshot 2026-01-07 090450](https://github.com/user-attachments/assets/21010b6c-9abd-46a7-8787-14ff8fcc9de4)

Login / Signup screens
**Features for users**
**User Authentication**
  - Login, Signup, Forgot Password
- **Splash & Loading Screens**
  - Professional multi-stage loading animation
 Loading splash screens

 **Dashboard**

![Welcome Screen.jpeg](https://github.com/kholaaa/CarRental/blob/07dec8de8199fc9a8b76c54a328072dec94229b6/welcom%20screen.jpeg)

Dashboard consist of following tables:
  book a car, view avaliable cars, add cars, customer details, return a car, generate a report and lastly logout.
  
  **Car Management**
  
 ![BookaCar.jpeg](https://github.com/kholaaa/CarRental/blob/864e71fd655f263d820a80e893c8d8a5740fe8cb/BookaCar.jpeg)
 
   Add new cars
   View all cars (with availability status) in a TableView
 
  **Customer Management**
  
   ![CustomerDetails.jpeg](https://github.com/kholaaa/CarRental/blob/864e71fd655f263d820a80e893c8d8a5740fe8cb/CustomerDetails.jpeg)

     Register customer details such as:
   Customer ID, Name, Email and PhoneNumber.
  **Booking System**

   ![AvaliableCars.jpeg](https://github.com/kholaaa/CarRental/blob/edca0c6ea5c7dc992a2c6b0d730ee0e8a8f7afa6/AvaliableCars.jpeg)

     Select available cars.
    Enter Car ID, model, Brand and price per day.
    Update car availability to "No" on booking.
    
 **Return System**
  
   ![ReturnCar.jpeg](https://github.com/kholaaa/CarRental/blob/1dc662207e0f2bfd7ffe720a1b2793caea864674/ReturnCar.jpeg)

    Record return details.
   Automatically set car availability back to "Yes"
 **Report Generation**
    
   ![CarRental\Report.jpeg](https://github.com/kholaaa/CarRental/blob/864e71fd655f263d820a80e893c8d8a5740fe8cb/Report.jpeg)

   View all bookings with status which includes:
    Customer ID 
    Car ID 
    Rental Days.
    
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



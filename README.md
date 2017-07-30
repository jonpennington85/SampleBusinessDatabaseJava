# SampleBusinessDatabaseJava
A sample database that manages products and orders written in java using a sample 'northwind' database

This program is written in Java. It assumes you have a Java VM installed and working. 
It can be compiled using the command "javac UserInterface.java Communication.java" and run using the command "java UserInterface"

It also assumes you have the correct Java Database Connector (com.mysql.jdbc.Driver) working 
and added to your CLASSPATH. It will exit if it cannot find the driver.

Run SQL script northwind.sql to create the northwind sample schema load sample data in MySQL.

When the program starts, it will attempt to connect to the mysql database at localhost.
The program asks for a user name and password to connect. Once it is connected, it will run the "use northwind" command,
and print a list of options that the user can type to modify and query the database

Available options are:

add customer
add order
remove order
ship order
print pending orders
restock
list products in stock
list products out of stock
exit

add customer
	This command adds a new customer to the database. It prompts the user for the various data, performs basic validity checks,
	and adds the customer data to the database. It will not allow blank entries and checks to make sure the customerID is not already taken.

add order
	This command adds a new order to the database. It prompts the user for the various data, and performs basic validity checks,
	makes sure the customerID is registered, the employeeID exists, has the option of getting the current system time for order date, 
	checks for a valid ShipperID, and allows multiple products to be ordered in a single order by adding one row for each product in the 
	order_details table. It asks for the product name and the quantity, and makes sure the product has enough UnitsInStock for quantity. 
	Upon placing an order, the UnitsInStock in the products table is decreased by quantity, and the UnitsOnOrder in the products table 
	is increased by quantity. By the assignment specifications, if a user enters a discount higher than 0.00, the order is cancelled.

remove order
	This command removes an order. It asks for an existing orderID, and removes all traces of the order. It increments UnitsInStock 
	of a product by the quantity in each row of order_details with the given orderID, and decrements UnitsOnOrder by the same. An error was noticed
	in the northwind.sql file in which UnitsOnOrder for some products is less than the quantity of those in order_details, even for orders not
	yet shipped. A work-around was done in which the UnitsOnOrder is never allowed to go negative, remaining at 0 instead.

ship order
	This command ships an order. It asks for an existing orderID, and checks to see whether that order has shipped. If it hasn't, the user
	is asked for a shipment date, and given the option of choosing the current system time for the shipment date. The order is then updated 
	with the shippingDate.

print pending orders
	This command prints all orders that have not yet shipped ordered by orderDate. It prints the orderID, customerID, orderDate, Name,
	Address, City, Region, PostalCode, and Country of all orders with a NULL shipment date on one line per order.
restock
	This command updates UnitsInStock with UnitsInStock plus the quantity given. It allows for negative numbers to reduce stock in case
	of missing stock from thieving employees. It does not allow UnitsInStock to go negative, rejecting the update if the new quantity is negative.
	It also does not allow a product to be restocked if it has been discontinued.

list products in stock
	This prints out a list of products that are currently in stock

list products out of stock
	This prints out a list of products that are currently not in stock

exit
	This closes the connection to the mysql database and exits the program. It is the preferred way to exit the program.

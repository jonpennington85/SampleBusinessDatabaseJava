import java.sql.*;
import java.util.*;

public class Communication {
	Connection con;
	ResultSet queryResults;
	Statement statement;

//*************************************************************************************/

	public Communication(Scanner scan) throws SQLException{

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.out.println("Missing Driver. Please add your jdbc driver to CLASSPATH");
			e1.printStackTrace();
			System.exit(-1);
		}

		queryResults= null;

		// We're going to connect to the MYSQL localhost, keep trying until we connect
		boolean isConnected=false;
		System.out.println("Welcome to the System. Attempting MYSQL login");
		while(!isConnected){
			try{
				System.out.print("MYSQL username: ");
				String Username=scan.nextLine();
				System.out.print("MYSQL password: ");
				String Password=scan.nextLine();
				con = DriverManager.getConnection("jdbc:mysql://localhost/", Username, Password);
				isConnected=true;
			}
			catch(SQLException e){
				System.out.println("Could not connect to localhost with Username and Password. " +
						"\nPlease try again");
			}
		}


		statement = con.createStatement();

		statement.execute("use northwind");

	}

//*************************************************************************************/

	public String FindProduct(String productName) throws SQLException{
		queryResults=statement.executeQuery("SELECT ProductID FROM products WHERE ProductName='"+productName+"';");
		queryResults.next();
		return queryResults.getString(1);
	}

//*************************************************************************************/

	public boolean isCustomer(String customerID) throws SQLException{
		queryResults=statement.executeQuery("SELECT CustomerID FROM customers WHERE CustomerID='"+customerID+"';");
		if(queryResults.next()){
			return true;
		}
		else return false;
	}

//*************************************************************************************/

	public boolean isEmployee(String employeeID) throws SQLException{
		queryResults=statement.executeQuery("SELECT EmployeeID FROM employees WHERE EmployeeID='"+employeeID+"';");
		if(queryResults.next()){
			return true;
		}
		else return false;
	}

//*************************************************************************************/

	public boolean isShipper(String shipVia) throws SQLException{
		queryResults=statement.executeQuery("SELECT ShipperID FROM shippers WHERE ShipperID='"+shipVia+"';");
		if(queryResults.next()){
			return true;
		}
		else return false;
	}

//*************************************************************************************/

	public boolean isRestockable(String productID) throws SQLException{
		queryResults=statement.executeQuery("SELECT discontinued FROM products WHERE ProductID = "+productID+";");
		queryResults.next();
		if(queryResults.getString(1).equalsIgnoreCase("y")) return false;
		else return true;
	}

//*************************************************************************************/

	public boolean isOrder(String orderID) throws SQLException{
		queryResults=statement.executeQuery("SELECT orderID from orders where orderID="+orderID+";");
		if(queryResults.next()) return true;
		else return false;
	}

//*************************************************************************************/

	public boolean isShippable(String orderID) throws SQLException{
		queryResults=statement.executeQuery("SELECT ShippedDate from orders where orderID="+orderID+";");
		queryResults.next();
		String result=queryResults.getString(1);
		if(result==null) return true;
		else return false;
	}

//*************************************************************************************/

	public int GetNewOrderID() throws SQLException{
		queryResults=statement.executeQuery("SELECT MAX(OrderID) FROM orders;");
		queryResults.next();
		String orderid=queryResults.getString(1);
		int newOrder=Integer.parseInt(orderid);
		newOrder++;
		return newOrder;
	}

//*************************************************************************************/

	public int getNewOrderDetailsID() throws SQLException{
		queryResults=statement.executeQuery("SELECT MAX(ID) FROM order_details;");
		queryResults.next();
		String id=queryResults.getString(1);
		int newOrderDetails=Integer.parseInt(id);
		newOrderDetails++;
		return newOrderDetails;
	}

//*************************************************************************************/

	public float getUnitPrice(String ProductID) throws SQLException{
		queryResults=statement.executeQuery("SELECT unitprice FROM products WHERE ProductID="+ProductID+";");
		queryResults.next();
		return Float.parseFloat(queryResults.getString(1));
	}

//*************************************************************************************/

	public void AddCustomer(String CustomerID, String CompanyName, String ContactName, String ContactTitle,
			String Address, String City, String Region, String PostalCode, String Country, String Phone,
			String Fax){

		// Add SQL statement
		try {
			statement.executeUpdate("INSERT INTO customers VALUES ('"+CustomerID+"','"+CompanyName+"','"+ContactName+
					"','"+ContactTitle+"','"+Address+"','"+City+"','"+Region+"','"+PostalCode+"','"+Country+
					"','"+Phone+"','"+Fax+"');");
		} catch (SQLException e) {
			System.out.println("Something has gone horribly awry with the SQL Statement for adding a new customer");
			e.printStackTrace();
			// return early if SQL statement messes up
			return;
		}

		System.out.println("\nAdd Customer Complete\n");
		return;
	}

//*************************************************************************************/

	public void ListProductsOutOfStock() throws SQLException{
		queryResults=statement.executeQuery("SELECT ProductName FROM products WHERE UnitsInStock=0");
		while(queryResults.next()) System.out.println(queryResults.getString(1));
		return;
	}

//*************************************************************************************/

	public void ListProductsInStock() throws SQLException{
		queryResults=statement.executeQuery("SELECT ProductName FROM products WHERE UnitsInStock>0");
		while(queryResults.next()) System.out.println(queryResults.getString(1));
		return;
	}

//*************************************************************************************/

	public void AddOrder(String CustomerID, String EmployeeID, String OrderDate, String RequiredDate,
			String ShippedDate, String ShipVia, String Freight, String ShipName, String ShipAddress, 
			String ShipCity, String ShipRegion, String ShipPostalCode, String ShipCountry, int[] quantity,
			float[] discount, String[] ProductID, int numberOfProducts) throws SQLException{
		int orderID=GetNewOrderID();

		if(OrderDate.equalsIgnoreCase("")) OrderDate="NULL";
		else OrderDate="'"+OrderDate+"'";

		if(RequiredDate.equalsIgnoreCase("")) RequiredDate="NULL";
		else RequiredDate="'"+RequiredDate+"'";

		if(ShippedDate.equalsIgnoreCase("")) ShippedDate="NULL";
		else ShippedDate="'"+ShippedDate+"'";

		// First we check to make sure all the orders are in stock. Cancel order if they aren't
		for(int i=0;i<=numberOfProducts;i++){
			int newStockNumber=GetUnitsInStock(ProductID[i])-quantity[i];
			if(newStockNumber<0){
				System.out.println("Not enough product for product "+ProductID[i]+". Cancelling order.");
				return;
			}
		}

		statement.executeUpdate("INSERT INTO orders VALUES ("+GetNewOrderID()+",'"+CustomerID+"',"+EmployeeID+
				","+OrderDate+","+RequiredDate+","+ShippedDate+","+ShipVia+","+Freight+",'"+ShipName+
				"','"+ShipAddress+"','"+ShipCity+"','"+ShipRegion+"','"+ShipPostalCode+"','"+ShipCountry+"');");

		for(int i=0;i<=numberOfProducts;i++){

			float unitPrice=getUnitPrice(ProductID[i]);
			statement.executeUpdate("INSERT INTO order_details VALUES ("+getNewOrderDetailsID()+","+orderID
					+","+ProductID[i]+","+unitPrice+","+quantity[i]+","+discount[i]+");");

			// Now we increment UnitsOnOrder for i
			int newOrderNumber=GetUnitsOnOrder(ProductID[i])+quantity[i];
			statement.executeUpdate("UPDATE products SET UnitsOnOrder = "+newOrderNumber+" WHERE ProductID="+ProductID[i]+";");

			// Now we decrement UnitsInStock for i
			int newStockNumber=GetUnitsInStock(ProductID[i])-quantity[i];
			statement.executeUpdate("UPDATE products SET UnitsInStock = "+newStockNumber+" WHERE ProductID="+ProductID[i]+";");

		}



		System.out.println("\nAdd Order Complete\n");

	}

//*************************************************************************************/

	public int GetUnitsOnOrder(String ProductID) throws SQLException{
		queryResults=statement.executeQuery("SELECT UnitsOnOrder FROM products WHERE ProductID="+ProductID+";");
		queryResults.next();
		return Integer.parseInt(queryResults.getString(1));
	}

//*************************************************************************************/

	public int GetUnitsInStock(String productID) throws SQLException{
		queryResults=statement.executeQuery("SELECT UnitsInStock from products where productID="+productID+";");
		queryResults.next();
		return Integer.parseInt(queryResults.getString(1));
	}

//*************************************************************************************/

	public void RemoveOrder(String orderID) throws SQLException{

		int quantity=0;
		int newOrders=0;
		int newStock=0;
		String productID=null;
		ResultSet queryResults2=null;
		int numberOfRows=0;
		queryResults2=statement.executeQuery("SELECT productID, quantity FROM order_details WHERE OrderID="+orderID+";");
		while(queryResults2.next()){
			numberOfRows++;
		}
		int toStep=1;

		while(toStep<=numberOfRows){
			queryResults2=statement.executeQuery("SELECT productID, quantity FROM order_details WHERE OrderID="+orderID+";");
			for(int i=0;i<toStep;i++){
				queryResults2.next();
			}
			productID=queryResults2.getString(1);
			quantity=Integer.parseInt(queryResults2.getString(2));
			newOrders=GetUnitsOnOrder(productID)-quantity;
			newStock=GetUnitsInStock(productID)+quantity;

			// The default database for this assignment has errors in it, where more items are (by default) 
			// Programmed with more items on order in order_details than in UnitsOnOrder in the products table
			// This is a fix for the bug, but northwind.sql should be corrected
			if(newOrders<0) newOrders=0;
			if(newStock<0) newStock=0;

			// Update UnitsOnOrder and UnitsInStock accordingly
			statement.executeUpdate("UPDATE products SET UnitsOnOrder="+newOrders+" WHERE productID="+productID+";");
			statement.executeUpdate("UPDATE products SET UnitsInStock="+newStock+" WHERE productID="+productID+";");
			toStep++;
		}

		statement.executeUpdate("DELETE l FROM order_details l WHERE OrderID="+orderID+";");
		statement.executeUpdate("DELETE l FROM orders l WHERE OrderID="+orderID+";");

		System.out.println("\nRemoving Order Complete\n");

	}

//*************************************************************************************/

	public void ShipOrder(String orderID, String shipDate) throws SQLException{

		statement.executeUpdate("UPDATE orders SET ShippedDate ='"+shipDate+"' WHERE orderID="+orderID);

		System.out.println("\nOrder Shipped\n");

	}

//*************************************************************************************/

	public void PrintOrders() throws SQLException{
		queryResults=statement.executeQuery("SELECT OrderID, CustomerID, OrderDate," +
				"ShipName,ShipAddress,ShipCity,ShipRegion,ShipPostalCode,ShipCountry FROM orders NATURAL JOIN customers where ShippedDate IS NULL ORDER BY OrderDate;");
		while(queryResults.next()){
			System.out.print(queryResults.getString(1)+": ");
			System.out.print(queryResults.getString(2)+", ");
			System.out.print(queryResults.getString(3)+", ");
			System.out.print(queryResults.getString(4)+", ");
			System.out.print(queryResults.getString(5)+", ");
			System.out.print(queryResults.getString(6)+", ");
			System.out.print(queryResults.getString(7)+", ");
			System.out.print(queryResults.getString(8)+", ");
			System.out.print(queryResults.getString(9)+"\n");
		}
		System.out.println("\nOrders Printed\n");

	}

//*************************************************************************************/

	public void Restock(String productID, int quantity) throws SQLException{

		int newQuantity=GetUnitsInStock(productID)+quantity;
		if(newQuantity<0){
			System.out.println("Cannot have negative UnitsInStock. Rejecting update.");
		}
		else{
			statement.executeUpdate("UPDATE products SET UnitsInStock="+newQuantity+" Where productID="+productID+";");
			System.out.println("\nProduct Restocked. Current stock is "+newQuantity);
		}
		return;
	}

//*************************************************************************************/

	public void Closer(){
		try {
			con.close();
		} catch (SQLException e) {
			System.out.println("Failed to close SQL Connection");
			e.printStackTrace();
		}
	}

//*************************************************************************************/

}

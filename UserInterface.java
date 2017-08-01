import java.sql.*;
import java.text.*;
import java.util.*;

public class UserInterface {

//*************************************************************************************/

	public static void main(String[] args) {
		Communication com=null;
		Scanner scan=new Scanner(System.in);
		try {
			com = new Communication(scan);
		} catch (SQLException e) {
			System.out.println("Error creating Object from Communication class");
			e.printStackTrace();
		}
		String userInput="";

		System.out.print("Welcome to the Northwinds database. ");

		// Continue until user types exit on main menu
		while(!userInput.equalsIgnoreCase("9")){
			System.out.println("Please type an option number\n\nOptions are:\n" +
					"1.) add customer\n2.) add order\n3.) remove order\n4.) ship order\n5.) print pending orders\n6.) restock\n7.) list products in stock\n8.) list products out of stock\n9.) exit\n");
			System.out.print("My Option: ");
			userInput=scan.nextLine();

			switch(userInput){
			// Here we handle the Add customer command
				case "1":
					System.out.println("You typed Add Customer\n");
					RunCustomer(scan,com);
					break;
			// Handle Add Order command
				case "2"://"add order":
					System.out.println("You typed Add Order\n");
					RunAddOrder(scan,com);
					break;
			// Remove Order
				case "3":
					System.out.println("You typed Remove Order\n");
					RunRemoveOrder(scan,com);
					break;
			// Ship Order
				case "4":
					RunShipOrder(scan,com);
					break;
			// Print Pending orders
				case "5":
					RunPrint(scan,com);
					break;
			// Restock
				case "6":
					RunRestock(scan,com);
					break;
				case "7":
					RunListProductsInStock(com);
					break;
				case "8":
					RunListProductsOutOfStock(com);
					break;
			}
		}
		scan.close();
		com.Closer();
		return;
	}

//*************************************************************************************/

	private static void RunListProductsOutOfStock(Communication com){
		System.out.println("The following products are out of stock\n");
		try {
			com.ListProductsOutOfStock();
		} catch (SQLException e) {
			System.out.println("Error printing items");
			e.printStackTrace();
		}
		System.out.println();
		return;
	}

//*************************************************************************************/

	private static void RunListProductsInStock(Communication com){
		System.out.println("Printing List of products in stock\n");
		try {
			com.ListProductsInStock();
		} catch (SQLException e) {
			System.out.println("Error printing items");
			e.printStackTrace();
		}
		System.out.println();
		return;
	}

//*************************************************************************************/

	private static void RunRestock(Scanner scan, Communication com){

		String productName="default";
		String productID=null;
		boolean isProduct=false;
		boolean isRestockable=false;
		System.out.print("Enter Product Name: ");
		while( (!isProduct||!isRestockable)&&!productName.equalsIgnoreCase("")){
			productName=scan.nextLine();
			try {
				productID=com.FindProduct(productName);
				System.out.println("Product ID is "+productID);
				isRestockable=com.isRestockable(productID);
				if(!isRestockable) System.out.print ("Product is discontinued. Cannot restock. " +
						"Please enter Product Name (leave blank to cancel): ");
				isProduct=true;
			} catch (SQLException e) {
				if(!productName.equalsIgnoreCase(""))
					System.out.print("Failed to find product. Please enter Product Name (leave blank to cancel): ");
			}
		}
		if(isProduct&&isRestockable&&!productName.equalsIgnoreCase("")){
			System.out.print("Enter quantity recieved: ");
			int quantity=0;
			try{
				quantity=Integer.parseInt(scan.nextLine());
			}
			catch(NumberFormatException e){
				System.out.println("Integers only for quantity please. Defaulting to 0");
				quantity=0;
			}
			try {
				if(quantity!=0) com.Restock(productID,quantity);
			} catch (SQLException e) {
				System.out.print("Something has gone horribly awry when restocking inventory");
				e.printStackTrace();
			}
		}
		System.out.println();
		return;
	}

//*************************************************************************************/

	private static void RunPrint(Scanner scan, Communication com){

		System.out.println("\nPrinting Order IDs not yet shipped\n");
		try {
			com.PrintOrders();
		} catch (SQLException e) {
			System.out.println("Error Printing Order IDs");
			e.printStackTrace();
		}
		return;
	}

//*************************************************************************************/

	private static void RunShipOrder(Scanner scan, Communication com){

		String orderID="default";
		boolean isOrder=false;
		boolean isShippable=false;
		String shipDate="";
		while( (!isOrder||!isShippable)&&!orderID.equalsIgnoreCase("") ){
			System.out.print("Enter Order ID to ship (blank to cancel): ");
			orderID=scan.nextLine();
			if(!orderID.equalsIgnoreCase("")){
				try {
					Integer.parseInt(orderID);
					isOrder=com.isOrder(orderID);
					if(!isOrder) System.out.println("Order ID not found.");
					else isShippable=com.isShippable(orderID);
					if(!isShippable&&isOrder) System.out.println("Order has already been shipped");
					if(isOrder&&isShippable) {
						System.out.print("Enter Shipped date and time (Format=YYYY-MM-DD HH:MM:SS) (blank for current system time): ");
						shipDate=scan.nextLine();
						// Got this example for getting the date and time from stack overflow at
						// https://stackoverflow.com/questions/5175728/how-to-get-the-current-date-time-in-java
						if(shipDate.equalsIgnoreCase("")) shipDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
						com.ShipOrder(orderID,shipDate);
						return;
					}
				} catch (SQLException e) {

					System.out.println("Something has gone horribly awry while shipping order");
					e.printStackTrace();
				}
				catch (NumberFormatException e1){
					System.out.println("Not a valid order number.");
				}
			}
		}

		return;
	}

//*************************************************************************************/

	private static void RunRemoveOrder(Scanner scan, Communication com){
		String orderID="default";
		boolean isOrder=false;

		while(!isOrder&&!orderID.equalsIgnoreCase("")){
			System.out.print("Enter Order ID: ");
			orderID=scan.nextLine();
			if(orderID.equalsIgnoreCase("")) return;
			try {
				isOrder=com.isOrder(orderID);
				if(!isOrder){
					System.out.println("Order not found. Please choose an existing Order ID (leave blank to cancel)");
				}
				//
				else com.RemoveOrder(orderID);
			} catch (SQLException e) {
					System.out.println("Order not found. Please choose an existing Order ID (leave blank to cancel)");
					e.printStackTrace();
			}
		}


		return;
	}

//*************************************************************************************/

	private static void RunAddOrder(Scanner scan, Communication com){

		// For orders table Orders (OrderID, CustomerID, EmployeeID, OrderDate, RequiredDate, ShippedDate, ShipVia,
		//							Freight, ShipName, ShipAddress, ShipCity, ShipRegion, ShipPostalCode, ShipCountry);
		String CustomerID=null;
		String EmployeeID=null;
		String OrderDate=null;
		String RequiredDate=null;
		String ShippedDate=null;
		String ShipVia=null;
		String Freight=null;
		String ShipName=null;
		String ShipAddress=null;
		String ShipCity=null;
		String ShipRegion=null;
		String ShipPostalCode=null;
		String ShipCountry=null;
		int numberOfProducts=0;

		// For order_details table Order_details (ID, OrderID, ProductID, UnitPrice, Quantity, Discount);
		String[] ProductName=new String[200];
		String[] ProductID=new String[200];
		int[] quantity=new int[200];
		float[] discount=new float[200];

		// We're going to have to make sure the customer actually exists
		System.out.print("Enter customer ID: ");
		boolean isCustomer=false;
		while(!isCustomer){
			CustomerID=scan.nextLine();
			try {
				isCustomer=com.isCustomer(CustomerID);
				if(!isCustomer){
					System.out.print("Customer ID not found. Please enter Customer ID: ");
				}
			} catch (SQLException e) {
				System.out.print("Customer ID not found. Please enter Customer ID: ");
			}
		}

		// We're going to have to make sure the employee actually exists
		System.out.print("\nEnter employee ID: ");
		boolean isEmployee=false;
		while(!isEmployee){
			EmployeeID=scan.nextLine();
			try {
				isEmployee=com.isEmployee(EmployeeID);
				if(!isEmployee){
					System.out.print("Employee ID not found. Please enter Employee ID: ");
				}
			} catch (SQLException e) {
				System.out.print("Employee ID not found. Please enter Employee ID: ");
			}
		}

		System.out.print("\nEnter order date(Format=YYYY-MM-DD HH:MM:SS) (blank for current system time): ");
		OrderDate=scan.nextLine();
		if(OrderDate.equalsIgnoreCase("")){
			// Got this example for getting the date and time from stack overflow at
			// https://stackoverflow.com/questions/5175728/how-to-get-the-current-date-time-in-java
			OrderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		}

		System.out.print("\nEnter required delivery date(Format=YYYY-MM-DD HH:MM:SS) (blank for NULL): ");
		RequiredDate=scan.nextLine();
		System.out.print("\nEnter shipped date(Format=YYYY-MM-DD HH:MM:SS) (blank for NULL): ");
		ShippedDate=scan.nextLine();

		// Check to make sure the Shipper ID actually exists
		System.out.print("\nEnter Shipper ID: ");
		boolean isShipper=false;
		while(!isShipper){
			ShipVia=scan.nextLine();
			try {
				isShipper=com.isShipper(ShipVia);
				if(!isShipper){
					System.out.print("Shipper ID not found. Please enter Shipper ID: ");
				}
			} catch (SQLException e) {
				System.out.print("Shipper ID not found. Please enter Shipper ID: ");
			}
		}

		System.out.print("\nEnter freight cost: ");
		Freight=scan.nextLine();
		System.out.print("\nEnter Ship Name: ");
		ShipName=scan.nextLine();
		System.out.print("\nEnter Ship Address: ");
		ShipAddress=scan.nextLine();
		System.out.print("\nEnter Ship City: ");
		ShipCity=scan.nextLine();
		System.out.print("\nEnter Ship Region: ");
		ShipRegion=scan.nextLine();
		System.out.print("\nEnter Ship Postal Code: ");
		ShipPostalCode=scan.nextLine();
		System.out.print("\nEnter Ship Country: ");
		ShipCountry=scan.nextLine();
		int i=0;
		boolean isAnotherProduct=true;
		while(isAnotherProduct){
			// We're going to need the Product ID for order_details, but on the front end we should probably
			// Ask for the name of the Product and deal with finding the Product ID ourselves
			System.out.print("\nEnter Product Name(leave blank for no more products): ");
			boolean isProduct=false;

			while(!isProduct){
				ProductName[i]=scan.nextLine();
				if(ProductName[i].equalsIgnoreCase("")) break;
				try {
					ProductID[i]=com.FindProduct(ProductName[i]);
					System.out.println("Product ID is "+ProductID[i]);
					isProduct=true;
				} catch (SQLException e) {
					System.out.print("\nProduct Not Found. Enter Product Name (not ID): ");
				}

			}
			if(ProductName[i].equalsIgnoreCase("")) break;
			numberOfProducts=i;
			System.out.print("\nEnter Quantity: ");
			try{
				quantity[i]=Integer.parseInt(scan.nextLine());
			}
			catch(NumberFormatException e){
				System.out.println("\nIntegers only for quantity. defaulting to 1");
			}
			System.out.print("\nEnter discount (blank for no discount): ");
			try{
				discount[i]=Float.parseFloat(scan.nextLine());
				if(discount[i]>0.00){
					System.out.println("According to the assignment specifications, " +
							"\"The order should be rejected if a product in the order is discounted.\" Sorry");
					return;
				}
			}
			catch(NumberFormatException e){
				System.out.println("\nDefaulting to 0.00 discount");
			}
			i++;
		}

			try {
				com.AddOrder(CustomerID, EmployeeID, OrderDate, RequiredDate, ShippedDate, ShipVia, Freight, ShipName, ShipAddress, ShipCity, ShipRegion, ShipPostalCode, ShipCountry, quantity, discount, ProductID, numberOfProducts);
			} catch (SQLException e) {
				System.out.println("\nSomething went horribly awry when adding a new order");
				e.printStackTrace();
			}

		return;
	}

//*************************************************************************************/

	private static void RunCustomer(Scanner scan, Communication com){
		String CustomerID="";
		String CompanyName="";
		String ContactName="";
		String ContactTitle="";
		String Address="";
		String City="";
		String Region="";
		String PostalCode="";
		String Country="";
		String Phone="";
		String Fax="";

		// Collect all the information we need
		boolean isCustomer=false;
		// Make sure customer ID doesn't already exist, and prompt again if it does
		while(!isCustomer||(CustomerID.length()>5)||CustomerID.equalsIgnoreCase("")){
			System.out.print("Enter customer ID : ");
			CustomerID=scan.nextLine();
			if(CustomerID.length()>5) System.out.println("Customer ID cannot be larger than 5 characters.");
			else {
				try {
					if(com.isCustomer(CustomerID)) System.out.println("Customer already exists. Please Try again");
					else isCustomer=true;
				} catch (SQLException e) {
					System.out.println("Error looking up customer ID");
					e.printStackTrace();
				}
			}
		}
		while(CompanyName.equalsIgnoreCase("")){
			System.out.print("\nEnter company name : ");
			CompanyName=scan.nextLine();
		}
		while(ContactName.equalsIgnoreCase("")){
			System.out.print("\nEnter contact name : ");
			ContactName=scan.nextLine();
		}
		while(ContactTitle.equalsIgnoreCase("")){
			System.out.print("\nEnter contact title : ");
			ContactTitle=scan.nextLine();
		}
		while(Address.equalsIgnoreCase("")){
			System.out.print("\nEnter address : ");
			Address=scan.nextLine();
		}
		while(City.equalsIgnoreCase("")){
			System.out.print("\nEnter city : ");
			City=scan.nextLine();
		}
		while(Region.equalsIgnoreCase("")){
			System.out.print("\nEnter region : ");
			Region=scan.nextLine();
		}
		while(PostalCode.equalsIgnoreCase("")){
			System.out.print("\nEnter postal code : ");
			PostalCode=scan.nextLine();
		}
		while(Country.equalsIgnoreCase("")){
			System.out.print("\nEnter country : ");
			Country=scan.nextLine();
		}
		while(Phone.equalsIgnoreCase("")){
			System.out.print("\nEnter phone number : ");
			Phone=scan.nextLine();
		}
		while(Fax.equalsIgnoreCase("")){
			System.out.print("\nEnter fax number : ");
			Fax=scan.nextLine();
		}

		// Add customer to database
		com.AddCustomer(CustomerID, CompanyName, ContactName, ContactTitle, Address, City, Region,
				PostalCode, Country, Phone, Fax);
		return;
	}

//*************************************************************************************/

}

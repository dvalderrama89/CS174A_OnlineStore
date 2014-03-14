import java.sql.*;
import java.util.*;

class CustomerInterface{
	public static Connection conn;
	public static Scanner keyboard = new Scanner(System.in);
	public static String customer;
	public static int cartID;
	
	public static void main(String[] args) throws SQLException{
		// 1. Load the Oracle JDBC driver for this program
		try
		{
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		}
		catch ( Exception e)
		{
			e.printStackTrace();
		}
		///////////////////////////////////////////////////

		
		// 2. Test functions for each query
		connectToDB();
		Boolean loggedIn = false;
		do{
			loggedIn = customerLogin();
		}while(!loggedIn);
		while(true){
			customerMenu();
		}
	}

	public static void connectToDB() throws SQLException
	{
		System.out.println("Connecting to database...\n");
		// Connect to the database
		String strConn = "jdbc:oracle:thin:@uml.cs.ucsb.edu:1521:xe";
		String strUsername = "dvalderrama";
		String strPassword = "3724481";
		conn = DriverManager.getConnection(strConn,strUsername,strPassword);
	}
	
	public static Boolean customerLogin() throws SQLException
	{
		Boolean success = false;
		System.out.println("Enter username:");
		String user = keyboard.nextLine();
		System.out.println("Password:");
		String pass = keyboard.nextLine();
		
		String login = String.format("customerid = '%s' AND password = '%s'", user, pass);
		
		//Check user/pass against the Customer table
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT CUSTOMERS.customerid, CUSTOMERS.password FROM Customers WHERE " + login);
		
		while(rs.next())
		{
			if(user.equals(rs.getString("CUSTOMERID")) && pass.equals(rs.getString("PASSWORD")))
			{
				customer = user;
				success = true;				
			}
			else
			{
				System.out.println("Login faied. Incorrect credentials.");
				return false;
			}
		}
		rs.close();
		if(success == false)
		{
			System.out.println("Login failed. Incorrect credentials.");
			return success;
		}
		
		//Link CartID to customer
		PreparedStatement query= conn.prepareStatement("SELECT CART.cartid FROM Cart WHERE customerid=?");
		query.setString(1,customer);
		ResultSet rs2 = query.executeQuery();
		
		cartID = -1;	
		
		while(rs2.next())
		{
			cartID = rs2.getInt("CARTID");			
		}
		rs2.close();
		
		return success;
	}
	
	public static void customerMenu() throws SQLException
	{
		System.out.println("Search for item: 1\nDelete from cart: 2"+
							"\nDisplay cart: 3\nCheck out: 4\nFind order: 5\nRerun order: 6\nExit: 7");
		System.out.println("Enter selection:");
		String selection = keyboard.nextLine();	
		switch(selection){
			case "1": System.out.println("Search"); productSearch(); break;
			case "2": System.out.println("Delete"); deleteFromCart(); break;
			case "3": System.out.println("Display"); displayCart(); break;
			case "4": System.out.println("Checkout"); checkout(); break;
			case "5": System.out.println("Find"); findOrder(); break;
			case "6": System.out.println("Rerun"); rerunOrder(); break;
			case "7": System.out.println("Exiting"); conn.close(); System.exit(0); break;
			default: System.out.println("Invalid selection.\n"); break;
		}
	}
	
	public static void productSearch() throws SQLException
	{
		String search = "";
		String t;
		Boolean previous = false;
		System.out.println("Enter nothing to skip a field.");
		System.out.println("Enter stock number:");
		t = keyboard.nextLine();
		if(!t.isEmpty())
		{
			search += "c.stocknum = " + t + " ";
			previous = true;
		}
		else
			previous = false;
		System.out.println("Enter manufacturer name:");
		t = keyboard.nextLine();
		if(!t.isEmpty())
		{
			if(previous)
				search += String.format("AND c.manufacturer = '%s' ", t);
			else
				search += String.format("c.manufacturer = '%s' ", t);
			previous = true;
		}
		System.out.println("Enter model number:");
		t = keyboard.nextLine();
		if(!t.isEmpty())
		{
			if(previous)
				search += "AND c.modelnum = " + t + " ";
			else
				search += "c.modelnum = " + t + " ";
			previous = true;
		}
		System.out.println("Enter category:");
		t = keyboard.nextLine();
		if(!t.isEmpty())
		{
			if(previous)
				search += String.format("AND c.category = '%s' ", t);
			else
				search += String.format("c.category = '%s' ", t);
			previous = true;
		}
		System.out.println("Enter description attribute:");
		t = keyboard.nextLine();
		if(!t.isEmpty())
		{
			if(previous)
				search += String.format("AND d.attribute = '%s' ", t);
			else
				search += String.format("d.attribute = '%s' ", t);
			previous = true;
		}
		System.out.println("Enter description value:");
		t = keyboard.nextLine();
		if(!t.isEmpty())
		{
			if(previous)
				search += String.format("AND d.value = '%s' ", t);
			else
				search += String.format("d.value = '%s' ", t);
			previous = true;
		}
		System.out.println("Enter accessory of stocknum:");
		t = keyboard.nextLine();
		if(!t.isEmpty())
		{
			if(previous)
				search += String.format("AND a.accof_stocknum = '%s' ", t);
			else
				search += String.format("a.accof_stocknum = '%s' ", t);
			previous = true;
		}
		System.out.println("Enter price:");
		t = keyboard.nextLine();
		if(!t.isEmpty())
		{
			if(previous)
				search += "AND c.price = " + t + " ";
			else
				search += "c.price = " + t + " ";
		}

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM CatalogItems c JOIN AccessoryOf a ON c.stocknum = a.acc_stocknum JOIN Descriptions d ON c.stocknum=d.stocknum WHERE " + search);

		System.out.format("%10s%20s%20s%10s%30s%15s%10s%10s%20s\n", "STOCKNUM", "CATEGORY",
				"MANUFACTURER", "MODELNUM", "DESCR ATTR", "DESCR VAL", "ACC OF", "WARRANTY", "PRICE");
		Boolean productNotFound = true;
		while(rs.next())
		{
			System.out.format("%10d%20s%20s%10d%30s%15s%10s%10d%20.2f\n\n", rs.getInt("STOCKNUM"), 
					rs.getString("CATEGORY"), rs.getString("MANUFACTURER"), rs.getInt("MODELNUM"),
					rs.getString("ATTRIBUTE"), rs.getString("VALUE"), rs.getString("ACCOF_STOCKNUM"), 
					rs.getInt("WARRANTY"), rs.getFloat("PRICE"));
			productNotFound = false;
		}
		rs.close();
		if(productNotFound)
		{
			System.out.println("Product not found!\nReturning to main menu...\n");
			return;
		}
		System.out.println("Enter nothing to return to the main menu or any key to start adding to cart:");
		t = keyboard.nextLine();
		if(!t.isEmpty())
		{
			addToCart();
		}
	}
	public static void addToCart() throws SQLException
	{
		Statement stmt = conn.createStatement();

		String stocknum = "";
		String quantity = "0";
		float price = 0;

		//CartContents = STOCKNUM, CARTID, QUANTITY, PRICE
		do{
			System.out.println("Enter a stock num to add to cart or nothing to return to main:");
			stocknum = keyboard.nextLine();
			if(stocknum.isEmpty())
				break;
			//Check if the stocknum is already in the cart and throw an error if it is
			boolean inCart = false;

			PreparedStatement alreadyInCartQuery = conn.prepareStatement("SELECT stocknum FROM CartContents WHERE stocknum = ?");
			alreadyInCartQuery.setString(1, stocknum);
			ResultSet inCartRS = alreadyInCartQuery.executeQuery();

			while(inCartRS.next())
			{
				if(stocknum.equals(inCartRS.getString("STOCKNUM")))
					inCart = true;
			}
			inCartRS.close();
			if(inCart)
			{
				System.out.println("That item is arleady in your cart! Delete it first then try adding "
						+ "it again if you want to revise your order.");
				System.out.println("Returning to main menu...\n");
				break;
			}
			
			String lookupPrice = String.format("SELECT CatalogItems.price FROM CatalogItems WHERE stocknum = '%s'", stocknum);
			Statement stmt2 = conn.createStatement();
			ResultSet rs3 = stmt2.executeQuery(lookupPrice);
			while(rs3.next())
				price = rs3.getFloat("PRICE");
			rs3.close();

			//Need to check entered quantity against the current stock in the warehouse and
			//return an error if there isn't enough stock 
			boolean validQuantity = false;
			do
			{
				int inputQuantity;
				do
				{
					System.out.println("Enter a quantity:");
					quantity = keyboard.nextLine();
					inputQuantity = Integer.parseInt(quantity);
				}while(inputQuantity < 1);
				PreparedStatement inventoryQuery = conn.prepareStatement("SELECT quantity FROM Inventory WHERE stock_num = ?");
				inventoryQuery.setString(1, stocknum);
				ResultSet inventoryRS = inventoryQuery.executeQuery();
				int warehouseQuantity=0;
				while(inventoryRS.next())
				{
					warehouseQuantity = inventoryRS.getInt("QUANTITY");
				}
				inventoryRS.close();
				if(inputQuantity > warehouseQuantity)
				{
					System.out.println("Error, not enough stock in the warehouse, please enter another value.");

				}
				else
				{
					validQuantity = true;
				}
			}while(!validQuantity);
			
			
			String snInsert = String.format("INSERT INTO CartContents VALUES('%s','%d', '%s', '%.2f')",stocknum,
					cartID, quantity, price);
			stmt.executeQuery(snInsert);
			System.out.println("Added to cart!\n");
		}while(!stocknum.isEmpty());		
	}
	public static void displayCart() throws SQLException
	{		
		System.out.println("Current Items in cart:");
		
		PreparedStatement query= conn.prepareStatement("SELECT * FROM CartContents WHERE cartid = ?");
		query.setInt(1,cartID);
		
		ResultSet rs = query.executeQuery();
		//If there's nothing in the cart
		Boolean emptyCart = true;
		System.out.format("%10s%10s%10s%10s\n", "STOCKNUM", "CARTID",
				"QUANTITY", "PRICE");
		while(rs.next())
		{
			System.out.format("%10d%10d%10d%10.2f\n\n", rs.getInt("STOCKNUM"), 
					rs.getInt("CARTID"), rs.getInt("QUANTITY"), rs.getFloat("PRICE"));
			emptyCart = false;
		}
		if(emptyCart)
			System.out.println("No items in cart!\n");
	}
	public static void deleteFromCart() throws SQLException
	{
		//PROBLEM: accepts a product stocknum that doesn't exist and doesn't return an error when the query executes
		
		System.out.println("Enter product to delete from cart:");
		String input = keyboard.nextLine();
		PreparedStatement query= conn.prepareStatement("DELETE FROM CartContents WHERE stocknum = ? AND cartid = ?");
		query.setString(1, input);
		query.setInt(2, cartID);
		query.executeQuery();
		System.out.println("Delete successful!\n");
	}
	public static void checkout() throws SQLException
	{
		float total=0, subTotal=0, shippingCost=0, discount=0, discountAmount=0;
		//Displays cart contents before calculating total
		System.out.println("Current Items in cart:");
		
		PreparedStatement query= conn.prepareStatement("SELECT * FROM CartContents WHERE cartid = ?");
		query.setInt(1,cartID);
		
		ResultSet rs = query.executeQuery();
		//If there's nothing in the cart
		Boolean emptyCart = true;
		System.out.format("%10s%10s%10s%10s\n", "STOCKNUM", "CARTID",
				"QUANTITY", "PRICE");
		
		//Setup the product sales update statement here but don't execute it until the customer checks out
		List<Integer> productQuantity = new ArrayList<Integer>();
		List<Integer> productStocknum = new ArrayList<Integer>();
		List<Float> productTotal = new ArrayList<Float>();
		List<String> categoryName = new ArrayList<String>();
		
		
		while(rs.next())
		{
			subTotal += rs.getInt("QUANTITY")*rs.getFloat("PRICE");
			
			//Should now work with a list of items for updating product sales			
			productQuantity.add(rs.getInt("QUANTITY"));
			productTotal.add(subTotal);
			productStocknum.add(rs.getInt("STOCKNUM"));
			
			//Get category from CatalogItems table for each item
			PreparedStatement categoryQuery = conn.prepareStatement("SELECT category FROM CatalogItems WHERE stocknum = ?");
			categoryQuery.setInt(1, rs.getInt("STOCKNUM"));
			ResultSet categoryRS = categoryQuery.executeQuery();
			while(categoryRS.next())
			{
				//Add the category of the item to the list that will later be passed to updateCategorySales
				categoryName.add(categoryRS.getString("CATEGORY"));
			}
			categoryRS.close();
			
			System.out.format("%10d%10d%10d%10.2f\n\n", rs.getInt("STOCKNUM"), 
					rs.getInt("CARTID"), rs.getInt("QUANTITY"), rs.getFloat("PRICE"));
			emptyCart = false;
		}
		rs.close();
		
		//Grab customer status to apply discount
		String customerStatus = "";
		PreparedStatement statusQuery = conn.prepareStatement("SELECT status FROM Customers WHERE customerid = ?");
		statusQuery.setString(1, customer);

		ResultSet statusResult = statusQuery.executeQuery();
		while(statusResult.next())
		{
			customerStatus = statusResult.getString("STATUS");
		}
		statusResult.close();
		switch(customerStatus)
		{
			case "Gold": discount = 0.10f; break;
			case "Silver":discount = 0.05f; break;
			case "Green": discount = 0.0f; break;
			case "New": discount = 0.10f; break;
			default: System.out.println("Invalid customer status."); break;
		}
		
		discountAmount = (subTotal*discount)*-1.0f;
		
		if(emptyCart){
			System.out.println("You have no items to checkout! Returning to main menu.\n");
			return;
		}
		if(subTotal > 100.00)
			shippingCost = 0;
		else
			shippingCost = subTotal * 0.10f;
		total = subTotal + shippingCost + discountAmount;
		System.out.format("%10s%10s%10s%10s\n","SUBTOTAL", "SHIPPING", "DISCOUNT", "TOTAL");
		System.out.format("%10.2f%10.2f%10.2f%10.2f\n\n", subTotal, shippingCost, discountAmount, total);
		
		//Transition to complete checkout
		System.out.println("Checkout: 1\nBack to menu: 2");
		String input = keyboard.nextLine();
		if(input.equals("1"))
		{
				//Update sales
				updateProductSales(productStocknum, productQuantity, productTotal);
				updateCategorySales(categoryName, productQuantity, productTotal);
				
				addToOrderHistory(total);
				//Delete current cart contents after added to order history
				deleteCurrentCartContents();
				//Calculate new status
				//Use this query: SELECT * FROM OrderHistory WHERE customerid = 'Rhagrid' ORDER BY ordernum DESC
				updateCustomerStatus();
				
				System.out.println("Checkout complete!\n");
		}
		else
			return;
	}
	
	public static void updateCategorySales(List<String> category, List<Integer> quantity, List<Float> total) throws SQLException
	{
		//Assumes all lists are of equal length
		for(int i = 0; i < category.size();i++)
		{
			//Retrieve the old values for quantity and total before updating
			PreparedStatement categorySalesQuery = conn.prepareStatement("SELECT * FROM CategorySales WHERE category = ?");
			categorySalesQuery.setString(1, category.get(i));
			ResultSet categoryRS = categorySalesQuery.executeQuery();
			int oldQuantity = 0;
			float oldTotal = 0;
			while(categoryRS.next())
			{
				oldQuantity = categoryRS.getInt("QUANTITY");
				oldTotal = categoryRS.getFloat("TOTAL");
			}
			categoryRS.close();
			
			//calculate new quantity and total
			quantity.set(i, quantity.get(i) + oldQuantity);
			total.set(i, total.get(i) + oldTotal);
			
			//Now update table
			PreparedStatement updateCategorySales = conn.prepareStatement("UPDATE CategorySales SET quantity = ?, total = ? WHERE category = ?");
			updateCategorySales.setInt(1, quantity.get(i));
			updateCategorySales.setFloat(2, total.get(i));
			updateCategorySales.setString(3, category.get(i));
			updateCategorySales.executeQuery();
		}
		
	}
	
	public static void updateProductSales(List<Integer> stocknum, List<Integer> quantity, List<Float> total) throws SQLException
	{
		
		//Assumes all lists are of equal length
		for(int i=0; i < stocknum.size(); i++)
		{
			//Need to grab old values of quantity and total before updating
			PreparedStatement productSalesQuery = conn.prepareStatement("SELECT * FROM ProductSales WHERE stocknum = ?");
			productSalesQuery.setInt(1, stocknum.get(i));
			ResultSet productSalesRS = productSalesQuery.executeQuery();
			int oldQuantity=0;
			float oldTotal=0;
			while(productSalesRS.next())
			{
				oldQuantity = productSalesRS.getInt("QUANTITY");
				oldTotal = productSalesRS.getFloat("TOTAL");
			}
			productSalesRS.close();

			//Calculate new quantity and total
			quantity.set(i,quantity.get(i) + oldQuantity);
			total.set(i, total.get(i) + oldTotal);

			//Now that we have the old values we can update the table with the old+passed in values
			PreparedStatement updateProductSales = conn.prepareStatement("UPDATE ProductSales SET quantity = ?, total = ? WHERE stocknum = ?");
			updateProductSales.setInt(1, quantity.get(i));
			updateProductSales.setFloat(2, total.get(i));
			updateProductSales.setInt(3, stocknum.get(i));
			updateProductSales.executeQuery();
		}
	}
	
	public static void updateCustomerStatus() throws SQLException
	{
		//This function messed up during testing (updated one checkout too slowly): more testing needed.
		//SELECT * FROM OrderHistory WHERE customerid = 'Rhagrid' ORDER BY ordernum DESC
		PreparedStatement orderQuery = conn.prepareStatement("SELECT ordertotal FROM OrderHistory WHERE customerid = ? ORDER BY ordernum DESC");
		orderQuery.setString(1, customer);
		ResultSet orderTotalRS = orderQuery.executeQuery();
		int maxOrders = 0;
		float total = 0;
		
		//Should not return empty, but could return less than 3 orders
		while(orderTotalRS.next() && maxOrders < 3)
		{
			total += orderTotalRS.getFloat("ORDERTOTAL");
			maxOrders++;
		}
		orderTotalRS.close();
		
		//Now assign a status given the value of total
		String status = "New";
		if(total > 0 && total <= 100)
			status = "Green";
		else if(total > 100 && total <= 500)
			status = "Silver";
		else if(total > 500)
			status = "Gold";
		else{
			//Note: this should never happen
			status = "New";
		}
		
		//Now set the new status using an update
		PreparedStatement updateCustomerStatus = conn.prepareStatement("UPDATE Customers SET status = ? WHERE customerid = ?");
		updateCustomerStatus.setString(1, status);
		updateCustomerStatus.setString(2, customer);
		updateCustomerStatus.executeQuery();
		
		System.out.println("Customer Status: " + status);
	}
	
	public static void deleteCurrentCartContents() throws SQLException
	{
		PreparedStatement query= conn.prepareStatement("DELETE FROM CartContents WHERE cartid = ?");
		query.setInt(1, cartID);
		query.executeQuery();
	}
	
	public static void addToOrderHistory(float total) throws SQLException
	{
		//Generate ordernum
		float orderTotal = total;
		int orderNum, lastOrderNum=0;
		PreparedStatement lastOrderQuery = conn.prepareStatement("SELECT MAX(ordernum) FROM OrderHistory");
		ResultSet lastOrderRS = lastOrderQuery.executeQuery();
		while(lastOrderRS.next())
		{
			lastOrderNum = lastOrderRS.getInt("MAX(ORDERNUM)");
		}
		lastOrderRS.close();
		orderNum = lastOrderNum + 1;	
		System.out.println("Your order number is: " + orderNum);
		
		//Insert ordernum and customer id into OrderHistory table
		PreparedStatement insertOrderNum = conn.prepareStatement("INSERT INTO OrderHistory VALUES(?,?,?)");
		insertOrderNum.setInt(1, orderNum);
		insertOrderNum.setString(2,customer);
		insertOrderNum.setFloat(3,orderTotal);
		insertOrderNum.executeQuery();
		
		//Grab cart contents and pair them with current order num, then insert into OrderContents table
		PreparedStatement cartContentsQuery = conn.prepareStatement("SELECT * FROM CartContents WHERE cartid = ?");
		cartContentsQuery.setInt(1, cartID);
		ResultSet cartContentsRS = cartContentsQuery.executeQuery();
		while(cartContentsRS.next())
		{
			int stocknum = cartContentsRS.getInt("STOCKNUM");
			float price = cartContentsRS.getFloat("PRICE");
			int quantity = cartContentsRS.getInt("QUANTITY");
			
			//Record cart contents into order contents
			PreparedStatement insertOrderContents = conn.prepareStatement("INSERT INTO OrderContents VALUES(?,?,?,?)");
			insertOrderContents.setInt(1,stocknum);
			insertOrderContents.setFloat(2,price);
			insertOrderContents.setInt(3,quantity);
			insertOrderContents.setInt(4,orderNum);
			insertOrderContents.executeQuery();
			
			//Insert into receivedOrders
			PreparedStatement insertReceivedOrder = conn.prepareStatement("INSERT INTO ReceivedOrders VALUES(?,?,?,?)");
			insertReceivedOrder.setInt(1,stocknum);
			insertReceivedOrder.setFloat(2,price);
			insertReceivedOrder.setInt(3, quantity);
			insertReceivedOrder.setInt(4,orderNum);
			insertReceivedOrder.executeQuery();
		}
		cartContentsRS.close();
		
	}
	
	public static void findOrder() throws SQLException
	{
		PreparedStatement findOrderQuery = conn.prepareStatement("SELECT * FROM OrderHistory WHERE customerid = ?");
		findOrderQuery.setString(1,customer);
		ResultSet findOrderRS = findOrderQuery.executeQuery();
		System.out.println("Past Orders:");
		System.out.format("%10s%15s\n", "ORDER NUMBER", "TOTAL");
		while(findOrderRS.next())
		{
			System.out.format("%10s%15s\n\n", findOrderRS.getInt("ORDERNUM"), findOrderRS.getFloat("ORDERTOTAL"));
		}
		findOrderRS.close();
	}
	
	public static void rerunOrder() throws SQLException
	{
		//PROBLEM: accepts input for an order number that doesn't exist and then adds a 0.0 entry to the order list
		
		//Generate new order number
		int currentOrderNum, lastOrderNum=0;
		PreparedStatement lastOrderQuery = conn.prepareStatement("SELECT MAX(ordernum) FROM OrderHistory");
		ResultSet lastOrderRS = lastOrderQuery.executeQuery();
		while(lastOrderRS.next())
		{
			lastOrderNum = lastOrderRS.getInt("MAX(ORDERNUM)");
		}
		lastOrderRS.close();
		currentOrderNum = lastOrderNum + 1;
		
		//Rerun old order using new order number
		System.out.println("Enter order number to rerun:");
		String ordernum = keyboard.nextLine();
		
		//Get old order from ORDERHISTORY table
		PreparedStatement oldPriceQuery = conn.prepareStatement("SELECT OrderTotal FROM OrderHistory WHERE ordernum = ?");
		oldPriceQuery.setString(1, ordernum);
		ResultSet oldPriceRS = oldPriceQuery.executeQuery();
		float oldPrice=0.f;
		while(oldPriceRS.next())
		{
			oldPrice = oldPriceRS.getFloat("ORDERTOTAL");
		}
		oldPriceRS.close();
		//Insert new order into ORDERHISTORY table first or the foreign key is violated
		PreparedStatement insertNewOrder = conn.prepareStatement("INSERT INTO OrderHistory VALUES(?,?,?)");
		insertNewOrder.setInt(1, currentOrderNum);
		insertNewOrder.setString(2,customer);
		insertNewOrder.setFloat(3,oldPrice);
		insertNewOrder.executeQuery();
		
		//Now we can rerun the order
		PreparedStatement orderQuery = conn.prepareStatement("SELECT * FROM OrderContents WHERE ordernum = ?");
		orderQuery.setString(1, ordernum);
		ResultSet orderRS = orderQuery.executeQuery();
		System.out.println("Rerunning the order...");
		while(orderRS.next())
		{
			int stocknum = orderRS.getInt("STOCKNUM");
			float price = orderRS.getFloat("PRICE");
			int quantity = orderRS.getInt("QUANTITY");
			
			//Record cart contents into order contents
			PreparedStatement insertOrderContents = conn.prepareStatement("INSERT INTO OrderContents VALUES(?,?,?,?)");
			insertOrderContents.setInt(1,stocknum);
			insertOrderContents.setFloat(2,price);
			insertOrderContents.setInt(3,quantity);
			insertOrderContents.setInt(4,currentOrderNum);
			insertOrderContents.executeQuery();
		}
		orderRS.close();
		System.out.println("Done!\n");
	}
}
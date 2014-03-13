import java.sql.*;
import java.util.*;


public class ManagerInterface {
	public static Connection conn;
	public static Scanner keyboard = new Scanner(System.in);
	public static void main(String[] args) throws SQLException {
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

		// TODO Auto-generated method stub
		connectToDB();
		do
		{
			managerMenu();
		}while(true);
	}
	public static void connectToDB() throws SQLException
	{
		// Connect to the database
		String strConn = "jdbc:oracle:thin:@uml.cs.ucsb.edu:1521:xe";
		String strUsername = "dvalderrama";
		String strPassword = "3724481";
		conn = DriverManager.getConnection(strConn,strUsername,strPassword);
	}	
	public static void managerMenu() throws SQLException
	{
		System.out.println("Check Sales: 1\nAdjust Customer Status: 2\nSend Order: 3\nChange Item Price: 4\n"
				+ "Delete Sales: 5\nExit: 6\nEnter selection:");
		String input = keyboard.nextLine();
		switch(input)
		{
			case "1": printSales(); break;
			case "2": adjustCustomerStatus(); break;
			case "3": sendOrder(); break;
			case "4": changeItemPrice(); break;
			case "5": deleteSales(); break;
			case "6": System.out.println("Exiting..."); System.exit(0); break;
			default: System.out.println("Exiting..."); System.exit(0); break;
		}
	}
	
	public static void deleteSales() throws SQLException
	{
		//First we need to grab a list of all customers who have an order history
		//Use: SELECT DISTINCT customerid FROM Orderhistory
		List<String> customerList = new ArrayList<String>();
		PreparedStatement customerQuery = conn.prepareStatement("SELECT DISTINCT customerid FROM OrderHistory");
		ResultSet customerRS = customerQuery.executeQuery();
		while(customerRS.next())
		{
			customerList.add(customerRS.getString("CUSTOMERID"));
		}
		customerRS.close();
		
		//Then we need to loop through each customer ID and only start deleting their history past the third count
		//We'll need to use this query: SELECT ordertotal FROM OrderHistory WHERE customerid = ? ORDER BY ordernum DESC
		System.out.println("Deleting orders...");
		for(int i = 0; i < customerList.size();i++)
		{
			PreparedStatement customerHistory = conn.prepareStatement("SELECT ordernum FROM OrderHistory WHERE customerid = ? ORDER BY ordernum DESC");
			customerHistory.setString(1, customerList.get(i));
			ResultSet historyRS = customerHistory.executeQuery();
			int historyCount = 0;
			while(historyRS.next())
			{
				int currentOrder = historyRS.getInt("ORDERNUM");
				//And in each while loop, have an IF condition which starts to delete from the table after the third loop
				if(historyCount > 2)
				{
					//Use: DELETE FROM OrderHistory WHERE customerid = ?
					PreparedStatement deleteCustomerHistory = conn.prepareStatement("DELETE FROM OrderHistory WHERE ordernum = ?");
					deleteCustomerHistory.setInt(1, currentOrder);
					deleteCustomerHistory.executeQuery();
				}
				historyCount++;
			}
			historyRS.close();
		}
		System.out.println("Done!");
		System.out.println("Returning to main menu...\n");
	}
	
	public static void adjustCustomerStatus() throws SQLException
	{
		//Use a customer id to overritde the customer's status
		System.out.println("Enter customer ID: ");
		String inputCustomer = keyboard.nextLine();
		System.out.println("Enter the new customer status:");
		String newStatus = keyboard.nextLine();
		
		PreparedStatement updateCustomer = conn.prepareStatement("UPDATE Customers SET status = ? WHERE customerid = ?");
		updateCustomer.setString(1, newStatus);
		updateCustomer.setString(2, inputCustomer);
		updateCustomer.executeQuery();
		System.out.println("Done!");
		System.out.println("Returning to main menu...\n");
	}
	
	public static void sendOrder() throws SQLException
	{
		//Grab the last notice ID so we can generate a new notice ID
		PreparedStatement noticeIDQuery = conn.prepareStatement("SELECT MAX(notice_id) FROM ShippingNotice");
		ResultSet noticeRS = noticeIDQuery.executeQuery();
		//default noticeID if there aren't any notices in the table
		int noticeID = 0;
		while(noticeRS.next())
		{
			noticeID = noticeRS.getInt("MAX(notice_id)");
		}
		noticeID += 1;
		
		//Now that we have the noticeID to use, go on to the next part(ask manager for input)
		//SHIPPINGNOTICE = notice_id, co_name, manufacturer, model_number, quantity
		System.out.println("Enter a company name:");
		String company = keyboard.nextLine();
		System.out.println("Enter a manufacturer:");
		String manufacturer = keyboard.nextLine();
		System.out.println("Enter a model number:");
		String modelnum = keyboard.nextLine();
		System.out.println("Enter a quantity:");
		String quantity = keyboard.nextLine();
		
		//Now send these over to the eDepot
		PreparedStatement insertShippingNotice = conn.prepareStatement("INSERT INTO ShippingNotice VALUES(?,?,?,?,?)");
		insertShippingNotice.setInt(1, noticeID);
		insertShippingNotice.setString(2, company);
		insertShippingNotice.setString(3, manufacturer);
		insertShippingNotice.setString(4, modelnum);
		insertShippingNotice.setString(5, quantity);
		insertShippingNotice.executeQuery();
		System.out.println("Order sent!");
		
		System.out.println("Returning to main menu...\n");
	}
	
	public static void printSales() throws SQLException
	{
		System.out.println("Sales by product: 1\nSales by category: 2\nHighest Spending Customer: 3\nBack to main menu: 4");
		String input = keyboard.nextLine();
		switch(input)
		{
			case "1": showProductSales(); break;
			case "2": showCategorySales(); break;
			case "3": customerSpending(); break;
			case "4": System.out.println("Returning to main menu...\n"); return;
			default: System.out.println("Returning to main menu...\n"); return;
		}
	}
	
	public static void customerSpending() throws SQLException
	{
		//User this query:
		//SELECT customerid, SUM(ordertotal) FROM OrderHistory GROUP BY customerid HAVING SUM(ordertotal) = (SELECT MAX(SUM(ordertotal)) FROM OrderHistory GROUP BY customerid);
		PreparedStatement customerQuery = conn.prepareStatement("SELECT customerid, SUM(ordertotal) FROM OrderHistory GROUP BY customerid HAVING SUM(ordertotal) = (SELECT MAX(SUM(ordertotal)) FROM OrderHistory GROUP BY customerid)");
		ResultSet customerRS = customerQuery.executeQuery();
		System.out.format("%10s%20s\n", "CUSTOMER", "SPENDING");
		while(customerRS.next())
		{
			System.out.format("%10s%20.2f\n\n", customerRS.getString("CUSTOMERID"), customerRS.getFloat("SUM(ordertotal)"));
		}
		customerRS.close();
		System.out.println("Returning to main menu...\n");
	}
	
	public static void showProductSales() throws SQLException
	{
		PreparedStatement productSalesQuery = conn.prepareStatement("SELECT * FROM ProductSales");
		ResultSet productSalesRS = productSalesQuery.executeQuery();
		System.out.format("%10s%10s%20s\n", "STOCKNUM", "QUANTITY", "TOTAL");
		while(productSalesRS.next())
		{
			System.out.format("%10d%10d%20.2f\n", productSalesRS.getInt("STOCKNUM"), productSalesRS.getInt("QUANTITY"), productSalesRS.getFloat("TOTAL"));
		}
		System.out.println("Returning to main menu...\n");
		productSalesRS.close();
	}
	
	public static void showCategorySales() throws SQLException
	{
		PreparedStatement categorySalesQuery = conn.prepareStatement("SELECT * FROM CategorySales");
		ResultSet categorySalesRS = categorySalesQuery.executeQuery();
		System.out.format("%10s%10s%20s\n", "CATEGORY", "QUANTITY", "TOTAL");
		while(categorySalesRS.next())
		{
			System.out.format("%10s%10d%20.2f\n", categorySalesRS.getString("CATEGORY"), categorySalesRS.getInt("QUANTITY"), categorySalesRS.getFloat("TOTAL"));
		}
		categorySalesRS.close();
		System.out.println("Returning to main menu...\n");
	}
	
	public static void changeItemPrice() throws SQLException
	{
		PreparedStatement updateItemPrice = conn.prepareStatement("UPDATE CatalogItems SET price = ? WHERE stocknum = ?");
		
		System.out.println("Enter the product stocknum:");
		String stocknum = keyboard.nextLine();
		System.out.println("Enter the new price for the product:");
		String price = keyboard.nextLine();
		
		updateItemPrice.setString(1,price);
		updateItemPrice.setString(2, stocknum);
		
		updateItemPrice.executeQuery();
		System.out.println("Update done!\nReturning to main menu...\n");
	}

}

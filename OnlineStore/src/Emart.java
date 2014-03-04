import java.sql.*;

class Emart{
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
		print_all();
	}

	public static void print_all() throws SQLException
	{
		// Connect to the database
		String strConn = "jdbc:oracle:thin:@uml.cs.ucsb.edu:1521:xe";
		String strUsername = "dvalderrama";
		String strPassword = "3724481";
		Connection conn = DriverManager.getConnection(strConn,strUsername,strPassword);

		// Create a Statement
		Statement stmt = conn.createStatement();

		// Specify the SQL Query to run
		ResultSet rs = stmt.executeQuery ("SELECT * FROM CS174A.Customers");

		// Iterate through the result and print the data
		System.out.println("result:");
		while(rs.next())
		{
			// Get the value from column "columnName" with integer type
			System.out.println(rs.getInt("DISCNT"));
			// Get the value from column "columnName" with float type
			//System.out.println(rs.getFloat("columnName"));
			// Get the value from the third column with string type
			System.out.println(rs.getString(1));
		}

		// don't miss this
		rs.close();
	}
}
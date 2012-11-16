package sqlconnect;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class SQLConnect implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8420374973016464332L;
	static Statement stmt;
	static ResultSet rs;
	
	//public HashMap<Double,ArrayList<Double>> result_map;
	//public HashMap<Double,HashSet<Double>> probe_map;
	
	public SQLConnect() {
		try {
			DBinit();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getCountofDisease(String disease) throws SQLException {
		rs = stmt.executeQuery("SELECT count(distinct p_id) as number from diagnosis  d where ds_id IN " +
				"( select ds_id from disease where name like '%"+disease+"%' or " +
						"description like '%"+disease+"%' or type like '%"+disease+"%')");
		rs.next();
		return rs.getInt("number");
	}
	
	
	public String[] getTypesofDrugs(String disease) throws SQLException {
		
		rs = stmt.executeQuery("select distinct type from drug d, drug_use du where  d.dr_id = du.dr_id and du.p_id IN " +
				"(select p_id from diagnosis d where d.ds_id IN " +
				"( Select ds_id from disease where name like '%"+disease+"%' or description like '%"+disease+"%'))");
		int count = 0;
		while(rs.next()) {
			count++;
		}
		String[] result = new String[count];
		count = 0;
		rs.beforeFirst();
		while(rs.next()) {
			result[count] = rs.getString("type");
			count++;
		}
		return result;
	}
	
	public String[] getExpressionValues(String disease, int cluster_id, int mu_id) throws SQLException {
		/*rs = stmt.executeQuery("select s_id, expression from microarray_fact where e_id IN" +
				"( SELECT DISTINCT e_id FROM  `microarray_fact` WHERE mu_id = "+mu_id+") AND s_id IN " +
				"( SELECT s_id FROM clinical_sample WHERE p_id IN" +
				"( SELECT p_id FROM diagnosis d WHERE d.ds_id IN " +
				"( SELECT ds_id FROM disease WHERE name LIKE '%"+disease+"%' OR description LIKE '%"+disease+"%'))) AND pb_id IN " +
				"( SELECT distinct pb_id FROM probe WHERE UID IN" +
				"( select UID from gene_fact where cl_id = "+cluster_id+" ))");
		*/
		int rs_temp;
		rs_temp = stmt.executeUpdate("drop view if exists temp_3");
		rs_temp = stmt.executeUpdate("create view temp_3 as SELECT s_id FROM clinical_sample WHERE p_id IN" +
				"( SELECT p_id FROM diagnosis d WHERE d.ds_id IN " +
				"( SELECT ds_id FROM disease WHERE name = '"+disease+"'))");
		rs_temp = stmt.executeUpdate("drop view if exists temp_4");
		rs_temp = stmt.executeUpdate("create view temp_4 as SELECT distinct pb_id FROM probe WHERE UID IN" +
				"( select UID from gene_fact where cl_id = "+cluster_id+" )");
		rs = stmt.executeQuery("select s_id, expression from microarray_fact where e_id IN " +
				"( SELECT DISTINCT e_id FROM  `microarray_fact` WHERE mu_id = "+mu_id+") AND s_id IN " +
				"( select * from temp_3) AND pb_id IN " +
				"( SELECT * from temp_4 )");
		int count = 0;
		while(rs.next()) {
			count++;
		}
		String[] result = new String[count];
		count = 0;
		rs.beforeFirst();
		while(rs.next()) {
			result[count] = rs.getString("expression");//rs.getInt("s_id") + " " + rs.getString("expression");
			count++;
		}
		return result;
	}
	
	public double[] getExpressionValuesWithDisease(String disease, int go_id) throws SQLException {
		int rs_temp;
		rs_temp = stmt.executeUpdate("drop view if exists temp_3");
		rs_temp = stmt.executeUpdate("create view temp_3 as SELECT s_id FROM clinical_sample WHERE p_id IN" +
				"( SELECT p_id FROM diagnosis d WHERE d.ds_id IN " +
				"( SELECT ds_id FROM disease WHERE name = '"+disease+"'))");
		rs_temp = stmt.executeUpdate("drop view if exists temp_4");
		rs_temp = stmt.executeUpdate("create view temp_4 as SELECT distinct pb_id FROM probe WHERE UID IN" +
				"( select UID from gene_fact where go_id = "+go_id+" )");
		rs = stmt.executeQuery("select expression from microarray_fact where s_id IN " +
				"( select * from temp_3) AND pb_id IN " +
				"( SELECT * from temp_4 )");
		int count = 0;
		
		while(rs.next()) {
			count++;
			
		}
		double[] result = new double[count];
		int i = 0;
		//rs.first();
		rs.beforeFirst();
		//double sum = 0;
		while(i < result.length) {
			rs.next();
			result[i] = Double.parseDouble(rs.getString("expression"));
			//sum+=result[i];
			i++;
		}
		//print(sum);
		return result;
	}
	
	public double[] getExpressionValuesWithoutDisease(String disease, int go_id) throws SQLException {
		int rs_temp;
		rs_temp = stmt.executeUpdate("drop view if exists temp_3");
		rs_temp = stmt.executeUpdate("create view temp_3 as SELECT s_id FROM clinical_sample WHERE p_id IN" +
				"( SELECT p_id FROM diagnosis d WHERE d.ds_id NOT IN " +
				"( SELECT ds_id FROM disease WHERE name = '"+disease+"'))");
		rs_temp = stmt.executeUpdate("drop view if exists temp_4");
		rs_temp = stmt.executeUpdate("create view temp_4 as SELECT distinct pb_id FROM probe WHERE UID IN" +
				"( select UID from gene_fact where go_id = "+go_id+" )");
		rs = stmt.executeQuery("select expression from microarray_fact where s_id IN " +
				"( select * from temp_3) AND pb_id IN " +
				"( SELECT * from temp_4 )");
		int count = 0;
		
		while(rs.next()) {
			count++;
			
		}
		double[] result = new double[count];
		int i = 0;
		//rs.first();
		rs.beforeFirst();
		//double sum = 0;
		while(i < result.length) {
			rs.next();
			result[i] = Double.parseDouble(rs.getString("expression"));
			//sum+=result[i];
			i++;
		}
		//print(sum);
		return result;
	}
	
	
	
	public Map<Double, ArrayList<Double>> getMapOfExpressionValuesWithDisease(String disease, int go_id) throws SQLException {
		
		int rs_temp;
		rs_temp = stmt.executeUpdate("drop view if exists temp_3");
		rs_temp = stmt.executeUpdate("create view temp_3 as SELECT s_id FROM clinical_sample WHERE p_id IN" +
				"( SELECT p_id FROM diagnosis d WHERE d.ds_id IN " +
				"( SELECT ds_id FROM disease WHERE name LIKE '%"+disease+"%' OR description LIKE '%"+disease+"%'))");
		
		rs_temp = stmt.executeUpdate("drop view if exists temp_4");
		rs_temp = stmt.executeUpdate("create view temp_4 as SELECT distinct pb_id FROM probe WHERE UID IN" +
				"( select UID from gene_fact where go_id = "+go_id+" )");
		rs = stmt.executeQuery("select s_id,expression from microarray_fact where s_id IN " +
				"( select * from temp_3) AND pb_id IN " +
				"( SELECT * from temp_4 )");

		Map<Double,ArrayList<Double>> result = new HashMap<Double,ArrayList<Double>>();
		double s_id,expression;
		int count = 0;
		while(rs.next()) {
			s_id = (double)rs.getInt("s_id");
			expression = Double.parseDouble(rs.getString("expression"));
			ArrayList<Double> expressionList = result.get(s_id);
			if(expressionList == null)
				result.put(s_id, expressionList = new ArrayList<Double>());
			expressionList.add(expression);
			count++;
		}
		//print(count+","+result.size());
		return result;
	}
	
	
	public  Map<Double, ArrayList<Double>> getSampleIDWithDisease(String disease) throws SQLException {
		
		int rs_temp;
		rs_temp = stmt.executeUpdate("drop view if exists temp_3");
		rs_temp = stmt.executeUpdate("create view temp_3 as SELECT s_id FROM clinical_sample WHERE p_id IN" +
				"( SELECT p_id FROM diagnosis d WHERE d.ds_id IN " +
				"( SELECT ds_id FROM disease WHERE name LIKE '%"+disease+"%' OR description LIKE '%"+disease+"%'))");
		
		rs = stmt.executeQuery("select s_id,expression from microarray_fact where s_id IN " +
				"( select * from temp_3)");
		
		Map<Double,ArrayList<Double>> result = new HashMap<Double,ArrayList<Double>>();
		double s_id,expression;
		while(rs.next()) {
			s_id = (double)rs.getInt("s_id");
			expression = Double.parseDouble(rs.getString("expression"));
			ArrayList<Double> expressionList = result.get(s_id);
			if(expressionList == null)
				result.put(s_id, expressionList = new ArrayList<Double>());
			expressionList.add(expression);
		}
		//print("rsult size,"+result.size());
		return result;
	}
	
	
	public  Map<Double, ArrayList<Double>> getSampleIDWithoutDisease(String disease) throws SQLException {
		
		int rs_temp;
		rs_temp = stmt.executeUpdate("drop view if exists temp_3");
		rs_temp = stmt.executeUpdate("create view temp_3 as SELECT s_id FROM clinical_sample WHERE p_id IN" +
				"( SELECT p_id FROM diagnosis d WHERE d.ds_id NOT IN " +
				"( SELECT ds_id FROM disease WHERE name LIKE '%"+disease+"%' OR description LIKE '%"+disease+"%'))");
		
		rs = stmt.executeQuery("select s_id,expression from microarray_fact where s_id IN " +
				"( select * from temp_3)");
		
		Map<Double,ArrayList<Double>> result = new HashMap<Double,ArrayList<Double>>();
		double s_id,expression;
		while(rs.next()) {
			s_id = (double)rs.getInt("s_id");
			expression = Double.parseDouble(rs.getString("expression"));
			ArrayList<Double> expressionList = result.get(s_id);
			if(expressionList == null)
				result.put(s_id, expressionList = new ArrayList<Double>());
			expressionList.add(expression);
		}
		//print("rsult size,"+result.size());
		return result;
	}
	
	
	public Map<Double, HashMap<Double, Double>> getMapOfGenes() throws SQLException {
		
		rs = stmt.executeQuery("select UID,s_id,expression from micro_probe");

		
		Map<Double,HashMap<Double,Double>> result = new HashMap<Double, HashMap<Double,Double>>();
		
		double UID,s_id,expression;
		while(rs.next()) {
			s_id = (double)rs.getInt("s_id");
			UID = (double) rs.getInt("UID");
			expression = Double.parseDouble(rs.getString("expression"));
			
			
			HashMap<Double, Double> sample_expr = result.get(UID);
			if(sample_expr == null)
				result.put(UID, sample_expr = new HashMap<Double,Double>());
			
			sample_expr.put(s_id,expression);
		}
		//print("result size ,"+result.size());
		return result;
	}
	
	public Map<Double, HashMap<Double, Double>> getMapOfSamples() throws SQLException {
		
		rs = stmt.executeQuery("select UID,s_id,expression from micro_probe");

		
		Map<Double,HashMap<Double,Double>> result = new HashMap<Double, HashMap<Double,Double>>();
		
		double UID,s_id,expression;
		while(rs.next()) {
			s_id = (double)rs.getInt("s_id");
			UID = (double) rs.getInt("UID");
			expression = Double.parseDouble(rs.getString("expression"));
			
			
			HashMap<Double, Double> sample_expr = result.get(s_id);
			if(sample_expr == null)
				result.put(s_id, sample_expr = new HashMap<Double,Double>());
			
			sample_expr.put(UID,expression);
		}
		//print("result size ,"+result.size());
		return result;
	}

	
	public List<TreeMap<Double, Double>> getTest_Samples() throws SQLException {

		List<TreeMap<Double,Double>> result = new ArrayList<TreeMap<Double,Double>>();
		
		double UID,expression;
		TreeMap<Double, Double> gene_expr = new TreeMap<Double,Double>();
		
		rs = stmt.executeQuery("select UID,test1 from test_samples");
		
		while(rs.next()) {
			
			UID = (double) rs.getInt("UID");
			expression = (double) rs.getInt("test1");
			gene_expr.put(UID,expression);
			
		}
		
		result.add(gene_expr);
		//print(result.get(0).size());
		gene_expr = new TreeMap<Double,Double>();
		rs = stmt.executeQuery("select UID,test2 from test_samples");
		
		while(rs.next()) {
			
			UID = (double) rs.getInt("UID");
			expression = (double) rs.getInt("test2");
			gene_expr.put(UID,expression);
			
		}
		result.add(gene_expr);
		//print(result.get(1).size());
		
		gene_expr = new TreeMap<Double,Double>();
		rs = stmt.executeQuery("select UID,test3 from test_samples");
		
		while(rs.next()) {
			
			UID = (double) rs.getInt("UID");
			expression = (double) rs.getInt("test3");
			gene_expr.put(UID,expression);
			
		}
		result.add(gene_expr);
		//print(result.get(2).size());
		gene_expr = new TreeMap<Double,Double>();
		rs = stmt.executeQuery("select UID,test4 from test_samples");
		
		while(rs.next()) {
			
			UID = (double) rs.getInt("UID");
			expression = (double) rs.getInt("test4");
			gene_expr.put(UID,expression);
			
		}
		result.add(gene_expr);
		//print(result.get(3).size());
		gene_expr = new TreeMap<Double,Double>();
		rs = stmt.executeQuery("select UID,test5 from test_samples");
		
		while(rs.next()) {
			
			UID = (double) rs.getInt("UID");
			expression = (double) rs.getInt("test5");
			gene_expr.put(UID,expression);
			
		}
		result.add(gene_expr);
		//print(result.get(4).size());
		
		return result;

	}
	public void DBinit() throws ClassNotFoundException, SQLException{

		//Register the JDBC driver for MySQL.
		Class.forName("com.mysql.jdbc.Driver");

		//Define URL of database server 
		String url =
				"jdbc:mysql://localhost:3306/cse_601";

		//Get a connection to the database for a user
		Connection con =
				DriverManager.getConnection(
						url,"root", "");

		//Display URL and connection information
		//print("URL: " + url);
		//print("Connection: " + con);

		//Get a Statement object
		stmt = con.createStatement();

		print("Database connection established");

	}
	
	public static void print(Object obj) {
		System.out.println(obj);
	}

}
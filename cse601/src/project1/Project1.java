package project1;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import sqlconnect.SQLConnect;

public class Project1 {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		SQLConnect sqlobj = new SQLConnect();
		
		
		
		
		
		
		//********part 2.1
		//query1(sqlobj,"ALL");
		
		//********part 2.2
		//query2(sqlobj,"ALL");
		
		//********part 2.3
		query3(sqlobj,"ALL",2,1);
	
		//********part 2.4
		//print(query4(sqlobj,"ALL",12502));
		
		//******part 2.5
		//print(query5(sqlobj,7154));
		
		//******part 2.6
		//query6(sqlobj,7154);
		
		//********part 3
		//Map<Double,Integer> informative_genes = part3(sqlobj,"ALL");
		
		//*******part 3.2
		//classify(sqlobj);
		
	}

	public static void print(Object obj) {
		System.out.print(obj);
	}
	

	
	public static double[] classify(SQLConnect sqlobj) throws SQLException {
		
		Map<Double,Integer> informative_genes = part3(sqlobj,"ALL");
		List<TreeMap<Double,Double>> test_samples = sqlobj.getTest_Samples();
		
		//filter test data according to informative genes
		for (int i = 0;i < test_samples.size(); i++) {
			for (Iterator<Double> it = test_samples.get(i).keySet().iterator(); it.hasNext();) {
				if(informative_genes.get(it.next()) == null) {
					it.remove();
				}
			}
		}
	
		List<double[]> test_sample_array = new ArrayList<double[]>();
		
		for (int i = 0;i < test_samples.size(); i++) {
			double[] temp_arr = new double[41];
			int index = 0;
			for (Iterator<Double> it = test_samples.get(i).values().iterator(); it.hasNext();) {
				temp_arr[index] = it.next();
				index++;
			}
			test_sample_array.add(temp_arr);
		}
	
		
		Map<Double, ArrayList<Double>> all_map = sqlobj.getSampleIDWithDisease("ALL");
		
		//print(all_map.size());
		
		Map<Double, ArrayList<Double>> other_map = sqlobj.getSampleIDWithoutDisease("ALL");
		
		//print(other_map.size());
		
		Map<Double, HashMap<Double, Double>> sample_expr = sqlobj.getMapOfSamples();
		
		//Set<Double> all_set = all_map.keySet();
		//Set<Double> other_set = other_map.keySet();
		double[] p_arr = new double[5];
		
		for (int test_num = 0; test_num < 5; test_num ++) {
			int len = informative_genes.size();
			double[] rA = new double[all_map.size()];
			
			List<double[]> listA = new ArrayList<double[]>();
			int traverse = 0;
			for (Entry<Double, ArrayList<Double>> o : all_map.entrySet()) {
				double[] temp_expr = new double[len];
				//map of sample_ids
				for (Entry<Double, HashMap<Double, Double>> e : sample_expr.entrySet()) {
					//check if sample_ids are equal ******don't ever use == with two doubles
					if(o.getKey().intValue() == e.getKey().intValue()) {
						int index = 0;
						//filter from informative_genes
						for (Entry<Double, Double> ge : e.getValue().entrySet()) {
							//System.out.print("\t"+ge.getKey());
							if(informative_genes.containsKey(ge.getKey())) {
								//System.out.print("########");
								temp_expr[index] = ge.getValue();
								//System.out.print("\t" +temp_expr[index]);
								index++;
							}
						}
						//print();
					}
				}
				listA.add(temp_expr);
				//print(temp_expr);
				rA[traverse] = calculateCorrelation(temp_expr, test_sample_array.get(test_num));
				traverse++;
			}
			
			
			double[] rB = new double[other_map.size()];
			List<double[]> listB = new ArrayList<double[]>();
			traverse = 0;
			for (Entry<Double, ArrayList<Double>> o : other_map.entrySet()) {
			//for (Iterator<Double> it = all_map.keySet().iterator(); it.hasNext();) {
				double[] temp_expr = new double[len];
				//map of sample_ids
				for (Entry<Double, HashMap<Double, Double>> e : sample_expr.entrySet()) {
					//check if sample_ids are equal ******don't ever use == with two doubles
					if(o.getKey().intValue() == e.getKey().intValue()) {
						int index = 0;
						//filter from informative_genes
						for (Entry<Double, Double> ge : e.getValue().entrySet()) {
							if(informative_genes.containsKey(ge.getKey())) {
								temp_expr[index] = ge.getValue();
								index++;
							}
						}
					}
				}
				listB.add(temp_expr);
				rB[traverse] = calculateCorrelation(temp_expr, test_sample_array.get(test_num));
				traverse++;
			}
			
			
			double t = calculateTStatistic(rA, rB);
			//calculate p
			TDistribution tdist = new TDistribution(degreesofFreedom(rA, rB));
			double p = 1 - tdist.cumulativeProbability(t);
			p_arr[test_num] = p;
					
			print("Test "+ test_num + " - >" + p + " " + (p < 0.05) );
		}
		return p_arr;
	}
	
	public static int query1(SQLConnect sqlobj, String disease) throws SQLException {
		
		int count = sqlobj.getCountofDisease(disease);
		print("Count of "+ disease + " = " + count);
		return count;
	}
	
	public static String[] query2(SQLConnect sqlobj, String disease) throws SQLException {
	
		String drugs[] = sqlobj.getTypesofDrugs(disease);
		
		print("Types of drugs for " + disease + "are: ");
		for(int i = 0;i< drugs.length;i++)
			print(drugs[i]);
		
		return drugs;
	}
	
	public static String[] query3(SQLConnect sqlobj, String disease, int cluster_id, int mu_id) throws SQLException {
	
		String mRNA[] = sqlobj.getExpressionValues(disease, cluster_id, mu_id);
		
		print("There are " +mRNA.length +" mRNA values : ");
		for(int i = 0;i< mRNA.length;i++)
			print(mRNA[i]);
		
		return mRNA;
	}
	
	public static double query4(SQLConnect sqlobj, int go_id) throws SQLException {
		
		double a1[] = sqlobj.getExpressionValuesWithDisease("ALL", go_id);
		double a2[] = sqlobj.getExpressionValuesWithoutDisease("ALL", go_id);
		
		return calculateTStatistic(a1, a2);
	}
	
	public static double query5(SQLConnect sqlobj, int go_id) throws SQLException {
		double a1[] = sqlobj.getExpressionValuesWithDisease("ALL", go_id);
		print(a1.length);
		double a2[] = sqlobj.getExpressionValuesWithDisease("AML", go_id);
		print(a2.length);
		double a3[] = sqlobj.getExpressionValuesWithDisease("colon tumor", go_id);
		print(a3.length);
		double a4[] = sqlobj.getExpressionValuesWithDisease("breast tumor", go_id);
		print(a4.length);
		
		return calculateFStat(a1,a2,a3,a4);
	}
	
	
	public static double[] query6(SQLConnect sqlobj,int go_id) throws SQLException {
		
		ArrayList<ArrayList<Double>> expr_list = new ArrayList<ArrayList<Double>>();
		Map<Double, ArrayList<Double>> mapOfExpressionValues_ALL = sqlobj.getMapOfExpressionValuesWithDisease("ALL", go_id);
		int count = 0;
		
		
		for (Entry<Double, ArrayList<Double>> l : mapOfExpressionValues_ALL.entrySet()) {
			if(count == 2)
				break;
			else {
				expr_list.add(l.getValue());
			}
			count++;
		}
		
		
		ArrayList<Double> expr = expr_list.get(0);
		double a1[] = getDoubleArray(expr);
		
		expr = expr_list.get(1);
		double a2[] = getDoubleArray(expr);
		
		double[] result = new double[2]; 
		double one = calculateCorrelation(a1,a2);
		print("Correlation between two ALL patients = "+one);
		result[0] = one;
		
		Map<Double, ArrayList<Double>> mapOfExpressionValues_AML = sqlobj.getMapOfExpressionValuesWithDisease("AML", go_id);
		
		count = 0;
		
		for (Entry<Double, ArrayList<Double>> l : mapOfExpressionValues_AML.entrySet()) {
			if(count == 1)
				break;
			else {
				expr = l.getValue();
			}
			count++;
		}
		
		double a3[] = getDoubleArray(expr);
		double two = calculateCorrelation(a1,a3);
		result[1] = two;
		//print("Correlation between one ALL patient and one AML patient = " + calculateCorrelation(a1,a3));
		return result;
	}
	
	public static Map<Double, Integer> part3(SQLConnect sqlobj,String disease) throws SQLException {
		Map<Double, ArrayList<Double>> all_map = sqlobj.getSampleIDWithDisease(disease);
		
		//print(all_map.size());
		
		Map<Double, ArrayList<Double>> other_map = sqlobj.getSampleIDWithoutDisease(disease);
		
		//print(other_map.size());
		
		Map<Double, HashMap<Double, Double>> gene_expr = sqlobj.getMapOfGenes();
		
		Set<Double> all_set = all_map.keySet();
		Set<Double> other_set = other_map.keySet();
		
		int count = 0;
		int informative_genes_count = 0;
		Map<Double,Integer> informative_genes = new TreeMap<Double,Integer>();
		for (Entry<Double, HashMap<Double, Double>> e : gene_expr.entrySet()) {
			
			Set<Double> s_id_gene_all = new HashSet<Double>();
			
			HashMap<Double, Double> sample_expr = e.getValue();//part3.get((double)8191);
			s_id_gene_all.addAll(sample_expr.keySet());//e.getValue());//
			
			Set<Double> s_id_gene_none = new HashSet<Double>();
			s_id_gene_none.addAll(sample_expr.keySet());//e.getValue());
				
			s_id_gene_all.retainAll(all_set);
			s_id_gene_none.retainAll(other_set);
			
			Iterator itr = s_id_gene_all.iterator();
			double[] a1 = new double[s_id_gene_all.size()];
			int i = 0;
			while(itr.hasNext()) {
				a1[i] = sample_expr.get(itr.next());
				i++;
			}
			
			i = 0;
			
			itr = s_id_gene_none.iterator();
			double[] a2 = new double[s_id_gene_none.size()];
			while(itr.hasNext()) {
				a2[i] = sample_expr.get(itr.next());
				i++;
			}
			
			
			//calculate t
			double t = calculateTStatistic(a1,a2);
			
			//calculate p
			TDistribution tdist = new TDistribution(degreesofFreedom(a1, a2));
			double p = 1 - tdist.cumulativeProbability(t);
			//print(p);
			//print((count++)+","+p);
			//print("p = "+p);
			if(p < 0.005) {
				informative_genes_count++;
				informative_genes.put(e.getKey(), 1);
				print(e.getKey()+"\t"+p);
			}
		}
		print("No. of informative genes = " + informative_genes_count);
		return informative_genes;
	}
	
	public static int degreesofFreedom(double a1[], double a2[]) {
		Mean m = new Mean();
		double m1 = m.evaluate(a1);
		double m2 = m.evaluate(a2);
		
		Variance v = new Variance();
		double v1 = v.evaluate(a1);
		double v2 = v.evaluate(a2);
		
		double num;
		double den;
		
		num = Math.pow(((v1/a1.length) + (v2/a2.length)), 2);
		
		den = ((Math.pow((v1/a1.length), 2) ) /(a1.length -1) ) + ((Math.pow((v2/a2.length), 2) ) /(a2.length -1) );
		
		
		double result = num/den;
		//print(result);
		return (int) result;
	}
	
	
	public static double[] getDoubleArray(ArrayList<Double> a) {
		
		//ArrayList<Double> te_1 = te.get(0);
		double arr[] = new double[a.size()];
		for (int i = 0; i< a.size(); i++)
			arr[i] = a.get(i);
		
		return arr;
	}
	
	
	public static double calculateTStatistic(double a1[], double a2[]) {
		//print(a1.length);
		//print(a2.length);
		
		Mean m = new Mean();
		double m1 = m.evaluate(a1);
		double m2 = m.evaluate(a2);
		
		Variance v = new Variance();
		double v1 = v.evaluate(a1);
		double v2 = v.evaluate(a2);
		
		//print("m1 = "+m1);
		//print("m2 = "+m2);
		
		//print("v1 = "+v1);
		//print("v2 = "+v2);
		
		double t = (m1-m2)/Math.sqrt((v1/a1.length)+(v2/a2.length));
		
		return Math.abs(t);
	}
	
	public static double calculateCorrelation(double a1[], double a2[]) {
		Variance v = new Variance();
		double varX = v.evaluate(a1);
		//print("varX: "+varX);
		double varY = v.evaluate(a2);
		//print("varY: "+varY);
				
		double cov = new Covariance().covariance(a1, a2,false);
		
		//print("cov: "+cov);
		double result = (cov/(Math.sqrt(varX*varY)));

		
		return result;
	}
	
	// method to calculate mean
	public static double calcMean(double mArr[]){		
		double size = mArr.length;
		double totalSum = 0;
		
		for(double each : mArr){
			totalSum = totalSum + each;
			//System.out.print(each+",");
		}
		//print();
		double mean = totalSum/size; 
		//print(mean);		
		
		return mean;
	}

	
	public static double calculateFStat(double a1[], double a2[], double a3[], double a4[]){
		int countDisease = 4;
		List<double[]> diseaseMeasure = new ArrayList<double[]>();		
		
		diseaseMeasure.add(a1);
		diseaseMeasure.add(a2);
		diseaseMeasure.add(a3);
		diseaseMeasure.add(a4);
		
		//TestUtils te;
		//double fStat = TestUtils.oneWayAnovaFValue(diseaseMeasure);
		//te.oneWayAnovaFValue(diseaseMeasure);
		// means of the measure per disease
		double meanArr[] = new double[countDisease];
		//overall mean of all measures from input[]
		double overallMean;
		int total_measure = a1.length + a2.length + a3.length +a4.length;
		
		
		for (int i = 0; i < diseaseMeasure.size();i++) {
			meanArr[i] = calcMean(diseaseMeasure.get(i));
			print(i+": "+meanArr[i]);
		}
		overallMean = (meanArr[0]+meanArr[1]+meanArr[2]+meanArr[3])/(countDisease);
		print("overallMean: "+overallMean);
		int z = 0;
		int k = 0;
		
		double
		// Disease Sum of Squares
				diseaseSS = 0,
		// Error Sum of Squares		
				errorSS = 0,
		// Degree of Freedom for Disease
				degreeDisease = countDisease - 1,
		// Disease Mean Squares		
				diseaseMS = 0,
		// Degree of Freedom for Errors
				degreeError = total_measure - countDisease,
		// Error Mean Square
				errorMS = 0,
		// F Statistic
				fStat = 0;

		double diff = 0;
		for(int i = 0; i < countDisease; i++){
			//index = 0;
			for(int j = 0; j < diseaseMeasure.get(i).length ; j++){
				diff = 0;
				// calculate difference between each mean of each disease and overallMean
				diff = (meanArr[i] - overallMean);
				diff = Math.pow(diff, 2);
				
				diseaseSS = diseaseSS + diff;
				
				diff = 0;
				// 
				diff = diseaseMeasure.get(i)[j] - meanArr[i];
				diff = Math.pow(diff, 2);
				errorSS = errorSS + diff;
			}
		}
		print("diseaseSS:"+diseaseSS+"\nerrorSS: "+errorSS);
	
		diseaseMS = diseaseSS / degreeDisease;
		errorMS = errorSS/ degreeError;
		
		// calculate the fStat
		fStat = diseaseMS/errorMS;
		print("fStat: "+fStat);
		
		return fStat;
	}	
}

package project1;

import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.correlation.Covariance;

public class Tdist {
	
	public static void main(String[] args) {
		
		TDistribution tdist = new TDistribution(5,100000000);
		
		System.out.println(1-tdist.cumulativeProbability(3.12));
		
		FDistribution fdist = new FDistribution(2, 12);
		
		System.out.println(1-fdist.cumulativeProbability(6.5));
		
		int count = 0;
		for(int i=0; i < 10; ++i)
		{
			count = count++;
			
			
		}
		System.out.println(count);
		int i=5,j=0;
		//j=i++ + i++ + i++;
		j=++i + ++i + ++i;
		System.out.println(i+" "+j);
	}
}

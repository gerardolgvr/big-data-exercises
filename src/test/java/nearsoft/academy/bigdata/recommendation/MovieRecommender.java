package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.collections.map.HashedMap;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class MovieRecommender {
	// variables
	private int totalReviews, totalProducts, totalUsers;
	private HashMap<String, Integer> mapUsers = new HashMap<>();
	private HashMap<String, Integer> mapProducts = new HashMap<>();
	private DataModel dataModel;
	private UserSimilarity similarity;
	private UserNeighborhood neighborhood;
	private UserBasedRecommender recommender;		
	private String inputPath, outputPath;
	
	//constructor
	public MovieRecommender(String inputPath) throws IOException, TasteException {
		this.outputPath = "src/data.csv";
		
		this.inputPath = inputPath;
		this.totalReviews = 0;
		this.totalProducts = 0;
		this.totalUsers = 0;
		
		this.loadData();
		
		
		this.dataModel = new FileDataModel(new File(this.outputPath));
		this.similarity = new PearsonCorrelationSimilarity(this.dataModel);
		this.neighborhood = new ThresholdUserNeighborhood(0.1, this.similarity, this.dataModel);
		this.recommender = new GenericUserBasedRecommender(this.dataModel, this.neighborhood, this.similarity);	
				
	}	
	
	public int getTotalReviews() {	
		return totalReviews;
	}

	public int getTotalProducts() {
		return totalProducts;
	}

	public int getTotalUsers() {
		return totalUsers;
	}
	
	public void loadData() throws IOException {		
		BufferedReader br = new BufferedReader(new FileReader(this.inputPath));
		BufferedWriter bw = new BufferedWriter(new FileWriter("src/data.csv"));
		String line, productId = "", userId = "", score = "";	
		
		//counters for translation
		int currentIdUser = 0, currentIdProduct = 0;
		
		line = br.readLine();
		while(line != null) {
			
			if(line.contains("product/productId")) {				
	        	productId = line.substring(18).trim();
	        	if(!mapProducts.containsKey(productId)) {	        		
	        		currentIdProduct++;		        		
	        		mapProducts.put(productId, currentIdProduct);
	        		// increase total products
	        		this.totalProducts++;
	        	} else {
	        		currentIdProduct = mapProducts.get(productId);
	        	}
	        	// increase total reviews
	        	this.totalReviews++;
	        }
			
			if(line.contains("review/userId")) {
	        	userId = line.substring(14).trim();		
	        	if(!mapUsers.containsKey(userId)) {	        		
	        		currentIdUser++;		        		
	        		mapUsers.put(userId, currentIdUser);
	        		// increase total users
	        		this.totalUsers++;
	        	} else {
	        		currentIdUser = mapUsers.get(userId);
	        	}
	        } 	
			
			if(line.contains("review/score")) {
	        	score = String.valueOf( (int) Float.parseFloat(line.substring(13).trim()));			        	
	        	bw.write(currentIdUser+","+currentIdProduct+","+score+"\n");
	        } 
			
			line = br.readLine();
		}
		
		br.close();
		bw.close();
	}

	public List<String> getRecommendationsForUser(String userId) throws TasteException {
		List<String> list = new ArrayList<String>();
		
        int translatedId = mapUsers.get(userId);        
        List<RecommendedItem> recommendations = recommender.recommend(translatedId, 3);
        
        for (RecommendedItem recommendation: recommendations) {
            list.add(MapUtils.getKey(this.mapProducts, (int) recommendation.getItemID()));
        }
                
        return list;
	}
	
	/*public static void main(String[] args) throws IOException, TasteException {				
		MovieRecommender movieRecommender = new MovieRecommender("src/movies.txt");
		
		System.out.println(" my reviews " + movieRecommender.getTotalReviews());
		System.out.println(" my products " + movieRecommender.getTotalProducts());
		System.out.println(" my users " + movieRecommender.getTotalUsers());
		
		List<String> recommendations = movieRecommender.getRecommendationsForUser("A141HP4LYPWMSR");
		for (String recommendation : recommendations) {
			  System.out.println(recommendation);
		}
	

	}*/

}


// method to get Map's key from value in java
class MapUtils {
	public static <K, V> K getKey(Map<K, V> map, V value) {
		for (K key : map.keySet()) {
			if (value.equals(map.get(key))) {
				return key;
			}
		}
		return null;
	}
}

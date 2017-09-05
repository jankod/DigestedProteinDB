package hr.pbf.digestdb.test;

import java.util.ArrayList;

import hr.pbf.digestdb.util.PermutationProteinTriplets;

public class TestPermutation {

	
	public static void main(String[] args) {
		ArrayList<String> res = PermutationProteinTriplets.getAllplets("1234567", 3);
		System.out.println(res);
		
	}
	
}

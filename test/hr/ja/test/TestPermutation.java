package hr.ja.test;

import java.util.ArrayList;

import hr.createnr.util.PermutationProteinTriplets;

public class TestPermutation {

	
	public static void main(String[] args) {
		ArrayList<String> res = PermutationProteinTriplets.getAllplets("1234567", 3);
		System.out.println(res);
		
	}
	
}

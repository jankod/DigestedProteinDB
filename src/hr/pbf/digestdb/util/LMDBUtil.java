package hr.pbf.digestdb.util;

import org.fusesource.lmdbjni.*;
import static org.fusesource.lmdbjni.Constants.*;

public class LMDBUtil {

	public static void main(String[] args) {
		 try (Env env = new Env("c:/tmp/lmdb.db")) {
			
			   try (Database db = env.openDatabase()) {
				   db.put(BiteUtil.toByte("jedan"), BiteUtil.toByte(222));
			   }
			 }
		 
		 
		 System.out.println("Finish");
	}
}

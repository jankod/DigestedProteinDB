package hr.pbf.digestdb.test.proba;

import java.io.Console;
import java.io.IOException;

public class ConsoleSamePlace {

	public static void main(String[] args) throws InterruptedException, IOException {
		String anim= "|/-\\";
        for (int x =0 ; x < 100 ; x++) {
            String data = "\r" + anim.charAt(x % anim.length()) + " " + x;
            System.out.write(data.getBytes());
            Thread.sleep(100);
        }
	}
}

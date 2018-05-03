package hr.pbf.digestdb.test.experiments;

public class Fix {

    public static final int FIXED_POINT = 16;
    public static final int ONE = 1 << FIXED_POINT;

    public static int mul(int a, int b) {
        return (int) ((long) a * (long) b >> FIXED_POINT);
    }

    public static int toFix( double val ) {
        return (int) (val * ONE);
    }

    public static int intVal( int fix ) {
        return fix >> FIXED_POINT;
    }

    public static double doubleVal( int fix ) {
        return ((double) fix) / ONE;
    }

    public static void main(String[] args) {
        double f1d = 4234.123456789;

        p("Orig: "+ f1d);
        p("Float "+ (float)f1d);
        int f1fix = toFix(f1d);
        p("to fix "+ f1fix);

        p("Double ponovo " + doubleVal(f1fix));
        p("Float ponovo "+ (float) doubleVal(f1fix));




        int f1 = toFix( Math.PI );
        int f2 = toFix( 2 );

        int result = mul( f1, f2 );
//        System.out.println( "f1:" + f1 + "," + intVal( f1 ) );
//        System.out.println( "f2:" + f2 + "," + intVal( f2 ) );
//        System.out.println( "r:" + result +"," + intVal( result));
//        System.out.println( "double: " + doubleVal( result ));

    }

    private static void p(Object s) {
        System.out.println(s);
    }
}

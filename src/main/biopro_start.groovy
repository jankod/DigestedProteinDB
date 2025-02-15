import groovy.lang.GroovyClassLoader

// Putanja do vašeg JAR fajla na serverur
String jarFilePath = "/Users/tag/IdeaProjects/DigestedProteinDB/target/digestdb-1.0-SNAPSHOT-jar-with-dependencies.jar"  // **VAŽNO: Promijenite ovu putanju!**

// Ime klase koju želite koristiti iz JAR fajla (puni paketni put)
String javaClassName = "hr.pbf.digestdb.rocksdb.MainUniprotToCsv"

try {
    // 1. Stvaranje GroovyClassLoader-a
    GroovyClassLoader groovyClassLoader = new GroovyClassLoader()

    // 2. Dodavanje JAR fajla u classpath ClassLoader-a
    groovyClassLoader.addClasspath(jarFilePath)

    // 3. Učitavanje Java klase iz JAR fajla
    Class<?> javaClass = groovyClassLoader.loadClass(javaClassName)

    // 4. Stvaranje instance Java klase (ako je potrebno)
    // Pretpostavka: Java klasa ima defaultni konstruktor (bez argumenata)
    def javaObject = javaClass.getDeclaredConstructor().newInstance()
   // hr.pbf.digestdb.rocksdb.MainUniprotToCsv c = new hr.pbf.digestdb.rocksdb.MainUniprotToCsv()

    println javaObject.swisprotPath

} catch (Exception e) {
    println "Greška prilikom pozivanja Java aplikacije: ${e.getMessage()}"
    e.printStackTrace() // Ispis detalja greške za debugiranje
}

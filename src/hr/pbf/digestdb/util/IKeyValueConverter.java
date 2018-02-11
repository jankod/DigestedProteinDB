package hr.pbf.digestdb.util;

public interface IKeyValueConverter<K, V> {

	byte[] keyToByte(K key);
	K byteToKey(byte[] bytes);
	
	
	byte[] valueToByte(V key);
	V byteToValue(byte[] bytes);
	
}

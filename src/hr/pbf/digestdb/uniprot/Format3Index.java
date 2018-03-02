package hr.pbf.digestdb.uniprot;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;

public class Format3Index {
	private float[] masses;
	private long[] filePositions;
	private long fileLength;

	public Format3Index(int length) {
		// int p = 13353898 all uniprot
		masses = new float[length];
		filePositions = new long[length]; // start position
	}

	public Format3Index(DataInput in) throws IOException {
		readFromDataOutput(in);
	}

	public void readFromDataOutput(DataInput in) throws IOException {
		this.fileLength = in.readLong();
		int arrayLength = in.readInt();
		masses = new float[arrayLength];
		filePositions = new long[arrayLength];
		for (int i = 0; i < filePositions.length; i++) {
			masses[i] = in.readFloat();
			filePositions[i] = in.readLong();
		}
	}

	public void writeToDataOutput(DataOutput o) throws IOException {
		if (filePositions.length != masses.length) {
			throw new RuntimeException("Not same length: filePositions and masses array");
		}
		o.writeLong(fileLength);
		o.writeInt(filePositions.length);
		for (int i = 0; i < filePositions.length; i++) {
			o.writeFloat(masses[i]);
			o.writeLong(filePositions[i]);
		}
	}

	// unique float 13.353.898 = 53 MB

	public void newEntry(int arrayIndexPosition, float mass, long position) {
		assert masses.length > arrayIndexPosition : "arrayIndexPosision is larger then array length. arrayIndex: "
				+ arrayIndexPosition + " masses length: " + masses.length;

		masses[arrayIndexPosition] = mass;
		filePositions[arrayIndexPosition] = position;
	}

	public int size() {
		if (masses == null) {
			return 0;
		}
		return masses.length;
	}

	/**
	 * If exist exactly mass in file
	 * 
	 * @param mass
	 * @return
	 */
	public long[] get(float mass) {
		int res = Arrays.binarySearch(masses, mass);
//		long position1;
//		long position2;
		
		long[] postitions = new long[2];
		if (res >= 0) {
			long filePosStart = filePositions[res];
			long filePosEnd;
			if (res + 1 >= filePositions.length) {
//				position1 = fileLength;
				filePosEnd = fileLength;
			} else {
				filePosEnd = filePositions[res + 1];
			}

			postitions[0] = filePosStart;
			postitions[1] = filePosEnd;
			// UniprotUtil.toByteArrayFast(fileD, from, to)
			return postitions;
		}

		return null;
	}

	public long[] get(float from, float to) {

		int resFrom = Arrays.binarySearch(masses, from);
		int resTo = Arrays.binarySearch(masses, to);

		long filePosStart;
		long filePostEnd;

		if (Math.abs(resFrom) > filePositions.length) {
			return null;
		}

		if (resTo == -1) {
			return null;
		}

		if (resFrom >= 0) {
			filePosStart = filePositions[resFrom];
		} else {

			int abs = Math.abs(resFrom);
			if (abs > filePositions.length) {
				filePosStart = fileLength;
			} else {
				filePosStart = abs;
			}

		}

		if (resTo >= 0) {
			filePostEnd = filePositions[resFrom];
		} else {
			int abs = Math.abs(resTo);
			if (abs > filePositions.length) {
				filePostEnd = fileLength;
			} else {
				filePostEnd = abs;
			}
		}
		return null; // NE VALJA
//		return new long[] { filePositions[ filePosStart], filePositions[ filePostEnd] };
	}

	public void setLastPostitionFileLength(long fileLength) {
		this.fileLength = fileLength;
	}

}

package fr.inria.powerapi.sensor.powerspy;

import java.nio.ByteOrder;

public class IEEE754Utils {
	public static Float fromString(String bits) throws NumberFormatException {
		if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
			char[] bitsCharArray = bits.toCharArray();
			for (int i = 0; i < bitsCharArray.length / 2; i += 2) {
				char first = bitsCharArray[i];
				char second = bitsCharArray[i + 1];
				bitsCharArray[i] = bitsCharArray[bitsCharArray.length - i - 2];
				bitsCharArray[i + 1] = bitsCharArray[bitsCharArray.length - i
						- 1];
				bitsCharArray[bitsCharArray.length - i - 2] = first;
				bitsCharArray[bitsCharArray.length - i - 1] = second;
			}
			bits = String.valueOf(bitsCharArray);
		}
		return Float.intBitsToFloat(Integer.valueOf(bits, 16));
	}
}

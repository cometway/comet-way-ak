
package com.cometway.util;


public class ByteTools
{
	public static byte[] intToBytes(int integer)
	{
		byte[] ret = new byte[4];

		ret[0] = (byte) ((integer >>> 24) & 0xFF);
		ret[1] = (byte) ((integer >>> 16) & 0xFF);
		ret[2] = (byte) ((integer >>>  8) & 0xFF);
		ret[3] = (byte) ((integer >>>  0) & 0xFF);

		return (ret);
	}


	public static void insertInt(int integer, byte[] bytes, int offset)
	{
		if (bytes.length < offset + 4)
		{
			throw new ArrayIndexOutOfBoundsException();
		}

		bytes[offset] = (byte) ((integer >>> 24) & 0xFF);
		bytes[offset + 1] = (byte) ((integer >>> 16) & 0xFF);
		bytes[offset + 2] = (byte) ((integer >>>  8) & 0xFF);
		bytes[offset + 3] = (byte) ((integer >>>  0) & 0xFF);
	}


	public static int bytesToInt(byte[] bytes, int offset)
	{
		if (bytes.length < offset + 4)
		{
			throw new ArrayIndexOutOfBoundsException();
		}

		int ch1 = (bytes[offset] & 0xFF);
		int ch2 = (bytes[offset + 1] & 0xFF);
		int ch3 = (bytes[offset + 2] & 0xFF);
		int ch4 = (bytes[offset + 3] & 0xFF);

		if ((ch1 | ch2 | ch3 | ch4) < 0)
		{
			throw new IllegalArgumentException();
		}

		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}


	public static int bytesToInt(byte[] bytes)
	{
		return (bytesToInt(bytes, 0));
	}


	public static byte[] longToBytes(long l)
	{
		byte[] ret = new byte[8];

		ret[0] = (byte) ((int)(l >>> 56) & 0xFF);
		ret[1] = (byte) ((int)(l >>> 48) & 0xFF);
		ret[2] = (byte) ((int)(l >>> 40) & 0xFF);
		ret[3] = (byte) ((int)(l >>> 32) & 0xFF);
		ret[4] = (byte) ((int)(l >>> 24) & 0xFF);
		ret[5] = (byte) ((int)(l >>> 16) & 0xFF);
		ret[6] = (byte) ((int)(l >>>  8) & 0xFF);
		ret[7] = (byte) ((int)(l >>>  0) & 0xFF);

		return (ret);
	}


	public static void insertLong(long l, byte[] bytes, int offset)
	{
		if (bytes.length < offset + 8)
		{
			throw new ArrayIndexOutOfBoundsException();
		}

		bytes[offset] = (byte) ((int)(l >>> 56) & 0xFF);
		bytes[offset + 1] = (byte) ((int)(l >>> 48) & 0xFF);
		bytes[offset + 2] = (byte) ((int)(l >>> 40) & 0xFF);
		bytes[offset + 3] = (byte) ((int)(l >>> 32) & 0xFF);
		bytes[offset + 4] = (byte) ((int)(l >>> 24) & 0xFF);
		bytes[offset + 5] = (byte) ((int)(l >>> 16) & 0xFF);
		bytes[offset + 6] = (byte) ((int)(l >>>  8) & 0xFF);
		bytes[offset + 7] = (byte) ((int)(l >>>  0) & 0xFF);
	}


	public static long bytesToLong(byte[] bytes, int offset)
	{
		if (bytes.length < offset + 8)
		{
			throw new ArrayIndexOutOfBoundsException();
		}

		return (((long)(bytesToInt(bytes, offset)) << 32) + (bytesToInt(bytes, offset + 4) & 0xFFFFFFFFL));
	}

	public static long bytesToLong(byte[] bytes)
	{
		return (bytesToLong(bytes, 0));
	}


	public static byte[] floatToBytes(float f)
	{
		return (intToBytes(Float.floatToIntBits(f)));
	}


	public static void insertFloat(float f, byte[] bytes, int offset)
	{
		insertInt(Float.floatToIntBits(f), bytes, offset);
	}


	public static float bytesToFloat(byte[] bytes, int offset)
	{
		return (Float.intBitsToFloat(bytesToInt(bytes, offset)));
	}


	public static float bytesToFloat(byte[] bytes)
	{
		return (bytesToFloat(bytes, 0));
	}


	public static byte[] doubleToBytes(double d)
	{
		return (longToBytes(Double.doubleToLongBits(d)));
	}


	public static void insertDouble(double d, byte[] bytes, int offset)
	{
		insertLong(Double.doubleToLongBits(d), bytes, offset);
	}


	public static double bytesToDouble(byte[] bytes, int offset)
	{
		return (Double.longBitsToDouble(bytesToLong(bytes, offset)));
	}


	public static double bytesToDouble(byte[] bytes)
	{
		return (bytesToDouble(bytes, 0));
	}
}



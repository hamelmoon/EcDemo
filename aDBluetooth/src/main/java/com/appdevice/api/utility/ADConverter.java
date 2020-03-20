package com.appdevice.api.utility;

import java.util.UUID;

public class ADConverter
{
	public static String byteArrayToHexString(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes)
			sb.append(String.format("%02X", b & 0xff));
		return sb.toString();
	}

	public static byte[] hexStringToByteArray(String string)
	{
		int length = string.length();
		byte[] data = new byte[length / 2];
		for (int i = 0; i < length; i += 2)
		{
			data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character.digit(string.charAt(i + 1), 16));
		}
		return data;
	}

	public static UUID UUIDfrom16BitString(String uuid16)
	{
		if (uuid16 == null)
		{
			throw new NullPointerException("uuid16 == null");
		}

		if (uuid16.length() != 4)
		{
			throw new IllegalArgumentException("Invalid UUID: " + uuid16);
		}

		return UUID.fromString(String.format("0000%s-0000-1000-8000-00805f9b34fb", uuid16));
	}
}

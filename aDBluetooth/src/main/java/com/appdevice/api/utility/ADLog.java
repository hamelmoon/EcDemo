package com.appdevice.api.utility;

public class ADLog
{
	public static boolean isLoggable = true;
	static int mLogLevel = android.util.Log.DEBUG;
	static final int mMinLogLevel = android.util.Log.VERBOSE;

	public static void setLogLevel(int logLevel)
	{
		if (logLevel < mMinLogLevel)
		{
			mLogLevel = mMinLogLevel;
		}
		else
		{
			mLogLevel = logLevel;
		}
	}

	public static void i(String tag, String format, Object... args)
	{
		if (isLoggable && android.util.Log.INFO >= mLogLevel)
		{
			String[] logStrings = spliteLogString(String.format(format, args));
			for (String logString : logStrings)
			{
				android.util.Log.i(tag, logString);
			}
		}
	}

	public static void e(String tag, String format, Object... args)
	{
		if (isLoggable && android.util.Log.ERROR >= mLogLevel)
		{
			String[] logStrings = spliteLogString(String.format(format, args));
			for (String logString : logStrings)
			{
				android.util.Log.e(tag, logString);
			}
		}
	}

	public static void d(String tag, String format, Object... args)
	{
		if (isLoggable && android.util.Log.DEBUG >= mLogLevel)
		{
			String[] logStrings = spliteLogString(String.format(format, args));
			for (String logString : logStrings)
			{
				android.util.Log.d(tag, logString);
			}
		}
	}

	public static void v(String tag, String format, Object... args)
	{
		if (isLoggable && android.util.Log.VERBOSE >= mLogLevel)
		{
			String[] logStrings = spliteLogString(String.format(format, args));
			for (String logString : logStrings)
			{
				android.util.Log.v(tag, logString);
			}
		}
	}

	public static void w(String tag, String format, Object... args)
	{
		if (isLoggable && android.util.Log.WARN >= mLogLevel)
		{
			String[] logStrings = spliteLogString(String.format(format, args));
			for (String logString : logStrings)
			{
				android.util.Log.w(tag, logString);
			}
		}
	}

	private static String[] spliteLogString(String logString)
	{
		int logStringLength = logString.length();
		int splitCount = logStringLength / 4056;

		if (logStringLength % 4056 != 0)
		{
			splitCount++;
		}

		String[] logStrings = new String[splitCount];

		for (int i = 0; i < splitCount; i++)
		{
			int start = 4056 * i;
			int end = 4056 * (i + 1);
			if (end > logStringLength)
			{
				end = start + logStringLength - start;

			}
			logStrings[i] = logString.substring(start, end);
		}
		return logStrings;
	}
}

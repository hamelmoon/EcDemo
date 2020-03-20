package com.appdevice.domyos;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

interface DCCommandCompletionBlock
{
	void completed(DCCommand command);
}

interface DCCommandCompletionBlockWithInfo
{
	void completed(DCCommand command, HashMap<String, Object> info);
}

interface DCCommandCompletionBlockWithError
{
	void completed(DCCommand command, DCError error);
}

public abstract class DCCommand
{
	DCCommandCompletionBlock mCommandCompletionBlock = null;
	DCCommandCompletionBlockWithInfo mCompletionBlockWithInfo = null;
	DCCommandCompletionBlockWithError mCommandCompletionBlockWithError = null;
	boolean mFinish = false;

	protected void setCompletionBlock(DCCommandCompletionBlock completionBlock)
	{
		mCommandCompletionBlock = completionBlock;
	}

	protected void setCompletionBlockWithInfo(DCCommandCompletionBlockWithInfo completionBlock)
	{
		mCompletionBlockWithInfo = completionBlock;
	}

	protected void setCompletionBlockWithError(DCCommandCompletionBlockWithError completionBlock)
	{
		mCommandCompletionBlockWithError = completionBlock;
	}

	protected byte generateChecksum(byte[] commandData)
	{
		byte checksum = 0;
		for (int i = 0; i < commandData.length; i++)
		{
			checksum += commandData[i];
		}
		return checksum;
	}

	protected byte[] generateConsoleUpdateCommand(byte[] data)
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

		try
		{
			dataOutputStream.writeByte(0x55);
			dataOutputStream.writeByte(0x55);

			int checksum = 0;
			for (int i = 0; i < data.length; i++)
			{
				byte value = data[i];
				if (value == (byte) 0x04 || value == (byte) 0x05 || value == (byte) 0x55)
				{
					dataOutputStream.writeByte(0x05);
				}
				dataOutputStream.writeByte(value);
				checksum += value;
			}
			checksum = (~checksum + 1) & 0xff;

			if (checksum == (byte) 0x04 || checksum == (byte) 0x05 || checksum == (byte) 0x55)
			{
				dataOutputStream.writeByte(0x05);
			}

			dataOutputStream.writeByte(checksum);
			dataOutputStream.writeByte(0x04);
			return byteArrayOutputStream.toByteArray();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;

	}

	protected boolean verifyConsoleUpdateCommand(byte[] fullCommandData)
	{
		if (fullCommandData != null && fullCommandData.length >= 5 && fullCommandData[0] == (byte) 0x55 && fullCommandData[1] == (byte) 0x55 && fullCommandData[fullCommandData.length - 1] == (byte) 0x04)
		{
			boolean isQualifier = false;
			int checksum = 0;
			for (int i = 2; i < fullCommandData.length - 2; i++)
			{
				byte value = fullCommandData[i];
				if (value == (byte) 0x04 || value == (byte) 0x05 || value == (byte) 0x55)
				{
					if (isQualifier)
					{
						checksum += (value & 0xFF);
						isQualifier = false;
					}
					else
					{
						if (value == (byte) 0x05)
						{
							isQualifier = true;
						}
						else
						{
							return false;
						}
					}
				}
				else
				{
					checksum += value;
				}
			}
			checksum = (~checksum + 1) & 0xff;
			int reciveChecksum = fullCommandData[fullCommandData.length - 2] & 0xFF;
			if (reciveChecksum == checksum)
			{
				return true;
			}
		}
		return false;
	}

	protected byte[] getConsoleUpdateCommandData(byte[] fullCommandData)
	{
		if (fullCommandData != null && fullCommandData.length >= 5 && fullCommandData[0] == (byte) 0x55 && fullCommandData[1] == (byte) 0x55 && fullCommandData[fullCommandData.length - 1] == (byte) 0x04)
		{
			boolean isQualifier = false;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

			try
			{
				for (int i = 2; i < fullCommandData.length - 2; i++)
				{
					byte value = fullCommandData[i];
					if (value == (byte) 0x04 || value == (byte) 0x05 || value == (byte) 0x55)
					{
						if (isQualifier)
						{
							dataOutputStream.writeByte(value);
							isQualifier = false;
						}
						else
						{
							if (value == (byte) 0x05)
							{
								isQualifier = true;
							}
							else
							{
								return null;
							}
						}
					}
					else
					{
						dataOutputStream.writeByte(value);
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			return byteArrayOutputStream.toByteArray();
		}
		return null;
	}

	protected abstract int getCompatibilityModes();

	protected abstract byte[] getRequestData();

	protected abstract DCError getParameterError();

	protected int getRetryCount()
	{
		return 2;
	}

	protected float getRetryTimeout()
	{
		return 0.5f;
	}

	protected abstract byte getExpectedResponseFirstValue();

	protected abstract int getExpectedResponseLength();

	protected abstract boolean isExpectedResponse(byte[] uncheckedResponse);

	protected abstract HashMap<String, Object> processResponse(byte[] expectedResponse);

	void runCompletionBlock(final HashMap<String, Object> info)
	{
		if (!mFinish)
		{
			if (mCompletionBlockWithInfo != null)
			{
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable()
				{

					@Override
					public void run()
					{
						mCompletionBlockWithInfo.completed(DCCommand.this, info);
					}
				});
			}
			else if (mCommandCompletionBlock != null)
			{
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable()
				{

					@Override
					public void run()
					{
						mCommandCompletionBlock.completed(DCCommand.this);
					}
				});
			}
			mFinish = true;
		}
	}

	void runCompletionBlockWithError(final DCError error)
	{
		if (!mFinish)
		{
			if (mCommandCompletionBlockWithError != null)
			{
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable()
				{

					@Override
					public void run()
					{
						mCommandCompletionBlockWithError.completed(DCCommand.this, error);
					}
				});

			}
			mFinish = true;
		}
	}
}

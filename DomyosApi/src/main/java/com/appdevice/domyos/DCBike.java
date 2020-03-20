package com.appdevice.domyos;


import androidx.annotation.NonNull;

import com.appdevice.domyos.commands.DCGetCumulativeKMCommand;
import com.appdevice.domyos.commands.DCGetInfoValueCommand;
import com.appdevice.domyos.commands.DCGetSerialNumberCommand;
import com.appdevice.domyos.commands.DCGetUsageHourCommand;
import com.appdevice.domyos.commands.DCGetVersionCommand;
import com.appdevice.domyos.commands.DCGetWorkoutResultCommand;
import com.appdevice.domyos.commands.DCSetDisplayZoneCommand;
import com.appdevice.domyos.commands.DCSetDisplayZoneOffCommand;
import com.appdevice.domyos.commands.DCSettingModeSetInfoValueCommand;
import com.appdevice.domyos.parameters.DCWorkoutModeSetInfoValueCommand;
import com.appdevice.domyos.parameters.bike.DCBikeDisplayZoneOffParameters;
import com.appdevice.domyos.parameters.bike.DCBikeDisplayZoneParameters;
import com.appdevice.domyos.parameters.bike.DCBikeSettingModeSetInfoParameters;
import com.appdevice.domyos.parameters.bike.DCBikeWorkoutModeSetInfoParameters;

import java.util.HashMap;

public class DCBike extends DCEquipment
{

	public interface DCBikeListener extends DCEquipmentListener
	{
	}

	;

	public static final int DCBikePressedButtonGeneral01 = 1;
	public static final int DCBikePressedButtonGeneral02 = 2;
	public static final int DCBikePressedButtonGeneral03 = 3;
	public static final int DCBikePressedButtonGeneral04 = 4;
	public static final int DCBikePressedButtonQuit = 5;
	public static final int DCBikePressedButtonStart = 6;
	public static final int DCBikePressedButtonProgram = 7;
	public static final int DCBikePressedButtonLoadPlus = 8;
	public static final int DCBikePressedButtonLoadMinus = 9;
	public static final int DCBikePressedButtonFanMinus = 10;
	public static final int DCBikePressedButtonFanPlus = 11;

	private DCBikeSportData mBikeSportData;

	DCBike()
	{
		mBikeSportData = new DCBikeSportData();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " - " + getName();
	}

	public void setListener(DCBikeListener bikeListener)
	{
		this.mListener = bikeListener;
	}

	@Override
	void resetEquipment()
	{
		super.resetEquipment();
		mBikeSportData = new DCBikeSportData();
	}

	@Override
	void getInfoValue(DCCommandCompletionBlockWithError failure)
	{
		DCGetInfoValueCommand getInfoValueCommand = new DCGetInfoValueCommand();

		getInfoValueCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				Integer watt = (Integer) info.get("watt");
				float currentSpeedKmPerHour = (Float) info.get("currentSpeedKmPerHour");
				Integer currentRPM = (Integer) info.get("currentRPM");
				int count = (Integer) info.get("count");
				int currentSessionCumulativeKCal = (Integer) info.get("currentSessionCumulativeKCal");
				float currentSessionCumulativeKM = (Float) info.get("currentSessionCumulativeKM");
				Integer torqueResistanceLevel = (Integer) info.get("torqueResistanceLevel");
				Integer errorNumber = (Integer) info.get("errorNumber");
				boolean tapOnEquipment = (Integer) info.get("tapOnEquipment") == 0 ? false : true;
				Integer analogHeartRate = (Integer) info.get("analogHeartRate");
				float currentSessionAverageSpeed = (Float) info.get("currentSessionAverageSpeed");
				Integer pressedButton = (Integer) info.get("pressedButton");
				Integer fanSpeedLevel = (Integer) info.get("fanSpeedLevel");
				Integer hotKeyStatus = (Integer) info.get("hotKeyStatus");

				mBikeSportData.setWatt(watt);
				mBikeSportData.setCurrentSpeedKmPerHour(currentSpeedKmPerHour);
				mBikeSportData.setCurrentRPM(currentRPM);
				mBikeSportData.setCount(count);
				mBikeSportData.setCurrentSessionCumulativeKCal(currentSessionCumulativeKCal);
				mBikeSportData.setCurrentSessionCumulativeKM(currentSessionCumulativeKM);
				mBikeSportData.setTorqueResistanceLevel(torqueResistanceLevel);
				mBikeSportData.setAnalogHeartRate(analogHeartRate);
				mBikeSportData.setCurrentSessionAverageSpeed(currentSessionAverageSpeed);
				setErrorNumber(errorNumber);
				setTabOnEquipment(tapOnEquipment);
				setPressedButton(pressedButton);
				setFanSpeedLevel(fanSpeedLevel);
				setHotKeyStatus(hotKeyStatus);
			}
		});

		getInfoValueCommand.setCompletionBlockWithError(failure);

		addCommand(getInfoValueCommand);
	}


	@Override
	public void setMode(int mode, DCCompletionBlock success, DCCompletionBlockWithError failure)
	{
		if (mMode == DCEquipmentModeWorkout && mMode != DCEquipmentModeWorkout)
		{
			if (mBikeSportData.getTorqueResistanceLevel() != 0)
			{
				if (failure != null)
				{
					failure.completedWithError(DCBike.this, new DCError(DCEquipment.DCErrorCodeChangeMode, "torqueResistanceLevel must to be zero"));
				}
			}
			else
			{
				mMode = mode;

				if (success != null)
				{
					success.completed(DCBike.this);
				}
			}

		}
		else
		{
			mMode = mode;

			if (success != null)
			{
				success.completed(DCBike.this);
			}
		}

	}

	public DCBikeSportData getSportData()
	{
		return mBikeSportData;
	}

	private interface DCGetVersionCompletionBlock
	{
		void completed(DCEquipment equipment, float consoleFirmwareVersion);
	}

	private void getVersion(final DCGetVersionCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCGetVersionCommand getVersionCommand = new DCGetVersionCommand();

		getVersionCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				Float consoleFirmwareVersion = (Float) info.get("consoleFirmwareVersion");

				if (success != null)
				{
					success.completed(DCBike.this, consoleFirmwareVersion);
				}

			}
		});

		getVersionCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCBike.this, error);
				}
			}
		});

		addCommand(getVersionCommand);
	}

	private interface DCGetSerialNumberCompletionBlock
	{
		void completed(DCEquipment equipment, String consoleFirmwareSerialNumber);
	}

	private void getSerialNumber(final DCGetSerialNumberCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCGetSerialNumberCommand getSerialNumberCommand = new DCGetSerialNumberCommand();

		getSerialNumberCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				String consoleFirmwareSerialNumber = (String) info.get("consoleFirmwareSerialNumber");

				if (success != null)
				{
					success.completed(DCBike.this, consoleFirmwareSerialNumber);
				}

			}
		});

		getSerialNumberCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCBike.this, error);
				}
			}
		});

		addCommand(getSerialNumberCommand);
	}

	private interface DCGetUsageHourCompletionBlock
	{
		void completed(DCEquipment equipment, int consoleUsageHour);
	}

	private void getUsageHour(final DCGetUsageHourCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCGetUsageHourCommand getUsageHourCommand = new DCGetUsageHourCommand();

		getUsageHourCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				Integer consoleUsageHour = (Integer) info.get("consoleUsageHour");

				if (success != null)
				{
					success.completed(DCBike.this, consoleUsageHour);
				}

			}
		});

		getUsageHourCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCBike.this, error);
				}
			}
		});

		addCommand(getUsageHourCommand);
	}

	private interface DCGetCumulativeKMCompletionBlock
	{
		void completed(DCEquipment equipment, int consoleCumulativeKM);
	}

	private void getCumulativeKM(final DCGetCumulativeKMCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCGetCumulativeKMCommand getCumulativeKMCommand = new DCGetCumulativeKMCommand();

		getCumulativeKMCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				Integer consoleCumulativeKM = (Integer) info.get("consoleCumulativeKM");

				if (success != null)
				{
					success.completed(DCBike.this, consoleCumulativeKM);
				}

			}
		});

		getCumulativeKMCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCBike.this, error);
				}
			}
		});

		addCommand(getCumulativeKMCommand);
	}

	public interface DCGetConsoleWorkoutResultCompletionBlock
	{
		void completed(@NonNull DCEquipment equipment, int user, int totalTimeInMinutes, float totalDistanceInKm, int totalCaloriesInKCal, float avgSpeed, int avgBpm);
	}

	/**
	 * @param user    the user number(id) in the console.
	 * @param success If success, api will run this block.
	 * @param failure If failed, api will run this block. Please refer to DCErrorCode
	 * @brief get workout result
	 * @details This is setting & workout mode.
	 */
	public void getConsoleWorkoutResult(int user, final @NonNull DCGetConsoleWorkoutResultCompletionBlock success, final @NonNull DCCompletionBlockWithError failure)
	{
		DCGetWorkoutResultCommand getWorkoutResultCommand = new DCGetWorkoutResultCommand();
		getWorkoutResultCommand.setUser(user);

		getWorkoutResultCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				if (success != null)
				{
					int user = ((Integer) info.get("User")).intValue();
					int totalTimeInMinutes = ((Integer) info.get("TotalTimeInMinutes")).intValue();
					float totalDistanceInKm = ((Float) info.get("TotalDistanceInKm")).floatValue();
					int totalCaloriesInKCal = ((Integer) info.get("TotalCaloriesInKCal")).intValue();
					float avgSpeed = ((Float) info.get("AvgSpeed")).floatValue();
					int avgBpm = ((Integer) info.get("AvgBpm")).intValue();

					success.completed(DCBike.this, user, totalTimeInMinutes, totalDistanceInKm, totalCaloriesInKCal, avgSpeed, avgBpm);
				}

			}
		});

		getWorkoutResultCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCBike.this, error);
				}
			}
		});

		addCommand(getWorkoutResultCommand);
	}

	public interface DCBikeGetEquipmentInfoCompletionBlock
	{
		void completed(DCEquipment equipment, DCEquipmentInfo consoleEquipmentInfo);
	}

	public void getEquipmentInfo(final DCBikeGetEquipmentInfoCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		getVersion(new DCGetVersionCompletionBlock()
		{

			@Override
			public void completed(DCEquipment equipment, float consoleFirmwareVersion)
			{
				final DCEquipmentInfo consoleEquipmentInfo = new DCEquipmentInfo();
				consoleEquipmentInfo.setFirmwareVersion(consoleFirmwareVersion);

				DCBike.this.getSerialNumber(new DCGetSerialNumberCompletionBlock()
				{

					@Override
					public void completed(DCEquipment equipment, String consoleFirmwareSerialNumber)
					{
						consoleEquipmentInfo.setSerialNumber(consoleFirmwareSerialNumber);

						DCBike.this.getUsageHour(new DCGetUsageHourCompletionBlock()
						{

							@Override
							public void completed(DCEquipment equipment, int consoleUsageHour)
							{
								consoleEquipmentInfo.setUsageHour(consoleUsageHour);

								DCBike.this.getCumulativeKM(new DCGetCumulativeKMCompletionBlock()
								{

									@Override
									public void completed(DCEquipment equipment, int consoleCumulativeKM)
									{
										consoleEquipmentInfo.setCumulativeKM(consoleCumulativeKM);

										if (success != null)
										{
											success.completed(DCBike.this, consoleEquipmentInfo);
										}

									}
								}, failure);

							}
						}, failure);

					}
				}, failure);

			}
		}, failure);
	}

	public void setSettingModeInfoValue(DCBikeSettingModeSetInfoParameters parameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCSettingModeSetInfoValueCommand settingModeSetInfoValueCommand = new DCSettingModeSetInfoValueCommand();
		if (parameters != null)
		{
			settingModeSetInfoValueCommand.mSetInfoParameters = parameters;
		}

		settingModeSetInfoValueCommand.setCompletionBlock(new DCCommandCompletionBlock()
		{

			@Override
			public void completed(DCCommand command)
			{
				if (success != null)
				{
					success.completed(DCBike.this);
				}
			}
		});

		settingModeSetInfoValueCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCBike.this, error);
				}
			}
		});

		addCommand(settingModeSetInfoValueCommand);
	}

	public void setWorkoutModeInfoValue(DCBikeWorkoutModeSetInfoParameters parameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCWorkoutModeSetInfoValueCommand wrkoutModeSetInfoValueCommand = new DCWorkoutModeSetInfoValueCommand();
		if (parameters != null)
		{
			wrkoutModeSetInfoValueCommand.mSetInfoParameters = parameters;
		}

		wrkoutModeSetInfoValueCommand.setCompletionBlock(new DCCommandCompletionBlock()
		{

			@Override
			public void completed(DCCommand command)
			{
				if (success != null)
				{
					success.completed(DCBike.this);
				}
			}
		});

		wrkoutModeSetInfoValueCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCBike.this, error);
				}
			}
		});

		addCommand(wrkoutModeSetInfoValueCommand);
	}

	public void setDisplayZones(DCBikeDisplayZoneParameters displayZoneParameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCSetDisplayZoneCommand setDisplayZoneCommand = new DCSetDisplayZoneCommand();

		setDisplayZoneCommand.displayZone1Parameter = displayZoneParameters.mDisplayZone1Parameter;
		setDisplayZoneCommand.displayZone2Parameter = displayZoneParameters.mDisplayZone2Parameter;
		setDisplayZoneCommand.displayZone3Parameter = displayZoneParameters.mDisplayZone3Parameter;
		setDisplayZoneCommand.displayZone4Parameter = displayZoneParameters.mDisplayZone4Parameter;
		setDisplayZoneCommand.displayZone5Parameter = displayZoneParameters.mDisplayZone5Parameter;
		setDisplayZoneCommand.displayZone6Parameter = displayZoneParameters.mDisplayZone6Parameter;

		setDisplayZoneCommand.setCompletionBlock(new DCCommandCompletionBlock()
		{

			@Override
			public void completed(DCCommand command)
			{
				if (success != null)
				{
					success.completed(DCBike.this);
				}
			}
		});

		setDisplayZoneCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCBike.this, error);
				}
			}
		});

		addCommand(setDisplayZoneCommand);
	}

	public void setDisplayZoneOff(DCBikeDisplayZoneOffParameters displayZoneOffParameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCSetDisplayZoneOffCommand setDisplayZoneOffCommand = new DCSetDisplayZoneOffCommand();

		setDisplayZoneOffCommand.mDisplayZoneOffParameters = displayZoneOffParameters;

		setDisplayZoneOffCommand.setCompletionBlock(new DCCommandCompletionBlock()
		{

			@Override
			public void completed(DCCommand command)
			{
				if (success != null)
				{
					success.completed(DCBike.this);
				}
			}
		});

		setDisplayZoneOffCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCBike.this, error);
				}
			}
		});

		addCommand(setDisplayZoneOffCommand);
	}

}

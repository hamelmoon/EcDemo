package com.appdevice.domyos;


import androidx.annotation.NonNull;

import com.appdevice.domyos.commands.DCGetCumulativeKMCommand;
import com.appdevice.domyos.commands.DCGetSerialNumberCommand;
import com.appdevice.domyos.commands.DCGetUsageHourCommand;
import com.appdevice.domyos.commands.DCGetVersionCommand;
import com.appdevice.domyos.commands.DCGetWorkoutResultCommand;
import com.appdevice.domyos.commands.DCSetDisplayZoneCommand;
import com.appdevice.domyos.commands.DCSetDisplayZoneOffCommand;
import com.appdevice.domyos.commands.DCSetDisplayZoneOffSecondAreaCommand;
import com.appdevice.domyos.commands.DCSetDisplayZoneSecondAreaCommand;
import com.appdevice.domyos.commands.DCSettingModeSetInfoValueCommand;
import com.appdevice.domyos.commands.rower.DCRowerGetInfoValueCommand;
import com.appdevice.domyos.parameters.DCDisplayZoneOffParameters;
import com.appdevice.domyos.parameters.DCDisplayZoneParameters;
import com.appdevice.domyos.parameters.DCEquipmentDisplayZoneOffSecondAreaParameters;
import com.appdevice.domyos.parameters.DCEquipmentDisplayZoneSecondAreaParameters;
import com.appdevice.domyos.parameters.DCWorkoutModeSetInfoValueCommand;
import com.appdevice.domyos.parameters.rower.DCRowerSettingModeSetInfoParameters;
import com.appdevice.domyos.parameters.rower.DCRowerWorkoutModeSetInfoParameters;

import java.util.HashMap;

/**
 * Created by apple on 2017/5/1.
 */

public class DCRower extends DCEquipment
{
	public interface DCRowerListener extends DCEquipmentListener
	{
	}

	;

	public static final int DCRowerPressedButtonGeneral01 = 1;
	public static final int DCRowerPressedButtonGeneral02 = 2;
	public static final int DCRowerPressedButtonGeneral03 = 3;
	public static final int DCRowerPressedButtonGeneral04 = 4;
	public static final int DCRowerPressedButtonQuit = 5;
	public static final int DCRowerPressedButtonStart = 6;
	public static final int DCRowerPressedButtonProgram = 7;
	public static final int DCRowerPressedButtonLoadPlus = 8;
	public static final int DCRowerPressedButtonLoadMinus = 9;

	private DCRowerSportData mRowerSportData;

	DCRower()
	{
		mRowerSportData = new DCRowerSportData();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " - " + getName();
	}

	public void setListener(DCRowerListener listener)
	{
		this.mListener = listener;
	}

	@Override
	void resetEquipment()
	{
		super.resetEquipment();
		mRowerSportData = new DCRowerSportData();
	}

	@Override
	void getInfoValue(DCCommandCompletionBlockWithError failure)
	{
		DCRowerGetInfoValueCommand getInfoValueCommand = new DCRowerGetInfoValueCommand();

		getInfoValueCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				Integer watt = (Integer) info.get("watt");
				int timePer500mInSeconds = (Integer) info.get("timePer500mInSeconds");
				Integer currentSPM = (Integer) info.get("currentSPM");
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

				mRowerSportData.setWatt(watt);
				mRowerSportData.setTimePer500mInSeconds(timePer500mInSeconds);
				mRowerSportData.setCurrentSPM(currentSPM);
				mRowerSportData.setCount(count);
				mRowerSportData.setCurrentSessionCumulativeKCal(currentSessionCumulativeKCal);
				mRowerSportData.setCurrentSessionCumulativeKM(currentSessionCumulativeKM);
				mRowerSportData.setTorqueResistanceLevel(torqueResistanceLevel);
				mRowerSportData.setAnalogHeartRate(analogHeartRate);
				mRowerSportData.setCurrentSessionAverageSpeed(currentSessionAverageSpeed);
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
			if (mRowerSportData.getTorqueResistanceLevel() != 0)
			{
				if (failure != null)
				{
					failure.completedWithError(DCRower.this, new DCError(DCEquipment.DCErrorCodeChangeMode, "torqueResistanceLevel must to be zero"));
				}
			}
			else
			{
				mMode = mode;

				if (success != null)
				{
					success.completed(DCRower.this);
				}
			}

		}
		else
		{
			mMode = mode;

			if (success != null)
			{
				success.completed(DCRower.this);
			}
		}

	}

	public DCRowerSportData getSportData()
	{
		return mRowerSportData;
	}

	private interface DCGetVersionCompletionBlock
	{
		void completed(DCEquipment equipment, float consoleFirmwareVersion);
	}

	private void getVersion(final DCRower.DCGetVersionCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCRower.this, consoleFirmwareVersion);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(getVersionCommand);
	}

	private interface DCGetSerialNumberCompletionBlock
	{
		void completed(DCEquipment equipment, String consoleFirmwareSerialNumber);
	}

	private void getSerialNumber(final DCRower.DCGetSerialNumberCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCRower.this, consoleFirmwareSerialNumber);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(getSerialNumberCommand);
	}

	private interface DCGetUsageHourCompletionBlock
	{
		void completed(DCEquipment equipment, int consoleUsageHour);
	}

	private void getUsageHour(final DCRower.DCGetUsageHourCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCRower.this, consoleUsageHour);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(getUsageHourCommand);
	}

	private interface DCGetCumulativeKMCompletionBlock
	{
		void completed(DCEquipment equipment, int consoleCumulativeKM);
	}

	private void getCumulativeKM(final DCRower.DCGetCumulativeKMCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCRower.this, consoleCumulativeKM);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(getCumulativeKMCommand);
	}
	public interface DCGetConsoleWorkoutResultCompletionBlock
	{
		void completed(@NonNull DCEquipment equipment, int user, int totalTimeInMinutes, float totalDistanceInKm, int totalCaloriesInKCal, int avgSpm, int avgBpm);
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
					int avgSpm = ((Integer) info.get("AvgSpm")).intValue();
					int avgBpm = ((Integer) info.get("AvgBpm")).intValue();

					success.completed(DCRower.this, user, totalTimeInMinutes, totalDistanceInKm, totalCaloriesInKCal, avgSpm, avgBpm);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(getWorkoutResultCommand);
	}
	public interface DCRowerGetEquipmentInfoCompletionBlock
	{
		void completed(DCEquipment equipment, DCEquipmentInfo consoleEquipmentInfo);
	}

	public void getEquipmentInfo(final DCRower.DCRowerGetEquipmentInfoCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		getVersion(new DCRower.DCGetVersionCompletionBlock()
		{

			@Override
			public void completed(DCEquipment equipment, float consoleFirmwareVersion)
			{
				final DCEquipmentInfo consoleEquipmentInfo = new DCEquipmentInfo();
				consoleEquipmentInfo.setFirmwareVersion(consoleFirmwareVersion);

				DCRower.this.getSerialNumber(new DCRower.DCGetSerialNumberCompletionBlock()
				{

					@Override
					public void completed(DCEquipment equipment, String consoleFirmwareSerialNumber)
					{
						consoleEquipmentInfo.setSerialNumber(consoleFirmwareSerialNumber);

						DCRower.this.getUsageHour(new DCRower.DCGetUsageHourCompletionBlock()
						{

							@Override
							public void completed(DCEquipment equipment, int consoleUsageHour)
							{
								consoleEquipmentInfo.setUsageHour(consoleUsageHour);

								DCRower.this.getCumulativeKM(new DCRower.DCGetCumulativeKMCompletionBlock()
								{

									@Override
									public void completed(DCEquipment equipment, int consoleCumulativeKM)
									{
										consoleEquipmentInfo.setCumulativeKM(consoleCumulativeKM);

										if (success != null)
										{
											success.completed(DCRower.this, consoleEquipmentInfo);
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

	public void setSettingModeInfoValue(DCRowerSettingModeSetInfoParameters parameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCRower.this);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(settingModeSetInfoValueCommand);
	}

	public void setWorkoutModeInfoValue(DCRowerWorkoutModeSetInfoParameters parameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCRower.this);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(wrkoutModeSetInfoValueCommand);
	}

	public void setDisplayZones(DCDisplayZoneParameters displayZoneParameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCRower.this);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(setDisplayZoneCommand);
	}

	public void setDisplayZoneOff(DCDisplayZoneOffParameters displayZoneOffParameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCRower.this);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(setDisplayZoneOffCommand);
	}

	public void setDisplayZonesSecondArea(DCEquipmentDisplayZoneSecondAreaParameters displayZoneParameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCSetDisplayZoneSecondAreaCommand setDisplayZoneCommand = new DCSetDisplayZoneSecondAreaCommand();

		setDisplayZoneCommand.displayZone7Parameter = displayZoneParameters.displayZone7Parameter;
		setDisplayZoneCommand.displayZone8Parameter = displayZoneParameters.displayZone8Parameter;
		setDisplayZoneCommand.displayZone9Parameter = displayZoneParameters.displayZone9Parameter;
		setDisplayZoneCommand.displayZone10Parameter = displayZoneParameters.displayZone10Parameter;
		setDisplayZoneCommand.displayZone11Parameter = displayZoneParameters.displayZone11Parameter;
		setDisplayZoneCommand.displayZone12Parameter = displayZoneParameters.displayZone12Parameter;

		setDisplayZoneCommand.setCompletionBlock(new DCCommandCompletionBlock()
		{

			@Override
			public void completed(DCCommand command)
			{
				if (success != null)
				{
					success.completed(DCRower.this);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(setDisplayZoneCommand);
	}

	public void setDisplayZoneSecondAreaOff(DCEquipmentDisplayZoneOffSecondAreaParameters displayZoneOffParameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCSetDisplayZoneOffSecondAreaCommand setDisplayZoneOffCommand = new DCSetDisplayZoneOffSecondAreaCommand();

		setDisplayZoneOffCommand.displayZone7OffParameter = displayZoneOffParameters.getDisplayZone7Off();
		setDisplayZoneOffCommand.displayZone8OffParameter = displayZoneOffParameters.getDisplayZone8Off();
		setDisplayZoneOffCommand.displayZone9OffParameter = displayZoneOffParameters.getDisplayZone9Off();
		setDisplayZoneOffCommand.displayZone10OffParameter = displayZoneOffParameters.getDisplayZone10Off();
		setDisplayZoneOffCommand.displayZone11OffParameter = displayZoneOffParameters.getDisplayZone11Off();
		setDisplayZoneOffCommand.displayZone12OffParameter = displayZoneOffParameters.getDisplayZone12Off();

		setDisplayZoneOffCommand.setCompletionBlock(new DCCommandCompletionBlock()
		{

			@Override
			public void completed(DCCommand command)
			{
				if (success != null)
				{
					success.completed(DCRower.this);
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
					failure.completedWithError(DCRower.this, error);
				}
			}
		});

		addCommand(setDisplayZoneOffCommand);
	}
}

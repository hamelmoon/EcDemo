package com.appdevice.domyos;


import androidx.annotation.NonNull;

import com.appdevice.domyos.commands.DCGetWorkoutResultCommand;
import com.appdevice.domyos.commands.DCSetDisplayZoneCommand;
import com.appdevice.domyos.commands.DCSetDisplayZoneOffCommand;
import com.appdevice.domyos.commands.DCSettingModeSetInfoValueCommand;
import com.appdevice.domyos.commands.treadmill.DCAskMotorInclineCalibrationCommand;
import com.appdevice.domyos.commands.treadmill.DCGetTreadmillDataCommand;
import com.appdevice.domyos.commands.treadmill.DCGetTreadmillInclineVoltageCommand;
import com.appdevice.domyos.commands.treadmill.DCSettingModeGetTreadmillDataCommand;
import com.appdevice.domyos.commands.treadmill.DCTreadmillGetCumulativeKMCommand;
import com.appdevice.domyos.commands.treadmill.DCTreadmillGetInfoValueCommand;
import com.appdevice.domyos.commands.treadmill.DCTreadmillGetSerialNumberCommand;
import com.appdevice.domyos.commands.treadmill.DCTreadmillGetUsageHourCommand;
import com.appdevice.domyos.commands.treadmill.DCTreadmillGetVersionCommand;
import com.appdevice.domyos.commands.treadmill.DCWorkoutModeGetTreadmillDataCommand;
import com.appdevice.domyos.parameters.DCWorkoutModeSetInfoValueCommand;
import com.appdevice.domyos.parameters.treadmill.DCTreadmillDisplayZoneOffParameters;
import com.appdevice.domyos.parameters.treadmill.DCTreadmillDisplayZoneParameters;
import com.appdevice.domyos.parameters.treadmill.DCTreadmillSettingModeSetInfoParameters;
import com.appdevice.domyos.parameters.treadmill.DCTreadmillWorkoutModeSetInfoParameters;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class DCTreadmill extends DCEquipment
{

	public interface DCTreadmillListener extends DCEquipmentListener
	{

		void treadmillOnSafetyMotorKeyChanged(DCEquipment equipment, boolean safetyMotorKey);

	}

	public static final int DCTreadmillPressedButtonGeneral01 = 1;
	public static final int DCTreadmillPressedButtonGeneral02 = 2;
	public static final int DCTreadmillPressedButtonGeneral03 = 3;
	public static final int DCTreadmillPressedButtonGeneral04 = 4;
	public static final int DCTreadmillPressedButtonProgram = 5;
	public static final int DCTreadmillPressedButtonStartPause = 6;
	public static final int DCTreadmillPressedButtonStop = 7;
	public static final int DCTreadmillPressedButtonSpeedPlus = 8;
	public static final int DCTreadmillPressedButtonSpeedMinus = 9;
	public static final int DCTreadmillPressedButtonFanMinus = 10;
	public static final int DCTreadmillPressedButtonFanPlus = 11;
	public static final int DCTreadmillPressedButtonInclinePlus = 12;
	public static final int DCTreadmillPressedButtonInclineMinu = 13;
	public static final int DCTreadmillPressedButtonSpeed1 = 14;
	public static final int DCTreadmillPressedButtonSpeed2 = 15;
	public static final int DCTreadmillPressedButtonSpeed3 = 16;
	public static final int DCTreadmillPressedButtonSpeed4 = 17;
	public static final int DCTreadmillPressedButtonIncline1 = 18;
	public static final int DCTreadmillPressedButtonIncline2 = 19;
	public static final int DCTreadmillPressedButtonIncline3 = 20;
	public static final int DCTreadmillPressedButtonIncline4 = 21;
	public static final int DCTreadmillPressedButtonSpeed5 = 22;
	public static final int DCTreadmillPressedButtonSpeed6 = 23;
	public static final int DCTreadmillPressedButtonSpeed7 = 24;
	public static final int DCTreadmillPressedButtonSpeed8 = 25;
	public static final int DCTreadmillPressedButtonIncline5 = 26;
	public static final int DCTreadmillPressedButtonIncline6 = 27;
	public static final int DCTreadmillPressedButtonIncline7 = 28;
	public static final int DCTreadmillPressedButtonIncline8 = 29;


	public static final int DCTreadmillCalibrationStatusNoCalibrate = 0;
	public static final int DCTreadmillCalibrationStatusInProgess = 1;

	private DCTreadmillSportData mTreadmillSportData;
	private boolean mCalibrationInProgress;
	private boolean mSafetyMotorKey;

	DCTreadmill()
	{
		mTreadmillSportData = new DCTreadmillSportData();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " - " + getName();
	}

	public void setListener(DCTreadmillListener treadmillListener)
	{
		this.mListener = treadmillListener;
	}

	public boolean getSafetyMotorKey()
	{
		return mSafetyMotorKey;
	}

	private void setSafetyMotorKey(boolean safetyMotorKey)
	{
		if (mSafetyMotorKey != safetyMotorKey)
		{
			mSafetyMotorKey = safetyMotorKey;

			if (mListener != null)
			{
				((DCTreadmillListener) mListener).treadmillOnSafetyMotorKeyChanged(this, safetyMotorKey);
			}
		}
	}

	@Override
	void resetEquipment()
	{
		super.resetEquipment();
		mTreadmillSportData = new DCTreadmillSportData();
		mSafetyMotorKey = false;
		mCalibrationInProgress = false;
	}

	@Override
	boolean canAddCommand(DCCommand command)
	{
		if (!super.canAddCommand(command))
		{
			return false;
		}
		if (mCalibrationInProgress && !(command instanceof DCGetTreadmillDataCommand))
		{
			DCError error = new DCError(DCErrorCodeRequestDenied, "Calibration in progress...");
			command.runCompletionBlockWithError(error);

			return false;
		}
		return true;
	}

	@Override
	void getInfoValue(DCCommandCompletionBlockWithError failure)
	{
		DCTreadmillGetInfoValueCommand getInfoValueCommand = new DCTreadmillGetInfoValueCommand();

		getInfoValueCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				float targetInclinePercentage = (Float) info.get("targetInclinePercentage");
				boolean safetyMotorKeyPlugIn = (Integer) info.get("safetyMotorKeyPlugIn") == 0 ? false : true;
				float currentSpeedKmPerHour = (Float) info.get("currentSpeedKmPerHour");
				int count = (Integer) info.get("count");
				Integer currentSessionCumulativeKCal = (Integer) info.get("currentSessionCumulativeKCal");
				float currentSessionCumulativeKM = (Float) info.get("currentSessionCumulativeKM");
				Integer errorNumber = (Integer) info.get("errorNumber");
				boolean tapOnEquipment = (Integer) info.get("tapOnEquipment") == 0 ? false : true;
				Integer analogHeartRate = (Integer) info.get("analogHeartRate");
				float currentSessionAverageSpeed = (Float) info.get("currentSessionAverageSpeed");
				Integer pressedButton = (Integer) info.get("pressedButton");
				Integer fanSpeedLevel = (Integer) info.get("fanSpeedLevel");
				Integer hotKeyStatus = (Integer) info.get("hotKeyStatus");

				mTreadmillSportData.setTargetInclinePercentage(targetInclinePercentage);
				setSafetyMotorKey(safetyMotorKeyPlugIn);
				mTreadmillSportData.setCurrentSpeedKmPerHour(currentSpeedKmPerHour);
				mTreadmillSportData.setCount(count);
				mTreadmillSportData.setCurrentSessionCumulativeKCal(currentSessionCumulativeKCal);
				mTreadmillSportData.setCurrentSessionCumulativeKM(currentSessionCumulativeKM);
				mTreadmillSportData.setAnalogHeartRate(analogHeartRate);
				mTreadmillSportData.setCurrentSessionAverageSpeed(currentSessionAverageSpeed);
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
		if (mMode == DCEquipmentModeWorkout && mode != DCEquipmentModeWorkout)
		{
			if (mTreadmillSportData.getCurrentSpeedKmPerHour() != 0)
			{
				DCError error = new DCError(DCErrorCodeChangeMode, "currentSpeedKmPerHour must to be zero");
				if (failure != null)
				{
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
			else
			{
				mMode = mode;
				if (success != null)
				{
					success.completed(DCTreadmill.this);
				}
			}
		}
		else
		{
			mMode = mode;

			if (success != null)
			{
				success.completed(DCTreadmill.this);
			}
		}

	}

	public DCTreadmillSportData getSportData()
	{
		return mTreadmillSportData;
	}

	private interface DCTreadmillGetVersionCompletionBlock
	{
		void completed(DCTreadmill treadmill, float consoleFirmwareVersion, float mcbFirmwareVersion);
	}

	private void getVersion(final DCTreadmillGetVersionCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCTreadmillGetVersionCommand getVersionCommand = new DCTreadmillGetVersionCommand();

		getVersionCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				Float consoleFirmwareVersion = (Float) info.get("consoleFirmwareVersion");
				Float mcbFirmwareVersion = (Float) info.get("mcbFirmwareVersion");

				if (success != null)
				{
					success.completed(DCTreadmill.this, consoleFirmwareVersion, mcbFirmwareVersion);
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
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(getVersionCommand);
	}

	private interface DCTreadmillGetSerialNumberCompletionBlock
	{
		void completed(DCTreadmill treadmill, String consoleFirmwareSerialNumber, String mcbFirmwareSerialNumber);
	}

	private void getSerialNumber(final DCTreadmillGetSerialNumberCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCTreadmillGetSerialNumberCommand getSerialNumberCommand = new DCTreadmillGetSerialNumberCommand();

		getSerialNumberCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				String consoleFirmwareSerialNumber = (String) info.get("consoleFirmwareSerialNumber");
				String mcbFirmwareSerialNumber = (String) info.get("mcbFirmwareSerialNumber");

				if (success != null)
				{
					success.completed(DCTreadmill.this, consoleFirmwareSerialNumber, mcbFirmwareSerialNumber);
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
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(getSerialNumberCommand);
	}

	private interface DCTreadmillGetUsageHourCompletionBlock
	{
		void completed(DCTreadmill treadmill, int consoleUsageHour, int mcbUsageHour);
	}

	private void getUsageHour(final DCTreadmillGetUsageHourCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCTreadmillGetUsageHourCommand getUsageHourCommand = new DCTreadmillGetUsageHourCommand();

		getUsageHourCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				Integer consoleUsageHour = (Integer) info.get("consoleUsageHour");
				Integer mcbUsageHour = (Integer) info.get("mcbUsageHour");

				if (success != null)
				{
					success.completed(DCTreadmill.this, consoleUsageHour, mcbUsageHour);
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
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(getUsageHourCommand);
	}

	private interface DCTreadmillGetCumulativeKMCompletionBlock
	{
		void completed(DCTreadmill treadmill, int consoleCumulativeKM, int mcbCumulativeKM);
	}

	private void getCumulativeKM(final DCTreadmillGetCumulativeKMCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCTreadmillGetCumulativeKMCommand getCumulativeKMCommand = new DCTreadmillGetCumulativeKMCommand();

		getCumulativeKMCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				Integer consoleCumulativeKM = (Integer) info.get("consoleCumulativeKM");
				Integer mcbCumulativeKM = (Integer) info.get("mcbCumulativeKM");

				if (success != null)
				{
					success.completed(DCTreadmill.this, consoleCumulativeKM, mcbCumulativeKM);
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
					failure.completedWithError(DCTreadmill.this, error);
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

					success.completed(DCTreadmill.this, user, totalTimeInMinutes, totalDistanceInKm, totalCaloriesInKCal, avgSpeed, avgBpm);
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
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(getWorkoutResultCommand);
	}

	public void setSettingModeInfoValue(DCTreadmillSettingModeSetInfoParameters parameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCTreadmill.this);
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
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(settingModeSetInfoValueCommand);
	}

	public void setWorkoutModeInfoValue(DCTreadmillWorkoutModeSetInfoParameters parameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCTreadmill.this);
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
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(wrkoutModeSetInfoValueCommand);
	}

	public void setDisplayZones(DCTreadmillDisplayZoneParameters displayZoneParameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCTreadmill.this);
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
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(setDisplayZoneCommand);
	}

	public void setDisplayZoneOff(DCTreadmillDisplayZoneOffParameters displayZoneOffParameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure)
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
					success.completed(DCTreadmill.this);
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
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(setDisplayZoneOffCommand);
	}

	public interface DCTreadmillGetEquipmentInfoCompletionBlock
	{
		void completed(DCTreadmill treadmill, DCEquipmentInfo consoleEquipmentInfo, DCEquipmentInfo mcbEquipmentInfo);
	}

	public void getEquipmentInfo(final DCTreadmillGetEquipmentInfoCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		getVersion(new DCTreadmillGetVersionCompletionBlock()
		{

			@Override
			public void completed(DCTreadmill treadmill, float consoleFirmwareVersion, float mcbFirmwareVersion)
			{
				final DCEquipmentInfo consoleEquipmentInfo = new DCEquipmentInfo();
				consoleEquipmentInfo.setFirmwareVersion(consoleFirmwareVersion);

				final DCEquipmentInfo mcbEquipmentInfo = new DCEquipmentInfo();
				mcbEquipmentInfo.setFirmwareVersion(mcbFirmwareVersion);

				DCTreadmill.this.getSerialNumber(new DCTreadmillGetSerialNumberCompletionBlock()
				{

					@Override
					public void completed(DCTreadmill treadmill, String consoleFirmwareSerialNumber, String mcbFirmwareSerialNumber)
					{
						consoleEquipmentInfo.setSerialNumber(consoleFirmwareSerialNumber);
						mcbEquipmentInfo.setSerialNumber(mcbFirmwareSerialNumber);

						DCTreadmill.this.getUsageHour(new DCTreadmillGetUsageHourCompletionBlock()
						{

							@Override
							public void completed(DCTreadmill treadmill, int consoleUsageHour, int mcbUsageHour)
							{
								consoleEquipmentInfo.setUsageHour(consoleUsageHour);
								mcbEquipmentInfo.setUsageHour(mcbUsageHour);

								DCTreadmill.this.getCumulativeKM(new DCTreadmillGetCumulativeKMCompletionBlock()
								{

									@Override
									public void completed(DCTreadmill treadmill, int consoleCumulativeKM, int mcbCumulativeKM)
									{
										consoleEquipmentInfo.setCumulativeKM(consoleCumulativeKM);
										mcbEquipmentInfo.setCumulativeKM(mcbCumulativeKM);

										if (success != null)
										{
											success.completed(DCTreadmill.this, consoleEquipmentInfo, mcbEquipmentInfo);
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

	public void askMotorInclineCalibration(final DCCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCAskMotorInclineCalibrationCommand askMotorInclineCalibrationCommand = new DCAskMotorInclineCalibrationCommand();
		askMotorInclineCalibrationCommand.setCompletionBlock(new DCCommandCompletionBlock()
		{

			@Override
			public void completed(DCCommand command)
			{
				mCalibrationInProgress = true;

				final Timer timer = new Timer();
				timer.schedule(new TimerTask()
				{

					@Override
					public void run()
					{
						DCGetTreadmillDataCommand getTreadmillDataCommand = new DCGetTreadmillDataCommand();

						getTreadmillDataCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
						{

							@Override
							public void completed(DCCommand command, HashMap<String, Object> info)
							{
								Integer calibrationStatus = (Integer) info.get("calibrationStatus");
								if (calibrationStatus == DCTreadmillCalibrationStatusNoCalibrate)
								{
									mCalibrationInProgress = false;
									timer.cancel();
									if (success != null)
									{
										success.completed(DCTreadmill.this);
									}
								}
							}
						});

						getTreadmillDataCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
						{

							@Override
							public void completed(DCCommand command, DCError error)
							{
								timer.cancel();
								if (failure != null)
								{
									failure.completedWithError(DCTreadmill.this, error);
								}
							}
						});

						DCTreadmill.this.addCommand(getTreadmillDataCommand);
					}
				}, 1000, 2000);
			}
		});

		askMotorInclineCalibrationCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCTreadmill.this, error);
				}

			}
		});

		addCommand(askMotorInclineCalibrationCommand);
	}

	public interface DCTreadmillGetSettingModeTreadmillDataCompletionBlock
	{
		void completed(DCTreadmill treadmill, int calibrationStatus, float motorSpeed, float motorVoltage, float motorCurrent, float inclineCalibrationMax, float inclineCalibrationMin, int temperature);
	}

	public void getSettingModeTreadmillData(final DCTreadmillGetSettingModeTreadmillDataCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCSettingModeGetTreadmillDataCommand settingModeGetTreadmillDataCommand = new DCSettingModeGetTreadmillDataCommand();

		settingModeGetTreadmillDataCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				Integer calibrationStatus = (Integer) info.get("calibrationStatus");
				Float motorSpeed = (Float) info.get("motorSpeed");
				Float motorVoltage = (Float) info.get("motorVoltage");
				Float motorCurrent = (Float) info.get("motorCurrent");
				Float inclineCalibrationMax = (Float) info.get("inclineCalibrationMax");
				Float inclineCalibrationMin = (Float) info.get("inclineCalibrationMin");
				Integer temperature = (Integer) info.get("temperature");
				if (success != null)
				{
					success.completed(DCTreadmill.this, calibrationStatus, motorSpeed, motorVoltage, motorCurrent, inclineCalibrationMax, inclineCalibrationMin, temperature);
				}
			}
		});

		settingModeGetTreadmillDataCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(settingModeGetTreadmillDataCommand);
	}

	public interface DCTreadmillGetWorkoutModeTreadmillDataCompletionBlock
	{
		void completed(DCTreadmill treadmill, float motorSpeed, float motorVoltage, float motorCurrent, float inclineCalibrationMax, float inclineCalibrationMin, int temperature);
	}

	public void getWorkoutModeTreadmillData(final DCTreadmillGetWorkoutModeTreadmillDataCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCWorkoutModeGetTreadmillDataCommand workoutModeGetTreadmillDataCommand = new DCWorkoutModeGetTreadmillDataCommand();

		workoutModeGetTreadmillDataCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{

			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				Float motorSpeed = (Float) info.get("motorSpeed");
				Float motorVoltage = (Float) info.get("motorVoltage");
				Float motorCurrent = (Float) info.get("motorCurrent");
				Float inclineCalibrationMax = (Float) info.get("inclineCalibrationMax");
				Float inclineCalibrationMin = (Float) info.get("inclineCalibrationMin");
				Integer temperature = (Integer) info.get("temperature");
				if (success != null)
				{
					success.completed(DCTreadmill.this, motorSpeed, motorVoltage, motorCurrent, inclineCalibrationMax, inclineCalibrationMin, temperature);
				}
			}
		});

		workoutModeGetTreadmillDataCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(workoutModeGetTreadmillDataCommand);
	}


	public interface DCTreadmillGetTreadmillInclineVoltageCompletionBlock
	{
		void completed(DCTreadmill treadmill, float inclineVoltage);
	}

	public void getTreadmillInclineVoltage(final DCTreadmillGetTreadmillInclineVoltageCompletionBlock success, final DCCompletionBlockWithError failure)
	{
		DCGetTreadmillInclineVoltageCommand getTreadmillInclineVoltageCommand = new DCGetTreadmillInclineVoltageCommand();

		getTreadmillInclineVoltageCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo()
		{
			@Override
			public void completed(DCCommand command, HashMap<String, Object> info)
			{
				if (success != null)
				{
					float inclineVoltage = (Float) info.get("inclineVoltage");
					success.completed(DCTreadmill.this, inclineVoltage);
				}

			}
		});

		getTreadmillInclineVoltageCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError()
		{

			@Override
			public void completed(DCCommand command, DCError error)
			{
				if (failure != null)
				{
					failure.completedWithError(DCTreadmill.this, error);
				}
			}
		});

		addCommand(getTreadmillInclineVoltageCommand);
	}
}

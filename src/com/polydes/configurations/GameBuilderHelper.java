package com.polydes.configurations;

import java.util.Optional;

import javax.swing.SwingWorker;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.log4j.Logger;

import stencyl.sw.SW;
import stencyl.sw.app.tasks.buildgame.GameBuilder;
import stencyl.sw.prefs.runconfigs.BuildConfig;

//XXX: This should be easily available through extension API
public class GameBuilderHelper
{
	private static final Logger log = Logger.getLogger(GameBuilderHelper.class);
	
	public static GameBuilder getRunningBuilder()
	{
		Optional<SwingWorker<Integer, Void>> builderWorker =
			SW.get().getTaskManager().getRunningTask
			(
				task -> task.getClass().getEnclosingClass() == GameBuilder.class
			);
		
		if(builderWorker.isEmpty())
		{
			log.info("No running GameBuilder.");
			return null;
		}
		
		Object o;
		try
		{
			o = FieldUtils.readDeclaredField(builderWorker.get(), "this$0", true);
		}
		catch (IllegalAccessException e)
		{
			log.error(e.getMessage(), e);
			return null;
		}
		
		if(o instanceof GameBuilder)
		{
			return (GameBuilder) o;
		}
		
		log.error("Couldn't access GameBuilder object.");
		return null;
	}
	
	public static void appendCommandLineArguments(GameBuilder builder, String[] arguments)
	{
		Object o = null;
		try
		{
			o = FieldUtils.readField(builder, "extra", true);
		}
		catch (IllegalAccessException e)
		{
			log.error(e.getMessage(), e);
		}
		
		if(o instanceof String[])
		{
			String[] existingExtra = (String[]) o;
			String[] newExtra = ArrayUtils.addAll(existingExtra, arguments);
			try
			{
				FieldUtils.writeField(builder, "extra", newExtra, true);
			}
			catch (IllegalAccessException e)
			{
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public static BuildConfig getRunningBuildConfig()
	{
		GameBuilder builder = getRunningBuilder();
		
		if(builder == null) return null;
		
		BuildConfig buildConfig;
		try
		{
			buildConfig = (BuildConfig) FieldUtils.readField(builder, "buildConfig", true);
		}
		catch (IllegalAccessException e)
		{
			log.error(e.getMessage(), e);
			return null;
		}
		
		return buildConfig;
	}
}

package com.polydes.configurations;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JToggleButton;

import com.polydes.common.res.ResourceLoader;
import com.polydes.common.res.Resources;

public class ToggleSwitch extends JToggleButton
{
	private static final Resources res = ResourceLoader.getResources("com.polydes.configurations");
	
	private static final Image enabledIcon = res.loadIcon("switch-enabled.png").getImage();
	private static final Image disabledIcon = res.loadIcon("switch-disabled.png").getImage();
	private static final int width = enabledIcon.getWidth(null);
	private static final int height = enabledIcon.getHeight(null);
	
	@Override
	public void paint(Graphics gr)
	{
		gr.drawImage(isSelected() ? enabledIcon : disabledIcon, 0, 0, null);
	}
	
	@Override
	public Dimension getMinimumSize()
	{
		return new Dimension(width, height);
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(width, height);
	}
	
	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(width, height);
	}
}
package com.polydes.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import stencyl.core.api.pnodes.DefaultBranch;
import stencyl.core.api.pnodes.DefaultLeaf;
import stencyl.core.api.pnodes.HierarchyModel;
import stencyl.core.api.pnodes.HierarchyRepresentation;
import stencyl.core.api.pnodes.Leaf;

public class Configurations extends HierarchyModel<DefaultLeaf, DefaultBranch>
{
	public static final String ACTIVE_CONFIGURATION = "activeConfiguration";
	private final HashMap<String, Configuration> configurations;
	private Configuration activeConfiguration;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public Configurations()
	{
		super(new DefaultBranch("configurationsRoot"), DefaultLeaf.class, DefaultBranch.class);
		setUniqueLeafNames(true);
		configurations = new HashMap<>();
		addRepresentation(nameTracker);
	}
	
	public Configuration getConfiguration(String name)
	{
		return configurations.get(name);
	}
	
	public void setActiveConfiguration(Configuration activeConfiguration)
	{
		Configuration oldActiveConfiguration = this.activeConfiguration;
		this.activeConfiguration = activeConfiguration;
		pcs.firePropertyChange(ACTIVE_CONFIGURATION, oldActiveConfiguration, activeConfiguration);
	}
	
	public Configuration getActiveConfiguration()
	{
		return activeConfiguration;
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(propertyName, listener);
	}

	//a flat mapping by globally unique names
	private final HierarchyRepresentation<DefaultLeaf, DefaultBranch> nameTracker = new HierarchyRepresentation<>()
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			if(
				((DefaultLeaf) evt.getSource()).getUserData() instanceof Configuration &&
					evt.getPropertyName().equals(Leaf.NAME) &&
					evt.getOldValue() instanceof String oldName &&
					evt.getNewValue() instanceof String newName
			)
			{
				configurations.put(newName, configurations.remove(oldName));
			}
		}

		@Override
		public void itemRemoved(DefaultBranch branch, DefaultLeaf item, int pos)
		{
			if(item.getUserData() instanceof Configuration cfg)
			{
				configurations.remove(cfg.getName());
				if(activeConfiguration == cfg) setActiveConfiguration(null);
			}
		}

		@Override
		public void itemAdded(DefaultBranch branch, DefaultLeaf item, int pos)
		{
			if(item.getUserData() instanceof Configuration cfg)
			{
				configurations.put(cfg.getName(), cfg);
				if(activeConfiguration == null) setActiveConfiguration(cfg);
			}
		}
	};
}

package com.polydes.configurations;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.polydes.common.nodes.DefaultBranch;
import com.polydes.common.nodes.DefaultLeaf;
import com.polydes.common.nodes.HierarchyModel;
import com.polydes.common.nodes.HierarchyRepresentation;
import com.polydes.common.nodes.Leaf;
import com.polydes.common.nodes.NodeCreator;
import com.polydes.common.res.ResourceLoader;
import com.polydes.common.res.Resources;

public class Configurations extends HierarchyModel<DefaultLeaf, DefaultBranch> implements NodeCreator<DefaultLeaf, DefaultBranch>
{
	private static final Resources res = ResourceLoader.getResources("com.polydes.configurations");
	
	//a flat mapping by globally unique names
	private final HierarchyRepresentation<DefaultLeaf, DefaultBranch> nameTracker = new HierarchyRepresentation<DefaultLeaf, DefaultBranch>()
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			if(evt.getPropertyName() == Leaf.NAME)
			{
				if(((DefaultLeaf) evt.getSource()).getUserData() instanceof Configuration)
				{
					configurations.put((String) evt.getNewValue(), configurations.remove(evt.getOldValue()));
				}
			}
		}
		
		@Override
		public void itemRemoved(DefaultBranch branch, DefaultLeaf item, int pos)
		{
			if(item.getUserData() instanceof Configuration)
			{
				Configuration cfg = (Configuration) item.getUserData();
				configurations.remove(cfg.getName());
				if(activeConfiguration == cfg) setActiveConfiguration(null);
			}
		}
		
		@Override
		public void itemAdded(DefaultBranch branch, DefaultLeaf item, int pos)
		{
			if(item.getUserData() instanceof Configuration)
			{
				Configuration cfg = (Configuration) item.getUserData();
				configurations.put(cfg.getName(), cfg);
				if(activeConfiguration == null) setActiveConfiguration(cfg);
			}
		}
	};
	private HashMap<String, Configuration> configurations;
	
	private Configuration activeConfiguration;
	
	public Configurations()
	{
		super(new DefaultBranch("configurationsRoot"), DefaultLeaf.class, DefaultBranch.class);
		setUniqueLeafNames(true);
		setNodeCreator(this);
		configurations = new HashMap<>();
		addRepresentation(nameTracker);
	}
	
	public Configuration getConfiguration(String name)
	{
		return configurations.get(name);
	}
	
	public void setActiveConfiguration(Configuration activeConfiguration)
	{
		if(this.activeConfiguration != null)
			this.activeConfiguration.getTreeNodeWrapper().setIcon(res.loadIcon("games-config-options.png"));
		this.activeConfiguration = activeConfiguration;
		this.activeConfiguration.getTreeNodeWrapper().setIcon(res.loadIcon("games-config-options-active.png"));
	}
	
	public Configuration getActiveConfiguration()
	{
		return activeConfiguration;
	}

	@Override
	public boolean attemptRemove(List<DefaultLeaf> item)
	{
		return true;
	}

	@Override
	public DefaultLeaf createNode(CreatableNodeInfo info, String name)
	{
		if(info.name.equals("Folder"))
			return new DefaultBranch(name);
		
		Configuration configuration = new Configuration();
		configuration.setName(name);
		return configuration.getTreeNodeWrapper();
	}

	@Override
	public void editNode(DefaultLeaf item)
	{
		
	}

	@Override
	public ArrayList<CreatableNodeInfo> getCreatableNodeList(DefaultBranch branch)
	{
		return new ArrayList<>(List.of(new CreatableNodeInfo("Configuration", null, res.loadIcon("games-config-options.png"))));
	}
	
	@Override
	public ArrayList<NodeAction<DefaultLeaf>> getNodeActions(DefaultLeaf[] targets)
	{
		if(targets.length == 1 && targets[0].getUserData() instanceof Configuration)
		{
			NodeAction<DefaultLeaf> markAsActiveConfig = new NodeAction<DefaultLeaf>("Make active configuration", null, leaf -> {
				setActiveConfiguration((Configuration) leaf.getUserData());
			});
			return new ArrayList<>(Arrays.asList(markAsActiveConfig));
		}
		return new ArrayList<>();
	}

	@Override
	public void nodeRemoved(DefaultLeaf item)
	{
		
	}
}

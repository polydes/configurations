package com.polydes.configurations;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.polydes.common.nodes.DefaultBranch;
import com.polydes.common.nodes.DefaultLeaf;
import com.polydes.common.nodes.HierarchyModel;
import com.polydes.common.nodes.NodeUtils;

public class Configuration
{
	private DefaultLeaf treeNodeWrapper;
	
	private String description = "";
	
	public final Set<String> defines;

	public Configuration()
	{
		this.defines = new HashSet<>();
		treeNodeWrapper = new DefaultLeaf("", this);
	}
	
	public DefaultLeaf getTreeNodeWrapper()
	{
		return treeNodeWrapper;
	}
	
	public String getName()
	{
		return treeNodeWrapper.getName();
	}

	public void setName(String name)
	{
		DefaultBranch root = treeNodeWrapper.getParent() == null ? null : NodeUtils.getRoot(treeNodeWrapper);
		if(HierarchyModel.rootModels.containsKey(root))
		{
			if(((Configurations) HierarchyModel.rootModels.get(root)).getConfiguration(name) != null)
			{
				return;
			}
		}
		treeNodeWrapper.setName(name);
		treeNodeWrapper.setDirty(true);
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
		treeNodeWrapper.setDirty(true);
	}
	
	public void addDefine(String define)
	{
		defines.add(define);
		treeNodeWrapper.setDirty(true);
	}
	
	public void removeDefine(String define)
	{
		defines.remove(define);
		treeNodeWrapper.setDirty(true);
	}

	public boolean containsDefine(String define)
	{
		return defines.contains(define);
	}

	public void forEachDefine(Consumer<? super String> action)
	{
		defines.forEach(action);
	}

	public int numberOfDefines()
	{
		return defines.size();
	}

	public boolean hasNoDefines()
	{
		return defines.isEmpty();
	}

	public Stream<String> streamDefines()
	{
		return defines.stream();
	}
}

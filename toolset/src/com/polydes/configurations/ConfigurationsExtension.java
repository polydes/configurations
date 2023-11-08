package com.polydes.configurations;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import stencyl.app.ext.PageAddon;
import stencyl.app.ext.PageAddon.EngineExtensionPageAddon;
import stencyl.core.api.fs.Locations;
import stencyl.core.api.pnodes.DefaultBranch;
import stencyl.core.api.pnodes.DefaultLeaf;
import stencyl.core.api.struct.NotifierMap;
import stencyl.core.ext.GameExtension;
import stencyl.core.ext.engine.ExtensionInstance;
import stencyl.core.ext.engine.ExtensionInstanceManager;
import stencyl.core.ext.engine.ExtensionInstanceManager.FormatUpdateSubmitter;
import stencyl.core.io.FileHelper;
import stencyl.core.io.XmlHelper;
import stencyl.core.lib.ProjectManager;
import stencyl.sw.app.center.GameLibrary;
import stencyl.sw.app.ext.SWExtensionInstance;
import stencyl.sw.core.lib.game.Game;

public class ConfigurationsExtension extends GameExtension
{
	private static final Logger log = Logger.getLogger(ConfigurationsExtension.class);
	
	private Configurations configurations;
	private Map<String, Map<String, Define>> engineExtensionDefines;
	private ConfigurationsPage configurationsPage;
	
	public Configurations getConfigurations()
	{
		return configurations;
	}
	
	public Map<String, Map<String, Define>> getEngineExtensionDefines()
	{
		return engineExtensionDefines;
	}

	@Override
	public void updateFromVersion(int fromVersion, FormatUpdateSubmitter formatUpdateQueue)
	{
		
	}
	
	@Override
	public void onLoad()
	{
		PageAddon configurationsSidebarPage = new EngineExtensionPageAddon(owner())
		{
			@Override
			public JPanel getPage()
			{
				return onGameCenterActivate();
			}
		};
		
		owner().getAddons().setAddon(GameLibrary.DASHBOARD_SIDEBAR_PAGE_ADDONS, configurationsSidebarPage);

		configurations = new Configurations();
		engineExtensionDefines = new HashMap<>();

		engineExtensionDefines.clear();
		((Game) getProject()).getExtensionManager().getLoadedEnabledExtensions().addListener(extensionUpdateListener);
		refreshExtensionDefinitions();

		String dataLocation = getProject().getFiles().getExtensionGameDataLocation(getManifest().id);
		File configXml = new File(dataLocation, "configurations.xml");
		if(configXml.exists())
		{
			try
			{
				Element configurationsXml = FileHelper.readXMLFromFile(dataLocation + "configurations.xml").getDocumentElement();
				loadConfigurations(configurationsXml, configurations.getRootBranch());
				String activeConfigurationName = configurationsXml.getAttribute("activeConfiguration");
				configurations.setActiveConfiguration(configurations.getConfiguration(activeConfigurationName));
			}
			catch (IOException e)
			{
				log.error(e.getMessage(), e);
			}
		}
		else
		{
			Configuration defaultConfiguration = new Configuration();
			defaultConfiguration.setName("Default");
			defaultConfiguration.setDescription("Default game configuration.");

			configurations.getRootBranch().addItem(defaultConfiguration.getTreeNodeWrapper());
		}
	}

	@Override
	public void onUnload()
	{
		((Game) getProject()).getExtensionManager().getLoadedEnabledExtensions().removeListener(extensionUpdateListener);
		engineExtensionDefines.clear();

		if(configurationsPage != null)
			configurationsPage.dispose();

		configurations = null;
		engineExtensionDefines = null;
		configurationsPage = null;
	}

	public JPanel onGameCenterActivate()
	{
		if(configurationsPage == null)
			configurationsPage = new ConfigurationsPage(this);
		return configurationsPage;
	}

	@Override
	protected void onSave()
	{
		super.onSave();
		//XXX: This could be true for the initial game save,
		//before the game has been opened for the first time.
		//This should probably be considered a bug in Stencyl's
		//active game lifecycle.
		if(configurations == null) return;

		String dataLocation = getProject().getFiles().getExtensionGameDataLocation(getManifest().id);
		new File(dataLocation).mkdirs();
		
		Document doc = FileHelper.newDocument();
		Element root = doc.createElement("configurations");
		doc.appendChild(root);
		
		saveConfigurations(doc, root, configurations.getRootBranch());
		if(configurations.getActiveConfiguration() != null)
		{
			root.setAttribute("activeConfiguration", configurations.getActiveConfiguration().getName());
		}
		try
		{
			FileHelper.writeXMLToFile(doc, new File(dataLocation + "configurations.xml"));
			configurations.getRootBranch().setDirty(false);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void addToBuildCommand(List<String> arr)
	{
		if(configurations.getActiveConfiguration() != null)
		{
			arr.add("-D" + "configurations");
			for(String define : configurations.getActiveConfiguration().defines)
			{
				arr.add("-D" + define);
			}
		}
	}
	
	private void refreshExtensionDefinitions()
	{
		ExtensionInstanceManager<? extends ExtensionInstance> extManager = getProject().getExtensionManager();
		var loadedEnabledExtensions = extManager.getLoadedEnabledExtensions();
		
		var idsToKeep = new HashSet<>(loadedEnabledExtensions.keySet());
		engineExtensionDefines.keySet().removeIf(key -> !idsToKeep.contains(key));
		
		for(ExtensionInstance inst : loadedEnabledExtensions.values())
		{
			String extensionID = inst.getExtensionID();
			if(!engineExtensionDefines.containsKey(extensionID))
			{
				engineExtensionDefines.put(extensionID, new HashMap<>());
				File extensionRoot = new File(Locations.getGameExtensionLocation(extensionID));
				File definesXml = new File(extensionRoot, "defines.xml");
				if(definesXml.exists())
				{
					try
					{
						Element e = FileHelper.readXMLFromFile(definesXml).getDocumentElement();
						loadDefines(extensionID, e);
					}
					catch(IOException e)
					{
						log.error(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	private void loadConfigurations(Element configurations, DefaultBranch addToBranch)
	{
		addToBranch.markAsLoading(true);
		for(Element e : XmlHelper.children(configurations))
		{
			if(e.getTagName().equals("section"))
			{
				DefaultBranch newBranch = new DefaultBranch(e.getAttribute("name"));
				loadConfigurations(e, newBranch);
				addToBranch.addItem(newBranch);
			}
			else if(e.getTagName().equals("configuration"))
			{
				Configuration cfg = new Configuration();
				cfg.getTreeNodeWrapper().markAsLoading(true);
				
				cfg.setName(e.getAttribute("name"));
				cfg.setDescription(e.getAttribute("description"));
				
				Element defines = XmlHelper.child(e, "defines");
				if(defines != null)
				{
					for(Element defE : XmlHelper.children(defines))
					{
						if(defE.getTagName().equals("define"))
						{
							cfg.addDefine(defE.getAttribute("name"));
						}
					}
				}
				
				cfg.getTreeNodeWrapper().markAsLoading(false);
				addToBranch.addItem(cfg.getTreeNodeWrapper());
			}
		}
		addToBranch.markAsLoading(false);
	}
	
	private void saveConfigurations(Document doc, Element addToElement, DefaultBranch configurations)
	{
		for(DefaultLeaf leaf : configurations.getItems())
		{
			if(leaf instanceof DefaultBranch)
			{
				Element section = doc.createElement("section");
				section.setAttribute("name", leaf.getName());
				saveConfigurations(doc, section, (DefaultBranch) leaf);
				addToElement.appendChild(section);
			}
			else if(leaf.getUserData() instanceof Configuration cfgToSave)
			{
				Element configuration = doc.createElement("configuration");
				configuration.setAttribute("name", cfgToSave.getName());
				configuration.setAttribute("description", cfgToSave.getDescription());
				
				Element defines = doc.createElement("defines");
				for(String defineName : cfgToSave.defines)
				{
					Element define = doc.createElement("define");
					define.setAttribute("name", defineName);
					defines.appendChild(define);
				}
				configuration.appendChild(defines);
				
				addToElement.appendChild(configuration);
			}
		}
	}
	
	private void loadDefines(String extensionID, Element defines)
	{
		for(Element e : XmlHelper.children(defines))
		{
			if(e.getTagName().equals("define"))
			{
				String name = e.getAttribute("name");
				String description = e.getAttribute("description");
				
				Define def = new Define(name, description);
				
				engineExtensionDefines.get(extensionID).put(name, def);
			}
		}
	}
	
	NotifierMap.MapListener<SWExtensionInstance> extensionUpdateListener = event -> {
		
		//If a game is being closed, the extension blocks map will be updated after setting the game to null.
		if(ProjectManager.noProjectOpened())
		{
			engineExtensionDefines.clear();
			return;
		}
		
		refreshExtensionDefinitions();
	};
}
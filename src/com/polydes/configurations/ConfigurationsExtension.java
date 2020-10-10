package com.polydes.configurations;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.polydes.common.io.XML;
import com.polydes.common.nodes.DefaultBranch;
import com.polydes.common.nodes.DefaultLeaf;

import stencyl.core.lib.Game;
import stencyl.sw.SW;
import stencyl.sw.app.tasks.buildgame.GameBuilder;
import stencyl.sw.editors.game.advanced.ExtensionInstance;
import stencyl.sw.editors.snippet.designer.AttributeType;
import stencyl.sw.editors.snippet.designer.Definition;
import stencyl.sw.editors.snippet.designer.Definition.Category;
import stencyl.sw.editors.snippet.designer.Definitions;
import stencyl.sw.editors.snippet.designer.Definitions.DefinitionMap;
import stencyl.sw.editors.snippet.designer.block.Block.BlockType;
import stencyl.sw.editors.snippet.designer.block.BlockTheme;
import stencyl.sw.editors.snippet.designer.codebuilder.CodeBuilder;
import stencyl.sw.editors.snippet.designer.codebuilder.CodeElement;
import stencyl.sw.editors.snippet.designer.codemap.BasicCodeMap;
import stencyl.sw.editors.snippet.designer.dropdown.DefaultCodeConverter;
import stencyl.sw.editors.snippet.designer.dropdown.DropdownData;
import stencyl.sw.ext.BaseExtension;
import stencyl.sw.ext.OptionsPanel;
import stencyl.sw.util.FileHelper;
import stencyl.sw.util.Locations;
import stencyl.sw.util.NotifierHashMap;

public class ConfigurationsExtension extends BaseExtension
{
	private static final Logger log = Logger.getLogger(ConfigurationsExtension.class);
	
	private Configurations configurations;
	private Map<String, Map<String, Define>> engineExtensionDefines;
	private DefinitionMap tagCache;
	private ConfigurationsPage configurationsPage;
	
	public Configurations getConfigurations()
	{
		return configurations;
	}
	
	public Map<String, Map<String, Define>> getEngineExtensionDefines()
	{
		return engineExtensionDefines;
	}
	
	/*
	 * Happens when StencylWorks launches. 
	 * 
	 * Avoid doing anything time-intensive in here, or it will
	 * slow down launch.
	 */
	@Override
	public void onStartup()
	{
		super.onStartup();
		
		isInMenu = false;
		isInGameCenter = true;
		gameCenterName = "Build Configurations";
	}
	
	@Override
	public DefinitionMap getDesignModeBlocks()
	{
		return tagCache;
	}
	
	public void addDesignModeBlocks()
	{
		// ===== simple wrapper version
		
		String spec = "#if %0";
		
		Definition blockDef = new Definition
		(
			Category.CUSTOM,
			"def-wrap-if",
			new AttributeType[] { AttributeType.BOOLEAN, AttributeType.CODE_BLOCK },
			new BasicCodeMap("#if ~\n"
					+ "{\n"
						+ "~\n"
					+ "}\n"
					+ "#end"),
			null,
			spec,
			BlockType.WRAPPER,
			AttributeType.VOID,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("charcoal");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
		// ===== simple inline version
		
		spec = "#if %0 %1 else %2";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"def-inline-ifelse",
			new AttributeType[] { AttributeType.BOOLEAN, AttributeType.OBJECT, AttributeType.OBJECT },
			new BasicCodeMap("#if ~ ~ #else ~ #end"),
			null,
			spec,
			BlockType.NORMAL,
			AttributeType.OBJECT,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("charcoal");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
		// ===== chain version
		
		spec = "#if %0 ...";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"def-chain-if",
			new AttributeType[] { AttributeType.BOOLEAN },
			new BasicCodeMap("#if ~"),
			null,
			spec,
			BlockType.ACTION,
			AttributeType.VOID,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("charcoal");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
		spec = "#... else if %0 ...";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"def-chain-elseif",
			new AttributeType[] { AttributeType.BOOLEAN },
			new BasicCodeMap("#elseif ~"),
			null,
			spec,
			BlockType.ACTION,
			AttributeType.VOID,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("charcoal");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
		spec = "#... else ...";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"def-chain-else",
			new AttributeType[] { AttributeType.OBJECT },
			new BasicCodeMap("#else"),
			null,
			spec,
			BlockType.ACTION,
			AttributeType.VOID,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("charcoal");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
		spec = "#... end";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"def-chain-end",
			new AttributeType[] { AttributeType.OBJECT },
			new BasicCodeMap("#end"),
			null,
			spec,
			BlockType.ACTION,
			AttributeType.VOID,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("charcoal");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
		spec = "%0";
		
		//XXX:
		DropdownData allPlatforms = new DropdownData(
			new String[] {
				    "plaf.flash",
				    "plaf.html5",
				    "plaf.desktop",
				    "plaf.ios",
				    "plaf.android",
				    "plaf.web",
				    "plaf.mobile",
				    "plaf.win",
				    "plaf.mac",
				    "plaf.lin" },
			new DefaultCodeConverter(new String[] {
					"flash",
					"html5",
					"desktop",
					"ios",
					"android",
					"(flash || html5)",
					"mobile",
					"windows",
					"mac",
					"linux" }));
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"def-platform-id",
			new AttributeType[] { AttributeType.DROPDOWN },
			new BasicCodeMap("~"),
			null,
			spec,
			BlockType.NORMAL,
			AttributeType.BOOLEAN,
			null
		);
		blockDef.initDropdowns(new DropdownData[] {allPlatforms.copy()});
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("charcoal");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
		spec = "%0 and %1";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"def-cond-and",
			new AttributeType[] { AttributeType.BOOLEAN, AttributeType.BOOLEAN },
			new BasicCodeMap("(~ && ~)"),
			null,
			spec,
			BlockType.NORMAL,
			AttributeType.BOOLEAN,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("charcoal");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
		spec = "%0 or %1";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"def-cond-or",
			new AttributeType[] { AttributeType.BOOLEAN, AttributeType.BOOLEAN },
			new BasicCodeMap("(~ || ~)"),
			null,
			spec,
			BlockType.NORMAL,
			AttributeType.BOOLEAN,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("charcoal");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
		spec = "not %0";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"def-cond-not",
			new AttributeType[] { AttributeType.BOOLEAN },
			new BasicCodeMap("!~"),
			null,
			spec,
			BlockType.NORMAL,
			AttributeType.BOOLEAN,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("charcoal");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
	}
	
	public void dispose()
	{
		for(String tag : tagCache.keySet())
			Definitions.get().remove(tag);
		tagCache.clear();
		
		if(configurationsPage != null)
			configurationsPage.dispose();
		
		configurations = null;
		engineExtensionDefines = null;
		tagCache = null;
		configurationsPage = null;
	}
	
	/*
	 * Happens when the extension is told to display.
	 * 
	 * May happen multiple times during the course of the app. 
	 * 
	 * A good way to handle this is to make your extension a singleton.
	 */
	@Override
	public void onActivate()
	{
	}
	
	/*
	 * Happens when StencylWorks closes.
	 *  
	 * Usually used to save things out.
	 */
	@Override
	public void onDestroy()
	{
	}
	
	@Override
	protected boolean hasOptions()
	{
		return false;
	}
	
	@Override
	public OptionsPanel onOptions()
	{
		return null;
	}
	
	@Override
	public JPanel onGameCenterActivate()
	{
		if(configurationsPage == null)
			configurationsPage = new ConfigurationsPage(this);
		return configurationsPage;
	}
	
	/*
	 * Happens when the extension is first installed.
	 */
	@Override
	public void onInstall()
	{
	}
	
	/*
	 * Happens when the extension is uninstalled.
	 * 
	 * Clean up files.
	 */
	@Override
	public void onUninstall()
	{
	}

	@Override
	public void onGameOpened(Game game)
	{
		configurations = new Configurations();
		engineExtensionDefines = new HashMap<>();
		tagCache = new DefinitionMap();
		
		engineExtensionDefines.clear();
		SW.get().getEngineExtensionManager().getExtensionBlocks().addListener(extensionUpdateListener);
		refreshExtensionDefinitions();
		addDesignModeBlocks();
		
		String dataLocation = Locations.getExtensionGameDataLocation(game, getManifest().id);
		File configXml = new File(dataLocation, "configurations.xml");
		if(configXml.exists())
		{
			try
			{
				Element configurationsXml = FileHelper.readXMLFromFile(dataLocation + "configurations.xml").getDocumentElement();
				loadConfigurations(configurationsXml, configurations.getRootBranch());
				String activeConfigurationName = configurationsXml.getAttribute("activeConfiguration");
				configurations.setActiveConfiguration(configurations.getConfiguration(activeConfigurationName));
				configurations.getRootBranch().setDirty(false);
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
			configurations.getRootBranch().setDirty(false);
		}
	}
	
	@Override
	public void onGameClosed(Game game)
	{
		SW.get().getEngineExtensionManager().getExtensionBlocks().removeListener(extensionUpdateListener);
		engineExtensionDefines.clear();
		dispose();
	}

	@Override
	public void onGameSave(Game game)
	{
		String dataLocation = Locations.getExtensionGameDataLocation(game, getManifest().id);
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
	public void onGameBuild(Game game)
	{
		if(configurations.getActiveConfiguration() != null)
		{
			String[] cliAdditions = configurations.getActiveConfiguration().defines.stream()
					.map(s -> "-D" + s).toArray(String[]::new);
			
			GameBuilder builder = GameBuilderHelper.getRunningBuilder();
			if(builder != null)
			{
				GameBuilderHelper.appendCommandLineArguments(builder, cliAdditions);
			}
			else
			{
				log.error("No GameBuilder found");
			}
		}
	}
	
	private void refreshExtensionDefinitions()
	{
		for(ExtensionInstance inst : Game.getGame().getExtensions().values())
		{
			String extensionID = inst.getExtensionID();
			if(inst.isEnabled())
			{
				if(!engineExtensionDefines.containsKey(extensionID) && SW.get().getEngineExtensionManager().getExtensionBlocks().containsKey(extensionID))
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
			else
			{
				engineExtensionDefines.remove(extensionID);
			}
		}
	}
	
	private void loadConfigurations(Element configurations, DefaultBranch addToBranch)
	{
		for(Element e : XML.children(configurations))
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
				
				cfg.setName(e.getAttribute("name"));
				cfg.setDescription(e.getAttribute("description"));
				
				Element defines = XML.child(e, "defines");
				if(defines != null)
				{
					for(Element defE : XML.children(defines))
					{
						if(defE.getTagName().equals("define"))
						{
							cfg.addDefine(defE.getAttribute("name"));
						}
					}
				}
				
				addToBranch.addItem(cfg.getTreeNodeWrapper());
			}
		}
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
			else if(leaf.getUserData() instanceof Configuration)
			{
				Configuration cfgToSave = (Configuration) leaf.getUserData();
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
		for(Element e : XML.children(defines))
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
	
	NotifierHashMap.Listener<DefinitionMap> extensionUpdateListener = event -> {
		
		//If a game is being closed, the extension blocks map will be updated after setting the game to null.
		if(Game.noGameOpened())
		{
			engineExtensionDefines.clear();
			return;
		}
		
		refreshExtensionDefinitions();
	};
}
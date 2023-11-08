package com.polydes.configurations;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;

import stencyl.app.api.nodes.HierarchyModelInterface;
import stencyl.app.api.nodes.NodeCreator;
import stencyl.app.api.nodes.NodeUIProperties;
import stencyl.app.comp.MiniSplitPane;
import stencyl.app.comp.darktree.DarkTree;
import stencyl.app.comp.dg.DialogPanel;
import stencyl.app.comp.util.DocumentAdapter;
import stencyl.app.ext.res.AppResourceLoader;
import stencyl.app.ext.res.AppResources;
import stencyl.app.lnf.Theme;
import stencyl.core.api.pnodes.DefaultBranch;
import stencyl.core.api.pnodes.DefaultLeaf;
import stencyl.core.api.pnodes.HierarchyModel;

/**
 * A page to set up configurations, set the defines used by those configuration,
 * and choose the active configuration.
 */
public class ConfigurationsPage extends JPanel
{
	private static final AppResources res = AppResourceLoader.getResources("com.polydes.configurations");

	private final ConfigurationsExtension extension;
	private final DarkTree<DefaultLeaf, DefaultBranch> configurationsTree;
	private final JPanel configurationPageWrapper;
	private JPanel currentConfigurationPage;
	
	private final ConfigurationsModelInterface configurationsInterface;
	private final ConfigNodeIconProvider nodeIconProvider = new ConfigNodeIconProvider();

	public ConfigurationsPage(ConfigurationsExtension extension)
	{
		super(new BorderLayout());

		configurationsInterface = new ConfigurationsModelInterface(extension.getConfigurations());
		
		this.extension = extension;
		configurationsTree = new DarkTree<>(configurationsInterface);
		configurationsTree.setIconProvider(nodeIconProvider::getIcon);
		configurationsTree.setNamingEditingAllowed(false);
		configurationsTree.getTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		configurationsTree.setListEditEnabled(true);

		configurationPageWrapper = new JPanel(new BorderLayout());
		configurationPageWrapper.setBackground(Theme.BG_COLOR);

		extension.getConfigurations().addPropertyChangeListener(Configurations.ACTIVE_CONFIGURATION, nodeIconProvider);
		
		MiniSplitPane splitPane = new MiniSplitPane();
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);

		splitPane.setLeftComponent(configurationsTree);
		splitPane.setRightComponent(configurationPageWrapper);
		splitPane.setDividerLocation(DarkTree.DEF_WIDTH);

		configurationsTree.getSelectionState().addSelectionListener(selectionEvent -> {

			DefaultLeaf toShow = configurationsTree.getSelectionState().firstNode();

			if(currentConfigurationPage instanceof DefinitionsPanel)
			{
				((DefinitionsPanel) currentConfigurationPage).dispose();
				currentConfigurationPage = null;
			}

			configurationPageWrapper.removeAll();
			configurationPageWrapper.revalidate();
			configurationPageWrapper.repaint();

			if(toShow == null || !(toShow.getUserData() instanceof Configuration selectedConfiguration))
			{
				return;
			}

			currentConfigurationPage = new DefinitionsPanel(extension, selectedConfiguration);
			configurationPageWrapper.add(currentConfigurationPage, BorderLayout.CENTER);

		});

		configurationsTree.forceRerender();
	}

	private static final ImageIcon activeIcon = res.loadIcon("games-config-options-active.png");
	private static final ImageIcon inactiveIcon = res.loadIcon("games-config-options.png");

	private final class ConfigNodeIconProvider implements PropertyChangeListener
	{
		public ImageIcon getIcon(DefaultLeaf object)
		{
			if(object.getUserData() instanceof Configuration)
			{
				if(object.getUserData() == extension.getConfigurations().getActiveConfiguration())
					return activeIcon;

				return inactiveIcon;
			}
			
			return null;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			if(evt.getPropertyName().equals(Configurations.ACTIVE_CONFIGURATION))
			{
				Configuration oldActiveConfiguration = (Configuration) evt.getOldValue();
				Configuration newActiveConfiguration = (Configuration) evt.getNewValue();
				if(oldActiveConfiguration != null)
					oldActiveConfiguration.getTreeNodeWrapper().firePropertyChange(NodeUIProperties.ICON, activeIcon, inactiveIcon);
				newActiveConfiguration.getTreeNodeWrapper().firePropertyChange(NodeUIProperties.ICON, inactiveIcon, activeIcon);
			}
		}
	}
	
	private final class ConfigurationsModelInterface
		extends HierarchyModelInterface<DefaultLeaf, DefaultBranch>
		implements NodeCreator<DefaultLeaf, DefaultBranch>
	{
		public ConfigurationsModelInterface(HierarchyModel<DefaultLeaf, DefaultBranch> model)
		{
			super(model);
			setNodeCreator(this);
		}

		@Override
		public boolean attemptRemove(java.util.List<DefaultLeaf> item)
		{
			return true;
		}

		@Override
		public DefaultLeaf createNode(CreatableNodeInfo info, String name, DefaultBranch newNodeFolder, int insertPosition)
		{
			DefaultLeaf newLeaf;
			if(info.name.equals("Folder"))
			{
				newLeaf = new DefaultBranch(name);
			}
			else
			{
				Configuration configuration = new Configuration();
				configuration.setName(name);
				newLeaf = configuration.getTreeNodeWrapper();
			}
			
			newLeaf.setDirty(true);
			return newLeaf;
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
				NodeAction<DefaultLeaf> markAsActiveConfig = new NodeAction<>("Make active configuration", null, leaf -> {
					extension.getConfigurations().setActiveConfiguration((Configuration) leaf.getUserData());
				});
				return new ArrayList<>(List.of(markAsActiveConfig));
			}
			return new ArrayList<>();
		}

		@Override
		public void nodeRemoved(DefaultLeaf item)
		{

		}
	}

	private static final class DefinitionsPanel extends JPanel
	{
		public DefinitionsPanel(ConfigurationsExtension extension, Configuration configuration)
		{
			super(new BorderLayout());

			DialogPanel panel = new DialogPanel(Theme.LIGHT_BG_COLOR);

			JTextField nameField = new JTextField();
			panel.addTextField("Name", nameField);
			nameField.setText(configuration.getName());
			nameField.getDocument().addDocumentListener(new DocumentAdapter(false)
			{
				@Override
				protected void update()
				{
					configuration.setName(nameField.getText());
				}
			});

			JTextArea descriptionField = new JTextArea();
			panel.addGenericRow("Description", descriptionField);
			descriptionField.setText(configuration.getDescription());
			descriptionField.getDocument().addDocumentListener(new DocumentAdapter(false)
			{
				@Override
				protected void update()
				{
					configuration.setDescription(descriptionField.getText());
				}
			});

			panel.finishBlock();

			for(Entry<String, Map<String, Define>> entry : extension.getEngineExtensionDefines().entrySet())
			{
				String extensionID = entry.getKey();
				String extensionName = extension.getProject().getExtensionManager().getExtension(extensionID).getName();

				Map<String, Define> defines = entry.getValue();

				if(!defines.isEmpty())
				{
					panel.addHeader(extensionName);

					for(Define define : defines.values())
					{
						ToggleSwitch ts = new ToggleSwitch();
						panel.addGenericRow(define.name, ts);
						panel.addDescriptionNoSpace(define.description);
						ts.setSelected(configuration.containsDefine(define.name));
						ts.addActionListener(event -> {
							if(ts.isSelected()) configuration.addDefine(define.name);
							else configuration.removeDefine(define.name);
						});
					}

					panel.finishBlockNoFill();
				}
			}

			//panel.finishBlock();

			panel.addFinalRow(new JLabel());

			add(panel, BorderLayout.CENTER);
		}

		public void dispose()
		{
		}
	}

	public void dispose()
	{
		configurationsTree.dispose();
		if(currentConfigurationPage instanceof DefinitionsPanel)
		{
			((DefinitionsPanel) currentConfigurationPage).dispose();
			currentConfigurationPage = null;
		}
		extension.getConfigurations().removePropertyChangeListener(Configurations.ACTIVE_CONFIGURATION, nodeIconProvider);
	}
}

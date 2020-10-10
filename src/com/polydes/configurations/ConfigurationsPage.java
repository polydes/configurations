package com.polydes.configurations;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.tree.TreeSelectionModel;

import com.polydes.common.comp.MiniSplitPane;
import com.polydes.common.comp.utils.DocumentAdapter;
import com.polydes.common.nodes.DefaultBranch;
import com.polydes.common.nodes.DefaultLeaf;
import com.polydes.common.ui.darktree.DarkTree;

import stencyl.sw.SW;
import stencyl.sw.lnf.Theme;
import stencyl.sw.util.dg.DialogPanel;

/**
 * A page to set up configurations, set the defines used by those configuration,
 * and choose the active configuration.
 */
public class ConfigurationsPage extends JPanel
{
	private final ConfigurationsExtension extension;
	private final DarkTree<DefaultLeaf, DefaultBranch> configurationsTree;
	private JPanel configurationPageWrapper;
	private JPanel currentConfigurationPage;
	
	public ConfigurationsPage(ConfigurationsExtension extension)
	{
		super(new BorderLayout());
		
		this.extension = extension;
		configurationsTree = new DarkTree<DefaultLeaf, DefaultBranch>(extension.getConfigurations());
		configurationsTree.setNamingEditingAllowed(false);
		configurationsTree.getTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		configurationsTree.setListEditEnabled(true);
		
		configurationPageWrapper = new JPanel(new BorderLayout());
		configurationPageWrapper.setBackground(Theme.BG_COLOR);
		
		MiniSplitPane splitPane = new MiniSplitPane();
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);
		
		splitPane.setLeftComponent(configurationsTree);
		splitPane.setRightComponent(configurationPageWrapper);
		splitPane.setDividerLocation(DarkTree.DEF_WIDTH);
		
		extension.getConfigurations().getSelection().addSelectionListener(selectionEvent -> {
			
			DefaultLeaf toShow = extension.getConfigurations().getSelection().firstNode();
			
			if(currentConfigurationPage instanceof DefinitionsPanel)
			{
				((DefinitionsPanel) currentConfigurationPage).dispose();
				currentConfigurationPage = null;
			}
			
			configurationPageWrapper.removeAll();
			configurationPageWrapper.revalidate();
			configurationPageWrapper.repaint();
			
			if(toShow == null || !(toShow.getUserData() instanceof Configuration))
			{
				return;
			}
			
			Configuration selectedConfiguration = (Configuration) toShow.getUserData();
			currentConfigurationPage = new DefinitionsPanel(extension, selectedConfiguration);
			configurationPageWrapper.add(currentConfigurationPage, BorderLayout.CENTER);
			
		});
		
		configurationsTree.forceRerender();
	}
	
	private static final class DefinitionsPanel extends JPanel
	{
		private final DialogPanel panel;
		
		public DefinitionsPanel(ConfigurationsExtension extension, Configuration configuration)
		{
			super(new BorderLayout());
			
			panel = new DialogPanel(Theme.LIGHT_BG_COLOR);
			
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
				String extensionName = SW.get().getEngineExtensionManager().getExtensions().get(extensionID).getName();
				
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
				}
			}
			
			panel.finishBlock();

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
	}
}

package ctrmap.pokescript.ide.forms.settings.project;

import ctrmap.pokescript.ide.PSIDE;
import ctrmap.pokescript.ide.forms.settings.project.panes.ProjectDefinitionsPane;
import ctrmap.pokescript.ide.system.project.IDEProject;
import ctrmap.stdlib.gui.components.tree.CustomJTreeNode;
import ctrmap.stdlib.gui.components.tree.CustomJTreeRootNode;
import ctrmap.stdlib.gui.components.tree.CustomJTreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;

public class ProjectSettings extends javax.swing.JFrame {

	private PSIDE ide;
	private IDEProject project;
	
	private ProjectSettingsPane currentPane;
	
	public ProjectSettings(PSIDE ide, IDEProject project) {
		initComponents();
		setLocationRelativeTo(ide);
		this.ide = ide;
		this.project = project;
		
		initTreeMenu();

		settingsMenuTree.addListener(new CustomJTreeSelectionListener() {
			@Override
			public void onNodeSelected(CustomJTreeNode node) {
				if (node != null){
					btnSaveActionPerformed(null);
					ProjectSettingsNode n = (ProjectSettingsNode)node;
					settingsSubPanel.setViewportView(n.pane);
					revalidate();
					currentPane = n.pane;
				}
			}
		});
	}
	
	private void initTreeMenu(){
		CustomJTreeRootNode root = settingsMenuTree.getRootNode();
		
		CustomJTreeNode compilerNode = addSettingsPane(root, "Compiler");
		addSettingsPane(compilerNode, new ProjectDefinitionsPane(ide, project));
		//todo rest of the panes
		
		((DefaultTreeModel)settingsMenuTree.getModel()).reload();
	}
	
	private ProjectSettingsNode addSettingsPane(CustomJTreeNode parent, ProjectSettingsPane pane){
		return addSettingsPane(parent, null, pane);
	}
	
	private ProjectSettingsNode addSettingsPane(CustomJTreeNode parent, String name){
		return addSettingsPane(parent, name, null);
	}
	
	private ProjectSettingsNode addSettingsPane(CustomJTreeNode parent, String overrideName, ProjectSettingsPane pane){
		ProjectSettingsNode node = new ProjectSettingsNode(overrideName, pane);
		parent.add(node);
		return node;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        settingsSubPanel = new javax.swing.JScrollPane();
        treeSP = new javax.swing.JScrollPane();
        settingsMenuTree = new ctrmap.stdlib.gui.components.tree.CustomJTree();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Project Properties");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        treeSP.setViewportView(settingsMenuTree);

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(treeSP, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(settingsSubPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnSave)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(settingsSubPanel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave))
                    .addComponent(treeSP, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		if (currentPane != null){
			currentPane.save();
		}
    }//GEN-LAST:event_btnSaveActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
		btnSaveActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSave;
    private ctrmap.stdlib.gui.components.tree.CustomJTree settingsMenuTree;
    private javax.swing.JScrollPane settingsSubPanel;
    private javax.swing.JScrollPane treeSP;
    // End of variables declaration//GEN-END:variables
}
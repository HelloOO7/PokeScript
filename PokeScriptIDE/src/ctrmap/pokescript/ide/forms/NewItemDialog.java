package ctrmap.pokescript.ide.forms;

import ctrmap.pokescript.LangConstants;
import ctrmap.pokescript.ide.system.project.IDEFile;
import xstandard.gui.components.listeners.DocumentAdapterEx;
import xstandard.text.StringEx;
import java.awt.Color;
import javax.swing.event.DocumentEvent;

public class NewItemDialog extends javax.swing.JDialog {

	private IDEFile parentFile;
	private IDEFileItemType type;

	public NewItemDialog(java.awt.Frame parent, boolean modal, IDEFile parentFile, IDEFileItemType type) {
		super(parent, modal);
		this.parentFile = parentFile;
		this.type = type;
		initComponents();
		setLocationRelativeTo(parent);
		setTitle(headingLabel.getText());
		getRootPane().setDefaultButton(btnFinish);

		checkFileNameValidSetWarn();

		fileName.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				btnFinish.setEnabled(checkFileNameValidSetWarn());
			}
		});
	}

	private boolean checkFileNameValidSetWarn() {
		String n = fileName.getText();
		hintLabel.setForeground(Color.BLACK);
		if (n == null || n.isEmpty()) {
			hintLabel.setText("File name can not be empty");
			return false;
		} else {
			char firstNonLetterOrDigit = StringEx.findFirstNonLetterOrDigit(n, '_', ' ', '-', '.');
			if (firstNonLetterOrDigit > 0) {
				hintLabel.setText("File name can not contain the character \"" + firstNonLetterOrDigit + "\"");
				return false;
			}

			String fullFileName = n + (type == IDEFileItemType.CLASS ? LangConstants.LANG_SOURCE_FILE_EXTENSION : "");
			if (parentFile.getChild(fullFileName).exists()) {
				hintLabel.setText("File already exists in directory.");
				return false;
			}

			hintLabel.setForeground(Color.DARK_GRAY);
			if (type == IDEFileItemType.CLASS) {
				if (Character.isLowerCase(n.charAt(0))) {
					hintLabel.setText("Warning: Class names should start with an uppercase letter.");
					return true;
				}
			} else if (type == IDEFileItemType.PACKAGE) {
				if (!n.toLowerCase().equals(n)) {
					hintLabel.setText("Warning: Package names should be all-lowercase.");
					return true;
				}
			}
			if (n.contains(" ")) {
				hintLabel.setText("Warning: File names should not contain spaces.");
			} else {
				hintLabel.setText("");
			}
			return true;
		}
	}
	
	private IDEFile result;
	
	public IDEFile getResult(){
		return result;
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headingLabel = new javax.swing.JLabel();
        separator = new javax.swing.JSeparator();
        nameLabel = new javax.swing.JLabel();
        fileName = new javax.swing.JTextField();
        btnFinish = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        hintLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        headingLabel.setFont(new java.awt.Font("Consolas", 0, 24)); // NOI18N
        headingLabel.setForeground(new java.awt.Color(51, 51, 51));
        headingLabel.setText("Add a new " + (getTypeHeaderName()));

        nameLabel.setText("Name:");

        btnFinish.setText("Finish");
        btnFinish.setEnabled(false);
        btnFinish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinishActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(separator, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileName)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFinish))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(headingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nameLabel))
                        .addGap(0, 79, Short.MAX_VALUE))
                    .addComponent(hintLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hintLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFinish)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
		dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

	private String getExtensionForType() {
		switch (type) {
			case CLASS:
				return LangConstants.LANG_SOURCE_FILE_EXTENSION;
			case HEADER:
			case ENUM:
				return LangConstants.LANG_GENERAL_HEADER_EXTENSION;
			case PACKAGE:
				return "";
		}
		return "";
	}
	
	private String getTypeHeaderName() {
		switch (type) {
			case CLASS:
				return "class";
			case HEADER:
				return "header";
			case PACKAGE:
				return "package";
			case ENUM:
				return "enum";
		}
		return "";
	}
	
    private void btnFinishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinishActionPerformed
		result = parentFile.getChild(fileName.getText() + getExtensionForType());
		dispose();
    }//GEN-LAST:event_btnFinishActionPerformed

	public enum IDEFileItemType {
		CLASS,
		ENUM,
		HEADER,
		PACKAGE
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnFinish;
    private javax.swing.JTextField fileName;
    private javax.swing.JLabel headingLabel;
    private javax.swing.JLabel hintLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JSeparator separator;
    // End of variables declaration//GEN-END:variables
}

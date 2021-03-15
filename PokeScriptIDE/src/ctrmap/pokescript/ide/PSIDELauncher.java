package ctrmap.pokescript.ide;

import ctrmap.pokescript.LangCompiler;
import ctrmap.pokescript.LangPlatform;
import ctrmap.pokescript.instructions.providers.CTRInstructionProvider;
import ctrmap.pokescript.instructions.providers.VInstructionProvider;
import ctrmap.stdlib.fs.accessors.DiskFile;
import java.util.Enumeration;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.UIManager;

public class PSIDELauncher extends javax.swing.JFrame {

	private Preferences lastOptPrefs = Preferences.userRoot().node("CTRMapActionSelector");
	private static final String PLAF_PREF_KEY = "PSIDE_PLATFORM";
	
	public PSIDELauncher() {
		initComponents();
		
		setLocationRelativeTo(null);
		
		int lastPlaf = lastOptPrefs.getInt(PLAF_PREF_KEY, 0);
		
		Enumeration<AbstractButton> btns = plafBtnGrp.getElements();
		
		int idx = 0;
		while (btns.hasMoreElements()){
			AbstractButton btn = btns.nextElement();
			if (idx == lastPlaf){
				plafBtnGrp.setSelected(btn.getModel(), true);
				break;
			}
			idx++;
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        plafBtnGrp = new javax.swing.ButtonGroup();
        pksLogo = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        btnGenVI = new javax.swing.JRadioButton();
        btnGenV = new javax.swing.JRadioButton();
        btnLaunchIDE = new javax.swing.JButton();
        plafSelectLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PokéScript IDE Launcher");
        setResizable(false);

        pksLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ctrmap/resources/scripting/ui/pokescript_logo.png"))); // NOI18N

        welcomeLabel.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        welcomeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        welcomeLabel.setText("Welcome to the PokéScript IDE!");

        plafBtnGrp.add(btnGenVI);
        btnGenVI.setSelected(true);
        btnGenVI.setText("Generation VI");

        plafBtnGrp.add(btnGenV);
        btnGenV.setText("Generation V");

        btnLaunchIDE.setText("Launch");
        btnLaunchIDE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLaunchIDEActionPerformed(evt);
            }
        });

        plafSelectLabel.setText("Please select a platform:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(78, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pksLogo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(welcomeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(78, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLaunchIDE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(125, 125, 125)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plafSelectLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnGenV)
                            .addComponent(btnGenVI))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pksLogo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(welcomeLabel)
                .addGap(7, 7, 7)
                .addComponent(plafSelectLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGenVI)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGenV)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(btnLaunchIDE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLaunchIDEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLaunchIDEActionPerformed
		Enumeration<AbstractButton> btns = plafBtnGrp.getElements();
		
		ButtonModel selected = plafBtnGrp.getSelection();
		
		int idx = 0;
		while (btns.hasMoreElements()){
			AbstractButton btn = btns.nextElement();
			if (btn.getModel() == selected){
				lastOptPrefs.putInt(PLAF_PREF_KEY, idx);
				break;
			}
			idx++;
		}
		
		LangCompiler.CompilerArguments args = new LangCompiler.CompilerArguments();
		DiskFile include = new DiskFile("include");
		if (include.exists()){
			args.includeRoots.add(include);
		}
		
		if (selected == btnGenVI.getModel()){
			args.optimizationPassCount = 2;
			args.setPlatform(LangPlatform.AMX_CTR);
		}
		else if (selected == btnGenV.getModel()){
			args.setPlatform(LangPlatform.EV_SWAN);
		}
		
		PSIDE ide = new PSIDE(args);
		ide.setLocationRelativeTo(this);
		ide.setVisible(true);
		ide.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		dispose();
    }//GEN-LAST:event_btnLaunchIDEActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(PSIDELauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new PSIDELauncher().setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton btnGenV;
    private javax.swing.JRadioButton btnGenVI;
    private javax.swing.JButton btnLaunchIDE;
    private javax.swing.JLabel pksLogo;
    private javax.swing.ButtonGroup plafBtnGrp;
    private javax.swing.JLabel plafSelectLabel;
    private javax.swing.JLabel welcomeLabel;
    // End of variables declaration//GEN-END:variables
}

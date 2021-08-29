package ctrmap.pokescript.ide.autocomplete.gui;

import ctrmap.pokescript.ide.autocomplete.nodes.AbstractNode;
import ctrmap.pokescript.ide.autocomplete.nodes.ClassNode;
import ctrmap.pokescript.ide.autocomplete.nodes.MemberNode;
import ctrmap.pokescript.ide.autocomplete.nodes.PackageNode;
import ctrmap.pokescript.stage0.Modifier;
import ctrmap.stdlib.math.MathEx;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionListener;

public class ACMainWindow extends javax.swing.JWindow {

	private DefaultListModel<AbstractNode> hintModel = new DefaultListModel<>();

	private ACDocWindow doc;

	public ACMainWindow(ACDocWindow subWDoc) {
		initComponents();
		doc = subWDoc;
	}

	public void addAcHintListListener(ListSelectionListener l) {
		hintList.addListSelectionListener(l);
	}

	private List<AbstractNode> oldContent = new ArrayList<>();
	
	public void buildList(List<AbstractNode> content) {
		if (!content.isEmpty() && oldContent.containsAll(content) && content.containsAll(oldContent)){
			return;
		}
		
		List<PackageNode> packages = new ArrayList<>();
		List<ClassNode> classes = new ArrayList<>();
		List<MemberNode> methods = new ArrayList<>();
		List<MemberNode> fields = new ArrayList<>();
		for (AbstractNode n : content) {
			if (n instanceof PackageNode) {
				packages.add((PackageNode)n);
			}
			else if (n instanceof ClassNode) {
				classes.add((ClassNode)n);
			}
			else if (n instanceof MemberNode) {
				MemberNode mn = (MemberNode) n;
				if (mn.member.hasModifier(Modifier.VARIABLE)) {
					fields.add(mn);
				}
				else {
					methods.add(mn);
				}
			}
		}
		Collections.sort(packages);
		Collections.sort(classes);
		Collections.sort(methods);
		Collections.sort(fields);
		
		content.clear();
		content.addAll(packages);
		content.addAll(classes);
		content.addAll(methods);
		content.addAll(fields);
		
		oldContent = content;
		hintModel.clear();
		hintModel.ensureCapacity(content.size());
		for (AbstractNode n : content) {
			hintModel.addElement(n);
		}
		setSize(Math.min(hintList.getPreferredSize().width + listSP.getVerticalScrollBar().getWidth() + 30, 600),
				Math.min(250, hintList.getPreferredSize().height + listSP.getHorizontalScrollBar().getHeight() + 10));
		if (hintModel.isEmpty()) {
			wClose();
		}
	}

	public boolean isHintRoot() {
		if (!hintModel.isEmpty()) {
			return hintModel.getElementAt(0).parent == null;	//This is a shortcut, but it works since all recommended nodes have to be on the same level
		}
		return false;
	}

	public AbstractNode getSelectedNode() {
		return hintList.getSelectedValue();
	}

	public void wOpen() {
		if (!hintModel.isEmpty()) {
			setVisible(true);
		} else {
			wClose();
		}
	}

	public void wClose() {
		doc.wClose();
		setVisible(false);
	}

	public void incrementSelection() {
		hintList.setSelectedIndex(MathEx.clampIntegerFlow(hintList.getSelectedIndex() + 1, 0, hintModel.getSize(), false));
		ensureSelectedVisibility();
	}

	public void decrementSelection() {
		hintList.setSelectedIndex(MathEx.clampIntegerFlow(hintList.getSelectedIndex() - 1, 0, hintModel.getSize(), false));
		ensureSelectedVisibility();
	}

	private void ensureSelectedVisibility() {
		if (hintList.getSelectedIndex() != -1) {
			hintList.ensureIndexIsVisible(hintList.getSelectedIndex());
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

        listSP = new javax.swing.JScrollPane();
        hintList = new javax.swing.JList<>();

        hintList.setModel(hintModel);
        hintList.setCellRenderer(new Renderer());
        listSP.setViewportView(hintList);

        getContentPane().add(listSP, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<AbstractNode> hintList;
    private javax.swing.JScrollPane listSP;
    // End of variables declaration//GEN-END:variables

	private static class Renderer implements ListCellRenderer<AbstractNode> {

		private static final Color SELECTION_COLOR = new Color(0x32, 0x97, 0xFD);

		@Override
		public Component getListCellRendererComponent(JList<? extends AbstractNode> list, AbstractNode value, int index, boolean isSelected, boolean cellHasFocus) {
			ACHintPanel pnl = value.getHintPanel();

			pnl.setOpaque(isSelected);
			pnl.setBackground(isSelected ? SELECTION_COLOR : Color.WHITE);

			return pnl;
		}
	}
}

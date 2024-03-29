/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ctrmap.pokescript.ide.system.project.tree.nodes;

import ctrmap.pokescript.ide.PSIDE;

/**
 *
 */
public class ContainerNode extends IDENodeBase {

	public static final int RESID_OFFSET = 6;
	public static final int RESID_MAX = Type.values().length + RESID_OFFSET;
	
	private Type type;
	
	public ContainerNode(PSIDE ide, Type t){
		super(ide);
		type = t;
	}
	
	@Override
	public int getIconResourceID() {
		return type.getResId();
	}

	@Override
	public String getNodeName() {
		return type.friendlyName;
	}

	@Override
	public String getUniqueName() {
		return getNodeName();
	}

	public static enum Type {
		LIBRARIES("Libraries");
		
		public final String friendlyName;
		
		private Type(String friendlyName){
			this.friendlyName = friendlyName;
		}
		
		public int getResId(){
			return RESID_OFFSET + ordinal();
		}
	}
}

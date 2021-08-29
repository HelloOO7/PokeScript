package ctrmap.pokescript.data;

import ctrmap.pokescript.expr.Throughput;
import ctrmap.pokescript.instructions.abstractcommands.AInstruction;
import ctrmap.pokescript.instructions.abstractcommands.APlainInstruction;
import ctrmap.pokescript.instructions.abstractcommands.APlainOpCode;
import ctrmap.pokescript.stage0.CompilerAnnotation;
import ctrmap.pokescript.stage0.IModifiable;
import ctrmap.pokescript.stage0.Modifier;
import ctrmap.pokescript.stage1.NCompileGraph;
import ctrmap.pokescript.stage1.NExpression;
import ctrmap.pokescript.types.TypeDef;
import java.util.ArrayList;
import java.util.List;

public abstract class Variable implements IModifiable {

	public String name;
	public boolean isNameAbsolute;
	public List<String> aliases = new ArrayList<>();
	public List<Modifier> modifiers = new ArrayList<>();
	public TypeDef typeDef;
	public int index;

	public int timesUsed = 0;

	public abstract VarLoc getLocation();

	public abstract AInstruction getReadIns(NCompileGraph cg);

	public abstract AInstruction getWriteIns(NCompileGraph cg);

	public Variable(String name, List<Modifier> modifiers, TypeDef type, NCompileGraph cg) {
		this.name = name;
		this.modifiers = modifiers;
		this.typeDef = type;
		//System.out.println(typeDef);
	}

	public boolean hasName(String name) {
		return this.name.equals(name) || aliases.contains(name);
	}

	@Override
	public List<Modifier> getModifiers() {
		return modifiers;
	}

	public void setNumeric(int n) {
		index = n;
	}

	public int getSizeOf(NCompileGraph cg) {
		//Classes will always be stored on heap
		return 1;
	}
	
	@Override
	public String toString() {
		return typeDef + " " + name;
	}

	public int getPointer(NCompileGraph g) {
		if (getLocation() == VarLoc.STACK) {
			return (index + (g.provider.getMemoryInfo().isStackOrderNatural() ? 1 : 0)) * g.provider.getMemoryInfo().getStackIndexingStep();
		} else {
			return index * g.provider.getMemoryInfo().getGlobalsIndexingStep();
		}
	}

	public enum VarLoc {
		STACK,
		DATA
	}

	public static class Global extends Variable {

		public List<AInstruction> init_from = new ArrayList<>();
		public List<CompilerAnnotation> annotations = new ArrayList<>();

		public Global(String name, List<Modifier> modifiers, TypeDef type, NExpression init_from, NCompileGraph cg) {
			super(name, modifiers, type, cg);
			if (init_from != null) {
				Throughput iptp = init_from.toThroughput(cg);
				if (iptp != null) {
					this.init_from.addAll(iptp.getCode(type));
					optimizeInitFrom(cg);
				}
			}
		}

		public Global(String name, List<Modifier> modifiers, TypeDef type, NCompileGraph cg, int value) {
			super(name, modifiers, type, cg);
			init_from.add(cg.getPlain(APlainOpCode.CONST_PRI, value));
		}

		public final void optimizeInitFrom(NCompileGraph cg) {
			if (init_from.size() == 2) {
				AInstruction p_ins0 = init_from.get(0);
				AInstruction p_ins1 = init_from.get(1);
				if (p_ins0 instanceof APlainInstruction && p_ins1 instanceof APlainInstruction) {
					APlainInstruction ins0 = (APlainInstruction) p_ins0;
					APlainInstruction ins1 = (APlainInstruction) p_ins1;
					if (ins0.opCode == APlainOpCode.CONST_PRI && ins1.opCode == APlainOpCode.NEGATE) {
						init_from.clear();
						init_from.add(cg.getPlain(APlainOpCode.CONST_PRI, -ins0.getArgument(0)));
					}
				}
			}
		}

		public boolean isImmediate() {
			if (init_from.isEmpty()) {
				return true; //uninitialized
			}
			if (init_from.size() == 1) {
				AInstruction ins = init_from.get(0);
				if (ins instanceof APlainInstruction) {
					APlainOpCode cmd = ((APlainInstruction) ins).opCode;
					if (cmd == APlainOpCode.CONST_PRI) {
						return true;
					}
				}
			}
			return false;
		}

		public int getImmediateValue() {
			if (isImmediate()) {
				return ((APlainInstruction) init_from.get(0)).getArgument(0);
			}
			return 0;
		}

		@Override
		public VarLoc getLocation() {
			return VarLoc.DATA;
		}

		@Override
		public AInstruction getReadIns(NCompileGraph cg) {
			if (hasModifier(Modifier.FINAL) && isImmediate()) {
				return cg.getPlain(APlainOpCode.CONST_PRI, getImmediateValue());
			}
			return cg.provider.getGlobalRead(name);
		}

		@Override
		public AInstruction getWriteIns(NCompileGraph cg) {
			return cg.provider.getGlobalWrite(name);
		}
	}

	public static class Local extends Variable {

		public Local(String name, List<Modifier> modifiers, TypeDef type, NCompileGraph cg) {
			super(name, modifiers, type, cg);
		}

		@Override
		public VarLoc getLocation() {
			return VarLoc.STACK;
		}

		@Override
		public AInstruction getReadIns(NCompileGraph cg) {
			return cg.getPlain(APlainOpCode.LOAD_STACK_PRI, getPointer(cg));
		}

		@Override
		public AInstruction getWriteIns(NCompileGraph cg) {
			return cg.getPlain(APlainOpCode.STORE_PRI_STACK, getPointer(cg));
		}
	}
}

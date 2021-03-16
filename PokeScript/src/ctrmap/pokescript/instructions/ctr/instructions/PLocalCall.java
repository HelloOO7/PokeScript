package ctrmap.pokescript.instructions.ctr.instructions;

import ctrmap.scriptformats.gen6.PawnInstruction;
import ctrmap.pokescript.FloatLib;
import ctrmap.pokescript.OutboundDefinition;
import ctrmap.pokescript.instructions.abstractcommands.ACompiledInstruction;
import ctrmap.pokescript.instructions.abstractcommands.AInstruction;
import ctrmap.pokescript.types.DataType;
import java.util.ArrayList;
import java.util.List;
import ctrmap.pokescript.instructions.abstractcommands.ALocalCall;
import ctrmap.pokescript.stage1.NCompilableMethod;
import ctrmap.pokescript.stage1.NCompileGraph;
import ctrmap.pokescript.stage1.NExpression;
import ctrmap.stdlib.util.ArraysEx;

public class PLocalCall extends ALocalCall {

	public PLocalCall(OutboundDefinition def) {
		super(def);
	}

	@Override
	public int getAllocatedPointerSpace(NCompileGraph cg) {
		//call = 8
		//args = nArgs * 4
		int ptr = 0;
		List<? extends ACompiledInstruction> precomp = compile(cg);
		for (ACompiledInstruction i : precomp) {
			ptr += i.getSize();
		}
		return ptr;
	}

	protected int getArgArrayBytes() {
		return getArgCount() * 4;
	}

	@Override
	public List<PawnInstruction> compile(NCompileGraph g) {
		List<PawnInstruction> r = new ArrayList<>();

		NCompilableMethod m = g.getMethodByDef(call);

		//push argument count so that the AM knows what to subtract from the stack afterwards
		PawnInstruction argCount = new PawnInstruction(PawnInstruction.Commands.PUSH_C, getArgArrayBytes());
		r.add(0, argCount);

		for (int i = 0; i < getArgCount(); i++) {
			r.add(0, new PawnInstruction(PawnInstruction.Commands.PUSH_PRI));

			//in case of floating point requirement, convert integers to floats
			if (m.def.args[i].typeDef.baseType == DataType.FLOAT && call.args[i].type.getBaseType() == DataType.INT) {
				if (!call.args[i].isImmediate() || call.args[i].getImmediateValue() != 0) {//don't need to cast 0 to a float
					r.addAll(0, getFloatConversionInstructions(g));
				}
			}

			r.addAll(0, CTRInstruction.compileIL(call.args[i].getCode(DataType.ANY), g));
		}

		int ptr = pointer;
		for (PawnInstruction i : r) {
			ptr += i.getSize();
		}

		PawnInstruction callIns = new PawnInstruction(PawnInstruction.Commands.CALL, m.getPointer() - ptr);
		r.add(callIns);

		return r;
	}

	protected static List<PawnInstruction> getFloatConversionInstructions(NCompileGraph g) {
		List<PawnInstruction> r = new ArrayList<>();
		g.addNative(FloatLib._float);
		g.addLibrary(FloatLib.LIBRARY_NAME);
		r.add(new PawnInstruction(PawnInstruction.Commands.PUSH_PRI)); //pushes the integer
		r.add(new PawnInstruction(PawnInstruction.Commands.SYSREQ_N, g.getNativeIdx(FloatLib._float.name), 4)); //calls the sysreq with the argument pushed here vvv
		//the float itself gets pushed by the push_pri at the end of the method
		return r;
	}
}

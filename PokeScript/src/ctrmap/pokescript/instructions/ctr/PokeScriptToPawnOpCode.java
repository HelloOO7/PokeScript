package ctrmap.pokescript.instructions.ctr;

import ctrmap.pokescript.instructions.abstractcommands.APlainOpCode;
import ctrmap.scriptformats.gen6.PawnOpCode;

public class PokeScriptToPawnOpCode {

	public static PawnOpCode getDirectConvOpCode(APlainOpCode plain) {
		switch (plain) {
			case ABORT_EXECUTION:
				return PawnOpCode.HALT;
			case BEGIN_METHOD:
				return PawnOpCode.PROC;
			case CONST_ALT:
				return PawnOpCode.CONST_ALT;
			case CONST_PRI:
				return PawnOpCode.CONST_PRI;
			case POP_ALT:
				return PawnOpCode.PALT;
			case POP_PRI:
				return PawnOpCode.PPRI;
			case PUSH_ALT:
				return PawnOpCode.PUSH_ALT;
			case PUSH_PRI:
				return PawnOpCode.PUSH_PRI;
			case RESIZE_STACK:
				return PawnOpCode.STACK;
			case RETURN:
				return PawnOpCode.RETN;
			case ZERO_ALT:
				return PawnOpCode.ZERO_ALT;
			case ZERO_PRI:
				return PawnOpCode.ZERO_PRI;
			case ADD:
				return PawnOpCode.ADD;
			case AND:
				return PawnOpCode.AND;
			case DIVIDE:
				return PawnOpCode.SDIV/*_ALT*/;
			case EQUAL:
				return PawnOpCode.EQ;
			case GEQUAL:
				return PawnOpCode.SGEQ;
			case GREATER:
				return PawnOpCode.SGRTR;
			case LEQUAL:
				return PawnOpCode.SLEQ;
			case LESS:
				return PawnOpCode.SLESS;
			case MOVE_PRI_TO_ALT:
				return PawnOpCode.MOVE_ALT;
			case MOVE_ALT_TO_PRI:
				return PawnOpCode.MOVE_PRI;
			case MULTIPLY:
				return PawnOpCode.SMUL;
			case MODULO:
				return PawnOpCode.SDIV/*_ALT*/;
			case NEGATE:
				return PawnOpCode.NEG;
			case NEQUAL:
				return PawnOpCode.NEQ;
			case NOT:
				return PawnOpCode.NOT;
			case OR:
				return PawnOpCode.OR;
			case SUBTRACT:
				return PawnOpCode.SUB/*_ALT*/;
			case XOR:
				return PawnOpCode.XOR;
			case SHL:
				return PawnOpCode.SHL;
			case SHR:
				return PawnOpCode.SHR;
			case SHL_C:
				return PawnOpCode.SHL_C_PRI;
			case SHR_C:
				return PawnOpCode.SHR_C_PRI;
			case JUMP:
				return PawnOpCode.JUMP;
			case JUMP_IF_ZERO:
				return PawnOpCode.JZER;
			case SWITCH:
				return PawnOpCode.SWITCH;
		}
		return null;
	}
}

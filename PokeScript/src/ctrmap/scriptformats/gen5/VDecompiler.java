package ctrmap.scriptformats.gen5;

import ctrmap.pokescript.instructions.gen5.VCmpResultRequest;
import ctrmap.pokescript.instructions.gen5.VOpCode;
import ctrmap.pokescript.instructions.ntr.NTRArgument;
import ctrmap.pokescript.instructions.ntr.NTRDataType;
import ctrmap.pokescript.instructions.ntr.NTRInstructionLink;
import ctrmap.scriptformats.gen5.disasm.DisassembledCall;
import ctrmap.scriptformats.gen5.disasm.DisassembledMethod;
import ctrmap.scriptformats.gen5.disasm.StackCommands;
import ctrmap.scriptformats.gen5.disasm.StackTracker;
import ctrmap.scriptformats.gen5.disasm.VDisassembler;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.gui.FormattingUtils;
import ctrmap.stdlib.io.util.IndentedPrintStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VDecompiler {

	private VScriptFile scr;

	private VDisassembler disasm;

	private StackTracker stack = new StackTracker();

	public VDecompiler(VScriptFile scr, VCommandDataBase cdb) {
		this.scr = scr;
		disasm = new VDisassembler(scr, cdb);
	}

	public static void main(String[] args) {		
		FSFile scrFile = new DiskFile("D:\\_REWorkspace\\CTRMapProjects\\BW2_CTRMap\\vfs\\data\\a\\0\\5\\6\\904");
		FSFile cdbFile = new DiskFile("C:\\Users\\Čeněk\\eclipse-workspace\\BsYmlGen\\B2W2.yml");
		FSFile outFile = new DiskFile("D:\\_REWorkspace\\pokescript_genv\\decomp\\out.pks");

		VDecompiler dec = new VDecompiler(new VScriptFile(scrFile), new VCommandDataBase(cdbFile));
		dec.decompileToFile(outFile);
	}

	public void decompile() {
		disasm.disassemble();
	}

	public void decompileToFile(FSFile fsf) {
		decompile();
		fsf.setBytes(dump().getBytes(StandardCharsets.UTF_8));
	}

	public String dump() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IndentedPrintStream out = new IndentedPrintStream(baos);

		List<String> imports = new ArrayList<>();

		for (DisassembledMethod m : disasm.methods) {
			for (DisassembledCall c : m.instructions) {
				if (c.command.isDecompPrintable()) {
					String imp = c.command.classPath;
					if (!imports.contains(imp)) {
						imports.add(imp);
					}
				}
				if (c.command.def.opCode == VOpCode.PushEventFlag.ordinal()) {
					String imp = "system.EventFlags";
					if (!imports.contains(imp)) {
						imports.add(imp);
					}
				}
				for (int i = 0; i < c.args.length; i++) {
					int av = c.args[i];
					NTRArgument ad = c.definition.parameters[i];

					if (ad.dataType == NTRDataType.VAR || ad.dataType == NTRDataType.FLEX) {
						if (av >= 0x4000 && av < 0x8000) {
							String imp = "system.EventWorks";
							if (!imports.contains(imp)) {
								imports.add(imp);
							}
						}
					}
				}
			}
		}

		Collections.sort(imports);
		for (String imp : imports) {
			out.print("import ");
			out.print(imp);
			out.println(";");
		}

		if (!imports.isEmpty()) {
			out.println();
		}

		out.print("public class ");
		out.print(FormattingUtils.getStrWithoutSpaces(FSUtil.getFileNameWithoutExtension(scr.getSourceFile().getName())));
		out.println(" {");
		out.incrementIndentLevel();

		for (DisassembledMethod m : disasm.methods) {
			if (m.isPublic) {
				dumpMethod(m, out);
			}
		}

		for (DisassembledMethod m : disasm.methods) {
			if (!m.isPublic) {
				//publics are dumped explicitly to preserve order
				dumpMethod(m, out);
			}
		}

		for (DisassembledMethod m : disasm.movements) {
			dumpMovement(m, out);
		}

		out.decrementIndentLevel();
		out.println("}");

		out.close();
		return new String(baos.toByteArray());
	}

	private void dumpMethod(DisassembledMethod m, IndentedPrintStream out) {
		if (m.isPublic) {
			out.print("public ");
		}
		out.print("static void ");
		out.print(m.getName());
		out.println("() {");
		out.incrementIndentLevel();

		List<Integer> usedRegisters = new ArrayList<>();
		for (DisassembledCall call : m.instructions) {
			for (int i = 0; i < call.args.length; i++) {
				int av = call.args[i];
				NTRArgument ad = call.definition.parameters[i];

				if (ad.dataType == NTRDataType.VAR || ad.dataType == NTRDataType.FLEX) {
					if (av >= 0x8000) {
						if (!usedRegisters.contains(av)) {
							usedRegisters.add(av);
						}
					}
				}
			}
		}
		Collections.sort(usedRegisters);

		for (int r : usedRegisters) {
			out.print("int v");
			out.print(r - 0x8000);
			out.println(";");
		}

		if (!usedRegisters.isEmpty()) {
			out.println();
		}

		for (int h = 0; h < m.instructions.size(); h++) {
			DisassembledCall call = m.instructions.get(h);

			handleInstruction(call, h, m, out);
		}

		out.decrementIndentLevel();
		out.println("}");

		out.println();
	}

	private boolean isJumpLabelUsed(DisassembledCall call, DisassembledMethod method) {
		for (DisassembledCall c : method.instructions) {
			if (c.link != null && c.link.target == call) {
				if (!call.ignoredLinks.contains(c.link)) {
					return true;
				}
				else {
					System.out.println("Label " + call.label + " used, but ignored.");
				}
			}
		}
		return false;
	}

	private void handleInstruction(DisassembledCall call, int insIndex, DisassembledMethod method, IndentedPrintStream out) {
		if (call.doNotDisassemble) {
			return;
		}

		StackCommands stackResult = stack.handleInstruction(call);

		if (call.label != null) {
			if (isJumpLabelUsed(call, method)) {
				if (!getIsPointlessToPrintLabel(call)) {
					out.println();
					out.print(call.label);
					out.println(":");
				} else {
					System.out.println("Pointless to label: " + call.label);
				}
			} else {
				System.out.println("Unused label: " + call.label);
			}
		}

		if (stackResult != null) {
			switch (stackResult) {
				case POP_TO:
					printFlex(call.args[0], out);
					out.print(" = ");
					StackTracker.StackElement elem = stack.pop();
					elem.print(out);
					out.println(";");
					break;
			}
		} else if (call.command.type == VCommandDataBase.CommandType.REGULAR) {
			if (!call.command.isDecompPrintable()) {
				call.doNotDisassemble = true;
				return;
			}

			out.print(call.command.getPKSCallName());
			out.print("(");

			for (int i = 0; i < call.args.length; i++) {
				if (i != 0) {
					out.print(", ");
				}
				int av = call.args[i];
				NTRArgument ad = call.definition.parameters[i];

				if (call.link != null && i == call.link.argIdx) {
					out.print(((DisassembledCall) (call.link.target)).label);
				} else {
					switch (ad.dataType) {
						case VAR:
						case FLEX:
							printFlex(av, out);
							break;
						case U16:
						case U8:
						case S32:
							out.print(av);
							break;
						case FX16:
						case FX32:
							out.print(av / 4096f);
							break;
					}
				}
			}

			out.println(");");

		} else {
			printIrregularCall(call, method, insIndex, out);
		}
		call.doNotDisassemble = true;
	}

	private boolean getCanBlockBeTornOut(DisassembledCall blockCaller, DisassembledMethod method) {
		DisassembledCall target = descendToJumpOrigin(blockCaller);
		if (target != null) {
			int linkCount = 0;
			for (DisassembledCall c : method.instructions){
				if (c.link != null && c.link.target != null && c.link.target == target){
					linkCount++;
					if (linkCount > 1){
						return false;
					}
				}
			}
			
			if (target.pointer > blockCaller.pointer) {
				int idx = method.instructions.indexOf(target) - 1;
				if (idx >= 0) {
					DisassembledCall before = descendToJumpOrigin(method.instructions.get(idx));
					if (before.command.isBranchEnd() || method.instructions.get(idx).command.isBranchEnd()) {
						return true;
					} else {
						//System.out.println("Can not tear out " + Integer.toHexString(target.pointer) + "(" + target.command.name +  ") not branch end " + before.command.name + "(" + Integer.toHexString(before.pointer) + ")");
					}
				}
			}
		}
		return false;
	}

	private List<DisassembledCall> tearOutBlock(DisassembledCall blockCaller, DisassembledMethod method) {
		List<DisassembledCall> instructions = new ArrayList<>();

		DisassembledCall target = descendToJumpOrigin(blockCaller);

		for (int h = method.instructions.indexOf(target); h < method.instructions.size(); h++) {
			DisassembledCall c = method.instructions.get(h);

			boolean end;

			if (c.command.type == VCommandDataBase.CommandType.JUMP && !c.command.isConditional) {
				end = true;
				instructions.add(c);
				break;
			} else {
				if (c.label != null) {
					end = false;

					for (DisassembledCall caller : method.instructions) {
						if (caller != blockCaller && caller.link != null && caller.link.target == c) {
							if (caller.pointer > c.pointer || caller.pointer < target.pointer) {
								end = true;
								break;
							}
						}
					}
				} else {
					end = false;
				}
			}

			if (end) {
				DisassembledCall jumpToNext = new DisassembledCall(-1, VOpCode.Jump.proto, 0);
				jumpToNext.command = new VCommandDataBase.VCommand("jump-dummy", jumpToNext.definition, VCommandDataBase.CommandType.JUMP, false, false);
				jumpToNext.link = new NTRInstructionLink(jumpToNext, c, 0);
				instructions.add(jumpToNext);
				break;
			} else {
				instructions.add(c);
			}
		}
		target.ignoredLinks.add(blockCaller.link);

		return instructions;
	}

	private boolean getIsPointlessToPrintLabel(DisassembledCall call) {
		//Those will be auto-copied to their respective places in the decompiler
		call = descendToJumpOrigin(call);
		return call.command.type == VCommandDataBase.CommandType.HALT || call.command.type == VCommandDataBase.CommandType.RETURN;
	}

	private void printIrregularCall(DisassembledCall call, DisassembledMethod method, int insIndex, IndentedPrintStream out) {
		switch (call.command.type) {
			case MOVEMENT_JUMP:
				out.print(((DisassembledCall) call.link.target).label);
				out.print("(");
				printFlex(call.args[0], out);
				out.println(");");
				break;
			case HALT:
				if (!method.isPublic) {
					out.println("pause;");
					break;
				}
			//fall through
			case RETURN:
				if (method.ptr == -1 || insIndex != method.instructions.size() - 1) {
					out.println("return;");
				}
				break;
			case CALL:
			case JUMP:
				if (call.command.isConditional) {
					out.print("if (");

					int cmpReq = call.args[0];

					if (cmpReq != VCmpResultRequest.STACK_RESULT) {
						String lhs = null;
						String rhs = null;

						DisassembledCall condSrc = null;
						for (int j = insIndex; j >= 0; j--) {
							if (method.instructions.get(j).command.setsCmpFlag) {
								condSrc = method.instructions.get(j);
								break;
							}
						}
						if (condSrc != null) {

							int opCode = condSrc.definition.opCode;

							boolean isGlobal = opCode < VOpCode.CmpVarConst.ordinal();
							if (!isGlobal) {
								lhs = flex2Str(condSrc.args[0]);
							} else {
								lhs = "g_" + condSrc.args[0];
							}
							if (opCode == VOpCode.CmpVMVarConst.ordinal() || opCode == VOpCode.CmpVarConst.ordinal()) {
								rhs = String.valueOf(condSrc.args[1]);
							} else {
								rhs = isGlobal ? "g_" + condSrc.args[1] : flex2Str(condSrc.args[1]);
							}

							out.print(lhs);
							out.print(" ");
							out.print(VCmpResultRequest.getOpStr(cmpReq));
							out.print(" ");
							out.print(rhs);
						} else {
							out.print("false");
						}
					} else {
						StackTracker.StackElement result = stack.pop();

						if (result != null) {
							result.print(out);
						} else {
							out.print("false");
						}
					}

					out.println(") {");
					out.incrementIndentLevel();
				}

				if (call.link == null) {
					System.err.println("Link error at " + call.command.name + "(" + Integer.toHexString(call.pointer) + ")");
					out.println("goto [LINK ERROR];");
				} else {
					String targetLabel = ((DisassembledCall) call.link.target).label;
					if (call.command.type == VCommandDataBase.CommandType.CALL) {
						out.print(targetLabel);
						out.println("();");
					} else {
						DisassembledCall trueTarget = descendToJumpOrigin(call);

						if (!getIsPointlessToGoto(trueTarget, insIndex, method)) {
							switch (trueTarget.command.type) {
								case HALT:
								case RETURN:
									printIrregularCall((DisassembledCall) call.link.target, method, insIndex, out);
									break;
								default:
									if (getCanBlockBeTornOut(call, method)) {
										List<DisassembledCall> tornOut = tearOutBlock(call, method);

										DisassembledMethod dummyMethod = new DisassembledMethod(-1);
										dummyMethod.instructions.addAll(tornOut);
										dummyMethod.isPublic = method.isPublic;

										for (int h2 = 0; h2 < dummyMethod.instructions.size(); h2++) {
											handleInstruction(dummyMethod.instructions.get(h2), h2, dummyMethod, out);
										}
									} else {
										out.print("goto ");
										out.print(targetLabel);
										out.println(";");
									}
									break;
							}
						}
					}
				}

				if (call.command.isConditional) {
					out.decrementIndentLevel();
					out.println("}");
				}
				break;
		}
	}

	private boolean getIsPointlessToGoto(DisassembledCall trueTarget, int insIndex, DisassembledMethod method) {
		for (insIndex++; insIndex < method.instructions.size(); insIndex++) {
			DisassembledCall ins = method.instructions.get(insIndex);

			if (!ins.doNotDisassemble) {
				if (ins != trueTarget) {
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}

	private DisassembledCall descendToJumpOrigin(DisassembledCall jump) {
		if (jump.link == null) {
			return jump;
		}
		DisassembledCall target = (DisassembledCall) jump.link.target;
		if (target == null || target.command == null) {
			return jump;
		}

		if (target.command.type == VCommandDataBase.CommandType.JUMP && !target.command.isConditional) {
			target = descendToJumpOrigin(target);
		}

		return target;
	}

	private void printFlex(int av, PrintStream out) {
		out.print(flex2Str(av));
	}

	public static String flex2Str(int av) {
		if (av < 0x8000) {
			if (av >= 0x4000) {
				return "EventWorks.WorkGet(" + (av /*- 0x4000*/) + ")";
			}
			return String.valueOf(av);
		} else {
			return "v" + (av - 0x8000);
		}
	}

	private void dumpMovement(DisassembledMethod m, IndentedPrintStream out) {
		out.print("static meta void ");
		out.print(m.getName());
		out.println("(int npcId) : VMovementFunc {");
		out.incrementIndentLevel();

		for (DisassembledCall call : m.instructions) {
			out.print("Movement");
			out.print(FormattingUtils.getStrWithLeadingZeros(4, Integer.toHexString(call.definition.opCode)));
			out.print("(");
			out.print(call.args[0]);
			out.println(");");
		}

		out.decrementIndentLevel();
		out.println("}");

		out.println();
	}
}
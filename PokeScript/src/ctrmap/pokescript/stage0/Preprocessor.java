package ctrmap.pokescript.stage0;

import ctrmap.pokescript.CompilerExceptionData;
import ctrmap.pokescript.CompilerLogger;
import ctrmap.pokescript.InboundDefinition;
import ctrmap.pokescript.LangCompiler;
import ctrmap.pokescript.LangConstants;
import ctrmap.pokescript.MemberDocumentation;
import ctrmap.pokescript.stage0.content.AbstractContent;
import ctrmap.pokescript.stage0.content.DeclarationContent;
import ctrmap.pokescript.stage1.NCompilableMethod;
import ctrmap.pokescript.stage1.NCompileGraph;
import ctrmap.pokescript.types.classes.ClassDefinition;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.util.ArraysEx;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Preprocessor {

	private List<EffectiveLine> lines = new ArrayList<>();
	private List<CommentDump> comments = new ArrayList<>();
	public List<FSFile> include = new ArrayList<>();

	private CompilerLogger log;
	private LangCompiler.CompilerArguments args;
	
	public String contextName = "UnnamedContext";
	public NCompileGraph parentGraph;
	public NCompileGraph cg;

	public Preprocessor(FSFile file, LangCompiler.CompilerArguments args, NCompileGraph parentGraph) {
		this(file.getInputStream(), file.getName(), args);
		this.parentGraph = parentGraph;
	}

	public Preprocessor(FSFile file, LangCompiler.CompilerArguments args) {
		this(file.getInputStream(), file.getName(), args);
	}

	public Preprocessor(InputStream stream, String contextName, LangCompiler.CompilerArguments args) {
		log = args.logger;
		this.contextName = contextName;
		include = args.includeRoots;
		this.args = args;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			int line = 1;

			EffectiveLine.AnalysisState state = new EffectiveLine.AnalysisState();
			EffectiveLine.PreprocessorState ppState = new EffectiveLine.PreprocessorState();
			ppState.defined = args.preprocessorDefinitions;

			while (reader.ready()) {
				EffectiveLine l = readLine(line, reader, LangConstants.COMMON_LINE_TERM);
				//System.out.print(l.data);
				line += l.newLineCount;
				l.trim();
				l.analyze0(state);
				if (l.hasType(EffectiveLine.LineType.PREPROCESSOR_COMMAND)) {
					l.analyze1(state);

					new TextPreprocessorCommandReader(l, log).processState(ppState);
					lines.add(l);
				} else {
					if (ppState.getIsCodePassthroughEnabled()) {
						lines.add(l);
						l.analyze1(state);
					}
				}
			}
			if (!ppState.ppStack.empty()){
				if (!lines.isEmpty()){
					lines.get(lines.size() - 1).throwException("Unclosed preprocessor condition. (Count: " + ppState.ppStack.size() + ")");
				}
			}
			args.pragmata.putAll(ppState.pragmata);
		} catch (IOException ex) {
			Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static String getStrWithoutTerminator(String s) {
		for (char term : LangConstants.COMMON_LINE_TERM) {
			if (s.endsWith(String.valueOf(term))) {
				return s.substring(0, s.length() - 1);
			}
		}
		return s;
	}

	public CommentDump getCommentBeforeLine(int line) {
		EffectiveLine lastLine = null;
		int lmax = -1;
		for (EffectiveLine l : lines) {
			int endl = l.startingLine;
			if (endl < line) {
				if (endl < lmax) {
					continue;
				}
				lmax = endl;
				lastLine = l;
			} else {
				break;
			}
		}
		int lineReq = lastLine != null ? lmax : line - 1;
		//System.err.println(contextName);
		//System.err.println("req " + lineReq + ", " + line);
		for (CommentDump cd : comments) {
			if (cd.endLine >= lineReq && cd.endLine <= line) {
				return cd;
			}
		}
		return null;
	}

	public List<NMember> getMembers() {
		return getMembers(true);
	}

	public List<NMember> getMembers(boolean localOnly) {
		if (!localOnly) {
			if (cg == null) {
				getCompileGraph();
			}
			if (cg == null) {
				return new ArrayList<>();
			}
		}
		List<NMember> members = new ArrayList<>();
		for (EffectiveLine el : lines) {
			if (el.content.getContentType() == AbstractContent.CompilerContentType.DECLARATION && el.context != EffectiveLine.AnalysisLevel.LOCAL) {
				DeclarationContent decCnt = (DeclarationContent) el.content;
				CommentDump cmt = getCommentBeforeLine(el.startingLine);
				NMember n = new NMember();
				n.modifiers = decCnt.declaredModifiers;
				n.type = decCnt.declaredType;
				n.doc = cmt != null ? new MemberDocumentation(cmt.contents) : null;
				if (decCnt.isMethodDeclaration()) {
					NCompilableMethod m = decCnt.getMethod();

					if (localOnly) {
						n.name = m.def.name;
					} else {
						n.name = cg.appliedNamespace == null ? m.def.name : cg.appliedNamespace + "." + m.def.name;
					}
					n.args = m.def.args;
				} else {
					if (localOnly) {
						n.name = decCnt.declaredName;
					} else {
						n.name = cg.appliedNamespace == null ? decCnt.declaredName : cg.appliedNamespace + "." + decCnt.declaredName;
					}
				}
				members.add(n);
			}
		}

		if (!localOnly) {
			for (Preprocessor sub : cg.includedReaders) {
				members.addAll(sub.getMembers());
			}
		}

		return members;
	}

	public List<InboundDefinition> getDeclaredMethods() {
		List<InboundDefinition> l = new ArrayList<>();
		for (EffectiveLine el : lines) {
			if (el.content != null && el.content.getContentType() == AbstractContent.CompilerContentType.DECLARATION && el.context != EffectiveLine.AnalysisLevel.LOCAL) {
				DeclarationContent decCnt = (DeclarationContent) el.content;
				if (decCnt.isMethodDeclaration()) {
					InboundDefinition def = decCnt.getMethod().def;
					l.add(def);
				}
			}
		}
		return l;
	}

	public List<String> getDeclaredFields() {
		List<String> l = new ArrayList<>();
		for (EffectiveLine el : lines) {
			if (el.content.getContentType() == AbstractContent.CompilerContentType.DECLARATION && el.context == EffectiveLine.AnalysisLevel.GLOBAL) {
				DeclarationContent decCnt = (DeclarationContent) el.content;
				if (decCnt.isVarDeclaration()) {
					l.add(decCnt.declaredName);
				}
			}
		}
		return l;
	}

	public NCompileGraph getCompileGraph() {
		cg = new NCompileGraph(args);
		if (parentGraph != null) {
			cg.merge(parentGraph);
		}
		cg.includePaths = include;

		cg.methodHeaders = getDeclaredMethods();
		List<CompilerExceptionData> exc;

		for (EffectiveLine line : lines) {
			cg.currentCompiledLine = line;
			if (line.exceptions.isEmpty() && line.content != null) {
				line.content.addToGraph(cg);
				if (line.hasType(EffectiveLine.LineType.BLOCK_END) && line.context == EffectiveLine.AnalysisLevel.LOCAL) {
					cg.popBlock();
				}
			}
		}
		exc = collectExceptions();

		for (CompilerExceptionData d : exc) {
			log.println(CompilerLogger.LogLevel.ERROR, d.toString());
		}
		if (!exc.isEmpty()) {
			return null;
		}

		return cg;
	}

	public List<CompilerExceptionData> collectExceptions() {
		List<CompilerExceptionData> d = new ArrayList<>();
		for (EffectiveLine l : lines) {
			d.addAll(l.getExceptionData());
		}
		return d;
	}

	public static boolean isTerminator(char c) {
		return isTerminator(c, false);
	}

	public static boolean isTerminator(char c, boolean allowNewLine) {
		if (allowNewLine) {
			if (c == '\n') {
				return true;
			}
		}
		for (Character chara : LangConstants.COMMON_LINE_TERM) {
			if (chara == c) {
				return true;
			}
		}
		return false;
	}

	public static BraceContent getContentInBraces(String source, int firstBraceIndex) {
		int braceLevel = 0;
		StringBuilder sb = new StringBuilder();
		BraceContent cnt = new BraceContent();
		for (int idx = firstBraceIndex; idx < source.length(); idx++) {
			char c = source.charAt(idx);
			if (c == '(') {
				braceLevel++;
			} else if (c == ')') {
				braceLevel--;
			}
			sb.append(c);
			if (braceLevel == 0) {
				cnt.hasIntegrity = true;
				cnt.endIndex = idx + 1;
				break;
			}
		}
		cnt.content = sb.toString();
		return cnt;
	}

	public static boolean checkNameValidity(String name) {
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (!Character.isLetterOrDigit(c) && !LangConstants.allowedNonAlphaNumericNameCharacters.contains(c)) {
				return false;
			}
		}
		return true;
	}

	public static char safeCharAt(String str, int idx) {
		if (idx < str.length()) {
			return str.charAt(idx);
		}
		return 0;
	}

	private EffectiveLine readLine(int line, Reader reader, Character... terminators) throws IOException {
		List<Character> termList = ArraysEx.asList(terminators);
		char c;
		StringBuilder unfiltered = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		StringBuilder commentSB = new StringBuilder();

		boolean notifyNextComment = false;
		boolean isInComment = false;
		CommentDump cd = new CommentDump();
		String commentTerm = null;

		boolean firstChar = true;
		boolean isCommentBegin = false;
		int beginCommentLines = 0;

		int lineAccumulator = 0;
		int blevel = 0;
		int charIndex = -1;

		while (reader.ready()) {
			c = (char) reader.read();

			if (firstChar) {
				if (c == LangConstants.CH_PP_KW_IDENTIFIER || c == LangConstants.CH_ANNOT_KW_IDENTIFIER) {
					termList.add('\n');
				}
				if (!Character.isWhitespace(c)) {
					firstChar = false;
				}
			}
			if (!firstChar) {
				charIndex++;
			}
			if (notifyNextComment) {
				switch (c) {
					case '*':
						commentTerm = "*/";
						isInComment = true;
						break;
					case '/':
						commentTerm = "\n";
						isInComment = true;
						break;
				}
			}
			if (notifyNextComment && isInComment) {
				if (charIndex <= 1) {
					isCommentBegin = true;
				}
				sb.deleteCharAt(sb.length() - 1);
				cd.startingLine = line + lineAccumulator;
			}
			notifyNextComment = false;
			switch (c) {
				case '\n':
					lineAccumulator++;
					if (isCommentBegin) {
						beginCommentLines++;
					}
					break;
			}
			if (!isInComment) {
				switch (c) {
					case '(':
						blevel++;
						break;
					case ')':
						blevel--;
						break;
					case '/':
						notifyNextComment = true;
						break;
					default:
						break;
				}
			}

			if (isInComment) {
				if (unfiltered.toString().endsWith(commentTerm)) {
					cd.endLine = line + lineAccumulator;
					cd.contents = commentSB.toString();
					commentSB = new StringBuilder();
					comments.add(cd);
					cd = new CommentDump();

					isInComment = false;
					isCommentBegin = false;
				}
			}
			if (!isInComment) {
				sb.append(c);
			} else {
				commentSB.append(c);
			}

			unfiltered.append(c);
			if (!isInComment && blevel == 0 && termList.contains(c)) {
				break;
			}
		}
		EffectiveLine l = new EffectiveLine();
		l.startingLine = line;
		l.startingLine += beginCommentLines;
		l.fileName = contextName;
		l.data = sb.toString();
		l.newLineCount = lineAccumulator;
		return l;
	}
}
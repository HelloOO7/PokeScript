package ctrmap.pokescript.stage0.content;

import ctrmap.pokescript.expr.Throughput;
import ctrmap.pokescript.stage0.Preprocessor;
import ctrmap.pokescript.stage0.EffectiveLine;
import ctrmap.pokescript.stage1.NCompilableMethod;
import ctrmap.pokescript.stage1.NCompileGraph;
import ctrmap.pokescript.stage1.NExpression;
import ctrmap.pokescript.types.DataType;

public class ExpressionContent extends AbstractContent {

	private EffectiveLine src;
	public String trimmedData;

	public ExpressionContent(EffectiveLine source) {
		src = source;
		trimmedData = Preprocessor.getStrWithoutTerminator(source.data);
		//we can safely remove ALL whitespaces in expressions, which makes analysis much more straightforward. - EDIT: the NExpression class will do this instead
	}

	@Override
	public CompilerContentType getContentType() {
		return CompilerContentType.EXPRESSION;
	}

	@Override
	public void addToGraph(NCompileGraph graph) {
		//create an NExpression that compiles into the method
		Throughput tp = new NExpression(trimmedData, src, graph).toThroughput(graph);
		if (tp != null) {
			graph.addInstructions(tp.getCode(DataType.ANY));
		}
	}
}

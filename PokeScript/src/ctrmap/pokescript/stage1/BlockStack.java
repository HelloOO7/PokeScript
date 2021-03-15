package ctrmap.pokescript.stage1;

import ctrmap.pokescript.data.Variable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class BlockStack<T extends CompileBlock> extends Stack<T> {

	public BlockStack() {
		super();
	}
	
	public CompileBlock getLatestBlock(){
		if (empty()){
			return null;
		}
		return peek();
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator() {
			int idx = size();

			@Override
			public boolean hasNext() {
				return idx > 0;
			}

			@Override
			public Object next() {
				idx--;
				return get(idx);
			}
		};
	}

	public BlockResult getBlocksToAttribute(CompileBlock.BlockAttribute a, String label) {
		BlockResult rsl = new BlockResult();

		Iterator<? extends CompileBlock> it = iterator();
		while (it.hasNext()) {
			CompileBlock blk = it.next();
			rsl.blocks.push(blk);
			if (blk.hasAttribute(a)) {
				if (label == null || blk.getShortBlockName().equals(label)) {
					break;
				}
			}
		}
		return rsl;
	}

	public static class BlockResult {

		public BlockStack<CompileBlock> blocks = new BlockStack<>();

		public List<Variable> collectLocalsNoBottom() {
			List<Variable> v = new ArrayList<>();
			for (CompileBlock b : blocks) {
				if (b != getBottomBlock()) {
					v.addAll(b.localsOfThisBlock);
				}
			}
			return v;
		}

		public CompileBlock getBottomBlock() {
			return blocks.firstElement();
		}
	}
}

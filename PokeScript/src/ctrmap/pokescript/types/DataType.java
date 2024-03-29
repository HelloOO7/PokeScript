package ctrmap.pokescript.types;

import ctrmap.pokescript.expr.Throughput;
import ctrmap.pokescript.stage1.NCompileGraph;

/**
 *
 */
public enum DataType {
	BOOLEAN("boolean", new BooleanTypeHandler()),
	INT("int", new IntegerTypeHandler(), DataTypeAttrib.NUMERIC),
	FLOAT("float", new FloatTypeHandler(), DataTypeAttrib.NUMERIC),
	ENUM("enum", new EnumTypeHandler(), DataTypeAttrib.NUMERIC),

	VAR_BOOLEAN,
	VAR_INT(DataTypeAttrib.NUMERIC),
	VAR_FLOAT(DataTypeAttrib.NUMERIC),
	VAR_CLASS,
	VAR_ENUM(DataTypeAttrib.NUMERIC),

	ANY,
	ANY_VAR, //THROWS EXCEPTION IF CHAINED
	VOID("void", null),
	
	CLASS("class", new ClassTypeHandler());
	
	private static final TypeDef[] PRIMITIVE_TYPEDEFS;
	
	static {
		DataType[] v = values();
		
		PRIMITIVE_TYPEDEFS = new TypeDef[v.length];
		
		for (int i = 0; i < v.length; i++) {
			PRIMITIVE_TYPEDEFS[i] = new TypeDef(v[i]);
		}
	}
	
	private String name;
	private AbstractTypeHandler handler;
	private DataTypeAttrib[] attribs;

	private DataType() {
		this(new DataTypeAttrib[0]);
	}
	
	private DataType(DataTypeAttrib... attribs) {
		this(null, null, attribs);
	}

	private DataType(String name, AbstractTypeHandler handler) {
		this(name, handler, new DataTypeAttrib[0]);
	}
	
	private DataType(String name, AbstractTypeHandler handler, DataTypeAttrib... attribs) {
		this.name = name;
		this.handler = handler;
		this.attribs = attribs;
	}
	
	public DataType getBaseType(){
		switch (this){
			case VAR_BOOLEAN:
				return BOOLEAN;
			case VAR_INT:
				return INT;
			case VAR_FLOAT:
				return FLOAT;
		}
		return this;
	}
	
	public DataType getVarType(){
		switch (this){
			case BOOLEAN:
				return VAR_BOOLEAN;
			case INT:
				return VAR_INT;
			case FLOAT:
				return VAR_FLOAT;
			case CLASS:
				return VAR_CLASS;
			case ENUM:
				return VAR_ENUM;
		}
		return this;
	}
	
	public TypeDef typeDef(){
		switch (this) {
			case CLASS:
			case ENUM:
				return new TypeDef(getFriendlyName(), this == ENUM); 
			default:
				return PRIMITIVE_TYPEDEFS[ordinal()];
		}
	}
	
	public boolean isNumber(){
		return hasAttrib(DataTypeAttrib.NUMERIC);
	}
	
	public boolean hasAttrib(DataTypeAttrib a){
		for (DataTypeAttrib a0 : attribs){
			if (a0 == a){
				return true;
			}
		}
		return false;
	}
	
	public AbstractTypeHandler requestHandler(){
		if (handler == null){
			throw new UnsupportedOperationException("Can not request handler for type " + this);
		}
		return handler;
	}
	
	public static Throughput getThroughput(String immSrc, NCompileGraph cg){
		for (DataType t : values()){
			if (t.handler != null){
				Throughput tp = t.handler.tryAssign(immSrc, cg);
				if (tp != null){
					return tp;
				}
			}
		}
		return null;
	}

	public static DataType fromName(String name) {
		if (name == null){
			return null;
		}
		for (DataType t : values()) {
			if (name.equals(t.name)) {
				return t;
			}
		}
		return null;
	}
	
	public static DataType getTypeCast(String str){
		for (DataType t : values()){
			if (t.name != null){
				if (str.startsWith("(" + t.name + ")")){
					return t;
				}
			}
		}
		return null;
	}

	public String getFriendlyName() {
		return name;
	}

	public static enum DataTypeAttrib {
		NUMERIC
	}
}

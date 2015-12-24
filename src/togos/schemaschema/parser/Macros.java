package togos.schemaschema.parser;

import java.util.List;

import togos.codeemitter.WordUtil;
import togos.lang.BaseSourceLocation;
import togos.lang.ScriptError;
import togos.lang.SourceLocation;
import togos.schemaschema.BaseSchemaObject;
import togos.schemaschema.Namespace;
import togos.schemaschema.PropertyUtil;
import togos.schemaschema.SchemaObject;
import togos.schemaschema.StringUtil;
import togos.schemaschema.namespaces.Core;
import togos.schemaschema.namespaces.Types;

public class Macros
{
	public static final Namespace FUNCTIONS_NS = Namespace.getInstance("http://ns.nuke24.net/Schema/Functions/");
	
	protected static Function defun(String name, Function f) {
		String capName = WordUtil.toPascalCase(name);
		String longName = FUNCTIONS_NS.prefix+capName;
		BaseSchemaObject bso = new BaseSchemaObject(name, longName, BaseSourceLocation.NONE);
		bso.scalarValue = f;
		PropertyUtil.add(bso.getProperties(), Core.TYPE, Types.FUNCTION);
		FUNCTIONS_NS.addItem(capName, bso);
		return f;
	}
	
	protected static String stringify(SchemaObject o) {
		Object sv = o.getScalarValue();
		if( sv != null ) {
			return sv.toString();
		} else if( o.getLongName() != null ) {
			return "<" + o.getLongName() + ">";
		} else if( o.getName() != null ) {
			return "<" + o.getName() + ">";
		} else {
			return "<null>";
		}
	}
	
	public static final Function CONCAT = defun("concat", new Function() {
		@Override public SchemaObject apply(SchemaInterpreter interp, SchemaObject[] arguments, List<SourceLocation> trace) throws ScriptError {
			if( arguments.length == 1 ) return arguments[0];
			
			// Really this should also work for non-string blobs, too.
			// But for now there probably aren't any.
			StringBuilder sb = new StringBuilder();
			for( SchemaObject o : arguments ) sb.append(stringify(o));
			
			return BaseSchemaObject.forScalar(sb.toString(), trace.get(trace.size()-1));
		}
	});
	
	public static final Function JOIN = defun("join", new Function() {
		@Override public SchemaObject apply(SchemaInterpreter interp, SchemaObject[] arguments, List<SourceLocation> trace) throws ScriptError {
			if( arguments.length == 1 ) return BaseSchemaObject.forScalar("", trace.get(trace.size()-1));
			
			// TODO: If arguments are lists, recursively join their contents
			
			if( arguments.length == 2 ) return arguments[1];
			
			String sep = stringify(arguments[1]);
			
			// Really this should also work for non-string blobs, too.
			// But for now there probably aren't any.
			StringBuilder sb = new StringBuilder();
			
			sb.append(stringify(arguments[2]));
			for( int i=2; i<arguments.length; ++i ) {
				sb.append(sep);
				sb.append(stringify(arguments[i]));
			}
			
			return BaseSchemaObject.forScalar(sb.toString(), trace.get(trace.size()-1));
		}
	});
	
	/** Handy for multi-line comments? */
	public static final Function UNINDENT = defun("unindent", new Function() {
		@Override public SchemaObject apply(SchemaInterpreter interp, SchemaObject[] arguments, List<SourceLocation> trace) throws ScriptError {
			if( arguments.length != 1 ) throw new ScriptError("Unindent takes only a single argument", trace);
			
			return BaseSchemaObject.forScalar(StringUtil.unindent(stringify(arguments[0])));
		}
	});
}

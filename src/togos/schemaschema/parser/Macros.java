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
import togos.schemaschema.namespaces.Core;
import togos.schemaschema.namespaces.Types;

public class Macros
{
	public static final Namespace FUNCTIONS_NS = Namespace.getInstance("http://ns.nuke24.net/Schema/Functions/");
	
	protected static Function defun(String name, Function f) {
		String longName = FUNCTIONS_NS.prefix+WordUtil.toCamelCase(name);
		BaseSchemaObject bso = new BaseSchemaObject(name, longName, BaseSourceLocation.NONE);
		bso.scalarValue = f;
		PropertyUtil.add(bso.getProperties(), Core.TYPE, Types.FUNCTION);
		FUNCTIONS_NS.addItem(name, bso);
		return f;
	}
	
	public static final Function CONCAT = defun("concat", new Function() {
		@Override public SchemaObject apply(SchemaInterpreter interp, SchemaObject[] arguments, List<SourceLocation> trace) throws ScriptError {
			if( arguments.length == 1 ) return arguments[0];
			
			// Really this should also work for non-string blobs, too.
			// But for now there probably aren't any.
			StringBuilder sb = new StringBuilder();
			for( SchemaObject o : arguments ) {
				Object sv = o.getScalarValue();
				if( sv != null ) {
					sb.append(sv.toString());
				} else if( o.getLongName() != null ) {
					sb.append("<"+o.getLongName()+">");
				} else if( o.getName() != null ) {
					sb.append("<"+o.getName()+">");
				} else {
					sb.append("<null>");
				}
			}
			
			return BaseSchemaObject.forScalar(sb.toString(), trace.get(trace.size()-1));
		}
	});
}

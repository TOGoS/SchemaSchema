package togos.schemaschema.parser;

import java.util.List;

import togos.lang.ScriptError;
import togos.lang.SourceLocation;
import togos.schemaschema.SchemaObject;

public interface Function {
	SchemaObject apply( SchemaInterpreter interp, SchemaObject[] arguments, List<SourceLocation> trace ) throws ScriptError;
}

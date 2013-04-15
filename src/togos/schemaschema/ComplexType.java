package togos.schemaschema;

import java.util.LinkedHashMap;
import java.util.Map;

import togos.schemaschema.parser.ast.Word;

public class ComplexType extends BaseSchemaObject implements Type
{
	public final Map<String,FieldSpec> fieldsByName = new LinkedHashMap<String,FieldSpec>();
	public final Map<String,IndexSpec> indexesByName = new LinkedHashMap<String,IndexSpec>();
	public final Map<String,ForeignKeySpec> foreignKeysByName = new LinkedHashMap<String,ForeignKeySpec>();
	
	public ComplexType( String name ) {
		super(name);
	}
	
	public String toString() {
		return "class "+Word.quote(name)+ (fieldsByName.size() == 0 ? " " :
			" {\n" + StringUtil.indent("\t", StringUtil.join("\n", fieldsByName.values())) + "\n}"
		);
	}
}

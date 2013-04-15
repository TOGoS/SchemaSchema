package togos.schemaschema;

import java.util.LinkedHashMap;
import java.util.Map;

public class ComplexType extends BaseSchemaObject implements Type
{
	public final Map<String,FieldSpec> fieldsByName = new LinkedHashMap<String,FieldSpec>();
	public final Map<String,IndexSpec> indexesByName = new LinkedHashMap<String,IndexSpec>();
	public final Map<String,ForeignKeySpec> foreignKeysByName = new LinkedHashMap<String,ForeignKeySpec>();
	
	public ComplexType( String name ) {
		super(name);
	}
	
	@Override public Type getParentType() { return Types.OBJECT; }
		
	public String toString() {
		return "class "+name+" {" + (fieldsByName.size() == 0 ?
			" " :
			"\n" + StringUtil.indent("\t", StringUtil.join("\n", fieldsByName.values())) + "\n"
		) + "}";
	}
}

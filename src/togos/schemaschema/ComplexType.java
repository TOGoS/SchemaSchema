package togos.schemaschema;

import java.util.Map;
import java.util.Set;

public class ComplexType extends BaseSchemaObject implements Type
{
	public final Type parentType;
	public final Map<String,FieldSpec> fieldsByName;
	public final Map<String,IndexSpec> indexesByName;
	public final Map<String,ForeignKeySpec> foreignKeysByName;
	
	public ComplexType(
		String name, Type parentType,
		Map<String,FieldSpec> fields,
		Map<String,IndexSpec> indexes,
		Map<String,ForeignKeySpec> foreignKeys,
		Map<Property,Set<Object>> propertyValues
	) {
		super( name, propertyValues );
		this.parentType = parentType;
		this.fieldsByName = fields;
		this.indexesByName = indexes;
		this.foreignKeysByName = foreignKeys;
	}
	
	@Override public String getName() { return name; }
	@Override public Type getParentType() { return Types.OBJECT; }
		
	public String toString() {
		return "class "+name+" {\n" + StringUtil.indent("\t", StringUtil.join("\n", fieldsByName.values())) + "\n}";
	}
}

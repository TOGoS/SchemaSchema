package togos.schemaschema;

import java.util.Collections;
import java.util.Map;

public class ObjectType implements Type
{
	public final String name;
	public final Type parentType;
	public final Map<String,FieldSpec> fieldsByName;
	public final Map<String,IndexSpec> indexesByName;
	public final Map<String,ForeignKeySpec> foreignKeysByName;
	
	public ObjectType(
		String name, Type parentType,
		Map<String,FieldSpec> fields,
		Map<String,IndexSpec> indexes,
		Map<String,ForeignKeySpec> foreignKeys
	) {
		this.name = name;
		this.parentType = parentType;
		this.fieldsByName = Collections.unmodifiableMap(fields);
		this.indexesByName = Collections.unmodifiableMap(indexes);
		this.foreignKeysByName = Collections.unmodifiableMap(foreignKeys);
	}
	
	@Override public String getName() { return name; }
	@Override public Type getParentType() { return Types.OBJECT; }
	
	public String toString() {
		return "class "+name+" {\n" + StringUtil.indent("\t", StringUtil.join("\n", fieldsByName.values())) + "\n}";
	}
}

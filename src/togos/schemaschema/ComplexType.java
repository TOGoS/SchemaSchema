package togos.schemaschema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import togos.schemaschema.parser.ast.Phrase;
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
		String extendStr = "";
		Set<Type> parentTypes = getParentTypes();
		if( parentTypes.size() > 0 ) {
			for( Type t : parentTypes ) {
				extendStr += extendStr.length() == 0 ? " : extends(" : ", ";
				extendStr += Phrase.quoteIfNecessary(t.getName());
			}
			extendStr += ")";
		}
		
		return "class "+Word.quote(name) + extendStr + (fieldsByName.size() == 0 ? " " :
			" {\n" + StringUtil.indent("\t", StringUtil.join("\n", fieldsByName.values())) + "\n}"
		);
	}
}

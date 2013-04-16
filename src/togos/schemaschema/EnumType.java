package togos.schemaschema;

import java.util.LinkedHashSet;
import java.util.Set;

import togos.schemaschema.parser.ast.Word;

public class EnumType extends ComplexType
{
	public final Set<SchemaObject> validValues = new LinkedHashSet<SchemaObject>();
	
	public EnumType( String name ) {
		super(name);
	}

	public void addValue(BaseSchemaObject v) {
		validValues.add( v );
	}
	
	public void addValue(String name) {
		addValue( new BaseSchemaObject(name, this) );
	}
	
	public String toString() {
		String s = "enum "+Word.quote(name);
		if( validValues.size() > 0 ) {
			s += " {\n";
			for( SchemaObject v : validValues ) {
				s += "\t" + v.getName()+"\n";
			}
			s += "}";
		}
		return s;
	}
}

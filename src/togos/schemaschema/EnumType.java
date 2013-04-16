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
	
	public Set<SchemaObject> getValidValues() {
		return validValues;
	}
	
	public void addValidValue(BaseSchemaObject v) {
		validValues.add( v );
	}
	
	public void addValidValue(String name) {
		addValidValue( new BaseSchemaObject(name, this) );
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

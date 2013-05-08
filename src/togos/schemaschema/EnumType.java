package togos.schemaschema;

import java.util.LinkedHashSet;
import java.util.Set;

import togos.lang.SourceLocation;
import togos.schemaschema.parser.ast.Word;

public class EnumType extends ComplexType
{
	public final Set<SchemaObject> validValues = new LinkedHashSet<SchemaObject>();
	
	public EnumType( String name, SourceLocation sLoc ) {
		super(name, sLoc);
	}
	
	public Set<SchemaObject> getValidValues() {
		return validValues;
	}
	
	public void addValidValue(BaseSchemaObject v) {
		validValues.add( v );
	}
	
	public void addValidValue(String name, SourceLocation sLoc) {
		addValidValue( new BaseSchemaObject(name, this, sLoc) );
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

	public String[] getValidValueNames() {
		String[] vv = new String[validValues.size()];
		int i = 0;
		for( SchemaObject v : validValues ) {
			vv[i++] = v.getName();
		}
		return vv;
	}
}

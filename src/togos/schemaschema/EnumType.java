package togos.schemaschema;

import java.util.LinkedHashSet;
import java.util.Set;

import togos.lang.BaseSourceLocation;
import togos.lang.SourceLocation;
import togos.schemaschema.namespaces.Core;
import togos.schemaschema.parser.ast.Word;

public class EnumType extends ComplexType
{
	private static final SourceLocation SLOC = new BaseSourceLocation(EnumType.class.getName(), 0, 0);
	
	public final Set<SchemaObject> validValues = new LinkedHashSet<SchemaObject>();
	
	public EnumType( String name, SourceLocation sLoc ) {
		super(name, sLoc);
		PropertyUtil.add(this.getProperties(), Core.IS_ENUM_TYPE, BaseSchemaObject.forScalar(Boolean.TRUE, SLOC));
	}
	
	public Set<SchemaObject> getValidValues() {
		return validValues;
	}
	
	public void addValidValue(BaseSchemaObject v) {
		validValues.add( v );
	}
	
	public SchemaObject addValidValue(String name, SourceLocation sLoc) {
		BaseSchemaObject obj = new BaseSchemaObject(name, this, sLoc);
		addValidValue( obj );
		return obj;
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

package togos.schemaschema;

import java.util.Map;
import java.util.Set;

import togos.schemaschema.parser.ast.Phrase;

public class FieldSpec extends BaseSchemaObject
{
	public FieldSpec( String name ) {
		super(name);
	}
	
	public Set<Type> getObjectTypes() {
		return PropertyUtil.getAll(properties, Predicates.OBJECTS_ARE_MEMBERS_OF, Type.class);
	}
	
	public String toString() {
		String s = Phrase.quoteIfNecessary(name);
		for( Map.Entry<Predicate,Set<Object>> e : properties.entrySet() ) {
			for( Object v : e.getValue() ) {
				if( e.getKey() == Predicates.OBJECTS_ARE_MEMBERS_OF ) {
					s += " : " + PropertyUtil.objectToString(v);
				} else {
					s += " : " + PropertyUtil.pairToString( e.getKey(), v );
				}
			}
		}
		return s;
	}
}

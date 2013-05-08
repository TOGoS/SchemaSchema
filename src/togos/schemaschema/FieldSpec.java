package togos.schemaschema;

import java.util.Map;
import java.util.Set;

import togos.lang.SourceLocation;
import togos.schemaschema.parser.ast.Phrase;

public class FieldSpec extends BaseSchemaObject
{
	public FieldSpec( String name, SourceLocation sLoc ) {
		super(name, sLoc);
	}
	
	public Set<Type> getObjectTypes() {
		return PropertyUtil.getAll(properties, Predicates.OBJECTS_ARE_MEMBERS_OF, Type.class);
	}
	
	public String toString() {
		String s = Phrase.quoteIfNecessary(name);
		for( Map.Entry<Predicate,Set<SchemaObject>> e : properties.entrySet() ) {
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

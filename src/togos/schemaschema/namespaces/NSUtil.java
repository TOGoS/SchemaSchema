package togos.schemaschema.namespaces;

import togos.codeemitter.WordUtil;
import togos.lang.BaseSourceLocation;
import togos.schemaschema.BaseSchemaObject;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;
import togos.schemaschema.PropertyUtil;
import togos.schemaschema.SimpleType;
import togos.schemaschema.Type;

public class NSUtil
{
	public static final String SCHEMA_PREFIX = "http://ns.nuke24.net/Schema/";
	
	public static Predicate definePredicate( Namespace ns, String name, Type objectType, String comment ) {
		Predicate p = new Predicate( name, ns.prefix+WordUtil.toCamelCase(name), objectType, BaseSourceLocation.NONE );
		ns.addPredicate(p);
		if( comment != null ) {
			// If comment itself is not defined and name = "comment", then 
			// we're probably in the process of defining comment itself!
			Predicate commentPredicate = (Core.COMMENT == null && "comment".equals(name)) ? p : Core.COMMENT; 
			PropertyUtil.add(p.getProperties(), commentPredicate, BaseSchemaObject.forScalar(comment, BaseSourceLocation.NONE));
		}
		return p;
	}
	
	public static Type defineType( Namespace ns, String name ) {
		SimpleType t = new SimpleType(name, BaseSourceLocation.NONE);
		ns.addType(t);
		return t;
	}
}

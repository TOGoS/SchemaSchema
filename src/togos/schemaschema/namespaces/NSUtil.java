package togos.schemaschema.namespaces;

import togos.codeemitter.WordUtil;
import togos.lang.BaseSourceLocation;
import togos.schemaschema.BaseSchemaObject;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;
import togos.schemaschema.SimpleType;
import togos.schemaschema.Type;

public class NSUtil
{
	public static final String SCHEMA_PREFIX = "http://ns.nuke24.net/Schema/";
	
	public static Predicate definePredicate( Namespace ns, String name, Type valueType, String comment ) {
		Predicate p = new Predicate( name, ns.prefix+WordUtil.toCamelCase(name), BaseSourceLocation.NONE );
		ns.addPredicate(p);
		if( valueType != null ) p.setProperty(Core.VALUE_TYPE, valueType);
		if( comment != null ) p.setProperty(Core.COMMENT, BaseSchemaObject.forScalar(comment, BaseSourceLocation.NONE));
		return p;
	}
	
	public static Type defineType( Namespace ns, String name ) {
		SimpleType t = new SimpleType(name, BaseSourceLocation.NONE);
		ns.addType(t);
		return t;
	}
}

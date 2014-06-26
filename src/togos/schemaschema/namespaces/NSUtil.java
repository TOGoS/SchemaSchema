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
	
	public static SimpleType defineType( Namespace ns, String name ) {
		String cName = WordUtil.toPascalCase(name);
		String longName = ns.prefix + cName;
		SimpleType t = new SimpleType(name, longName, Types.CLASS, BaseSourceLocation.NONE);
		ns.addItem(cName, t);
		return t;
	}
}

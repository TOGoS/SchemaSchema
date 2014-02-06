package togos.schemaschema.namespaces;

import static togos.schemaschema.namespaces.NSUtil.definePredicate;
import static togos.schemaschema.namespaces.NSUtil.defineType;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;
import togos.schemaschema.Type;

/**
 * Defines types and properties relevant to relational databases 
 */
public class RDB
{
	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX+"RDB/");
	
	public static final Type INDEX       = defineType(NS, "index");
	public static final Type FOREIGN_KEY = defineType(NS, "foreign key");
	
	// Table properties
	public static final Predicate HAS_FOREIGN_KEY = definePredicate(NS, "has foreign key", FOREIGN_KEY, null);
	public static final Predicate IS_SELF_KEYED = definePredicate(NS, "is self-keyed", Types.BOOLEAN, "indicates the subject is a class that uses all of its fields as a primary key");
	public static final Predicate HAS_INDEX = definePredicate(NS, "has index", INDEX, null);
	
	// Column properties
	public static final Predicate IS_AUTO_INCREMENTED = definePredicate(NS, "is auto-incremented", Types.BOOLEAN, null);
}

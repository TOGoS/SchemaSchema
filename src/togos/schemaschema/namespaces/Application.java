package togos.schemaschema.namespaces;

import static togos.schemaschema.namespaces.NSUtil.definePredicate;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;

/**
 * Predicates that define how a class should be used within a program
 */
public class Application
{
	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX+"Application/");
	
	private Application() { }
	
	public static final Predicate HAS_DB_TABLE     = definePredicate(NS, "has a database table", Types.BOOLEAN, "indicates that there should be a database table with rows corresponding to instances of this class");
	public static final Predicate HAS_REST_SERVICE = definePredicate(NS, "has a REST service"  , Types.BOOLEAN, "indicates that the this class's instances should be exposed via REST services");
	public static final Predicate MEMBERS_ARE_PUBLIC = definePredicate(NS, "members are public"  , Types.BOOLEAN, "indicates that instances of this class are not secret and may be visible to the general public");
	public static final Predicate MEMBERS_ARE_MUTABLE = definePredicate(NS, "members are mutable"  , Types.BOOLEAN, "indicates that instances of this class may be modified while retaining their identity");
	public static final Predicate MEMBER_SET_IS_MUTABLE = definePredicate(NS, "member set is mutable"  , Types.BOOLEAN, "indicates that instances of this class may be added or deleted at runtime");
}

package togos.schemaschema.namespaces;

import static togos.schemaschema.namespaces.NSUtil.definePredicate;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;

public class Application
{
	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX+"Application/");
	
	private Application() { }
	
	public static final Predicate HAS_DB_TABLE     = definePredicate(NS, "has an associated database table", Types.BOOLEAN, "indicates that there should be a database table with rows corresponding to instances of a class");
	public static final Predicate HAS_REST_SERVICE = definePredicate(NS, "has an associated rest service"  , Types.BOOLEAN, "indicates that the subject's records should be exposed via services");
}

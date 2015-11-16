package togos.schemaschema.namespaces;

import java.util.ArrayList;
import java.util.List;

import togos.codeemitter.WordUtil;
import togos.lang.BaseSourceLocation;
import togos.schemaschema.BaseSchemaObject;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;
import togos.schemaschema.PropertyUtil;
import togos.schemaschema.SimpleType;
import togos.schemaschema.Type;
import togos.schemaschema.Values;

public class Core
{
	public static final Namespace RDF_NS = Namespace.getInstance("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	public static final Namespace RDFS_NS = Namespace.getInstance("http://www.w3.org/2000/01/rdf-schema#");
	public static final Namespace SCHEMA_NS = Namespace.getInstance("http://schema.org/");
	
	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX);
	public static final Namespace TYPES_NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX+"Types/");
	
	private Core() { }
	
	// We have to resort to some 2-passery here so as not run into nulls
	// when using certain predicates as part of their own definitions.
	
	private static class PredicatePredefinition {
		public final Namespace ns;
		public final String name;
		public final Predicate pred;
		public final Type valueType;
		public final String comment;
		public PredicatePredefinition( Predicate p, Namespace ns, String name, Type valueType, String comment ) {
			this.pred = p;
			this.ns = ns;
			this.name = name;
			this.valueType = valueType;
			this.comment = comment;
		}
	}
	private static List<PredicatePredefinition> predefs = new ArrayList<PredicatePredefinition>();
	private static boolean fixedUp = false;
	private static Predicate predefinePredicate(Namespace ns, String name, Type valueType, String comment) {
		assert !fixedUp;
		Predicate pred = Predicate.getWithoutInitializing(ns, name);
		predefs.add(new PredicatePredefinition(pred, ns, name, valueType, comment));
		return pred;
	}
	private static void fixUpPredefinitions() {
		assert !fixedUp;
		for( PredicatePredefinition ppd : predefs ) {
			String cName = WordUtil.toCamelCase(ppd.name);
			String longName = NS.prefix+cName;
			ppd.pred.setProperty(TYPE, PREDICATE);
			ppd.pred.setProperty(NAME, BaseSchemaObject.forScalar(ppd.name));
			ppd.pred.setProperty(LONGNAME, BaseSchemaObject.forScalar(longName));
			if( ppd.valueType != null ) ppd.pred.addObjectType(ppd.valueType);
			if( ppd.comment != null ) ppd.pred.setProperty(COMMENT, BaseSchemaObject.forScalar(ppd.comment, BaseSourceLocation.NONE));
			ppd.ns.addItem(cName, ppd.pred);
		}
		fixedUp = true;
	}
	
	// These needs to be defined right away, since they are referenced by all other definitions
	// including their own!
	public static final SimpleType CLASS     = new SimpleType("class", TYPES_NS.prefix+"Class", BaseSourceLocation.NONE);
	public static final SimpleType PREDICATE = new SimpleType("predicate", TYPES_NS.prefix+"Predicate", BaseSourceLocation.NONE);
	public static final Predicate  TYPE      = Predicate.getWithoutInitializing(RDF_NS, "type");
	static {
		predefinePredicate(RDF_NS, "type", CLASS, null);
	}
	
	// 'Types' reference CLASS and PREDICATE.
	// Therefore we cannot reference Types.anything before they are defined! 
	
	public static final Predicate COMMENT  = predefinePredicate(NS, "comment", Types.STRING, "a note about the subject");
	
	public static final Predicate NAME     = predefinePredicate(NS, "name", Types.STRING, "short, natural-language name of the subject");
	public static final Predicate LONGNAME = predefinePredicate(NS, "long name", Types.STRING, "globally unique name; usually in URI form");
	
	public static final Predicate EXTENDS      = predefinePredicate(RDFS_NS, "is subclass of", Types.CLASS, null);
	public static final Predicate IS_ENUM_TYPE = predefinePredicate(NS, "is enum type", Types.BOOLEAN, "indicates that the subject is a type that defines a list of valid named members");
	
	// Field/predicate properties
	public static final Predicate VALUE_TYPE   = predefinePredicate(NS, "value type", Types.CLASS, "indicates that the subject is a field or predicate whose objects must be of the specified class");
	public static final Predicate UNIT_VALUE   = predefinePredicate(NS, "unit value", Types.UNIT, "for numeric fields, indicates what '1' means");
	public static final Predicate IS_NULLABLE  = predefinePredicate(NS, "is nullable", Types.BOOLEAN, "indicates that the subject is a field for which null is a valid value");
	public static final Predicate DEFAULT_VALUE= predefinePredicate(NS, "default value", null, "default value of field");
	public static final Predicate HAS_FIELD    = predefinePredicate(NS, "has field", Types.FIELD, null);
	
	static {
		// Fix stuff up!
		fixUpPredefinitions();
		CLASS.fixCoreProperties(true);
		CLASS.setProperty(TYPE, CLASS);
		PREDICATE.fixCoreProperties(true);
		PREDICATE.setProperty(TYPE, CLASS);
		
		// Define some singleton values
		SCHEMA_NS.addItem("True", Values.TRUE);
		SCHEMA_NS.addItem("False", Values.FALSE);
	}
}

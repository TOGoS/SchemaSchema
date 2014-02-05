package togos.schemaschema;

import java.util.HashMap;
import java.util.Map;

import togos.codeemitter.WordUtil;

public class Namespace
{
	public static final Namespace ROOT = new Namespace("");
	
	public static Namespace getInstance(String prefix) {
		return ROOT.getNamespace(prefix, true);
	}
	
	public final String prefix;
	public final Map<String,Object> items;
	public final Map<String,Namespace> containedNamespaces;
	
	public Namespace( String prefix, Map<String,Object> items ) {
		this.prefix = prefix;
		this.items = items;
		this.containedNamespaces = new HashMap<String,Namespace>();
	}
	
	public Namespace( String prefix ) {
		this( prefix, new HashMap<String,Object>() );
	}
	
	public synchronized void addNamespace( Namespace ns ) {
		if( !ns.prefix.startsWith(prefix) ) {
			throw new RuntimeException("Contained namespace's prefix must start with containing namespace's prefix but doesn't: '"+ns+"' / '"+prefix+"'");
		}
		String n = ns.prefix.substring(prefix.length());
		containedNamespaces.put(n, ns);
	}
	
	protected synchronized void addItem( String urlName, Object v ) {
		items.put( urlName, v );
	}
	
	public void addType( SchemaObject obj ) {
		addItem( WordUtil.toPascalCase(obj.getName()), obj );
	}
	
	public void addPredicate( SchemaObject obj ) {
		addItem( WordUtil.toCamelCase(obj.getName()), obj );
	}
	
	public Object get( String name ) {
		if( !name.startsWith(prefix) ) return null;
		
		String postFix = name.substring(prefix.length());
		
		Object i = items.get(postFix);
		if( i != null ) return i;
		
		Namespace ns = containedNamespaces.get(postFix);
		if( ns != null ) return ns;
		
		for( Namespace cn : containedNamespaces.values() ) {
			i = cn.get(name);
			if( i != null ) return i;
		}
		return null;
	}
	
	public synchronized Namespace getNamespace(String prefix, boolean create) {
		if( prefix.length() == 0 ) return this;
		
		Object i = get(prefix);
		if( i instanceof Namespace ) return (Namespace)i;
		
		if( i == null ) {
			if( create ) {
				Namespace ns = new Namespace(prefix);
				addNamespace(ns);
				return ns;
			} else {
				return null;
			}
		}
		
		throw new RuntimeException(prefix+" is already defined as something other than a namespace");
	}
	
	public String toString() {
		return prefix;
	}
}

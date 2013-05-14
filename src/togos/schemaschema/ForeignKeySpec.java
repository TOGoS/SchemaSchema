package togos.schemaschema;

import java.util.Collection;
import java.util.Collections;

public class ForeignKeySpec
{
	public final static class Component {
		public final FieldSpec targetField;
		public final FieldSpec localField;
		
		public Component( FieldSpec targetField, FieldSpec localField ) {
			this.targetField = targetField;
			this.localField = localField;
		}
		
		public String toString() { return targetField + " = " + localField; }
	}
	
	public final String name;
	public final ComplexType target;
	public final Collection<Component> components;
	
	public ForeignKeySpec( String name, ComplexType target, Collection<Component> components ) {
		this.name = name;
		this.target = target;
		this.components = Collections.unmodifiableCollection(components);
	}
	
	public String toString() { return StringUtil.join("\n", components); }
}

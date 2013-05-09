package togos.schemaschema;

import java.util.Collection;
import java.util.Collections;

public class ForeignKeySpec
{
	public final static class Component {
		public final FieldSpec targetField;
		public final FieldSpec localField;
		
		Component( FieldSpec targetField, FieldSpec localField ) {
			this.targetField = targetField;
			this.localField = localField;
		}
		
		public String toString() { return targetField + " = " + localField; }
	}
	
	public final ComplexType target;
	public final Collection<Component> components;
	
	ForeignKeySpec( ComplexType target, Collection<Component> components ) {
		this.target = target;
		this.components = Collections.unmodifiableCollection(components);
	}
	
	public String toString() { return StringUtil.join("\n", components); }
}

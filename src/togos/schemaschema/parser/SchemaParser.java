package togos.schemaschema.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import togos.lang.InterpretError;
import togos.lang.SourceLocation;
import togos.schemaschema.ComplexType;
import togos.schemaschema.EnumType;
import togos.schemaschema.FieldSpec;
import togos.schemaschema.ForeignKeySpec;
import togos.schemaschema.IndexSpec;
import togos.schemaschema.Predicates;
import togos.schemaschema.Predicate;
import togos.schemaschema.PropertyUtil;
import togos.schemaschema.SchemaObject;
import togos.schemaschema.Type;
import togos.schemaschema.parser.ast.Block;
import togos.schemaschema.parser.ast.Command;
import togos.schemaschema.parser.ast.Parameterized;
import togos.schemaschema.parser.ast.Phrase;
import togos.schemaschema.parser.ast.Word;
import togos.schemaschema.parser.asyncstream.BaseStreamSource;
import togos.schemaschema.parser.asyncstream.StreamDestination;
import togos.schemaschema.parser.asyncstream.StreamUtil;

public class SchemaParser extends BaseStreamSource<SchemaObject> implements StreamDestination<Command>
{
	protected static String singleString( Parameterized p, String contextDescription ) throws InterpretError {
		if( p.parameters.length != 0 ) {
			throw new InterpretError( contextDescription + " cannot take arguments", p.sLoc );
		}
		return p.subject.unquotedText();
	}
	
	interface ModifierSpec {
		public Modifier bind( SchemaParser sp, Parameterized[] params, SourceLocation sLoc ) throws InterpretError;
	}
	
	interface Modifier {
		public void apply( SchemaObject subject );
	}
	
	interface FieldModifier extends Modifier {
		public void apply( ComplexType classObject, FieldSpec fieldSpec );
	}
	
	public static class SimplePredicateModifierSpec implements ModifierSpec {
		final Predicate predicate;
		
		public SimplePredicateModifierSpec( Predicate p ) {
			this.predicate = p;
		}
		
		@Override public Modifier bind(SchemaParser sp, Parameterized[] params, SourceLocation sLoc) throws InterpretError {
			final Set<Object> values = new HashSet<Object>();
			if( params.length == 0 ) {
				values.add( Boolean.TRUE );
			} else {
				for( Parameterized p : params ) {
					values.add( sp.evaluate( predicate, p ) );
				}
			}
			
			return new Modifier() {
				public void apply(SchemaObject subject) {
					PropertyUtil.addAll( subject.getProperties(), predicate, values );					
				}
			};
		}
	}
	
	/** Modifier that is equivalent to a set of property -> value pairs */
	public static class AliasModifier implements Modifier, ModifierSpec {
		final String name;
		final Map<Predicate,Set<Object>> propertyValues;
		
		public AliasModifier( String name, Map<Predicate,Set<Object>> propertyValues ) {
			this.name = name;
			this.propertyValues = propertyValues;
		}
		
		@Override
		public Modifier bind(SchemaParser sp, Parameterized[] params, SourceLocation sLoc) throws InterpretError {
			if( params.length > 0 ) {
				throw new InterpretError(name+" modifier takes no arguments", params[0].sLoc);
			}
			return this;
		}
		
		@Override
		public void apply(SchemaObject subject) {
			PropertyUtil.addAll( subject.getProperties(), propertyValues );
		}
	}
	
	public static class FieldIndexModifierSpec implements ModifierSpec {
		public static FieldIndexModifierSpec INSTANCE = new FieldIndexModifierSpec();
		
		@Override
		public Modifier bind(SchemaParser sp, Parameterized[] params, SourceLocation sLoc) throws InterpretError {
			final ArrayList<String> indexNames = new ArrayList<String>();
			
			if( params.length == 0 ) {
				throw new InterpretError("Index modifier with no arguments is useless", sLoc);
			}
			for( Parameterized indexParam : params ) {
				for( Parameterized indexParamParam : indexParam.parameters ) {
					throw new InterpretError("Index name takes no parameters, but parameters given", indexParamParam.sLoc);
				}
				indexNames.add( indexParam.subject.unquotedText() );
			}

			return new FieldModifier() {
				@Override
				public void apply(SchemaObject subject) {
					throw new UnsupportedOperationException("Index field modifier doesn't support apply(subject) -- use the other apply method");
				}
				
				@Override
				public void apply(ComplexType classObject, FieldSpec fieldSpec) {
					for( String indexName : indexNames ) {
						IndexSpec index = classObject.indexesByName.get(indexName);
						if( index == null ) {
							index = new IndexSpec(indexName);
							classObject.indexesByName.put(indexName, index);
						}
						index.fields.add( fieldSpec );
					}
				}
			};
		}
	}
	
	/**
	 * Used to specify an new (anonymous) enum type for a single field
	 */
	public static class EnumFieldModifierSpec implements ModifierSpec {
		public static final EnumFieldModifierSpec INSTANCE = new EnumFieldModifierSpec(); 
		
		@Override
		public Modifier bind(SchemaParser sp, Parameterized[] params, SourceLocation sLoc) throws InterpretError {
			final LinkedHashSet<String> valueNames = new LinkedHashSet<String>(); 
			for( Parameterized p : params ) {
				for( Parameterized pp : p.parameters ) {
					throw new InterpretError( "Enum values cannot themselves take parameters", pp.sLoc);
				}
			}
			
			return new Modifier() {
				@Override public void apply(SchemaObject subject) {
					EnumType er = new EnumType(subject.getName());
					for( String valueName : valueNames ) er.addValidValue(valueName);
					PropertyUtil.add( subject.getProperties(), Predicates.OBJECTS_ARE_MEMBERS_OF, er );
				}
			};
		}
	}
	
	////
	
	protected Map<String,Object> things = new HashMap<String,Object>();
	protected Map<String,Type> types = new HashMap<String,Type>();
	protected Map<String,Predicate> predicates = new HashMap<String,Predicate>();
	protected Map<String,ModifierSpec> generalModifiers = new HashMap<String,ModifierSpec>();
	protected Map<String,ModifierSpec> fieldModifiers = new HashMap<String,ModifierSpec>();
	protected Map<String,ModifierSpec> classModifiers = new HashMap<String,ModifierSpec>();
	
	public SchemaParser() { }
	
	public void defineType( Type t ) throws Exception {
		things.put( t.getName(), t );
		types.put( t.getName(), t );
		HashMap<Predicate,Set<Object>> appliedProperties = new HashMap<Predicate,Set<Object>>();
		PropertyUtil.add(appliedProperties, Predicates.OBJECTS_ARE_MEMBERS_OF, t);
		fieldModifiers.put( t.getName(), new AliasModifier(Predicates.OBJECTS_ARE_MEMBERS_OF.name, appliedProperties) );
		_data( t );
	}
	
	public void defineFieldPredicate( Predicate pred ) throws Exception {
		things.put( pred.name, pred );
		predicates.put( pred.name, pred );
		fieldModifiers.put( pred.name, new SimplePredicateModifierSpec(pred) );
		_data( pred );
	}
	
	public void defineClassPredicate( Predicate pred ) throws Exception {
		things.put( pred.name, pred );
		predicates.put( pred.name, pred );
		classModifiers.put( pred.name, new SimplePredicateModifierSpec(pred) );
		_data( pred );
	}
	
	public void defineClassModifier( String name, ModifierSpec spec ) {
		classModifiers.put( name, spec );
	}
	
	public void defineFieldModifier( String name, ModifierSpec spec ) {
		fieldModifiers.put( name, spec );
	}
	
	private ModifierSpec findPropertyModifierSpec(Phrase subject) {
		return null;
	}
	
	private ModifierSpec findClassModifierSpec(Phrase subject) {
		String name = subject.unquotedText();
		ModifierSpec m;
		if( (m = classModifiers.get(name)) != null ) return m;
		if( (m = generalModifiers.get(name)) != null ) return m;
		return null;
	}
	
	private ModifierSpec findFieldModifierSpec(Phrase subject) {
		String name = subject.unquotedText();
		ModifierSpec m;
		if( (m = fieldModifiers.get(name)) != null ) return m;
		if( (m = generalModifiers.get(name)) != null ) return m;
		return null;
	}
	
	protected Object evaluate( Object v, Parameterized[] parameters ) throws InterpretError {
		if( parameters.length > 0 ) {
			throw new InterpretError("Don't know how to parameterize "+v.getClass(), parameters[0].sLoc);
		}
		return v;
	}
	
	/**
	 * @param context predicate whose object we are evaluating; may be null
	 * @param p Parameterized representation of the value
	 * @return
	 * @throws InterpretError
	 */
	protected Object evaluate( Predicate context, Parameterized p ) throws InterpretError {
		if( p.subject.words.length == 1 && p.subject.words[0].quoting == Token.Type.DOUBLE_QUOTED_STRING ) {
			return p.subject.unquotedText();
		}
		
		String name = p.subject.unquotedText();
		Set<Object> possibleValues = new HashSet<Object>();
		if( context != null ) {
			for( Type t : context.getObjectTypes() ) {
				if( t instanceof EnumType ) {
					for( SchemaObject enumValue : ((EnumType)t).getValidValues() ) {
						if( name.equals(enumValue.getName()) ) {
							possibleValues.add(enumValue);
						}
					}
				}
			}
		}
		
		if( possibleValues.size() == 0 ) {
			Object v = things.get(name);
			if( v != null ) possibleValues.add( v );
		}
		
		if( possibleValues.size() == 0 ) {
			throw new InterpretError("Unrecognized symbol: "+Word.quote(name), p.subject.sLoc);
		} else if( possibleValues.size() > 1 ) {
			// TODO: list definition locations
			throw new InterpretError("Symbol "+Word.quote(name)+" is ambiguous", p.subject.sLoc);
		} else {
			for( Object v : possibleValues ) {
				return evaluate( v, p.parameters );
			}
			throw new RuntimeException("Somehow foreach body wasn't evaluated for single-item set");
		}
	}
	
	protected FieldSpec defineSimpleField(
		ComplexType objectType,
		Command fieldCommand
	) throws InterpretError {
		String fieldName = singleString(fieldCommand.subject, "field name");
		if( objectType.fieldsByName.containsKey(fieldName) ) {
			throw new InterpretError( "Field '"+fieldName+"' already defined", fieldCommand.sLoc );
		}
		
		FieldSpec fieldSpec = new FieldSpec( fieldName );
		
		for( Parameterized modifier : fieldCommand.modifiers ) {
			ModifierSpec ms;
			if( (ms = findFieldModifierSpec(modifier.subject)) != null ) {
				Modifier m = ms.bind( this, modifier.parameters, modifier.sLoc );
				if( m instanceof FieldModifier ) {
					((FieldModifier)m).apply( objectType, fieldSpec );
				} else {
					m.apply( fieldSpec );
				}
			} else {
				throw new InterpretError(
					"Unrecognised field modifier: "+modifier.subject.toString(), modifier.sLoc
				);
			}
		}
		
		objectType.fieldsByName.put( fieldSpec.name, fieldSpec );
		return fieldSpec;
	}
	
	protected FieldSpec getSimpleField(
		ComplexType objectType,
		Command fieldCommand
	) throws InterpretError {
		String fieldName = singleString(fieldCommand.subject, "field name");
		FieldSpec fieldSpec = objectType.fieldsByName.get(fieldName);
		if( fieldSpec == null ) {
			fieldSpec = defineSimpleField( objectType, fieldCommand );
		} else if( fieldCommand.modifiers.length > 0 ) {
			throw new InterpretError( "Cannot redefine field '"+fieldName+"'", fieldCommand.sLoc );
		}
		return fieldSpec;
	}
	
	protected Predicate parseProperty( String name, Parameterized[] modifiers, Block body ) throws InterpretError {
		Predicate pred = new Predicate(name);
		for( Parameterized p : modifiers ) {
			ModifierSpec ms;
			if( (ms = findPropertyModifierSpec(p.subject)) != null ) {
				// TODO: evaluate modifier
			}
			Object modValue = evaluate( null, p );
			if( modValue instanceof Type ) {
				pred.addObjectType( (Type)modValue );
			} else {
				throw new InterpretError("Don't know how to interpret property modifier value: "+modValue.getClass(), p.sLoc);
			}
		}
		return pred;
	}
	
	private ModifierSpec parseFieldModifierSpec(final String name, Parameterized[] modifierModifiers, Block body) throws InterpretError {
		final ArrayList<Modifier> subModifiers = new ArrayList<Modifier>();
		if( body.commands.length != 1 ) {
			throw new InterpretError("Field modifier must have exactly 1 command, "+body.commands.length+" given",
				body.commands.length == 0 ? body.sLoc : body.commands[1].sLoc );
		}
		for( Command cmd : body.commands ) {
			for( Parameterized p : cmd.modifiers ) {
				ModifierSpec ms = findFieldModifierSpec(p.subject);
				if( ms == null ) {
					throw new InterpretError("Unrecognised field modifier: "+p.subject.unquotedText(), p.sLoc);
				}
			}
		}
		return new ModifierSpec() {
			@Override
			public Modifier bind(SchemaParser sp, Parameterized[] params, SourceLocation sLoc) throws InterpretError {
				if( params.length > 0 ) {
					throw new InterpretError("Custom field modifier "+name+" takes no parameters", sLoc);
				}
				return new FieldModifier() {
					@Override
					public void apply( ComplexType classObject, FieldSpec fieldSpec ) {
						for( Modifier m : subModifiers ) {
							if( m instanceof FieldModifier ) {
								((FieldModifier)m).apply(classObject, fieldSpec);
							} else {
								m.apply(fieldSpec);
							}
						}
					}
					
					@Override public void apply( SchemaObject fieldSpec ) {
						for( Modifier m : subModifiers ) {
							m.apply(fieldSpec);
						}
					}
				};
			}
		};
	}
	
	public ComplexType parseClass( String name, Parameterized[] modifiers, Block body ) throws InterpretError {
		ComplexType t = new ComplexType( name );
		
		ModifierSpec m;
		for( Command fieldCommand : body.commands ) {
			ArrayList<Modifier> _fieldModifiers = new ArrayList<Modifier>();
			for( Parameterized fieldNameParameter : fieldCommand.subject.parameters ) {
				throw new InterpretError("Field name cannot have parameters", fieldNameParameter.sLoc );
			}
			Block referenceBody = null;
			for( Parameterized mod : fieldCommand.modifiers ) {
				if( "reference".equals(mod.subject.unquotedText()) ) {
					if( mod.parameters.length != 1 ) {
						throw new InterpretError(
							"'reference' field modifier takes a single parameter: "+
							"the name of the type being referenced.  Got "+
							mod.parameters.length+" parameters", mod.sLoc
						);
					}
					if( fieldCommand.body == null ) {
						throw new InterpretError(
							"'reference' field specification requires a block", fieldCommand.sLoc
						);
					}
					if( fieldCommand.body.commands.length == 0 ) {
						throw new InterpretError(
							"'reference' field specificatino requires at least one foreign key component",
							fieldCommand.body.sLoc
						);
					}
					referenceBody = fieldCommand.body;
				} else if( (m = findClassModifierSpec(mod.subject)) != null ) {
					_fieldModifiers.add(m.bind(this, mod.parameters, mod.sLoc));
				}
			}
			
			FieldSpec f;
			if( referenceBody != null ) {
				ArrayList<ForeignKeySpec.Component> fkComponents = new ArrayList<ForeignKeySpec.Component>();
				for( Command fkCommand : referenceBody.commands ) {
					String foreignFieldName = singleString( fkCommand.subject, "foreign field name" );
					
					Command localFieldNode;
					if( fkCommand.body != null ) {
						if( fkCommand.body.commands.length != 1 ) {
							throw new InterpretError(
								"Foreign key component requires exactly 0 or 1 local field specifications; given "+
								fkCommand.body.commands.length, fkCommand.body.sLoc
							);
						}
						if( fkCommand.modifiers.length != 0 ){
							throw new InterpretError(
								"Modifiers not allowed for foreign field specification",
								fkCommand.modifiers[0].sLoc
							);
						}
						localFieldNode = fkCommand.body.commands[0];
					} else {
						localFieldNode = fkCommand;
					}
					
					String localFieldName = singleString(localFieldNode.subject, "local field name");
					FieldSpec localField = getSimpleField( t, localFieldNode );;
					
					// TODO: Implement rest of this
				}
				//fkSpec
				//fieldType = new ForeignKeyReferenceType( singleString(mod.parameters[0], "referenced class name"), Types.REFERENCE, fkSpec);
			} else {
				f = defineSimpleField( t, fieldCommand );
				for( Modifier _m : _fieldModifiers ) {
					_m.apply(f);
				}
			}
		}
		
		for( Parameterized mod : modifiers ) {
			if( (m = findClassModifierSpec(mod.subject)) != null ) {
				m.bind(this, mod.parameters, mod.sLoc).apply(t);
			} else {
				throw new InterpretError("Unrecognised class modifier: '"+mod.subject+"'", mod.sLoc);
			}
		}
		
		if( PropertyUtil.isTrue(t.getProperties(), Predicates.IS_SELF_KEYED) ) {
			t.indexesByName.put("primary", new IndexSpec("primary", t.fieldsByName.values()));
		}
		
		return t;
	}
	
	private EnumType parseEnum(String name, Parameterized[] modifiers, Block body) throws InterpretError {
		EnumType t = new EnumType(name);
		
		ModifierSpec m;
		for( Parameterized mod : modifiers ) {
			if( (m = findClassModifierSpec(mod.subject)) != null ) {
				m.bind(this, mod.parameters, mod.sLoc).apply(t);
			} else {
				throw new InterpretError("Unrecognised class modifier: '"+mod.subject+"'", mod.sLoc);
			}
		}
		
		for( Command c : body.commands ) {
			ensureNoParameters(c.subject, "enum value");
			for( Parameterized mod : c.modifiers ) {
				throw new InterpretError("Enum value modifiers are ignored", mod.sLoc);
			}
			if( c.body.commands.length > 0 ) {
				throw new InterpretError("Enum value body is ignored", c.body.sLoc);
			}
			
			t.addValidValue(c.subject.subject.unquotedText());
		}
		
		return t;
	}
	
	protected void ensureNoParameters( Parameterized s, String context ) throws InterpretError {
		for( Parameterized classNameParameter : s.parameters ) {
			throw new InterpretError(context + " cannot have parameters", classNameParameter.sLoc );
		}
	}
	
	@Override public void data(Command value) throws Exception {
		Phrase cmd = value.subject.subject;
		if( cmd.startsWithWords("field","property") ) {
			defineFieldPredicate( parseProperty( cmd.tail(2).unquotedText(), value.modifiers, value.body ) );
		} else if( cmd.startsWithWords("field","modifier") ) {
			String name = cmd.tail(2).unquotedText();
			defineFieldModifier( name, parseFieldModifierSpec(name, value.modifiers, value.body) );
		} else if( cmd.startsWithWords("class","modifier") ) {
			String name = cmd.tail(2).unquotedText();
			defineClassModifier( name, parseFieldModifierSpec(name, value.modifiers, value.body) );
		} else if( cmd.startsWithWords("class","property") ) {
			defineClassPredicate( parseProperty( cmd.tail(2).unquotedText(), value.modifiers, value.body ) );
		} else if( cmd.startsWithWord("class") ) {
			ensureNoParameters(value.subject, "class name");
			defineType( parseClass( value.subject.subject.tail().unquotedText(), value.modifiers, value.body ) );
		} else if( cmd.startsWithWord("enum") ) {
			ensureNoParameters(value.subject, "enum name");
			defineType( parseEnum( value.subject.subject.tail().unquotedText(), value.modifiers, value.body ) );
		} else {
			throw new InterpretError("Unrecognised command: '"+cmd+"'", value.sLoc);
		}
	}

	@Override public void end() throws Exception {
		_end();
	}
	
	//// Convenience methods for when you don't feel like setting
	//// up your own tokenizers and yaddah yaddah yaddah
	
	public void parse( Reader r, String sourceName ) throws IOException, InterpretError {
		Tokenizer t = new Tokenizer();
		if( sourceName != null ) t.setSourceLocation( sourceName, 1, 1 );
		Parser p = new Parser();
		p.pipe(this);
		t.pipe(p);
		try {
			StreamUtil.pipe( r, t );
		} catch( InterpretError e ) {
			throw e;
		} catch( Exception e ) {
			throw new RuntimeException(e);
		}
	}
	
	public void parse( String source, String sourceName ) throws InterpretError {
		try {
			parse( new StringReader(source), sourceName );
		} catch( IOException e ) {
			throw new RuntimeException(e);
		}
	}
}

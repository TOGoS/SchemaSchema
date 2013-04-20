package togos.schemaschema.parser;


public class CommandInterpreters
{
	private CommandInterpreters() { }
	
	public static void defineTypeDefinitionCommands( SchemaParser sp ) {
		sp.defineCommand("class", sp.new ClassDefinitionCommandInterpreter());
		sp.defineCommand("enum", sp.new EnumDefinitionCommandInterpreter());
		sp.defineCommand("property", sp.new PropertyDefinitionCommandInterpreter(sp.generalModifiers));
		sp.defineCommand("modifier", sp.new ModifierDefinitionCommandInterpreter(sp.generalModifiers, "modifier"));
		sp.defineCommand("field property", sp.new PropertyDefinitionCommandInterpreter(sp.fieldModifiers));
		sp.defineCommand("class property", sp.new PropertyDefinitionCommandInterpreter(sp.classModifiers));
		sp.defineCommand("field modifier", sp.new ModifierDefinitionCommandInterpreter(sp.fieldModifiers, "field modifier"));
		sp.defineCommand("class modifier", sp.new ModifierDefinitionCommandInterpreter(sp.classModifiers, "class modifier"));
	}
}

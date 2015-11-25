package togos.schemaschema;

import java.util.ArrayList;
import java.util.List;

public class ListUtil
{
	public static <T> List<T> appended(List<T> l, T v) {
		l = new ArrayList<T>(l);
		l.add(v);
		return l;
	}
}

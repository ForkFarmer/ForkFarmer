package util.swing.jfuntable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TField {
	public String name;
	final Supplier<String> supplier;
	final Consumer<String> consumer;

	public TField(final String name, final Supplier<String> supplier, final Consumer<String> consumer)
	{
		this.name = name;
		this.supplier = supplier;
		this.consumer = consumer;
	}
	
}
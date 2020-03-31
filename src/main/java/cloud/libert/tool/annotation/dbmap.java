package cloud.libert.tool.annotation;

public @interface dbmap {
	public int length() default 0;
	public boolean isIndex() default false;
	public boolean isMap() default true;
}

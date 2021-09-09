package fr.theoszanto.webserver.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonExclude {
	boolean fromSerialization() default true;

	boolean fromDeserialization() default true;

	class Strategy implements ExclusionStrategy {
		private final boolean serialization;

		public Strategy(boolean serialization) {
			this.serialization = serialization;
		}

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			JsonExclude exclude = f.getAnnotation(JsonExclude.class);
			if (exclude == null)
				return false;
			return this.serialization ? exclude.fromSerialization() : exclude.fromDeserialization();
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	}
}

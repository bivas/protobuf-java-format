/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package com.googlecode.protobuf.format;

import java.lang.reflect.InvocationTargetException;

public class FormatFactory {
	
	public FormatFactory() {}
	
	public enum Formatter {
		COUCHDB (CouchDBFormat.class),
		HTML (HtmlFormat.class),
		JAVA_PROPS (JavaPropsFormat.class),
		JSON (JsonFormat.class),
		XML (XmlFormat.class),
		SMILE (SmileFormat.class),
		JSON_JACKSON (JsonJacksonFormat.class),
		XML_JAVAX (XmlJavaxFormat.class);
		
		private Class<? extends ProtobufFormatter> formatterClass;
		Formatter(Class<? extends ProtobufFormatter> formatterClass) {
			this.formatterClass = formatterClass;
		}
		protected Class<? extends ProtobufFormatter> getFormatterClass() {
			return formatterClass;
		}
	}
	

	public static class ProtobufFormatterBuilder {
		private final Formatter formatter;
		private EnumWriteMode enumWriteMode = EnumWriteMode.NAME;

		public ProtobufFormatterBuilder(Formatter formatter) {
			this.formatter = formatter;
		}

		public ProtobufFormatterBuilder withEnumWriteMode(EnumWriteMode enumWriteMode) {
			this.enumWriteMode = enumWriteMode;
			return this;
		}

		public ProtobufFormatter build() {
			try {
				return formatter.getFormatterClass().getConstructor(EnumWriteMode.class).newInstance(enumWriteMode);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public ProtobufFormatterBuilder createFormatter(Formatter formatter) {
		return new ProtobufFormatterBuilder(formatter);
	}
}

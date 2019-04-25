package org.chare.maven.plugins.updateXmlFile;

import static java.text.MessageFormat.format;
import static org.chare.maven.plugins.updateXmlFile.FileUtils.convertToFile;
import static org.chare.maven.plugins.updateXmlFile.Property.SOURCE_TYPE_FILE;
import static org.chare.maven.plugins.updateXmlFile.Property.SOURCE_TYPE_VARIABLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertyReader {
	private final Properties projectProperties;

	public PropertyReader(Properties projectProperties) {
		this.projectProperties = new Properties(projectProperties);
	}

	public List<Property> readProperties(List<String> lines) {
		List<Property> properties = new ArrayList<>();
		for (String line : lines) {
			Property property = createProperty(line);
			properties.add(property);
		}
		return properties;
	}

	private Property createProperty(String line) {
		String[] parts = line.split(";");
		String targetPropertyXPath = parts[0];
		String sourceType = parts[1];
		String reference = parts[2];

		if (sourceType.equals(SOURCE_TYPE_VARIABLE)) {
			String propertyValue = getValue(reference, projectProperties);
			return new Property(targetPropertyXPath, sourceType, propertyValue);
		} else if (sourceType.equals(SOURCE_TYPE_FILE)) {
			String[] referenceParts = reference.split(":");
			String fileName = referenceParts[0];
			String propertyValue = getValue(fileName, projectProperties);
			String filePath = convertToFile(propertyValue).getAbsolutePath();
			return new Property(targetPropertyXPath, sourceType, reference.replace(fileName, filePath));
		} else {
			throw new IllegalStateException("Unknown property source type " + sourceType);
		}
	}

	private static String getValue(String valueName, Properties projectProperties) {
		String value = System.getProperty(valueName) == null ? projectProperties.getProperty(valueName)
				: System.getProperty(valueName);
		if (value == null) {
			throw new IllegalStateException(createValueNotFoundErrorMessage(valueName));
		}
		return value;
	}

	private static String createValueNotFoundErrorMessage(String valueName) {
		return format("Could not find value of \"{0}\" that was defined in the configuration file. "
				+ "Add a value to it using maven parameter -D{0}=value", valueName);
	}
}
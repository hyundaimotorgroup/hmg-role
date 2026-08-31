package com.hmg.role;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("/features") // This selector is picked up by Cucumber
@SelectPackages("com.hmg.role")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty")
public class CucumberIntegrationTest {}

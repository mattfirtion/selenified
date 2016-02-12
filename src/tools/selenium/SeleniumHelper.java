package tools.selenium;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;

import secureci.exceptions.InvalidActionException;
import secureci.exceptions.InvalidBrowserException;
import secureci.exceptions.InvalidLocatorTypeException;
import tools.logging.TestOutput;
import tools.logging.TestOutput.Result;

/**
 * Selenium Webdriver Before each action is performed a screenshot is taken
 * After each check is performed a screenshot is taken These are all placed into
 * the output file
 * 
 * @author Max Saperstone
 * @version 1.0.2
 * @lastupdate 1/3/2014
 */
public class SeleniumHelper {

	// this will be the name of the file we write all commands out to
	private TestOutput output;

	// what locator actions are available in webdriver
	/**
	 * Select a Locator for the element we are interacting with Available
	 * options are: xpath, id, name, classname, paritallinktext, linktext,
	 * tagname
	 */
	public enum Locators {
		xpath, id, name, classname, paritallinktext, linktext, tagname
	};

	// what browsers are we interested in implementing
	/**
	 * Select a browser to run Available options are: HtmlUnit (only locally -
	 * not on grid), Firefox, Chrome, InternetExplorer, Android, Ipad (only
	 * locally - not on grid), Iphone (only locally, not on grid, Opera, Safari
	 */
	public enum Browsers {
		None, HtmlUnit, Firefox, Chrome, InternetExplorer, Android, Ipad, Iphone, Opera, Safari
	};

	// this is our driver that will be used for all selenium actions
	private WebDriver driver;
	// this is the browser that we are using
	private Browsers browser;

	/**
	 * our constructor, determining which browser use and how to run the
	 * browser: either grid or standalone
	 * 
	 * @param output
	 *            - the TestOutput file. This is provided by the
	 *            SeleniumTestBase functionality
	 * @throws InvalidBrowserException
	 * @throws MalformedURLException
	 */
	public SeleniumHelper(TestOutput output) throws InvalidBrowserException,
			MalformedURLException {
		browser = Browsers.valueOf(System.getProperty("browser"));
		this.output = output;
		if (System.getProperty("hubAddress") != "LOCAL") {
			DesiredCapabilities capability;
			String hubAddress = "http://" + System.getProperty("hubAddress")
					+ ":4444/wd/hub";
			switch (browser) { // check our browser
			case HtmlUnit: {
				capability = DesiredCapabilities.htmlUnit();
				break;
			}
			case Firefox: {
				capability = DesiredCapabilities.firefox();
				break;
			}
			case Chrome: {
				capability = DesiredCapabilities.chrome();
				break;
			}
			case InternetExplorer: {
				capability = DesiredCapabilities.internetExplorer();
				break;
			}
			case Android: {
				capability = DesiredCapabilities.android();
				break;
			}
			case Iphone: {
				capability = DesiredCapabilities.iphone();
				break;
			}
			case Ipad: {
				capability = DesiredCapabilities.ipad();
				break;
			}
			case Safari: {
				capability = DesiredCapabilities.safari();
				break;
			}
			case Opera: {
				capability = DesiredCapabilities.opera();
				break;
			}
			// if our browser is not listed, throw an error
			default: {
				throw new InvalidBrowserException("The selected browser "
						+ browser);
			}
			}
			driver = new RemoteWebDriver(new URL(hubAddress), capability);
		} else {
			switch (browser) { // check our browser
			case HtmlUnit: {
				driver = new HtmlUnitDriver();
				break;
			}
			// TODO - do we enable javascript - will emulate firefox 3.6's
			// javascript handling - new HtmlUnitDriver(true)
			case Firefox: {
				driver = new FirefoxDriver();
				break;
			}
			case Chrome: {
				driver = new ChromeDriver();
				break;
			}
			case InternetExplorer: {
				driver = new InternetExplorerDriver();
				break;
			}
			// case Android: { driver = new AndroidDriver(); break; }
			// case Iphone: { driver = new IPhoneDriver(); break; }
			// case Ipad: { driver = new IPadDriver(); break; }
			case Safari: {
				driver = new SafariDriver();
				break;
			}
			case Opera: {
				driver = new OperaDriver();
				break;
			}
			// if our browser is not listed, throw an error
			default: {
				throw new InvalidBrowserException("The selected browser "
						+ browser);
			}
			}
		}
		// driver.manage().window().maximize();
		output.setSelHelper(this);
	}

	/**
	 * a method to allow retrieving our driver instance
	 * 
	 * @return WebDriver: access to the driver controlling our browser via
	 *         webdriver
	 */
	public WebDriver getDriver() {
		return driver;
	}

	/**
	 * a method to allow retrieving our browser
	 * 
	 * @return Browsers: the enum of the browser
	 */
	public Browsers getBrowser() {
		return browser;
	}

	/**
	 * a method to end our driver instance
	 */
	public void killDriver() {
		if (driver != null) {
			driver.quit();
		}
	}

	// ///////////////////////////////////////
	// generic navigational functionality
	// ///////////////////////////////////////

	/**
	 * a method for navigating to a new url
	 * 
	 * @param URL
	 *            : the URL to navigate to
	 */
	public int goToURL(String URL) {
		String action = "Loading " + URL;
		String expected = "Loaded " + URL;
		long start = System.currentTimeMillis();
		try {
			driver.get(URL);
		} catch (Exception e) {
			output.recordAction(action, expected, "Fail to Load " + URL,
					Result.FAILURE);
			return 1;
		}
		double timetook = System.currentTimeMillis() - start;
		timetook = timetook / 1000;
		output.recordAction(action, expected, "Loaded " + URL + " in "
				+ timetook + " seconds", Result.SUCCESS);
		return 0;
	}

	// ///////////////////////////////////////
	// waiting functionality
	// ///////////////////////////////////////

	/**
	 * a method for allowing Selenium to pause for a set amount of time
	 * 
	 * @param seconds
	 *            : the number of seconds to wait
	 */
	public int wait(int seconds) {
		return wait(Double.valueOf(seconds));
	}

	/**
	 * a method for allowing Selenium to pause for a set amount of time
	 * 
	 * @param seconds
	 *            : the number of seconds to wait
	 */
	public int wait(double seconds) {
		String action = "Wait " + seconds + " seconds";
		String expected = "Waited " + seconds + " seconds";
		try {
			Thread.sleep((long) (seconds * 1000));
		} catch (InterruptedException e) {
			output.recordAction(action, expected, "Failed to wait " + seconds
					+ " seconds", Result.FAILURE);
			return 1;
		}
		output.recordAction(action, expected, "Waited " + seconds + " seconds",
				Result.SUCCESS);
		return 0;
	}

	/**
	 * a method for waiting until an element is present for a maximum of 5
	 * seconds
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int waitForElementPresent(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		return waitForElementPresent(type, locator, 5);
	}

	/**
	 * a method for waiting until an element is present
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param seconds
	 *            : the number of seconds to wait
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int waitForElementPresent(Locators type, String locator, int seconds)
			throws InvalidActionException, InvalidLocatorTypeException {
		String action = "Wait up to " + seconds + " seconds for " + type + " "
				+ locator + " to be present";
		String expected = type + " " + locator + " is present";
		// wait for up to XX seconds for our error message
		long end = System.currentTimeMillis() + (seconds * 1000);
		while (System.currentTimeMillis() < end) {
			try { // If results have been returned, the results are displayed in
					// a drop down.
				getWebElement(type, locator).getText();
				break;
			} catch (NoSuchElementException e) {
			}
		}
		double timetook = Math.min(
				(seconds * 1000) - (end - System.currentTimeMillis()),
				seconds * 1000);
		timetook = timetook / 1000;
		if (!isElementPresent(type, locator, false)) {
			output.recordAction(action, expected, "After waiting " + timetook
					+ " seconds for " + type + " " + locator
					+ " is not present", Result.FAILURE);
			return 1;
		}
		output.recordAction(action, expected, "Waited " + timetook
				+ " seconds for " + type + " " + locator + " to be present",
				Result.SUCCESS);
		return 0;
	}

	/**
	 * a method for waiting until an element is no longer present for a maximum
	 * of 5 seconds
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int waitForElementNotPresent(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		return waitForElementNotPresent(type, locator, 5);
	}

	/**
	 * a method for waiting until an element is no longer present
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param seconds
	 *            : the number of seconds to wait
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int waitForElementNotPresent(Locators type, String locator,
			int seconds) throws InvalidActionException,
			InvalidLocatorTypeException {
		String action = "Wait up to " + seconds + " seconds for " + type + " "
				+ locator + " to not be present";
		String expected = type + " " + locator + " is not present";
		// wait for up to XX seconds for our error message
		long end = System.currentTimeMillis() + (seconds * 1000);
		while (System.currentTimeMillis() < end) {
			if (!isElementPresent(type, locator)) {
				break;
			}
		}
		double timetook = Math.min(
				(seconds * 1000) - (end - System.currentTimeMillis()),
				seconds * 1000);
		timetook = timetook / 1000;
		if (isElementPresent(type, locator)) {
			output.recordAction(action, expected, "After waiting " + timetook
					+ " seconds for " + type + " " + locator
					+ " is still present", Result.FAILURE);
			return 1;
		}
		output.recordAction(action, expected,
				"Waited " + timetook + " seconds for " + type + " " + locator
						+ " to not be present", Result.SUCCESS);
		return 0;
	}

	/**
	 * a method for waiting until an element is displayed for a maximum of 5
	 * seconds
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int waitForElementDisplayed(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		return waitForElementDisplayed(type, locator, 5);
	}

	/**
	 * a method for waiting until an element is displayed
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param seconds
	 *            : the number of seconds to wait
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int waitForElementDisplayed(Locators type, String locator,
			int seconds) throws InvalidActionException,
			InvalidLocatorTypeException {
		String action = "Wait up to " + seconds + " seconds for " + type + " "
				+ locator + " to be displayed";
		String expected = type + " " + locator + " is displayed";
		long start = System.currentTimeMillis();
		if (!isElementPresent(type, locator, false)) {
			int success = waitForElementPresent(type, locator, seconds);
			if (success == 1) {
				return success;
			}
		}
		WebElement element = getWebElement(type, locator);
		if (!element.isDisplayed()) {
			// wait for up to XX seconds
			long end = System.currentTimeMillis() + (seconds * 1000);
			while (System.currentTimeMillis() < end) {
				if (element.isDisplayed()) {
					break;
				}
			}
		}
		double timetook = (System.currentTimeMillis() - start) / 1000;
		if (!element.isDisplayed()) {
			output.recordAction(action, expected, "After waiting " + timetook
					+ " seconds for " + type + " " + locator
					+ " is not displayed", Result.FAILURE);
			return 1;
		}
		output.recordAction(action, expected, "Waited " + timetook
				+ " seconds for " + type + " " + locator + " to be displayed",
				Result.SUCCESS);
		return 0;
	}

	/**
	 * a method for waiting until an element is not displayed for a maximum of 5
	 * seconds
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int waitForElementNotDisplayed(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		return waitForElementNotDisplayed(type, locator, 5);
	}

	/**
	 * a method for waiting until an element is not displayed
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param seconds
	 *            : the number of seconds to wait
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int waitForElementNotDisplayed(Locators type, String locator,
			int seconds) throws InvalidActionException,
			InvalidLocatorTypeException {
		// TODO - this might fail if the element disappears completely
		String action = "Wait up to " + seconds + " seconds for " + type + " "
				+ locator + " to not be displayed";
		String expected = type + " " + locator + " is not displayed";
		long start = System.currentTimeMillis();
		WebElement element = getWebElement(type, locator);
		if (element.isDisplayed()) {
			// wait for up to XX seconds
			long end = System.currentTimeMillis() + (seconds * 1000);
			while (System.currentTimeMillis() < end) {
				if (!element.isDisplayed()) {
					break;
				}
			}
		}
		double timetook = (System.currentTimeMillis() - start) / 1000;
		if (element.isDisplayed()) {
			output.recordAction(action, expected, "After waiting " + timetook
					+ " seconds for " + type + " " + locator
					+ " is still displayed", Result.FAILURE);
			return 1;
		}
		output.recordAction(action, expected, "Waited " + timetook
				+ " seconds for " + type + " " + locator
				+ " to not be displayed", Result.SUCCESS);
		return 0;
	}

	/**
	 * a method for waiting until an element is enabled for a maximum of 5
	 * seconds
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int waitForElementEnabled(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		return waitForElementEnabled(type, locator, 5);
	}

	/**
	 * a method for waiting until an element is enabled
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param seconds
	 *            : the number of seconds to wait
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int waitForElementEnabled(Locators type, String locator, int seconds)
			throws InvalidLocatorTypeException, InvalidActionException {
		String action = "Wait up to " + seconds + " seconds for " + type + " "
				+ locator + " to be enabled";
		String expected = type + " " + locator + " is enabled";
		long start = System.currentTimeMillis();
		if (!isElementEnabled(type, locator, false)) {
			if (!isElementPresent(type, locator, false)) {
				waitForElementPresent(type, locator, seconds);
			}
			if (!isElementEnabled(type, locator, false)) {
				WebElement element = getWebElement(type, locator);
				// wait for up to XX seconds for our error message
				long end = System.currentTimeMillis() + (seconds * 1000);
				while (System.currentTimeMillis() < end) {
					// If results have been returned, the results are displayed
					// in a drop down.
					if (element.isEnabled()) {
						break;
					}
				}
			}
		}
		double timetook = (System.currentTimeMillis() - start) / 1000;
		if (!isElementEnabled(type, locator, false)) {
			output.recordAction(action, expected, "After waiting " + timetook
					+ " seconds for " + type + " " + locator
					+ " is not enabled", Result.FAILURE);
			return 1;
		}
		output.recordAction(action, expected, "Waited " + timetook
				+ " seconds for " + type + " " + locator + " to be enabled",
				Result.SUCCESS);
		return 0;
	}

	/**
	 * a method for waiting until an element is not enabled for a maximum of 5
	 * seconds
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int waitForElementNotEnabled(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		return waitForElementNotEnabled(type, locator, 5);
	}

	/**
	 * a method for waiting until an element is not enabled
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param seconds
	 *            : the number of seconds to wait
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int waitForElementNotEnabled(Locators type, String locator,
			int seconds) throws InvalidLocatorTypeException,
			InvalidActionException {
		// TODO - this might fail if the element is no longer present

		String action = "Wait up to " + seconds + " seconds for " + type + " "
				+ locator + " to not be enabled";
		String expected = type + " " + locator + " is not enabled";
		long start = System.currentTimeMillis();
		WebElement element = getWebElement(type, locator);
		if (element.isEnabled()) {
			// wait for up to XX seconds
			long end = System.currentTimeMillis() + (seconds * 1000);
			while (System.currentTimeMillis() < end) {
				if (!element.isEnabled()) {
					break;
				}
			}
		}
		double timetook = (System.currentTimeMillis() - start) / 1000;
		if (element.isDisplayed()) {
			output.recordAction(action, expected, "After waiting " + timetook
					+ " seconds for " + type + " " + locator
					+ " is still enabled", Result.FAILURE);
			return 1;
		}
		output.recordAction(action, expected,
				"Waited " + timetook + " seconds for " + type + " " + locator
						+ " to not be enabled", Result.SUCCESS);
		return 0;
	}

	// ////////////////////////////////////
	// checking element availability
	// ////////////////////////////////////

	/**
	 * a method for checking if an element is present
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return boolean: whether the element is present or not
	 * @throws InvalidLocatorTypeException
	 * @throws InvalidActionException
	 */
	public boolean isElementPresent(Locators type, String locator)
			throws InvalidLocatorTypeException, InvalidActionException {
		return isElementPresent(type, locator, false);
	}

	/**
	 * a method for checking if an element is present
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param print
	 *            : whether or not to printout the action
	 * @return boolean: whether the element is present or not
	 * @throws InvalidLocatorTypeException
	 * @throws InvalidActionException
	 */
	public boolean isElementPresent(Locators type, String locator, boolean print)
			throws InvalidLocatorTypeException, InvalidActionException {
		boolean isPresent = false;
		try {
			getWebElement(type, locator).getText();
			isPresent = true;
		} catch (NoSuchElementException e) {
		}
		if (print) {
			output.recordExpected("Checking for " + type + " " + locator
					+ " to be present");
		}
		return isPresent;
	}

	/**
	 * a method for checking if an element is enabled
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return boolean: whether the element is present or not
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public boolean isElementEnabled(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		return isElementEnabled(type, locator, false);
	}

	/**
	 * a method for checking if an element is enabled
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param print
	 *            : whether or not to printout the action
	 * @return boolean: whether the element is present or not
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public boolean isElementEnabled(Locators type, String locator, boolean print)
			throws InvalidActionException, InvalidLocatorTypeException {
		boolean isEnabled = false;
		try {
			isEnabled = getWebElement(type, locator).isEnabled();
		} catch (NoSuchElementException e) {
		}
		if (print) {
			output.recordExpected("Checking for " + type + " " + locator
					+ " to be enabled");
		}
		return isEnabled;
	}

	/**
	 * a method for checking if an element is checked
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return boolean: whether the element is checked or not
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public boolean isElementChecked(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		return isElementChecked(type, locator, false);
	}

	/**
	 * a method for checking if an element is checked
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param print
	 *            : whether or not to printout the action
	 * @return boolean: whether the element is checked or not
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public boolean isElementChecked(Locators type, String locator, boolean print)
			throws InvalidActionException, InvalidLocatorTypeException {
		boolean isChecked = false;
		try {
			isChecked = getWebElement(type, locator).isSelected();
		} catch (NoSuchElementException e) {
		}
		if (print) {
			output.recordExpected("Checking for " + type + " " + locator
					+ " to be checked");
		}
		return isChecked;
	}

	/**
	 * a method for checking if an element is displayed
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return boolean: whether the element is displayed or not
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public boolean isElementDisplayed(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		return isElementDisplayed(type, locator, false);
	}

	/**
	 * a method for checking if an element is displayed
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param print
	 *            : whether or not to printout the action
	 * @return boolean: whether the element is displayed or not
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public boolean isElementDisplayed(Locators type, String locator,
			boolean print) throws InvalidActionException,
			InvalidLocatorTypeException {
		boolean isDisplayed = false;
		try {
			isDisplayed = getWebElement(type, locator).isDisplayed();
		} catch (NoSuchElementException e) {
		}
		if (print) {
			output.recordExpected("Checking for " + type + " " + locator
					+ " to be displayed");
		}
		return isDisplayed;
	}

	// ///////////////////////////////////
	// selenium retreval functions
	// ///////////////////////////////////

	/**
	 * get the number of options from the select drop down
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer: the number of select options
	 * @throws InvalidLocatorTypeException
	 * @throws InvalidActionException
	 */
	public int getNumOfSelectOptions(Locators type, String locator)
			throws InvalidLocatorTypeException, InvalidActionException {
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			return 0;
		}
		WebElement element = getWebElement(type, locator);
		List<WebElement> allOptions = element
				.findElements(By.tagName("option"));
		return allOptions.size();
	}

	/**
	 * get the options from the select drop down
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return String[]: the options from the select element
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public String[] getSelectOptions(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			return new String[0];
		}
		WebElement element = getWebElement(type, locator);
		List<WebElement> allOptions = element
				.findElements(By.tagName("option"));
		String[] options = new String[allOptions.size()];
		for (int i = 0; i < allOptions.size(); i++) {
			options[i] = allOptions.get(i).getAttribute("value");
		}
		return options;
	}

	/**
	 * get the rows of a table
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return List<WebElement>: a list of the table rows
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public List<WebElement> getTableRows(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			return new ArrayList<WebElement>();
		}
		WebElement element = getWebElement(type, locator);
		// TODO - this locator may need to be updated
		return element.findElements(By.tagName("tr"));
	}

	/**
	 * get the number of rows of a table
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer: the number of table rows
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int getNumOfTableRows(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		List<WebElement> rows = getTableRows(type, locator);
		return rows.size();
	}

	/**
	 * get the columns of a table
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return List<WebElement>: a list of the table columns
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public List<WebElement> getTableColumns(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			return new ArrayList<WebElement>();
		}
		WebElement element = getWebElement(type, locator);
		// TODO - this locator may need to be updated
		return element.findElements(By.xpath(".//tr[1]/th"));
	}

	/**
	 * get the number of columns of a table
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer: the number of table columns
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int getNumOfTableColumns(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		List<WebElement> columns = getTableColumns(type, locator);
		return columns.size();
	}

	/**
	 * a method to retrieve the row number in a table that has a header (th) of
	 * the indicated value
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param header
	 *            : the full text value expected in a th cell
	 * @return Integer: the row number containing the header
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int getTableRowWHeader(Locators type, String locator, String header)
			throws InvalidActionException, InvalidLocatorTypeException {
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			return 0; // indicates table not found
		}
		List<WebElement> tables = getWebElements(type, locator);
		for (WebElement table : tables) {
			// TODO - might want to redo this logical check
			List<WebElement> rows = table.findElements(By.tagName("tr"));
			int counter = 1;
			for (WebElement row : rows) {
				// TODO - might want to redo this logical check
				if (row.findElement(By.xpath(".//td[1]|.//th[1]")).getText()
						.equals(header)) {
					return counter;
				}
				counter++;
			}
		}
		return 0; // indicates header not found
	}

	/**
	 * get a specific column from a table
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param colNum
	 *            : the column number of the table to obtain - note, column
	 *            numbering starts at 1, NOT 0
	 * @return List<WebElement>: a list of the table cells in the columns
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public List<WebElement> getTableColumn(Locators type, String locator,
			int colNum) throws InvalidActionException,
			InvalidLocatorTypeException {
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			return new ArrayList<WebElement>(); // indicates table not found
		}
		List<WebElement> tables = getWebElements(type, locator);
		List<WebElement> column = tables.get(0).findElements(
				By.className("NONEEXISTS")); // cludge to initialize
		for (WebElement table : tables) {
			// TODO - this locator may need to be updated
			List<WebElement> cells = table.findElements(By.xpath(".//th["
					+ colNum + "]|.//td[" + colNum + "]"));
			column.addAll(cells);
		}
		return column;
	}

	/**
	 * get the contents of a specific cell
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param row
	 *            : the number of the row in the table - note, row numbering
	 *            starts at 1, NOT 0
	 * @param col
	 *            : the number of the column in the table - note, column
	 *            numbering starts at 1, NOT 0
	 * @return WebElement: the cell element object, and all associated values
	 *         with it
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public WebElement getTableCell(Locators type, String locator, int row,
			int col) throws InvalidActionException, InvalidLocatorTypeException {
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			return null; // indicates table not found
		}
		List<WebElement> tables = getWebElements(type, locator);
		for (WebElement table : tables) {
			// TODO - this locator may need to be updated
			return table.findElement(By.xpath(".//tr[" + row + "]/td[" + col
					+ "]"));
		}
		return null; // indicates cell not present
	}

	// ///////////////////////////////////
	// selenium actions functionality
	// ///////////////////////////////////

	/**
	 * our generic selenium click functionality implemented
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int click(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		String action = "Clicking " + type + " " + locator;
		String expected = type + " " + locator
				+ " is present, displayed, and enabled to be clicked";
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			output.recordAction(action, expected, "Unable to click " + type
					+ " " + locator + " as it is not present", Result.FAILURE);
			return 1; // indicates element not present
		}
		// wait for element to be displayed
		if (!isElementDisplayed(type, locator, false)) {
			waitForElementDisplayed(type, locator);
		}
		if (!isElementDisplayed(type, locator, false)) {
			output.recordAction(action, expected, "Unable to click " + type
					+ " " + locator + " as it is not displayed", Result.FAILURE);
			return 1; // indicates element not displayed
		}
		// wait for element to be enabled
		if (!isElementEnabled(type, locator, false)) {
			waitForElementEnabled(type, locator);
		}
		if (!isElementEnabled(type, locator, false)) {
			output.recordAction(action, expected, "Unable to click " + type
					+ " " + locator + " as it is not enabled", Result.FAILURE);
			return 1; // indicates element not enabled
		}
		WebElement element = getWebElement(type, locator);
		element.click();
		output.recordAction(action, expected,
				"Clicked " + type + " " + locator, Result.SUCCESS);
		return 0;
	}

	/**
	 * our generic selenium submit functionality implemented
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int submit(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		String action = "Submitting " + type + " " + locator;
		String expected = type + " " + locator
				+ " is present, displayed, and enabled to be submitted	";
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			output.recordAction(action, expected, "Unable to submit " + type
					+ " " + locator + " as it is not present", Result.FAILURE);
			return 1; // indicates element not present
		}
		// wait for element to be displayed
		if (!isElementDisplayed(type, locator, false)) {
			waitForElementDisplayed(type, locator);
		}
		if (!isElementDisplayed(type, locator, false)) {
			output.recordAction(action, expected, "Unable to submit " + type
					+ " " + locator + " as it is not displayed", Result.FAILURE);
			return 1; // indicates element not displayed
		}
		// wait for element to be enabled
		if (!isElementEnabled(type, locator, false)) {
			waitForElementEnabled(type, locator);
		}
		if (!isElementEnabled(type, locator, false)) {
			output.recordAction(action, expected, "Unable to submit " + type
					+ " " + locator + " as it is not enabled", Result.FAILURE);
			return 1; // indicates element not enabled
		}
		WebElement element = getWebElement(type, locator);
		element.submit();
		output.recordAction(action, expected, "Submitted " + type + " "
				+ locator, Result.SUCCESS);
		return 0;
	}

	/**
	 * a method to simulate the mouse hovering over an element
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int hover(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		String action = "Hovering over " + type + " " + locator;
		String expected = type + " " + locator
				+ " is present, and displayed to be hovered over";
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			output.recordAction(action, expected, "Unable to hover over "
					+ type + " " + locator + " as it is not present",
					Result.FAILURE);
			return 1; // indicates element not present
		}
		// wait for element to be displayed
		if (!isElementDisplayed(type, locator, false)) {
			waitForElementDisplayed(type, locator);
		}
		if (!isElementDisplayed(type, locator, false)) {
			output.recordAction(action, expected, "Unable to hover over "
					+ type + " " + locator + " as it is not displayed",
					Result.FAILURE);
			return 1; // indicates element not displayed
		}
		Actions selAction = new Actions(driver);
		WebElement element = getWebElement(type, locator);
		selAction.moveToElement(element).perform();
		output.recordAction(action, expected, "Hovered over " + type + " "
				+ locator, Result.SUCCESS);
		return 0;
	}

	/**
	 * our generic selenium type functionality implemented
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param text
	 *            : the text to be typed in
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int type(Locators type, String locator, String text)
			throws InvalidActionException, InvalidLocatorTypeException {
		String action = "Typing text '" + text + "' in " + type + " " + locator;
		String expected = type + " " + locator
				+ " is present, displayed, and enabled to have text " + text
				+ " typed in";
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			output.recordAction(action, expected, "Unable to type in " + type
					+ " " + locator + " as it is not present", Result.FAILURE);
			return 1; // indicates element not present
		}
		// wait for element to be displayed
		if (!isElementDisplayed(type, locator, false)) {
			waitForElementDisplayed(type, locator);
		}
		if (!isElementDisplayed(type, locator, false)) {
			output.recordAction(action, expected, "Unable to type in " + type
					+ " " + locator + " as it is not displayed", Result.FAILURE);
			return 1; // indicates element not displayed
		}
		// wait for element to be enabled
		if (!isElementEnabled(type, locator, false)) {
			waitForElementEnabled(type, locator);
		}
		if (!isElementEnabled(type, locator, false)) {
			output.recordAction(action, expected, "Unable to type in " + type
					+ " " + locator + " as it is not enabled", Result.FAILURE);
			return 1; // indicates element not enabled
		}
		WebElement element = getWebElement(type, locator);
		element.sendKeys(text);
		output.recordAction(action, expected, "Typed text '" + text + "' in "
				+ type + " " + locator, Result.SUCCESS);
		return 0;
	}

	/**
	 * our generic select selenium functionality
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param value
	 *            : the select option to be selected - note, row numbering
	 *            starts at 0
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int select(Locators type, String locator, int value)
			throws InvalidActionException, InvalidLocatorTypeException {
		String[] options = getSelectOptions(type, locator);
		return select(type, locator, options[value]);
	}

	/**
	 * our generic select selenium functionality
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param value
	 *            : the select option to be selected
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 *             , InvalidLocatorTypeException
	 */
	public int select(Locators type, String locator, String value)
			throws InvalidActionException, InvalidLocatorTypeException {
		String action = "Selecting " + value + " in " + type + " " + locator;
		String expected = type + " " + locator
				+ " is present, displayed, and enabled to have the value "
				+ value + " selected";
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			output.recordAction(action, expected, "Unable to select " + type
					+ " " + locator + " as it is not present", Result.FAILURE);
			return 1; // indicates element not present
		}
		// wait for element to be displayed
		if (!isElementDisplayed(type, locator, false)) {
			waitForElementDisplayed(type, locator);
		}
		if (!isElementDisplayed(type, locator, false)) {
			output.recordAction(action, expected, "Unable to select " + type
					+ " " + locator + " as it is not displayed", Result.FAILURE);
			return 1; // indicates element not displayed
		}
		// wait for element to be enabled
		if (!isElementEnabled(type, locator, false)) {
			waitForElementEnabled(type, locator);
		}
		if (!isElementEnabled(type, locator, false)) {
			output.recordAction(action, expected, "Unable to select " + type
					+ " " + locator + " as it is not enabled", Result.FAILURE);
			return 1; // indicates element not enabled
		}
		WebElement element = getWebElement(type, locator);
		List<WebElement> allOptions = element
				.findElements(By.tagName("option"));
		for (WebElement option : allOptions) {
			if (option.getText().equals(value)) { // getAttribute("value")
				option.click();
			}
		}
		output.recordAction(action, expected, "Selected " + value + " in "
				+ type + " " + locator, Result.SUCCESS);
		return 0;
	}

	/**
	 * An extension of the basic Selenium action of 'moveToElement' This will
	 * scroll or move the page to ensure the element is visible
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int move(Locators type, String locator)
			throws InvalidActionException, InvalidLocatorTypeException {
		String action = "Moving screen to " + type + " " + locator;
		String expected = type + " " + locator
				+ " is now present on the visible page";
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			output.recordAction(action, expected, "Unable to move to " + type
					+ " " + locator + " as it is not present", Result.FAILURE);
			return 1; // indicates element not present
		}
		WebElement element = getWebElement(type, locator);
		Actions builder = new Actions(driver);
		builder.moveToElement(element);

		if (!isElementDisplayed(type, locator)) {
			output.recordAction(action, expected, type + " " + locator
					+ " is not present on visible page", Result.FAILURE);
			return 1; // indicates element not visible
		}
		output.recordAction(action, expected, type + " " + locator
				+ " is present on visible page", Result.SUCCESS);
		return 0; // indicates element successfully moved to
	}

	/**
	 * An extension of the basic Selenium action of 'moveToElement' This will
	 * scroll or move the page to ensure the element is visible
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param position
	 *            - how many pixels above the element to scroll to
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int move(Locators type, String locator, int position)
			throws InvalidActionException, InvalidLocatorTypeException {
		String action = "Moving screen to " + position + " pixels above "
				+ type + " " + locator;
		String expected = type + " " + locator
				+ " is now present on the visible page";
		// wait for element to be present
		if (!isElementPresent(type, locator, false)) {
			waitForElementPresent(type, locator);
		}
		if (!isElementPresent(type, locator, false)) {
			output.recordAction(action, expected, "Unable to move to " + type
					+ " " + locator + " as it is not present", Result.FAILURE);
			return 1; // indicates element not present
		}

		JavascriptExecutor jse = (JavascriptExecutor) driver;
		WebElement element = getWebElement(type, locator);
		int elementPosition = element.getLocation().getY();
		int newPosition = elementPosition - position;
		jse.executeScript("window.scrollBy(0, " + newPosition + ")");

		if (!isElementDisplayed(type, locator)) {
			output.recordAction(action, expected, type + " " + locator
					+ " is not present on visible page", Result.FAILURE);
			return 1; // indicates element not visible
		}
		output.recordAction(action, expected, type + " " + locator
				+ " is present on visible page", Result.SUCCESS);
		return 0; // indicates element successfully moved to
	}

	/**
	 * An custom script to scroll to a given position on the page
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int scroll(int position) throws InvalidActionException,
			InvalidLocatorTypeException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		Long initialPosition = (Long) jse
				.executeScript("return window.scrollY;");

		String action = "Scrolling page from " + initialPosition + " to "
				+ position;
		String expected = "Page is now set at position " + position;

		jse.executeScript("window.scrollBy(0, " + position + ")");

		Long newPosition = (Long) jse.executeScript("return window.scrollY;");

		output.recordAction(action, expected, "Page is now set at position "
				+ newPosition, Result.FAILURE);
		if (newPosition != position) {
			return 1; // indicates page didn't scroll properly
		}
		return 0; // indicates page didn't scroll properly
	}

	/**
	 * Some basic functionality for clicking 'OK' on an alert box
	 * 
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 */
	public int acceptAlert() {
		String action = "Clicking 'OK' on an alert";
		String expected = "Alert is present to be clicked";
		// wait for element to be present
		if (!isAlertPresent(false)) {
			waitForAlertPresent();
		}
		if (!isAlertPresent(false)) {
			output.recordAction(action, expected,
					"Unable to click alert as it is not present",
					Result.FAILURE);
			return 1; // indicates element not present
		}
		Alert alert = driver.switchTo().alert();
		alert.accept();
		driver.switchTo().defaultContent();
		output.recordAction(action, expected, "Clicked 'OK' on the alert",
				Result.SUCCESS);
		return 0;
	}

	/**
	 * Some basic functionality for clicking 'OK' on a confirmation box
	 * 
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 */
	public int acceptConfirmation() {
		String action = "Clicking 'OK' on a confirmation";
		String expected = "Confirmation is present to be clicked";
		// wait for element to be present
		if (!isConfirmationPresent(false)) {
			waitForConfirmationPresent();
		}
		if (!isConfirmationPresent(false)) {
			output.recordAction(action, expected,
					"Unable to click confirmation as it is not present",
					Result.FAILURE);
			return 1; // indicates element not present
		}
		Alert alert = driver.switchTo().alert();
		alert.accept();
		driver.switchTo().defaultContent();
		output.recordAction(action, expected,
				"Clicked 'OK' on the confirmation", Result.SUCCESS);
		return 0;
	}

	/**
	 * Some basic functionality for clicking 'Cancel' on a confirmation box
	 * 
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 */
	public int dismissConfirmation() {
		String action = "Clicking 'Cancel' on a confirmation";
		String expected = "Confirmation is present to be clicked";
		// wait for element to be present
		if (!isConfirmationPresent(false)) {
			waitForConfirmationPresent();
		}
		if (!isConfirmationPresent(false)) {
			output.recordAction(action, expected,
					"Unable to click confirmation as it is not present",
					Result.FAILURE);
			return 1; // indicates element not present
		}
		Alert alert = driver.switchTo().alert();
		alert.dismiss();
		driver.switchTo().defaultContent();
		output.recordAction(action, expected,
				"Clicked 'Cancel' on the confirmation", Result.SUCCESS);
		return 0;
	}

	/**
	 * Some basic functionality for clicking 'OK' on a prompt box
	 * 
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 */
	public int acceptPrompt() {
		String action = "Clicking 'OK' on a prompt";
		String expected = "Prompt is present to be clicked";
		// wait for element to be present
		if (!isPromptPresent(false)) {
			waitForPromptPresent();
		}
		if (!isPromptPresent(false)) {
			output.recordAction(action, expected,
					"Unable to click prompt as it is not present",
					Result.FAILURE);
			return 1; // indicates element not present
		}
		Alert alert = driver.switchTo().alert();
		alert.accept();
		driver.switchTo().defaultContent();
		output.recordAction(action, expected, "Clicked 'OK' on the prompt",
				Result.SUCCESS);
		return 0;
	}

	/**
	 * Some basic functionality for clicking 'Cancel' on a prompt box
	 * 
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 */
	public int dismissPrompt() {
		String action = "Clicking 'Cancel' on a prompt";
		String expected = "Prompt is present to be clicked";
		// wait for element to be present
		if (!isPromptPresent(false)) {
			waitForPromptPresent();
		}
		if (!isPromptPresent(false)) {
			output.recordAction(action, expected,
					"Unable to click prompt as it is not present",
					Result.FAILURE);
			return 1; // indicates element not present
		}
		Alert alert = driver.switchTo().alert();
		alert.dismiss();
		driver.switchTo().defaultContent();
		output.recordAction(action, expected, "Clicked 'Cancel' on the prompt",
				Result.SUCCESS);
		return 0;
	}

	/**
	 * Some basic functionality for typing text into a prompt box
	 * 
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 */
	public int typeIntoPrompt(String text) {
		String action = "Typing text '" + text + "' into prompt";
		String expected = "Prompt is present and enabled to have text " + text
				+ " typed in";
		// wait for element to be present
		if (!isPromptPresent(false)) {
			waitForPromptPresent();
		}
		if (!isPromptPresent(false)) {
			output.recordAction(action, expected,
					"Unable to type in prompt as it is not present",
					Result.FAILURE);
			return 1; // indicates element not present
		}
		Alert alert = driver.switchTo().alert();
		alert.sendKeys(text);
		driver.switchTo().defaultContent();
		output.recordAction(action, expected, "Typed text '" + text
				+ "' into prompt", Result.SUCCESS);
		return 0;
	}

	// //////////////////////////////////
	// extra base selenium functionality
	// //////////////////////////////////

	/**
	 * a method to determine selenium's By object using selenium webdriver
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return By: the selenium object
	 * @throws InvalidLocatorTypeException
	 */
	private By defineByElement(Locators type, String locator)
			throws InvalidLocatorTypeException {
		// TODO - consider adding strengthening
		By byElement;
		switch (type) { // determine which locator type we are interested in
		case xpath: {
			byElement = By.xpath(locator);
			break;
		}
		case id: {
			byElement = By.id(locator);
			break;
		}
		case name: {
			byElement = By.name(locator);
			break;
		}
		case classname: {
			byElement = By.className(locator);
			break;
		}
		case linktext: {
			byElement = By.linkText(locator);
			break;
		}
		case paritallinktext: {
			byElement = By.partialLinkText(locator);
			break;
		}
		case tagname: {
			byElement = By.tagName(locator);
			break;
		}
		default: {
			throw new InvalidLocatorTypeException(type
					+ " is not a valid locator type");
		}
		}
		return byElement;
	}

	/**
	 * a method to grab the first matching web element using selenium webdriver
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return WebElement: the element object, and all associated values with it
	 * @throws InvalidLocatorTypeException
	 */
	public WebElement getWebElement(Locators type, String locator)
			throws InvalidLocatorTypeException {
		By byElement = defineByElement(type, locator);
		WebElement query = driver.findElement(byElement); // grab our element
															// based on the
															// locator
		return query; // return our query
	}

	/**
	 * a method to grab all matching web elements using selenium webdriver
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return List<WebElement>: a list of element objects, and all associated
	 *         values with them
	 * @throws InvalidLocatorTypeException
	 */
	private List<WebElement> getWebElements(Locators type, String locator)
			throws InvalidLocatorTypeException {
		By byElement = defineByElement(type, locator);
		List<WebElement> query = driver.findElements(byElement); // grab our
																	// element
																	// based on
																	// the
																	// locator
		return query; // return our query
	}

	/**
	 * a method to obtain screenshots
	 * 
	 * @param imageName
	 *            : the name of the image typically generated via functions from
	 *            TestOutput.generateImageName
	 * @throws InvalidActionException
	 */
	public void takeScreenshot(String imageName) throws InvalidActionException {
		if (browser == Browsers.HtmlUnit) {
			return;
		}
		try {
			// take a screenshot
			File srcFile = new File("");
			if (System.getProperty("hubAddress") != "LOCAL") {
				WebDriver augemented = new Augmenter().augment(driver);
				srcFile = ((TakesScreenshot) augemented)
						.getScreenshotAs(OutputType.FILE);
			} else {
				srcFile = ((TakesScreenshot) driver)
						.getScreenshotAs(OutputType.FILE);
			}
			// now we need to save the file
			FileUtils.copyFile(srcFile, new File(imageName));
		} catch (Exception e) {
			System.out.println("Error taking screenshot:" + e.getMessage());
		}
	}

	// /////////////////////////////////////////////
	// a set of selenium checking functionality, used for building logic
	// ///////////////////////////////////////////

	/**
	 * a method to check if an alert is present
	 * 
	 * @return boolean - is an alert present
	 */
	public boolean isAlertPresent() {
		return isAlertPresent(false);
	}

	/**
	 * a method to check if an alert is present
	 * 
	 * @param print
	 *            - whether or not to print out this wait statement
	 * @return boolean - is an alert present
	 */
	public boolean isAlertPresent(boolean print) {
		boolean isPresent = false;
		try {
			driver.switchTo().alert();
			isPresent = true;
		} catch (NoAlertPresentException e) {
		}
		if (print) {
			output.recordExpected("Checking for alert to be present");
		}
		driver.switchTo().defaultContent();
		return isPresent;
	}

	/**
	 * a method for waiting up to 5 seconds for an alert is present
	 * 
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int waitForAlertPresent() {
		return waitForAlertPresent(5);
	}

	/**
	 * a method for waiting until an alert is present
	 * 
	 * @param seconds
	 *            : the number of seconds to wait
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int waitForAlertPresent(int seconds) {
		String action = "Wait up to " + seconds
				+ " seconds for an alert to be present";
		String expected = "An alert is present";
		// wait for up to XX seconds for our error message
		long end = System.currentTimeMillis() + (seconds * 1000);
		while (System.currentTimeMillis() < end) {
			try { // If results have been returned, the results are displayed in
					// a drop down.
				driver.switchTo().alert();
				break;
			} catch (NoAlertPresentException e) {
			}
		}
		double timetook = Math.min(
				(seconds * 1000) - (end - System.currentTimeMillis()),
				seconds * 1000);
		timetook = timetook / 1000;
		driver.switchTo().defaultContent();
		if (!isAlertPresent(false)) {
			output.recordAction(action, expected, "After waiting " + timetook
					+ " seconds, an alert is not present", Result.FAILURE);
			return 1;
		}
		output.recordAction(action, expected, "Waited " + timetook
				+ " seconds for an alert to be present", Result.SUCCESS);
		return 0;
	}

	/**
	 * a method to return the content of an alert
	 * 
	 * @return String - the content of an alert
	 */
	public String getAlert() {
		if (!isAlertPresent(false)) {
			waitForAlertPresent();
		}
		if (!isAlertPresent(false)) {
			return "";
		}
		Alert alert = driver.switchTo().alert();
		driver.switchTo().defaultContent();
		return alert.getText();
	}

	/**
	 * a method to check if a confirmation is present
	 * 
	 * @return boolean - is a confirmation present
	 */
	public boolean isConfirmationPresent() {
		return isConfirmationPresent(false);
	}

	/**
	 * a method to check if a confirmation is present
	 * 
	 * @param print
	 *            - whether or not to print out this wait statement
	 * @return boolean - is a confirmation present
	 */
	public boolean isConfirmationPresent(boolean print) {
		boolean isPresent = false;
		try {
			driver.switchTo().alert();
			isPresent = true;
		} catch (NoAlertPresentException e) {
		}
		if (print) {
			output.recordExpected("Checking for confirmation to be present");
		}
		driver.switchTo().defaultContent();
		return isPresent;
	}

	/**
	 * a method for waiting up to 5 seconds for a confirmation is present
	 * 
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int waitForConfirmationPresent() {
		return waitForConfirmationPresent(5);
	}

	/**
	 * a method for waiting until a confirmation is present
	 * 
	 * @param seconds
	 *            : the number of seconds to wait
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int waitForConfirmationPresent(int seconds) {
		String action = "Wait up to " + seconds
				+ " seconds for a confirmation to be present";
		String expected = "An alert is present";
		// wait for up to XX seconds for our error message
		long end = System.currentTimeMillis() + (seconds * 1000);
		while (System.currentTimeMillis() < end) {
			try { // If results have been returned, the results are displayed in
					// a drop down.
				driver.switchTo().alert();
				break;
			} catch (NoAlertPresentException e) {
			}
		}
		double timetook = Math.min(
				(seconds * 1000) - (end - System.currentTimeMillis()),
				seconds * 1000);
		timetook = timetook / 1000;
		driver.switchTo().defaultContent();
		if (!isConfirmationPresent(false)) {
			output.recordAction(action, expected, "After waiting " + timetook
					+ " seconds, a confirmation is not present", Result.FAILURE);
			return 1;
		}
		output.recordAction(action, expected, "Waited " + timetook
				+ " seconds for a confirmation to be present", Result.SUCCESS);
		return 0;
	}

	/**
	 * a method to return the content of a confirmation
	 * 
	 * @return String - the content of the confirmation
	 */
	public String getConfirmation() {
		if (!isConfirmationPresent(false)) {
			waitForConfirmationPresent();
		}
		if (!isConfirmationPresent(false)) {
			return "";
		}
		Alert alert = driver.switchTo().alert();
		driver.switchTo().defaultContent();
		return alert.getText();
	}

	/**
	 * a method to check if a prompt is present
	 * 
	 * @return boolean - is a prompt present
	 */
	public boolean isPromptPresent() {
		return isPromptPresent(false);
	}

	/**
	 * a method to check if a prompt is present
	 * 
	 * @param print
	 *            - whether or not to print out this wait statement
	 * @return boolean - is a prompt present
	 */
	public boolean isPromptPresent(boolean print) {
		boolean isPresent = false;
		try {
			driver.switchTo().alert();
			isPresent = true;
		} catch (NoAlertPresentException e) {
		}
		if (print) {
			output.recordExpected("Checking for prompt to be present");
		}
		driver.switchTo().defaultContent();
		return isPresent;
	}

	/**
	 * a method for waiting up to 5 seconds for a prompt is present
	 * 
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int waitForPromptPresent() {
		return waitForPromptPresent(5);
	}

	/**
	 * a method for waiting until a prompt is present
	 * 
	 * @param seconds
	 *            : the number of seconds to wait
	 * @return Integer - the number of errors encountered while executing these
	 *         steps
	 * @throws InvalidActionException
	 * @throws InvalidLocatorTypeException
	 */
	public int waitForPromptPresent(int seconds) {
		String action = "Wait up to " + seconds
				+ " seconds for a prompt to be present";
		String expected = "An alert is present";
		// wait for up to XX seconds for our error message
		long end = System.currentTimeMillis() + (seconds * 1000);
		while (System.currentTimeMillis() < end) {
			try { // If results have been returned, the results are displayed in
					// a drop down.
				driver.switchTo().alert();
				break;
			} catch (NoAlertPresentException e) {
			}
		}
		double timetook = Math.min(
				(seconds * 1000) - (end - System.currentTimeMillis()),
				seconds * 1000);
		timetook = timetook / 1000;
		driver.switchTo().defaultContent();
		if (!isPromptPresent(false)) {
			output.recordAction(action, expected, "After waiting " + timetook
					+ " seconds, a prompt is not present", Result.FAILURE);
			return 1;
		}
		output.recordAction(action, expected, "Waited " + timetook
				+ " seconds for a prompt to be present", Result.SUCCESS);
		return 0;
	}

	/**
	 * a method to return the content of a prompt
	 * 
	 * @return String - the content of the prompt
	 */
	public String getPrompt() {
		if (!isPromptPresent(false)) {
			waitForPromptPresent();
		}
		if (!isPromptPresent(false)) {
			return "";
		}
		Alert alert = driver.switchTo().alert();
		driver.switchTo().defaultContent();
		return alert.getText();
	}

	public boolean isCookiePresent(String expectedCookieName) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getCookieByName(String expectedCookieName) {
		// TODO Auto-generated method stub
		return "NOT YET IMPLEMENTED";
	}

	public boolean isOrdered(String firstObject, String secondObject) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSomethingSelected(Locators type, String locator) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getSelectedLabel(Locators type, String locator) {
		// TODO Auto-generated method stub
		return "NOT YET IMPLEMENTED";
	}

	public String[] getSelectedLabels(Locators type, String locator) {
		// TODO Auto-generated method stub
		return new String[] { "NOT YET IMPLEMENTED" };
	}

	/**
	 * a specialized selenium is text present in the page source functionality
	 * 
	 * @param expectedText
	 *            - the text we are expecting to be present on the page
	 * @return boolean - whether or not the text is present
	 */
	public boolean isTextPresentInSource(String expectedText) {
		return driver.getPageSource().contains(expectedText);
	}

	/**
	 * our generic selenium is text present functionality implemented
	 * 
	 * @param expectedText
	 *            - the text we are expecting to be present on the page
	 * @return boolean - whether or not the text is present
	 */
	public boolean isTextPresent(String expectedText) {
		String bodyText = driver.findElement(By.tagName("body")).getText();
		return bodyText.contains(expectedText);
	}

	/**
	 * our generic selenium get text from an element functionality implemented
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return String - the text of the element
	 */
	public String getText(Locators type, String locator)
			throws InvalidLocatorTypeException {
		WebElement element = getWebElement(type, locator);
		return element.getText();
	}

	
	/**
	 * our generic selenium get value from an element functionality implemented
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @return String - the text of the element
	 */
	public String getValue(Locators type, String locator) throws InvalidLocatorTypeException {
		WebElement element = getWebElement(type, locator);
		return element.getAttribute("value");
	}

	/**
	 * a function to return one css attribute of the provided element
	 * 
	 * @param type
	 *            - the locator type e.g. Locators.id, Locators.xpath
	 * @param locator
	 *            - the locator string e.g. login, //input[@id='login']
	 * @param attribute
	 *            - the css attribute to be returned
	 * @return String - the value of the css attribute
	 * @throws InvalidLocatorTypeException
	 */
	public String getCss(Locators type, String locator, String attribute)
			throws InvalidLocatorTypeException {
		WebElement element = getWebElement(type, locator);
		return element.getCssValue(attribute);
	}

	/**
	 * An extension of the selenium functionality to retrieve the current url of
	 * the application
	 * 
	 * @return String - current url
	 */
	public String getLocation() {
		return driver.getCurrentUrl();
	}

	/**
	 * An extension of the selenium functionality to retrieve the current title
	 * of the application
	 * 
	 * @return String - title
	 */
	public String getTitle() {
		return driver.getTitle();
	}

	/**
	 * An extension of the selenium functionality to retrieve the html source of
	 * the application
	 * 
	 * @return String - page source
	 */
	public String getHtmlSource() {
		return driver.getPageSource();
	}

	public String getEval(String javascriptFunction) {
		// TODO Auto-generated method stub
		return "NOT YET IMPLEMENTED";
	}
}
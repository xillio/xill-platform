/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.plugins.web.data;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This class represents NODE "pseudo" variable, it encapsulates basic operations.
 *
 * @author Ivor van der Hoog.
 */
public class NodeVariable implements WebVariable {
    private final WebDriver driver;
    private final WebElement element;

    /**
     * <p>
     * The constructor for the NodeVariable.
     * </p>
     * <p>
     * This class represents the NODE "pseudo" variable.
     * </p>
     *
     * @param driver  The {@link WebDriver} we're using.
     * @param element The {@link WebElement} we're representing the node of.
     */
    public NodeVariable(final WebDriver driver, final WebElement element) {
        this.driver = driver;
        this.element = element;
    }

    @Override
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public WebElement getElement() {
        return element;
    }

    @Override
    public String getTextPreview() {
        if (element != null) {
            return element.getAttribute("outerHTML");
        } else {
            return driver.getPageSource();
        }
    }
}

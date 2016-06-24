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
 * An adapter which represents a Page and encapsulates the selenium library.
 */
public class PageVariable implements WebVariable, AutoCloseable {
    private final WebDriver driver;
    private final WebElement element;
    private PhantomJSPool.Entity entity;

    /**
     * <p>
     * The constructor for the PageVariable
     * </p>
     * <p>
     * This class represents the Page "pseudo" variable.
     * </p>
     *
     * @param driver  The {@link WebDriver} we're using.
     * @param element The {@link WebElement} we're representing the node of.
     */
    public PageVariable(final WebDriver driver, final WebElement element) {
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

    /**
     * Sets the entity which generated the page variable
     *
     * @param entity The entity.
     */
    public void setEntity(final PhantomJSPool.Entity entity) {
        this.entity = entity;
    }

    @Override
    public void close() throws Exception {
        if (entity != null) {
            entity.close();
        }
    }

    @Override
    public String getTextPreview() {
        return driver.getPageSource();
    }
}

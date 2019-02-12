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
package nl.xillio.xill.plugins.engine.stubs;

import nl.xillio.engine.connector.dropbox.DropboxConnector;
import nl.xillio.engine.connector.dropbox.converters.XDIPProvider;
import nl.xillio.engine.connector.dropbox.services.DropboxObjectMapper;
import nl.xillio.engine.connector.dropbox.services.DropboxService;
import nl.xillio.xill.plugins.engine.services.ConverterScanner2;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DropboxConnectorStub extends DropboxConnector {
    @Inject
    public DropboxConnectorStub(ConverterScanner2 converterScanner) {
        super(converterScanner,
                new DropboxService(new XDIPProvider()),
                new DropboxObjectMapper());
    }
}

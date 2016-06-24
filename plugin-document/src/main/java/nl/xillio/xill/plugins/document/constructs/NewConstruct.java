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
package nl.xillio.xill.plugins.document.constructs;

import nl.xillio.xill.api.components.ExpressionBuilder;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This construct will build a udm document from input parameters.
 * It only assembles the structure and touches no persistence.
 *
 * @author Thomas Biesaart
 */
public class NewConstruct extends AbstractUDMConstruct {

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("contentType", ATOMIC),
                new Argument("currentVersion", OBJECT),
                new Argument("history", emptyList(), LIST)
        );
    }

    public MetaExpression process(MetaExpression contentType, MetaExpression currentVersion, MetaExpression history) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        result.put("contentType", ExpressionBuilder.fromValue(contentType.getStringValue()));
        result.put("source", buildHistory(currentVersion, history));
        result.put("target", buildHistory(currentVersion, history));

        return ExpressionBuilder.fromValue(result);
    }


    private MetaExpression buildHistory(MetaExpression currentVersion, MetaExpression versions) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        result.put("current", buildRevision(currentVersion));
        result.put("versions", buildRevisionList(versions));

        return ExpressionBuilder.fromValue(result);
    }

    private MetaExpression buildRevisionList(MetaExpression bodies) {
        if (bodies.getType() != ExpressionDataType.LIST) {
            throw new IllegalArgumentException("Invalid Structure: Document history must be a list of bodies");
        }

        List<MetaExpression> children = bodies.getValue();

        return ExpressionBuilder.fromValue(
                children.stream().map(this::buildRevision).collect(Collectors.toList())
        );
    }

    private MetaExpression buildRevision(MetaExpression body) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();

        if (body.getType() != ExpressionDataType.OBJECT) {
            throw new IllegalArgumentException("Unable to parse as a document body [" + body + "]");
        }

        Map<String, MetaExpression> bodyData = body.getValue();
        for (Map.Entry<String, MetaExpression> entry : bodyData.entrySet()) {
            if ("version".equals(entry.getKey()) || "_id".equals(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            } else {
                result.put(entry.getKey(), buildDecorator(entry.getValue()));
            }
        }
        
        return ExpressionBuilder.fromValue(result);
    }

    private MetaExpression buildDecorator(MetaExpression body) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();

        if (body.getType() != ExpressionDataType.OBJECT) {
            throw new IllegalArgumentException("Unable to parse decorator [" + body + "]");
        }

        body.<Map<String, MetaExpression>>getValue().entrySet().forEach(
                entry -> result.put(entry.getKey(), parseField(entry.getValue()))
        );

        return ExpressionBuilder.fromValue(result);
    }

    private MetaExpression parseField(MetaExpression value) {
        // Create copy
        return MetaExpression.parseObject(MetaExpression.extractValue(value));
    }
}

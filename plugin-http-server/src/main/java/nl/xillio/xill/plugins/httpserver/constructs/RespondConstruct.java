package nl.xillio.xill.plugins.httpserver.constructs;

import com.google.inject.Singleton;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.httpserver.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class RespondConstruct extends Construct {
    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("request", ExpressionDataType.OBJECT),
                new Argument("status", NULL, ExpressionDataType.ATOMIC),
                new Argument("headers", NULL),
                new Argument("body", NULL)
        );
    }

    private MetaExpression process(MetaExpression requestVar, MetaExpression statusVar, MetaExpression headersVar, MetaExpression bodyVar) {
        Request request = requestVar.getMeta(Request.class);
        try {
            Map<String, Object> headers = headersVar.getType() == OBJECT ? (Map) extractValue(headersVar) : new HashMap<>();
            InputStream body = bodyVar.isNull() ? null : bodyVar.getBinaryValue().getInputStream();
            int defaultStatus = body == null ? 204 : 200;
            int status = statusVar.isNull() ?
                    defaultStatus :
                    statusVar.getNumberValue().intValue();
            request.sendResponse(
                    status,
                    headers,
                    body
            );
            return TRUE;
        } catch (IOException e) {
            throw new OperationFailedException("write response", e.getMessage());
        }
    }
}

package io.ballerina.stdlib.mi;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BXml;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.module.core.SimpleMediator;
import org.wso2.carbon.module.core.SimpleMessageContext;

public class Mediator extends SimpleMediator {
    private String firstArgument = "arg7";
    private String secondArgument = "arg8";
    private String functionName = "foo";
    private static final Module module = new Module(Constants.ORG_NAME, Constants.MODULE_NAME, "1");
    private static final Runtime rt = Runtime.from(module);

    public Mediator() {
        System.out.println("Initializing Ballerina Mediator ....................");
        rt.init();
        rt.start();
    }

    public void mediate(SimpleMessageContext context) {
        Callback returnCallback = new Callback() {
            public void notifySuccess(Object result) {
                System.out.println("notifySuccess");
                System.out.println(result);
                context.setProperty(Constants.RESULT, result.toString());
            }

            public void notifyFailure(BError result) {
                System.out.println("notifyFailure");
                System.out.println(result);
                context.setProperty(Constants.RESULT, result);
            }
        };

        Object[] args = new Object[2];
        args[0] = StringUtils.fromString(firstArgument);
        args[1] = StringUtils.fromString(secondArgument);

        rt.invokeMethodAsync(functionName, returnCallback, args);
    }

    public BXml getBXmlParameter(SimpleMessageContext context, String parameterName) {
        return OMElementConverter.toBXml((OMElement) context.getProperty((String) context.getProperty(parameterName)));
    }
}

/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.mi.plugin;

import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

public class DynamicServiceRegisterAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        FunctionCallExpressionNode functionCallExpressionNode = (FunctionCallExpressionNode) context.node();
        if (!(functionCallExpressionNode.functionName() instanceof QualifiedNameReferenceNode qualifiedNameReferenceNode)) {
            return;
        }
        if (qualifiedNameReferenceNode.modulePrefix().text().equals("runtime") &&
                qualifiedNameReferenceNode.identifier().text().equals("registerListener")) {
            DiagnosticErrorCode diagnosticCode = DiagnosticErrorCode.DYNAMIC_SERVICE_REGISTER_NOT_ALLOWED;
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(diagnosticCode.diagnosticId(), diagnosticCode.message(),
                    DiagnosticSeverity.ERROR);
            context.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, functionCallExpressionNode.location()));
        }
    }

}

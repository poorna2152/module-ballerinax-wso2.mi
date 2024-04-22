package io.ballerina.stdlib.mi.plugin;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.mi.plugin.model.Component;
import io.ballerina.stdlib.mi.plugin.model.Connector;
import io.ballerina.stdlib.mi.plugin.model.Param;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class FunctionDefinitionAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {
    private static final Connector connector = new Connector();
    private boolean isFirstIteration = true;
    private File connectorFolder;

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        if (isFirstIteration) {
            Path connectorFolderPath = context.currentPackage().project().sourceRoot().resolve(Constants.CONNECTOR);
            connectorFolder = new File(connectorFolderPath.toUri());
            if (!connectorFolder.exists()) {
                connectorFolder.mkdir();
            }
            try {
                URI jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
                Utils.copyResources(getClass().getClassLoader(), connectorFolderPath, jarPath);
            } catch (IOException | URISyntaxException e ) {
                throw new RuntimeException(e);
            }
            isFirstIteration = false;
        }

        PackageDescriptor descriptor = context.currentPackage().manifest().descriptor();
        String orgName = descriptor.org().value();
        String moduleName = descriptor.name().value();
        String version = String.valueOf(descriptor.version().value());

        FunctionDefinitionNode node = (FunctionDefinitionNode) context.node();

        SemanticModel semanticModel = context.compilation().getSemanticModel(context.moduleId());
        if (semanticModel.symbol(node).isEmpty()) return;

        FunctionSymbol functionSymbol = (FunctionSymbol) semanticModel.symbol(node).get();
        Component component = new Component(functionSymbol.getName().get());
        int noOfParams = 0;
        if (functionSymbol.typeDescriptor().params().isPresent()) {
            noOfParams = functionSymbol.typeDescriptor().params().get().size();
        }

        for (int i = 0; i < noOfParams; i++) {
            if (functionSymbol.typeDescriptor().params().get().get(i).getName().isEmpty()) continue;
            Param param = new Param(Integer.toString(i), functionSymbol.typeDescriptor().params().get().get(i).getName().get());
            component.setParam(param);
        }

        Param functionName = new Param("FunctionName", functionSymbol.getName().get());
        Param sizeParam = new Param("Size", Integer.toString(noOfParams));
        Param orgParam = new Param("OrgName", orgName);
        Param moduleParam = new Param("ModuleName", moduleName);
        Param versionParam = new Param("Version", version);

        component.setParam(functionName);
        component.setParam(sizeParam);
        component.setParam(orgParam);
        component.setParam(moduleParam);
        component.setParam(versionParam);

        connector.setComponent(component);
        component.generateInstanceXml(connectorFolder);
        component.generateTemplateXml(connectorFolder);

        connector.generateInstanceXml(connectorFolder);
    }
}

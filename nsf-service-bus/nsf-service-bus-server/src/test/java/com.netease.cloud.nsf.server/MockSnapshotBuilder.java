package com.netease.cloud.nsf.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.cloud.nsf.server.resource.SnapshotBuilder;
import com.netease.cloud.nsf.service.TranslateService;
import com.netease.cloud.nsf.step.Step;
import nsb.route.ResourceOuterClass;
import nsb.route.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

public class MockSnapshotBuilder implements SnapshotBuilder {

    private ObjectMapper objectMapper;
    private TranslateService translateService;
    private Path jsonDirectoryPath;
    private Path xmlDirectoryPath;

    public MockSnapshotBuilder(ObjectMapper objectMapper, TranslateService translateService, Path jsonDirectoryPath, Path xmlDirectoryPath) {
        this.objectMapper = objectMapper;
        this.translateService = translateService;
        this.jsonDirectoryPath = jsonDirectoryPath;
        this.xmlDirectoryPath = xmlDirectoryPath;
    }

    @Override
    public Service.Resources build() {
        try {
            Service.Resources.Builder builder = Service.Resources.newBuilder();
            builder.addAllResources(generateFromStepJson(jsonDirectoryPath));
            builder.addAllResources(generateFromXml(xmlDirectoryPath));
            builder.setNonce(new Date().toString());
            return builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private List<ResourceOuterClass.Resource> generateFromStepJson(Path path) throws Exception {
        List<ResourceOuterClass.Resource> out = new ArrayList<>();
        List<String> jsonList = readFilesUnderPath(path, p -> p.getFileName().toString().endsWith("json"));
        for (String jsonItem : jsonList) {
            String xmlItem = translateService.translate(toStep(jsonItem));
            ResourceOuterClass.Metadata metadata = ResourceOuterClass.Metadata.newBuilder().setVersion(String.valueOf(xmlItem.hashCode())).build();
            out.add(ResourceOuterClass.Resource.newBuilder().setMetadata(metadata).setBody(xmlItem).build());
        }
        return out;
    }

    private List<ResourceOuterClass.Resource> generateFromXml(Path path) throws Exception {
        List<ResourceOuterClass.Resource> out = new ArrayList<>();
        List<String> xmlList = readFilesUnderPath(path, p -> p.getFileName().toString().endsWith("xml"));
        for (String xmlItem : xmlList) {
            ResourceOuterClass.Metadata metadata = ResourceOuterClass.Metadata.newBuilder().setVersion(String.valueOf(xmlItem.hashCode())).build();
            out.add(ResourceOuterClass.Resource.newBuilder().setMetadata(metadata).setBody(xmlItem).build());
        }
        return out;
    }

    private List<String> readFilesUnderPath(Path directory, Predicate<Path> filter) throws Exception {
        List<String> files = new ArrayList<>();
        Files.list(directory).forEach(item -> {
            try {
                if (item.toFile().isFile() && filter.test(item)) {
                    byte[] buf = Files.readAllBytes(item);
                    files.add(new String(buf));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return files;
    }

    private Step toStep(String json) throws Exception {
        return objectMapper.readValue(json, Step.class);
    }
}

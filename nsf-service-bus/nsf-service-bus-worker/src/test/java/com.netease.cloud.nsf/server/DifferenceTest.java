package com.netease.cloud.nsf.server;

import com.netease.cloud.nsf.resource.DefaultResourceManager;
import nsb.route.ResourceOuterClass;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/20
 **/
public class DifferenceTest {
    @Test
    public void test() throws Exception {
        ResourceOuterClass.Resource[] olds = new ResourceOuterClass.Resource[]{
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("7855").setVersion("1").build()).build(),
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("2477").setVersion("1").build()).build(),
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("446").setVersion("1").build()).build(),
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("1").setVersion("1").build()).build(),
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("0875").setVersion("1").build()).build(),
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("0").setVersion("1").build()).build(),
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("").setVersion("1").build()).build(),
        };
        ResourceOuterClass.Resource[] news = new ResourceOuterClass.Resource[]{
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("").setVersion("2").build()).build(),
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("0875").setVersion("6").build()).build(),
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("7777").setVersion("5").build()).build(),
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("2477").setVersion("67").build()).build(),
                ResourceOuterClass.Resource.newBuilder().setMetadata(ResourceOuterClass.Metadata.newBuilder().setName("9999").setVersion("5").build()).build(),
        };
        Method m = DefaultResourceManager.class.getDeclaredMethod("compare", ResourceOuterClass.Resource[].class, ResourceOuterClass.Resource[].class);
        m.setAccessible(true);
        Object obj = m.invoke(new DefaultResourceManager(null), olds, news);
        Field add = obj.getClass().getDeclaredField("add");
        Field update = obj.getClass().getDeclaredField("update");
        Field delete = obj.getClass().getDeclaredField("delete");
        add.setAccessible(true);
        update.setAccessible(true);
        delete.setAccessible(true);

        List<ResourceOuterClass.Resource> addResult = (List<ResourceOuterClass.Resource>) add.get(obj);
        List<ResourceOuterClass.Resource[]> updateResult = (List<ResourceOuterClass.Resource[]>) update.get(obj);
        List<ResourceOuterClass.Resource> deleteResult = (List<ResourceOuterClass.Resource>) delete.get(obj);

        Assert.assertEquals(2, addResult.size());
        Assert.assertEquals(3, updateResult.size());
        Assert.assertEquals(4, deleteResult.size());
    }
}

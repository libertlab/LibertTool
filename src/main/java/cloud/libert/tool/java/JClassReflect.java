package cloud.libert.tool.java;


import cloud.libert.tool.LibertToolException;

import java.util.Arrays;

public class JClassReflect {

    String mBasePackage = "";

    public JClassReflect(String basePackage) {
        mBasePackage = basePackage;
    }

    public void print(Object obj) {
        System.out.println(obj);
    }

    public void scanEntity(String className) {
        try {
            Class<?> obj = Class.forName(mBasePackage+".entity."+className);

            print(obj.getAnnotations());
            Arrays.asList(obj.getDeclaredFields()).stream().forEach((field)->{
                print(field);
            });

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) throws LibertToolException {
        String path = "D:/dev/java/work/jxm_web2020/src/main/java/com/jxm/web/db/ORMMapImpl.java";
        JClass parser = new JClass(path);

    }
}

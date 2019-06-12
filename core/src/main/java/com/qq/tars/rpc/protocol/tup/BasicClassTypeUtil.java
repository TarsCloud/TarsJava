/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.qq.tars.rpc.protocol.tup;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;

public class BasicClassTypeUtil {

    private static void addType(ArrayList<String> list, String type) {
        int point = type.length();
        while (type.charAt(point - 1) == '>') {
            point--;
            if (point == 0) {
                break;
            }
        }
        list.add(0, uni2JavaType(type.substring(0, point)));
    }

    public static ArrayList<String> getTypeList(String fullType) {
        ArrayList<String> type = new ArrayList<String>();
        int point = 0;
        int splitPoint = fullType.indexOf("<");
        int mapPoint = -1;
        while (point < splitPoint) {
            addType(type, fullType.substring(point, splitPoint));
            point = splitPoint + 1;
            splitPoint = fullType.indexOf("<", point);
            mapPoint = fullType.indexOf(",", point);
            if (splitPoint == -1) {
                splitPoint = mapPoint;
            }
            if (mapPoint != -1 && mapPoint < splitPoint) {
                splitPoint = mapPoint;
            }
        }
        addType(type, fullType.substring(point, fullType.length()));
        return type;
    }

    public static void main(String[] args) {
        ArrayList<String> src = new ArrayList<String>();
        src.add("char");
        src.add("list<char>");
        src.add("list<list<char>>");
        src.add("map<short,string>");
        src.add("map<double,map<float,list<bool>>>");
        src.add("map<int64,list<Test.UserInfo>>");
        src.add("map<short,Test.FriendInfo>");

        for (String ss : src) {
            ArrayList<String> list = getTypeList(ss);
            for (String s : list) {
                System.out.println(s);
            }
            Collections.reverse(list);
            System.out.println("-------------finished " + transTypeList(list));
        }
    }

    public static String transTypeList(ArrayList<String> listType) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listType.size(); i++) {
            listType.set(i, java2UniType(listType.get(i)));
        }
        Collections.reverse(listType);
        for (int i = 0; i < listType.size(); i++) {
            String type = listType.get(i);
            switch (type) {
                case "list":
                    listType.set(i - 1, "<" + listType.get(i - 1));
                    listType.set(0, listType.get(0) + ">");
                    break;
                case "map":
                    listType.set(i - 1, "<" + listType.get(i - 1) + ",");
                    listType.set(0, listType.get(0) + ">");
                    break;
                case "Array":
                    listType.set(i - 1, "<" + listType.get(i - 1));
                    listType.set(0, listType.get(0) + ">");
                    break;
            }
        }
        Collections.reverse(listType);
        for (String s : listType) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static Object createClassByUni(String className) throws ObjectCreateException {
        ArrayList<String> list = getTypeList(className);
        Object last = null;
        Object last2 = null;
        Object returnObject = null;
        for (String name : list) {
            returnObject = createClassByName(name);

            if (returnObject instanceof String) {
                if ("Array".equals((String) (returnObject))) {
                    if (last == null) {
                        returnObject = Array.newInstance(Byte.class, 0);
                    }
                } else if ("?".equals((String) (returnObject))) {

                } else {
                    if (last == null) {
                        last = returnObject;
                    } else {
                        last2 = last;
                        last = returnObject;
                    }
                }
            } else if (returnObject instanceof List) {
                if (null != last && last instanceof Byte) {
                    returnObject = Array.newInstance(Byte.class, 1);
                    Array.set(returnObject, 0, last);
                } else {
                    if (last != null) {
                        ((List) returnObject).add(last);
                    } else {

                    }
                    last = null;
                }
            } else if (returnObject instanceof Map) {
                if (last != null & last2 != null) {
                    ((Map) returnObject).put(last, last2);
                } else {

                }
                last = null;
                last2 = null;
            } else {
                if (last == null) {
                    last = returnObject;
                } else {
                    last2 = last;
                    last = returnObject;
                }
            }
        }
        return returnObject;
    }

    public static Object createClassByName(String name) throws ObjectCreateException {
        switch (name) {
            case "java.lang.Integer":
                return 0;
            case "java.lang.Boolean":
                return false;
            case "java.lang.Byte":
                return (byte) 0;
            case "java.lang.Double":
                return (double) 0;
            case "java.lang.Float":
                return (float) 0;
            case "java.lang.Long":
                return (long) 0;
            case "java.lang.Short":
                return (short) 0;
            case "java.lang.Character":
                throw new java.lang.IllegalArgumentException("can not support java.lang.Character");
            case "java.lang.String":
                return "";
            case "java.util.List":
                return new ArrayList();
            case "java.util.Map":
                return new HashMap();
            case "Array":
                return "Array";
            case "?":
                return name;
            default:
                Object result = null;
                try {
                    Class newoneClass = Class.forName(name);
                    Constructor cons = newoneClass.getConstructor();
                    result = cons.newInstance();
                } catch (Exception e) {
                    throw new ObjectCreateException(e);
                }
                return result;
        }
    }

    public static String java2UniType(String srcType) {
        switch (srcType) {
            case "java.lang.Integer":
            case "int":
                return "int32";
            case "java.lang.Boolean":
            case "boolean":
                return "bool";
            case "java.lang.Byte":
            case "byte":
                return "char";
            case "java.lang.Double":
            case "double":
                return "double";
            case "java.lang.Float":
            case "float":
                return "float";
            case "java.lang.Long":
            case "long":
                return "int64";
            case "java.lang.Short":
            case "short":
                return "short";
            case "java.lang.Character":
                throw new java.lang.IllegalArgumentException("can not support java.lang.Character");
            case "java.lang.String":
                return "string";
            case "java.util.List":
                return "list";
            case "java.util.Map":
                return "map";
            default:
                return srcType;
        }
    }

    public static String uni2JavaType(String srcType) {
        switch (srcType) {
            case "int32":
                return "java.lang.Integer";
            case "bool":
                return "java.lang.Boolean";
            case "char":
                return "java.lang.Byte";
            case "double":
                return "java.lang.Double";
            case "float":
                return "java.lang.Float";
            case "int64":
                return "java.lang.Long";
            case "short":
                return "java.lang.Short";
            case "string":
                return "java.lang.String";
            case "list":
                return "java.util.List";
            case "map":
                return "java.util.Map";
            default:
                return srcType;
        }
    }

    public static boolean isBasicType(String name) {
        switch (name) {
            case "int":
            case "boolean":
            case "byte":
            case "double":
            case "float":
            case "long":
            case "short":
            case "char":
            case "Integer":
            case "Boolean":
            case "Byte":
            case "Double":
            case "Float":
            case "Long":
            case "Short":
            case "Char":
                return true;
            default:
                return false;
        }
    }

    public static String getClassTransName(String name) {
        switch (name) {
            case "int":
                return "Integer";
            case "boolean":
                return "Boolean";
            case "byte":
                return "Byte";
            case "double":
                return "Double";
            case "float":
                return "Float";
            case "long":
                return "Long";
            case "short":
                return "Short";
            case "char":
                return "Character";
            default:
                return name;
        }
    }

    public static String getVariableInit(String name, String type) {
        switch (type) {
            case "int":
            case "double":
            case "float":
            case "long":
            case "short":
                return type + " " + name + "=0 ;\n";
            case "boolean":
                return type + " " + name + "=false ;\n";
            case "byte":
            case "char":
                return type + " " + name + " ;\n";
            default:
                return type + " " + name + " = null ;\n";
        }
    }
}

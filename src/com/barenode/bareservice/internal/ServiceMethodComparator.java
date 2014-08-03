package com.barenode.bareservice.internal;

import java.util.Comparator;


public class ServiceMethodComparator implements Comparator<ServiceMethod> {
	
    @Override
    public int compare(ServiceMethod method1, ServiceMethod method2) {
        String[] path1 = method1.getPath();
        String[] path2 = method2.getPath();
        if(path1.length == path2.length) {
            for(int i = 0; i < path1.length; i++) {
                if(path1[i] == null && path2[i] == null)
                    continue;
                if(path1[i] == null)
                    return 1;
                if(path2[i] == null)
                    return -1;
            }
            return 0;
        }
        else
            return path1.length - path2.length;
    }
}

//package com.apple.inc.user.util.threadLocal;
//
//public class ThreadContextDirectory {
//
//    private static final ThreadContextLocal local = new ThreadContextLocal();
//
//    public static ThreadContext get() {
//        return (ThreadContext) local.get();
//    }
//
//    public static void set(ThreadContext context) {
//        local.set(context);
//    }
//
//    public static void invalidateData() {
//        local.remove();
//        local.set((Object) null);
//    }
//}
//
//

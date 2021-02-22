package com.bjc.gulimall.search.thread;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/*  1. 创建异步对象
        CompletableFuture提供了4个静态的方法来创建一个异步操作
            没有返回结果的：
            1.1 public static CompletableFuture<Void> runAsync(Runnable runnable)
            1.2 public static CompletableFuture<Void> runAsync(Runnable runnable,Executor executor)
            带返回结果的；
            1.3 public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
            1.4 public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier,Executor executor)
        参数executor表示线程池，如果不传，那么使用默认的线程池
*
* */
public class CompletableFutureTest {

    // 创建一个10个线程的线程池  注意：一个系统应该只有有限的线程池，每个异步任务，都交给线程池，让其去执行
    public static ExecutorService service = Executors.newFixedThreadPool(10);

public static void main(String[] args) throws Exception {
    System.out.println("main start...");
    // 任务1
    CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("查询商品属性！");
        return "attr";
    },service);
    // 任务2
    CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("查询商品图片！");
        return "hello.jpg";
    },service);
    // 任务3
    CompletableFuture<String> futureInfo = CompletableFuture.supplyAsync(() -> {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("查询商品信息！");
        return "info";
    },service);
    CompletableFuture<Void> allOf = CompletableFuture.allOf(futureAttr, futureImg, futureInfo);
    // allOf.join();
    allOf.get();    // 等待所有结果完成
    System.out.println("main end...");
}

    private static void test04() throws InterruptedException, ExecutionException {
        // 任务1
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1启动了");
            return 10 / 5;
        });
        // 任务2
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2启动了");
            return 20;
        });

        CompletableFuture<String> future = future1.applyToEitherAsync(future2, res -> res + "_哈哈", service);
        String res = future.get();
        System.out.println("任务3的执行结果：" + res);
    }

    private static void test03() {
        // 任务1
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1启动了");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            return 10 / 5;
        });
        // 任务2
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            System.out.println("任务2启动了");
            return "xxx";
        });
        // 任务3
        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2.1启动了");
            return 20;
        });

        future1.acceptEitherAsync(future3,(res) -> {
            Integer newRes = res.intValue() * 5;
            System.out.println("任务3执行的结果：" + newRes);
        },service);
/*
    future1.runAfterEitherAsync(future2,()->{
        System.out.println("任务3启动了");
    },service);

    CompletableFuture<String> future = future1.thenCombineAsync(future2, (res1, res2) -> {
        return res1 + "_" + res2;
    }, service);
    System.out.println(future.get());

    future1.thenAcceptBothAsync(future2,(res1,res2)->{
        System.out.println(res1+res2);
    },service);

    // 任务1和任务2都完成了执行任务三
    future1.runAfterBothAsync(future1,()->{
        System.out.println("任务3启动了");
    },service);
    */
    }

    private static void test02() {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 10 / 0, service).handleAsync((res, exception) -> {
            if (null == exception) {
                return res * 2;
            } else {
                return -1;
            }
        });
    }

    private static void test01() {
        CompletableFuture<Integer> future =
                CompletableFuture.supplyAsync(() -> 10/0, service)
                        .whenComplete((res,exception) -> {
                            System.out.printf("执行的结果是%d,异常信息是%s\r\n",res,exception);
                        })
                        .exceptionally(e ->{
                            System.out.println(e);
                            return -1;
                        });
        ;
    }

    private static void supplyXXX() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("线程" + Thread.currentThread().getId() + "运行结果：" + i);
            return i;
        }, service);
        Integer i = future.get();
        System.out.println("i = " + i);
    }

    private static void runXX() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("线程" + Thread.currentThread().getId() + "运行结果：" + i);
        }, service);

        Void aVoid = future.get();
        System.out.println(aVoid);
    }

}

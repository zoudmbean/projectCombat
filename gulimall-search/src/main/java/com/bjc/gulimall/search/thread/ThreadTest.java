package com.bjc.gulimall.search.thread;

import java.util.concurrent.*;

/*
* 多线程复习
* 创建多线程的方式：
*   1）继承Thread
*   2）实现Runable接口
*   3）实现Callable接口 + FutureTask（可以拿到返回结果，可以处理异常）
*   4）线程池【ExecutorService】（5种）
*       4.1 创建
*           4.1.1 使用Executors工具类
*           4.1.2 使用原生 new ThreadPoolExecutor
*               ThreadPoolExecutor最完整的构造器的七大参数
                ThreadPoolExecutor pool = new ThreadPoolExecutor(
                        10,
                        1000,
                        10,
                        TimeUnit.SECONDS,
                        new LinkedBlockingDeque<>(10000),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy()
                );
*               1）corePoolSize：核心线程数，只要线程池不销毁，就一直存在（除非设置了允许核心线程超时设置allowCoreThreadTimeOut），线程池创建好之后，就准备就绪的线程数量，就等待来接受异步任务去执行
*               2）maximumPoolSize：最大线程数。用于控制资源并发
*               3）keepAliveTime：存活时间。如果当前线程数量 > 核心数量，线程空闲时间 > keepAliveTime指定的值，就会释放空闲的线程（非核心线程）
*               4）unit：存活时间单位
*               5）BlockingQueue<Runnable> workQueue：阻塞队列。如果任务很多，就会将目前多的任务放入队列中，只要有空闲的线程了，就会去队列中获取新的任务执行
*               6）threadFactory：线程的创建工厂。
*               7）RejectedExecutionHandler handler：如果队列满了，按照指定的拒绝策略拒绝执行任务。默认是丢弃策略（AbortPolicy 丢掉最新的任务）
*       4.2 工作顺序
*           1）线程池创建，准备好core数量的核心线程，准备接受任务
*           2）新的任务进来了，用core准备好的空闲线程执行
*               2.1 core满了，就将再进来的任务放入阻塞队列中，空闲的core就会自己去队列中获取任务执行
*               2.2 阻塞队列满了，就直接开辟新线程执行，最大只能开辟到maximumPoolSize数目的线程
*               2.3 max都执行好了，max-core数量的空闲线程会在keepAliveTime指定的时间后自动销毁，最终保持core大小
*               2.4 如果线程数开辟到了max的数量，还有新的任务进来，就会使用RejectedExecutionHandler指定的拒绝策略进行处理
*           3）所有的线程都由指定的factory创建。
*       注意：创建阻塞队列的时候，需要指定容量，因为默认的是Long的最大值，很容易将内存占满，因此，在实际开发中，我们也不会使用Executors创建线程池，特别是对于高并发的系统。
        面试题：
        一个线程池 core=7  max=20  queue=50，  100个并发进来怎么分配
        先有7个能直接得到执行，接下来的50个进入队列排队，在多开13个继续执行，现在70个都被安排上了，剩下30个默认拒绝策略
* * */
public class ThreadTest {
    // 创建一个10个线程的线程池  注意：一个系统应该只有有限的线程池，每个异步任务，都交给线程池，让其去执行
    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws Exception {
        System.out.println("main。。。");
        // 1. 1）继承Thread
        // new Thread01().start();

        // 2）实现Runable接口
        // new Thread(new Runable01()).start();

        // 3）实现Callable接口 + FutureTask（可以拿到返回结果，可以处理异常）
        /*
        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
        new Thread(futureTask).start();
        // get的作用：等待整个线程执行完成获取返回结果
        Integer integer = futureTask.get();
        System.out.println(integer);
        */
        // 注意：在实际开发中，以上三种方式都不会使用，因为上面的方式可能导致资源耗尽，实际开发中，我们都是使用线程池技术

        // 4) 线程池
        /*
        *   提交线程的两个方法：
        *       1）service.submit()：带返回值，参数可以是Runable和Callable
        *       2）service.execute()：不带返回值，参数是Runable
        * */
        //service.submit();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                10,
                1000,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );


        System.out.println("main。。。end");
    }

    public static class Thread01 extends Thread{
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2 ;
            System.out.println("线程"+Thread.currentThread().getId()+"运行结果：" + i);
        }
    }

    public static class Runable01 implements Runnable{

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2 ;
            System.out.println("线程"+Thread.currentThread().getId()+"运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer>{
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2 ;
            System.out.println("线程"+Thread.currentThread().getId()+"运行结果：" + i);
            return i;
        }
    }
}
